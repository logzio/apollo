package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.Environment;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class EnvironmentsStackTest {

    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static final String stackName = "EnvironmentsStack";
    private static final boolean isStackEnabled = true;
    private static int stackId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        stackId = apolloTestClient.addEnvironmentsStack(stackName, isStackEnabled);
    }

    @Test
    public void addEnvironmentsToStack() throws Exception {
        int environmentsSize = (int) (Math.random() * 9 + 1);
        for(int i = 0; i < environmentsSize; i ++) {
            int environmentId =  ModelsGenerator.createAndSubmitEnvironment(apolloTestClient).getId();
            apolloTestClient.addEnvironmentToStack(environmentId, stackId);
        }
        List<Environment> environments = apolloTestClient.getEnvironmentsByStack(stackId);
        assertThat(apolloTestClient.getEnvironmentStackName(stackId)).isEqualTo(stackName);
        assertThat(apolloTestClient.isEnvironmentStackEnabled(stackId)).isEqualTo(isStackEnabled);
        assertThat(environments.size()).isEqualTo(environmentsSize);
    }

}
