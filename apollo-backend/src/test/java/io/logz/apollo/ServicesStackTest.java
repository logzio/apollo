package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.Service;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ServicesStackTest {

    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static final String stackName = "ServicesStack";
    private static final boolean isStackEnabled = true;
    private static int stackId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();
        stackId = apolloTestClient.addServicesStack(stackName, isStackEnabled);
    }

    @Test
    public void addServicesToStack() throws Exception {
        int servicesSize = (int) (Math.random() * 9 + 1);
        for(int i = 0; i < servicesSize; i ++) {
            int serviceId =  ModelsGenerator.createAndSubmitService(apolloTestClient).getId();
            apolloTestClient.addServiceToStack(serviceId, stackId);
        }
        List<Service> services = apolloTestClient.getServicesByStack(stackId);
        assertThat(apolloTestClient.getServiceStackName(stackId)).isEqualTo(stackName);
        assertThat(apolloTestClient.isServiceStackEnabled(stackId)).isEqualTo(isStackEnabled);
        assertThat(services.size()).isEqualTo(servicesSize);
    }

}
