package io.logz.apollo;

import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.User;
import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.exceptions.ApolloCouldNotLoginException;
import io.logz.apollo.exceptions.ApolloCouldNotSignupException;
import io.logz.apollo.exceptions.ApolloNotAuthorizedException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by roiravhon on 11/22/16.
 */
public class AuthTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthTest.class);
    private final StandaloneApollo standaloneApollo;

    public AuthTest() throws ScriptException, IOException, SQLException {

        standaloneApollo = StandaloneApollo.getOrCreateServer();
    }

    @Test
    public void testSignup() throws Exception {

        ApolloTestAdminClient apolloTestAdminClient = standaloneApollo.createTestAdminClient();
        ApolloTestClient apolloTestClient = standaloneApollo.createTestClient();

        // Check that user that is not authenticated cannot sign up
        assertThatThrownBy(() -> apolloTestAdminClient.signup(apolloTestClient.getTestUser(), Common.DEFAULT_PASSWORD))
                .isInstanceOf(ApolloNotAuthorizedException.class);

        // Login admin
        apolloTestAdminClient.login();

        // Signup the user
        apolloTestAdminClient.signup(apolloTestClient.getTestUser(), Common.DEFAULT_PASSWORD);

        // Make sure we cant signup again
        assertThatThrownBy(() -> apolloTestAdminClient.signup(apolloTestClient.getTestUser(), Common.DEFAULT_PASSWORD))
                .isInstanceOf(ApolloCouldNotSignupException.class);
    }

    @Test
    public void testLogin() throws Exception {

        ApolloTestAdminClient apolloTestAdminClient = standaloneApollo.createTestAdminClient();
        ApolloTestClient apolloTestClient = standaloneApollo.createTestClient();

        // Try to login before signup
        assertThatThrownBy(apolloTestClient::login).isInstanceOf(ApolloCouldNotLoginException.class);

        // Login admin and signup user
        apolloTestAdminClient.login();
        apolloTestAdminClient.signup(apolloTestClient.getTestUser(), Common.DEFAULT_PASSWORD);

        // Login the new user
        apolloTestClient.login();
    }

    @Test
    public void testEnabled() throws Exception {
        ApolloTestAdminClient apolloTestAdminClient = standaloneApollo.createTestAdminClient();
        ApolloTestClient apolloTestClient = standaloneApollo.createTestClient();

        User testUser = apolloTestClient.getTestUser();

        // Login admin and signup user
        apolloTestAdminClient.login();
        apolloTestAdminClient.signup(testUser, Common.DEFAULT_PASSWORD);

        apolloTestClient.login();

        // Check that its valid
        assertThat(apolloTestClient.getAllServices()).size().isGreaterThanOrEqualTo(0);

        testUser.setEnabled(false);
        apolloTestAdminClient.updateUser(testUser);

        // Check that user is forbidden even without logging in
        assertThatThrownBy(apolloTestClient::getAllUsers).isInstanceOf(ApolloNotAuthorizedException.class);

        // Check that user cant log in
        assertThatThrownBy(apolloTestClient::login).isInstanceOf(ApolloCouldNotLoginException.class);

        testUser.setEnabled(true);
        apolloTestAdminClient.updateUser(testUser);

        // Check that its valid again
        assertThat(apolloTestClient.getAllServices()).size().isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testGetAllUsers() throws Exception {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Get all users
        List<User> allUsers = apolloTestClient.getAllUsers();

        // Find our user in the list
        Optional<User> userFromApi = allUsers.stream().filter(user -> user.getUserEmail().equals(apolloTestClient.getTestUser().getUserEmail())).findFirst();

        boolean userFound = false;
        if (userFromApi.isPresent()) {

            if (userFromApi.get().getFirstName().equals(apolloTestClient.getTestUser().getFirstName()) &&
                userFromApi.get().getLastName().equals(apolloTestClient.getTestUser().getLastName()) &&
                userFromApi.get().isAdmin() == apolloTestClient.getTestUser().isAdmin() &&
                userFromApi.get().getHashedPassword().contains("*")) {

                userFound = true;
            }
        }

        assertThat(userFound).isTrue();
    }

    @Test
    public void testDeploymentWithNoPermissions() throws ApolloClientException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add all foreign keys
        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        Deployment testDeployment = ModelsGenerator.createDeployment(testService, testEnvironment, testDeployableVersion);
        assertThat(apolloTestClient.addDeployment(testDeployment).getUnsuccessful().get(0).getException().getMessage()).isEqualTo("Not Authorized!");
    }

    @Test
    public void testDeploymentWithWildCardService() throws ApolloClientException, ScriptException, IOException, SQLException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add two environments, one that will get permissions and one that does not
        Environment firstTestEnvironment = ModelsGenerator.createEnvironment();
        Environment secondTestEnvironment = ModelsGenerator.createEnvironment();
        firstTestEnvironment.setId(apolloTestClient.addEnvironment(firstTestEnvironment).getId());
        secondTestEnvironment.setId(apolloTestClient.addEnvironment(secondTestEnvironment).getId());

        // Foreign keys
        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());
        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        // Create the permission
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(firstTestEnvironment), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        Deployment okDeployment = ModelsGenerator.createDeployment(testService, firstTestEnvironment, testDeployableVersion);
        apolloTestClient.addDeployment(okDeployment);

        Deployment failDeployment = ModelsGenerator.createDeployment(testService, secondTestEnvironment, testDeployableVersion);
        assertThat(apolloTestClient.addDeployment(failDeployment).getUnsuccessful().get(0).getException().getMessage()).isEqualTo("Not Authorized!");
    }

    @Test
    public void testDeploymentWithWildCardEnvironment() throws ApolloClientException, ScriptException, IOException, SQLException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        // Add two services, one that will get permissions and one that does not
        Service firstTestService = ModelsGenerator.createService();
        firstTestService.setId(apolloTestClient.addService(firstTestService).getId());
        Service secondTestService = ModelsGenerator.createService();
        secondTestService.setId(apolloTestClient.addService(secondTestService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(firstTestService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        // Associate user with group, and permission to the first service
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.empty(), Optional.of(firstTestService), DeploymentPermission.PermissionType.ALLOW);

        Deployment okDeployment = ModelsGenerator.createDeployment(firstTestService, testEnvironment, testDeployableVersion);
        apolloTestClient.addDeployment(okDeployment);

        Deployment failDeployment = ModelsGenerator.createDeployment(secondTestService, testEnvironment, testDeployableVersion);
        assertThat(apolloTestClient.addDeployment(failDeployment).getUnsuccessful().get(0).getException().getMessage()).startsWith("DeployableVersion with");
        assertThat(apolloTestClient.addDeployment(failDeployment).getUnsuccessful().get(0).getException().getMessage()).contains("is not applicable on service");
    }

    @Test
    public void testDeploymentWithBroadAllowAndSpecificDeny() throws ApolloClientException, ScriptException, IOException, SQLException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        // Add two services, one that will get permissions and one that does not
        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        // Set broad permissions
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(testEnvironment), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        // And block the specific service
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(testEnvironment), Optional.of(testService), DeploymentPermission.PermissionType.DENY);

        Deployment failDeployment = ModelsGenerator.createDeployment(testService, testEnvironment, testDeployableVersion);
        assertThat(apolloTestClient.addDeployment(failDeployment).getUnsuccessful().get(0).getException().getMessage()).isEqualTo("Not Authorized!");
    }

    @Test
    public void testDeploymentWithBroadDenyAndSpecificAllow() throws ApolloClientException, ScriptException, IOException, SQLException {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        // Add two services, one that will get permissions and one that does not
        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        // Set broad deny permissions
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(testEnvironment), Optional.empty(), DeploymentPermission.PermissionType.DENY);

        // And allow the specific service
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(testEnvironment), Optional.of(testService), DeploymentPermission.PermissionType.ALLOW);

        Deployment successfulDeployment = ModelsGenerator.createDeployment(testService, testEnvironment, testDeployableVersion);
        apolloTestClient.addDeployment(successfulDeployment);
    }

    @Test
    public void testDeploymentWithBothBroadAllowAndDeny() throws ApolloClientException, ScriptException, IOException, SQLException {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Environment testEnvironment = ModelsGenerator.createEnvironment();
        testEnvironment.setId(apolloTestClient.addEnvironment(testEnvironment).getId());

        // Add two services, one that will get permissions and one that does not
        Service testService = ModelsGenerator.createService();
        testService.setId(apolloTestClient.addService(testService).getId());

        DeployableVersion testDeployableVersion = ModelsGenerator.createDeployableVersion(testService);
        testDeployableVersion.setId(apolloTestClient.addDeployableVersion(testDeployableVersion).getId());

        // Set broad allow permission
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.of(testEnvironment), Optional.empty(), DeploymentPermission.PermissionType.ALLOW);

        // Set broad deny permission
        ModelsGenerator.createAndSubmitPermissions(apolloTestClient, Optional.empty(), Optional.of(testService), DeploymentPermission.PermissionType.DENY);

        Deployment failDeployment = ModelsGenerator.createDeployment(testService, testEnvironment, testDeployableVersion);
        assertThat(apolloTestClient.addDeployment(failDeployment).getUnsuccessful().get(0).getException().getMessage()).isEqualTo("Not Authorized!");
    }
}
