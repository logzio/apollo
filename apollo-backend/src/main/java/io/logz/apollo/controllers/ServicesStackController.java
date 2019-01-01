package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import io.logz.apollo.common.HttpStatus;
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
import java.util.stream.StreamSupport;

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
    @POST("/services-in-stacks")
    public void addServiceToStack(int serviceId, int stackId, Req req) {
        servicesStackDao.addServiceToStack(serviceId, stackId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, stackId);
    }

    @LoggedIn
    @Transactional
    @POST("/services-stack")
    public void addServicesStack(String name, String isEnabled, String servicesCsv, Req req) {
        Iterable<String> servicesString = Splitter.on(SERVICES_DELIMITER).omitEmptyStrings().trimResults().split(servicesCsv);
        List<Integer> servicesIds = StreamSupport.stream(servicesString.spliterator(), false)
                                                     .map(service -> Integer.parseInt(service))
                                                     .collect(Collectors.toList());
        ServicesStack servicesStack = new ServicesStack(name, Boolean.valueOf(isEnabled));
        stackDao.addStack(servicesStack);
        if (servicesIds.size() > 0) {
            servicesStackDao.addServicesToStack(servicesIds, servicesStack.getId());
        }
        assignJsonResponseToReq(req, HttpStatus.CREATED, servicesStack.getId());
    }

    @LoggedIn
    @Transactional
    @PUT("/services-stack")
    public void updateServicesStack(int id, String name, boolean isEnabled, String servicesCsv, Req req) {
        Iterable<String> servicesString = Splitter.on(SERVICES_DELIMITER).omitEmptyStrings().trimResults().split(servicesCsv);
        List<Integer> servicesIds = StreamSupport.stream(servicesString.spliterator(), false)
                                                     .map(service -> Integer.parseInt(service))
                                                     .collect(Collectors.toList());
        ServicesStack servicesStack = new ServicesStack(id, name, isEnabled);
        stackDao.updateStack(servicesStack);
        servicesStackDao.clearServicesStack(id);//TODO check with mesika
        if (servicesIds.size() <= 0) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "The ServicesStack you asked to update has an empty services list");
            return;
        }
        servicesStackDao.addServicesToStack(servicesIds, id);
        assignJsonResponseToReq(req, HttpStatus.OK, id);
    }

    @LoggedIn
    @DELETE("/services-in-stacks/service-id/{serviceId}/stack-id/{stackId}")
    public void removeServiceFromStack(int serviceId, int stackId) {
        servicesStackDao.removeServiceFromStack(serviceId, stackId);
    }

    @LoggedIn
    @DELETE("/services-in-stacks/{stackId}/services")
    public void clearServicesStack(int stackId) {
        servicesStackDao.clearServicesStack(stackId);
    }

    @LoggedIn
    @Transactional
    @DELETE("/services-in-stacks/{stackId}")
    public void deleteServicesStack(int stackId) {
        servicesStackDao.clearServicesStack(stackId);
        stackDao.deleteStack(stackId);
    }
}
