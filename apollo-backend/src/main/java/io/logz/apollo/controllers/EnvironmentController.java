package io.logz.apollo.controllers;

import com.google.common.collect.ImmutableMap;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.common.StringParser;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.models.Environment;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.Administrator;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
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
    public List<Environment> getEnvironments(Req req) {
        Map<String, String> queryStringMap = StringParser.getQueryStringMap(req.query());
        boolean active = Boolean.parseBoolean(queryStringMap.get("active"));

        List<Environment> environments = active ? environmentDao.getAllActiveEnvironments() :
                environmentDao.getAllEnvironments();
        return environments.stream()
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
                               String kubernetesToken, String kubernetesCaCert, String kubernetesNamespace, Integer servicePortCoefficient,
                               Boolean requireDeploymentMessage, Boolean requireHealthCheck, Integer concurrencyLimit,
                               String additionalParams, Boolean isActive, Req req) {
        Environment newEnvironment = new Environment();
        newEnvironment.setName(name);
        newEnvironment.setGeoRegion(geoRegion);
        newEnvironment.setAvailability(availability);
        newEnvironment.setKubernetesMaster(kubernetesMaster);
        newEnvironment.setKubernetesToken(kubernetesToken);
        newEnvironment.setKubernetesCaCert(kubernetesCaCert);
        newEnvironment.setKubernetesNamespace(kubernetesNamespace);
        newEnvironment.setServicePortCoefficient(servicePortCoefficient);
        newEnvironment.setRequireDeploymentMessage(requireDeploymentMessage);
        newEnvironment.setRequireHealthCheck(requireHealthCheck);
        newEnvironment.setConcurrencyLimit(concurrencyLimit);
        newEnvironment.setAdditionalParams(additionalParams);
        newEnvironment.setIsActive(isActive);

        environmentDao.addEnvironment(newEnvironment);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newEnvironment);
    }

    @LoggedIn
    @Administrator
    @PUT("/environment")
    public void editEnvironment(Integer id, String name, String geoRegion, String availability, String kubernetesMaster,
                                String kubernetesToken, String kubernetesCaCert, String kubernetesNamespace, Integer servicePortCoefficient,
                                Boolean requireDeploymentMessage, Boolean requireHealthCheck, Integer concurrencyLimit,
                                String additionalParams, Boolean isActive, Req req) {
        Environment environment = environmentDao.getEnvironment(id);

        if (environment == null) {
            Map<String, String> message = ImmutableMap.of("message", "Environment not found");
            assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, message);
            return;
        }

        environment.setName(name);
        environment.setGeoRegion(geoRegion);
        environment.setAvailability(availability);
        environment.setKubernetesMaster(kubernetesMaster);
        environment.setKubernetesToken(kubernetesToken);
        environment.setKubernetesCaCert(kubernetesCaCert);
        environment.setKubernetesNamespace(kubernetesNamespace);
        environment.setServicePortCoefficient(servicePortCoefficient);
        environment.setRequireDeploymentMessage(requireDeploymentMessage);
        environment.setRequireHealthCheck(requireHealthCheck);
        environment.setConcurrencyLimit(concurrencyLimit);
        environment.setAdditionalParams(additionalParams);
        environment.setIsActive(isActive);

        environmentDao.updateEnvironment(environment);
        Environment updatedEnvironment = maskCredentials(environment);
        assignJsonResponseToReq(req, HttpStatus.OK, updatedEnvironment);
    }

    private Environment maskCredentials(Environment environment) {
        environment.setKubernetesToken("******");
        return environment;
    }

}
