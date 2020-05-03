package io.logz.apollo.transformers.deployment;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.transformers.LabelsNormalizer;

import java.util.Map;
import java.util.Optional;

/**
 * Created by roiravhon on 1/31/17.
 */
public class DeploymentLabelsTransformer implements BaseDeploymentTransformer {

    @Override
    public Deployment transform(Deployment deployment,
                                io.logz.apollo.models.Deployment apolloDeployment,
                                io.logz.apollo.models.Service apolloService,
                                io.logz.apollo.models.Environment apolloEnvironment,
                                io.logz.apollo.models.DeployableVersion apolloDeployableVersion,
                                io.logz.apollo.models.Group group) {

        Map<String, String> desiredLabels = ImmutableMap.<String, String> builder()
                .put("environment", LabelsNormalizer.normalize(apolloEnvironment.getName()))
                .put("geo_region", LabelsNormalizer.normalize(apolloEnvironment.getGeoRegion()))
                .put("service", LabelsNormalizer.normalize(apolloService.getName()))
                .put("availability", LabelsNormalizer.normalize(apolloEnvironment.getAvailability()))
                .put(ApolloToKubernetes.getApolloCommitShaKey(), LabelsNormalizer.normalize(apolloDeployableVersion.getGitCommitSha()))
                .put(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                        ApolloToKubernetes.getApolloDeploymentUniqueIdentifierValue(apolloEnvironment, apolloService, Optional.ofNullable(apolloDeployment.getGroupName())))
                .build();

        // Get the deployment labels
        Map<String, String> labelsFromDeployment = deployment.getMetadata().getLabels();

        // Just make sure we are not overriding any label explicitly provided by the user
        desiredLabels.forEach((key, value) -> {
            if (!labelsFromDeployment.containsKey(key)) {
                labelsFromDeployment.put(key, value);
            }
        });

        // And notify all back to the deployment
        deployment.getMetadata().setLabels(labelsFromDeployment);

        // We also need to tag the pod
        Map<String, String> labelsFromDeploymentPod = deployment.getSpec().getTemplate().getMetadata().getLabels();
        labelsFromDeploymentPod.put(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                ApolloToKubernetes.getApolloPodUniqueIdentifier(apolloEnvironment, apolloService, Optional.ofNullable(apolloDeployment.getGroupName())));
        deployment.getSpec().getTemplate().getMetadata().setLabels(labelsFromDeploymentPod);

        return deployment;
    }
}
