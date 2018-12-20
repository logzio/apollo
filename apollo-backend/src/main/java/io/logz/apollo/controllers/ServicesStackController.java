package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.ServicesStackDao;
import io.logz.apollo.models.Service;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;

import javax.inject.Inject;
import java.util.List;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class ServicesStackController {

    private final ServicesStackDao servicesStackDao;

    @Inject
    public ServicesStackController(ServicesStackDao servicesStackDao) {
        this.servicesStackDao = requireNonNull(servicesStackDao);
    }

    @GET("/service-stack/{id}")
    public List<Service> getServices(int id) {
        return servicesStackDao.getServices(id);
    }

    @GET("/service-stack/name/{id}")
    public String getStackName(int id) {
        return servicesStackDao.getStackName(id);
    }

    @GET("/service-stack/is-enabled/{id}")
    public boolean isStackEnabled(int id) {
        return servicesStackDao.isStackEnabled(id);
    }

    @POST("/services-stack")
    public void addServicesStack(String name, boolean isEnabled, Req req) {
        servicesStackDao.addServicesStack(name, isEnabled);
        int id = servicesStackDao.getStackIdByName(name);
        assignJsonResponseToReq(req, HttpStatus.CREATED, id);
    }

    @POST("/service-to-stack")
    public void addServiceToStack(int serviceId, int stackId, Req req) {
        servicesStackDao.addServiceToStack(serviceId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, stackId);
    }
}
