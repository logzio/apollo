package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.exceptions.ApolloNotAuthorizedException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.Group;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static io.logz.apollo.helpers.ModelsGenerator.createAndSubmitGroup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GroupTest {

    private static ApolloTestClient apolloTestClient;
    private static GroupDao groupDao;

    @BeforeClass
    public static void initialize() throws ScriptException, IOException, SQLException {
        apolloTestClient = Common.signupAndLogin();
        StandaloneApollo standaloneApollo = StandaloneApollo.getOrCreateServer();
        groupDao = standaloneApollo.getInstance(GroupDao.class);
    }

    @Test
    public void testAddAndGetGroup() throws ApolloClientException {
        // Add group
        Group testGroup = createAndSubmitGroup(apolloTestClient);
        // Make sure we can't add it again
        assertThatThrownBy(() -> apolloTestClient.addGroup(testGroup)).isInstanceOf(ApolloClientException.class);

        // Get the group back from the api and validate the returned value
        Group returnedGroup = apolloTestClient.getGroupByName(testGroup.getName());

        assertThat(returnedGroup.getName()).isEqualTo(testGroup.getName());
        assertThat(returnedGroup.getServiceId()).isEqualTo(testGroup.getServiceId());
        assertThat(returnedGroup.getEnvironmentId()).isEqualTo(testGroup.getEnvironmentId());
        assertThat(returnedGroup.getScalingFactor()).isEqualTo(testGroup.getScalingFactor());
        assertThat(returnedGroup.getJsonParams()).isEqualTo(testGroup.getJsonParams());
    }

    @Test
    public void testGetAllGroups() throws ApolloClientException {
        // Add group
        Group testGroup = createAndSubmitGroup(apolloTestClient);

        // Get all groups, and filter for ours
        Optional<Group> groupFromApi = apolloTestClient.getAllGroups().stream()
                .filter(group -> group.getName().equals(testGroup.getName())).findFirst();

        boolean found = false;

        // Make sure we got the correct one
        if (groupFromApi.isPresent()) {
            if (groupFromApi.get().getName().equals(testGroup.getName()) &&
                    groupFromApi.get().getServiceId() == testGroup.getServiceId() &&
                    groupFromApi.get().getEnvironmentId() == testGroup.getEnvironmentId() &&
                    groupFromApi.get().getScalingFactor() == testGroup.getScalingFactor() &&
                    groupFromApi.get().getJsonParams().equals(testGroup.getJsonParams())) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    public void testTryingToScaleGroupThatIsBlockedToScaling() throws ApolloClientException {
        Group group = createAndSubmitGroup(apolloTestClient);
        group.setScalingStatus(Group.ScalingStatus.BLOCKED);
        groupDao.updateGroup(group);
        assertThatThrownBy(() -> apolloTestClient.updateScalingFactor(group.getId(), group.getScalingFactor() + 1)).isInstanceOf(ApolloNotAuthorizedException.class);
    }
}