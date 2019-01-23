package io.logz.apollo.models;

public abstract class Stack {

    private int id;
    private String name;
    private boolean isEnabled;
    protected StackType stackType;

    public Stack() {}

    public Stack(int id, String name, boolean isEnabled, StackType stackType) {
        this.id = id;
        this.id = id;
        this.name = name;
        this.isEnabled = isEnabled;
        this.stackType = stackType;
    }

    public Stack(String name, boolean isEnabled, StackType stackType) {
        this.name = name;
        this.isEnabled = isEnabled;
        this.stackType = stackType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public StackType getStackType() {
        return stackType;
    }

    public void setStackType(StackType stackType) {
        this.stackType = stackType;
    }
}
