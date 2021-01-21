package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import io.logz.apollo.notifications.ApolloNotifications;
import io.logz.apollo.models.Notification.NotificationType;
import io.logz.apollo.models.Notification;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

public class NotificationsTest {

    private static int mockHttpServerPort = Common.getAvailablePort();
    private static final String slackPath = "/slack/submit";
    private static final String markersPath = "/markers/create-marker";
    private static String slackMockDomain = "http://localhost:" + mockHttpServerPort + slackPath;
    private static String markersServiceMockDomain = "http://localhost:" + mockHttpServerPort + markersPath;
    private static String slackNotificationConfiguration;
    private static String markerNotificationConfiguration;

    private MockServerClient mockServerClient;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this, mockHttpServerPort);

    @BeforeClass
    public static void beforeClass() {
        slackNotificationConfiguration = "{\n" +
                "        \"channel\": \"#slack\",\n" +
                "        \"webhookUrl\": \"" + slackMockDomain + "\"\n" +
                "}";

        markerNotificationConfiguration = "{\n" +
                "        \"apiToken\": \"bla\",\n" +
                "        \"markersEndpointUrl\": \"" + markersServiceMockDomain + "\"\n" +
                "}";
    }

    @Test
    public void testSlackNotification() throws Exception {

        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        Notification notification = ModelsGenerator.createAndSubmitNotification(apolloTestClient,
                NotificationType.SLACK, slackNotificationConfiguration);

        Environment environment = apolloTestClient.getEnvironment(notification.getEnvironmentId());
        Service service = apolloTestClient.getService(notification.getServiceId());

        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion);

        mockSlackServer();

        StandaloneApollo.getOrCreateServer().getInstance(ApolloNotifications.class).notify(Deployment.DeploymentStatus.DONE, deployment);

        waitForRequest(slackPath);

        mockServerClient.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(slackPath),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    public void testMarkerNotification() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        Notification notification = ModelsGenerator.createAndSubmitNotification(apolloTestClient,
                NotificationType.MARKER, markerNotificationConfiguration);

        Environment environment = apolloTestClient.getEnvironment(notification.getEnvironmentId());
        Service service = apolloTestClient.getService(notification.getServiceId());

        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion);

        mockMarkersService();

        StandaloneApollo.getOrCreateServer().getInstance(ApolloNotifications.class).notify(Deployment.DeploymentStatus.STARTED, deployment);

        waitForRequest(markersPath);

        mockServerClient.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(markersPath),
                VerificationTimes.exactly(1)
        );
    }

    @Test
    public void testNotificationsNotSentWhenShouldBeFiltered() throws Exception {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();

        Notification markerNotification = ModelsGenerator.createAndSubmitNotification(apolloTestClient,
                NotificationType.MARKER, markerNotificationConfiguration);

        ModelsGenerator.createAndSubmitNotification(apolloTestClient,
                NotificationType.SLACK, slackNotificationConfiguration);

        Environment environment = apolloTestClient.getEnvironment(markerNotification.getEnvironmentId());
        Service service = apolloTestClient.getService(markerNotification.getServiceId());

        DeployableVersion deployableVersion = ModelsGenerator.createAndSubmitDeployableVersion(apolloTestClient, service);
        Deployment deployment = ModelsGenerator.createAndSubmitDeployment(apolloTestClient, environment, service, deployableVersion);

        mockMarkersService();
        mockSlackServer();

        StandaloneApollo.getOrCreateServer().getInstance(ApolloNotifications.class).notify(Deployment.DeploymentStatus.PENDING, deployment);

        waitForRequest(markersPath);

        mockServerClient.verifyZeroInteractions();

        StandaloneApollo.getOrCreateServer().getInstance(ApolloNotifications.class).notify(Deployment.DeploymentStatus.STARTED, deployment);

        waitForRequest(slackPath);

        mockServerClient.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(slackPath),
                VerificationTimes.exactly(0)
        );
    }

    private void mockSlackServer() {

        mockServerClient.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(slackPath)
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
        );
    }

    private void mockMarkersService() {

        mockServerClient.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(markersPath)
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
        );
    }

    private void waitForRequest(String requestPath) throws InterruptedException {
        long waitDuration = 100;
        while (mockServerClient.retrieveRecordedRequests(HttpRequest.request()
                .withMethod("POST")
                .withPath(requestPath)
        ).length == 0 && waitDuration-- > 0) {
            Thread.sleep(10);
        }
    }
}
