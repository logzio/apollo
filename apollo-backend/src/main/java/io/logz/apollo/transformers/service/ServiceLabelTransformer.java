package io.logz.apollo.transformers.service;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.transformers.LabelsNormalizer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roiravhon on 1/31/17.
 */
public class ServiceLabelTransformer implements BaseServiceTransformer {
    @Override
    public Service transform(Service service,
                             io.logz.apollo.models.Deployment apolloDeployment,
                             io.logz.apollo.models.Service apolloService,
                             io.logz.apollo.models.Environment apolloEnvironment,
                             io.logz.apollo.models.DeployableVersion apolloDeployableVersion) {

        Map<String, String> desiredLabels = ImmutableMap.<String, String> builder()
                .put("environment", LabelsNormalizer.normalize(apolloEnvironment.getName()))
                .put("geo_region", LabelsNormalizer.normalize(apolloEnvironment.getGeoRegion()))
                .put("apollo_unique_identifier", ApolloToKubernetes.getApolloServiceUniqueIdentifier(apolloEnvironment,
                        apolloService, Optional.empty()))
                .build();

        Map<String, String> labelsFromService = service.getMetadata().getLabels();
        Map<String, String> labelsToSet = new LinkedHashMap<>();

        if (labelsFromService != null) {
            labelsToSet.putAll(labelsFromService);
        }

        // Just make sure we are not overriding any label explicitly provided by the user
        desiredLabels.forEach((key, value) -> {
            if (!labelsToSet.containsKey(key)) {
                labelsToSet.put(key, value);
            }
        });

        // And add all back to the deployment
        service.getMetadata().setLabels(labelsToSet);

        return service;
    }
}