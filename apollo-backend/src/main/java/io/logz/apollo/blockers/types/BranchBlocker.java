package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.scm.GithubConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by roiravhon on 6/4/17.
 */
@BlockerType(name = "branch")
public class BranchBlocker implements DeploymentBlockerFunction {

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

        String repoName = GithubConnector.getRepoNameFromRepositoryUrl(deployableVersion.getGithubRepositoryUrl());

        Optional<String> validBranch = Arrays.stream(branchBlockerConfiguration.getbranchName().split(","))
                .filter(branch -> blockerInjectableCommons.getGithubConnector().isCommitInBranchHistory(repoName, branch, deployableVersion.getGitCommitSha()))
                .findFirst();

        Boolean shouldBlock = !validBranch.isPresent();

        if (shouldBlock) {
            logger.info("Commit sha {} is not part of branches {} on repo {}, blocking!",
                    deployableVersion.getGitCommitSha(), String.join(", ", branchBlockerConfiguration.getbranchName()),
                    deployableVersion.getGithubRepositoryUrl());
        } else {
            logger.info("Commit sha {} is part of branch {} on repo {}, not blocking!",
                    deployableVersion.getGitCommitSha(), validBranch.get(),
                    deployableVersion.getGithubRepositoryUrl());
        }

        return shouldBlock;
    }

    public static class BranchBlockerConfiguration {
        private String branchName;

        public BranchBlockerConfiguration() {
        }

        public String getbranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }
    }
}
