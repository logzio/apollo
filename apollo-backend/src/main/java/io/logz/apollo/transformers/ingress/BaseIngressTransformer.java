package io.logz.apollo.transformers.ingress;

import io.fabric8.kubernetes.api.model.extensions.Ingress;

public interface BaseIngressTransformer {
    Ingress transform(Ingress ingress,
                      io.logz.apollo.models.Deployment apolloDeployment,
                      io.logz.apollo.models.Service apolloService,
                      io.logz.apollo.models.Environment apolloEnvironment);
}
