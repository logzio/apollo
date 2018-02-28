package io.logz.apollo.blockers;

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
                .filter(clazz -> clazz.getAnnotation(BlockerType.class).name().equals(blockerTypeName))
                .findFirst();

        foundClass.ifPresent(aClass -> blockerTypeNameBindings.put(blockerTypeName, aClass));
        return foundClass;
    }

    public Optional<Blocker> shouldBlock(Deployment deployment) {
        for (Blocker blocker : getBlockers()) {
            if (isBlockerInScope(blocker, deployment)) {
                if (blocker.getBlockerFunction().shouldBlock(blockerInjectableCommons, deployment)) {
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
                .map(this::createBlockerFromDefinition)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Blocker> createBlockerFromDefinition(BlockerDefinition blockerDefinition) {
        Optional<Class<? extends BlockerFunction>> blockerTypeBinding = getBlockerTypeBinding(blockerDefinition.getBlockerTypeName());
        if (!blockerTypeBinding.isPresent()) {
            logger.warn("Got blocker definition (id {}) of an unknown blocker name {}, nothing to do here!",
                    blockerDefinition.getId(), blockerDefinition.getBlockerTypeName());
            return Optional.empty();
        }

        try {
            BlockerFunction blockerFunction = getBlockerTypeBinding(blockerDefinition.getBlockerTypeName()).get().newInstance();
            blockerFunction.init(blockerDefinition.getBlockerJsonConfiguration());
            return Optional.of(new Blocker(blockerDefinition.getId(), blockerDefinition.getName(), blockerDefinition.getBlockerTypeName(), blockerDefinition.getServiceId(),
                    blockerDefinition.getEnvironmentId(), blockerDefinition.getActive(), blockerFunction));

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
        if (!blocker.getActive()) {
            return false;
        }

        if (isUserAllowedToOverride(deployment, blocker)) {
            return false;
        }

        if (blocker.getEnvironmentId() == null && blocker.getServiceId() == null) {
            return true;
        }

        if (blocker.getEnvironmentId() == null && blocker.getServiceId().equals(deployment.getServiceId())) {
            return true;
        }

        if (blocker.getServiceId() == null && blocker.getEnvironmentId().equals(deployment.getEnvironmentId())) {
            return true;
        }

        if (blocker.getEnvironmentId() != null && blocker.getServiceId() != null) {
            if (blocker.getEnvironmentId().equals(deployment.getEnvironmentId()) && blocker.getServiceId().equals(deployment.getServiceId())) {
                return true;
            }
        }

        return false;
    }
}
