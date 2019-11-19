package io.logz.apollo.deployment;

import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class DeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    private final Duration RUNNING_DEPLOYMENT_TIMEOUT = Duration.ofHours(12);

    private final DeploymentDao deploymentDao;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public DeploymentService(DeploymentDao deploymentDao) {
        this.deploymentDao = deploymentDao;
    }

    @PostConstruct
    private void scheduleTask() {
        executor.scheduleWithFixedDelay(this::cancelStuckDeployments, 0L, 30, TimeUnit.MINUTES);
    }

    private void cancelStuckDeployments() {
        try {
            List<Integer> stuckDeployments = deploymentDao.getAllRunningDeployments().stream()
                    .filter(deployment -> deployment.getLastUpdate().toInstant().isBefore(Instant.now().minus(RUNNING_DEPLOYMENT_TIMEOUT)))
                    .map(Deployment::getId)
                    .collect(Collectors.toList());

            logger.warn("Found {} stuck deployments, cancelling the following ids: {}", stuckDeployments.size(), stuckDeployments);
            stuckDeployments.forEach(deploymentId -> deploymentDao.updateDeploymentStatus(deploymentId, Deployment.DeploymentStatus.CANCELED));
        } catch (Exception e) {
            logger.error("Got an error while trying to cancel stuck deployments", e);
        }
    }

    @PreDestroy
    private void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            logger.error("Got exception while trying to shutdown executor", e);
        }
    }
}
