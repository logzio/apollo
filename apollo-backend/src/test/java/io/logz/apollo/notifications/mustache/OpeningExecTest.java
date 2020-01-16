package io.logz.apollo.notifications.mustache;

import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.clients.ApolloWebSocketClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.StandaloneApollo;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class OpeningExecTest
{
    private final StandaloneApollo standaloneApollo;

    public OpeningExecTest() throws ScriptException, IOException, SQLException {

        standaloneApollo = StandaloneApollo.getOrCreateServer();
    }

    @Test
    public void test() throws Exception {

        ApolloWebSocketClient apolloWebSocketClient = standaloneApollo.createTestWebSocketClient();

        ApolloTestAdminClient apolloTestAdminClient = standaloneApollo.createTestAdminClient();
        ApolloTestClient apolloTestClient = standaloneApollo.createTestClient();

        apolloTestClient.getTestUser().setExecAllowed(true);

        apolloTestAdminClient.login();
        apolloTestAdminClient.signup(apolloTestClient.getTestUser(), Common.DEFAULT_PASSWORD);

        apolloTestClient.login();

        apolloWebSocketClient.setToken(apolloTestClient.getToken());
        apolloWebSocketClient.start();
    }

//    class ApolloWebSocketListener extends WebSocketListener {
//
//        private AtomicInteger openSessionCounter;
//        private AtomicInteger failedSessionCounter;
//
//        public ApolloWebSocketListener() {
//            openSessionCounter = new AtomicInteger(0);
//            failedSessionCounter = new AtomicInteger(0);
//        }
//
//        @Override
//        public void onOpen(WebSocket webSocket, Response response) {
//            openSessionCounter.incrementAndGet();
//        }
//
//        @Override
//        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
//            failedSessionCounter.incrementAndGet();
//        }
//
//        public AtomicInteger getOpenSessionCounter() {
//            return openSessionCounter;
//        }
//
//        public AtomicInteger getFailedSessionCounter() {
//            return failedSessionCounter;
//        }
//    }
}
