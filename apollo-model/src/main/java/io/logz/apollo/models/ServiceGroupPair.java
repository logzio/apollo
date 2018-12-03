package io.logz.apollo.models;

import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

public class ServiceGroupPair {
    private Optional<Pair<Service, Optional<List<Group>>>> pair;

    public ServiceGroupPair() {
        pair = Optional.empty();
    }

    public ServiceGroupPair(Service service, Optional<List<Group>> groups) {
        this.pair = Optional.of(new Pair<>(service, groups));
    }

    public Optional<Pair<Service, Optional<List<Group>>>> getPair() {
        return pair;
    }

    public void setPair(Optional<Pair<Service, Optional<List<Group>>>> pair) {
        this.pair = pair;
    }

    public Service getService() {
        return pair.get().getKey();
    }

    public Optional<List<Group>> getGroupList() {
        return pair.get().getValue();
    }

    public boolean isPresent() {
        return pair.isPresent();
    }
}
