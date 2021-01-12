package io.logz.apollo.blockers;

public class SingleRegionBlockerResponse {
    public static final String blockerName = "ServiceByRegionBlocker";
    private final boolean shouldBlock;
    private final boolean isReqToRunOnMultipleEnvironments;
    private final Integer serviceId;

    public SingleRegionBlockerResponse(boolean shouldBlock){
        this.shouldBlock = shouldBlock;
        this.serviceId = null;
        this.isReqToRunOnMultipleEnvironments = false;
    }

    public SingleRegionBlockerResponse(boolean shouldBlock, Integer serviceId, boolean isReqToRunOnMultipleEnvironments){
        this.shouldBlock = shouldBlock;
        this.serviceId = serviceId;
        this.isReqToRunOnMultipleEnvironments = isReqToRunOnMultipleEnvironments;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public boolean isShouldBlock() {
        return shouldBlock;
    }

    public boolean isReqToRunOnMultipleEnvironments() {
        return isReqToRunOnMultipleEnvironments;
    }
}

