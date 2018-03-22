package io.logz.apollo;

import static org.assertj.core.api.Assertions.assertThat;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.Service;
import org.junit.Ignore;
import org.junit.Test;
import java.util.List;

public class StatusTest {

    // TODO: Finish this test and write other tests to StatusController when we can mock k8s.

    @Ignore
    @Test
    public void testGetCurrentServiceStatus() throws Exception {
        // Initiate everything needed for the test
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        // Environments
        Environment environment1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment environment2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        // Grant permissions
        Common.grantUserFullPermissionsOnEnvironment(apolloTestClient, environment1);
        Common.grantUserFullPermissionsOnEnvironment(apolloTestClient, environment2);
        // Services
        Service service1 = ModelsGenerator.createAndSubmitService(apolloTestClient);
        Service service2 = ModelsGenerator.createAndSubmitService(apolloTestClient);
        // Group
        Group group = ModelsGenerator.createAndSubmitGroup(apolloTestClient, service1.getId(), environment1.getId());

        apolloTestClient.updateService(service1.getId(), service1.getName(), service1.getDeploymentYaml(), service1.getServiceYaml(), service1.getIngressYaml(), true);

        List<KubernetesDeploymentStatus> result = apolloTestClient.getCurrentServiceStatus(service1.getId());

        KubernetesDeploymentStatus expectedKubernetesDeploymentStatus = new KubernetesDeploymentStatus();
        expectedKubernetesDeploymentStatus.setServiceId(service1.getId());
        expectedKubernetesDeploymentStatus.setGroupName(group.getName());

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).equals(expectedKubernetesDeploymentStatus));
    }
}