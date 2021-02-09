package io.logz.apollo.blockers;

import java.util.List;

public interface RequestBlockerFunction extends BlockerFunction {
    RequestBlockerResponse shouldBlock(List<Integer> serviceIds, int numOfEnvironments,String availability);
}
