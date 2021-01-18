package io.logz.apollo.blockers;

import java.util.List;

public interface RequestBlockerFunction extends BlockerFunction {
    SingleRegionBlockerResponse shouldBlock(List<Integer> serviceIds, List<Integer> environmentIds, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability);
}
