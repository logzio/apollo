package io.logz.apollo.helpers;

import io.logz.apollo.models.DeploymentRole;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.auth.PasswordManager;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.models.User;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Notification.NotificationType;
import io.logz.apollo.models.Notification;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

/**
 * Created by roiravhon on 12/20/16.
 */
public class ModelsGenerator {

    public static int DEFAULT_SCALING_FACTOR = 3;

    public static Environment createEnvironment() {
        Environment testEnvironment = new Environment();
        testEnvironment.setName("env-name-" + Common.randomStr(5));
        testEnvironment.setGeoRegion("us-east-" + Common.randomStr(5));
        testEnvironment.setAvailability("PROD-" + Common.randomStr(5));
        testEnvironment.setKubernetesMaster("kube.prod." + Common.randomStr(5));
        testEnvironment.setKubernetesToken("AaBbCc" + Common.randomStr(10));
        testEnvironment.setKubernetesNamespace("namespace-" + Common.randomStr(5));
        testEnvironment.setServicePortCoefficient(0);
        testEnvironment.setRequireDeploymentMessage(false);
        testEnvironment.setRequireDeploymentMessage(true);

        return testEnvironment;
    }

    public static Environment createAndSubmitEnvironment(ApolloTestClient apolloTestClient) throws ApolloClientException {
        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());
        return testEnvironment;
    }

    public static Group createGroup() {
        Group testGroup = new Group();
        testGroup.setName("group-name-" + Common.randomStr(5));
        testGroup.setScalingFactor(DEFAULT_SCALING_FACTOR);
        testGroup.setJsonParams(Common.generateJson("json", "params"));

        return testGroup;
    }

    public static DeployableVersion createDeployableVersion(Service relatedService) {
        DeployableVersion testDeployableVersion = new DeployableVersion();
        testDeployableVersion.setGitCommitSha("abc129aed837f6" + Common.randomStr(5));
        testDeployableVersion.setGithubRepositoryUrl("http://test.com/logzio/" + Common.randomStr(5));
        testDeployableVersion.setServiceId(relatedService.getId());

        return testDeployableVersion;
    }

    public static DeployableVersion createDeployableVersion(Service relatedService, String repositoryUrl, String commitSha) {
        DeployableVersion testDeployableVersion = new DeployableVersion();
        testDeployableVersion.setGitCommitSha(commitSha);
        testDeployableVersion.setGithubRepositoryUrl(repositoryUrl);
        testDeployableVersion.setServiceId(relatedService.getId());

        return testDeployableVersion;
    }

    public static DeployableVersion createAndSubmitDeployableVersion(ApolloTestClient apolloTestClient) throws ApolloClientException {

        // Needed for FK
        Service testService = createAndSubmitService(apolloTestClient);

        // Add deployable version
        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        return testDeployableVersion;
    }

    public static DeployableVersion createAndSubmitDeployableVersion(ApolloTestClient apolloTestClient, Service service) throws ApolloClientException {

        // Add deployable version
        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(service);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        return testDeployableVersion;
    }

    public static DeployableVersion createAndSubmitDeployableVersion(ApolloTestClient apolloTestClient, Service service, String repositoryUrl, String commitSha) throws ApolloClientException {

        // Add deployable version
        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(service, repositoryUrl, commitSha);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        return testDeployableVersion;
    }

    public static Service createService() {
        Service testService = new Service();
        testService.setName("Prod app " + Common.randomStr(5));
        testService.setDeploymentYaml("");  // TODO: fill something real
        testService.setServiceYaml("");  // TODO: fill something real
        testService.setIngressYaml("");  // TODO: fill something real
        testService.setIsPartOfGroup(false);

        return testService;
    }

    public static Service createAndSubmitService(ApolloTestClient apolloTestClient) throws ApolloClientException {
        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());
        return testService;
    }

    public static Group createAndSubmitGroup(ApolloTestClient apolloTestClient) throws ApolloClientException {
        return createAndSubmitGroup(apolloTestClient, createAndSubmitService(apolloTestClient).getId(), createAndSubmitEnvironment(apolloTestClient).getId());
    }

    public static Group createAndSubmitGroup(ApolloTestClient apolloTestClient, int environmentId) throws ApolloClientException {
        return createAndSubmitGroup(apolloTestClient, createAndSubmitService(apolloTestClient).getId(), environmentId);
    }

    public static Group createAndSubmitGroup(ApolloTestClient apolloTestClient, int serviceId, int environmentId) throws ApolloClientException {
        Group testGroup = createGroup();

        testGroup.setServiceId(serviceId);
        testGroup.setEnvironmentId(environmentId);

        apolloTestClient.addGroup(testGroup);

        testGroup.setId(apolloTestClient.getGroupByName(testGroup.getName()).getId());

        return testGroup;
    }

    public static Deployment createDeployment(Service relatedService, Environment relatedEnvironment,
                                              DeployableVersion relatedDeployableVersion) {
        return createDeployment(relatedService, relatedEnvironment, relatedDeployableVersion, null);
    }

    public static Deployment createDeployment(Service relatedService, Environment relatedEnvironment,
                                              DeployableVersion relatedDeployableVersion, String groupName) {

        Deployment testDeployment = new Deployment();
        testDeployment.setEnvironmentId(relatedEnvironment.getId());
        testDeployment.setServiceId(relatedService.getId());
        testDeployment.setDeployableVersionId(relatedDeployableVersion.getId());
        testDeployment.setLastUpdate(new Date());
        testDeployment.setUserEmail("user-" + Common.randomStr(5));
        testDeployment.setGroupName(groupName);
        testDeployment.setDeploymentMessage("message-" + Common.randomStr(5));
        return testDeployment;
    }

    public static Deployment createAndSubmitDeployment(ApolloTestClient apolloTestClient) throws Exception {

        // Add all foreign keys
        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        return createAndSubmitDeployment(apolloTestClient, testEnvironment, testService, testDeployableVersion);
    }

    public static Deployment createAndSubmitDeployment(ApolloTestClient apolloTestClient, Environment environment,
                                                       Service service, DeployableVersion deployableVersion) throws Exception {

        // Give the user permissions to deploy
        Common.grantUserFullPermissionsOnEnvironment(apolloTestClient, environment);

        // Now we have enough to create a deployment
        Deployment testDeployment = ModelsGenerator.createDeployment(service, environment, deployableVersion);
        MultiDeploymentResponseObject result = apolloTestClient.addDeployment(testDeployment);

        if (result.getSuccessful().size() > 0) {
            Deployment deployment = result.getSuccessful().get(0).getDeployment();
            testDeployment.setId(deployment.getId());
        } else {
            throw result.getUnsuccessful().get(0).getException();
        }

        return testDeployment;
    }

    public static DeploymentRole createDeploymentRole() {
        DeploymentRole testDeploymentRole = new DeploymentRole();
        testDeploymentRole.setName("DeploymentRole " + Common.randomStr(10));

        return testDeploymentRole;
    }

    public static DeploymentPermission createAllowDeploymentPermission(Optional<Environment> relatedEnvironment, Optional<Service> relatedService) {
        return createDeploymentPermission(relatedEnvironment, relatedService, true);
    }

    public static DeploymentPermission createDenyDeploymentPermission(Optional<Environment> relatedEnvironment, Optional<Service> relatedService) {
        return createDeploymentPermission(relatedEnvironment, relatedService, false);
    }

    public static BlockerDefinition createBlockerDefinition(Environment environment, Service service, String blockerTypeName, String blockerJsonConfiguration) {
        BlockerDefinition blockerDefinition = new BlockerDefinition();

        if (environment != null)
            blockerDefinition.setEnvironmentId(environment.getId());

        if (service != null)
            blockerDefinition.setServiceId(service.getId());

        blockerDefinition.setName("blocker-" + Common.randomStr(5));
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(true);

        return blockerDefinition;
    }

    public static BlockerDefinition createAndSubmitBlocker(ApolloTestAdminClient apolloTestAdminClient, String blockerTypeName,
                                                           String blockerJsonConfiguration, Environment environment,
                                                           Service service) throws Exception {

        BlockerDefinition testBlockerDefinition = ModelsGenerator.createBlockerDefinition(environment, service, blockerTypeName, blockerJsonConfiguration);
        testBlockerDefinition.setId(apolloTestAdminClient.addBlocker(testBlockerDefinition).getId());

        return testBlockerDefinition;
    }

    public static Notification createAndSubmitNotification(ApolloTestClient apolloTestClient,
                                                           NotificationType notificationType,
                                                           String notificationJsonConfiguration) throws ApolloClientException {

        Environment environment = createAndSubmitEnvironment(apolloTestClient);
        Service service = createAndSubmitService(apolloTestClient);
        return createAndSubmitNotification(apolloTestClient, environment, service, notificationType, notificationJsonConfiguration);
    }

    public static Notification createAndSubmitNotification(ApolloTestClient apolloTestClient,
                                                           Environment environment,
                                                           Service service,
                                                           NotificationType notificationType,
                                                           String notificationJsonConfiguration) throws ApolloClientException {


        Notification notification = new Notification();
        notification.setEnvironmentId(environment.getId());
        notification.setServiceId(service.getId());
        notification.setName("notification" + Common.randomStr(5));
        notification.setType(notificationType);
        notification.setNotificationJsonConfiguration(notificationJsonConfiguration);

        notification.setId(apolloTestClient.addNotification(notification).getId());
        return notification;
    }

    private static DeploymentPermission createDeploymentPermission(Optional<Environment> relatedEnvironment, Optional<Service> relatedService, boolean allow) {
        DeploymentPermission testDeploymentPermission = new DeploymentPermission();
        testDeploymentPermission.setName("DeploymentPermission " + Common.randomStr(10));
        relatedEnvironment.ifPresent(environment -> testDeploymentPermission.setEnvironmentId(environment.getId()));
        relatedService.ifPresent(service -> testDeploymentPermission.setServiceId(service.getId()));

        if (allow) {
            testDeploymentPermission.setPermissionType(DeploymentPermission.PermissionType.ALLOW);
        } else {
            testDeploymentPermission.setPermissionType(DeploymentPermission.PermissionType.DENY);
        }

        return testDeploymentPermission;
    }

    public static User createRegularUser() {
        return createUser(false);
    }

    public static User createAdminUser() {
        return createUser(true);
    }

    private static User createUser(boolean admin) {

        User testUser = new User();
        testUser.setUserEmail("tahat+" + Common.randomStr(5) + "@logz.io");
        testUser.setFirstName("Tahat " + Common.randomStr(5));
        testUser.setLastName("Tahatson " + Common.randomStr(5));
        testUser.setHashedPassword(PasswordManager.encryptPassword(Common.DEFAULT_PASSWORD));
        testUser.setAdmin(admin);

        return testUser;
    }

    public static void createAndSubmitPermissions(ApolloTestClient apolloTestClient, Optional<Environment> testEnvironment,
                                                  Optional<Service> testService, DeploymentPermission.PermissionType permissionType) throws ScriptException, IOException, SQLException, ApolloClientException {
        // Associate user with role, and permission to the first env
        ApolloTestAdminClient apolloTestAdminClient = Common.getAndLoginApolloTestAdminClient();
        DeploymentRole newDeploymentRole = ModelsGenerator.createDeploymentRole();
        newDeploymentRole.setId(apolloTestAdminClient.addDeploymentRole(newDeploymentRole).getId());

        DeploymentPermission newDeploymentPermission;

        if (permissionType == DeploymentPermission.PermissionType.ALLOW) {
            newDeploymentPermission = ModelsGenerator.createAllowDeploymentPermission(testEnvironment, testService);
        } else {
            newDeploymentPermission = ModelsGenerator.createDenyDeploymentPermission(testEnvironment, testService);
        }

        newDeploymentPermission.setId(apolloTestAdminClient.addDeploymentPermission(newDeploymentPermission).getId());

        apolloTestAdminClient.addDeploymentPermissionToDeploymentRole(newDeploymentRole.getId(), newDeploymentPermission.getId());
        apolloTestAdminClient.addUserToRole(apolloTestClient.getTestUser().getUserEmail(), newDeploymentRole.getId());
    }
}
