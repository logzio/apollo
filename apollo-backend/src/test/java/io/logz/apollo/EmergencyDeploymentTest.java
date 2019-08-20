package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.KubernetesMonitor;
import io.logz.apollo.models.Deployment;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class EmergencyDeploymentTest {

    private KubernetesMonitor kubernetesMonitor;
    private EnvironmentDao environmentDao;
    private DeploymentDao deploymentDao;
    private final ApolloTestClient apolloTestClient;

    public EmergencyDeploymentTest() throws ScriptException, IOException, SQLException {
        StandaloneApollo standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();

        kubernetesMonitor = standaloneApollo.getInstance(KubernetesMonitor.class);
        environmentDao = standaloneApollo.getInstance(EnvironmentDao.class);
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
    }

    @Test
    public void changingToEmergencyDeploymentWhileThereIsAnotherOngoingDeployment() throws Exception {
        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient);
        environmentDao.updateConcurrencyLimit(deployment.getEnvironmentId(), 1);

        deploymentDao.updateDeploymentStatus(deployment.getId(), Deployment.DeploymentStatus.STARTED);
        deployment = deploymentDao.getDeployment(deployment.getId());

        Deployment deployment1 = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environmentDao.getEnvironment(deployment.getEnvironmentId()));

        assertThat(kubernetesMonitor.isDeployAllowed(deployment1, environmentDao, deploymentDao)).isFalse();

        deploymentDao.updateEmergencyDeployment(deployment1.getId(), true);
        deployment1 = deploymentDao.getDeployment(deployment1.getId());

        assertThat(kubernetesMonitor.isDeployAllowed(deployment1, environmentDao, deploymentDao)).isTrue();
    }
}
