package io.logz.apollo.controllers;

import com.google.inject.Inject;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.kubernetes.KubernetesHealth;
import io.logz.apollo.services.SlaveService;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.Map;
import java.util.Set;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 3/2/17.
 */
@Controller
public class HealthController {

    private final KubernetesHealth kubernetesHealth;
    private final EnvironmentDao environmentDao;
    private final SlaveService slaveService;
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Inject
    public HealthController(KubernetesHealth kubernetesHealth, EnvironmentDao environmentDao, SlaveService slaveService) {
        this.kubernetesHealth = requireNonNull(kubernetesHealth);
        this.environmentDao = requireNonNull(environmentDao);
        this.slaveService = requireNonNull(slaveService);
    }

    @GET("/health")
    public void getHealth(Req req) {
        Set<Integer> scopedEnvironments = slaveService.getScopedEnvironments();
        Map<Integer, Boolean> environmentsHealthMap = kubernetesHealth.getEnvironmentsHealthMap();
        environmentsHealthMap.keySet()
                             .stream()
                             .filter(envId -> !scopedEnvironments.contains(envId))
                             .forEach(environmentsHealthMap::remove);

        if (environmentsHealthMap.containsValue(false)) {
            environmentsHealthMap.entrySet()
                                 .stream()
                                 .filter(environment -> !environment.getValue())
                                 .forEach(environment -> {
                                     MDC.put("environmentId", String.valueOf(environment.getKey()));
                                     MDC.put("environmentName", String.valueOf(environmentDao.getEnvironment(environment.getKey()).getName()));
                                     logger.error("Unhealthy environment, environmentId: {}, environmentName: {}.", environment.getKey(), environmentDao.getEnvironment(environment.getKey()).getName());
                                 });
            assignJsonResponseToReq(req, HttpStatus.INTERNAL_SERVER_ERROR, environmentsHealthMap);
        } else {
            assignJsonResponseToReq(req, HttpStatus.OK, environmentsHealthMap);
        }
    }
}
