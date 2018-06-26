package io.logz.apollo.models;

import io.logz.apollo.models.Deployment.DeploymentStatus;

import java.util.Date;

public class DeploymentHistoryDetails {

    private int id;
    private Date lastUpdate;
    private String environmentName;
    private String serviceName;
    private int deployableVersionId;
    private String groupName;
    private String userEmail;
    private DeploymentStatus status;
    private String deploymentMessage;

    public DeploymentHistoryDetails() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getDeployableVersionId() {
        return deployableVersionId;
    }

    public void setDeployableVersionId(int deployableVersionId) {
        this.deployableVersionId = deployableVersionId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public String getDeploymentMessage() {
        return deploymentMessage;
    }

    public void setDeploymentMessage(String deploymentMessage) {
        this.deploymentMessage = deploymentMessage;
    }
}
