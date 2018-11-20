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


    public List<EnvironmentServices> getUndeployedServicesByEnvironmentAvailability(String availability, TimeUnit timeUnit, int undeployedTimeAmount) {
        List<EnvironmentServices> result = new ArrayList<>();
        environmentDao.getEnvironmentsByAvailability(availability).forEach(environment -> {
            Map<Service,Optional<Group>> undeployedServices = getUndeployedServicesByEnvironment(environment.getId(), timeUnit, undeployedTimeAmount);
            if (undeployedServices.size() != 0) {
                result.add(new EnvironmentServices(environment.getId(), environment.getName(), undeployedServices));
            }
        });
        return result;
    }

    private Map<Service, Optional<Group>> getUndeployedServicesByEnvironment(int environmentId, TimeUnit timeUnit, int maxUndeployedTime) {
        List<Service> services = serviceDao.getAllServices();
        Map<Service, Optional<Group>> serviceGroupMap = new HashMap<>();
        Map<Service, Optional<Group>> serviceGroupMapResult = new HashMap<>();
        services.forEach(service -> {
            if(service.getIsPartOfGroup()) {
                groupDao.getGroupsPerServiceAndEnvironment(service.getId(), environmentId).forEach(group -> serviceGroupMap.put(service, Optional.of(group))); //TODO computeifabsent
            }
            else {
                serviceGroupMap.put(service, Optional.empty());
            }
        });
        serviceGroupMap
                .entrySet()
                .stream()
                .filter(pair -> isServiceUndeployed(pair.getKey().getId(), environmentId, pair.getValue(), timeUnit, maxUndeployedTime))
                .forEach(pair -> serviceGroupMapResult.put(pair.getKey(), pair.getValue()));
        return serviceGroupMapResult;
    }

    private boolean isServiceUndeployed(int serviceId, int environmentId, Optional<Group> group, TimeUnit timeUnit, int maxUndeployedTime) {
        Date latestDeploymentDate;
        Date latestUpdatedDate = getLatestDeployableVersionDateByServiceId(serviceId);
        if(group.isPresent()) {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndGroupInEnvironment(serviceId, environmentId, group.get().getName());
        }
        else {
            latestDeploymentDate = getLatestDeploymentDateByServiceAndEnvironment(serviceId, environmentId);
        }
        return isDeploymentDateExpired(latestDeploymentDate, latestUpdatedDate, timeUnit, maxUndeployedTime);
    }

    private boolean isDeploymentDateExpired(Date latestDeploymentDate, Date latestUpdatedDate, TimeUnit timeUnit, int maxUndeployedTime) {
        if (latestDeploymentDate != null && latestUpdatedDate != null) {
            return getTimeDiff(latestDeploymentDate, latestUpdatedDate, timeUnit) > maxUndeployedTime;
        }
        if (latestUpdatedDate != null) {
            return latestUpdatedDate.compareTo(calculateMaxDateSinceLatestUpdate(timeUnit, maxUndeployedTime)) > 0;
        }
        return false;
    }

    private Date getLatestDeploymentDateByServiceAndEnvironment(int serviceId, int environmentId) {
        Deployment deployment = deploymentDao.getLatestDeploymentOfServiceAndEnvironment(serviceId, environmentId);
        if(deployment != null){
            return deployment.getLastUpdate();
        }
        return null;
    }

    private Date getLatestDeploymentDateByServiceAndGroupInEnvironment(int serviceId, int environmentId, String groupName) {
        Deployment deployment = deploymentDao.getLatestDeploymentOfServiceAndEnvironmentByGroupName(serviceId, environmentId, groupName);
        if(deployment != null){
            return deployment.getLastUpdate();
        }
        return null;
    }

    private Date getLatestDeployableVersionDateByServiceId(int serviceId) {
        DeployableVersion latestDeployableVersion = deployableVersionDao.getLatestDeployableVersionByServiceId(serviceId);
        if(latestDeployableVersion != null) {
            return latestDeployableVersion.getCommitDate();
        }
        return null;
    }

    private long getTimeDiff(Date date1, Date date2, TimeUnit resultTimeUnit) {
        return resultTimeUnit.convert(Duration.between(date1.toInstant(), date2.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
    }

    private Date calculateMaxDateSinceLatestUpdate(TimeUnit timeUnit, int maxTimeInterval) {
        return Date.from(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(TimeUnit.SECONDS.convert(maxTimeInterval,timeUnit)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
