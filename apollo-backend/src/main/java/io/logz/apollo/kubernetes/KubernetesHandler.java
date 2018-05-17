package io.logz.apollo.kubernetes;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.excpetions.ApolloKubernetesException;
import io.logz.apollo.excpetions.ApolloNotFoundException;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.PodStatus;
import io.logz.apollo.models.Service;

import io.logz.apollo.notifications.ApolloNotifications;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.rapidoid.http.Req;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class KubernetesHandler {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesHandler.class);
    private static final int NUMBER_OF_LOG_LINES_TO_FETCH = 500;
    private static final String APOLLO_JOLOKIA_PORT_LABEL = "apollo_jolokia_port";
    private final ApolloToKubernetesStore apolloToKubernetesStore;
    private final KubernetesClient kubernetesClient;
    private final Environment environment;
    private final ApolloNotifications apolloNotifications;

    @VisibleForTesting
    KubernetesHandler(ApolloToKubernetesStore apolloToKubernetesStore, KubernetesClient kubernetesClient,
                      Environment environment, ApolloNotifications apolloNotifications) {
        this.apolloToKubernetesStore = requireNonNull(apolloToKubernetesStore);
        this.kubernetesClient = requireNonNull(kubernetesClient);
        this.environment = requireNonNull(environment);
        this.apolloNotifications = requireNonNull(apolloNotifications);
    }

    public KubernetesHandler(ApolloToKubernetesStore apolloToKubernetesStore, Environment environment,
                             ApolloNotifications apolloNotifications) {
        this.apolloToKubernetesStore = requireNonNull(apolloToKubernetesStore);
        this.environment = requireNonNull(environment);
        this.apolloNotifications = requireNonNull(apolloNotifications);

        this.kubernetesClient = createKubernetesClient(environment);
    }

    Deployment startDeployment(Deployment deployment) {
        try {
            ApolloToKubernetes apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);
            io.fabric8.kubernetes.api.model.extensions.Deployment kubernetesDeployment = apolloToKubernetes.getKubernetesDeployment();
            io.fabric8.kubernetes.api.model.Service kubernetesService = apolloToKubernetes.getKubernetesService();
            io.fabric8.kubernetes.api.model.extensions.Ingress kubernetesIngress = apolloToKubernetes.getKubernetesIngress();

            kubernetesClient
                    .extensions()
                    .deployments()
                    .inNamespace(environment.getKubernetesNamespace())
                    .createOrReplace(kubernetesDeployment);

            // Services are allowed to be null
            if (kubernetesService != null) {
                kubernetesClient
                        .services()
                        .inNamespace(environment.getKubernetesNamespace())
                        .createOrReplace(kubernetesService);
            }
            // Ingress are allowed to be null
            if (kubernetesIngress != null) {
                kubernetesClient
                        .extensions()
                        .ingresses()
                        .inNamespace(environment.getKubernetesNamespace())
                        .createOrReplace(kubernetesIngress);
            }

            logger.info("Started deployment id {}", deployment.getId());
            deployment.setStatus(Deployment.DeploymentStatus.STARTED);
            return deployment;

        } catch (Exception e) {
            logger.error("Got exception while deploying to kubernetes deployment id {}. Leaving in its original state", deployment.getId(), e);
            return deployment;
        }
    }

    Deployment cancelDeployment(Deployment deployment) {
        try {
            ApolloToKubernetes apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);
            io.fabric8.kubernetes.api.model.extensions.Deployment kubernetesDeployment = apolloToKubernetes.getKubernetesDeployment();
            kubernetesClient
                    .extensions()
                    .deployments()
                    .inNamespace(environment.getKubernetesNamespace())
                    .createOrReplace(kubernetesDeployment);

            logger.info("Canceled deployment id {}", deployment.getId());
            deployment.setStatus(Deployment.DeploymentStatus.CANCELING);
            return deployment;

        } catch (Exception e) {
            logger.error("Got exception while canceling deployment id {}. Leaving in its original state", deployment.getId(), e);
            return deployment;
        }
    }

    Deployment monitorDeployment(Deployment deployment) {

        try {
            ApolloToKubernetes apolloToKubernetes = apolloToKubernetesStore.getOrCreateApolloToKubernetes(deployment);
            Optional<io.fabric8.kubernetes.api.model.extensions.Deployment> returnedDeployment = kubernetesClient
                    .extensions()
                    .deployments()
                    .inNamespace(environment.getKubernetesNamespace())
                    .withLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(), apolloToKubernetes.getApolloDeploymentUniqueIdentifierValue())
                    .list()
                    .getItems()
                    .stream()
                    .findFirst();

            if (returnedDeployment.isPresent()) {
                io.fabric8.kubernetes.api.model.extensions.DeploymentStatus deploymentStatus = returnedDeployment.get().getStatus();

                int totalReplicas = deploymentStatus.getReplicas();

                if (totalReplicas == 0) {
                    logger.info("Monitoring of deployment id {}: No replicas required.", deployment.getId());
                    updateDeploymentStatus(deployment);
                } else {
                    int updatedReplicas = deploymentStatus.getUpdatedReplicas();
                    logger.info("Monitoring of deployment id {}: out of {} replicas, {} are updated", deployment.getId(), totalReplicas, updatedReplicas);

                    if (updatedReplicas == totalReplicas) {
                        updateDeploymentStatus(deployment);
                    }
                }

            } else {
                logger.warn("Found no deployments in kubernetes matching apollo_unique_identifier={}", apolloToKubernetes.getApolloDeploymentUniqueIdentifierValue());
            }

            return deployment;

        } catch (Exception e) {
            logger.error("Got exception while monitoring deployment {}. Leaving it in its original state", deployment.getId(), e);
            return deployment;
        }
    }

    private void updateDeploymentStatus(Deployment deployment) {
        if (deployment.getStatus().equals(Deployment.DeploymentStatus.STARTED)) {
            logger.info("Deployment id {} is done deploying", deployment.getId());
            apolloNotifications.notify(Deployment.DeploymentStatus.DONE, deployment);
            deployment.setStatus(Deployment.DeploymentStatus.DONE);
        } else if (deployment.getStatus().equals(Deployment.DeploymentStatus.CANCELING)) {
            logger.info("Deployment id {} is done canceling", deployment.getId());
            apolloNotifications.notify(Deployment.DeploymentStatus.CANCELED, deployment);
            deployment.setStatus(Deployment.DeploymentStatus.CANCELED);
        }
    }

    public KubernetesDeploymentStatus getCurrentStatus(Service service) {
        return getCurrentStatus(service, Optional.empty());
    }

    public KubernetesDeploymentStatus getCurrentStatus(Service service, Optional<String> groupName) {

        io.fabric8.kubernetes.api.model.extensions.Deployment deployment = getKubernetesDeployment(service, groupName);

        if (deployment == null) {
            logger.warn("Could not find deployment for environment {} and service {}, can't return the status!", environment.getId(), service.getId());
            return null;
        }

        List<PodStatus> podStatusList = kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                        ApolloToKubernetes.getApolloPodUniqueIdentifier(environment, service, groupName))
                .list()
                .getItems()
                .stream()
                .map(pod -> pod.getMetadata().getName())
                .map(this::getPodStatus)
                .collect(Collectors.toList());

        KubernetesDeploymentStatus kubernetesDeploymentStatus = new KubernetesDeploymentStatus();
        kubernetesDeploymentStatus.setServiceId(service.getId());
        kubernetesDeploymentStatus.setEnvironmentId(environment.getId());
        kubernetesDeploymentStatus.setGitCommitSha(deployment.getMetadata().getLabels().get(ApolloToKubernetes.getApolloCommitShaKey()));
        kubernetesDeploymentStatus.setReplicas(deployment.getStatus().getReplicas());
        kubernetesDeploymentStatus.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
        kubernetesDeploymentStatus.setUpdatedReplicas(deployment.getStatus().getUpdatedReplicas());
        kubernetesDeploymentStatus.setUnavailableReplicas(deployment.getStatus().getUnavailableReplicas());
        kubernetesDeploymentStatus.setPodStatuses(podStatusList);
        groupName.ifPresent(kubernetesDeploymentStatus::setGroupName);

        return kubernetesDeploymentStatus;
    }

    public void restartPod(String podName) throws ApolloKubernetesException {
        // Deleting a pod, which created with a deployment is basically restarting since the replica set will create a new one immediately
        try {
            kubernetesClient
                    .pods()
                    .inNamespace(environment.getKubernetesNamespace())
                    .withName(podName)
                    .delete();
        } catch (KubernetesClientException e) {
            // Async deletion throws KubernetesClientException with code 202, but it's working and actually deleting the pod
            if (e.getCode() != HttpStatus.ACCEPTED) {
                throw new ApolloKubernetesException();
            }
        }
    }

    public void restartAllPods(Service service, Optional<String> groupName) throws ApolloKubernetesException, ApolloNotFoundException {
        try {
            io.fabric8.kubernetes.api.model.extensions.Deployment deployment = getKubernetesDeployment(service, groupName);

            if (deployment == null) {
                throw new ApolloNotFoundException("Deployment not found!");
            }
            // There's no option to force kubernetes deployment to restart all pods gradually, so we have to edit an innocuous field of the deployment
            kubernetesClient
                    .extensions()
                    .deployments()
                    .inNamespace(environment.getKubernetesNamespace())
                    .withName(deployment.getMetadata().getName())
                    .edit()
                    .editSpec()
                    .editTemplate()
                    .editMetadata()
                    .addToLabels("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")))
                    .endMetadata()
                    .endTemplate()
                    .endSpec()
                    .done();

        } catch (KubernetesClientException e) {
            throw new ApolloKubernetesException();
        }
    }

    public ExecWatch getExecWatch(String podName, String containerName, String... command) {

        return kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(podName)
                .inContainer(containerName)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .withTTY()
                .exec(command);
    }

    public LogWatch getLogWatch(String podName, String containerName) {

        return kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(podName)
                .inContainer(containerName)
                .tailingLines(500)
                .watchLog();
    }

    public Optional<Response> proxyJolokia(String podName, String jolokiaPath, Req req) {
        try {
            Optional<Integer> podJolokiaPort = getPodJolokiaPort(podName);
            if (!podJolokiaPort.isPresent()) {
                return Optional.empty();
            }

            String url = kubernetesClient.getMasterUrl().toString() + "api/v1/namespaces/" + environment.getKubernetesNamespace() +
                    "/pods/http:" + podName + ":" + String.valueOf(podJolokiaPort.get()) + "/proxy/jolokia/" + jolokiaPath + "?" + req.query();
            Request request;

            switch (req.verb()) {
                case "GET":
                    request = new Request.Builder().url(url).build();
                    break;
                case "POST":
                    request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse(req.contentType().toString()), req.body())).build();
                    break;
                case "PUT":
                    request = new Request.Builder().url(url).put(RequestBody.create(MediaType.parse(req.contentType().toString()), req.body())).build();
                    break;
                case "DELETE":
                    request = new Request.Builder().url(url).delete(RequestBody.create(MediaType.parse(req.contentType().toString()), req.body())).build();
                    break;
                case "HEAD":
                    request = new Request.Builder().url(url).head().build();
                    break;
                case "OPTIONS":
                    request = new Request.Builder().url(url).method("OPTIONS", RequestBody.create(null, new byte[0])).build();
                    break;
                default:
                    logger.info("Unsupported Verb {}", req.verb());
                    return Optional.empty();
            }

            return Optional.of(
                    ((DefaultKubernetesClient) kubernetesClient)
                    .getHttpClient()
                    .newCall(request)
                    .execute());
        } catch (IOException e) {
            logger.warn("Got IOException while proxy the request to jolokia!", e);
            return Optional.empty();
        }
    }

    public Optional<String> getServiceLatestCreatedPodName(Service service) {
        return getServiceLatestCreatedPodName(service, Optional.empty());
    }

    public Optional<String> getServiceLatestCreatedPodName(Service service, Optional<String> groupName) {
        PodList podList = kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                        ApolloToKubernetes.getApolloPodUniqueIdentifier(environment, service, groupName))
                .list();

        if (podList == null) {
            return Optional.empty();
        }

        Optional<Pod> newestPod = podList.getItems()
                .stream()
                .sorted((o1, o2) -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    LocalDateTime pod1 = LocalDateTime.parse(o1.getStatus().getStartTime(), formatter);
                    LocalDateTime pod2 = LocalDateTime.parse(o2.getStatus().getStartTime(), formatter);
                    return pod1.compareTo(pod2);
                }).findFirst();

        return newestPod.map(pod -> pod
                .getMetadata()
                .getName());
    }

    public List<String> getPodContainerNames(String podName) {
        return kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(podName)
                .get()
                .getSpec()
                .getContainers()
                .stream()
                .map(Container::getName)
                .collect(Collectors.toList());
    }

    private PodStatus getPodStatus(String podName) {
        io.fabric8.kubernetes.api.model.PodStatus kubernetesPodStatus = kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(podName)
                .get()
                .getStatus();

        PodStatus podStatus = new PodStatus();
        podStatus.setName(podName);
        podStatus.setHostIp(kubernetesPodStatus.getHostIP());
        podStatus.setPodIp(kubernetesPodStatus.getPodIP());
        podStatus.setPhase(kubernetesPodStatus.getPhase());
        podStatus.setReason(kubernetesPodStatus.getReason());
        podStatus.setStartTime(kubernetesPodStatus.getStartTime());
        podStatus.setHasJolokia(getPodJolokiaPort(podName).isPresent());

        podStatus.setContainers(getPodContainerNames(podName));

        return podStatus;
    }

    private Optional<Integer> getPodJolokiaPort(String podName) {
        Pod pod = kubernetesClient
                .pods()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(podName)
                .get();

        if (pod == null) {
            return Optional.empty();
        }

        String jolokiaPort = pod
                .getMetadata()
                .getLabels()
                .get(APOLLO_JOLOKIA_PORT_LABEL);

        if (jolokiaPort == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(jolokiaPort));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private KubernetesClient createKubernetesClient(Environment environment) {
        try {
            Config config = new ConfigBuilder()
                    .withMasterUrl(environment.getKubernetesMaster())
                    .withOauthToken(environment.getKubernetesToken())
                    .build();

            return new DefaultKubernetesClient(config);
        } catch (Exception e) {
            logger.error("Could not create kubernetes client for environment {}", environment.getId(), e);
            throw new RuntimeException();
        }
    }

    public int getScalingFactor(Service service, String groupName) throws ApolloNotFoundException {
        io.fabric8.kubernetes.api.model.extensions.Deployment kubernetesDeployment = getKubernetesDeployment(service, Optional.of(groupName));

        if (kubernetesDeployment == null) {
            throw new ApolloNotFoundException("Could not find deployment for environment " + environment.getId() + ", service "
                    + service.getId() + " and group " + groupName + ", can't return the status!");
        }

        return Optional.ofNullable(kubernetesDeployment.getStatus().getAvailableReplicas()).orElse(0);
    }

    public void setScalingFactor(Service service, String groupName, int scalingFactor) throws ApolloNotFoundException {
        io.fabric8.kubernetes.api.model.extensions.Deployment kubernetesDeployment = getKubernetesDeployment(service, Optional.of(groupName));

        if (kubernetesDeployment == null) {
            throw new ApolloNotFoundException("Could not find deployment for environment " + environment.getId() + ", service "
                    + service.getId() + " and group " + groupName + ", can't return the status!");
        }

        kubernetesClient
                .extensions()
                .deployments()
                .inNamespace(environment.getKubernetesNamespace())
                .withName(kubernetesDeployment.getMetadata().getName())
                .scale(scalingFactor);
    }

    private io.fabric8.kubernetes.api.model.extensions.Deployment getKubernetesDeployment(Service service, Optional<String> groupName) {

        return kubernetesClient
                .extensions()
                .deployments()
                .inNamespace(environment.getKubernetesNamespace())
                .withLabel(ApolloToKubernetes.getApolloDeploymentUniqueIdentifierKey(),
                        ApolloToKubernetes.getApolloDeploymentUniqueIdentifierValue(environment, service, groupName))
                .list()
                .getItems()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public Boolean isEnvironmentHealthy() {
        try {
            kubernetesClient
                    .extensions()
                    .deployments()
                    .inNamespace(environment.getKubernetesNamespace())
                    .list();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}