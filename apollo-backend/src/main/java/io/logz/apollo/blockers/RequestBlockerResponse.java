package io.logz.apollo.blockers;

import java.util.List;

public class RequestBlockerResponse {

    private final boolean shouldBlock;
    private final List<Integer> serviceIds;
    private final String blockerName;

    public RequestBlockerResponse(boolean shouldBlock, String blockerName) {
        this.shouldBlock = shouldBlock;
        this.blockerName = blockerName;
        this.serviceIds = null;
    }

    public RequestBlockerResponse(boolean shouldBlock, String blockerName, List<Integer> serviceIds) {
        this.shouldBlock = shouldBlock;
        this.blockerName = blockerName;
        this.serviceIds = serviceIds;
    }

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public boolean isShouldBlock() {
        return shouldBlock;
    }

    public String getBlockerName() { return blockerName; }
}

