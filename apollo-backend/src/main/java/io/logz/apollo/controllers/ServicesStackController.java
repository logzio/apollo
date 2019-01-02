package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.common.StringParser;
import io.logz.apollo.dao.ServicesStackDao;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.ServicesStack;
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
public class ServicesStackController {

    private final ServicesStackDao servicesStackDao;
    private final StackDao stackDao;
    private final static String SERVICES_DELIMITER = ",";

    @Inject
    public ServicesStackController(ServicesStackDao servicesStackDao, StackDao stackDao) {
        this.servicesStackDao = requireNonNull(servicesStackDao);
        this.stackDao = requireNonNull(stackDao);
    }

    @LoggedIn
    @Transactional
    @GET("/services-stack/{stackId}")
    public ServicesStack getServicesStack(int stackId) {
        ServicesStack servicesStack = stackDao.getServicesStack(stackId);
        servicesStack.setServices(servicesStackDao.getServices(stackId));
        return servicesStack;
    }

    @LoggedIn
    @GET("/services-stack")
    public List<ServicesStack> getAllServicesStacks() {
        List<ServicesStack> servicesStacks = stackDao.getAllServicesStacks();
        return servicesStacks.stream().map(stack -> {
            List<Integer> services = servicesStackDao.getServices(stack.getId());
            return new ServicesStack(stack.getId(), stack.getName(), stack.isEnabled(), services);
        }).collect(Collectors.toList());
    }

    @LoggedIn
    @POST("/services-stack/service")
    public void addServiceToStack(int serviceId, int stackId, Req req) {
        servicesStackDao.addServiceToStack(serviceId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, getServicesStack(stackId));
    }

    @LoggedIn
    @Transactional
    @POST("/services-stack")
    public void addServicesStack(String name, boolean isEnabled, String servicesCsv, Req req) {
        List<Integer> servicesIds = StringParser.splitCsvToIntegerList(servicesCsv, SERVICES_DELIMITER);
        ServicesStack servicesStack = new ServicesStack(name, Boolean.valueOf(isEnabled));
        stackDao.addStack(servicesStack);
        if (servicesIds.size() > 0) {
            servicesStackDao.addServicesToStack(servicesIds, servicesStack.getId());
        }
        assignJsonResponseToReq(req, HttpStatus.CREATED, getServicesStack(servicesStack.getId()));
    }

    @LoggedIn
    @Transactional
    @PUT("/services-stack")
    public void updateServicesStack(int id, String name, boolean isEnabled, String servicesCsv, Req req) {
        List<Integer> servicesIds = StringParser.splitCsvToIntegerList(servicesCsv, SERVICES_DELIMITER);
        if (servicesIds.size() <= 0) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "The ServicesStack you asked to update has an empty services list");
            return;
        }
        ServicesStack servicesStack = new ServicesStack(id, name, isEnabled);
        stackDao.updateStack(servicesStack);
        servicesStackDao.clearServicesStack(id);
        servicesStackDao.addServicesToStack(servicesIds, id);
        assignJsonResponseToReq(req, HttpStatus.OK, getServicesStack(id));
    }

    @LoggedIn
    @DELETE("/services-stack/service/{id}/stack/{stackId}")
    public void removeServiceFromStack(int id, int stackId) {
        servicesStackDao.removeServiceFromStack(id, stackId);
    }

    @LoggedIn
    @DELETE("/services-stack/{id}/clear")
    public void clearServicesStack(int id) {
        servicesStackDao.clearServicesStack(id);
    }

    @LoggedIn
    @Transactional
    @DELETE("/services-stack/{id}")
    public void deleteServicesStack(int id) {
        servicesStackDao.clearServicesStack(id);
        stackDao.deleteStack(id);
    }
}
