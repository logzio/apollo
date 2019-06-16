package io.logz.apollo.helpers;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.extensions.Deployment;

import static org.assertj.core.api.Assertions.assertThat;

public class Fabric8TestMethods {

    public static void assertImageName(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).isEqualTo(imageName);
    }

    public static void assertImageNameContains(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).contains(imageName);
    }

    public static void assertDeploymentLabelExists(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String labelKey, String labelValue) {
        assertThat(deployment.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertServiceLabelExists(io.fabric8.kubernetes.api.model.Service service, String labelKey, String labelValue) {
        assertThat(service.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertIngressLabelExists(io.fabric8.kubernetes.api.model.extensions.Ingress ingress, String labelKey, String labelValue) {
        assertThat(ingress.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertDeploymentEnvironmentVariableExists(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String envName, String envValue) {
        assertThat(getEnvVarByName(deployment, envName).getValue()).isEqualTo(envValue);
    }

    public static void assertDeploymentEnvironmentVariableHasValueFromResource(io.fabric8.kubernetes.api.model.extensions.Deployment deployment, String envName, String resourcePath) {
        EnvVar envVar = getEnvVarByName(deployment, envName);
        assertThat(envVar.getValueFrom().getResourceFieldRef().getResource()).isEqualTo(resourcePath);
    }

    private static EnvVar getEnvVarByName(Deployment deployment, String envName) {
        return deployment.getSpec().getTemplate().getSpec().getContainers()
                .stream()
                .findFirst()
                .get()
                .getEnv()
                .stream()
                .filter(envVar -> envVar.getName().equals(envName))
                .findFirst()
                .orElse(null);
    }

    public static void assertServiceNodePort(io.fabric8.kubernetes.api.model.Service service, int port) {
        assertThat(service.getSpec().getPorts().stream().anyMatch(servicePort -> servicePort.getNodePort().equals(port))).isTrue();
    }
}
