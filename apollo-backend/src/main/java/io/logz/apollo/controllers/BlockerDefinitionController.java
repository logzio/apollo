package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.blockers.BlockerService;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.BlockerDefinitionDao;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.DELETE;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.Administrator;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 6/4/17.
 */
@Controller
public class BlockerDefinitionController {

    private final static String CSV_DELIMITER = ",";

    private static final Logger logger = LoggerFactory.getLogger(BlockerDefinitionController.class);

    private final BlockerDefinitionDao blockerDefinitionDao;
    private final BlockerService blockerService;

    @Inject
    public BlockerDefinitionController(BlockerDefinitionDao blockerDefinitionDao, BlockerService blockerService) {
        this.blockerDefinitionDao = requireNonNull(blockerDefinitionDao);
        this.blockerService = requireNonNull(blockerService);
    }

    @LoggedIn
    @GET("/blocker-definition")
    public List<BlockerDefinition> getAllBlockerDefinitions() {
        return blockerDefinitionDao.getAllBlockerDefinitions();
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional")
    public List<Integer> getUnconditionalBlockers() {
        return blockerDefinitionDao.getUnconditionalBlockers();
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional/regions/{regionsCsv}/environment-types/{environmentTypesCsv}")
    public List<Integer> getUnconditionalBlockersByEnvironmentTypeAndRegion(String regionsCsv, String environmentTypesCsv) {
        Iterable<String> regions = Splitter.on(CSV_DELIMITER).omitEmptyStrings().trimResults().split(regionsCsv);
        Iterable<String> environmentTypes = Splitter.on(CSV_DELIMITER).omitEmptyStrings().trimResults().split(environmentTypesCsv);
        return blockerDefinitionDao.getUnconditionalBlockersByEnvironmentTypeAndRegion(Lists.newArrayList(regions), Lists.newArrayList(environmentTypes));
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional/regions/{regionsCsv}")
    public List<Integer> getUnconditionalBlockersByRegion(String regionsCsv) {
        Iterable<String> regions = Splitter.on(CSV_DELIMITER).omitEmptyStrings().trimResults().split(regionsCsv);
        return blockerDefinitionDao.getUnconditionalBlockersByRegion(Lists.newArrayList(regions));
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional/environment-types/{environmentTypesCsv}")
    public List<Integer> getUnconditionalBlockersDefinitionsByEnvironmentTypeA(String environmentTypesCsv) {
        Iterable<String> environmentTypes = Splitter.on(CSV_DELIMITER).omitEmptyStrings().trimResults().split(environmentTypesCsv);
        return blockerDefinitionDao.getUnconditionalBlockersByEnvironmentType(Lists.newArrayList(environmentTypes));
    }

    @LoggedIn
    @GET("/blocker-definition/{id}")
    public BlockerDefinition getBlockerDefinition(int id) {
        return blockerDefinitionDao.getBlockerDefinition(id);
    }

    @Administrator
    @POST("/blocker-definition")
    public void addBlockerDefinition(String name, String environmentId, String serviceId, String stackId, String availability,
                                     Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {

        if (!blockerService.getBlockerTypeBinding(blockerTypeName).isPresent()) {
            logger.warn("Could not find proper class that annotated with {}", blockerTypeName);
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "There is no implementation for blocker with name " + blockerTypeName);
            return;
        }

        Integer environmentIdParsed = null;
        Integer serviceIdParsed = null;
        Integer stackIdParsed = null;

        if (environmentId != null && !environmentId.equals("null"))
            environmentIdParsed = Integer.parseInt(environmentId);

        if (serviceId != null && !serviceId.equals("null"))
            serviceIdParsed = Integer.parseInt(serviceId);

        if (stackId != null && !stackId.equals("null")) {
            stackIdParsed = Integer.parseInt(stackId);
            if (environmentId != null && !environmentId.equals("null") || serviceId != null && !serviceId.equals("null")) {
                assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, String.format("Trying to add a stack blocker that is also an environment or a service blocker. stackId - %s, environmentId - %s, serviceId - %s", stackId, environmentId, serviceId));
                return;
            }
        }

        checkAvailabilityBlockerValidation(availability, environmentId, "add", req);

        BlockerDefinition blockerDefinition = new BlockerDefinition();

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentIdParsed);
        blockerDefinition.setServiceId(serviceIdParsed);
        blockerDefinition.setStackId(stackIdParsed);
        blockerDefinition.setAvailability(availability);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.addBlockerDefinition(blockerDefinition);
        logger.info(String.format("Added blocker: blockerId - %s, blockerName - %s, active - %s", blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getActive()));
        assignJsonResponseToReq(req, HttpStatus.CREATED, blockerDefinition);
    }

    @Administrator
    @PUT("/blocker-definition/{id}")
    public void updateBlockerDefinition(int id, String name, String environmentId, String serviceId, String stackId, String availability,
                                        Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {

        BlockerDefinition blockerDefinition = blockerDefinitionDao.getBlockerDefinition(id);

        if (blockerDefinition == null) {
            Map<String, String> message = ImmutableMap.of("message", "Blocker not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        Integer environmentIdParsed = null;
        Integer serviceIdParsed = null;
        Integer stackIdParsed = null;

        if (environmentId != null && !environmentId.equals("null"))
            environmentIdParsed = Integer.parseInt(environmentId);

        if (serviceId != null && !serviceId.equals("null"))
            serviceIdParsed = Integer.parseInt(serviceId);

        if (stackId != null && !stackId.equals("null")) {
            stackIdParsed = Integer.parseInt(stackId);
            if (environmentId != null && !environmentId.equals("null") || serviceId != null && !serviceId.equals("null")) {
                assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, String.format("Trying to update a stack blocker that is also an environment or a service blocker. stackId - {}, environmentId - {}, serviceId - {}", stackId, environmentId, serviceId));
            }
        }

        checkAvailabilityBlockerValidation(availability, environmentId, "update", req);

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentIdParsed);
        blockerDefinition.setServiceId(serviceIdParsed);
        blockerDefinition.setStackId(stackIdParsed);
        blockerDefinition.setAvailability(availability);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.updateBlockerDefinition(blockerDefinition);
        logger.info(String.format("Updated blocker: blockerId - %s, blockerName - %s, active - %s", blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getActive()));
        assignJsonResponseToReq(req, HttpStatus.OK, blockerDefinition);
    }

    private void checkAvailabilityBlockerValidation(String availability, String environmentId, String methodType, Req req) {
        if (availability != null && !availability.isEmpty()) {
            if (environmentId != null && !environmentId.equals("null")) {
                assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, String.format("Trying to %s an availability blocker that is also an environment blocker. availability - %s, environmentId - %s", methodType, availability, environmentId));
                return;
            }
        }
    }

    @Administrator
    @DELETE("/blocker-definition/{id}")
    public void deleteBlockerDefinition(int id, Req req) {
        blockerDefinitionDao.deleteBlockerDefinition(id);
        assignJsonResponseToReq(req, HttpStatus.OK, "deleted");
    }

    @Administrator
    @POST("/blocker-definition/override/user")
    public void addBlockerDefinitionUserOverride(String userEmail, int blockerId, Req req) {
        blockerDefinitionDao.addUserToBlockerOverride(userEmail, blockerId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, "ok");
    }

    @Administrator
    @DELETE("/blocker-definition/override/user")
    public void deleteBlockerDefinitionUserOverride(String userEmail, int blockerId, Req req) {
        blockerDefinitionDao.deleteUserToBlockerOverride(userEmail, blockerId);
        assignJsonResponseToReq(req, HttpStatus.OK, "deleted");
    }

    @PUT("/blocker-definition/{id}/active/{active}")
    public void updateBlockerDefinitionActiveness(int id, String active, Req req) {

        BlockerDefinition blockerDefinition = blockerDefinitionDao.getBlockerDefinition(id);

        if (blockerDefinition == null) {
            Map<String, String> message = ImmutableMap.of("message", "Blocker not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        try {
            blockerDefinition.setActive(Boolean.valueOf(active));
        } catch (Exception e) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, blockerDefinition);
            return;
        }

        blockerDefinitionDao.updateBlockerDefinition(blockerDefinition);
        logger.info(String.format("Updated blocker's activeness: blockerId - %s, blockerName - %s, active - %s", blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getActive()));
        assignJsonResponseToReq(req, HttpStatus.OK, blockerDefinition);
    }
}
