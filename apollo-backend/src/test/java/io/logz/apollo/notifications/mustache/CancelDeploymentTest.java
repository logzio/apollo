package io.logz.apollo.notifications.mustache;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.Deployment;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static io.logz.apollo.models.Deployment.DeploymentStatus.CANCELED;
import static org.assertj.core.api.Assertions.assertThat;

public class CancelDeploymentTest {

    private static StandaloneApollo standaloneApollo;
    private static DeploymentDao deploymentDao;

    @BeforeClass
    public static void beforeClass() throws ScriptException, IOException, SQLException {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
    }

    @Test
    public void tesIsDeploymentShouldBeCanceled() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient);

        Common.waitABit(1*5);

        assertThat(apolloTestClient.isDeploymentShouldBeCanceled(deployment.getId(), TimeUnit.SECONDS, 1)).isTrue();
        assertThat(apolloTestClient.isDeploymentShouldBeCanceled(deployment.getId(), TimeUnit.DAYS, 1)).isFalse();

        apolloTestClient.cancelDeployment(deployment);
        assertThat(deploymentDao.getDeployment(deployment.getId()).getStatus()).isEqualTo(CANCELED);
    }
}