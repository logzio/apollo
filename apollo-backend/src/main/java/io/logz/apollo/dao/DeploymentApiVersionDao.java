package io.logz.apollo.dao;

import org.apache.ibatis.annotations.Param;

public interface DeploymentApiVersionDao {
    String getApiVersion(@Param("serviceId") int serviceId, @Param("environmentId") int environmentId);
}
