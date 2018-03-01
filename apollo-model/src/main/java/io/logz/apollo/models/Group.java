package io.logz.apollo.models;

public class Group {

    public enum ScalingStatus {
        PENDING,
        DONE,
        CANCELED,
        BLOCKED
    }

    private int id;
    private String name;
    private int serviceId;
    private int environmentId;
    private int scalingFactor;
    private Group.ScalingStatus scalingStatus;
    private String jsonParams;

    public Group() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public Group.ScalingStatus getScalingStatus() {
        return scalingStatus;
    }

    public void setScalingStatus(Group.ScalingStatus scalingStatus) {
        this.scalingStatus = scalingStatus;
    }

    public String getJsonParams() {
        return jsonParams;
    }

    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
    }
}
