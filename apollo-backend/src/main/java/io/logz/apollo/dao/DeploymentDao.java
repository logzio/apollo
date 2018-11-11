package io.logz.apollo.dao;

import io.logz.apollo.database.OrderDirection;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.DeploymentHistoryDetails;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Created by roiravhon on 1/5/17.
 */
public interface DeploymentDao {
    Deployment getDeployment(int id);
    List<Deployment> getAllDeployments();
    List<Deployment> getAllRunningDeployments();
    List<Deployment> getAllStartedDeployments();
    List<Deployment> getRunningAndJustFinishedDeployments();
    List<Deployment> getLatestDeployments();
    Deployment getLatestDeploymentOfServiceInEnvironment(@Param("serviceId") int serviceId, @Param("environmentId") int environmentId);
    String getCurrentGitCommitSha(@Param("serviceId") int serviceId, @Param("environmentId") int environmentId);
    void addDeployment(Deployment deployment);
    void updateDeploymentStatus(@Param("id") int id, @Param("status") Deployment.DeploymentStatus status);
    void updateDeploymentEnvStatus(@Param("id") int id, @Param("envStatus") String envStatus);
    void updateDeployment(Deployment deployment);
    String getDeploymentEnvStatus(@Param("id") int id);
    List<Integer> getServicesDeployedOnEnv(@Param("environmentId") int environmentId);
    int getTotalDeploymentsCount();
    int getFilteredDeploymentHistoryCount(@Param("searchTerm") String searchTerm);
    List<DeploymentHistoryDetails> filterDeploymentHistoryDetails(@Param("searchTerm") String searchTerm,
                                                                  @Param("orderDirection") OrderDirection orderDirection,
                                                                  @Param("offset") int offset,
                                                                  @Param("limit") int limit);
}
