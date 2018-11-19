package io.logz.apollo.status;

import io.logz.apollo.dao.DeployableVersionDao;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.DeployableVersion;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.EnvironmentServices;
import io.logz.apollo.models.Service;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ServiceStatusHandler {
    private final DeploymentDao deploymentDao;
    private final DeployableVersionDao deployableVersionDao;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;

    @Inject
    public ServiceStatusHandler(DeploymentDao deploymentDao, DeployableVersionDao deployableVersionDao, EnvironmentDao environmentDao, ServiceDao serviceDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
        this.deployableVersionDao = requireNonNull(deployableVersionDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
    }


    public List<EnvironmentServices> getUndeployedServicesByEnvironmentAvailability(String availability, TimeUnit timeUnit, int undeployedTimeAmount) {
        List<EnvironmentServices> result = new ArrayList<>();
        environmentDao.getEnvironmentsByAvailability(availability).forEach(environment -> {
            List<Service> undeployedServices = getUndeployedServicesByEnvironmentId(environment.getId(), timeUnit, undeployedTimeAmount);
            if (undeployedServices.size() != 0) {
                result.add(new EnvironmentServices(environment.getId(), environment.getName(), undeployedServices));
            }
        });
        return result;
    }

    private List<Service> getUndeployedServicesByEnvironmentId(int environmentId, TimeUnit timeUnit, int maxTimeInterval) {
        List<Service> services = serviceDao.getAllServices();
        return services.stream().filter(service -> isServiceUndeployed(environmentId, service.getId(), timeUnit, maxTimeInterval)).collect(Collectors.toList());
    }

    private boolean isServiceUndeployed(int environmentId, int serviceId, TimeUnit timeUnit, int maxTimeInterval) {
        Date latestDeploymentDate = getLatestDeploymentDateByServiceIdInEnvironment(environmentId, serviceId);
        Date latestUpdatedDate = getLatestDeployableVersionDateByServiceId(serviceId);

        if (latestDeploymentDate != null) {
            return getTimeInterval(latestDeploymentDate, latestUpdatedDate, timeUnit) > maxTimeInterval;
        }
        if (latestUpdatedDate != null) {
            return latestUpdatedDate.compareTo(calculateMaxDateSinceLatestUpdate(timeUnit, maxTimeInterval)) > 0;
        }
        return false;
    }

    private Date getLatestDeploymentDateByServiceIdInEnvironment(int environmentId, int serviceId) {
        Deployment deployment = deploymentDao.getLatestDeploymentOfServiceInEnvironment(serviceId, environmentId);
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

    private long getTimeInterval(Date date1, Date date2, TimeUnit resultTimeUnit) {
        return resultTimeUnit.convert(Duration.between(date1.toInstant(), date2.toInstant()).toMillis(), TimeUnit.MILLISECONDS);
    }

    private Date calculateMaxDateSinceLatestUpdate(TimeUnit timeUnit, int maxTimeInterval) {
        return Date.from(LocalDateTime.now(ZoneId.of("UTC")).minusSeconds(TimeUnit.SECONDS.convert(maxTimeInterval,timeUnit)).atZone(ZoneId.systemDefault()).toInstant());
    }
}
