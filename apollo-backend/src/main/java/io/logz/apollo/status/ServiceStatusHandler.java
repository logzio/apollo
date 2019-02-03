package io.logz.apollo.status;

import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.EnvironmentServiceGroupMap;
import io.logz.apollo.models.Group;
import io.logz.apollo.models.Service;
import io.logz.apollo.models.ServiceGroupPair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Singleton
public class ServiceStatusHandler {
    private final DeploymentDao deploymentDao;
    private final DeployableVersionDao deployableVersionDao;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;
    private final GroupDao groupDao;

    @Inject
    public ServiceStatusHandler(DeploymentDao deploymentDao, DeployableVersionDao deployableVersionDao, EnvironmentDao environmentDao, ServiceDao serviceDao, GroupDao groupDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);
    }

    public List<EnvironmentServiceGroupMap> getUndeployedServicesByAvailability(String availability, TimeUnit timeUnit, int duration) {
        List<EnvironmentServiceGroupMap> result = new ArrayList<>();
        environmentDao.getEnvironmentsByAvailability(availability).forEach(environment -> {
            Map<Service,Optional<List<Group>>> undeployedServicesAndGroups = getUndeployedServicesByEnvironment(environment.getId(), timeUnit, duration);
            if (undeployedServicesAndGroups.size() > 0) {
                result.add(new EnvironmentServiceGroupMap(environment.getId(), environment.getName(), convertServiceGroupsMapToNamesMap(undeployedServicesAndGroups)));
            }
        });
        return result;
    }

    private Map<String,List<String>> convertServiceGroupsMapToNamesMap(Map<Service,Optional<List<Group>>> serviceGroups) {
        Map<String,List<String>> serviceGroupsNamesMap = new HashMap<>();
        serviceGroups.forEach((service, groups) -> {
            if(groups.isPresent()) {
                List<String> groupsNames = new ArrayList<>();
                groups.get().forEach(group -> groupsNames.add(group.getName()));
                serviceGroupsNamesMap.put(service.getName(), groupsNames);
            }
            else {
                serviceGroupsNamesMap.put(service.getName(),new ArrayList<>());
            }
        });
        return serviceGroupsNamesMap;
    }

    private Map<Service, Optional<List<Group>>> getUndeployedServicesByEnvironment(int environmentId, TimeUnit timeUnit, int duration) {
        List<Service> services = serviceDao.getAllServices();
        Map<Service, Optional<List<Group>>> serviceGroupMapResult = new HashMap<>();
        services.forEach(service -> {
            Optional<ServiceGroupPair> pairOfUndeployedServiceAndGroup = getPairOfUndeployedServiceAndGroup(service, environmentId, timeUnit, duration);
            if (pairOfUndeployedServiceAndGroup.isPresent()) {
                ServiceGroupPair pair = pairOfUndeployedServiceAndGroup.get();
                serviceGroupMapResult.put(pair.getService(), pair.getGroupList());
            }
        });
        return serviceGroupMapResult;
    }

    private Optional<ServiceGroupPair> getPairOfUndeployedServiceAndGroup(Service service, int environmentId, TimeUnit timeUnit, int duration) {
        List<Group> groups;
        if(service.getIsPartOfGroup()) {
            groups = getUndeployedGroupsByServiceAndEnvironment(service.getId(), environmentId, timeUnit, duration);
            if(groups.size() > 0) {
                return Optional.ofNullable(new ServiceGroupPair(service, Optional.ofNullable(groups)));
            }
        }
        else {
            if(isServiceUndeployed(service.getId(), environmentId, Optional.empty(), timeUnit, duration)) {
                return Optional.ofNullable(new ServiceGroupPair(service, Optional.empty()));
            }
        }
        return Optional.empty();
    }

    private List<Group> getUndeployedGroupsByServiceAndEnvironment(int serviceId, int environmentId, TimeUnit timeUnit, int duration) {
        return groupDao.getGroupsPerServiceAndEnvironment(serviceId, environmentId).stream().filter(group ->
                isServiceUndeployed(serviceId, environmentId, Optional.ofNullable(group), timeUnit, duration)).collect(Collectors.toList());
    }

    private boolean isServiceUndeployed(int serviceId, int environmentId, Optional<Group> group, TimeUnit timeUnit, int duration) {
        Optional<Date> latestDeploymentDate;
        Optional<Date> latestDeployableVersionDate = getLatestDeployableVersionDateByService(serviceId);
        if(group.isPresent()) {
            latestDeploymentDate = getLatestDeploymentDateByServiceEnvironmentAndGroup(serviceId, environmentId, group.get().getName());
        }
        else {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndEnvironment(serviceId, environmentId);
        }
        return isDeploymentDateExpired(latestDeploymentDate, latestDeployableVersionDate, timeUnit, duration);
    }

    private boolean isDeploymentDateExpired(Optional<Date> latestDeploymentDate, Optional<Date> latestDeployableVersionDate, TimeUnit timeUnit, int duration) {
        if (latestDeploymentDate.isPresent() && latestDeployableVersionDate.isPresent()) {
            return getTimeDiff(latestDeploymentDate.get(), latestDeployableVersionDate.get(), timeUnit) > duration;
        }
        return false;
    }

    private Optional<Date> getLatestDeploymentDateByServiceAndEnvironment(int serviceId, int environmentId) {
        Deployment deployment = deploymentDao.getLatestDeploymentOfServiceAndEnvironment(serviceId, environmentId);
        if(Objects.isNull(deployment)) {
            return Optional.empty();
        }
        return Optional.ofNullable(deployment.getLastUpdate());
    }

    private Optional<Date> getLatestDeploymentDateByServiceEnvironmentAndGroup(int serviceId, int environmentId, String groupName) {
        Deployment deployment = deploymentDao.getLatestDeploymentOfServiceAndEnvironmentByGroupName(serviceId, environmentId, groupName);
        if (Objects.isNull(deployment)) {
            return Optional.empty();
        }
        return Optional.ofNullable(deployment.getLastUpdate());
    }

    private Optional<Date> getLatestDeployableVersionDateByService(int serviceId) {
        DeployableVersion latestDeployableVersion = deployableVersionDao.getLatestDeployableVersionByServiceId(serviceId);
        if(Objects.isNull(latestDeployableVersion)) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestDeployableVersion.getCommitDate());
    }

    private long getTimeDiff(Date latestDeploymentDate, Date latestDeployableVersionDate, TimeUnit timeUnit) {
        return timeUnit.convert(Duration.between(latestDeploymentDate.toInstant().atZone(ZoneId.of("UTC")), latestDeployableVersionDate.toInstant().atZone(ZoneId.of("UTC"))).toMillis(), TimeUnit.MILLISECONDS);
    }

}
