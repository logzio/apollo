package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.Blocker;
import io.logz.apollo.blockers.BlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.scm.GithubConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by roiravhon on 6/4/17.
 */
@BlockerType(name = "branch")
public class BranchBlocker implements BlockerFunction {

    private static final Logger logger = LoggerFactory.getLogger(BranchBlocker.class);
    private BranchBlockerConfiguration branchBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        branchBlockerConfiguration = mapper.readValue(jsonConfiguration, BranchBlockerConfiguration.class);
    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment, Blocker blocker) {
        DeployableVersion deployableVersion = blockerInjectableCommons.getDeployableVersionDao()
                                                                      .getDeployableVersion(deployment.getDeployableVersionId());

        String repoName = GithubConnector.getRepoNameFromRepositoryUrl(deployableVersion.getGithubRepositoryUrl());

        if (!blockerInjectableCommons.getGithubConnector().isCommitInBranchHistory(repoName,
                        branchBlockerConfiguration.getBranchName(), deployableVersion.getGitCommitSha())) {

            logger.info("Commit sha {} is not part of branch {} on repo {}, blocking!",
                    deployableVersion.getGitCommitSha(), branchBlockerConfiguration.getBranchName(),
                    deployableVersion.getGithubRepositoryUrl());

            return true;
        }

        return false;
    }

    public static class BranchBlockerConfiguration {
        private String branchName;

        public BranchBlockerConfiguration() {
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }
    }
}
