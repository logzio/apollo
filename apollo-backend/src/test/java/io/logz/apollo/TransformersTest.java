package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.excpetions.ApolloParseException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.RealDeploymentGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.kubernetes.ApolloToKubernetesStore;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by roiravhon on 2/1/17.
 */
public class TransformersTest {

    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static ApolloToKubernetesStore apolloToKubernetesStore;

    @BeforeClass
    public static void init() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        apolloToKubernetesStore = standaloneApollo.getInstance(ApolloToKubernetesStore.class);
    }

    @Test
    public void testImageNameTransformer() throws ApolloParseException, IOException {
        String imageNameWithRepoAndVersion = "repo:1234/image:version";
        String imageNameWithRepoAndNoVersion = "repo:1234/image";
        String imageNameWithSimpleRepoAndNoVersion = "repo/image";
        String imageNameWithNoRepoAndVersion = "image:version";
        String imageNameWithNoRepoAndNoVersion = "image";

        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithRepoAndVersion, "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithRepoAndVersion);

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithRepoAndNoVersion, "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithRepoAndNoVersion + ":" + realDeploymentGenerator.getDeployableVersion().getGitCommitSha());

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithSimpleRepoAndNoVersion, "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithSimpleRepoAndNoVersion + ":" + realDeploymentGenerator.getDeployableVersion().getGitCommitSha());

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithNoRepoAndVersion, "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithNoRepoAndVersion);

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithNoRepoAndNoVersion, "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithNoRepoAndNoVersion + ":" + realDeploymentGenerator.getDeployableVersion().getGitCommitSha());

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithNoRepoAndNoVersion, "key", "value", 0);
        realDeploymentGenerator.updateDeploymentStatus(Deployment.DeploymentStatus.PENDING_CANCELLATION);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithNoRepoAndNoVersion + ":" + realDeploymentGenerator.getDeployment().getSourceVersion());

        realDeploymentGenerator = new RealDeploymentGenerator(imageNameWithNoRepoAndNoVersion, "key", "value", 0);
        realDeploymentGenerator.updateDeploymentStatus(Deployment.DeploymentStatus.CANCELING);
        realDeploymentGenerator.updateDeploymentStatus(Deployment.DeploymentStatus.CANCELING);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertImageName(apolloToKubernetes.getKubernetesDeployment(), imageNameWithNoRepoAndNoVersion + ":" + realDeploymentGenerator.getDeployment().getSourceVersion());
    }

    @Test
    public void testDeploymentLabelsTransformer() throws ApolloParseException, IOException {

        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        String SampleLabelFromTransformer = "environment";

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(), realDeploymentGenerator.getDefaultLabelKey(), realDeploymentGenerator.getDefaultLabelValue());

        // Check for one of the default labels that the transformer assigns
        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(),
                SampleLabelFromTransformer, realDeploymentGenerator.getEnvironment().getName());

        // Check that the transformer does not override a given label with a default one
        realDeploymentGenerator = new RealDeploymentGenerator("image", SampleLabelFromTransformer, "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertDeploymentLabelExists(apolloToKubernetes.getKubernetesDeployment(), SampleLabelFromTransformer, "value");
    }

    @Test
    public void testServiceLabelsTransformer() throws ApolloParseException {

        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        String sampleLabelFromTransformer = "environment";

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertServiceLabelExists(apolloToKubernetes.getKubernetesService(), realDeploymentGenerator.getDefaultLabelKey(), realDeploymentGenerator.getDefaultLabelValue());

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertServiceLabelExists(apolloToKubernetes.getKubernetesService(),
                sampleLabelFromTransformer, realDeploymentGenerator.getEnvironment().getName());
    }

    @Test
    public void testIngressLabelsTransformer() throws ApolloParseException {

        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        String sampleLabelFromTransformer = "environment";

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());
        assertIngressLabelExists(apolloToKubernetes.getKubernetesIngress(), realDeploymentGenerator.getDefaultLabelKey(), realDeploymentGenerator.getDefaultLabelValue());
        assertIngressLabelExists(apolloToKubernetes.getKubernetesIngress(), sampleLabelFromTransformer, realDeploymentGenerator.getEnvironment().getName());
    }

    @Test
    public void testDeploymentEnvironmentVariablesTransformer() throws ApolloParseException, IOException {

        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        String regionEnvNameFromTransformer = "REGION";

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());

        assertDeploymentEnvironmentVariableExists(apolloToKubernetes.getKubernetesDeployment(), regionEnvNameFromTransformer, realDeploymentGenerator.getEnvironment().getGeoRegion());
        assertDeploymentEnvironmentVariableExists(apolloToKubernetes.getKubernetesDeployment(), realDeploymentGenerator.getDefaultEnvironmentVariableName(), realDeploymentGenerator.getDefaultEnvironmentVariableValue());
    }

    @Test
    public void testDeploymentScalingFactorTransformerAndDeploymentNameTransformer() throws ApolloClientException, ApolloParseException, IOException {
        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        Service service = ModelsGenerator.createAndSubmitService(apolloTestClient);
        service = apolloTestClient.updateService(service.getId(), service.getName(), service.getDeploymentYaml(), service.getServiceYaml(), service.getIngressYaml(), true);

        Environment environment = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);

        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service.getId(), environment.getId());

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "value", 0, null, service, environment, group.getName());
        Deployment deployment = realDeploymentGenerator.getDeployment();

        apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);

        assertThat(group.getScalingFactor()).isGreaterThan(1); // If it's 1, the transformer wouldn't change anything.
        assertThat(apolloToKubernetes.getKubernetesDeployment().getSpec().getReplicas()).isEqualTo(group.getScalingFactor());

        assertThat(apolloToKubernetes.getKubernetesDeployment().getMetadata().getName()).isEqualTo("nginx-" + deployment.getGroupName());
    }

    @Test
    public void testServicePortCoefficient() throws ApolloParseException {
        RealDeploymentGenerator realDeploymentGenerator;
        ApolloToKubernetes apolloToKubernetes;

        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "label", 0);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());

        assertServiceNodePort(apolloToKubernetes.getKubernetesService(), realDeploymentGenerator.getDefaultNodePort());

        int servicePortCoefficient = 200;
        realDeploymentGenerator = new RealDeploymentGenerator("image", "key", "label", servicePortCoefficient);
        apolloToKubernetes = createForDeployment(realDeploymentGenerator.getDeployment());

        assertServiceNodePort(apolloToKubernetes.getKubernetesService(), realDeploymentGenerator.getDefaultNodePort() + servicePortCoefficient);
    }

    private void assertImageName(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).isEqualTo(imageName);
    }

    private void assertDeploymentLabelExists(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String labelKey, String labelValue) {
        assertThat(deployment.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    private void assertServiceLabelExists(io.fabric8.kubernetes.api.model.Service service, String labelKey, String labelValue) {
        assertThat(service.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    private void assertIngressLabelExists(io.fabric8.kubernetes.api.model.extensions.Ingress ingress, String labelKey, String labelValue) {
        assertThat(ingress.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    private void assertDeploymentEnvironmentVariableExists(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String envName, String envValue) {
        assertThat(
                deployment.getSpec().getTemplate().getSpec().getContainers()
                        .stream()
                        .findFirst()
                        .get()
                        .getEnv()
                            .stream()
                            .filter(envVar -> envVar.getName().equals(envName))
                            .findFirst()
                            .orElse(null)
                            .getValue()
        ).isEqualTo(envValue);
    }

    private void assertServiceNodePort(io.fabric8.kubernetes.api.model.Service service, int port) {
        assertThat(service.getSpec().getPorts().stream().anyMatch(servicePort -> servicePort.getNodePort().equals(port))).isTrue();
    }

    private ApolloToKubernetes createForDeployment(Deployment deployment) {
        DeployableVersionDao deployableVersionDao = standaloneApollo.getInstance(DeployableVersionDao.class);
        EnvironmentDao environmentDao = standaloneApollo.getInstance(EnvironmentDao.class);
        DeploymentDao deploymentDao = standaloneApollo.getInstance(DeploymentDao.class);
        ServiceDao serviceDao = standaloneApollo.getInstance(ServiceDao.class);
        GroupDao groupDao = standaloneApollo.getInstance(GroupDao.class);

        DeployableVersion deployableVersion = deployableVersionDao.getDeployableVersion(deployment.getDeployableVersionId());
        Environment environment = environmentDao.getEnvironment(deployment.getEnvironmentId());
        Service service = serviceDao.getService(deployment.getServiceId());

        return new ApolloToKubernetes(deploymentDao, deployableVersion, environment, deployment, service, groupDao);
    }

}
