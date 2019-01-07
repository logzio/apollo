package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.common.StringParser;
import io.logz.apollo.dao.EnvironmentsStackDao;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.EnvironmentsStack;
import org.mybatis.guice.transactional.Transactional;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.DELETE;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class EnvironmentsStackController {

    private final EnvironmentsStackDao environmentsStackDao;
    private final StackDao stackDao;

    @Inject
    public EnvironmentsStackController(EnvironmentsStackDao environmentsStackDao, StackDao stackDao) {
        this.environmentsStackDao = requireNonNull(environmentsStackDao);
        this.stackDao = requireNonNull(stackDao);
    }

    @LoggedIn
    @Transactional
    @GET("/environments-stack/{stackId}")
    public EnvironmentsStack getEnvironmentsStack(int stackId) {
        EnvironmentsStack environmentsStack = stackDao.getEnvironmentsStack(stackId);
        environmentsStack.setEnvironments(environmentsStackDao.getEnvironments(stackId));
        return environmentsStack;
    }

    @LoggedIn
    @GET("/environments-stack")
    public List<EnvironmentsStack> getAllEnvironmentsStacks() {
        List<EnvironmentsStack> environmentStacks = stackDao.getAllEnvironmentsStacks();
        return environmentStacks.stream().map(stack -> {
            List<Integer> environments = environmentsStackDao.getEnvironments(stack.getId());
            return new EnvironmentsStack(stack.getId(), stack.getName(), stack.isEnabled(),environments);
        }).collect(Collectors.toList());
    }

    @LoggedIn
    @POST("/environments-stack/environment")
    public void addEnvironmentToStack(int environmentId, int stackId, Req req) {
        environmentsStackDao.addEnvironmentToStack(environmentId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, getEnvironmentsStack(stackId));
    }

    @LoggedIn
    @Transactional
    @POST("/environments-stack")
    public void addEnvironmentsStack(String name, boolean isEnabled, String environmentsCsv, Req req) {
        List<Integer> environmentsIds = StringParser.splitCsvToIntegerList(environmentsCsv);
        EnvironmentsStack environmentsStack = new EnvironmentsStack(name, isEnabled);
        stackDao.addStack(environmentsStack);
        if (environmentsIds.size() > 0) {
            environmentsStackDao.addEnvironmentsToStack(environmentsIds, environmentsStack.getId());
        }
        assignJsonResponseToReq(req, HttpStatus.CREATED, getEnvironmentsStack(environmentsStack.getId()));
    }

    @LoggedIn
    @Transactional
    @PUT("/environments-stack")
    public void updateEnvironmentsStack(int id, String name, boolean isEnabled, String environmentsCsv, Req req) {
        List<Integer> environmentsIds = StringParser.splitCsvToIntegerList(environmentsCsv);
        if (environmentsIds.size() <= 0) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "The EnvironmentsStack you asked to update has an empty environments list");
            return;
        }
        EnvironmentsStack environmentsStack = new EnvironmentsStack(id, name, isEnabled);
        stackDao.updateStack(environmentsStack);
        environmentsStackDao.clearEnvironmentsStack(id);
        environmentsStackDao.addEnvironmentsToStack(environmentsIds, id);
        assignJsonResponseToReq(req, HttpStatus.OK, getEnvironmentsStack(id));
    }

    @LoggedIn
    @DELETE("/environments-stack/environment/{id}/stack/{stackId}")
    public void removeEnvironmentFromStack(int id, int stackId) {
        environmentsStackDao.removeEnvironmentFromStack(id, stackId);
    }

    @LoggedIn
    @DELETE("environments-stack/{id}/clear")
    public void clearEnvironmentsStack(int id) {
        environmentsStackDao.clearEnvironmentsStack(id);
    }

    @LoggedIn
    @Transactional
    @DELETE("/environments-stack/{id}")
    public void deleteEnvironmentsStack(int id) {
        environmentsStackDao.clearEnvironmentsStack(id);
        stackDao.deleteStack(id);
    }
}
