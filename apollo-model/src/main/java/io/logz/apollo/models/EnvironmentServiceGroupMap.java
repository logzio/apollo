package io.logz.apollo.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvironmentServiceGroupMap {

    private Integer environmentId;
    private String environmentName;
    private Map<String,List<String>> serviceGroupMap = new HashMap<>();

    public EnvironmentServiceGroupMap() {

    }
    public EnvironmentServiceGroupMap(Integer environmentId, String environmentName, Map<String,List<String>> serviceGroupMap) {
        this.environmentId = environmentId;
        this.environmentName = environmentName;
        this.serviceGroupMap = serviceGroupMap;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public Map<String,List<String>> getServiceGroupMap() {
        return serviceGroupMap;
    }

    public void setServiceGroupMap(Map<String, List<String>> serviceGroupMap) {
        this.serviceGroupMap = serviceGroupMap;
    }
}
