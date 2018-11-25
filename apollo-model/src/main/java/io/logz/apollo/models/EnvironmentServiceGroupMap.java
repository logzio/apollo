package io.logz.apollo.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnvironmentServiceGroupMap {

    private Integer environmentId;
    private String environmentName;
    private Map<String,List<String>> serviceGroupMap = new HashMap<>();

    public EnvironmentServiceGroupMap() {

    }

    public EnvironmentServiceGroupMap(Integer environmentId, String environmentName, Map<Service,Optional<List<Group>>> serviceAndGroups) {
        this.environmentId = environmentId;
        this.environmentName = environmentName;
        serviceAndGroups.forEach((service, groups) -> {
            if(groups.isPresent()) {
                List<String> groupsNames = new ArrayList<>();
                groups.get().forEach(group -> groupsNames.add(group.getName()));
                this.serviceGroupMap.put(service.getName(), groupsNames);
            }
            else {
                this.serviceGroupMap.put(service.getName(),new ArrayList<>());
            }
        });
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EnvironmentServiceGroupMap)) {
            return false;
        }
        EnvironmentServiceGroupMap environmentServiceGroupMap = (EnvironmentServiceGroupMap) o;
        return environmentServiceGroupMap.environmentId == this.environmentId &&
                environmentServiceGroupMap.environmentName.equals(this.environmentName) &&
                environmentServiceGroupMap.serviceGroupMap.equals(this.serviceGroupMap);
    }
}
