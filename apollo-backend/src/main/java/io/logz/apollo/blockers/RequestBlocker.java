package io.logz.apollo.blockers;

public class RequestBlocker extends Blocker {

    private final RequestBlockerFunction function;

    public RequestBlocker(Integer id, String name, String typeName, Integer serviceId, Integer environmentId,
                          Integer stackId, String availability, Boolean isActive, RequestBlockerFunction function) {
        super(id, name, typeName, serviceId, environmentId, availability, stackId, isActive);
        this.function = function;
    }

    public RequestBlockerFunction getFunction() {
        return function;
    }
}
