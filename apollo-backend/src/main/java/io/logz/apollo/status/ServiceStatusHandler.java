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
import javafx.util.Pair;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ServiceStatusHandler {
    private final DeploymentDao deploymentDao;
    private final DeployableVersionDao deployableVersionDao;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;
    private final GroupDao groupDao;
    private TimeUnit timeUnit;
    private int maxUndeployedTime;

    @Inject
    public ServiceStatusHandler(DeploymentDao deploymentDao, DeployableVersionDao deployableVersionDao, EnvironmentDao environmentDao, ServiceDao serviceDao, GroupDao groupDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);
    }

    public List<EnvironmentServiceGroupMap> getUndeployedServicesByEnvironmentAvailability(String availability, TimeUnit timeUnit, int maxUndeployedTime) {
        this.timeUnit = timeUnit;
        this.maxUndeployedTime = maxUndeployedTime;
        List<EnvironmentServiceGroupMap> result = new ArrayList<>();
        environmentDao.getEnvironmentsByAvailability(availability).forEach(environment -> {
            Map<Service,Optional<List<Group>>> undeployedServicesAndGroups = getUndeployedServicesByEnvironment(environment.getId());
            if (undeployedServicesAndGroups.size() > 0) {
                result.add(new EnvironmentServiceGroupMap(environment.getId(), environment.getName(), undeployedServicesAndGroups));
            }
        });
        return result;
    }

    private Map<Service, Optional<List<Group>>> getUndeployedServicesByEnvironment(int environmentId) {
        List<Service> services = serviceDao.getAllServices();
        Map<Service, Optional<List<Group>>> serviceGroupMapResult = new HashMap<>();
        services.forEach(service -> {
            Optional<Pair<Service,Optional<List<Group>>>> pair = getPairOfUndeployedServiceAndGroup(service, environmentId);
            if(pair.isPresent()) {
                serviceGroupMapResult.put(pair.get().getKey(), pair.get().getValue());
            }
        });
        return serviceGroupMapResult;
    }

    private Optional<Pair<Service,Optional<List<Group>>>> getPairOfUndeployedServiceAndGroup(Service service, int environmentId) {
        List<Group> groups;
        if(service.getIsPartOfGroup()) {
            groups = getUndeployedGroupsByServiceAndEnvironment(service.getId(), environmentId);
            if(groups.size() > 0) {
                return Optional.of(new Pair<>(service, Optional.of(groups)));
            }
        }
        else {
            if(isServiceUndeployed(service.getId(), environmentId, Optional.empty())) {
                return Optional.of(new Pair<>(service, Optional.empty()));
            }
        }
        return Optional.empty();
    }

    private List<Group> getUndeployedGroupsByServiceAndEnvironment(int serviceId, int environmentId) {
        return groupDao.getGroupsPerServiceAndEnvironment(serviceId, environmentId).stream().filter(group ->
                isServiceUndeployed(serviceId, environmentId, Optional.of(group))).collect(Collectors.toList());
    }

    private boolean isServiceUndeployed(int serviceId, int environmentId, Optional<Group> group) {
        Optional<Date> latestDeploymentDate;
        Optional<Date> latestUpdatedDate = getLatestDeployableVersionDateByService(serviceId);
        if(group.isPresent()) {
            latestDeploymentDate = getLatestDeploymentDateByServiceEnvironmentAndGroup(serviceId, environmentId, group.get().getName());
        }
        else {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndEnvironment(serviceId, environmentId);
        }
        return isDeploymentDateExpired(latestDeploymentDate, latestUpdatedDate);
    }

    private boolean isDeploymentDateExpired(Optional<Date> latestDeploymentDate, Optional<Date> latestUpdatedDate) {
        if (latestDeploymentDate.isPresent() && latestUpdatedDate.isPresent()) {
            return getTimeDiff(latestDeploymentDate.get(), latestUpdatedDate.get()) > maxUndeployedTime;
        }
        if (latestUpdatedDate.isPresent()) {
            return latestUpdatedDate.get().compareTo(calculateMaxDateSinceLatestUpdate()) > 0;
        }
        return false;
    }

    private Optional<Date> getLatestDeploymentDateByServiceAndEnvironment(int serviceId, int environmentId) {
        Optional<Deployment> deployment = Optional.ofNullable(deploymentDao.getLatestDeploymentOfServiceAndEnvironment(serviceId, environmentId));
        return deployment.isPresent() ? Optional.of(deployment.get().getLastUpdate()) : Optional.empty();
    }

    private Optional<Date> getLatestDeploymentDateByServiceEnvironmentAndGroup(int serviceId, int environmentId, String groupName) {
        Optional<Deployment> deployment = Optional.ofNullable(deploymentDao.getLatestDeploymentOfServiceAndEnvironmentByGroupName(serviceId, environmentId, groupName));
        return deployment.isPresent() ? Optional.of(deployment.get().getLastUpdate()) : Optional.empty();
    }

    private Optional<Date> getLatestDeployableVersionDateByService(int serviceId) {
        Optional<DeployableVersion> latestDeployableVersion = Optional.ofNullable(deployableVersionDao.getLatestDeployableVersionByServiceId(serviceId));
        return latestDeployableVersion.isPresent() ? Optional.of(latestDeployableVersion.get().getCommitDate()) : Optional.empty();
    }

    private long getTimeDiff(Date date1, Date date2) {
        return timeUnit.convert(Duration.between(date1.toInstant(), date2.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
    }

    private Date calculateMaxDateSinceLatestUpdate() {
        return Date.from(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(TimeUnit.SECONDS.convert(maxUndeployedTime,timeUnit)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
