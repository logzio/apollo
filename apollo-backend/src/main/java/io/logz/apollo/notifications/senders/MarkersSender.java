package io.logz.apollo.notifications.senders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.models.DeploymentMarker;
import io.logz.apollo.models.DeploymentMarkers;
import io.logz.apollo.notifications.NotificationTemplateMetadata;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class MarkersSender implements NotificationSender {
    private static final Logger logger = LoggerFactory.getLogger(MarkersSender.class);
    private final String X_API_TOKEN_HEADER = "X-API-TOKEN";
    private final String apiToken;
    private final String markersEndpointUrl;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public MarkersSender(String notificationJsonConfiguration) throws IOException {
        objectMapper = new ObjectMapper();
        httpClient = new OkHttpClient();
        DeploymentMarkersConfiguration markersConfiguration = objectMapper.readValue(notificationJsonConfiguration, DeploymentMarkersConfiguration.class);
        this.apiToken = markersConfiguration.getApiToken();
        this.markersEndpointUrl = markersConfiguration.getMarkersEndpointUrl();
    }

    @Override
    public boolean send(NotificationTemplateMetadata notificationTemplateMetadata) {
        Request.Builder builder = new Request.Builder().url(this.markersEndpointUrl)
                .header(X_API_TOKEN_HEADER, this.apiToken);

        DeploymentMarkers deploymentMarkers = getDeploymentMarkers(notificationTemplateMetadata);
        executeMarkersRequest(httpClient, builder, deploymentMarkers);
        return true;
    }

    private void executeMarkersRequest(OkHttpClient httpClient, Request.Builder builder, DeploymentMarkers deploymentMarkers) {
        try {
            String markersJson = objectMapper.writeValueAsString(deploymentMarkers);

            Request request = builder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), markersJson)).build();
            try (okhttp3.Response response = httpClient.newCall(request).execute()) {
                if (HttpStatus.isPositive(response.code())) {
                    logger.info("successfully sent marker");
                } else {
                    logger.warn("exception while sending marker");
                }
            } catch (IOException e) {
                logger.warn("IO Exception when trying to send deployment markers request", e);
            }
        } catch (JsonProcessingException jsonProcessingException) {
            logger.warn("Could not process json", jsonProcessingException);
        }
    }

    private DeploymentMarkers getDeploymentMarkers(NotificationTemplateMetadata deployment) {
        DeploymentMarker deploymentMarker = new DeploymentMarker();
        deploymentMarker.setTitle(deployment.getServiceName());
        deploymentMarker.setDescription(String.format("Deployment to service: %s, with commit message: %s", deployment.getServiceName(), deployment.getDeploymentMessage()));

        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("deployment_message", String.valueOf(deployment.getDeploymentMessage()));
        metadata.put("deployment_id", String.valueOf(deployment.getDeploymentId()));
        metadata.put("deployment_status", String.valueOf(deployment.getStatus()));
        metadata.put("group_name", String.valueOf(deployment.getGroupName()));
        metadata.put("service_name", String.valueOf(deployment.getServiceName()));
        metadata.put("environment_name", String.valueOf(deployment.getEnvironmentName()));
        deploymentMarker.setMetadata(metadata);

        DeploymentMarkers deploymentMarkers = new DeploymentMarkers();
        deploymentMarkers.setMarkers(ImmutableList.of(deploymentMarker));
        return deploymentMarkers;
    }

    public static class DeploymentMarkersConfiguration {
        private String apiToken;
        private String markersEndpointUrl;

        public DeploymentMarkersConfiguration() {
        }

        public String getApiToken() {
            return apiToken;
        }

        public String getMarkersEndpointUrl() {
            return markersEndpointUrl;
        }
    }
}


