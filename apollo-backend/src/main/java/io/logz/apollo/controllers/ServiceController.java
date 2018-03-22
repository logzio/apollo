package io.logz.apollo.controllers;

import com.google.common.collect.ImmutableMap;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.Service;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 12/20/16.
 */
@Controller
public class ServiceController {

    private final ServiceDao serviceDao;

    @Inject
    public ServiceController(ServiceDao serviceDao) {
        this.serviceDao = requireNonNull(serviceDao);
    }

    @LoggedIn
    @GET("/service")
    public List<Service> getAllServices() {
        return serviceDao.getAllServices();
    }

    @LoggedIn
    @GET("/service/{id}")
    public Service getService(int id) {
        return serviceDao.getService(id);
    }

    @LoggedIn
    @POST("/service")
    public void addService(String name, String deploymentYaml, String serviceYaml, String ingressYaml, Boolean isPartOfGroup, Req req) {
        Service newService = new Service();

        newService.setName(name);
        newService.setDeploymentYaml(deploymentYaml);
        newService.setServiceYaml(serviceYaml);
        newService.setIngressYaml(ingressYaml);
        newService.setIsPartOfGroup(isPartOfGroup);

        serviceDao.addService(newService);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newService);
    }

    @LoggedIn
    @PUT("/service/{id}")
    public void updateService(int id, String name, String deploymentYaml, String serviceYaml, String ingressYaml, Boolean isPartOfGroup, Req req) {
        Service service = serviceDao.getService(id);

        if (service == null) {
            Map<String, String> message = ImmutableMap.of("message", "Service not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        service.setName(name);
        service.setDeploymentYaml(deploymentYaml);
        service.setServiceYaml(serviceYaml);
        service.setIngressYaml(ingressYaml);
        service.setIsPartOfGroup(isPartOfGroup);

        serviceDao.updateService(service);
        assignJsonResponseToReq(req, HttpStatus.OK, service);
    }
}
