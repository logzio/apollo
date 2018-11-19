package io.logz.apollo.models;

import java.util.List;

public class EnvironmentServices {

    private Integer environmentId;
    private String environmentName;
    private List<Service> services;

    public EnvironmentServices() {

    }

    public EnvironmentServices(Integer environmentId, String environmentName, List<Service> services) {
        this.environmentId = environmentId;
        this.environmentName = environmentName;
        this.services = services;
    }

    public EnvironmentServices(Environment environment, List<Service> services) {
        this.environmentId = environment.getId();
        this.environmentName = environment.getName();
        this.services = services;
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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EnvironmentServices)) {
            return false;
        }
        EnvironmentServices environmentServices = (EnvironmentServices) o;
        return environmentServices.environmentId == this.environmentId &&
                environmentServices.environmentName.equals(this.environmentName) &&
                environmentServices.services.equals(this.services);
    }
}
