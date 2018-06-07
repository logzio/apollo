package io.logz.apollo.blockers.types;

import io.logz.apollo.blockers.BlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.scm.GithubConnector;
import java.io.IOException;

@BlockerType(name = "githubCommitStatus")
public class GHCommitStatusBlocker implements BlockerFunction {

    @Override
    public void init(String jsonConfiguration) throws IOException {

    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment) {
        DeployableVersion deployableVersion = blockerInjectableCommons.getDeployableVersionDao()
                .getDeployableVersion(deployment.getDeployableVersionId());

        String repoName = GithubConnector.getRepoNameFromRepositoryUrl(deployableVersion.getGithubRepositoryUrl());

        return !blockerInjectableCommons.getGithubConnector().isCommitStatusOK(repoName, deployableVersion.getGitCommitSha());
    }
}
