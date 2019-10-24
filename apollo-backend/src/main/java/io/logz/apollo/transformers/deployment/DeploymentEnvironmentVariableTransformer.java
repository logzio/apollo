package io.logz.apollo.transformers.deployment;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by roiravhon on 3/10/17.
 */
public class DeploymentEnvironmentVariableTransformer implements BaseDeploymentTransformer {

    @Override
    public Deployment transform(Deployment deployment,
                                io.logz.apollo.models.Deployment apolloDeployment,
                                Service apolloService, Environment apolloEnvironment,
                                DeployableVersion apolloDeployableVersion,
                                Group group) {

        // TODO: This should probably be an externally configured map of key names and desired dynamic values
        // TODO: Keeping this logz.io specific at the moment
        Map<String, String> desiredEnvironmentVariables = ImmutableMap.<String, String> builder()
                .put("ENV", apolloEnvironment.getAvailability())
                .put("REGION", apolloEnvironment.getGeoRegion())
                .build();

        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(container -> {
            List<EnvVar> envVarList = container.getEnv();
            desiredEnvironmentVariables.forEach((key, value) -> {

                // Do not override values supplied externally
                if (envVarList.stream().noneMatch(envVar -> envVar.getName().equals(key))) {
                    envVarList.add(new EnvVar(key, value, null));
                }
            });

            container.setEnv(envVarList);
        });

        return deployment;
    }
}
