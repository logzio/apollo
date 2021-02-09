package io.logz.apollo.deployment;

import io.logz.apollo.LockService;
import io.logz.apollo.auth.PermissionsValidator;
import io.logz.apollo.blockers.DeploymentBlocker;
import io.logz.apollo.blockers.RequestBlockerResponse;
import io.logz.apollo.controllers.DeploymentController;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.DeploymentPermissionDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.excpetions.ApolloDeploymentBlockedException;
import io.logz.apollo.excpetions.ApolloDeploymentConflictException;
import io.logz.apollo.excpetions.ApolloDeploymentException;
import io.logz.apollo.excpetions.ApolloDeploymentTooManyRequestsException;
import io.logz.apollo.excpetions.ApolloIllegalEnvironmentException;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.Service;
import io.logz.apollo.services.BlockerService;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class DeploymentHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentHandler.class);

    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final DeploymentPermissionDao deploymentPermissionDao;
    private final EnvironmentDao environmentDao;
    private final DeploymentDao deploymentDao;
    private final ServiceDao serviceDao;
    private final LockService lockService;
    private final BlockerService blockerService;

    @Inject
    public DeploymentHandler(KubernetesHandlerStore kubernetesHandlerStore,
                             DeploymentPermissionDao deploymentPermissionDao, EnvironmentDao environmentDao,
                             DeploymentDao deploymentDao, ServiceDao serviceDao, LockService lockService,
                             BlockerService blockerService) {
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.deploymentPermissionDao = requireNonNull(deploymentPermissionDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.deploymentDao = requireNonNull(deploymentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.lockService = requireNonNull(lockService);
        this.blockerService = requireNonNull(blockerService);
    }

    public Deployment addDeployment(int environmentId, int serviceId, int deployableVersionId, String deploymentMessage, Req req) throws ApolloDeploymentException {
        return addDeployment(environmentId, serviceId, deployableVersionId, deploymentMessage, "", Optional.empty(), req);
    }

    public Deployment addDeployment(int environmentId, int serviceId, int deployableVersionId, String deploymentMessage, Optional<Group> group, Req req) throws ApolloDeploymentException {
        String groupName = group.isPresent() ? group.get().getName() : "";
        return addDeployment(environmentId, serviceId, deployableVersionId, deploymentMessage, groupName, group, req);
    }

    public Deployment addDeployment(int environmentId, int serviceId, int deployableVersionId, String deploymentMessage, String groupName, Optional<Group> group, Req req) throws ApolloDeploymentException {
        return addDeployment(environmentId, serviceId, deployableVersionId, deploymentMessage, groupName, group, false, req);
    }

    public Deployment addDeployment(int environmentId, int serviceId, int deployableVersionId, String deploymentMessage, String groupName, Optional<Group> group, Boolean isEmergencyDeployment, Req req) throws ApolloDeploymentException {
        // Get the username from the token
        String userEmail = req.token().get("_user").toString();
        String sourceVersion = null;

        if (isEmergencyDeployment == null) {
            isEmergencyDeployment = false;
        }

        Environment environment = environmentDao.getEnvironment(environmentId);
        try {
            // Get the current commit sha from kubernetes so we can revert if necessary
            Service service = serviceDao.getService(serviceId);
            KubernetesDeploymentStatus kubernetesDeploymentStatus;

            if (group.isPresent()) {
                kubernetesDeploymentStatus = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment).getCurrentStatus(service, Optional.of(group.get().getName()));
            } else {
                kubernetesDeploymentStatus = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment).getCurrentStatus(service);
            }

            if (kubernetesDeploymentStatus != null)
                sourceVersion = kubernetesDeploymentStatus.getGitCommitSha();

        } catch (Exception e) {
            logger.error("Got exception while getting the current gitCommitSha from kubernetes. That means no revert.", e);
        }

        MDC.put("environmentId", String.valueOf(environmentId));
        MDC.put("serviceId", String.valueOf(serviceId));
        MDC.put("deployableVersionId", String.valueOf(deployableVersionId));
        MDC.put("userEmail", userEmail);
        MDC.put("sourceVersion", sourceVersion);
        MDC.put("groupName", groupName);
        MDC.put("isEmergencyDeployment", String.valueOf(isEmergencyDeployment));

        logger.info("Got request for a new deployment");

        if (!environment.getIsActive()) {
            logger.info("Environment {} is not active. Cannot deploy to this environment", environment.getName());
            throw new ApolloIllegalEnvironmentException("Cannot deploy. Target environment is not active.");
        }

        List<DeploymentPermission> userPermissions = deploymentPermissionDao.getPermissionsByUser(userEmail);
        if (!PermissionsValidator.isAllowedToDeploy(serviceId, environmentId, userPermissions)) {
            logger.info("User is not authorized to perform this deployment!");
            throw new ApolloDeploymentBlockedException("Not Authorized!");
        }

        boolean hasSingleRegionBlocker = blockerService.hasSingleRegionBlocker(serviceId);
        String lockName = hasSingleRegionBlocker ? lockService.getDeploymentLockName(serviceId, environment.getAvailability()) :
                lockService.getDeploymentLockName(serviceId, environmentId);

        if (!lockService.getAndObtainLock(lockName)) {
            logger.warn("A deployment request of this sort is currently being run");
            throw new ApolloDeploymentTooManyRequestsException("A deployment request is currently running for this service and environment! Wait until its done");
        }

        try {
            logger.info("Permissions verified, Checking that no other deployment is currently running");

            Optional<Deployment> runningDeployment = deploymentDao.getAllRunningDeployments()
                    .stream()
                    .filter(deployment ->
                                    deployment.getServiceId() == serviceId &&
                                            deployment.getEnvironmentId() == environmentId &&
                                            (!group.isPresent() || deployment.getGroupName().equals(group.get().getName())))
                    .findAny();

            if (runningDeployment.isPresent()) {
                logger.warn("There is already a running deployment that initiated by {}. Can't start a new one",
                            runningDeployment.get().getUserEmail());
                throw new ApolloDeploymentConflictException("There is an on-going deployment for this service in this environment with this group");
            }

            Deployment newDeployment = new Deployment();
            newDeployment.setEnvironmentId(environmentId);
            newDeployment.setServiceId(serviceId);
            newDeployment.setDeployableVersionId(deployableVersionId);
            newDeployment.setUserEmail(userEmail);
            newDeployment.setStatus(Deployment.DeploymentStatus.PENDING);
            newDeployment.setSourceVersion(sourceVersion);
            newDeployment.setDeploymentMessage(deploymentMessage);
            newDeployment.setGroupName(groupName);
            newDeployment.setEmergencyDeployment(isEmergencyDeployment);
            newDeployment.setAvailability(environment.getAvailability());

            if (group.isPresent()) {
                newDeployment.setGroupName(group.get().getName());
                newDeployment.setDeploymentParams(group.get().getJsonParams());
            }

            logger.info("Checking for blockers");
            Optional<DeploymentBlocker> blocker = blockerService.shouldBlock(newDeployment);
            if (blocker.isPresent()) {
                logger.info("Deployment is blocked by {}", blocker.get().getName());
                throw new ApolloDeploymentTooManyRequestsException("Deployment is currently blocked by '" + blocker.get().getName() + "' of type '" + blocker.get().getTypeName() + "'");
            }

            logger.info("All checks passed. Running deployment");
            deploymentDao.addDeployment(newDeployment);
            return newDeployment;
        } finally {
            lockService.releaseLock(lockName);
        }
    }

    public void checkDeploymentShouldBeBlockedByRequestBlocker(List<Integer> serviceIds, int numOfEnvironments, String availability) throws ApolloDeploymentException {
        RequestBlockerResponse requestBlockerResponse = blockerService.checkDeploymentShouldBeBlockedBySingleRegionBlocker(serviceIds, numOfEnvironments, availability);
        if (requestBlockerResponse.isShouldBlock()) {
            logger.info("User is not allowed to perform this deployment!");

            String messagePrefix = "Deployment is currently blocked by '" + requestBlockerResponse.getBlockerName() + "' blocker -";

            logger.warn("attempt to deploy the service(s) {} to multiple environments on single request", requestBlockerResponse.getServiceIds());
            throw new ApolloDeploymentConflictException(messagePrefix + " you can not deploy requested services to multiple environments simultaneously.");
        }
    }

}