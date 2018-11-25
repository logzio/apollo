package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.EnvironmentServiceGroupMap;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;
import org.junit.BeforeClass;
import org.junit.Test;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UndeployedServicesTest {
    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static DeploymentDao deploymentDao;
    private static DeployableVersionDao deployableVersionDao;
    private static Deployment deployment;
    private static DeployableVersion deployableVersion1;
    private static DeployableVersion deployableVersion2;
    private static final String environmentAvailability = "PROD";

    @BeforeClass
    public static void beforeClass() throws Exception {
        apolloTestClient = Common.signupAndLogin();
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);
        ModelsGenerator.createAndSubmitService(apolloTestClient);
    }

    public void initLatestDeploymentWhenLatestCommitTest(Environment environment, Service service, Group group) throws Exception {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);

        deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion1, group.getName());
        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.DONE);
    }

    private void initNoDeploymentAndCommitExistsTest(Environment environment, Service service, Group group) throws ApolloClientException {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);
    }

    private void initNewerCommitThanLatestDeploymentTest(Environment environment, Service service, Group group) throws Exception {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);

        Date laterDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion2 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion2.getId(), laterDateUtcZone);

        deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion1, group.getName());
        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.DONE);
    }

    @Test
    public void latestDeploymentWhenLatestCommit() throws Exception {
        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "prod1", environmentAvailability);
        Service service = ModelsGenerator.createAndSubmitService(apolloTestClient, true);
        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId(), "my-group1");

        initLatestDeploymentWhenLatestCommitTest(environment, service, group);

        List<EnvironmentServiceGroupMap> result = apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1);
        assertThat(result
                .stream()
                .anyMatch(
                        env -> env.getEnvironmentId().equals(environment.getId()) &&
                                env.getEnvironmentName().equals(environment.getName()) &&
                                env.getServiceGroupMap().containsKey(service.getName()) &&
                                env.getServiceGroupMap().get(service.getName()).stream().anyMatch(gName -> gName.equals(group.getName()))
                )).isFalse();
    }

    @Test
    public void noDeploymentAndCommitExists() throws Exception {
        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "prod2", environmentAvailability);
        Service service = ModelsGenerator.createAndSubmitService(apolloTestClient, true);
        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId(), "my-group2");

        initNoDeploymentAndCommitExistsTest(environment, service, group);

        List<EnvironmentServiceGroupMap> result = apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1);
        assertThat(result
                        .stream()
                        .anyMatch(
                                env -> env.getEnvironmentId().equals(environment.getId()) &&
                                        env.getEnvironmentName().equals(environment.getName()) &&
                                        env.getServiceGroupMap().containsKey(service.getName()) &&
                                        env.getServiceGroupMap().get(service.getName()).stream().anyMatch(gName -> gName.equals(group.getName()))
                        )).isTrue();

    }

    @Test
    public void NewerCommitThanLatestDeployment() throws Exception {
        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "prod3", environmentAvailability);
        Service service = ModelsGenerator.createAndSubmitService(apolloTestClient, true);
        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId(), "my-group3");

        initNewerCommitThanLatestDeploymentTest(environment, service, group);

        List<EnvironmentServiceGroupMap> result = apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1);
        assertThat(result
                .stream()
                .anyMatch(
                        env -> env.getEnvironmentId().equals(environment.getId()) &&
                                env.getEnvironmentName().equals(environment.getName()) &&
                                env.getServiceGroupMap().containsKey(service.getName()) &&
                                env.getServiceGroupMap().get(service.getName()).stream().anyMatch(gName -> gName.equals(group.getName()))
                )).isTrue();
    }
}