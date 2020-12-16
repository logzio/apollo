package io.logz.apollo.controllers;

import com.google.common.collect.ImmutableMap;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.models.Group;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.DELETE;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class GroupController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);
    private final GroupDao groupDao;

    @Inject
    public GroupController(GroupDao groupDao) {
        this.groupDao = requireNonNull(groupDao);
    }

    @LoggedIn
    @GET("/group")
    public List<Group> getAllGroups() {
        return groupDao.getAllGroups();
    }

    @LoggedIn
    @GET("/group/environment/{environmentId}/service/{serviceId}")
    public List<Group> getGroupsPerServiceAndEnvironment(int environmentId, int serviceId) {
        return groupDao.getGroupsPerServiceAndEnvironment(serviceId, environmentId);
    }

    @LoggedIn
    @GET("/group/{id}")
    public Group getGroup(int id) {
        return groupDao.getGroup(id);
    }

    @LoggedIn
    @GET("/group/name/{name}")
    public Group getGroupByName(String name) {
        return groupDao.getGroupByName(name);
    }

    @LoggedIn
    @POST("/group")
    public void addGroup(String name, int serviceId, int environmentId, int scalingFactor, String jsonParams, Req req) {

        Group newGroup = new Group();

        newGroup.setName(name);
        newGroup.setServiceId(serviceId);
        newGroup.setEnvironmentId(environmentId);
        newGroup.setScalingFactor(scalingFactor);
        newGroup.setJsonParams(jsonParams);
        try {
            groupDao.addGroup(newGroup);
            assignJsonResponseToReq(req, HttpStatus.CREATED, newGroup);
            logger.info("New group was created with id " + groupDao.getGroupByName(name).getId());
        } catch (Exception e){
            logger.error("Could not create new group with name {}", name, e);
        }
    }

    @LoggedIn
    @PUT("/group/{id}")
    public void updateGroup(int id, String name, int serviceId, int environmentId, int scalingFactor, String jsonParams, Req req) {
        Group group = groupDao.getGroup(id);

        if (group == null) {
            Map<String, String> message = ImmutableMap.of("message", "Group not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        group.setName(name);
        group.setServiceId(serviceId);
        group.setEnvironmentId(environmentId);
        group.setScalingFactor(scalingFactor);
        group.setJsonParams(jsonParams);

        groupDao.updateGroup(group);
        assignJsonResponseToReq(req, HttpStatus.OK, group);
    }

    @LoggedIn
    @DELETE("/group/{id}")
    public void deleteGroup(int id, Req req) {
        groupDao.deleteGroup(id);
        assignJsonResponseToReq(req, HttpStatus.OK, "deleted");
    }
}
