package io.logz.apollo.blockers;

import io.logz.apollo.models.Deployment;

import java.io.IOException;

/**
 * Created by roiravhon on 6/4/17.
 */
public interface BlockerFunction {
    void init(String jsonConfiguration) throws IOException;
    boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment, Blocker blocker);
}
