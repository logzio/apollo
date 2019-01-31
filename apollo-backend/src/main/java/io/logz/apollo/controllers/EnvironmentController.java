package io.logz.apollo.controllers;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.models.Environment;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 12/19/16.
 */
@Controller
public class EnvironmentController {

    private final EnvironmentDao environmentDao;

    @Inject
    public EnvironmentController(EnvironmentDao environmentDao) {
        this.environmentDao = requireNonNull(environmentDao);
    }

    @LoggedIn
    @GET("/environment")
    public List<Environment> getEnvironments() {
        return environmentDao.getAllEnvironments().stream()
                .map(this::maskCredentials)
                .collect(Collectors.toList());
    }

    @LoggedIn
    @GET("/environment/{id}")
    public Environment getEnvironment(int id) {
        Environment gotEnvironment = environmentDao.getEnvironment(id);
        gotEnvironment = maskCredentials(gotEnvironment);
        return gotEnvironment;
    }

    @LoggedIn
    @POST("/environment")
    public void addEnvironment(String name, String geoRegion, String availability, String kubernetesMaster,
                               String kubernetesToken, String kubernetesNamespace, Integer servicePortCoefficient,
                               Boolean requireDeploymentMessage, Boolean requireHealthCheck, Integer concurrencyLimit, String additionalParams, Req req) {
        Environment newEnvironment = new Environment();
        newEnvironment.setName(name);
        newEnvironment.setGeoRegion(geoRegion);
        newEnvironment.setAvailability(availability);
        newEnvironment.setKubernetesMaster(kubernetesMaster);
        newEnvironment.setKubernetesToken(kubernetesToken);
        newEnvironment.setKubernetesNamespace(kubernetesNamespace);
        newEnvironment.setServicePortCoefficient(servicePortCoefficient);
        newEnvironment.setRequireDeploymentMessage(requireDeploymentMessage);
        newEnvironment.setRequireHealthCheck(requireHealthCheck);
        newEnvironment.setConcurrencyLimit(concurrencyLimit);
        newEnvironment.setAdditionalParams(additionalParams);

        environmentDao.addEnvironment(newEnvironment);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newEnvironment);
    }

    private Environment maskCredentials(Environment environment) {
        environment.setKubernetesToken("******");
        return environment;
    }

}
