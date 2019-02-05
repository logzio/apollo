package io.logz.apollo.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.logz.apollo.common.Encryptor;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.excpetions.ApolloParseException;
import io.logz.apollo.notifications.mustache.TemplateInjector;
import io.logz.apollo.transformers.LabelsNormalizer;
import io.logz.apollo.transformers.deployment.BaseDeploymentTransformer;
import io.logz.apollo.transformers.deployment.DeploymentEnvironmentVariableTransformer;
import io.logz.apollo.transformers.deployment.DeploymentImageNameTransformer;
import io.logz.apollo.transformers.deployment.DeploymentLabelsTransformer;
import io.logz.apollo.transformers.deployment.DeploymentNameTransformer;
import io.logz.apollo.transformers.deployment.DeploymentScalingFactorTransformer;
import io.logz.apollo.transformers.ingress.BaseIngressTransformer;
import io.logz.apollo.transformers.ingress.IngressLabelTransformer;
import io.logz.apollo.transformers.service.BaseServiceTransformer;
import io.logz.apollo.transformers.service.ServiceLabelTransformer;
import io.logz.apollo.transformers.service.ServiceNodePortCoefficientTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 1/31/17.
 */
public class ApolloToKubernetes {

    private static final Logger logger = LoggerFactory.getLogger(ApolloToKubernetes.class);
    private static final String APOLLO_UNIQUE_IDENTIFIER_KEY = "apollo_unique_identifier";
    private static final String APOLLO_COMMIT_SHA_KEY = "current_commit_sha";
    private final io.logz.apollo.models.Service apolloService;
    private final io.logz.apollo.models.Environment apolloEnvironment;
    private final io.logz.apollo.models.DeployableVersion apolloDeployableVersion;
    private final ObjectMapper mapper;
    private io.logz.apollo.models.Deployment apolloDeployment;
    private final String groupName;

    private final Set<BaseDeploymentTransformer> deploymentTransformers;
    private final Set<BaseServiceTransformer> serviceTransformers;
    private final Set<BaseIngressTransformer> ingressTransformers;

    private final DeploymentDao deploymentDao;
    private final GroupDao groupDao;

    private final TemplateInjector templateInjector;

    private final String REGION = "region";
    private final String ENVIRONMENT = "environment";

    public ApolloToKubernetes(DeploymentDao deploymentDao,
                              io.logz.apollo.models.DeployableVersion apolloDeployableVersion,
                              io.logz.apollo.models.Environment apolloEnvironment,
                              io.logz.apollo.models.Deployment apolloDeployment,
                              io.logz.apollo.models.Service apolloService,
                              GroupDao groupDao) {
        this.apolloDeployableVersion = requireNonNull(apolloDeployableVersion);
        this.apolloEnvironment = requireNonNull(apolloEnvironment);
        this.apolloDeployment = requireNonNull(apolloDeployment);
        this.apolloService = requireNonNull(apolloService);
        this.deploymentDao = requireNonNull(deploymentDao);
        this.groupDao = requireNonNull(groupDao);
        this.groupName = apolloDeployment.getGroupName();

        mapper = new ObjectMapper(new YAMLFactory());

        // Define the set of transformers the deployment object will go through
        deploymentTransformers = Sets.newHashSet(Arrays.asList(
                new DeploymentImageNameTransformer(),
                new DeploymentLabelsTransformer(),
                new DeploymentEnvironmentVariableTransformer()
        ));

        if (apolloService.getIsPartOfGroup() != null && apolloService.getIsPartOfGroup()) {
            deploymentTransformers.add(new DeploymentScalingFactorTransformer());
            deploymentTransformers.add(new DeploymentNameTransformer());
        }

        // Define the set of transformers the service object will go through
        serviceTransformers = Sets.newHashSet(Arrays.asList(
                new ServiceLabelTransformer(),
                new ServiceNodePortCoefficientTransformer()
        ));

        // Define the set of transformers the ingress object will go through
        ingressTransformers = Sets.newHashSet(Arrays.asList(
                new IngressLabelTransformer()
        ));

        templateInjector = new TemplateInjector();
    }

    public Deployment getKubernetesDeployment() throws ApolloParseException, IOException {
        try {
            // Update the deployment, as it could have changed since (Status)
            apolloDeployment = deploymentDao.getDeployment(apolloDeployment.getId());
            String deploymentYaml = apolloService.getDeploymentYaml();

            // Fill deployment YAML with parameters if needed
            HashMap<String, String> additionalParams = new HashMap<>();
            String environmentAdditionalParams = apolloEnvironment.getAdditionalParams();

            if (environmentAdditionalParams != null && !environmentAdditionalParams.isEmpty()) {
                additionalParams.putAll(jsonToMap(environmentAdditionalParams));
            }

            if (apolloService.getIsPartOfGroup()) {
                additionalParams.putAll(jsonToMap(apolloDeployment.getDeploymentParams()));
            }

            // Convert the deployment object to fabric8 model
            Deployment deployment = getClassFromYamlWithParameters(deploymentYaml, additionalParams, Deployment.class);

            // Programmatically access to change all the stuff we need
            logger.debug("About to run {} transformations on the deployment yaml of deployment id {}",
                    deploymentTransformers.size(), apolloDeployment.getId());
            deploymentTransformers.forEach(transformer ->
                    transformer.transform(deployment, apolloDeployment, apolloService, apolloEnvironment, apolloDeployableVersion, groupDao.getGroupByName(groupName)));

            return deployment;

        } catch (IOException e) {
            throw new ApolloParseException("Could not parse deployment YAML from DB", e);
        }
    }

