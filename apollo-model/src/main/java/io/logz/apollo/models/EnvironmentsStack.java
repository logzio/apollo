package io.logz.apollo.models;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentsStack extends Stack {

    private static final StackType STACK_TYPE = StackType.ENVIRONMENTS;

    List<Integer> environments;

    public EnvironmentsStack() {
        this.stackType = STACK_TYPE;
    }

    public EnvironmentsStack(int id, String name, boolean isEnabled) {
        super(id, name, isEnabled, STACK_TYPE);
        this.environments = new ArrayList<>();
    }

    public EnvironmentsStack(String name, boolean isEnabled) {
        super(name, isEnabled, STACK_TYPE);
        this.environments = new ArrayList<>();
    }

    public EnvironmentsStack(int id, String name, boolean isEnabled, List<Integer> environments) {
        super(id, name, isEnabled, STACK_TYPE);
        this.environments = environments;
    }

    public List<Integer> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Integer> environments) {
        this.environments = environments;
    }
}
