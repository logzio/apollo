package io.logz.apollo.blockers;

/**
 * Created by roiravhon on 6/4/17.
 */
public class Blocker <T extends CrossBlockerFunction>{

    private final Integer id;
    private final String name;
    private final String typeName;
    private final Integer serviceId;
    private final Integer environmentId;
    private final String availability;
    private final Integer stackId;
    private final Boolean isActive;
    private final T crossBlockerFunction;

    public Blocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId, Integer stackId, String availability,
                   Boolean isActive, T crossBlockerFunction) {
        this.id = id;
        this.name = name;
        this.typeName = typeName;
        this.serviceId = serviceId;
        this.environmentId = environmentId;
        this.stackId = stackId;
        this.availability = availability;
        this.isActive = isActive;
        this.crossBlockerFunction = crossBlockerFunction;
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

    public T getCrossBlockerFunction() {
        return crossBlockerFunction;
    }
}
