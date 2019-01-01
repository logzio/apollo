package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import io.logz.apollo.common.HttpStatus;
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
import java.util.stream.StreamSupport;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class EnvironmentsStackController {

    private final EnvironmentsStackDao environmentsStackDao;
    private final StackDao stackDao;
    private final static String ENVIRONMENTS_DELIMITER = ",";

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
        List<EnvironmentsStack> environmentStacks = stackDao.getAllEnvironmentStacks();
        return environmentStacks.stream().map(stack -> {
            List<Integer> environments = environmentsStackDao.getEnvironments(stack.getId());
            return new EnvironmentsStack(stack.getId(), stack.getName(), stack.isEnabled(),environments);
        }).collect(Collectors.toList());
    }

    @LoggedIn
    @POST("/environments-in-stacks")
    public void addEnvironmentToStack(int environmentId, int stackId, Req req) {
        environmentsStackDao.addEnvironmentToStack(environmentId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, stackId);
    }

    @LoggedIn
    @Transactional
    @POST("/environments-stack")
    public void addEnvironmentsStack(String name, String isEnabled, String environmentsCsv, Req req) {
        Iterable<String> environmentsString = Splitter.on(ENVIRONMENTS_DELIMITER).omitEmptyStrings().trimResults().split(environmentsCsv);
        List<Integer> environmentsIds = StreamSupport.stream(environmentsString.spliterator(), false)
                                                     .map(environment -> Integer.parseInt(environment))
                                                     .collect(Collectors.toList());
        EnvironmentsStack environmentsStack = new EnvironmentsStack(name, Boolean.valueOf(isEnabled));
        stackDao.addStack(environmentsStack);
        if (environmentsIds.size() > 0) {
            environmentsStackDao.addEnvironmentsToStack(environmentsIds, environmentsStack.getId());
        }
        assignJsonResponseToReq(req, HttpStatus.CREATED, environmentsStack.getId());
    }

    @LoggedIn
    @Transactional
    @PUT("/environments-stack")
    public void updateEnvironmentsStack(int id, String name, boolean isEnabled, String environmentsCsv, Req req) {
        Iterable<String> environmentsString = Splitter.on(ENVIRONMENTS_DELIMITER).omitEmptyStrings().trimResults().split(environmentsCsv);
        List<Integer> environmentsIds = StreamSupport.stream(environmentsString.spliterator(), false)
                                                     .map(environment -> Integer.parseInt(environment))
                                                     .collect(Collectors.toList());
        EnvironmentsStack environmentsStack = new EnvironmentsStack(id, name, isEnabled);
        stackDao.updateStack(environmentsStack);
        environmentsStackDao.clearEnvironmentsStack(id);//TODO check with mesika
        if (environmentsIds.size() <= 0) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "The EnvironmentsStack you asked to update has an empty environments list");
            return;
        }
        environmentsStackDao.addEnvironmentsToStack(environmentsIds, id);
        assignJsonResponseToReq(req, HttpStatus.OK, id);
    }

    @LoggedIn
    @DELETE("/environments-in-stacks/environment-id/{environmentId}/stack-id/{stackId}")
    public void removeEnvironmentFromStack(int environmentId, int stackId) {
        environmentsStackDao.removeEnvironmentFromStack(environmentId, stackId);
    }

    @LoggedIn
    @DELETE("/environments-in-stacks/{stackId}/environments")
    public void clearEnvironmentsStack(int stackId) {
        environmentsStackDao.clearEnvironmentsStack(stackId);
    }

    @LoggedIn
    @Transactional
    @DELETE("/environments-in-stacks/{stackId}")
    public void deleteEnvironmentsStack(int stackId) {
        environmentsStackDao.clearEnvironmentsStack(stackId);
        stackDao.deleteStack(stackId);
    }
}
