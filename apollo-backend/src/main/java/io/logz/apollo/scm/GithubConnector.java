package io.logz.apollo.scm;

import io.logz.apollo.configuration.ApolloConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class GithubConnector {

    private static final Logger logger = LoggerFactory.getLogger(GithubConnector.class);

    private final GitHub gitHub;

    @Inject
    public GithubConnector(ApolloConfiguration apolloConfiguration) {
        try {
            logger.info("Initializing Github Connector");

            // If no user or oauth was provided, attempt to go anonymous
            if (StringUtils.isEmpty(apolloConfiguration.getScm().getGithubLogin()) || StringUtils.isEmpty(apolloConfiguration.getScm().getGithubOauthToken())) {
                logger.info("Trying to connect anonymously to GitHub");
                gitHub = GitHub.connectAnonymously();
                logger.info("Succeeded to connect anonymously to GitHub");
            } else {
                logger.info("Trying to connect to GitHub");
                gitHub = GitHub.connect(apolloConfiguration.getScm().getGithubLogin(), apolloConfiguration.getScm().getGithubOauthToken());
                logger.info("Succeeded to connect to GitHub");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not open connection to Github!", e);
        }
    }

    public Optional<CommitDetails> getCommitDetails(String githubRepo, String sha) {
        try {
            logger.info("Getting commit details for sha {} on url {}", sha, githubRepo);
            GHCommit commit = gitHub.getRepository(githubRepo).getCommit(sha);

            GHUser author = commit.getAuthor();
            logger.info("1) Author of commit sha {} is {}", sha, author);
            String committerName = (author == null) ? null : author.getName();
            if (committerName == null || committerName.isEmpty()) {
                logger.info("2) Committer name of commit sha {} is {}", sha, committerName);
                committerName = author.getLogin();
                logger.info("3) Committer name of commit sha {} is {}", sha, committerName);
            }

            CommitDetails commitDetails = new CommitDetails(sha, commit.getHtmlUrl().toString(),
                    commit.getCommitShortInfo().getMessage(), commit.getCommitDate(), commit.getLastStatus(),
                    author.getAvatarUrl(), committerName);
            logger.info("CommitDetails: {}", commitDetails);
            return Optional.of(commitDetails);
        } catch (IOException e) {
            logger.warn("Could not get commit details from Github!", e);
            return Optional.empty();
        }
    }

    public List<String> getLatestCommitsShaOnMaster(String githubRepo, int commitsAmount) {
        try {
            PagedIterator<GHCommit> iterator = gitHub.getRepository(githubRepo).listCommits().iterator();
            List<String> commits = new ArrayList<>();
            for (int i = 0; i < commitsAmount; i ++) {
                commits.add(iterator.next().getSHA1());
            }
            return commits;
        } catch (Exception e) {
            logger.warn("Could not get latest commit on branch from Github!", e);
            return new ArrayList<>();
        }
    }

    public String getLatestCommitShaOnBranch(String githubRepo, String branchName) {
        try {
            return gitHub.getRepository(githubRepo).getBranch(branchName).getSHA1();
        } catch (Exception e) {
            logger.warn("Could not get latest commit on branch from Github!", e);
            return "";
        }
    }

    public boolean isCommitStatusOK(String githubRepo, String sha) {
        Optional<CommitDetails> commit = getCommitDetails(githubRepo, sha);

        if (commit.isPresent()) {
            return commit.get().getCommitStatus().getState() == GHCommitState.SUCCESS;
        }

        return false;
    }

    public boolean isCommitInBranchHistory(String githubRepo, String branch, String sha) {

        // First get the initial commit so we can reduce the number of requests to github
        Optional<CommitDetails> commit = getCommitDetails(githubRepo, sha);

        if (commit.isPresent()) {

            Optional<List<GHCommit>> allCommitsOnABranch = getAllCommitsOnABranch(githubRepo, branch, commit.get().getCommitDate());
            return allCommitsOnABranch.map(ghCommits -> ghCommits
                    .stream()
                    .anyMatch(ghCommit -> ghCommit.getSHA1().equals(sha)))
                    .orElse(false);
        }

        return false;
    }

    public static String getRepoNameFromRepositoryUrl(String githubRepositoryUrl) {
        return githubRepositoryUrl.replaceFirst("https?://github.com/", "");
    }

    private Optional<List<GHCommit>> getAllCommitsOnABranch(String githubRepo, String branch, Date since) {
        try {
            // Reducing 1 hour of the "since" as it is not getting the desired commits on the exact range
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(since);
            calendar.add(Calendar.DATE, -1);

            return Optional.of(gitHub.getRepository(githubRepo).queryCommits().since(calendar.getTime()).from(branch).list().asList());
        } catch (Throwable e) {  // The library is throwing and Error and not an exception, for god sake
            logger.warn("Could not get all commits on branch {} for repo {}", branch, githubRepo, e);
            return Optional.empty();
        }
    }
}
