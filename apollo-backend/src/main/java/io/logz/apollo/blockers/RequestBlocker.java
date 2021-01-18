package io.logz.apollo.blockers;

public class RequestBlocker extends Blocker {

    private RequestBlockerFunction function;

    public RequestBlocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId,
                          Integer stackId, String availability, Boolean isActive, RequestBlockerFunction function) {
        super(id, name, typeName, serviceId, environmentId, availability, stackId, isActive);
        this.setFunction(function);
    }

    @Override
    protected void setFunction(BlockerFunction function) {
        this.function = (RequestBlockerFunction) function;
    }

    public RequestBlockerFunction getFunction() {
        return function;
    }
}
