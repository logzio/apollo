package io.logz.apollo.controllers;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.scm.CommitDetails;
import io.logz.apollo.scm.GithubConnector;
import org.apache.commons.lang.StringUtils;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static io.logz.apollo.scm.GithubConnector.getRepoNameFromRepositoryUrl;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 12/20/16.
 */
@Controller
public class DeployableVersionController {

    private final DeployableVersionDao deployableVersionDao;
    private final GithubConnector githubConnector;

    private final static int LATEST_DEPLOYABLE_VERSIONS_COUNT = 200;

    public static int MAX_COMMIT_FIELDS_LENGTH = 1000;
    public static int MAX_COMMIT_MESSAGE_LENGTH = 10000;
    public static String UNKNOWN_COMMIT_FIELD = "Unknown";

    @Inject
    public DeployableVersionController(DeployableVersionDao deployableVersionDao, GithubConnector githubConnector) {
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.githubConnector = requireNonNull(githubConnector);
    }

    @LoggedIn
    @GET("/deployable-version")
    public List<DeployableVersion> getAllDeployableVersion() {
        return deployableVersionDao.getAllDeployableVersions();
    }

    @LoggedIn
    @GET("/deployable-version/{id}")
    public DeployableVersion getDeployableVersion(int id) {
        return deployableVersionDao.getDeployableVersion(id);
    }

    @LoggedIn
    @GET("/deployable-version/sha/{sha}/service/{serviceId}")
    public DeployableVersion getDeployableVersionFromSha(String sha, int serviceId) {
        return deployableVersionDao.getDeployableVersionFromSha(sha, serviceId);
    }

    @LoggedIn
    @GET("/deployable-version/latest/service/{serviceId}")
    public List<DeployableVersion> getLatestDeployableVersionsByServiceId(int serviceId) {
        return deployableVersionDao.getLatestDeployableVersionsByServiceId(serviceId, LATEST_DEPLOYABLE_VERSIONS_COUNT);
    }

    @LoggedIn
    @GET("/deployable-version/multi-service/{serviceIdsCsv}")
    public List<DeployableVersion> getDeployableVersionForMultiServices(String serviceIdsCsv) {
        Iterable<String> serviceIds = Splitter.on(",").omitEmptyStrings().trimResults().split(serviceIdsCsv);
        return deployableVersionDao.getDeployableVersionForMultiServices(Joiner.on(",").join(serviceIds),  Iterables.size(serviceIds));
    }

    @LoggedIn
    @GET("/deployable-version/latest/branch/{branchName}/repofrom/{deployableVersionId}")
    public DeployableVersion getLatestDeployableVersionOnBranchBasedOnOtherDeployableVersion(String branchName, int deployableVersionId, Req req) {
        DeployableVersion referenceDeployableVersion = deployableVersionDao.getDeployableVersion(deployableVersionId);
        String actualRepo = getRepoNameFromRepositoryUrl(referenceDeployableVersion.getGithubRepositoryUrl());

        Optional<String> latestSha = githubConnector.getLatestCommitShaOnBranch(actualRepo, branchName);

        if (!latestSha.isPresent()) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "Did not found latest commit on that branch");
            throw new RuntimeException();
        }

        DeployableVersion deployableVersionFromSha = deployableVersionDao.getDeployableVersionFromSha(latestSha.get(),
                referenceDeployableVersion.getServiceId());

        if (deployableVersionFromSha == null) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "Did not found deployable version matching the sha " + latestSha);
            throw new RuntimeException();
        } else {
            return deployableVersionFromSha;
        }
    }

    @POST("/deployable-version")
    public void addDeployableVersion(String gitCommitSha, String githubRepositoryUrl, int serviceId, Req req) {
        DeployableVersion newDeployableVersion = new DeployableVersion();

        // Getting the commit details
        String actualRepo = getRepoNameFromRepositoryUrl(githubRepositoryUrl);
        newDeployableVersion.setGitCommitSha(gitCommitSha);
        newDeployableVersion.setGithubRepositoryUrl(githubRepositoryUrl);
        newDeployableVersion.setServiceId(serviceId);
        newDeployableVersion.setCommitDate(new Date());

        // Just to protect tests from reaching github rate limit
        if (githubRepositoryUrl.contains("github.com")) {

            Optional<CommitDetails> commit = githubConnector.getCommitDetails(actualRepo, gitCommitSha);

            if (commit.isPresent()) {

                CommitDetails commitDetails = commit.get();

                newDeployableVersion.setCommitUrl(commitDetails.getCommitUrl());
                newDeployableVersion.setCommitMessage(commitDetails.getCommitMessage());
                newDeployableVersion.setCommitDate(commitDetails.getCommitDate());
                newDeployableVersion.setCommitterAvatarUrl(commitDetails.getCommitterAvatarUrl());
                newDeployableVersion.setCommitterName(commitDetails.getCommitterName());

                String commitMessage = newDeployableVersion.getCommitMessage();
                newDeployableVersion.setCommitMessage(commitMessage == null ? UNKNOWN_COMMIT_FIELD : StringUtils.abbreviate(commitMessage, MAX_COMMIT_MESSAGE_LENGTH));

                String commitSha = newDeployableVersion.getGitCommitSha();
                newDeployableVersion.setGitCommitSha(commitSha == null ? UNKNOWN_COMMIT_FIELD : StringUtils.abbreviate(commitSha, MAX_COMMIT_FIELDS_LENGTH));

                String commitUrl = newDeployableVersion.getCommitUrl();
                newDeployableVersion.setCommitUrl(commitUrl == null ? UNKNOWN_COMMIT_FIELD : StringUtils.abbreviate(commitUrl, MAX_COMMIT_FIELDS_LENGTH));

                String commitGithubRepositoryUrl = newDeployableVersion.getGithubRepositoryUrl();
                newDeployableVersion.setGithubRepositoryUrl(commitGithubRepositoryUrl == null ? UNKNOWN_COMMIT_FIELD : StringUtils.abbreviate(commitGithubRepositoryUrl, MAX_COMMIT_FIELDS_LENGTH));

                String committerAvatarUrl = newDeployableVersion.getCommitterAvatarUrl();
                newDeployableVersion.setCommitterAvatarUrl(committerAvatarUrl == null ? UNKNOWN_COMMIT_FIELD : StringUtils.abbreviate(committerAvatarUrl, MAX_COMMIT_FIELDS_LENGTH));

                String committerName = newDeployableVersion.getCommitterName();
                newDeployableVersion.setCommitterName(committerName == null ? UNKNOWN_COMMIT_FIELD :StringUtils.abbreviate(committerName, MAX_COMMIT_FIELDS_LENGTH));
            } else {
                assignJsonResponseToReq(req, HttpStatus.INTERNAL_SERVER_ERROR, "Could not get commit details from GitHub, make sure your GitHub user is well defined.");
                return;
            }
        }

        // Avoid duplicate entry errors
        DeployableVersion existingDeployableVersion = deployableVersionDao.getDeployableVersionFromSha(newDeployableVersion.getGitCommitSha(), newDeployableVersion.getServiceId());
        if (existingDeployableVersion != null) {
            assignJsonResponseToReq(req, HttpStatus.CREATED, existingDeployableVersion);
            return;
        }

        deployableVersionDao.addDeployableVersion(newDeployableVersion);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newDeployableVersion);
    }
}
