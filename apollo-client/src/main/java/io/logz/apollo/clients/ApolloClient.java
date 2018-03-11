package io.logz.apollo.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import io.logz.apollo.models.User;
import io.logz.apollo.models.BlockerDefinition;

import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.exceptions.ApolloCouldNotLoginException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.Service;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Notification;
import io.logz.apollo.models.DeploymentGroupsResponseObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApolloClient {

    private final GenericApolloClient genericApolloClient;

    public ApolloClient(String userName, String plainPassword, String protocol, String hostname, int port) {
        genericApolloClient = new GenericApolloClient(userName, plainPassword, protocol, hostname, port, Optional.empty());
    }

    public ApolloClient(String userName, String plainPassword, String protocol, String hostname, int port, String prefix) {
        genericApolloClient = new GenericApolloClient(userName, plainPassword, protocol, hostname, port, Optional.of(prefix));
    }

    public void login() throws IOException, ApolloCouldNotLoginException {
        genericApolloClient.login();
    }

    public List<User> getAllUsers() throws ApolloClientException {
        return genericApolloClient.getResult("/users", new TypeReference<List<User>>(){});
    }

    public Environment addEnvironment(Environment environment) throws ApolloClientException {
        String requestBody = Common.generateJson("name", environment.getName(), "geoRegion", environment.getGeoRegion(),
                "availability", environment.getAvailability(), "kubernetesMaster", environment.getKubernetesMaster(),
                "kubernetesToken", environment.getKubernetesToken(), "kubernetesNamespace", environment.getKubernetesNamespace(),
                "servicePortCoefficient", String.valueOf(environment.getServicePortCoefficient()),
                "requireDeploymentMessage", String.valueOf(environment.getRequireDeploymentMessage()),
                "requiresHealthCheck", String.valueOf(environment.getRequireHealthCheck()));

        return genericApolloClient.postAndGetResult("/environment", requestBody, new TypeReference<Environment>(){});
    }

    public Environment getEnvironment(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/environment/" + id, new TypeReference<Environment>(){});
    }

    public List<Environment> getAllEnvironments() throws ApolloClientException {
        return genericApolloClient.getResult("/environment", new TypeReference<List<Environment>>(){});
    }

    public Service addService(Service service) throws ApolloClientException {
        String requestBody = Common.generateJson("name", service.getName(),
                "deploymentYaml", service.getDeploymentYaml(),
                "serviceYaml", service.getServiceYaml(),
                "isPartOfGroup", String.valueOf(service.getIsPartOfGroup()));
        return genericApolloClient.postAndGetResult("/service", requestBody, new TypeReference<Service>(){});
    }

    public Service getService(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/service/" + id, new TypeReference<Service>(){});
    }

    public List<Service> getAllServices() throws ApolloClientException {
        return genericApolloClient.getResult("/service", new TypeReference<List<Service>>(){});
    }

    public DeployableVersion addDeployableVersion(DeployableVersion deployableVersion) throws ApolloClientException {
        String requestBody = Common.generateJson("gitCommitSha", deployableVersion.getGitCommitSha(),
                "githubRepositoryUrl", deployableVersion.getGithubRepositoryUrl(),
                "serviceId", String.valueOf(deployableVersion.getServiceId()));

        return genericApolloClient.postAndGetResult("/deployable-version", requestBody, new TypeReference<DeployableVersion>(){});
    }

    public DeployableVersion getDeployableVersion(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/deployable-version/" + id, new TypeReference<DeployableVersion>(){});
    }

    public List<DeployableVersion> getAllDeployableVersions() throws ApolloClientException {
        return genericApolloClient.getResult("/deployable-version/", new TypeReference<List<DeployableVersion>>(){});
    }

    public DeployableVersion getDeployableVersionFromSha(String sha, int serviceId) throws ApolloClientException {
        return genericApolloClient.getResult("/deployable-version/sha/" + sha + "/service/" + serviceId, new TypeReference<DeployableVersion>() {});
    }

    public List<DeployableVersion> getLatestDeployableVersionsByServiceId(int serviceId) throws ApolloClientException {
        return genericApolloClient.getResult("/deployable-version/latest/service/" + serviceId, new TypeReference<List<DeployableVersion>>() {});
    }

    public Deployment addDeployment(Deployment deployment) throws ApolloClientException {
        String requestBody = Common.generateJson("environmentId", String.valueOf(deployment.getEnvironmentId()),
                "serviceId", String.valueOf(deployment.getServiceId()),
                "deployableVersionId", String.valueOf(deployment.getDeployableVersionId()),
                "deploymentMessage", deployment.getDeploymentMessage());

        return genericApolloClient.postAndGetResult("/deployment", requestBody, new TypeReference<Deployment>() {});
    }

    public DeploymentGroupsResponseObject addDeployment(Deployment deployment, String groupIdsCsv) throws ApolloClientException {
        String requestBody = Common.generateJson("environmentId", String.valueOf(deployment.getEnvironmentId()),
                "serviceId", String.valueOf(deployment.getServiceId()),
                "deployableVersionId", String.valueOf(deployment.getDeployableVersionId()),
                "groupIdsCsv", groupIdsCsv,
                "deploymentMessage", deployment.getDeploymentMessage());

        return genericApolloClient.postAndGetResult("/deployment-groups", requestBody, new TypeReference<DeploymentGroupsResponseObject>() {});
    }

    public Deployment getDeployment(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/deployment/" + id, new TypeReference<Deployment>() {});
    }

    public List<Deployment> getAllDeployments() throws ApolloClientException {
        return genericApolloClient.getResult("/deployment", new TypeReference<List<Deployment>>() {});
    }

    public Group addGroup(Group group) throws ApolloClientException {
        String requestBody = Common.generateJson("name", String.valueOf(group.getName()),
                "serviceId", String.valueOf(group.getServiceId()),
                "environmentId", String.valueOf(group.getEnvironmentId()),
                "scalingFactor", String.valueOf(group.getScalingFactor()),
                "jsonParams", group.getJsonParams());

        return  genericApolloClient.postAndGetResult("/group", requestBody, new TypeReference<Group>() {});
    }

    public Group getGroup(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/group/" + id, new TypeReference<Group>() {});
    }

    public Group getGroupByName(String name) throws ApolloClientException {
        return genericApolloClient.getResult("/group/name/" + name, new TypeReference<Group>() {});
    }

    public List<Group> getAllGroups() throws ApolloClientException {
        return genericApolloClient.getResult("/group", new TypeReference<List<Group>>() {});
    }

    public Group updateGroup(int groupId, String name, int serviceId, int environmentId, int scalingFactor, String jsonParams) throws ApolloClientException {
        String requestBody = Common.generateJson("id", String.valueOf(groupId), "name", name, "serviceId", String.valueOf(serviceId),
                "environmentId", String.valueOf(environmentId), "scalingFactor", String.valueOf(scalingFactor), "jsonParams", jsonParams);
        return genericApolloClient.putAndGetResult("/group/" + groupId, requestBody, new TypeReference<Group>() {});
    }

    public Group updateScalingFactor(int groupId, int scalingFactor) throws ApolloClientException {
        String requestBody = Common.generateJson("id", String.valueOf(groupId), "scalingFactor", String.valueOf(scalingFactor));
        return genericApolloClient.putAndGetResult("/scaling/" + groupId, requestBody, new TypeReference<Group>() {});
    }

    public int getScalingFactor(int groupId) throws ApolloClientException {
        return genericApolloClient.getResult("/scaling/apollo-factor/" + groupId, new TypeReference<Integer>() {});
    }

    public int getKubeScalingFactor(int groupId) throws ApolloClientException {
        return genericApolloClient.getResult("/scaling/kubernetes-factor/" + groupId, new TypeReference<Integer>() {});
    }

    public Service updateService(int id, String name, String deploymentYaml, String serviceYaml, Boolean isPartOfGroup) throws ApolloClientException {
        String requestBody = Common.generateJson("id", String.valueOf(id), "name", name, "deploymentYaml", deploymentYaml, "serviceYaml", serviceYaml, "isPartOfGroup", isPartOfGroup.toString());
        return genericApolloClient.putAndGetResult("/service/" + id, requestBody, new TypeReference<Service>() {});
    }

    public BlockerDefinition getBlockerDefinition(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/blocker-definition/" + id, new TypeReference<BlockerDefinition>() {});
    }

    public List<BlockerDefinition> getAllBlockerDefinitions() throws ApolloClientException {
        return genericApolloClient.getResult("/blocker-definition/", new TypeReference<List<BlockerDefinition>>() {});
    }

    public BlockerDefinition updateBlockerDefinitionActiveness(int id, String active) throws ApolloClientException {
        return genericApolloClient.putAndGetResult("/blocker-definition/" + id + "/active/" + active, "", new TypeReference<BlockerDefinition>() {});
    }

    public Notification addNotification(Notification notification) throws ApolloClientException {
        String requestBody = Common.generateJson("name", notification.getName(),
                "environmentId", String.valueOf(notification.getEnvironmentId()),
                "serviceId", String.valueOf(notification.getServiceId()),
                "type", String.valueOf(notification.getType()),
                "notificationJsonConfiguration", notification.getNotificationJsonConfiguration());

        return genericApolloClient.postAndGetResult("/notification", requestBody, new TypeReference<Notification>() {});
    }

    public Notification updateNotification(Notification notification) throws ApolloClientException {
        String requestBody = Common.generateJson("id", String.valueOf(notification.getId()),
                "name", notification.getName(),
                "environmentId", String.valueOf(notification.getEnvironmentId()),
                "serviceId", String.valueOf(notification.getServiceId()),
                "type", String.valueOf(notification.getType()),
                "notificationJsonConfiguration", notification.getNotificationJsonConfiguration());

        return genericApolloClient.putAndGetResult("/notification/" + notification.getId(), requestBody, new TypeReference<Notification>() {});
    }

    public Notification getNotification(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/notification/" + id, new TypeReference<Notification>() {});
    }

    public List<Notification> getAllNotifications() throws ApolloClientException {
        return genericApolloClient.getResult("/notification/", new TypeReference<List<Notification>>() {});
    }

    public List<KubernetesDeploymentStatus> getCurrentServiceStatus(int serviceId) throws ApolloClientException {
        return genericApolloClient.getResult("/status/service/" + serviceId, new TypeReference<List<KubernetesDeploymentStatus>>() {});
    }

    public Map<Integer, Boolean> getHealth() throws ApolloClientException {
        return genericApolloClient.getResult("/health", new TypeReference<Map<Integer, Boolean>>() {});
    }
}
