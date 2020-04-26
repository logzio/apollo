package io.logz.apollo.transformers.deployment;

import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;

public class AppsDeploymentTranformer implements BaseDeploymentTransformer {
    @Override
    public Deployment transform(Deployment deployment,
                                io.logz.apollo.models.Deployment apolloDeployment,
                                Service apolloService,
                                Environment apolloEnvironment,
                                DeployableVersion apolloDeployableVersion,
                                Group apolloGroup) {
        deployment.setApiVersion("apps/v1");

        LabelSelectorBuilder labelSelectorBuilder = new LabelSelectorBuilder();
        deployment.getSpec().getTemplate().getMetadata().getLabels()
                  .forEach(labelSelectorBuilder::addToMatchLabels);
        deployment.getSpec().setSelector(labelSelectorBuilder.build());

        return deployment;
    }
}
