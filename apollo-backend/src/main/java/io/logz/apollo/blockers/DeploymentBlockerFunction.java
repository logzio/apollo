package io.logz.apollo.blockers;

import io.logz.apollo.models.Deployment;

/**
 * Created by roiravhon on 6/4/17.
 */
public interface DeploymentBlockerFunction extends BlockerFunction {
     boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment);
}
