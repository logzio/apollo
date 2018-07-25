package io.logz.apollo.models;

/**
 * Created by roiravhon on 12/18/16.
 */
public class Environment {

    private Integer id;
    private String name;
    private String geoRegion;
    private String availability;
    private String kubernetesMaster;
    private String kubernetesToken;
    private String kubernetesNamespace;
    private Integer servicePortCoefficient;
    private Boolean requireDeploymentMessage;
    private Boolean requireHealthCheck;
    private Integer concurrencyLimit;

    public Environment() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeoRegion() {
        return geoRegion;
    }

    public void setGeoRegion(String geoRegion) {
        this.geoRegion = geoRegion;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getKubernetesMaster() {
        return kubernetesMaster;
    }

    public void setKubernetesMaster(String kubernetesMaster) {
        this.kubernetesMaster = kubernetesMaster;
    }

    public String getKubernetesToken() {
        return kubernetesToken;
    }

    public void setKubernetesToken(String kubernetesToken) {
        this.kubernetesToken = kubernetesToken;
    }

    public String getKubernetesNamespace() {
        return kubernetesNamespace;
    }

    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

    public Integer getServicePortCoefficient() {
        return servicePortCoefficient;
    }

    public void setServicePortCoefficient(Integer servicePortCoefficient) {
        this.servicePortCoefficient = servicePortCoefficient;
    }

    public Boolean getRequireDeploymentMessage() {
        return requireDeploymentMessage;
    }

    public void setRequireDeploymentMessage(Boolean requireDeploymentMessage) {
        this.requireDeploymentMessage = requireDeploymentMessage;
    }

    public Boolean getRequireHealthCheck() {
        return requireHealthCheck;
    }

    public void setRequireHealthCheck(Boolean requireHealthCheck) {
        this.requireHealthCheck = requireHealthCheck;
    }

    public Integer getConcurrencyLimit() {
        return concurrencyLimit;
    }

    public void setConcurrencyLimit(Integer concurrencyLimit) {
        this.concurrencyLimit = concurrencyLimit;
    }
}
