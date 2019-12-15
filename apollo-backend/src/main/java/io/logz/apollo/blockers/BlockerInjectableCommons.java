package io.logz.apollo.blockers;

import io.logz.apollo.controllers.StackService;
import io.logz.apollo.dao.BlockerDefinitionDao;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.EnvironmentsStackDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.dao.ServicesStackDao;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.scm.GithubConnector;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 6/4/17.
 */
@Singleton
public class BlockerInjectableCommons {

    private final GithubConnector githubConnector;
    private final BlockerDefinitionDao blockerDefinitionDao;
    private final DeployableVersionDao deployableVersionDao;
    private final DeploymentDao deploymentDao;
    private final StackDao stackDao;
    private final EnvironmentsStackDao environmentsStackDao;
    private final ServicesStackDao servicesStackDao;
    private final ServiceDao serviceDao;
    private final EnvironmentDao environmentDao;
    private final StackService stackService;

    @Inject
    public BlockerInjectableCommons(GithubConnector githubConnector, BlockerDefinitionDao blockerDefinitionDao, DeployableVersionDao deployableVersionDao, DeploymentDao deploymentDao,
                                    StackDao stackDao, EnvironmentsStackDao environmentsStackDao, ServicesStackDao servicesStackDao, ServiceDao serviceDao, EnvironmentDao environmentDao,
                                    StackService stackService) {
        this.githubConnector = requireNonNull(githubConnector);
        this.blockerDefinitionDao = requireNonNull(blockerDefinitionDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.deploymentDao = requireNonNull(deploymentDao);
        this.stackDao = requireNonNull(stackDao);
        this.environmentsStackDao = requireNonNull(environmentsStackDao);
        this.servicesStackDao = requireNonNull(servicesStackDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.stackService = requireNonNull(stackService);
    }

    public GithubConnector getGithubConnector() {
        return githubConnector;
    }

    public BlockerDefinitionDao getBlockerDefinitionDao() { return blockerDefinitionDao; }

    public DeployableVersionDao getDeployableVersionDao() { return deployableVersionDao; }

    public DeploymentDao getDeploymentDao() { return deploymentDao; }

    public StackDao getStackDao() { return stackDao; }

    public EnvironmentsStackDao getEnvironmentsStackDao() { return environmentsStackDao; }

    public ServicesStackDao getServicesStackDao() { return servicesStackDao; }

    public ServiceDao getServiceDao() { return serviceDao; }

    public EnvironmentDao getEnvironmentDao() { return environmentDao; }

    public StackService getStackService() { return stackService; }
}
