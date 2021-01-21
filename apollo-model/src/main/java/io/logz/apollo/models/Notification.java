package io.logz.apollo.models;

public class Notification {

    private int id;
    private String name;
    private Integer serviceId;
    private Integer environmentId;
    private NotificationType type;
    private String notificationJsonConfiguration;
    public enum NotificationType {
        SLACK, MARKER
    }

    public Notification() {
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

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getNotificationJsonConfiguration() {
        return notificationJsonConfiguration;
    }

    public void setNotificationJsonConfiguration(String notificationJsonConfiguration) {
        this.notificationJsonConfiguration = notificationJsonConfiguration;
    }
}
