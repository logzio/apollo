package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.Service;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployment;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitEnvironment;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitService;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployableVersion;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by roiravhon on 1/5/17.
 */
public class DeploymentTest {

    private static ApolloTestClient apolloTestClient;

    @BeforeClass
    public static void init() throws Exception {
        apolloTestClient = Common.signupAndLogin();
    }

    @Test
    public void testGetAndAddDeployment() throws Exception {

        Deployment testDeployment = createAndSubmitDeployment(apolloTestClient);

        Deployment returnedDeployment = apolloTestClient.getDeployment(testDeployment.getId());

        assertThat(returnedDeployment.getEnvironmentId()).isEqualTo(testDeployment.getEnvironmentId());
        assertThat(returnedDeployment.getServiceId()).isEqualTo(testDeployment.getServiceId());
        assertThat(returnedDeployment.getDeployableVersionId()).isEqualTo(testDeployment.getDeployableVersionId());
        assertThat(returnedDeployment.getUserEmail()).isEqualTo(apolloTestClient.getTestUser().getUserEmail());
        assertThat(returnedDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.PENDING);
        assertThat(returnedDeployment.getDeploymentMessage()).isNotBlank();
    }

    @Test
    public void testGetAllDeployments() throws Exception {

        Deployment testDeployment = createAndSubmitDeployment(apolloTestClient);

        Optional<Deployment> deploymentFromApi = apolloTestClient.getAllDeployments().stream()
                .filter(deployment -> deployment.getId() == testDeployment.getId()).findFirst();

        boolean found = false;

        if (deploymentFromApi.isPresent()) {
            if (deploymentFromApi.get().getEnvironmentId() == testDeployment.getEnvironmentId() &&
                    deploymentFromApi.get().getServiceId() == testDeployment.getServiceId() &&
                    deploymentFromApi.get().getDeployableVersionId() == testDeployment.getDeployableVersionId() &&
                    deploymentFromApi.get().getStatus().toString().equals(Deployment.DeploymentStatus.PENDING.toString())) {
                found = true;
            }
        }

        assertThat(found).isTrue();
    }

    @Test
    public void testSimultaneousDeployments() throws Exception {

        Deployment deployment1 = createAndSubmitDeployment(apolloTestClient);

        // Submit that again to verify we can't run the same one twice
        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(deployment1);
        assertThat(result.getUnsuccessful().size()).isEqualTo(1);
        assertThat(result.getSuccessful().size()).isEqualTo(0);

        // Just to make sure we are not blocking different deployments to run on the same time
        createAndSubmitDeployment(apolloTestClient);
    }

    @Test
    public void testMultiServiceDeployment() throws Exception {
        Environment env1 = createAndSubmitEnvironment(apolloTestClient);
        Environment env2 = createAndSubmitEnvironment(apolloTestClient);
        Service service1 = createAndSubmitService(apolloTestClient);
        Service service2 = createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion1 = createAndSubmitDeployableVersion(apolloTestClient, service1);
        createAndSubmitDeployableVersion(apolloTestClient, service2, deployableVersion1.getGithubRepositoryUrl(), deployableVersion1.getGitCommitSha());

        String envIdsCsv = String.valueOf(env1.getId()) + "," + String.valueOf(env2.getId());
        String serviceIdsCsv = String.valueOf(service1.getId()) + "," + String.valueOf(service2.getId());

        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env1), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(env2), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(envIdsCsv, serviceIdsCsv, deployableVersion1.getId());

        assertThat(result.getSuccessful().size()).isEqualTo(4);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);

        Deployment deployment = result.getSuccessful().get(0).getDeployment();
        assertThat(apolloTestClient.getDeployment(deployment.getId())).isNotNull();
    }
}