    public Service getKubernetesService() throws ApolloParseException {
        try {
            // Update the deployment, as it could have changed since (Status)
            apolloDeployment = deploymentDao.getDeployment(apolloDeployment.getId());

            // Services are allowed to be null
            if (apolloService.getServiceYaml() == null) {
                return null;
            }

            // Convert the service object to fabric8 model
            Service service = getClassFromYamlWithParameters(apolloService.getServiceYaml(), jsonToMap(apolloEnvironment.getAdditionalParams()), Service.class);

            // Programmatically access to change all the stuff we need
            logger.debug("About to run {} transformations on the service yaml of deployment id {}",
                    serviceTransformers.size(), apolloDeployment.getId());
            serviceTransformers.forEach(transformer ->
                    transformer.transform(service, apolloDeployment, apolloService, apolloEnvironment, apolloDeployableVersion));

            return service;

        } catch (IOException e) {
            throw new ApolloParseException("Could not parse service YAML from DB", e);
        }
    }


    public Ingress getKubernetesIngress() throws ApolloParseException {
        try {
            // Update the deployment, as it could have changed since (Status)
            apolloDeployment = deploymentDao.getDeployment(apolloDeployment.getId());

            // Ingress are allowed to be null
            if (apolloService.getIngressYaml() == null) {
                return null;
            }

            // Convert the ingress object to fabric8 model
            Ingress ingress = getClassFromYamlWithParameters(apolloService.getIngressYaml(), jsonToMap(apolloEnvironment.getAdditionalParams()), Ingress.class);

            // Programmatically access to change all the stuff we need
            logger.debug("About to run {} transformations on the ingress yaml of deployment id {}",
                    ingressTransformers.size(), apolloDeployment.getId());
            ingressTransformers.forEach(transformer ->
                    transformer.transform(ingress, apolloDeployment, apolloService, apolloEnvironment));

            return ingress;

        } catch (IOException e) {
            throw new ApolloParseException("Could not parse ingress YAML from DB", e);
        }
    }

    public String getApolloDeploymentUniqueIdentifierValue() {
        // there's a deployment here
        return getApolloDeploymentUniqueIdentifierValue(apolloEnvironment, apolloService, Optional.ofNullable(groupName));
    }

    public String getApolloDeploymentPodUniqueIdentifierValue() {
        // there's a deployment here
        return getApolloPodUniqueIdentifier(apolloEnvironment, apolloService, Optional.ofNullable(groupName));
    }

    public static String getApolloDeploymentUniqueIdentifierKey() {
        return APOLLO_UNIQUE_IDENTIFIER_KEY;
    }

    public static String getApolloCommitShaKey() {
        return APOLLO_COMMIT_SHA_KEY;
    }

    public static String getApolloDeploymentUniqueIdentifierValue(io.logz.apollo.models.Environment apolloEnvironment,
                                                                  io.logz.apollo.models.Service apolloService, Optional<String> groupName) {

        return getApolloUniqueIdentifierWithPrefix(apolloEnvironment, apolloService, groupName, "deployment");
    }

    public static String getApolloServiceUniqueIdentifier(io.logz.apollo.models.Environment apolloEnvironment,
                                                           io.logz.apollo.models.Service apolloService, Optional<String> groupName) {
        return getApolloUniqueIdentifierWithPrefix(apolloEnvironment, apolloService, groupName, "service");
    }

    public static String getApolloIngressUniqueIdentifier(io.logz.apollo.models.Environment apolloEnvironment,
                                                          io.logz.apollo.models.Service apolloService, Optional<String> groupName) {
        return getApolloUniqueIdentifierWithPrefix(apolloEnvironment, apolloService, groupName, "ingress");
    }

    public static String getApolloPodUniqueIdentifier(io.logz.apollo.models.Environment apolloEnvironment,
                                                          io.logz.apollo.models.Service apolloService, Optional<String> groupName) {
        return getApolloUniqueIdentifierWithPrefix(apolloEnvironment, apolloService, groupName, "pod");
    }

    private static String getApolloUniqueIdentifierWithPrefix(io.logz.apollo.models.Environment apolloEnvironment,
                                                              io.logz.apollo.models.Service apolloService,
                                                              Optional<String> groupName,
                                                              String prefix) {
        String naiveUniqueIdentofier = "apollo_" + prefix + "_env_" + apolloEnvironment.getId() + "_service_" + apolloService.getId();
        if (groupName.isPresent()) {
            naiveUniqueIdentofier += "_group_" + groupName.get();
        }
        return Encryptor.encryptString(LabelsNormalizer.normalize(naiveUniqueIdentofier));
    }

    private String fillYamlWithParams(String yaml, HashMap parameters) {
        return templateInjector.injectToTemplate(yaml, parameters);
    }

    private <T> T getClassFromYamlWithParameters(String yaml, HashMap<String, String> parameters, Class<T> valueType) throws IOException {
        yaml = fillYamlWithParams(yaml, fillDefaultParameters(parameters));
        return mapper.readValue(yaml, valueType);
    }

    private HashMap<String, String> fillDefaultParameters(HashMap<String, String> parameters) {
        parameters.put(REGION, apolloEnvironment.getGeoRegion());
        parameters.put(ENVIRONMENT, apolloEnvironment.getAvailability());

        return parameters;
    }

    private HashMap<String, String> jsonToMap(String deploymentParams) throws IOException {
        if (deploymentParams == null) {
            return new HashMap<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(deploymentParams, new TypeReference<HashMap<String, String>>() {});
    }
}
