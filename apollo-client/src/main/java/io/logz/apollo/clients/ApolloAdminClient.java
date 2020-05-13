package io.logz.apollo.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import io.logz.apollo.models.DeploymentRole;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.User;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.exceptions.ApolloCouldNotLoginException;
import io.logz.apollo.exceptions.ApolloCouldNotSignupException;
import io.logz.apollo.exceptions.ApolloNotAuthenticatedException;
import io.logz.apollo.exceptions.ApolloNotAuthorizedException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.RestResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ApolloAdminClient {

    private final GenericApolloClient genericApolloClient;

    public ApolloAdminClient(String userName, String plainAdminPassword, String protocol, String hostname, int port) {
        genericApolloClient = new GenericApolloClient(userName, plainAdminPassword, protocol, hostname, port, Optional.empty());
    }

    public ApolloAdminClient(String userName, String plainAdminPassword, String protocol, String hostname, int port, String prefix) {
        genericApolloClient = new GenericApolloClient(userName, plainAdminPassword, protocol, hostname, port, Optional.of(prefix));
    }

    public void login() throws IOException, ApolloCouldNotLoginException {
        genericApolloClient.login();
    }

    public void signup(User signupUser, String plainPassword) throws IOException, ApolloCouldNotSignupException, ApolloNotAuthorizedException, ApolloNotAuthenticatedException {
        RestResponse response = genericApolloClient.post("/signup", generateSignupJson(signupUser, plainPassword));
        switch (response.getCode()) {
            case 200:
                break;
            case 401:
                throw new ApolloNotAuthenticatedException();
            case 403:
                throw new ApolloNotAuthorizedException();
            default:
                throw new ApolloCouldNotSignupException();
        }
    }

    public DeploymentRole addDeploymentRole(DeploymentRole deploymentRole) throws ApolloClientException {
        String requestBody = Common.generateJson("name", deploymentRole.getName());
        return genericApolloClient.postAndGetResult("/deployment-roles", requestBody, new TypeReference<DeploymentRole>() {});
    }

    public DeploymentRole getDeploymentRole(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/deployment-roles/" + id, new TypeReference<DeploymentRole>() {});
    }

    public List<DeploymentRole> getAllDeploymentRoles() throws ApolloClientException {
        return genericApolloClient.getResult("/deployment-roles", new TypeReference<List<DeploymentRole>>() {});
    }

    public DeploymentPermission addDeploymentPermission(DeploymentPermission deploymentPermission) throws ApolloClientException {
        String requestBody = Common.generateJson("name", deploymentPermission.getName(),
                "serviceId", String.valueOf(deploymentPermission.getServiceId()),
                "environmentId", String.valueOf(deploymentPermission.getEnvironmentId()),
                "permissionType", deploymentPermission.getPermissionType().toString());

        return genericApolloClient.postAndGetResult("/deployment-permission", requestBody, new TypeReference<DeploymentPermission>() {});
    }

    public DeploymentPermission getDeploymentPermission(int id) throws ApolloClientException {
        return genericApolloClient.getResult("/deployment-permission", new TypeReference<DeploymentPermission>() {});
    }

    public List<DeploymentPermission> getAllDeploymentPermissions() throws ApolloClientException {
        return genericApolloClient.getResult("/deployment-permission", new TypeReference<List<DeploymentPermission>>() {});
    }

    public void addUserToRole(String userEmail, int deploymentRoleId) throws ApolloClientException {
        String requestBody = Common.generateJson("userEmail", userEmail,
                "deploymentRoleId", String.valueOf(deploymentRoleId));

        genericApolloClient.postAndGetResult("/deployment-roles/add-user", requestBody, new TypeReference<Object>() {});
    }

    public void addDeploymentPermissionToDeploymentRole(int deploymentRoleId, int deploymentPermissionId) throws ApolloClientException {
        String requestBody = Common.generateJson("deploymentRoleId", String.valueOf(deploymentRoleId),
                "deploymentPermissionId", String.valueOf(deploymentPermissionId));

        genericApolloClient.postAndGetResult("/deployment-roles/add-deployment-permission", requestBody, new TypeReference<Object>() {});
    }

    public BlockerDefinition addBlocker(BlockerDefinition blockerDefinition) throws ApolloClientException {
        String requestBody = Common.generateJson("name", blockerDefinition.getName(),
                "environmentId", valueOfString(blockerDefinition.getEnvironmentId()),
                "serviceId", valueOfString(blockerDefinition.getServiceId()),
                "stackId", valueOfString(blockerDefinition.getStackId()),
                "availability", blockerDefinition.getAvailability(),
                "isActive", String.valueOf(blockerDefinition.getActive()),
                "blockerTypeName", blockerDefinition.getBlockerTypeName(),
                "blockerJsonConfiguration", blockerDefinition.getBlockerJsonConfiguration());

        return genericApolloClient.postAndGetResult("/blocker-definition", requestBody, new TypeReference<BlockerDefinition>() {});
    }

    public BlockerDefinition updateBlocker(BlockerDefinition blockerDefinition) throws ApolloClientException {
        String requestBody = Common.generateJson("id", String.valueOf(blockerDefinition.getId()),
                "name", blockerDefinition.getName(),
                "environmentId", valueOfString(blockerDefinition.getEnvironmentId()),
                "serviceId", valueOfString(blockerDefinition.getServiceId()),
                "stackId", valueOfString(blockerDefinition.getStackId()),
                "availability", blockerDefinition.getAvailability(),
                "isActive", String.valueOf(blockerDefinition.getActive()),
                "blockerTypeName", blockerDefinition.getBlockerTypeName(),
                "blockerJsonConfiguration", blockerDefinition.getBlockerJsonConfiguration());

        return genericApolloClient.putAndGetResult("/blocker-definition/" + blockerDefinition.getId(), requestBody, new TypeReference<BlockerDefinition>() {});
    }

    public void overrideBlockerByUser(User user, BlockerDefinition blockerDefinition) throws ApolloClientException {
        String requestBody = Common.generateJson("userEmail", user.getUserEmail(),
                "blockerId", String.valueOf(blockerDefinition.getId()));

        genericApolloClient.postAndGetResult("/blocker-definition/override/user", requestBody, new TypeReference<Object>() {});
    }

    public void deleteBlockerByUser(User user, BlockerDefinition blockerDefinition) throws ApolloClientException {
        String requestBody = Common.generateJson("userEmail", user.getUserEmail(),
                "blockerId", String.valueOf(blockerDefinition.getId()));

        genericApolloClient.deleteAndGetResult("/blocker-definition/override/user", requestBody, new TypeReference<Object>() {});
    }

    public User updateUser(User user) throws ApolloClientException {

        String requestBody = Common.generateJson("userEmail", user.getUserEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "password", user.getHashedPassword(),
                "isAdmin", String.valueOf(user.isAdmin()),
                "isEnabled", String.valueOf(user.isEnabled()));

        return genericApolloClient.putAndGetResult("/users", requestBody, new TypeReference<User>() {});
    }

    private String generateSignupJson(User user, String plainPassword) {
        return Common.generateJson( "firstName", user.getFirstName(),
                                    "lastName", user.getLastName(),
                                    "userEmail", user.getUserEmail(),
                                    "password", plainPassword);
    }

    private String valueOfString(Object object) {
        if (object != null) {
            return String.valueOf(object);
        }
        return null;
    }
}
