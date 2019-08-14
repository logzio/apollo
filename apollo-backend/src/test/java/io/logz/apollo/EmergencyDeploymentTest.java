package io.logz.apollo;

import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.helpers.RealDeploymentGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.KubernetesMonitor;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class EmergencyDeploymentTest {

    private KubernetesMonitor kubernetesMonitor;
    private EnvironmentDao environmentDao;
    private DeploymentDao deploymentDao;
    private RealDeploymentGenerator realDeploymentGenerator;
    private Deployment deployment;

    public EmergencyDeploymentTest() throws ScriptException, IOException, SQLException {
        StandaloneApollo standaloneApollo = StandaloneApollo.getOrCreateServer();

        kubernetesMonitor = standaloneApollo.getInstance(KubernetesMonitor.class);
        environmentDao = standaloneApollo.getInstance(EnvironmentDao.class);
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        deployment = realDeploymentGenerator.getDeployment();
    }

    @Test
    public void testMonitorEmergencyDeploymentOnUnlimitedConcurrencyEnvironment() {
        Environment environment = realDeploymentGenerator.getEnvironment();
        environmentDao.updateConcurrencyLimit(environment.getId(), -1);
        Deployment EmergencyDeployment = realDeploymentGenerator.getDeployment();
        EmergencyDeployment.setEmergencyDeployment(true);

        assertThat(kubernetesMonitor.isDeployAllowed(deployment, environmentDao, deploymentDao)).isTrue();
    }

    @Test
    public void testMonitorEmergencyDeploymentOnUnlimitedConcurrencyEnvironmentWithOngoingDeployment() {
        Environment environment = realDeploymentGenerator.getEnvironment();

        RealDeploymentGenerator realDeploymentGeneratorLaterDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        realDeploymentGeneratorLaterDeployment.setEnvironment(environment);
        Deployment deployment = realDeploymentGeneratorLaterDeployment.getDeployment();
        deployment.setEmergencyDeployment(false);

        environmentDao.updateConcurrencyLimit(environment.getId(), -1);
        Deployment EmergencyDeployment = realDeploymentGenerator.getDeployment();
        EmergencyDeployment.setEmergencyDeployment(true);

        assertThat(kubernetesMonitor.isDeployAllowed(this.deployment, environmentDao, deploymentDao)).isTrue();
    }

    @Test
    public void testMonitorEmergencyDeploymentOnConcurrencyLimitedEnvironmentWithOngoingDeployment() throws ScriptException, IOException, SQLException {
        StandaloneApollo.getOrCreateServer();

        Environment environment = realDeploymentGenerator.getEnvironment();
        environmentDao.updateConcurrencyLimit(environment.getId(), 1);
        realDeploymentGenerator.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);
        Deployment startedDeployment = realDeploymentGenerator.getDeployment();
        startedDeployment.setEmergencyDeployment(false);

        RealDeploymentGenerator realDeploymentGeneratorLaterDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        realDeploymentGeneratorLaterDeployment.setEnvironment(environment);
        Deployment laterDeployment = realDeploymentGeneratorLaterDeployment.getDeployment();
        laterDeployment.setEmergencyDeployment(true);

        assertThat(kubernetesMonitor.isDeployAllowed(laterDeployment, environmentDao, deploymentDao)).isTrue();
    }

    @Test
    public void testMonitorRegularDeploymentOnConcurrencyLimitedEnvironmentWithOngoingDeployment() throws ScriptException, IOException, SQLException {
        StandaloneApollo.getOrCreateServer();

        Environment environment = realDeploymentGenerator.getEnvironment();
        environmentDao.updateConcurrencyLimit(environment.getId(), 1);
        realDeploymentGenerator.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);
        Deployment startedDeployment = realDeploymentGenerator.getDeployment();
        startedDeployment.setEmergencyDeployment(false);

        RealDeploymentGenerator realDeploymentGeneratorLaterDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        realDeploymentGeneratorLaterDeployment.setEnvironment(environment);
        Deployment laterDeployment = realDeploymentGeneratorLaterDeployment.getDeployment();
        laterDeployment.setEmergencyDeployment(false);

        assertThat(kubernetesMonitor.isDeployAllowed(laterDeployment, environmentDao, deploymentDao)).isFalse();
    }
}
