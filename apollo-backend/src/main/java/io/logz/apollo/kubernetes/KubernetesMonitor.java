package io.logz.apollo.kubernetes;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.deployment.DeploymentEnvStatusManager;
import io.logz.apollo.excpetions.ApolloNotFoundException;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 11/21/16.
 */
@Singleton
public class KubernetesMonitor {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesMonitor.class);
    private static final int TIMEOUT_TERMINATION = 60;
    public static final String LOCAL_RUN_PROPERTY = "localrun";
    public static final int MINIMUM_CONCURRENCY_LIMIT = 1;

    private final DeploymentEnvStatusManager deploymentEnvStatusManager;
    private final ScheduledExecutorService scheduledExecutorService;
    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final ApolloConfiguration apolloConfiguration;
    private final EnvironmentDao environmentDao;
    private final DeploymentDao deploymentDao;
    private final ServiceDao serviceDao;
    private final GroupDao groupDao;

    @Inject
    public KubernetesMonitor(KubernetesHandlerStore kubernetesHandlerStore, ApolloConfiguration apolloConfiguration,
                             EnvironmentDao environmentDao, DeploymentDao deploymentDao, ServiceDao serviceDao,
                             GroupDao groupDao, DeploymentEnvStatusManager deploymentEnvStatusManager) {
        this.deploymentEnvStatusManager = requireNonNull(deploymentEnvStatusManager);
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.apolloConfiguration = requireNonNull(apolloConfiguration);
        this.environmentDao = requireNonNull(environmentDao);
        this.deploymentDao = requireNonNull(deploymentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("kubernetes-monitor-%d").build();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
    }

    @PostConstruct
    public void start() {
        try {
            if (isLocalRun()) {
                logger.info("Running in local-mode, kubernetes monitor thread is not up.");
                return;
            }

            logger.info("Starting kubernetes monitor thread");
            int monitorThreadFrequency = apolloConfiguration.getKubernetes().getMonitoringFrequencySeconds();
            scheduledExecutorService.scheduleWithFixedDelay(this::monitor, 0, monitorThreadFrequency, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Could not start kubernetes monitor thread! Bailing..", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (isLocalRun()) return;

        try {
            logger.info("Stopping kubernetes monitoring thread");
            scheduledExecutorService.shutdown();
            scheduledExecutorService.awaitTermination(TIMEOUT_TERMINATION, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Got interrupt while waiting for ordinarily termination of the monitoring thread, force close.");
            Thread.currentThread().interrupt();
        } finally {
            scheduledExecutorService.shutdownNow();
        }
    }

    public void monitor() {
        // Defensive try, just to make sure nothing will close our executor service
        try {
            deploymentDao.getAllRunningDeployments().forEach(deployment -> {

                Environment relatedEnv = environmentDao.getEnvironment(deployment.getEnvironmentId());
                KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(relatedEnv);

                Deployment returnedDeployment;

                switch (deployment.getStatus()) {
                    case PENDING:
                        if (isDeployedEnvironmentConcurrencyLimitPermitsDeployment(deployment)) {
                            returnedDeployment = kubernetesHandler.startDeployment(deployment);
                        } else {
                            logger.info("Environment {} concurrency limit reached, not starting new deployment {} until one is done.", deployment.getEnvironmentId(), deployment.getId());
                            returnedDeployment = deployment;
                        }
                        break;
                    case PENDING_CANCELLATION:
                        returnedDeployment = kubernetesHandler.cancelDeployment(deployment);
                        break;
                    default:
                        returnedDeployment = kubernetesHandler.monitorDeployment(deployment);
                        break;
                }

                deploymentDao.updateDeploymentStatus(deployment.getId(), returnedDeployment.getStatus());

                if (deployment.getStatus().equals(Deployment.DeploymentStatus.DONE) || deployment.getStatus().equals(Deployment.DeploymentStatus.CANCELED)) {
                    deploymentEnvStatusManager.updateDeploymentEnvStatus(deployment, deploymentEnvStatusManager.getDeploymentCurrentEnvStatus(deployment, kubernetesHandler));
                }
            });
        } catch (Exception e) {
            logger.error("Got unexpected exception in the monitoring thread! swallow and moving on..", e);
        }

        // Scaling factors
        try {
            groupDao.getAllRunningScalingOperations().forEach(group -> {
                Environment environment = environmentDao.getEnvironment(group.getEnvironmentId());
                KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
                try {
                    kubernetesHandler.setScalingFactor(serviceDao.getService(group.getServiceId()), group.getName(), group.getScalingFactor());
                    group.setScalingStatus(Group.ScalingStatus.DONE);
                    groupDao.updateGroupScalingStatus(group.getId(), Group.ScalingStatus.DONE);
                    logger.info("Updated k8s scaling factor for group " + group.getName() + " to " + group.getScalingFactor());
                } catch (ApolloNotFoundException e) {
                    // If there's no such deployment, we don't want to keep trying to change it's scaling factor, so we're setting the status to DONE.
                    group.setScalingStatus(Group.ScalingStatus.DONE);
                    groupDao.updateGroupScalingStatus(group.getId(), Group.ScalingStatus.DONE);
                    logger.error("Could not find Kubernetes deployment with service ID " + group.getServiceId() + " and group " + group.getName(), e);
                }
            });
        } catch (Exception e) {
            logger.error("Got unexpected exception in the scaling monitoring thread! swallow and moving on..", e);
        }
    }

    private boolean isDeployedEnvironmentConcurrencyLimitPermitsDeployment(Deployment deployment) {
        Integer concurrencyLimit = environmentDao.getEnvironment(deployment.getEnvironmentId()).getConcurrencyLimit();
        if (concurrencyLimit != null && concurrencyLimit >= MINIMUM_CONCURRENCY_LIMIT) {
            long startedDeploymentOnEnvironment = deploymentDao.getAllStartedDeployments()
                    .stream()
                    .filter(runningDeployment -> runningDeployment.getEnvironmentId() == deployment.getEnvironmentId())
                    .count();

            return startedDeploymentOnEnvironment < concurrencyLimit;
        }

        return true;
    }

    private boolean isLocalRun() {
        return Boolean.valueOf(System.getenv(LOCAL_RUN_PROPERTY)) || Boolean.valueOf(System.getProperty(LOCAL_RUN_PROPERTY));
    }
}
