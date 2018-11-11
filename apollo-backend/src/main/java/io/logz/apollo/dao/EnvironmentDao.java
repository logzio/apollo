package io.logz.apollo.dao;

import io.logz.apollo.models.Environment;

import java.util.List;

/**
 * Created by roiravhon on 12/18/16.
 */
public interface EnvironmentDao {

    Environment getEnvironment(int id);
    List<Environment> getAllEnvironments();
    List<Integer> getProductionEnvironmentsIds();
    void addEnvironment(Environment environment);
}
