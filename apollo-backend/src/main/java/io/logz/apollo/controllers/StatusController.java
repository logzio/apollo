package io.logz.apollo.controllers;

import io.logz.apollo.common.ControllerCommon;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.kubernetes.KubernetesHandler;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.EnvironmentServiceGroupMap;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.KubernetesDeploymentStatus;
import io.logz.apollo.models.Service;
import io.logz.apollo.status.ServiceStatusHandler;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 2/22/17.
 */
@Controller
public class StatusController {

    private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;
    private final GroupDao groupDao;
    private final ServiceStatusHandler serviceStatusHandler;
    private final DeployableVersionController deployableVersionController;

    @Inject
    public StatusController(KubernetesHandlerStore kubernetesHandlerStore, EnvironmentDao environmentDao,
                            ServiceDao serviceDao, GroupDao groupDao, ServiceStatusHandler serviceStatusHandler,
                            DeployableVersionController deployableVersionController) {
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);
        this.serviceStatusHandler = requireNonNull(serviceStatusHandler);
        this.deployableVersionController = deployableVersionController;
    }

    @GET("/status/service/{id}")
    public List<KubernetesDeploymentStatus> getCurrentServiceStatus(int id) {
        Service service = serviceDao.getService(id);
        List<KubernetesDeploymentStatus> kubernetesDeploymentStatusList = new ArrayList<>();

        environmentDao.getAllEnvironments().forEach(environment -> {
            KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
            if (service.getIsPartOfGroup()) {
                List<Group> relatedGroups = groupDao.getGroupsPerServiceAndEnvironment(id, environment.getId());
                relatedGroups.forEach(group -> {
                    String groupName = group.getName();
                    try {
                        KubernetesDeploymentStatus kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service, Optional.of(groupName));
                        kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, service.getId());
                        kubernetesDeploymentStatusList.add(kubernetesDeploymentStatus);
                    } catch (Exception e) {
                        logger.warn("Could not get status of service {}, on environment {}, group {}! trying others..", id, environment.getId(), groupName, e);
                    }
                });
            } else {
                try {
                    KubernetesDeploymentStatus kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service);
                    kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, service.getId());
                    kubernetesDeploymentStatusList.add(kubernetesDeploymentStatus);
                } catch (Exception e) {
                    logger.warn("Could not get status of service {}, on environment {}! trying others..", id, environment.getId(), e);
                }
            }
        });

        return kubernetesDeploymentStatusList;
    }

    @GET("/status/environment/{id}")
    public List<KubernetesDeploymentStatus> getCurrentEnvironmentStatus(int id) {
        List<KubernetesDeploymentStatus> kubernetesDeploymentStatusList = new ArrayList<>();
        Environment environment = environmentDao.getEnvironment(id);
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        serviceDao.getAllServices().forEach(service -> {
            if (service.getIsPartOfGroup()) {
                List<Group> relatedGroups = groupDao.getGroupsPerServiceAndEnvironment(service.getId(), id);
                relatedGroups.forEach(group -> {
                    String groupName = group.getName();
                    try {
                        KubernetesDeploymentStatus kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service, Optional.of(groupName));
                        kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, service.getId());
                        kubernetesDeploymentStatusList.add(kubernetesDeploymentStatus);
                    } catch (Exception e) {
                        logger.warn("Could not get status of service {}, on environment {}, group {}! trying others..", service.getId(), id, groupName, e);
                    }
                });
            } else {
                try {
                    KubernetesDeploymentStatus kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service);
                    kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, service.getId());
                    kubernetesDeploymentStatusList.add(kubernetesDeploymentStatus);
                } catch (Exception e) {
                    logger.warn("Could not get status of service {} on environment {}! trying others..", service.getId(), id, e);
                }
            }
        });

        return kubernetesDeploymentStatusList;
    }

    @GET("/status/environment/{envId}/service/{serviceId}/group/{groupName}")
    public KubernetesDeploymentStatus getOneSpecificStatusWithGroup(int envId, int serviceId, String groupName) {

        Environment environment = environmentDao.getEnvironment(envId);
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
        Service service = serviceDao.getService(serviceId);
        KubernetesDeploymentStatus kubernetesDeploymentStatus = null;

        if (service != null) {
            try {
                kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service, Optional.of(groupName));
                kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, serviceId);
            } catch (Exception e) {
                logger.warn("Could not get status of service {}, on environment {}, group {}!", service.getId(), envId, groupName, e);
            }
        }

        return kubernetesDeploymentStatus;
    }

    @GET("/status/environment/{envId}/service/{serviceId}/all-groups")
    public List<KubernetesDeploymentStatus> getStatusesWithGroups(int envId, int serviceId) {

        Environment environment = environmentDao.getEnvironment(envId);
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
        Service service = serviceDao.getService(serviceId);
        List<KubernetesDeploymentStatus> kubernetesDeploymentStatuses = new ArrayList<>();

        if (service != null) {
            List<Group> groups = groupDao.getGroupsPerServiceAndEnvironment(serviceId,envId);
            try {
                groups.forEach(group -> {
                    KubernetesDeploymentStatus kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service, Optional.of(group.getName()));
                    kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, serviceId);
                    kubernetesDeploymentStatuses.add(kubernetesDeploymentStatus);
                });
            } catch (Exception e) {
                logger.warn("Could not get status of service {}, on environment {}!", service.getId(), envId, e);
            }
        }

        return kubernetesDeploymentStatuses;
    }

    @GET("/status/environment/{envId}/service/{serviceId}")
    public KubernetesDeploymentStatus getOneSpecificStatus(int envId, int serviceId) {

        Environment environment = environmentDao.getEnvironment(envId);
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
        Service service = serviceDao.getService(serviceId);
        KubernetesDeploymentStatus kubernetesDeploymentStatus = null;

        if (service != null) {
            try {
                kubernetesDeploymentStatus = kubernetesHandler.getCurrentStatus(service);
                kubernetesDeploymentStatus = addCommitUrlToStatus(kubernetesDeploymentStatus, serviceId);
            } catch (Exception e) {
                logger.warn("Could not get status of service {}, on environment {}!", service.getId(), envId, e);
            }
        }

        return  kubernetesDeploymentStatus;
    }

    @LoggedIn
    @GET("/status/environment/{environmentId}/service/{serviceId}/latestpod")
    public String getLatestPodName(int environmentId, int serviceId, Req req) {
        Environment environment = environmentDao.getEnvironment(environmentId);
        Service service = serviceDao.getService(serviceId);

        if (service.getIsPartOfGroup()) {
            ControllerCommon.assignJsonResponseToReq(req, HttpStatus.NOT_ACCEPTABLE, "Missing group ID for service " + service.getId());
            return "";
        }

        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        Optional<String> serviceLatestCreatedPodName = kubernetesHandler.getServiceLatestCreatedPodName(service);
        if (serviceLatestCreatedPodName.isPresent()) {
            return serviceLatestCreatedPodName.get();
        } else {
            ControllerCommon.assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, "Can't find pod");
            return "";
        }
    }

    @LoggedIn
    @GET("/status/environment/{environmentId}/service/{serviceId}/group/{groupName}/latestpod")
    public String getLatestPodName(int environmentId, int serviceId, String groupName, Req req) {
        Environment environment = environmentDao.getEnvironment(environmentId);
        Service service = serviceDao.getService(serviceId);

        if (!service.getIsPartOfGroup()) {
            ControllerCommon.assignJsonResponseToReq(req, HttpStatus.NOT_ACCEPTABLE, "Service " + service.getId() + " can't have group ID");
            return "";
        }

        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        Optional<String> serviceLatestCreatedPodName = kubernetesHandler.getServiceLatestCreatedPodName(service, Optional.of(groupName));
        if (serviceLatestCreatedPodName.isPresent()) {
            return serviceLatestCreatedPodName.get();
        } else {
            ControllerCommon.assignJsonResponseToReq(req, HttpStatus.NOT_FOUND, "Can't find pod");
            return "";
        }
    }

    @LoggedIn
    @GET("/status/environment/{environmentId}/pod/{podName}/containers")
    public List<String> getPodContainers(int environmentId, String podName) {
        Environment environment = environmentDao.getEnvironment(environmentId);
        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        return kubernetesHandler.getPodContainerNames(podName);
    }

    @GET("/status/get-undeployed-services/avaiability/{availability}/time-unit/{timeUnit}/duration/{duration}")
    public List<EnvironmentServiceGroupMap> getUndeployedServicesByAvailability(String availability, String timeUnit, int duration) {
        TimeUnit timeUnitEnum;
        try {
            timeUnitEnum = TimeUnit.valueOf(timeUnit);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Please pass timeUnit parameter in TimeUnit type template", e.getCause());
        }
        return serviceStatusHandler.getUndeployedServicesByAvailability(availability, timeUnitEnum, duration);
    }

    private KubernetesDeploymentStatus addCommitUrlToStatus(KubernetesDeploymentStatus kubernetesDeploymentStatus, int serviceId) {
        if (kubernetesDeploymentStatus != null && kubernetesDeploymentStatus.getGitCommitSha() != null) {
            DeployableVersion deployableVersion = deployableVersionController.getDeployableVersionFromSha(kubernetesDeploymentStatus.getGitCommitSha(), serviceId);
            if (deployableVersion != null) {
                kubernetesDeploymentStatus.setGitCommitUrl(deployableVersion.getCommitUrl());
            }
        }
        return kubernetesDeploymentStatus;
    }
}
