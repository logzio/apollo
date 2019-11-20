package io.logz.apollo;

import com.google.common.net.UrlEscapers;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentListBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.RealDeploymentGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.kubernetes.ApolloToKubernetesStore;
import io.logz.apollo.kubernetes.KubernetesHandler;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.PodStatus;
import io.logz.apollo.models.Service;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitGroup;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by roiravhon on 2/7/17.
 */
public class KubernetesHandlerTest {

    private static final String LOG_MESSAGE_IN_POD = "test log message to search..";

    //    private static KubernetesMockClient kubernetesMockClient;
    private static RealDeploymentGenerator notFinishedDeployment;
    private static RealDeploymentGenerator finishedDeployment;
    private static RealDeploymentGenerator finishedDeploymentForEnvTest;
    private static RealDeploymentGenerator notFinishedCanceledDeployment;
    private static RealDeploymentGenerator finishedCanceledDeployment;
    private static RealDeploymentGenerator statusDeployment;
    private static RealDeploymentGenerator groupWithScalingFactorZeroDeployment;
    private static PodStatus podStatus;

    private static DeploymentDao deploymentDao;
    private static GroupDao groupDao;
    private static Group group;

    private static KubernetesHandler notFinishedDeploymentHandler;
    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;

    @ClassRule
    public static KubernetesServer server = new KubernetesServer();

    private static KubernetesClient client;

    private static final Logger logger = LoggerFactory.getLogger(KubernetesHandlerTest.class);


