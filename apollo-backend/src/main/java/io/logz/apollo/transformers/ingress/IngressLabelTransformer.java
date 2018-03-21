package io.logz.apollo.transformers.ingress;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import com.google.common.collect.ImmutableMap;
import io.logz.apollo.kubernetes.ApolloToKubernetes;
import io.logz.apollo.transformers.LabelsNormalizer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;


public class IngressLabelTransformer implements BaseIngressTransformer {
    @Override
    public Ingress transform(Ingress ingress,
                             io.logz.apollo.models.Deployment apolloDeployment,
                             io.logz.apollo.models.Service apolloService,
                             io.logz.apollo.models.Environment apolloEnvironment) {

        Map<String, String> desiredLabels = ImmutableMap.<String, String> builder()
                .put("environment", LabelsNormalizer.normalize(apolloEnvironment.getName()))
                .put("geo_region", LabelsNormalizer.normalize(apolloEnvironment.getGeoRegion()))
                .put("apollo_unique_identifier", ApolloToKubernetes.getApolloIngressUniqueIdentifier(apolloEnvironment,
                        apolloService, Optional.empty()))
                .build();

        Map<String, String> labelsFromIngress = ingress.getMetadata().getLabels();
        Map<String, String> labelsToSet = new LinkedHashMap<>();

        if (labelsFromIngress != null) {
            labelsToSet.putAll(labelsFromIngress);
        }

        // Just make sure we are not overriding any label explicitly provided by the user
        desiredLabels.forEach((key, value) -> {
            if (!labelsToSet.containsKey(key)) {
                labelsToSet.put(key, value);
            }
        });

        // And add all back to the ingress
        ingress.getMetadata().setLabels(labelsToSet);

        return ingress;
    }
}
