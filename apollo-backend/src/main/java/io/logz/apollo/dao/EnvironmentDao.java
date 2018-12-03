package io.logz.apollo.dao;

import io.logz.apollo.models.Environment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by roiravhon on 12/18/16.
 */
public interface EnvironmentDao {

    Environment getEnvironment(int id);
    List<Environment> getAllEnvironments();
    List<Environment> getEnvironmentsByAvailability(@Param("availability") String availability);
    void addEnvironment(Environment environment);
}

