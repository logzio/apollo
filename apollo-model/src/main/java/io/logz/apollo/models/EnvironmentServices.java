package io.logz.apollo.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EnvironmentServices {

    private Integer environmentId;
    private String environmentName;
    private Map<Integer, Integer> serviceGroupMap = new HashMap<>();

    public EnvironmentServices() {

    }

    public EnvironmentServices(Integer environmentId, String environmentName, Map<Service,Optional<Group>> servicesAndGroups) {
        this.environmentId = environmentId;
        this.environmentName = environmentName;
        servicesAndGroups.forEach((service,group) -> serviceGroupMap.put(service.getId(),  group.isPresent() ? group.get().getId() : 0));
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

    public Map<Integer, Integer> getServiceGroupMap() {
        return serviceGroupMap;
    }

    public void setServiceGroupMap(Map<Integer, Integer> serviceGroupMap) {
        this.serviceGroupMap = serviceGroupMap;
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
                environmentServices.serviceGroupMap.equals(this.serviceGroupMap);
    }
}
