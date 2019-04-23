package io.logz.apollo.models;

import java.util.List;

/**
 * Created by roiravhon on 2/22/17.
 */
public class KubernetesDeploymentStatus {

    private int serviceId;
    private int environmentId;
    private String gitCommitSha;
    private String gitCommitUrl;
    private Integer replicas;
    private Integer availableReplicas;
    private Integer updatedReplicas;
    private Integer unavailableReplicas;
    private List<PodStatus> podStatuses;
    private String groupName;

    public KubernetesDeploymentStatus() {
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

    public String getGitCommitSha() {
        return gitCommitSha;
    }

    public void setGitCommitSha(String gitCommitSha) {
        this.gitCommitSha = gitCommitSha;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public Integer getAvailableReplicas() {
        return availableReplicas;
    }

    public void setAvailableReplicas(Integer availableReplicas) {
        this.availableReplicas = availableReplicas;
    }

    public Integer getUpdatedReplicas() {
        return updatedReplicas;
    }

    public void setUpdatedReplicas(Integer updatedReplicas) {
        this.updatedReplicas = updatedReplicas;
    }

    public Integer getUnavailableReplicas() {
        return unavailableReplicas;
    }

    public void setUnavailableReplicas(Integer unavailableReplicas) {
        this.unavailableReplicas = unavailableReplicas;
    }

    public List<PodStatus> getPodStatuses() {
        return podStatuses;
    }

    public void setPodStatuses(List<PodStatus> podStatuses) {
        this.podStatuses = podStatuses;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGitCommitUrl() {
        return gitCommitUrl;
    }

    public void setGitCommitUrl(String gitCommitUrl) {
        this.gitCommitUrl = gitCommitUrl;
    }
}
