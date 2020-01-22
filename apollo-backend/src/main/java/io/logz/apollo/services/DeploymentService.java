package io.logz.apollo.services;

import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
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

    @Inject
    public DeploymentService(ServiceDao serviceDao, DeployableVersionDao deployableVersionDao) {
        this.serviceDao = requireNonNull(serviceDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
    }

    public void logDeploymentDescription(Deployment deployment) {
        try {
            DeployableVersion deployableVersion = deployableVersionDao.getDeployableVersion(deployment.getDeployableVersionId());
            Service service = serviceDao.getService(deployment.getServiceId());
            MDC.put("markers", String.format("service-name:%s", service.getName()));
            logger.info("<a href='{}'>{} Deployed Commit</a>", deployableVersion.getCommitUrl(), deployment.getUserEmail());
        } finally {
            MDC.remove("markers");
        }
    }
}
