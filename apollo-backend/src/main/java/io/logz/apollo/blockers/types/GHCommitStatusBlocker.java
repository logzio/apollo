package io.logz.apollo.blockers.types;

import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.scm.GithubConnector;

import java.io.IOException;

@BlockerType(name = BlockerTypeName.GITHUB_COMMIT_STATUS)
public class GHCommitStatusBlocker implements DeploymentBlockerFunction {

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
