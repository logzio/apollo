package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.Fabric8TestMethods;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.RealDeploymentGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.kubernetes.ApolloToKubernetesStore;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.DeploymentHistory;
import io.logz.apollo.models.DeploymentHistoryDetails;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.Service;
import org.assertj.core.api.AbstractThrowableAssert;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployableVersion;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitDeployment;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitEnvironment;
import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitService;
import static io.logz.apollo.models.Deployment.DeploymentStatus.CANCELED;
import static io.logz.apollo.models.Deployment.DeploymentStatus.DONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by roiravhon on 1/5/17.
 */
public class DeploymentTest {

    private static ApolloTestClient apolloTestClient;
    private static ApolloToKubernetesStore apolloToKubernetesStore;
    private static StandaloneApollo standaloneApollo;

    @BeforeClass
    public static void init() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        apolloToKubernetesStore = standaloneApollo.getInstance(ApolloToKubernetesStore.class);
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
    public void testDeploymentHistory() throws Exception {
        int pageNumber = 1;
        int pageSize = 50;
        Boolean descending = true;

        Deployment testDeployment = createAndSubmitDeployment(apolloTestClient);

        String emptySearchTerm = null;
        String randomSearchTerm = Common.randomStr(25);
        String userEmail = apolloTestClient.getTestUser().getUserEmail();

        verifyGetAllHistoryDeployments(emptySearchTerm, testDeployment, pageNumber, pageSize, descending);
        verifyDeploymentsFilteringByNonCommonTerm(randomSearchTerm, pageNumber, pageSize, descending);
        verifyGetFilteredDeploymentsByEmail(userEmail, testDeployment, pageNumber, pageSize, descending);
    }

    private void verifyGetAllHistoryDeployments(String emptySearchTerm, Deployment testDeployment, int pageNumber, int pageSize, Boolean descending) throws ApolloClientException {

        DeploymentHistory deploymentsHistoryFromApi = apolloTestClient.getDeploymentsHistory(descending, pageNumber, pageSize, emptySearchTerm);
        assertThat(deploymentsHistoryFromApi.getRecordsTotal()).isEqualTo(deploymentsHistoryFromApi.getRecordsFiltered());
        assertThat(deploymentsHistoryFromApi.getData().size()).isEqualTo(deploymentsHistoryFromApi.getRecordsTotal());

        Optional<DeploymentHistoryDetails> data = deploymentsHistoryFromApi.getData().stream()
                .filter(deployment -> deployment.getId() == testDeployment.getId()).findFirst();

        verifyDeploymentDetails(testDeployment, data);
    }

    private void verifyDeploymentsFilteringByNonCommonTerm(String searchTerm, int pageNumber, int pageSize, Boolean descending) throws ApolloClientException {

        DeploymentHistory deploymentsHistoryFromApi = apolloTestClient.getDeploymentsHistory(descending, pageNumber, pageSize, searchTerm);
        assertThat(deploymentsHistoryFromApi.getRecordsTotal()).isNotEqualTo(deploymentsHistoryFromApi.getRecordsFiltered());
        assertThat(deploymentsHistoryFromApi.getData().size()).isEqualTo(deploymentsHistoryFromApi.getRecordsFiltered());
    }


    private void verifyGetFilteredDeploymentsByEmail(String userEmail, Deployment testDeployment, int pageNumber, int pageSize, Boolean descending) throws ApolloClientException {

        DeploymentHistory deploymentsHistoryFromApi = apolloTestClient.getDeploymentsHistory(descending, pageNumber, pageSize, userEmail);

        Optional<DeploymentHistoryDetails> data = deploymentsHistoryFromApi.getData().stream()
                .filter(deployment -> deployment.getUserEmail().equals(userEmail)).findFirst();

        verifyDeploymentDetails(testDeployment, data);
    }

