package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import org.junit.Test;

public class EmergencyRollbackTest {

    @Test
    public void testEmergencyRollbackWorkingWhenCrossingConcurrencyLimit() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, 1);

        Service service1 = ModelsGenerator.createAndSubmitService(apolloTestClient);
        Service service2 = ModelsGenerator.createAndSubmitService(apolloTestClient);

        DeployableVersion deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service1);
        DeployableVersion deployableVersion2 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service2);

        ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service1, deployableVersion1);

        ModelsGenerator.createAndSubmitEmergencyRollback(apolloTestClient, environment, service2, deployableVersion2);
    }

    @Test
    public void testRegularDeploymentIsBlockedWhenCrossingConcurrencyLimit() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient, 1);

        Service service1 = ModelsGenerator.createAndSubmitService(apolloTestClient);
        Service service2 = ModelsGenerator.createAndSubmitService(apolloTestClient);

        DeployableVersion deployableVersion1 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service1);
        DeployableVersion deployableVersion2 = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service2);

        ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service1, deployableVersion1);

//        Common.waitABit(60);

        ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service2, deployableVersion2);
    }
}
