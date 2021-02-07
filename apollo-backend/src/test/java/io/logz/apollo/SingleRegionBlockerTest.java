package io.logz.apollo;

import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.Service;
import org.junit.After;
import org.junit.Test;

import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitBlocker;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployableVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SingleRegionBlockerTest {

    private static ApolloTestAdminClient apolloTestAdminClient;
    private static BlockerDefinition blocker;

    @After
    public void after() throws ApolloClientException {
        blocker.setActive(false);
        apolloTestAdminClient.updateBlocker(blocker);
    }

    @Test
    public void testSingleRegionBlockerWithMultiEnvironmentsException() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();

        Service serviceToBeLimitToOneRegion = ModelsGenerator.createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, serviceToBeLimitToOneRegion);

        Environment env1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment env2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);

        blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION,"{}", null, serviceToBeLimitToOneRegion, null, env1.getAvailability());

        String envIdsCsv = String.valueOf(env1.getId()) + "," + String.valueOf(env2.getId());

        Exception exception = assertThrows(ApolloClientException.class, () -> {
            apolloTestClient.addDeployment(envIdsCsv, String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        });

        String exceptionMsg = exception.getMessage();
        assertTrue(exceptionMsg.contains("you can not deploy requested services to multiple environments simultaneously."));
    }

    @Test
    public void testSingleRegionBlockerWithServiceAlreadyRunInSpecificAvailabilityException() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();

        Service serviceToBeLimitToOneRegion = ModelsGenerator.createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = createAndSubmitDeployableVersion(apolloTestClient, serviceToBeLimitToOneRegion);

        final String availabilityProd = "PRODTest";
        final String availabilityStaging = "StagingTest";

        Environment env1 = ModelsGenerator.createEnvironment(availabilityProd, null);
        env1.setId(apolloTestClient.addEnvironment(env1).getId());

        Environment env2 = ModelsGenerator.createEnvironment(availabilityProd, null);
        env2.setId(apolloTestClient.addEnvironment(env2).getId());

        Environment env3 = ModelsGenerator.createEnvironment(availabilityStaging, null);

        env3.setId(apolloTestClient.addEnvironment(env3).getId());

        blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION, "{}", null, serviceToBeLimitToOneRegion, null, env1.getAvailability());

        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env1), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env2), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env3), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(String.valueOf(env1.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getSuccessful().size()).isEqualTo(1);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);

        assertThat(apolloTestClient.addDeployment(String.valueOf(env2.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId())
                           .getUnsuccessful().get(0).getException().getMessage().contains("' of type '" + BlockerTypeName.SINGLE_REGION + "'"));

        result = apolloTestClient.addDeployment(String.valueOf(env3.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getSuccessful().size()).isEqualTo(1);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);
    }

}
