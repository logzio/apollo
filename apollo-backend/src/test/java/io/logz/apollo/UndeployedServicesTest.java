package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.EnvironmentServices;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class UndeployedServicesTest {
    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static DeploymentDao deploymentDao;
    private static DeployableVersionDao deployableVersionDao;
    private static ServiceDao serviceDao;
    private static GroupDao groupDao;
    private static Environment environment;
    private static Service service;
    private static Deployment deployment;
    private static DeployableVersion deployableVersion1;
    private static DeployableVersion deployableVersion2;
    private static Group group;
    private static final String environmentAvailability = "PROD";
    private static List<EnvironmentServices> expectedResult = new ArrayList<>();

    @BeforeClass
    public static void beforeClass() throws Exception {
        apolloTestClient = Common.signupAndLogin();
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);
        serviceDao = standaloneApollo.getInstance(ServiceDao.class);
        groupDao = standaloneApollo.getInstance(GroupDao.class);
        environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "prod", environmentAvailability);
        service = ModelsGenerator.createAndSubmitService(apolloTestClient);
        ModelsGenerator.createAndSubmitService(apolloTestClient);
        group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId());
        serviceDao.updateServiceIsPartOfGroup(service.getId(), true);
    }

    @After
    public void after() {
        expectedResult.clear();

        if(deployment != null) {
            deploymentDao.deleteDeployment(deployment.getId());
        }
        if(deployableVersion1 != null) {
            deployableVersionDao.deleteDeployableVersion(deployableVersion1.getId());
        }
        if(deployableVersion2 != null) {
            deployableVersionDao.deleteDeployableVersion(deployableVersion2.getId());
        }
    }

    private void initExpectedResult() {
        Map<Service, Optional<Group>> map = new HashMap<>();
        map.put(service, Optional.of(group));
        expectedResult.add(new EnvironmentServices(environment.getId(), environment.getName(), map));
    }

    public void initLatestDeploymentWhenLatestCommitTest() throws Exception {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);

        deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion1);
        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.DONE);
        deploymentDao.updateDeploymentGroupName(deployment.getId(), group.getName());
    }

    @Test
    public void latestDeploymentWhenLatestCommit() throws Exception {
        initLatestDeploymentWhenLatestCommitTest();
        assertThat(apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1).size()).isEqualTo(0);
    }

    private void initNoDeploymentAndCommitExistsTest() throws ApolloClientException {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);

        initExpectedResult();
    }

    @Test
    public void noDeploymentAndCommitExists() throws Exception {
        initNoDeploymentAndCommitExistsTest();
        assertThat(apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1)).isEqualTo(expectedResult);
    }

    private void initNewerCommitThanLatestDeploymentTest() throws Exception {
        Date currentDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion1.getId(), currentDateUtcZone);

        Date laterDateUtcZone = Date.from(LocalDateTime.now(ZoneId.of("UTC")).plusHours(2).atZone(ZoneId.systemDefault()).toInstant());
        deployableVersion2 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        deployableVersionDao.updateCommitDate(deployableVersion2.getId(), laterDateUtcZone);

        deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion1);
        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.DONE);

        initExpectedResult();
    }

    @Test
    public void NewerCommitThanLatestDeployment() throws Exception {
        initNewerCommitThanLatestDeploymentTest();
        assertThat(apolloTestClient.getUndeployedServicesByEnvironmentAvailability(environmentAvailability, TimeUnit.HOURS, 1)).isEqualTo(expectedResult);
    }
}