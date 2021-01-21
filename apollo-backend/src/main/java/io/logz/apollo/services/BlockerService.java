package io.logz.apollo.services;

import io.logz.apollo.blockers.Blocker;
import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.blockers.DeploymentBlocker;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.BlockerFunction;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.RequestBlocker;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.RequestBlockerResponse;
import io.logz.apollo.blockers.types.SingleRegionBlocker;
import io.logz.apollo.dao.BlockerDefinitionDao;
import io.logz.apollo.models.BlockerDefinition;
import io.logz.apollo.models.Deployment;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 6/4/17.
 */
@Singleton
public class BlockerService {

    private static final Logger logger = LoggerFactory.getLogger(BlockerService.class);

    private final BlockerDefinitionDao blockerDefinitionDao;
    private final BlockerInjectableCommons blockerInjectableCommons;
    private final Map<String, Class<? extends BlockerFunction>> blockerTypeNameBindings;
    private final Reflections reflections;

    @Inject
    public BlockerService(BlockerDefinitionDao blockerDefinitionDao, BlockerInjectableCommons blockerInjectableCommons) {
        this.blockerDefinitionDao = requireNonNull(blockerDefinitionDao);
        this.blockerInjectableCommons = requireNonNull(blockerInjectableCommons);

        blockerTypeNameBindings = new HashMap<>();
        reflections = new Reflections("io.logz.apollo.blockers.types");
    }

    public Optional<Class<? extends BlockerFunction>> getBlockerTypeBinding(String blockerTypeName) {
        if (blockerTypeNameBindings.containsKey(blockerTypeName)) {
            return Optional.of(blockerTypeNameBindings.get(blockerTypeName));
        }

        Set<Class<? extends BlockerFunction>> classes = reflections.getSubTypesOf(BlockerFunction.class);

        Optional<Class<? extends BlockerFunction>> foundClass = classes.stream()
                .filter(clazz -> !clazz.isInterface())
                .filter(clazz -> clazz.getAnnotation(BlockerType.class).name().equals(blockerTypeName))
                .findFirst();

        foundClass.ifPresent(aClass -> blockerTypeNameBindings.put(blockerTypeName, aClass));
        return foundClass;
    }

