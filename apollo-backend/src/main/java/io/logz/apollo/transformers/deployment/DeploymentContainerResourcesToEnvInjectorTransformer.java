package io.logz.apollo.transformers.deployment;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;

import java.util.Map;

public class DeploymentContainerResourcesToEnvInjectorTransformer implements BaseDeploymentTransformer {

    @Override
    public Deployment transform(Deployment deployment, io.logz.apollo.models.Deployment apolloDeployment,
                                Service apolloService, Environment apolloEnvironment,
                                DeployableVersion apolloDeployableVersion, Group apolloGroup) {

        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(this::injectResourcesAsEncVariables);
        return deployment;
    }

    private void injectResourcesAsEncVariables(Container container) {
        ResourceRequirements resources = container.getResources();
        injectResourcesToEnv(container, resources.getRequests(), "requests");
        injectResourcesToEnv(container, resources.getLimits(), "limits");
    }

    private void injectResourcesToEnv(Container container, Map<String, Quantity> resources, String resourceType) {
        if (resources == null) {
            return;
        }

        resources.keySet().forEach(type -> {
            EnvVar envVar = new EnvVarBuilder()
                    .withName(String.format("container_%s_%s", resourceType, type).toUpperCase())
                    .withNewValueFrom()
                        .withNewResourceFieldRef()
                            .withContainerName(container.getName())
                            .withResource(String.format("%s.%s", resourceType, type))
                            .and()
                        .and()
                    .build();
            container.getEnv().add(envVar);
        });
    }

}
