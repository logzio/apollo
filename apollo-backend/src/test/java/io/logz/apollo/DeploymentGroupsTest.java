package io.logz.apollo;

import io.logz.apollo.excpetions.ApolloDeploymentException;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.kubernetes.ApolloToKubernetesStore;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.RealDeploymentGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitGroup;
import static org.assertj.core.api.Assertions.assertThat;


public class DeploymentGroupsTest {

    private static ApolloTestClient apolloTestClient;
    private static ApolloToKubernetesStore apolloToKubernetesStore;

    @BeforeClass
    public static void init() throws Exception {
        StandaloneApollo standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        apolloToKubernetesStore = standaloneApollo.getInstance(ApolloToKubernetesStore.class);
    }

    @Test
    public void testApolloDeploymentGroupYaml() throws Exception {
        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        HashMap<String, Object> params = new HashMap<>();

        params.put("image", "great image");
        params.put("key", "such key");
        params.put("value", "much value");

        realDeploymentGenerator = new RealDeploymentGenerator("{{ image }}", "{{ key }}", "{{ value }}", 0, new JSONObject(params).toString());
        Deployment deployment = realDeploymentGenerator.getDeployment();
        Service service = apolloTestClient.getService(deployment.getServiceId());
        apolloTestClient.updateService(service.getId(), service.getName(), service.getDeploymentYaml(), service.getServiceYaml(), true);

        apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);

        assertImageName(apolloToKubernetes.getKubernetesDeployment(), "great image");
        assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(), "such key", "much value");
    }

    @Test
    public void testAddDeploymentForGroups() throws Exception {
        // Prepare groups
        Group goodGroup1 = createAndSubmitGroup(apolloTestClient);
        Group goodGroup2 = createAndSubmitGroup(apolloTestClient, apolloTestClient.getService(goodGroup1.getServiceId()).getId(),
                apolloTestClient.getEnvironment(goodGroup1.getEnvironmentId()).getId());
        Group groupWithDifferentServiceId = createAndSubmitGroup(apolloTestClient);

        String[] groupIdsArr = {"0", String.valueOf(goodGroup1.getId()), String.valueOf(goodGroup2.getId()), String.valueOf(groupWithDifferentServiceId.getId())};
        String groupIdsCsv = String.join(",", groupIdsArr);

        // Prepare deployment
        Service service = apolloTestClient.getService(goodGroup1.getServiceId());
        Environment environment = apolloTestClient.getEnvironment(goodGroup1.getEnvironmentId());

        DeployableVersion deployableVersion = ModelsGenerator.createDeployableVersion(service);
        deployableVersion.setId(apolloTestClient.addDeployableVersion(deployableVersion).getId());

        Deployment deployment = ModelsGenerator.createDeployment(service, environment, deployableVersion);

        // Create expected response object
        MultiDeploymentResponseObject expectedResponse = new MultiDeploymentResponseObject();
        expectedResponse.addSuccessful(goodGroup1.getId(), deployment);
        expectedResponse.addSuccessful(goodGroup2.getId(), deployment);
        expectedResponse.addUnsuccessful(0, new ApolloDeploymentException("Non existing group."));
        expectedResponse.addUnsuccessful(groupWithDifferentServiceId.getId(),new ApolloDeploymentException("The deployment service ID " + deployment.getServiceId() +
                " doesn't match the group service ID " + groupWithDifferentServiceId.getServiceId()));

        // Grant permissions
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(environment), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        // Get results
        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(deployment, groupIdsCsv);

        assertThat(result.getUnsuccessful().size()).isEqualTo(expectedResponse.getUnsuccessful().size());
        assertThat(result.getSuccessful().get(0).getGroupId())
                .isEqualTo(expectedResponse.getSuccessful().get(0).getGroupId());
        assertThat(result.getSuccessful().get(1).getGroupId())
                .isEqualTo(expectedResponse.getSuccessful().get(1).getGroupId());
    }

    private void assertImageName(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).contains(imageName);
    }

    private void assertDeploymentLabelExists(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String labelKey, String labelValue) {
        assertThat(deployment.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }
}
