package io.logz.apollo.transformers.deployment;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;

public class DeploymentNameTransformer implements BaseDeploymentTransformer {

    @Override
    public Deployment transform(Deployment deployment,
                                io.logz.apollo.models.Deployment apolloDeployment,
                                Service apolloService,
                                Environment apolloEnvironment,
                                DeployableVersion apolloDeployableVersion,
                                Group apolloGroup) {

        if (apolloGroup != null) {
            deployment.getMetadata().setName(deployment.getMetadata().getName() + "-" + apolloGroup.getName());
        }
        return deployment;
    }
}
