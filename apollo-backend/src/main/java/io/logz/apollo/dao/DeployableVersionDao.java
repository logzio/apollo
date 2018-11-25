package io.logz.apollo.dao;

import io.logz.apollo.models.DeployableVersion;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

/**
 * Created by roiravhon on 12/20/16.
 */
public interface DeployableVersionDao {

    DeployableVersion getDeployableVersion(int id);
    List<DeployableVersion> getAllDeployableVersions();
    DeployableVersion getDeployableVersionFromSha(@Param("gitCommitSha") String gitCommitSha, @Param("serviceId") int serviceId);
    List<DeployableVersion> getLatestDeployableVersionsByServiceId(@Param("serviceId") int serviceId, @Param("amount") int amount);
    DeployableVersion getLatestDeployableVersionByServiceId(@Param("serviceId") int serviceId);
    void addDeployableVersion(DeployableVersion deployableVersion);
    List<DeployableVersion> getDeployableVersionForMultiServices(@Param("serviceIdsCsv") String serviceIdsCsv, @Param("numOfServices") int numOfServices);
    void updateCommitDate(@Param("id") int id, @Param("commitDate") Date commitDate);
}
