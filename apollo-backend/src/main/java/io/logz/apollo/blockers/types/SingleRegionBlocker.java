package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.SingleRegionBlockerResponse;
import io.logz.apollo.blockers.SingleRegionBlockerResponse.BlockerCause;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@BlockerType(name = BlockerTypeName.SINGLE_REGION)
public class SingleRegionBlocker implements RequestBlockerFunction {
    private static final Logger logger = LoggerFactory.getLogger(SingleRegionBlocker.class);
    private static final String PROD_AVAILABILITY = "PROD";
    private static final String STAGING_AVAILABILITY = "STAGING";

    private SingleRegionBlockerConfiguration singleRegionBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        singleRegionBlockerConfiguration = mapper.readValue(jsonConfiguration, SingleRegionBlockerConfiguration.class);
    }

    @Override
    public SingleRegionBlockerResponse shouldBlock(List<Integer> serviceIds, List<Integer> environmentIds, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        List<Integer> serviceIdsToCheck = getServicesWithinSingleRegionBlocker(serviceIds);

        if (!serviceIdsToCheck.isEmpty()) {
            if (environmentIds.size() > 1) {
                return new SingleRegionBlockerResponse(true, serviceIdsToCheck, BlockerCause.MULTIPLE_ENVIRONMENTS);
            }

            boolean checkedServicesInProd = false;
            boolean checkedServicesInStaging = false;

            for (Integer environmentId : environmentIds) {
                String curEnvironmentAvailability = blockerInjectableCommons.getEnvironmentDao().getEnvironment(environmentId).getAvailability();
                //Checks if there is a point to check the services on the current environment.
                if (!(checkedServicesInProd && curEnvironmentAvailability.equals(PROD_AVAILABILITY)) &&
                        !(checkedServicesInStaging && curEnvironmentAvailability.equals(STAGING_AVAILABILITY))) {
                    //Checks if the current block availability is in the scope of the deployment request.
                    if (isEnvironmentInTheBlockerScope(curEnvironmentAvailability, blockAvailability)) {
                        //For all needed services we checks if the services are already running on the blocker's scope.
                        List<Integer> runningServices = getAllOngoingDeploymentsByServiceIds(serviceIdsToCheck, blockerInjectableCommons, blockAvailability);
                        if (!runningServices.isEmpty()) {
                            logger.warn("There are already running deployments for the services {}", runningServices);
                            return new SingleRegionBlockerResponse(true, runningServices, BlockerCause.SERVICE_ALREADY_RUN);
                        }
                    }

                    if (blockAvailability == null) {
                        return new SingleRegionBlockerResponse(false);
                    }

                    if (curEnvironmentAvailability.equals(PROD_AVAILABILITY)) {
                        checkedServicesInProd = true;
                    } else if (curEnvironmentAvailability.equals(STAGING_AVAILABILITY)) {
                        checkedServicesInStaging = true;
                    }
                } else if ((checkedServicesInProd && blockAvailability.equals(PROD_AVAILABILITY)) ||
                        (checkedServicesInStaging && blockAvailability.equals(STAGING_AVAILABILITY))) {
                    return new SingleRegionBlockerResponse(false);
                }

            }
        }

        return new SingleRegionBlockerResponse(false);
    }

    private List<Integer> getServicesWithinSingleRegionBlocker(List<Integer> serviceIds) {
        return serviceIds.stream()
                .distinct()
                .filter(singleRegionBlockerConfiguration.getServiceIds()::contains)
                .collect(Collectors.toList());
    }

    private List<Integer> getAllOngoingDeploymentsByServiceIds(List<Integer> serviceIds, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        return blockerInjectableCommons.getDeploymentDao().getAllOngoingDeploymentsByServiceIds(serviceIds).stream()
                .filter(deployment -> isDeploymentInTheBlockerScope(deployment, blockerInjectableCommons, blockAvailability))
                .map(deployment -> deployment.getServiceId())
                .collect(Collectors.toList());
    }

    private boolean isDeploymentInTheBlockerScope(Deployment deployment, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        String environmentAvailability = blockerInjectableCommons.getEnvironmentDao().getEnvironment(deployment.getEnvironmentId()).getAvailability();
        return isEnvironmentInTheBlockerScope(environmentAvailability, blockAvailability);
    }

    private boolean isEnvironmentInTheBlockerScope(String environmentAvailability, String blockAvailability) {
        if (Objects.isNull(blockAvailability))
            return true;

        return environmentAvailability.equals(blockAvailability);
    }


    private static class SingleRegionBlockerConfiguration {
        private List<Integer> serviceIds;

        public SingleRegionBlockerConfiguration() {
        }

        public List<Integer> getServiceIds() {
            return serviceIds;
        }

        public void setServiceIds(List<Integer> serviceId) {
            this.serviceIds = serviceId;
        }
    }
}
