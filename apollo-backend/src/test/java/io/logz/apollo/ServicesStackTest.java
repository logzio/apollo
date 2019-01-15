package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.ServicesStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ServicesStackTest {

    private static StandaloneApollo standaloneApollo;
    private static ApolloTestClient apolloTestClient;
    private static int servicesSize;
    private static int serviceId;
    private static List<Integer> services = new ArrayList<>();
    private static ServicesStack servicesStack;
    private static int stackId;
    private static String newStackName;
    private static final boolean NEW_ENABLED = false;

    @BeforeClass
    public static void beforeClass() throws Exception {
        standaloneApollo = StandaloneApollo.getOrCreateServer();
        apolloTestClient = Common.signupAndLogin();

        servicesSize = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < servicesSize; i++) {
            serviceId = ModelsGenerator.createAndSubmitService(apolloTestClient).getId();
            services.add(serviceId);
        }
        servicesStack = ModelsGenerator.createAndSubmitServicesStack(apolloTestClient, services);
        stackId = servicesStack.getId();
    }

    @Before
    public void before() {
        newStackName = "new-stack-name" + Common.randomStr(5);
    }

    @Test
    public void testAddAndGetServicesStack() throws Exception {
        apolloTestClient.removeServiceFromStack(serviceId, stackId);
        assertThat(apolloTestClient.getServicesStack(stackId).getServices()).doesNotContain(serviceId);
        apolloTestClient.addServiceToStack(serviceId, stackId);
        assertThat(apolloTestClient.getServicesStack(stackId).getServices()).contains(serviceId);
    }

    @Test
    public void testUpdateServicesStack() throws Exception {
        List<Integer> newServicesList = new ArrayList<>();
        for (int i = 0; i < servicesSize; i++) {
            newServicesList.add(ModelsGenerator.createAndSubmitService(apolloTestClient).getId());
        }
        servicesStack.setServices(newServicesList);
        servicesStack.setName(newStackName);
        servicesStack.setEnabled(NEW_ENABLED);
        apolloTestClient.updateServicesStack(servicesStack);
        ServicesStack servicesStackFromDb = apolloTestClient.getServicesStack(stackId);
        this.assertThatServicesStacksEquals(servicesStack, servicesStackFromDb);
    }

    @Test
    public void testGetAllServicesStacks() throws Exception {
        List<Integer> servicesStacksIds = new ArrayList<>();
        int servicesStacksAmount = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < servicesStacksAmount; i++) {
            servicesStacksIds.add(ModelsGenerator.createAndSubmitServicesStack(apolloTestClient).getId());
        }
        List<ServicesStack> allServicesStacks = apolloTestClient.getAllServicesStacks();
        List<Integer> allServicesStacksIds = allServicesStacks.stream()
                                                                      .map(servicesStack -> servicesStack.getId())
                                                                      .collect(Collectors.toList());
        servicesStacksIds.stream().forEach(id -> {
            assertThat(allServicesStacksIds.contains(id)).isTrue();
        });
    }

    private void assertThatServicesStacksEquals(ServicesStack servicesStack1, ServicesStack servicesStack2) {
        assertThat(servicesStack1.getName()).isEqualTo(servicesStack2.getName());
        assertThat(servicesStack1.isEnabled()).isEqualTo(servicesStack2.isEnabled());
        assertThat(servicesStack1.getStackType()).isEqualByComparingTo(servicesStack2.getStackType());
        assertThat(servicesStack1.getServices()).isEqualTo(servicesStack2.getServices());
    }
}
