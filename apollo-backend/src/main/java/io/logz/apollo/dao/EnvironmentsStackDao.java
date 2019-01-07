package io.logz.apollo.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EnvironmentsStackDao {
    List<Integer> getEnvironments(int stackId);
    void addEnvironmentToStack(@Param("environmentId") int environmentId, @Param("stackId") int stackId);
    void removeEnvironmentFromStack(@Param("environmentId") int environmentId, @Param("stackId") int stackId);
    void addEnvironmentsToStack(@Param("environments") List<Integer> environments, @Param("stackId") int stackId);
    void clearEnvironmentsStack(int stackId);
}
