package io.logz.apollo.blockers;

import io.logz.apollo.models.Deployment;

import java.util.List;

public interface RequestBlockerFunction extends CrossBlockerFunction{
    SingleRegionBlockerResponse shouldBlock(List<Integer> serviceIds, List<Integer> environmentIds, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability);
}
