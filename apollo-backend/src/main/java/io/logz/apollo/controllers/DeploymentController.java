package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.database.OrderDirection;
import io.logz.apollo.deployment.DeploymentHandler;
import io.logz.apollo.LockService;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.excpetions.ApolloDeploymentException;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.DeploymentHistoryDetails;
import io.logz.apollo.models.DeploymentHistory;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.DELETE;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

/**
 * Created by roiravhon on 1/5/17.
 */
@Controller
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);
    private static final String IDS_DELIMITER = ",";

    private final DeploymentDao deploymentDao;
    private final DeployableVersionDao deployableVersionDao;
    private final LockService lockService;
    private final DeploymentHandler deploymentHandler;
    private final EnvironmentDao environmentDao;

    @Inject
    public DeploymentController(DeploymentDao deploymentDao, DeployableVersionDao deployableVersionDao, LockService lockService, DeploymentHandler deploymentHandler, EnvironmentDao environmentDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.lockService = requireNonNull(lockService);
        this.deploymentHandler = requireNonNull(deploymentHandler);
        this.environmentDao = requireNonNull(environmentDao);
    }

    @LoggedIn
    @GET("/deployment")
    public List<Deployment> getAllDeployments() {
        return deploymentDao.getAllDeployments();
    }

    @LoggedIn
    @GET("/deployment/{id}")
    public Deployment getDeployment(int id) {
        return deploymentDao.getDeployment(id);
    }

    @LoggedIn
    @GET("/latest-deployments")
    public List<Deployment> getLatestDeployments() {
        return deploymentDao.getLatestDeployments();
    }

    @LoggedIn
    @GET("/latest-deployment/{serviceId}/{environmentId}")
    public Deployment getLatestDeploymentOfServiceInEnvironment(int serviceId, int environmentId) {
        return deploymentDao.getLatestDeploymentOfServiceAndEnvironment(serviceId, environmentId);
    }

    @LoggedIn
    @GET("/running-deployments")
    public List<Deployment> getRunningDeployments() {
        return deploymentDao.getAllRunningDeployments();
    }

    @LoggedIn
    @GET("/running-and-just-finished-deployments")
    public List<Deployment> getRunningAndJustFinishedDeployments() {
        return deploymentDao.getRunningAndJustFinishedDeployments();
    }

    @LoggedIn
    @GET("/deployment/{id}/envstatus")
    public String getDeploymentEnvStatus(int id) {
        return deploymentDao.getDeploymentEnvStatus(id);
    }

    @LoggedIn
    @POST("/deployment")
    public void addDeployment(String environmentIdsCsv, String serviceIdsCsv, int deployableVersionId, String deploymentMessage, String groupName, Boolean isEmergencyDeployment, Req req) {
        List<Integer> environmentIds = new ArrayList<>();
        List<Integer> serviceIds = new ArrayList<>();

        try {
            Splitter.on(IDS_DELIMITER).omitEmptyStrings().trimResults().split(environmentIdsCsv).forEach(id -> environmentIds.add(Integer.valueOf(id)));
            Splitter.on(IDS_DELIMITER).omitEmptyStrings().trimResults().split(serviceIdsCsv).forEach(id -> serviceIds.add(Integer.valueOf(id)));
        } catch (NumberFormatException e) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, e.getMessage());
            return;
        }

        MultiDeploymentResponseObject responseObject = new MultiDeploymentResponseObject();

        DeployableVersion deployableVersion = deployableVersionDao.getDeployableVersion(deployableVersionId);

        Map<String, List<Integer>> environmentsPerAvailability = environmentIds.stream().collect(groupingBy(id -> environmentDao.getEnvironment(id).getAvailability()));
        for (String availability : environmentsPerAvailability.keySet()) {
            try {
                deploymentHandler.checkDeploymentShouldBeBlockedByRequestBlocker(serviceIds, environmentsPerAvailability.get(availability).size(), availability);
            } catch (ApolloDeploymentException e) {
                responseObject.addUnsuccessful(e);
                assignJsonResponseToReq(req, HttpStatus.CREATED, responseObject);
                return;
            }
        }

        environmentIds.forEach(environmentId -> serviceIds.forEach(serviceId -> {
            DeployableVersion serviceDeployableVersion = deployableVersionDao.getDeployableVersionFromSha(deployableVersion.getGitCommitSha(), serviceId);

            if (serviceDeployableVersion == null) {
                responseObject.addUnsuccessful(environmentId, serviceId, new ApolloDeploymentException("DeployableVersion with sha" + deployableVersion.getGitCommitSha() + " is not applicable on service " + serviceId));
            } else {
                try {
                    Deployment deployment = deploymentHandler.addDeployment(environmentId, serviceId, serviceDeployableVersion.getId(), deploymentMessage, groupName, Optional.empty(), isEmergencyDeployment, req);
                    responseObject.addSuccessful(environmentId, serviceId, deployment);
                } catch (ApolloDeploymentException e) {
                    responseObject.addUnsuccessful(environmentId, serviceId, e);
                }
            }
        }));

        assignJsonResponseToReq(req, HttpStatus.CREATED, responseObject);
    }


    @LoggedIn
    @POST("/revert-deployment")
    public void revertDeployment(String environmentIdsCsv, String serviceIdsCsv) {
        logger.info("revertDeployment endpoint");
        return;
    }

    @LoggedIn
    @DELETE("/deployment/{id}")
    public void cancelDeployment(int id, Req req) {
        String lockName = lockService.getDeploymentCancelationName(id);
        try {
            if (!lockService.getAndObtainLock(lockName)) {
                logger.warn("A deployment cancel request is currently running for this deployment! Wait until its done");
                assignJsonResponseToReq(req, HttpStatus.TOO_MANY_REQUESTS, "A deployment cancel request is currently running for this deployment! Wait until its done");
                return;
            }

            // Get the username from the token
            String userEmail = req.token().get("_user").toString();

            MDC.put("userEmail", userEmail);
            MDC.put("deploymentId", String.valueOf(id));

            logger.info("Got request for a deployment cancellation");

            Deployment deployment = deploymentDao.getDeployment(id);

            // Check that the deployment is not already done, or canceled
            if (!deployment.getStatus().equals(Deployment.DeploymentStatus.DONE) && !deployment.getStatus().equals(Deployment.DeploymentStatus.CANCELED)) {
                logger.info("Setting deployment to status PENDING_CANCELLATION");
                deploymentDao.updateDeploymentStatus(id, Deployment.DeploymentStatus.PENDING_CANCELLATION);
                assignJsonResponseToReq(req, HttpStatus.ACCEPTED, "Deployment Canceled!");
            } else {
                logger.warn("Deployment is in status {}, can't cancel it now!", deployment.getStatus());
                assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "Can't cancel the deployment as it is not in a state that's allows canceling");
            }
        } finally {
            lockService.releaseLock(lockName);
        }
    }

    @LoggedIn
    @POST("/deployment-history")
    public void fetchPaginatedDeploymentHistory(Boolean descending, int pageNumber, int pageSize, String searchTerm, Req req) {
        String search = searchTerm != null ? "%" + searchTerm + "%" : null;
        OrderDirection orderDirection = descending ? OrderDirection.DESC : OrderDirection.ASC;
        int pageInitIndex = getPageInitIndex(pageNumber, pageSize);

        try {
            int recordsTotal = deploymentDao.getTotalDeploymentsCount();
            int recordsFiltered = deploymentDao.getFilteredDeploymentHistoryCount(search);
            List<DeploymentHistoryDetails> data = deploymentDao.filterDeploymentHistoryDetails(search, orderDirection, pageInitIndex, pageSize);

            DeploymentHistory deploymentHistory = new DeploymentHistory(pageNumber, pageSize, recordsTotal, recordsFiltered, data);

            assignJsonResponseToReq(req, HttpStatus.CREATED, deploymentHistory);
        } catch (NumberFormatException e) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private int getPageInitIndex(int pageNumber, int pageSize) {
        return (pageNumber - 1) * pageSize;
    }
}
