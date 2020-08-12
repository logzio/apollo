package io.logz.apollo.transformers.deployment;

import io.fabric8.kubernetes.api.model.apps.Deployment;

/**
 * Created by roiravhon on 1/31/17.
 */
public interface BaseDeploymentTransformer {
    Deployment transform(Deployment deployment,
                         io.logz.apollo.models.Deployment apolloDeployment,
                         io.logz.apollo.models.Service apolloService,
                         io.logz.apollo.models.Environment apolloEnvironment,
                         io.logz.apollo.models.DeployableVersion apolloDeployableVersion,
                         io.logz.apollo.models.Group apolloGroup);
}
