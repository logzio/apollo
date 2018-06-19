package io.logz.apollo.controllers;

import com.google.common.collect.ImmutableMap;
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
    @GET("/blocker-definition/unconditional/regions/{regions}/environment-types/{environmentTypes}")
    public List<Integer> getUnconditionalBlockersByEnvironmentTypeAndRegion(String regions, String environmentTypes) {
        return blockerDefinitionDao.getUnconditionalBlockersByEnvironmentTypeAndRegion(regions, environmentTypes);
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional/regions/{regions}")
    public List<Integer> getUnconditionalBlockersByRegion(String regions) {
        return blockerDefinitionDao.getUnconditionalBlockersByRegion(regions);
    }

    @LoggedIn
    @GET("/blocker-definition/unconditional/environment-types/{environmentTypes}")
    public List<Integer> getUnconditionalBlockersDefinitionsByEnvironmentTypeA(String environmentTypes) {
        return blockerDefinitionDao.getUnconditionalBlockersByEnvironmentType(environmentTypes);
    }

    @LoggedIn
    @GET("/blocker-definition/{id}")
    public BlockerDefinition getBlockerDefinition(int id) {
        return blockerDefinitionDao.getBlockerDefinition(id);
    }

    @Administrator
    @POST("/blocker-definition")
    public void addBlockerDefinition(String name, String environmentId, String serviceId, Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {

        if (!blockerService.getBlockerTypeBinding(blockerTypeName).isPresent()) {
            logger.warn("Could not find proper class that annotated with {}", blockerTypeName);
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "There is no implementation for blocker with name " + blockerTypeName);
            return;
        }

        Integer environmentIdParsed = null;
        Integer serviceIdParsed = null;

        if (environmentId != null && !environmentId.equals("null"))
            environmentIdParsed = Integer.parseInt(environmentId);

        if (serviceId != null && !serviceId.equals("null"))
            serviceIdParsed = Integer.parseInt(serviceId);

        BlockerDefinition blockerDefinition = new BlockerDefinition();

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentIdParsed);
        blockerDefinition.setServiceId(serviceIdParsed);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.addBlockerDefinition(blockerDefinition);
        assignJsonResponseToReq(req, HttpStatus.CREATED, blockerDefinition);
    }

    @Administrator
    @PUT("/blocker-definition/{id}")
    public void updateBlockerDefinition(int id, String name, String environmentId, String serviceId, Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {

        BlockerDefinition blockerDefinition = blockerDefinitionDao.getBlockerDefinition(id);

        if (blockerDefinition == null) {
            Map<String, String> message = ImmutableMap.of("message", "Blocker not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        Integer environmentIdParsed = null;
        Integer serviceIdParsed = null;

        if (environmentId != null && !environmentId.equals("null"))
            environmentIdParsed = Integer.parseInt(environmentId);

        if (serviceId != null && !serviceId.equals("null"))
            serviceIdParsed = Integer.parseInt(serviceId);

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentIdParsed);
        blockerDefinition.setServiceId(serviceIdParsed);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.updateBlockerDefinition(blockerDefinition);
        assignJsonResponseToReq(req, HttpStatus.OK, blockerDefinition);
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

        assignJsonResponseToReq(req, HttpStatus.OK, blockerDefinition);
    }
}
