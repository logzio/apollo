package io.logz.apollo;

import io.logz.apollo.excpetions.ApolloDeploymentException;
import io.logz.apollo.helpers.Fabric8TestMethods;
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

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitEnvironment;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitGroup;
import static org.assertj.core.api.Assertions.assertThat;

public class DeploymentGroupsTest {

    private static ApolloTestClient apolloTestClient;
    private static ApolloToKubernetesStore apolloToKubernetesStore;
    private static ApolloToKubernetes apolloToKubernetes;
    private static RealDeploymentGenerator realDeploymentGenerator;

    @BeforeClass
    public static void init() throws Exception {
        StandaloneApollo standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        apolloToKubernetesStore = standaloneApollo.getInstance(ApolloToKubernetesStore.class);
    }

    @Test
    public void testApolloDeploymentGroupYaml() throws Exception {
        HashMap<String, Object> params = new HashMap<>();

        params.put("image", "great image");
        params.put("key", "such key");
        params.put("value", "much value");

        realDeploymentGenerator = new RealDeploymentGenerator("{{ image }}", "{{ key }}", "{{ value }}", 0, new JSONObject(params).toString());
        Deployment deployment = realDeploymentGenerator.getDeployment();
        Service service = apolloTestClient.getService(deployment.getServiceId());
        apolloTestClient.updateService(service.getId(), service.getName(), service.getDeploymentYaml(), service.getServiceYaml(), service.getIngressYaml(), true);

        apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);

        Fabric8TestMethods.assertImageNameContains(apolloToKubernetes.getKubernetesDeployment(), "great image");
        Fabric8TestMethods.assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(), "such key", "much value");
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

    @Test
    public void testGroupDeploymentWithAdditionalParams() throws Exception {
        // Create an environment with additional parameters
        HashMap<String, Object> environmentParams = new HashMap<>();

        String deploymentValue = "deployment_value";
        String serviceValue = "service_value";
        String ingressValue = "ingress_value";

        environmentParams.put(deploymentValue, deploymentValue);
        environmentParams.put(serviceValue, serviceValue);
        environmentParams.put(ingressValue, ingressValue);

        Environment environment = createAndSubmitEnvironment(apolloTestClient, new JSONObject(environmentParams).toString());

        Service service = ModelsGenerator.createAndSubmitService(apolloTestClient);
        service = apolloTestClient.updateService(service.getId(), service.getName(), service.getDeploymentYaml(), service.getServiceYaml(), service.getIngressYaml(), true);

        // Create a group with additional parameters
        HashMap<String, Object> groupParams = new HashMap<>();

        groupParams.put("image", "great image");
        groupParams.put("group_key", "such key");
        groupParams.put("group_value", "much value");

        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId());
        group.setJsonParams(new JSONObject(groupParams).toString());

        realDeploymentGenerator = new RealDeploymentGenerator
                ("{{ image }}", "{{ group_key }}", "{{ group_value }}", 0,
                        new JSONObject(groupParams).toString(), service, environment, group.getName(), true);

        Deployment deployment = realDeploymentGenerator.getDeployment();

        apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);

        Fabric8TestMethods.assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(),
                RealDeploymentGenerator.DEFAULT_DEPLOYMENT_LABLE_KEY, deploymentValue);
        Fabric8TestMethods.assertServiceLabelExists(apolloToKubernetes.getKubernetesService(),
                RealDeploymentGenerator.DEFAULT_SERVICE_LABLE_KEY, serviceValue);
        Fabric8TestMethods.assertIngressLabelExists(apolloToKubernetes.getKubernetesIngress(),
                RealDeploymentGenerator.DEFAULT_INGRESS_LABLE_KEY, ingressValue);

        Fabric8TestMethods.assertImageNameContains(apolloToKubernetes.getKubernetesDeployment(), "great image");
        Fabric8TestMethods.assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(), "such key", "much value");
    }
}