    @BeforeClass
    public static void initialize() throws ScriptException, IOException, SQLException, ApolloClientException {
        client = server.getClient();

        apolloTestClient = Common.signupAndLogin();

        // We need a server here
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        ApolloToKubernetesStore apolloToKubernetesStore = standaloneApollo.getInstance(ApolloToKubernetesStore.class);
        KubernetesHandlerStore kubernetesHandlerStore = standaloneApollo.getInstance(KubernetesHandlerStore.class);
        deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        groupDao = standaloneApollo.getInstance(GroupDao.class);

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Create deployments
        notFinishedDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        finishedDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        notFinishedCanceledDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        finishedCanceledDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        statusDeployment = new RealDeploymentGenerator("image", "key", "value", 0);
        groupWithScalingFactorZeroDeployment = new RealDeploymentGenerator("image", "key", "value", 0);

        // Prepare env for env_test
        group = createAndSubmitGroup(apolloTestClient, finishedDeployment.getEnvironment().getId());
        group.setEnvironmentId(finishedDeployment.getEnvironment().getId());
        groupDao.updateGroup(group);
        Service service = apolloTestClient.getService(group.getServiceId());
        apolloTestClient.updateService(service.getId(), service.getName(), service.getDeploymentYaml(), service.getServiceYaml(), service.getIngressYaml(), true);

        finishedDeploymentForEnvTest = new RealDeploymentGenerator("image", "key", "value", 0,
                null, apolloTestClient.getService(group.getServiceId()), null, group.getName());

        // Set mock endpoints
        setMockDeploymentStatus(notFinishedDeployment, false, apolloToKubernetesStore.getOrCreateApolloToKubernetes(notFinishedDeployment.getDeployment()));
        setMockDeploymentStatus(finishedDeployment, true, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedDeployment.getDeployment()));
        setMockDeploymentStatus(notFinishedCanceledDeployment, false, apolloToKubernetesStore.getOrCreateApolloToKubernetes(notFinishedCanceledDeployment.getDeployment()));
        setMockDeploymentStatus(finishedCanceledDeployment, true, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedCanceledDeployment.getDeployment()));
        setMockDeploymentStatus(statusDeployment, true, apolloToKubernetesStore.getOrCreateApolloToKubernetes(statusDeployment.getDeployment()));
        setMockDeploymentStatus(groupWithScalingFactorZeroDeployment, true, apolloToKubernetesStore.getOrCreateApolloToKubernetes(groupWithScalingFactorZeroDeployment.getDeployment()), null);
        setMockDeploymentStatus(finishedDeploymentForEnvTest, true, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedDeploymentForEnvTest.getDeployment()));

        // Setting a mock pod status
        podStatus = new PodStatus();
        podStatus.setName("mavet-pod-" + Common.randomStr(5));
        podStatus.setHostIp("1.1.1.1");
        podStatus.setPodIp("2.2.2.2");
        podStatus.setPhase("Dying");
        podStatus.setReason("Kaha");
        podStatus.setStartTime("Beginning of humanity");


        // Set the logs and status mock to all deployments
        setMockPodLogsAndStatus(notFinishedDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(notFinishedDeployment.getDeployment()));
        setMockPodLogsAndStatus(finishedDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedDeployment.getDeployment()));
        setMockPodLogsAndStatus(notFinishedCanceledDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(notFinishedCanceledDeployment.getDeployment()));
        setMockPodLogsAndStatus(finishedCanceledDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedCanceledDeployment.getDeployment()));
        setMockPodLogsAndStatus(statusDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(statusDeployment.getDeployment()));
        setMockPodLogsAndStatus(groupWithScalingFactorZeroDeployment, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(groupWithScalingFactorZeroDeployment.getDeployment()));
        setMockPodLogsAndStatus(finishedDeploymentForEnvTest, LOG_MESSAGE_IN_POD, podStatus, apolloToKubernetesStore.getOrCreateApolloToKubernetes(finishedDeploymentForEnvTest.getDeployment()));

        // Prepare deployment for env_test
        finishedDeploymentForEnvTest.getDeployment().setEnvironmentId(finishedDeployment.getEnvironment().getId());
        finishedDeploymentForEnvTest.setEnvironment(finishedDeployment.getEnvironment());
        deploymentDao.updateDeployment(finishedDeploymentForEnvTest.getDeployment());

        // Inject the client
        notFinishedDeploymentHandler = kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(notFinishedDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(finishedDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(notFinishedCanceledDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(finishedCanceledDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(statusDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(groupWithScalingFactorZeroDeployment.getEnvironment(), client);
        kubernetesHandlerStore.getOrCreateKubernetesHandlerWithSpecificClient(finishedDeploymentForEnvTest.getEnvironment(), client);

        // Since the mock library does not support "createOrReplace" we can't mock this phase (and its fine to neglect it since its fabric8 code)
        notFinishedDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);
        finishedDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);
        notFinishedCanceledDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.CANCELING);
        finishedCanceledDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.CANCELING);
        statusDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.DONE);
        groupWithScalingFactorZeroDeployment.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);
        finishedDeploymentForEnvTest.updateDeploymentStatus(Deployment.DeploymentStatus.STARTED);

        // TODO: This can cause test concurrency issues in case we will want to run this in parallel. In the current singleton nature of those tests, no other way unfortunately
        standaloneApollo.startKubernetesMonitor();
    }

    @Test
    public void testDeploymentMonitor() throws JSONException {
        // We need to wait at least 2 iterations of the monitoring thread + 1 buffer
        Common.waitABit(5);

        Deployment currentNotFinishedDeployment = deploymentDao.getDeployment(notFinishedDeployment.getDeployment().getId());
        Deployment currentFinishedDeployment = deploymentDao.getDeployment(finishedDeployment.getDeployment().getId());
        Deployment currentNotFinishedCanceledDeployment = deploymentDao.getDeployment(notFinishedCanceledDeployment.getDeployment().getId());
        Deployment currentFinishedCanceledDeployment = deploymentDao.getDeployment(finishedCanceledDeployment.getDeployment().getId());
        Deployment currentGroupWithScalingFactorZeroDeployment = deploymentDao.getDeployment(groupWithScalingFactorZeroDeployment.getDeployment().getId());

        assertThat(currentNotFinishedDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.STARTED);
        assertThat(currentFinishedDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.DONE);
        assertThat(currentNotFinishedCanceledDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.CANCELING);
        assertThat(currentFinishedCanceledDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.CANCELED);
        assertThat(currentGroupWithScalingFactorZeroDeployment.getStatus()).isEqualTo(Deployment.DeploymentStatus.DONE);

        // Test envStatus
        JSONObject envStatusFromObjectJson = new JSONObject(currentFinishedDeployment.getEnvStatus().replaceAll("\\\\", ""));
        JSONObject expectedEnvStatusJson = new JSONObject(getExpectedEnvStatus(currentFinishedDeployment));

        JSONAssert.assertEquals(expectedEnvStatusJson, envStatusFromObjectJson, false);
    }

    @Test
    public void testDeploymentMonitorScalingUpdates() throws ApolloClientException {

        Common.waitABit(3);

        int newScalingFactor = group.getScalingFactor() + 1;
        apolloTestClient.updateScalingFactor(group.getId(), newScalingFactor);
        Group updatedGroupBefore = groupDao.getGroup(group.getId());

        assertThat(updatedGroupBefore.getScalingStatus()).isEqualByComparingTo(Group.ScalingStatus.PENDING);
        assertThat(updatedGroupBefore.getScalingFactor()).isEqualTo(newScalingFactor);

        Common.waitABit(3);

        Group updatedGroupAfter = groupDao.getGroup(group.getId());
        // TODO: Make the mocks work for a real test
        // assertThat(updatedGroupAfter.getScalingStatus()).isEqualByComparingTo(Deployment.DeploymentStatus.DONE);
    }

    @Test
    public void testGetStatus() {

        KubernetesDeploymentStatus returnedDeploymentStatus = notFinishedDeploymentHandler.getCurrentStatus(notFinishedDeployment.getService());
        PodStatus returnedPodStatus = returnedDeploymentStatus.getPodStatuses().stream().findFirst().orElse(null);

        assertThat(returnedPodStatus).isNotNull();

        assertThat(returnedDeploymentStatus.getAvailableReplicas()).isEqualTo(1);
        assertThat(returnedDeploymentStatus.getReplicas()).isEqualTo(1);
        assertThat(returnedDeploymentStatus.getUnavailableReplicas()).isEqualTo(0);
        assertThat(returnedDeploymentStatus.getUpdatedReplicas()).isEqualTo(0);
        assertThat(returnedPodStatus.getName()).isEqualTo(podStatus.getName());
        assertThat(returnedPodStatus.getHostIp()).isEqualTo(podStatus.getHostIp());
        assertThat(returnedPodStatus.getPodIp()).isEqualTo(podStatus.getPodIp());
        assertThat(returnedPodStatus.getPhase()).isEqualTo(podStatus.getPhase());
        assertThat(returnedPodStatus.getReason()).isEqualTo(podStatus.getReason());
        assertThat(returnedPodStatus.getStartTime()).isEqualTo(podStatus.getStartTime());
        assertThat(returnedPodStatus.getContainers()).contains(podStatus.getName() + "-container");
    }

    @Test
    public void testNewDeploymentGetLatestShaOnNewDeployment() throws Exception {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        Common.grantUserFullPermissionsOnEnvironment(apolloTestClient, statusDeployment.getEnvironment());

        DeployableVersion deployableVersion = ModelsGenerator.createDeployableVersion(statusDeployment.getService());
        deployableVersion.setId(apolloTestClient.addDeployableVersion(deployableVersion).getId());

        Deployment restDeployment = ModelsGenerator.createDeployment(statusDeployment.getService(),
                statusDeployment.getEnvironment(),
                deployableVersion);

        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(restDeployment);

        Deployment deployment = result.getSuccessful().get(0).getDeployment();
        restDeployment.setId(deployment.getId());

        assertThat(apolloTestClient.getDeployment(restDeployment.getId()).getSourceVersion())
                .isEqualTo(statusDeployment.getDeployableVersion().getGitCommitSha());
    }

    private String getExpectedEnvStatus(Deployment deployment) {
        DeployableVersionDao deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);

        String commitShaFromDeployableVersion = deployableVersionDao.getDeployableVersion(deployment.
                                                                                                            getDeployableVersionId()).getGitCommitSha();
        // String commitShaFromOtherDeployableVersion = deployableVersionDao.getDeployableVersion(finishedDeploymentForEnvTest.getDeployment()
        //         .getDeployableVersionId()).getGitCommitSha();

        return "{\"" + String.valueOf(deployment.getServiceId()) + "\":\"" + commitShaFromDeployableVersion + "\",\"" +
                String.valueOf(finishedDeploymentForEnvTest.getService().getId()) + "\":{}}";

        // TODO: Should be this when we can test with real env and not mocks.
        // return "{\"" + String.valueOf(deployment.getServiceId()) + "\":\"" + commitShaFromDeployableVersion + "\",\"" +
        //         String.valueOf(finishedDeploymentForEnvTest.getService().getId()) + "\":{\"" + String.valueOf(group.getId()) + "\":\"" +
        //         commitShaFromOtherDeployableVersion + "\"}}";
    }

    private static void setMockDeploymentStatus(RealDeploymentGenerator realDeploymentGenerator, boolean finished, ApolloToKubernetes apolloToKubernetes) {
        setMockDeploymentStatus(realDeploymentGenerator, finished, apolloToKubernetes, 1);
    }

    private static void setMockDeploymentStatus(RealDeploymentGenerator realDeploymentGenerator, boolean finished, ApolloToKubernetes apolloToKubernetes, Integer totalReplicas) {

        DeploymentStatus deploymentStatus;
        if (finished) {
            deploymentStatus = new DeploymentStatus(1, 1, new ArrayList<>(), 1L, 1, totalReplicas, 0, 1);
        } else {
            deploymentStatus = new DeploymentStatus(1, 1, new ArrayList<>(), 1L, 1, totalReplicas, 0, 0);
        }

        server.expect()
              .get()
              .withPath(String.format("/apis/apps/v1/namespaces/%s/deployments?labelSelector=%s",
                      realDeploymentGenerator.getEnvironment().getKubernetesNamespace(),
                      getUrlEscapedLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                              apolloToKubernetes.getApolloDeploymentUniqueIdentifierValue())))
              .andReturn(HttpStatus.OK,
                      new DeploymentListBuilder()
                              .withItems(
                                      new DeploymentBuilder()
                                              .withStatus(deploymentStatus)
                                              .withNewMetadata()
                                              .withLabels(
                                                      ImmutableMap.of(ApolloToKubernetes.getApolloCommitShaKey(),
                                                              realDeploymentGenerator.getDeployableVersion().getGitCommitSha())
                                              )
                                              .endMetadata()
                                              .build())
                              .build())
              .always();

        logger.info("");
    }

    private static String getUrlEscapedLabel(String labelName, String labelValue) {
        return UrlEscapers.urlFormParameterEscaper().escape(String.format("%s=%s", labelName, labelValue));
    }

    private static void setMockPodLogsAndStatus(RealDeploymentGenerator realDeploymentGenerator,
                                                String log, PodStatus podStatus, ApolloToKubernetes apolloToKubernetes) {

        String containerName = podStatus.getName() + "-container";
        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podStatus.getName())
                .endMetadata()
                .withNewSpec()
                .withContainers(
                        new ContainerBuilder()
                                .withName(containerName)
                                .build()
                )
                .endSpec()
                .withNewStatus()
                .withHostIP(podStatus.getHostIp())
                .withPodIP(podStatus.getPodIp())
                .withPhase(podStatus.getPhase())
                .withReason(podStatus.getReason())
                .withStartTime(podStatus.getStartTime())
                .endStatus()
                .build();

        server.expect()
              .get()
              .withPath(String.format("/api/v1/namespaces/%s/pods?labelSelector=%s",
                      realDeploymentGenerator.getEnvironment().getKubernetesNamespace(),
                      getUrlEscapedLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                              apolloToKubernetes.getApolloDeploymentPodUniqueIdentifierValue())))
              .andReturn(HttpStatus.OK,
                      new PodListBuilder()
                              .withItems(pod)
                              .build()
              )
              .always();

        server.expect()
              .get()
              .withPath(String.format("/api/v1/namespaces/%s/pods/%s/log", realDeploymentGenerator.getEnvironment().getKubernetesNamespace(), podStatus.getName()))
              .andReturn(HttpStatus.OK, log)
              .always();

        server.expect()
              .get()
              .withPath(String.format("/api/v1/namespaces/%s/pods/%s", realDeploymentGenerator.getEnvironment().getKubernetesNamespace(), podStatus.getName()))
              .andReturn(HttpStatus.OK, pod)
              .always();

        logger.info("");
    }
}