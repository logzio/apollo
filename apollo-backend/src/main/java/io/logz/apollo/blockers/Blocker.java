package io.logz.apollo.blockers;

/**
 * Created by roiravhon on 6/4/17.
 */
public class Blocker {

    private final Integer id;
    private final String name;
    private final String typeName;
    private final Integer serviceId;
    private final Integer environmentId;
    private final Boolean isActive;
    private final BlockerFunction blockerFunction;

    public Blocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId, Boolean isActive, BlockerFunction blockerFunction) {
        this.id = id;
        this.name = name;
        this.typeName = typeName;
        this.serviceId = serviceId;
        this.environmentId = environmentId;
        this.isActive = isActive;
        this.blockerFunction = blockerFunction;
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

    public Boolean getActive() {
        return isActive;
    }

    public BlockerFunction getBlockerFunction() {
        return blockerFunction;
    }
}
