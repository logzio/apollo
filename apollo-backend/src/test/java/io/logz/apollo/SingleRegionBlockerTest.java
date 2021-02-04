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
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitBlocker;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployableVersion;
import static org.assertj.core.api.Assertions.assertThat;

public class SingleRegionBlockerTest {

    private static ApolloTestAdminClient apolloTestAdminClient;
    private static BlockerDefinition blocker;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    @After
    public void after() throws ApolloClientException {
        blocker.setActive(false);
        apolloTestAdminClient.updateBlocker(blocker);
    }

    @AfterClass
    public static void cleanUp() {
        executorService.shutdownNow();
    }

    @Test
    public void testSingleRegionBlockerWithMultiEnvironmentsException() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();

        Service serviceToBeLimitToOneRegion = ModelsGenerator.createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, serviceToBeLimitToOneRegion);

        Environment env1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment env2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);

        blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION, "{}", null, serviceToBeLimitToOneRegion, null, env1.getAvailability());

        String envIdsCsv = env1.getId() + "," + env2.getId();

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(envIdsCsv, String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
        assertThat(result.getUnsuccessful().get(0).getException().getMessage()).contains("you can not deploy requested services to multiple environments simultaneously.");
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

        List<Environment> environments = Arrays.asList(env1, env2, env3);

        blocker = createAndSubmitBlocker(apolloTestAdminClient, BlockerTypeName.SINGLE_REGION, "{}", null, serviceToBeLimitToOneRegion, null, env1.getAvailability());

        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env1), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env2), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env3), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        List<Callable<MultiDeploymentResponseObject>> deploymentRequests = new ArrayList<>();

        environments.forEach(region -> {
            deploymentRequests.add(() -> {
                return apolloTestClient.addDeployment(String.valueOf(region.getId()), String.valueOf(serviceToBeLimitToOneRegion.getId()), deployableVersion.getId());
            });
        });

        List<Future<MultiDeploymentResponseObject>> futures = executorService.invokeAll(deploymentRequests);

        List<MultiDeploymentResponseObject> results = futures.stream()
                .map(this::futureGetUnchecked)
                .collect(Collectors.toList());

        List<MultiDeploymentResponseObject.SuccessfulDeploymentResponseObject> success = results.stream()
                .map(MultiDeploymentResponseObject::getSuccessful)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<MultiDeploymentResponseObject.UnsuccessfulDeploymentResponseObject> error = results.stream()
                .map(MultiDeploymentResponseObject::getUnsuccessful)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        assertThat(success.size()).isEqualTo(1);
        assertThat(error.size()).isEqualTo(2);
    }

    private MultiDeploymentResponseObject futureGetUnchecked(Future<MultiDeploymentResponseObject> multiDeploymentResponseObjectFuture) {
        try {
            return multiDeploymentResponseObjectFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
