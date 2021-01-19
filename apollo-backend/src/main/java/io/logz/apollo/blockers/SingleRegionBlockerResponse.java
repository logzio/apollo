package io.logz.apollo.blockers;

import java.util.List;

public class SingleRegionBlockerResponse {
    public static final String BLOCKER_NAME = "ServiceByRegionBlocker";

    public enum BlockerCause {NONE, MULTIPLE_ENVIRONMENTS, SERVICE_ALREADY_RUN}

    private final boolean shouldBlock;
    private final List<Integer> serviceIds;
    private final BlockerCause blockerCause;

    public SingleRegionBlockerResponse(boolean shouldBlock) {
        this.shouldBlock = shouldBlock;
        this.serviceIds = null;
        this.blockerCause = BlockerCause.NONE;
    }

    public SingleRegionBlockerResponse(boolean shouldBlock, List<Integer> serviceIds, BlockerCause blockerCause) {
        this.shouldBlock = shouldBlock;
        this.serviceIds = serviceIds;
        this.blockerCause = blockerCause;
    }

    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    public boolean isShouldBlock() {
        return shouldBlock;
    }

    public BlockerCause getBlockerCause() {
        return blockerCause;
    }
}

