package io.logz.apollo.status;

import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.GroupDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.EnvironmentServices;
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

    @Inject
    public ServiceStatusHandler(DeploymentDao deploymentDao, DeployableVersionDao deployableVersionDao, EnvironmentDao environmentDao, ServiceDao serviceDao, GroupDao groupDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
        this.groupDao = requireNonNull(groupDao);
    }

    public List<EnvironmentServices> getUndeployedServicesByEnvironmentAvailability(String availability, TimeUnit timeUnit, int maxUndeployedTime) {
        List<EnvironmentServices> result = new ArrayList<>();
        environmentDao.getEnvironmentsByAvailability(availability).forEach(environment -> {
            Map<Service,Optional<List<Group>>> undeployedServicesAndGroups = getUndeployedServicesByEnvironment(environment.getId(), timeUnit, maxUndeployedTime);
            if (undeployedServicesAndGroups.size() != 0) {
                result.add(new EnvironmentServices(environment.getId(), environment.getName(), undeployedServicesAndGroups));
            }
        });
        return result;
    }

    private Map<Service, Optional<List<Group>>> getUndeployedServicesByEnvironment(int environmentId, TimeUnit timeUnit, int maxUndeployedTime) {
        List<Service> services = serviceDao.getAllServices();
        Map<Service, Optional<List<Group>>> serviceGroupMapResult = new HashMap<>();
        services.forEach(service -> {
            Optional<Pair<Service,Optional<List<Group>>>> pair = getPairOfUndeployedServiceAndGroup(service, environmentId, timeUnit, maxUndeployedTime);
            if(pair.isPresent()) {
                serviceGroupMapResult.put(pair.get().getKey(), pair.get().getValue());
            }
        });
        return serviceGroupMapResult;
    }

    private Optional<Pair<Service,Optional<List<Group>>>> getPairOfUndeployedServiceAndGroup(Service service, int environmentId, TimeUnit timeUnit, int maxUndeployedTime) {
        List<Group> groups;
        if(service.getIsPartOfGroup()) {
            groups = getUndeployedGroupsByService(service.getId(), environmentId, timeUnit, maxUndeployedTime);
            if(groups.size() != 0) {
                return Optional.of(new Pair<>(service, Optional.of(groups)));
            }
        }
        else {
            if(isServiceUndeployed(service.getId(), environmentId, Optional.empty(), timeUnit, maxUndeployedTime)) {
                return Optional.of(new Pair<>(service, Optional.empty()));
            }
        }
        return Optional.empty();
    }

    private List<Group> getUndeployedGroupsByService(int serviceId, int environmentId, TimeUnit timeUnit, int maxUndeployedTime) {
        return groupDao.getGroupsPerServiceAndEnvironment(serviceId, environmentId).stream().filter(group ->
                isServiceUndeployed(serviceId, environmentId, Optional.of(group), timeUnit, maxUndeployedTime)).collect(Collectors.toList());
    }

    private boolean isServiceUndeployed(int serviceId, int environmentId, Optional<Group> group, TimeUnit timeUnit, int maxUndeployedTime) {
        Optional<Date> latestDeploymentDate;
        Optional<Date> latestUpdatedDate = getLatestDeployableVersionDateByService(serviceId);
        if(group.isPresent()) {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndGroupAndEnvironment(serviceId, environmentId, group.get().getName());
        }
        else {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndEnvironment(serviceId, environmentId);
        }
        return isDeploymentDateExpired(latestDeploymentDate, latestUpdatedDate, timeUnit, maxUndeployedTime);
    }

    private boolean isDeploymentDateExpired(Optional<Date> latestDeploymentDate, Optional<Date> latestUpdatedDate, TimeUnit timeUnit, int maxUndeployedTime) {
        if (latestDeploymentDate.isPresent() && latestUpdatedDate.isPresent()) {
            return getTimeDiff(latestDeploymentDate.get(), latestUpdatedDate.get(), timeUnit) > maxUndeployedTime;
        }
        if (latestUpdatedDate.isPresent()) {
            return latestUpdatedDate.get().compareTo(calculateMaxDateSinceLatestUpdate(timeUnit, maxUndeployedTime)) > 0;
        }
        return false;
    }

    private Optional<Date> getLatestDeploymentDateByServiceAndEnvironment(int serviceId, int environmentId) {
        Optional<Deployment> deployment = Optional.ofNullable(deploymentDao.getLatestDeploymentOfServiceAndEnvironment(serviceId, environmentId));
        return deployment.isPresent() ? Optional.of(deployment.get().getLastUpdate()) : Optional.empty();
    }

    private Optional<Date> getLatestDeploymentDateByServiceAndGroupAndEnvironment(int serviceId, int environmentId, String groupName) {
        Optional<Deployment> deployment = Optional.ofNullable(deploymentDao.getLatestDeploymentOfServiceAndEnvironmentByGroupName(serviceId, environmentId, groupName));
        return deployment.isPresent() ? Optional.of(deployment.get().getLastUpdate()) : Optional.empty();
    }

    private Optional<Date> getLatestDeployableVersionDateByService(int serviceId) {
        Optional<DeployableVersion> latestDeployableVersion = Optional.ofNullable(deployableVersionDao.getLatestDeployableVersionByServiceId(serviceId));
        return latestDeployableVersion.isPresent() ? Optional.of(latestDeployableVersion.get().getCommitDate()) : Optional.empty();
    }

    private long getTimeDiff(Date date1, Date date2, TimeUnit resultTimeUnit) {
        return resultTimeUnit.convert(Duration.between(date1.toInstant(), date2.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
    }

    private Date calculateMaxDateSinceLatestUpdate(TimeUnit timeUnit, int maxTimeInterval) {
        return Date.from(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(TimeUnit.SECONDS.convert(maxTimeInterval,timeUnit)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
