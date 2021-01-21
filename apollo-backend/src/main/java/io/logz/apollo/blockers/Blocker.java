package io.logz.apollo.blockers;

public abstract class Blocker {
    private final Integer id;
    private final String name;
    private final String typeName;
    private final Integer serviceId;
    private final Integer environmentId;
    private final String availability;
    private final Integer stackId;
    private final Boolean isActive;

    public Blocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId, String availability, Integer stackId, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.typeName = typeName;
        this.serviceId = serviceId;
        this.environmentId = environmentId;
        this.availability = availability;
        this.stackId = stackId;
        this.isActive = isActive;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getTypeName() { return typeName; }

    public Integer getServiceId() {
        return serviceId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public Integer getStackId() {
        return stackId;
    }

    public String getAvailability() {
        return availability;
    }

    public Boolean getActive() {
        return isActive;
    }
}
