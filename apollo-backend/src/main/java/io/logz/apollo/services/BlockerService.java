package io.logz.apollo.services;

import io.logz.apollo.blockers.Blocker;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.CrossBlockerFunction;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.SingleRegionBlockerResponse;
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
    private static final String singleRegionBlockerTypeName = "singleregion";

    private final BlockerDefinitionDao blockerDefinitionDao;
    private final BlockerInjectableCommons blockerInjectableCommons;
    private final Map<String, Class<? extends CrossBlockerFunction>> blockerTypeNameBindings;
    private final Reflections reflections;

    @Inject
    public BlockerService(BlockerDefinitionDao blockerDefinitionDao, BlockerInjectableCommons blockerInjectableCommons) {
        this.blockerDefinitionDao = requireNonNull(blockerDefinitionDao);
        this.blockerInjectableCommons = requireNonNull(blockerInjectableCommons);

        blockerTypeNameBindings = new HashMap<>();
        reflections = new Reflections("io.logz.apollo.blockers.types");
    }

    public Optional<Class<? extends CrossBlockerFunction>> getBlockerTypeBinding(String blockerTypeName) {
        if (blockerTypeNameBindings.containsKey(blockerTypeName)) {
            return Optional.of(blockerTypeNameBindings.get(blockerTypeName));
        }

        Set<Class<? extends CrossBlockerFunction>> classes = reflections.getSubTypesOf(CrossBlockerFunction.class);

        Optional<Class<? extends CrossBlockerFunction>> foundClass = classes.stream()
                .filter(clazz -> !clazz.isInterface())
                .filter(clazz -> clazz.getAnnotation(BlockerType.class).name().equals(blockerTypeName))
                .findFirst();

        foundClass.ifPresent(aClass -> blockerTypeNameBindings.put(blockerTypeName, aClass));
        return foundClass;
    }

    public Optional<Blocker> shouldBlock(Deployment deployment) {
        for (Blocker blocker : getBlockers()) {
            if (isBlockerInScope(blocker, deployment)) {
                if (((DeploymentBlockerFunction)blocker.getCrossBlockerFunction()).shouldBlock(blockerInjectableCommons, deployment)) {
                    logger.info("Blocking deployment for service {}, in environment {}, with deployable version of {} from {} due to {} blocker",
                            deployment.getServiceId(), deployment.getEnvironmentId(), deployment.getDeployableVersionId(), deployment.getUserEmail(), blocker.getName());

                    return Optional.of(blocker);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isUserAllowedToOverride(Deployment deployment, Blocker blocker) {
        return blockerInjectableCommons.getBlockerDefinitionDao().getOverrideBlockersIdsByUser(deployment.getUserEmail())
                .stream().anyMatch(id -> id == blocker.getId());
    }

    private List<Blocker> getBlockers() {
        return blockerDefinitionDao.getAllBlockerDefinitions()
                .stream()
                .filter(blockerDefinition -> !blockerDefinition.getBlockerTypeName().equals(singleRegionBlockerTypeName))
                .map(this::createBlockerFromDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<Blocker> getSingleRegionBlockers() {
        return blockerDefinitionDao.getAllBlockerDefinitions()
                .stream()
                .filter(blockerDefinition -> blockerDefinition.getBlockerTypeName().equals(singleRegionBlockerTypeName))
                .map(this::createBlockerFromDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Blocker> createBlockerFromDefinition(BlockerDefinition blockerDefinition) {
        Optional<Class<? extends CrossBlockerFunction>> blockerTypeBinding = getBlockerTypeBinding(blockerDefinition.getBlockerTypeName());
        if (!blockerTypeBinding.isPresent()) {
            logger.warn("Got blocker definition (id {}) of an unknown blocker name {}, nothing to do here!",
                    blockerDefinition.getId(), blockerDefinition.getBlockerTypeName());
            return Optional.empty();
        }

        try {
            CrossBlockerFunction crossBlockerFunction = getBlockerTypeBinding(blockerDefinition.getBlockerTypeName()).get().newInstance();
            crossBlockerFunction.init(blockerDefinition.getBlockerJsonConfiguration());
            return Optional.of(new Blocker(blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getBlockerTypeName(), blockerDefinition.getServiceId(),
                    blockerDefinition.getEnvironmentId(), blockerDefinition.getStackId(), blockerDefinition.getAvailability(), blockerDefinition.getActive(), crossBlockerFunction));

        } catch (InstantiationException | IllegalAccessException e) {
            logger.warn("Could not create instance of {} ", blockerDefinition.getBlockerTypeName(), e);
            return Optional.empty();
        } catch (IOException e) {
            logger.warn("Could not parse parameters for blocker definition {}", blockerDefinition.getId(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean isBlockerInScope(Blocker blocker, Deployment deployment) {
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

        if (blocker.getAvailability() != null && !blocker.getAvailability().isEmpty()) {
            if (blockerInjectableCommons.getEnvironmentDao().getEnvironment(deployment.getEnvironmentId()).getAvailability().equals(blocker.getAvailability()))     {
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

    public SingleRegionBlockerResponse checkDeploymentShouldBeBlockedByServiceByRegionBlocker(List<Integer> serviceIds, List<Integer> environmentIds) {
        for (Blocker blocker : getSingleRegionBlockers()) {
            SingleRegionBlockerResponse singleRegionBlockerResponse = ((RequestBlockerFunction)blocker.getCrossBlockerFunction()).shouldBlock(serviceIds, environmentIds, blockerInjectableCommons, blocker.getAvailability());
            if (singleRegionBlockerResponse.isShouldBlock()){
                return  singleRegionBlockerResponse;
            }
        }

        return new SingleRegionBlockerResponse(false);
    }

}
