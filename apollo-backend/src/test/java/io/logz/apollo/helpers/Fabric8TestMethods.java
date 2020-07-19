package io.logz.apollo.helpers;

import static org.assertj.core.api.Assertions.assertThat;

public class Fabric8TestMethods {

    public static void assertImageName(io.fabric8.kubernetes.api.model.apps.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).isEqualTo(imageName);
    }

    public static void assertImageNameContains(io.fabric8.kubernetes.api.model.apps.Deployment deployment, String imageName) {
        assertThat(deployment.getSpec().getTemplate().getSpec().getContainers().stream().findFirst().get().getImage()).contains(imageName);
    }

    public static void assertDeploymentLabelExists(io.fabric8.kubernetes.api.model.apps.Deployment deployment, String labelKey, String labelValue) {
        assertThat(deployment.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertServiceLabelExists(io.fabric8.kubernetes.api.model.Service service, String labelKey, String labelValue) {
        assertThat(service.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertIngressLabelExists(io.fabric8.kubernetes.api.model.extensions.Ingress ingress, String labelKey, String labelValue) {
        assertThat(ingress.getMetadata().getLabels().get(labelKey)).isEqualTo(labelValue);
    }

    public static void assertDeploymentEnvironmentVariableExists(io.fabric8.kubernetes.api.model.apps.Deployment deployment, String envName, String envValue) {
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

    public static void assertServiceNodePort(io.fabric8.kubernetes.api.model.Service service, int port) {
        assertThat(service.getSpec().getPorts().stream().anyMatch(servicePort -> servicePort.getNodePort().equals(port))).isTrue();
    }
}