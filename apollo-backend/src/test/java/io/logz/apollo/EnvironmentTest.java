package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.models.Environment;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitEnvironment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by roiravhon on 12/19/16.
 */
public class EnvironmentTest {

    @Test
    public void testAddAndGetEnvironment() throws ApolloClientException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add environment
        Environment testEnvironment = createAndSubmitEnvironment(apolloTestClient);

        // Make sure we cant add that again
        assertThatThrownBy(() -> apolloTestClient.addEnvironment(testEnvironment)).isInstanceOf(ApolloClientException.class);

        // Get the environment back from the api and validate the returned value
        Environment returnedEnv = apolloTestClient.getEnvironment(testEnvironment.getId());

        assertThat(returnedEnv.getName()).isEqualTo(testEnvironment.getName());
        assertThat(returnedEnv.getGeoRegion()).isEqualTo(testEnvironment.getGeoRegion());
        assertThat(returnedEnv.getAvailability()).isEqualTo(testEnvironment.getAvailability());
        assertThat(returnedEnv.getKubernetesMaster()).isEqualTo(testEnvironment.getKubernetesMaster());
        assertThat(returnedEnv.getKubernetesNamespace()).isEqualTo(testEnvironment.getKubernetesNamespace());
        assertThat(returnedEnv.getKubernetesToken()).contains("*");
    }

    @Test
    public void testGetAllEnvironments() throws ApolloClientException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add environment
        Environment testEnvironment = createAndSubmitEnvironment(apolloTestClient);

        // Get all environments, and filter for ours
        Optional<Environment> environmentFromApi = apolloTestClient.getAllEnvironments().stream()
                .filter(environment -> environment.getId() == testEnvironment.getId()).findFirst();

        boolean found = false;

        // Make sure we got the correct one
        assertThat(environmentFromApi).isPresent();
        assertEqualEnvironments(environmentFromApi.get(), testEnvironment);
    }

    @Test
    public void testGetAllActiveEnvironments() throws ApolloClientException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add environments
        Environment testEnvironment = createAndSubmitEnvironment(apolloTestClient, false);
        Environment activeTestEnvironment = createAndSubmitEnvironment(apolloTestClient);

        // Get all environments should return everything
        List<Environment> allEnvironments = apolloTestClient.getAllEnvironments();

        boolean foundNotActiveEnvironment = allEnvironments.stream()
                .anyMatch(environment -> environment.getId().equals(testEnvironment.getId()));
        assertThat(foundNotActiveEnvironment).isTrue();

        boolean foundActiveEnvironment = allEnvironments.stream()
                .anyMatch(environment -> environment.getId().equals(activeTestEnvironment.getId()));
        assertThat(foundActiveEnvironment).isTrue();


        List<Environment> allActiveEnvironments = apolloTestClient.getAllActiveEnvironments();

        // Inactive environments should be excluded from search results
        foundNotActiveEnvironment = allActiveEnvironments.stream()
                .anyMatch(environment -> environment.getId().equals(testEnvironment.getId()));
        assertThat(foundNotActiveEnvironment).isFalse();

        // Active environment should be present in response
        foundActiveEnvironment = allActiveEnvironments.stream()
                .anyMatch(environment -> environment.getId().equals(activeTestEnvironment.getId()));
        assertThat(foundActiveEnvironment).isTrue();
    }

    @Test
    public void testUpdateEnvironment() throws ApolloClientException {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        // Add environment
        Environment testEnvironment = createAndSubmitEnvironment(apolloTestClient);

        // Create a new environment object
        Environment newEnvironment = new Environment();
        newEnvironment.setId(testEnvironment.getId());
        newEnvironment.setName("eu-west-1234-logz-staging");
        newEnvironment.setGeoRegion("eu-west-1234");
        newEnvironment.setAvailability("staging-224");
        newEnvironment.setKubernetesMaster(testEnvironment.getKubernetesMaster());
        newEnvironment.setKubernetesNamespace("staging");
        newEnvironment.setConcurrencyLimit(2);
        newEnvironment.setServicePortCoefficient(testEnvironment.getServicePortCoefficient());
        newEnvironment.setRequireDeploymentMessage(testEnvironment.getRequireDeploymentMessage());
        newEnvironment.setConcurrencyLimit(testEnvironment.getConcurrencyLimit());
        newEnvironment.setIsActive(false);

        // Update previously saved environment
        apolloTestClient.updateEnvironment(newEnvironment);

        // Updated environment should be present in response
        Optional<Environment> environmentFromApi = apolloTestClient.getAllEnvironments().stream()
                .filter(environment -> environment.getId().equals(newEnvironment.getId()))
                .findFirst();

        assertThat(environmentFromApi).isPresent();

        // Make sure we got the correct one
        assertEqualEnvironments(environmentFromApi.get(), newEnvironment);
    }

    private void assertEqualEnvironments(Environment first, Environment second) {
        assertThat(first.getName()).isEqualTo(second.getName());
        assertThat(first.getGeoRegion()).isEqualTo(second.getGeoRegion());
        assertThat(first.getAvailability()).isEqualTo(second.getAvailability());
        assertThat(first.getKubernetesMaster()).isEqualTo(second.getKubernetesMaster());
        assertThat(first.getKubernetesToken()).contains("*");
        assertThat(first.getKubernetesNamespace()).isEqualTo(second.getKubernetesNamespace());
        assertThat(first.getConcurrencyLimit()).isEqualTo(second.getConcurrencyLimit());
        assertThat(first.getIsActive()).isEqualTo(second.getIsActive());
    }
}
