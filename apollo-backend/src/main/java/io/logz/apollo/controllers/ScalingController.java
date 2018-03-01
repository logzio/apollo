package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.excpetions.ApolloNotFoundException;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Group;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import io.logz.apollo.kubernetes.KubernetesHandler;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;

import javax.inject.Inject;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class ScalingController {

    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;
    private final GroupDao groupDao;

    @Inject
    public ScalingController(KubernetesHandlerStore kubernetesHandlerStore, GroupDao groupDao, EnvironmentDao environmentDao, ServiceDao serviceDao) {
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);
    }

    @LoggedIn
    @GET("/scaling/apollo-factor/{groupId}")
    public int getScalingFactor(int groupId) {
        return groupDao.getScalingFactor(groupId);
    }

    @LoggedIn
    @GET("/scaling/kubernetes-factor/{groupId}")
    public void getKubeScalingFactor(int groupId, Req req) {
        Group group = groupDao.getGroup(groupId);

        if (group == null) {
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, groupId);
            return;
        }

        Environment environment = environmentDao.getEnvironment(group.getEnvironmentId());
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
        try {
            int scalingFactor = kubernetesHandler.getScalingFactor(serviceDao.getService(group.getServiceId()), group.getName());
            assignJsonResponseToReq(req, HttpStatus.OK, scalingFactor);
        } catch (ApolloNotFoundException e) {
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @LoggedIn
    @PUT("/scaling/{groupId}")
    public void updateScalingFactor(int groupId, int scalingFactor, Req req) {
        Group group = groupDao.getGroup(groupId);

        if (group == null) {
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, groupId);
            return;
        }

        if (group.getScalingStatus() == Group.ScalingStatus.BLOCKED) {
            assignJsonResponseToReq(req, HttpStatus.FORBIDDEN, groupId);
            return;
        }

        group.setScalingFactor(scalingFactor);
        group.setScalingStatus(Group.ScalingStatus.PENDING);
        groupDao.updateGroup(group);

        assignJsonResponseToReq(req, HttpStatus.OK, group);
    }
}
