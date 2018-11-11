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
import io.logz.apollo.models.Service;
import org.junit.BeforeClass;
import org.junit.Test;
import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class UndeployedServicesTest {
    private static ApolloTestClient apolloTestClient;
    private static int prodEnvironmentId;
    private static int stagingEnvironmentId;
    private static DeploymentDao deploymentDao;
    private static DeployableVersionDao deployableVersionDao;
    private static StandaloneApollo standaloneApollo;
    private static Date date1;
    private static Date date2;
    private static int serviceId1;
    private static int serviceId2;
    private static int serviceId3;

    @BeforeClass
    public static void beforeClass() throws ApolloClientException, ScriptException, IOException, SQLException {
        apolloTestClient = Common.signupAndLogin();
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);
        initEnvironments();
        initServices();
        initDates();
    }

    private static void initEnvironments() throws ApolloClientException {
        prodEnvironmentId = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "prod").getId();
        stagingEnvironmentId = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, "staging").getId();
    }

    private static void initServices() throws ApolloClientException {
        serviceId1 = ModelsGenerator.createAndSubmitService(apolloTestClient).getId();
        serviceId2 = ModelsGenerator.createAndSubmitService(apolloTestClient).getId();
        serviceId3 = ModelsGenerator.createAndSubmitService(apolloTestClient).getId();
    }

    private static void initDates() {
        date1 = Date.from(LocalDateTime.now().minusDays(22).atZone(ZoneId.systemDefault()).toInstant());
        date2 = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    private void initTest(int environmentId, int serviceId, Date date1, Date date2) throws Exception {
        Service service = apolloTestClient.getService(serviceId);

        int deployableVersionId1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service).getId();
        deployableVersionDao.updateCommitDate(deployableVersionId1, date1);
        DeployableVersion deployableVersion1 = deployableVersionDao.getDeployableVersion(deployableVersionId1);

        int deployableVersionId2 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service).getId();
        deployableVersionDao.updateCommitDate(deployableVersionId2, date2);

        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, apolloTestClient.getEnvironment(environmentId), service, deployableVersion1);
        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.DONE);
    }

    private void initDeployToProductionEarlierThanLastUpdated(int serviceId) throws Exception {
       initTest(prodEnvironmentId, serviceId, date1, date2);
    }

    @Test
    public void deployToProductionEarlierThanLastUpdated() throws Exception {
        initDeployToProductionEarlierThanLastUpdated(serviceId1);
        assertThat(apolloTestClient.getUndeployedServicesInProductionEnvironments().contains(serviceId1));
    }

    private void initDeployToStagingDoNothing(int serviceId) throws Exception {
       initTest(stagingEnvironmentId, serviceId, date1, date2);
    }

    @Test
    public void deployToStagingDoNothing() throws Exception {
        initDeployToStagingDoNothing(serviceId2);
        assertThat(apolloTestClient.getUndeployedServicesInProductionEnvironments().contains(serviceId2));
    }

    private void initDeployToProductionSameTimeAsLastUpdated(int serviceId) throws Exception {
        initTest(stagingEnvironmentId, serviceId, date1, date1);
    }

    @Test
    public void deployToProductionSameTimeAsLastUpdated() throws Exception {
        initDeployToProductionSameTimeAsLastUpdated(serviceId3);
        assertThat(apolloTestClient.getUndeployedServicesInProductionEnvironments().contains(serviceId3));
    }
}
