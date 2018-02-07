package io.logz.apollo.clients;

import io.logz.apollo.helpers.Common;

public class ApolloTestAdminClient extends ApolloAdminClient {

    public ApolloTestAdminClient(String hostname, int port, String protocol) {
        super(Common.DEFAULT_ADMIN_USERNAME, Common.DEFAULT_ADMIN_PASSWORD, protocol, hostname, port);
    }

}
