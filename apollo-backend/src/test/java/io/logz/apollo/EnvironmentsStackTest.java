package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.EnvironmentsStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class EnvironmentsStackTest {

    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static int environmentsSize;
    private static int environmentId;
    private static List<Integer> environments = new ArrayList<>();
    private static EnvironmentsStack environmentsStack;
    private static int stackId;
    private static String newStackName;
    private static final boolean NEW_ENABLED = false;

    @BeforeClass
    public static void beforeClass() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();

        environmentsSize = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < environmentsSize; i++) {
            environmentId = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient).getId();
            environments.add(environmentId);
        }
        environmentsStack = ModelsGenerator.createAndSubmitEnvironmentsStack(apolloTestClient, environments);
        stackId = environmentsStack.getId();
    }

    @Before
    public void before() {
        newStackName = "new-stack-name" + Common.randomStr(5);
    }

    @Test
    public void testAddAndGetEnvironmentsStack() throws Exception {
        apolloTestClient.removeEnvironmentFromStack(environmentId, stackId);
        assertThat(apolloTestClient.getEnvironmentsStack(stackId).getEnvironments()).doesNotContain(environmentId);
        apolloTestClient.addEnvironmentToStack(environmentId, stackId);
        assertThat(apolloTestClient.getEnvironmentsStack(stackId).getEnvironments()).contains(environmentId);
    }

    @Test
    public void testUpdateEnvironmentsStack() throws Exception {
        List<Integer> newEnvironmentsList = new ArrayList<>();
        for (int i = 0; i < environmentsSize; i++) {
            newEnvironmentsList.add(ModelsGenerator.createAndSubmitEnvironment(apolloTestClient).getId());
        }
        environmentsStack.setEnvironments(newEnvironmentsList);
        environmentsStack.setName(newStackName);
        environmentsStack.setEnabled(NEW_ENABLED);
        apolloTestClient.updateEnvironmentsStack(environmentsStack);
        EnvironmentsStack environmentsStackFromDb = apolloTestClient.getEnvironmentsStack(stackId);
        this.assertThatEnvironmentsStacksEquals(environmentsStack, environmentsStackFromDb);
    }

    @Test
    public void testGetAllEnvironmentsStacks() throws Exception {
        List<Integer> environmentsStacksIds = new ArrayList<>();
        int environmentsStacksAmount = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < environmentsStacksAmount; i++) {
            environmentsStacksIds.add(ModelsGenerator.createAndSubmitEnvironmentsStack(apolloTestClient).getId());
        }
        List<EnvironmentsStack> allEnvironmentsStacks = apolloTestClient.getAllEnvironmentsStacks();
        List<Integer> allEnvironmentsStacksIds = allEnvironmentsStacks.stream()
                                                                      .map(environmentsStack -> environmentsStack.getId())
                                                                      .collect(Collectors.toList());
        environmentsStacksIds.stream().forEach(id -> {
            assertThat(allEnvironmentsStacksIds.contains(id)).isTrue();
        });
    }

    private void assertThatEnvironmentsStacksEquals(EnvironmentsStack environmentsStack1, EnvironmentsStack environmentsStack2) {
        assertThat(environmentsStack1.getName()).isEqualTo(environmentsStack2.getName());
        assertThat(environmentsStack1.isEnabled()).isEqualTo(environmentsStack2.isEnabled());
        assertThat(environmentsStack1.getStackType()).isEqualByComparingTo(environmentsStack2.getStackType());
        assertThat(environmentsStack1.getEnvironments()).isEqualTo(environmentsStack2.getEnvironments());
    }
}
