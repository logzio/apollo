package io.logz.apollo;

import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.Service;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitBlocker;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployableVersion;
import static org.assertj.core.api.Assertions.assertThat;

public class SingleRegionDeploymentBlockerTest {

    @Test
    public void testSingleRegionBlockerWithMultiEnvironmentsException() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        ApolloTestAdminClient apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();

        Service serviceToBeLimitToOneRegion = ModelsGenerator.createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, serviceToBeLimitToOneRegion);

        List<Integer> serviceIds = new ArrayList<Integer>() {{ add(serviceToBeLimitToOneRegion.getId()); }};
        BlockerDefinition blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION, getSingleRegionBlockerConfiguration(serviceIds), null, null, null, null);

        Environment env1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment env2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);

        String envIdsCsv = String.valueOf(env1.getId()) + "," + String.valueOf(env2.getId());

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(envIdsCsv, String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getUnsuccessful().get(0).getException().getMessage().contains("you can not deploy the next service on multi environments"));


        blocker.setActive(false);
        apolloTestAdminClient.updateBlocker(blocker);
    }

    @Test
    public void testSingleRegionBlockerWithServiceAlreadyRunInSpecificAvailabilityException() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        ApolloTestAdminClient apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();

        Service serviceToBeLimitToOneRegion = ModelsGenerator.createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = createAndSubmitDeployableVersion(apolloTestClient, serviceToBeLimitToOneRegion);

        final String availabilityProd = "PRODTest";
        final String availabilityStaging = "StagingTest";

        Environment env1 = ModelsGenerator.createEnvironment(availabilityProd, null);
        env1.setId(apolloTestClient.addEnvironment(env1).getId());

        Environment env2 = ModelsGenerator.createEnvironment(availabilityProd, null);
        env2.setId(apolloTestClient.addEnvironment(env2).getId());

        Environment env3 = ModelsGenerator.createEnvironment(availabilityStaging, null);;
        env3.setId(apolloTestClient.addEnvironment(env3).getId());

        List<Integer> serviceIds = new ArrayList<Integer>() {{ add(serviceToBeLimitToOneRegion.getId()); }};
        BlockerDefinition blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION, getSingleRegionBlockerConfiguration(serviceIds), null, null, null, env1.getAvailability());

        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env1), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env2), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env3), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(String.valueOf(env1.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getSuccessful().size()).isEqualTo(1);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);

        assertThat(apolloTestClient.addDeployment(String.valueOf(env2.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId())
                           .getUnsuccessful().get(0).getException().getMessage().contains("the service is involve on another deployment already"));

        result = apolloTestClient.addDeployment(String.valueOf(env3.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getSuccessful().size()).isEqualTo(1);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);

        blocker.setActive(false);
        apolloTestAdminClient.updateBlocker(blocker);

    }

    private String getSingleRegionBlockerConfiguration(List<Integer> serviceIds) {
        return "{\n" +
                "  \"serviceIds\":" + serviceIds.toString() +
                "}";
    }
}
