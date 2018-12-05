package io.logz.apollo.models;

import java.util.List;
import java.util.Optional;

public class ServiceGroupPair {
    private final Service service;
    private final Optional<List<Group>> groups;

    public ServiceGroupPair(Service service, Optional<List<Group>> groups) {
        this.service = service;
        this.groups = groups;
    }

    public Service getService() {
        return service;
    }

    public Optional<List<Group>> getGroupList() {
        return groups;
    }
}