    private void verifyDeploymentDetails(Deployment testDeployment, Optional<DeploymentHistoryDetails> data) throws ApolloClientException {

        String testEnvName = apolloTestClient.getEnvironment(testDeployment.getEnvironmentId()).getName();
        String testServiceName = apolloTestClient.getService(testDeployment.getServiceId()).getName();

        assertThat(data).isPresent();
        assertThat(data.get().getEnvironmentName()).isEqualTo(testEnvName);
        assertThat(data.get().getServiceName()).isEqualTo(testServiceName);
        assertThat(data.get().getDeployableVersionId()).isEqualTo(testDeployment.getDeployableVersionId());
        assertThat(data.get().getStatus().toString().equals(String.valueOf(Deployment.DeploymentStatus.PENDING))).isTrue();
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

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(envIdsCsv, serviceIdsCsv, deployableVersion1.getId(), false);

        assertThat(result.getSuccessful().size()).isEqualTo(4);
        assertThat(result.getUnsuccessful().size()).isEqualTo(0);

        Deployment deployment = result.getSuccessful().get(0).getDeployment();
        assertThat(apolloTestClient.getDeployment(deployment.getId())).isNotNull();
    }

    @Test
    public void testDeploymentWithAdditionalParams() throws Exception {
        HashMap<String, Object> params = new HashMap<>();

        String deploymentValue = "deployment_value";
        String serviceValue = "service_value";
        String ingressValue = "ingress_value";

        params.put("image", "great image");
        params.put(deploymentValue, deploymentValue);
        params.put(serviceValue, serviceValue);
        params.put(ingressValue, ingressValue);

        Environment env = createAndSubmitEnvironment(apolloTestClient, new JSONObject(params).toString());

        RealDeploymentGenerator realDeploymentGenerator = new RealDeploymentGenerator
                ("{{ image }}", null, null, 0, null, null, env, null, true);

        Deployment deployment = realDeploymentGenerator.getDeployment();

        ApolloToKubernetes apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);

        Fabric8TestMethods.assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(),
                RealDeploymentGenerator.DEFAULT_DEPLOYMENT_LABLE_KEY, deploymentValue);
        Fabric8TestMethods.assertServiceLabelExists(apolloToKubernetes.getKubernetesService(),
                RealDeploymentGenerator.DEFAULT_SERVICE_LABLE_KEY, serviceValue);
        Fabric8TestMethods.assertIngressLabelExists(apolloToKubernetes.getKubernetesIngress(),
                RealDeploymentGenerator.DEFAULT_INGRESS_LABLE_KEY, ingressValue);
    }

    @Test
    public void testDeploymentToInactiveEnvironment() throws Exception {
        Environment inactiveEnvironment = createAndSubmitEnvironment(apolloTestClient, false);
        Service service = createAndSubmitService(apolloTestClient);
        DeployableVersion deployableVersion = createAndSubmitDeployableVersion(apolloTestClient, service);

        AbstractThrowableAssert<?, ? extends Throwable> throwableAssert = assertThatThrownBy(
                () -> createAndSubmitDeployment(apolloTestClient, inactiveEnvironment, service, deployableVersion)
        );

        throwableAssert.isInstanceOf(Exception.class);
        throwableAssert.hasMessage("Cannot deploy. Target environment is not active.");
    }

    @Test
    public void testShouldDeploymentBeCanceled() throws Exception {
        DeploymentDao deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);

        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient);
        int id = deployment.getId();
        //Canceling a deployment that didn't passed the timeout
        assertThatThrownBy(() -> apolloTestClient.cancelDeployment(deploymentDao.getDeployment(id)))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Please be patient, the time has not expired yet");

        int timeoutInSeconds = standaloneApollo.getInstance(ApolloConfiguration.class).getCancelDeployment().getForceCancelTimeoutSeconds();

        Common.waitABit(timeoutInSeconds+1);
        //Canceling an expired deployment that passed the timeout
        deployment = apolloTestClient.cancelDeployment(deployment);
        assertThat(deployment.getStatus()).isEqualTo(CANCELED);

        deploymentDao.updateDeploymentStatus(deployment.getId(), DONE);
        //Canceling a deployment in a deterministic state
        assertThatThrownBy(() -> apolloTestClient.cancelDeployment(deploymentDao.getDeployment(id)))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Deployment is not stuck");
    }
}
