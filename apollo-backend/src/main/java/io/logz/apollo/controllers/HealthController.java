package io.logz.apollo.controllers;

import com.google.inject.Inject;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.kubernetes.KubernetesHealth;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.util.Map;
import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 3/2/17.
 */
@Controller
public class HealthController {

    private final KubernetesHealth kubernetesHealth;
    private final EnvironmentDao environmentDao;
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Inject
    public HealthController(KubernetesHealth kubernetesHealth, EnvironmentDao environmentDao) {
        this.kubernetesHealth= requireNonNull(kubernetesHealth);
        this.environmentDao= requireNonNull(environmentDao);
    }

    @GET("/health")
    public void getHealth(Req req) {
        Map<Integer, Boolean> environmentsHealthMap = kubernetesHealth.getEnvironmentsHealthMap();
        if (environmentsHealthMap.containsValue(false)) {
            environmentsHealthMap.entrySet()
                                 .stream()
                                 .filter(environment -> !environment.getValue())
                                 .forEach(environment -> {
                                     MDC.put("environmentId", String.valueOf(environment.getKey()));
                                     MDC.put("environmentName", String.valueOf(environment.getValue()));
                                     logger.error("Unhealthy environment, environmentId: {}, environmentName: {}.", environment.getKey(), environmentDao.getEnvironment(environment.getKey()).getName());
                                 });
            assignJsonResponseToReq(req, HttpStatus.INTERNAL_SERVER_ERROR, environmentsHealthMap);
        } else {
            assignJsonResponseToReq(req, HttpStatus.OK, environmentsHealthMap);
        }
    }
}
