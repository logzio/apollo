package io.logz.apollo.services;

import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

@Singleton
public class DeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);

    private final ServiceDao serviceDao;
    private final DeployableVersionDao deployableVersionDao;
    private final EnvironmentDao environmentDao;

    @Inject
    public DeploymentService(ServiceDao serviceDao, DeployableVersionDao deployableVersionDao,EnvironmentDao environmentDao) {
        this.serviceDao = requireNonNull(serviceDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.environmentDao = requireNonNull(environmentDao);
    }

    public void logDeploymentDescription(Deployment deployment) {
        try {
            DeployableVersion deployableVersion = deployableVersionDao.getDeployableVersion(deployment.getDeployableVersionId());
            Service service = serviceDao.getService(deployment.getServiceId());
            Environment env = environmentDao.getEnvironment(deployment.getEnvironmentId());
            MDC.put("markers", String.format("service-name:%s", service.getName()));
            MDC.put("env", env.getAvailability());
            MDC.put("region", env.getGeoRegion());
            MDC.put("service", service.getName());
            MDC.put("commit", deployableVersion.getGitCommitSha());
            MDC.put("committer", deployableVersion.getCommitterName());
            logger.info("Apollo deployed the commit message = {} triggered by {}", deployableVersion.getCommitMessage(), deployment.getUserEmail());
            logger.info("<a href='{}'>{} Deployed commit</a>",deployableVersion.getCommitUrl(),deployment.getUserEmail());
        } finally {
            MDC.remove("markers");
            MDC.remove("env");
            MDC.remove("region");
            MDC.remove("service");
            MDC.remove("commit");
            MDC.remove("committer");
        }
    }
}