package io.logz.apollo.clients;

import io.logz.apollo.helpers.Common;
import io.logz.apollo.models.User;

public class ApolloTestClient extends ApolloClient {

    private final User testUser;

    public ApolloTestClient(User user, String hostname, int port, String protocol) {
        super(user.getUserEmail(), Common.DEFAULT_PASSWORD, protocol, hostname, port);
        testUser = user;
    }

    public User getTestUser() {
        return testUser;
    }

}