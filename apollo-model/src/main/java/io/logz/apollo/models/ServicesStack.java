package io.logz.apollo.models;

import java.util.ArrayList;
import java.util.List;

public class ServicesStack extends Stack {

    private static final StackType STACK_TYPE = StackType.SERVICES;

    List<Integer> services;

    public ServicesStack() {
        this.stackType = STACK_TYPE;
    }

    public ServicesStack(int id, String name, boolean isEnabled) {
        super(id, name, isEnabled, STACK_TYPE);
        this.services = new ArrayList<>();
    }

    public ServicesStack(String name, boolean isEnabled) {
        super(name, isEnabled, STACK_TYPE);
        this.services = new ArrayList<>();
    }

    public ServicesStack(int id, String name, boolean isEnabled, List<Integer> services) {
        super(id, name, isEnabled, STACK_TYPE);
        this.services = services;
    }

    public List<Integer> getServices() {
        return services;
    }

    public void setServices(List<Integer> services) {
        this.services = services;
    }
}
