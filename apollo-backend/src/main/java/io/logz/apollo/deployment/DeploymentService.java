package io.logz.apollo.deployment;

import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
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

    private final DeploymentDao deploymentDao;
    private final ScheduledExecutorService executor;

    @Inject
    public DeploymentService(DeploymentDao deploymentDao) {
        this.deploymentDao = deploymentDao;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    private void scheduleTask() {
        executor.scheduleWithFixedDelay(this::cancelStuckDeployments, 0L, 5, TimeUnit.MINUTES);
    }

    private void cancelStuckDeployments() {
        try {
            List<Deployment> stuckDeployments = deploymentDao.getAllRunningDeployments().stream()
                    .filter(deployment -> deployment.getLastUpdate().toInstant().isBefore(Instant.now().minus(Duration.ofHours(2))))
                    .collect(Collectors.toList());

            logger.warn("Found {} stuck deployments, cancelling the following ids: {}", stuckDeployments.size(), stuckDeployments);
            stuckDeployments.forEach(deployment -> deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.CANCELED));
        } catch (Exception e) {
            logger.error("Got an error while trying to cancel stuck deployments", e);
        }
    }
}