    public Optional<DeploymentBlocker> shouldBlock(Deployment deployment) {
        for (DeploymentBlocker blocker : getDeploymentBlockers()) {
            if (isBlockerInScope(blocker, deployment)) {
                if (blocker.getFunction().shouldBlock(blockerInjectableCommons, deployment)) {
                    logger.info("Blocking deployment for service {}, in environment {}, with deployable version of {} from {} due to {} blocker",
                                deployment.getServiceId(), deployment.getEnvironmentId(), deployment.getDeployableVersionId(), deployment.getUserEmail(), blocker.getName());

                    return Optional.of(blocker);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isUserAllowedToOverride(Deployment deployment, DeploymentBlocker deploymentBlocker) {
        return blockerInjectableCommons.getBlockerDefinitionDao().getOverrideBlockersIdsByUser(deployment.getUserEmail())
                .stream().anyMatch(id -> id == deploymentBlocker.getId());
    }

    private List<DeploymentBlocker> getDeploymentBlockers() {
        return blockerDefinitionDao.getAllBlockerDefinitions()
                .stream()
                .map(blockerDefinition -> createBlockerFromDefinition(blockerDefinition, true))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(blocker -> (DeploymentBlocker) blocker)
                .collect(Collectors.toList());
    }

    private List<RequestBlocker> getRequestBlockers() {
        return blockerDefinitionDao.getAllBlockerDefinitions()
                .stream()
                .filter(blockerDefinition -> blockerDefinition.getBlockerTypeName().equals(BlockerTypeName.SINGLE_REGION))
                .map(blockerDefinition -> createBlockerFromDefinition(blockerDefinition, false))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(blocker -> (RequestBlocker) blocker)
                .collect(Collectors.toList());
    }

    private Optional<? extends Blocker> createBlockerFromDefinition(BlockerDefinition blockerDefinition, boolean isDeploymentBlocker) {
        Optional<Class<? extends BlockerFunction>> blockerTypeBinding = getBlockerTypeBinding(blockerDefinition.getBlockerTypeName());

        if (!blockerTypeBinding.isPresent()) {
            logger.warn("Got blocker definition (id {}) of an unknown blocker name {}, nothing to do here!",
                        blockerDefinition.getId(), blockerDefinition.getBlockerTypeName());
            return Optional.empty();
        }

        try {
            return isDeploymentBlocker ? createDeploymentBlockerFromDefinition(blockerDefinition, blockerTypeBinding.get()) :
                    createRequestBlockerFromDefinition(blockerDefinition, blockerTypeBinding.get());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.warn("Could not create instance of {} ", blockerDefinition.getBlockerTypeName(), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Could not parse parameters for blocker definition {}", blockerDefinition.getId(), e);
            return Optional.empty();
        }
    }

    private Optional<RequestBlocker> createRequestBlockerFromDefinition(BlockerDefinition blockerDefinition, Class<? extends BlockerFunction> clazz)
            throws IllegalAccessException, InstantiationException, IOException {
        RequestBlockerFunction blockerFunction = (RequestBlockerFunction) clazz.newInstance();
        blockerFunction.init(blockerDefinition.getBlockerJsonConfiguration());
        return Optional.of(new RequestBlocker(blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getBlockerTypeName(), blockerDefinition.getServiceId(),
                                              blockerDefinition.getEnvironmentId(), blockerDefinition.getStackId(), blockerDefinition.getAvailability(), blockerDefinition.getActive(), blockerFunction));
    }

    private Optional<DeploymentBlocker> createDeploymentBlockerFromDefinition(BlockerDefinition blockerDefinition, Class<? extends BlockerFunction> clazz)
            throws IllegalAccessException, InstantiationException, IOException {
        DeploymentBlockerFunction blockerFunction = (DeploymentBlockerFunction) clazz.newInstance();
        blockerFunction.init(blockerDefinition.getBlockerJsonConfiguration());
        return Optional.of(new DeploymentBlocker(blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getBlockerTypeName(), blockerDefinition.getServiceId(),
                                                 blockerDefinition.getEnvironmentId(), blockerDefinition.getStackId(), blockerDefinition.getAvailability(), blockerDefinition.getActive(), blockerFunction));
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean isBlockerInScope(DeploymentBlocker blocker, Deployment deployment) {
        if (isUserAllowedToOverride(deployment, blocker)) {
            return false;
        }

        Integer environmentToCheck = null;
        Integer serviceToCheck = null;

        if (blocker.getEnvironmentId() != null) {
            environmentToCheck = blocker.getEnvironmentId();
        }

        if (blocker.getServiceId() != null) {
            serviceToCheck = blocker.getServiceId();
        }

        if (blocker.getStackId() != null) {
            switch (blockerInjectableCommons.getStackService().getStackType(blocker.getStackId())) {
                case ENVIRONMENTS:
                    if (blockerInjectableCommons.getStackService().getEnvironmentsStack(blocker.getStackId()).getEnvironments().stream().anyMatch(environmentId -> environmentId == deployment.getEnvironmentId())) {
                        environmentToCheck = deployment.getEnvironmentId();
                    } else {
                        return false;
                    }
                    break;
                case SERVICES:
                    if (blockerInjectableCommons.getStackService().getServicesStack(blocker.getStackId()).getServices().stream().anyMatch(serviceId -> serviceId == deployment.getServiceId())) {
                        serviceToCheck = deployment.getServiceId();
                    } else {
                        return false;
                    }
                    break;
            }

            if (blocker.getStackId() != null && environmentToCheck == null && serviceToCheck == null) {
                return false;
            }
        }

        if (!blocker.getActive()) {
            return false;
        }

        if (environmentToCheck == null && serviceToCheck == null && blocker.getAvailability() == null) {
            return true;
        }

        if (blocker.getTypeName().equals(BlockerTypeName.SINGLE_REGION) && blocker.getAvailability() == null) {
            return true;
        }

        if (blocker.getAvailability() != null && !blocker.getAvailability().isEmpty()) {
            if (blockerInjectableCommons.getEnvironmentDao().getEnvironment(deployment.getEnvironmentId()).getAvailability().equals(blocker.getAvailability())) {
                if ((serviceToCheck != null) && serviceToCheck == deployment.getServiceId()) {
                    return true;
                }
                if (blocker.getServiceId() == null) {
                    return true;
                }
            }
            return false;
        }

        if (environmentToCheck == null && serviceToCheck.equals(deployment.getServiceId())) {
            return true;
        }

        if (serviceToCheck == null && environmentToCheck.equals(deployment.getEnvironmentId())) {
            return true;
        }

        if (environmentToCheck != null && serviceToCheck != null) {
            if (environmentToCheck.equals(deployment.getEnvironmentId()) && serviceToCheck.equals(deployment.getServiceId())) {
                return true;
            }
        }

        return false;
    }

    public RequestBlockerResponse checkDeploymentShouldBeBlockedBySingleRegionBlocker(List<Integer> serviceIds, int numOfEnvironments) {
        for (RequestBlocker blocker : getRequestBlockers()) {
            if (blocker.getActive()) {
                RequestBlockerResponse requestBlockerResponse = blocker.getFunction().shouldBlock(serviceIds, numOfEnvironments);
                if (requestBlockerResponse.isShouldBlock()) {
                    return requestBlockerResponse;
                }
            }
        }

        return new RequestBlockerResponse(false, SingleRegionBlocker.BLOCKER_NAME);
    }

}
