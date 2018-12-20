package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentsStackDao;
import io.logz.apollo.models.Environment;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;

import javax.inject.Inject;
import java.util.List;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class EnvironmentsStackController {

    private final EnvironmentsStackDao environmentsStackDao;

    @Inject
    public EnvironmentsStackController(EnvironmentsStackDao environmentsStackDao) {
        this.environmentsStackDao = requireNonNull(environmentsStackDao);
    }

    @GET("/environment-stack/{id}")
    public List<Environment> getEnvironments(int id) {
      return environmentsStackDao.getEnvironments(id);
    }

    @GET("/environment-stack/name/{id}")
    public String getStackName(int id) {
        return environmentsStackDao.getStackName(id);
    }

    @GET("/environment-stack/is-enabled/{id}")
    public boolean isStackEnabled(int id) {
        return environmentsStackDao.isStackEnabled(id);
    }

    @POST("/environments-stack")
    public void addEnvironmentsStack(String name, boolean isEnabled, Req req) {
        environmentsStackDao.addEnvironmentsStack(name, isEnabled);
        int id = environmentsStackDao.getStackIdByName(name);
        assignJsonResponseToReq(req, HttpStatus.CREATED, id);
    }

    @POST("/environment-to-stack")
    public void addEnvironmentToStack(int environmentId, int stackId, Req req) {
        environmentsStackDao.addEnvironmentToStack(environmentId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, stackId);
    }
}
