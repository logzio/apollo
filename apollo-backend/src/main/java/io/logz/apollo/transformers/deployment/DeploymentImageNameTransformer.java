package io.logz.apollo.transformers.deployment;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.logz.apollo.models.Deployment.DeploymentStatus.CANCELING;
import static io.logz.apollo.models.Deployment.DeploymentStatus.PENDING_CANCELLATION;

/**
 * Created by roiravhon on 1/31/17.
 */
public class DeploymentImageNameTransformer implements BaseDeploymentTransformer {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentImageNameTransformer.class);

    @Override
    public Deployment transform(Deployment deployment,
                                io.logz.apollo.models.Deployment apolloDeployment,
                                io.logz.apollo.models.Service apolloService,
                                io.logz.apollo.models.Environment apolloEnvironment,
                                io.logz.apollo.models.DeployableVersion apolloDeployableVersion,
                                io.logz.apollo.models.Group group) {

        // Each deployment can have multiple containers
        deployment.getSpec().getTemplate().getSpec().getContainers().forEach(container -> {

            String containerImage = container.getImage();

            // Some container names contain a repository inside, and repos are allowed to have :port, so we need to check this first
            String containerImageWithoutRepo = containerImage;
            if (containerImage.contains("/")) {
                containerImageWithoutRepo = containerImage.split("/")[1];
            }

            // If the image name was specified with :version, not changing anything
            if (containerImageWithoutRepo.contains(":")) {
                logger.debug("Got container image {}. Not appending version, as this is already contains a version", containerImage);
            } else {
                if (apolloDeployment.getStatus().equals(PENDING_CANCELLATION) || apolloDeployment.getStatus().equals(CANCELING)) {
                    containerImage = containerImage + ":" + apolloDeployment.getSourceVersion();
                } else {
                    containerImage = containerImage + ":" + apolloDeployableVersion.getGitCommitSha();
                }
                logger.info("Setting image of container {} out of deployment id {} to {}",
                        container.getName(), apolloDeployment.getId(), containerImage);

                container.setImage(containerImage);
            }
        });

        return deployment;
    }
}
