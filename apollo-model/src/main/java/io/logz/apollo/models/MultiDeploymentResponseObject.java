package io.logz.apollo.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiDeploymentResponseObject {

    private final static String GROUP = "group";
    private final static String ENVIRONMENT = "environment";
    private final static String SERVICE = "service";
    private final static String REASON = "reason";
    private final static String DEPLOYMENT = "deployment";

    private List<Map<String, Object>> successful;
    private List<Map<String, Object>> unsuccessful;

    public MultiDeploymentResponseObject() {
        successful = new ArrayList<>();
        unsuccessful = new ArrayList<>();
    }

    public void addUnsuccessful(int groupId, String reason) {
        Map<String, Object> unsuccessfulGroup = new HashMap<>();
        unsuccessfulGroup.put(GROUP, groupId);
        unsuccessfulGroup.put(REASON, reason);
        unsuccessful.add(unsuccessfulGroup);
    }

    public void addSuccessful(int groupId, Deployment deployment) {
        Map<String, Object> successfulGroup = new HashMap<>();
        successfulGroup.put(GROUP, groupId);
        successfulGroup.put(DEPLOYMENT, deployment);
        successful.add(successfulGroup);
    }

    public void addUnsuccessful(int environmentId, int serviceId, String reason) {
        Map<String, Object> unsuccessfulEnvAndService = new HashMap<>();
        unsuccessfulEnvAndService.put(ENVIRONMENT, environmentId);
        unsuccessfulEnvAndService.put(SERVICE, serviceId);
        unsuccessfulEnvAndService.put(REASON, reason);
        unsuccessful.add(unsuccessfulEnvAndService);
    }

    public void addSuccessful(int environmentId, int serviceId, Deployment deployment) {
        Map<String, Object> successfulEnvAndService = new HashMap<>();
        successfulEnvAndService.put(ENVIRONMENT, environmentId);
        successfulEnvAndService.put(SERVICE, serviceId);
        successfulEnvAndService.put(DEPLOYMENT, deployment);
        successful.add(successfulEnvAndService);
    }

    public List<Map<String, Object>> getUnsuccessful() {
        return unsuccessful;
    }

    public List<Map<String, Object>> getSuccessful() {
        return successful;
    }
}
