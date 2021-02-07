package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.models.StackType;
import io.logz.apollo.services.BlockerService;
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
import java.util.Optional;

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
    private final StackDao stackDao;
    private final BlockerService blockerService;

    @Inject
    public BlockerDefinitionController(BlockerDefinitionDao blockerDefinitionDao, StackDao stackDao, BlockerService blockerService) {
        this.blockerDefinitionDao = requireNonNull(blockerDefinitionDao);
        this.stackDao = requireNonNull(stackDao);
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
    public void addBlockerDefinition(String name, Integer environmentId, Integer serviceId, Integer stackId, String availability,
                                     Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {
        String userEmail = req.token().get("_user").toString();
        logger.info("User: {} has just added blocker, name:{}", userEmail, name);

        if (blockerTypeName.equals(BlockerTypeName.SINGLE_REGION)) {
            blockerJsonConfiguration = initSingleRegionBlockerJsonConfig(environmentId, serviceId, stackId, availability, blockerJsonConfiguration, req);
            if (blockerJsonConfiguration == null) return;
        }

        if (!blockerService.getBlockerTypeBinding(blockerTypeName).isPresent()) {
            logger.warn("Could not find proper class that annotated with {}", blockerTypeName);
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "There is no implementation for blocker with name " + blockerTypeName);
            return;
        }

        if (!isValid(availability, stackId, environmentId, serviceId)) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, String.format("Trying to add invalid blocker. stackId - %s, environmentId - %s, serviceId - %s, availability - %s", stackId, environmentId, serviceId, availability));
            return;
        }

        BlockerDefinition blockerDefinition = new BlockerDefinition();

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentId);
        blockerDefinition.setServiceId(serviceId);
        blockerDefinition.setStackId(stackId);
        blockerDefinition.setAvailability(availability);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.addBlockerDefinition(blockerDefinition);
        logger.info(String.format("Added blocker: blockerId - %s, blockerName - %s, active - %s", blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getActive()));
        assignJsonResponseToReq(req, HttpStatus.CREATED, blockerDefinition);
    }

    private String initSingleRegionBlockerJsonConfig(Integer environmentId, Integer serviceId, Integer stackId, String availability, String blockerJsonConfiguration, Req req) {
        String jsonConfig = null;

        if (availability == null) {
            logger.error("Could not initiate a SingleRegionBlocker without availability");
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "SingleRegionBlocker cannot be defined without specific availability");
        } else if (environmentId != null || isStackEnvironmentType(stackId)) {
            logger.error("Could not initiate a SingleRegionBlocker with environment or stack");
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "SingleRegionBlocker cannot be defined for a specific environment or stack");
        } else if (blockerJsonConfiguration == null || blockerJsonConfiguration.equals("{}")) {
            Optional<String> configOpt = blockerService.initSingleRegionBlockerConfiguration(serviceId, stackId);
             if (configOpt.isPresent()) {
                 jsonConfig = configOpt.get();
             } else {
                 logger.error("Could not initiate a SingleRegionBlocker without service or service-stack");
                 assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "SingleRegionBlocker cannot be defined without service(s)");
             }
        } else {
            logger.error("Could not initiate a SingleRegionBlocker");
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, "SingleRegionBlocker cannot be defined - please initialize with service/services and availability");
        }

        return jsonConfig;
    }

    private boolean isStackEnvironmentType(Integer stackId) {
        if (stackId == null)
            return false;

        return stackDao.getStackType(stackId) == StackType.ENVIRONMENTS;
    }

    @Administrator
    @PUT("/blocker-definition/{id}")
    public void updateBlockerDefinition(int id, String name, Integer environmentId, Integer serviceId, Integer stackId, String availability,
                                        Boolean isActive, String blockerTypeName, String blockerJsonConfiguration, Req req) {
        String userEmail = req.token().get("_user").toString();
        logger.info("User: {} has just updated blocker, id:{}, name:{}", userEmail, id, name);

        BlockerDefinition blockerDefinition = blockerDefinitionDao.getBlockerDefinition(id);

        if (blockerDefinition == null) {
            Map<String, String> message = ImmutableMap.of("message", "Blocker not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        if (!isValid(availability, stackId, environmentId, serviceId)) {
            assignJsonResponseToReq(req, HttpStatus.BAD_REQUEST, String.format("Trying to update invalid blocker. stackId - {}, environmentId - {}, serviceId - {}, availability - {}", stackId, environmentId, serviceId, availability));
            return;
        }

        blockerDefinition.setName(name);
        blockerDefinition.setEnvironmentId(environmentId);
        blockerDefinition.setServiceId(serviceId);
        blockerDefinition.setStackId(stackId);
        blockerDefinition.setAvailability(availability);
        blockerDefinition.setBlockerTypeName(blockerTypeName);
        blockerDefinition.setBlockerJsonConfiguration(blockerJsonConfiguration);
        blockerDefinition.setActive(isActive);

        blockerDefinitionDao.updateBlockerDefinition(blockerDefinition);
        logger.info(String.format("Updated blocker: blockerId - %s, blockerName - %s, active - %s", blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getActive()));
        assignJsonResponseToReq(req, HttpStatus.OK, blockerDefinition);
    }

    private boolean isValid(String availability, Integer stackId, Integer environmentId, Integer serviceId) {
        if (stackId != null) {
            switch (stackDao.getStackType(stackId)) {
                case ENVIRONMENTS:
                    if (environmentId != null) {
                        logger.error("Error trying to add invalid stack blocker with environment. stackId - {}, environmentId - {}", stackId, environmentId);
                        return false;
                    }
                    break;
                case SERVICES:
                    if (serviceId != null) {
                        logger.error("Error trying to add invalid stack blocker with service. stackId - {}, serviceId - {}", stackId, serviceId);
                        return false;
                    }
                    break;
            }
        }

        if (availability != null) {
            if (environmentId != null) {
                logger.error("Error trying to add invalid availability blocker with environment. environmentId - {}, availability - {}", environmentId, availability);
                return false;
            }
        }

        return true;
    }

    @Administrator
    @DELETE("/blocker-definition/{id}")
    public void deleteBlockerDefinition(int id, Req req) {
        String userEmail = req.token().get("_user").toString();
        logger.info("User: {} has just deleted blocker, id:{}", userEmail, id);

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
