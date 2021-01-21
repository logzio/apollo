package io.logz.apollo.blockers;

/**
 * Created by roiravhon on 6/4/17.
 */
public class DeploymentBlocker extends Blocker {

    private final DeploymentBlockerFunction function;

    public DeploymentBlocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId,
                             Integer stackId, String availability, Boolean isActive, DeploymentBlockerFunction function) {
        super(id, name, typeName, serviceId, environmentId, availability, stackId, isActive);
        this.function = function;
    }

    public DeploymentBlockerFunction getFunction() {
        return function;
    }
}
