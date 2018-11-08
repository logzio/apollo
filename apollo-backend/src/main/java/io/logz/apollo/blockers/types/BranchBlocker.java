package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.BlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.scm.GithubConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment) {
        DeployableVersion deployableVersion = blockerInjectableCommons.getDeployableVersionDao()
                                                                      .getDeployableVersion(deployment.getDeployableVersionId());

        Boolean shouldBlock = true;
        String repoName = GithubConnector.getRepoNameFromRepositoryUrl(deployableVersion.getGithubRepositoryUrl());

        for (String branch : branchBlockerConfiguration.getBranchesNames()) {
            if (blockerInjectableCommons.getGithubConnector().isCommitInBranchHistory(repoName,
                    branch, deployableVersion.getGitCommitSha())) {

                logger.info("Commit sha {} is part of branch {} on repo {}, not blocking!",
                        deployableVersion.getGitCommitSha(), branch, deployableVersion.getGithubRepositoryUrl());

                shouldBlock = false;
                break;
            }
        }

        if (shouldBlock) {
            logger.info("Commit sha {} is not part of branches {} on repo {}, blocking!",
                    deployableVersion.getGitCommitSha(), String.join(", ", branchBlockerConfiguration.getBranchesNames()),
                    deployableVersion.getGithubRepositoryUrl());
        }

        return shouldBlock;
    }

    public static class BranchBlockerConfiguration {
        private List<String> branchesNames;

        public BranchBlockerConfiguration() {
        }

        public List<String> getBranchesNames() {
            return branchesNames;
        }

        public void setBranceshNames(List<String> branchesNames) {
            this.branchesNames = branchesNames;
        }
    }
}
