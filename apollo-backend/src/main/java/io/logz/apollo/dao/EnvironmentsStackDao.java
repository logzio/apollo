package io.logz.apollo.dao;

import io.logz.apollo.models.Environment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EnvironmentsStackDao {

    List<Environment> getEnvironments(int id);
    String getStackName(int id);
    boolean isStackEnabled(int id);
    void addEnvironmentsStack(@Param("name") String name, @Param("isEnabled") boolean isEnabled);
    void addEnvironmentToStack(@Param("environmentId") int environmentId, @Param("stackId") int stackId);
}
