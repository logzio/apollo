package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.SingleRegionBlockerResponse;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@BlockerType(name = "singleregion")
public class SingleRegionBlocker implements RequestBlockerFunction {
    private static final Logger logger = LoggerFactory.getLogger(SingleRegionBlocker.class);
    private SingleRegionBlockerConfiguration singleRegionBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        singleRegionBlockerConfiguration = mapper.readValue(jsonConfiguration, SingleRegionBlockerConfiguration.class);
    }

    public SingleRegionBlockerResponse shouldBlock(List<Integer> serviceIdsFromReq, List<Integer> environmentIdsFromReq, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        List<Integer> exceptionServiceIds = getExceptionServiceIds(serviceIdsFromReq);
        if (!exceptionServiceIds.isEmpty()) {
            if (environmentIdsFromReq.size() > 1) {
                return new SingleRegionBlockerResponse(true, exceptionServiceIds.get(0), true);
            }

            //Checks if the current block availability is in the scope of the deployment request.
            if (isEnvironmentInTheBlockerScope(environmentIdsFromReq.get(0), blockerInjectableCommons, blockAvailability)) {
                //For each service we checks if the current service is running already on the blocker's scope.
                for (Integer serviceId : exceptionServiceIds) {
                    if (isServiceRunsAlready(serviceId, blockerInjectableCommons, blockAvailability)) {
                        return new SingleRegionBlockerResponse(true, serviceId, false);
                    }
                }
            }
        }

        return new SingleRegionBlockerResponse(false);
    }

    private List<Integer> getExceptionServiceIds(List<Integer> serviceIds) {
        return serviceIds.stream()
                .distinct()
                .filter(singleRegionBlockerConfiguration.getServiceIds()::contains)
                .collect(Collectors.toList());
    }

    private boolean isServiceRunsAlready(int serviceId, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        Optional<Deployment> deploymentOpt = blockerInjectableCommons.getDeploymentDao().getAllRunningDeployments().stream()
                .filter(deployment -> deployment.getServiceId() == serviceId)
                .filter(deployment -> isDeploymentInTheBlockerScope(deployment, blockerInjectableCommons, blockAvailability))
                .findFirst();

        if (deploymentOpt.isPresent()) {
            Deployment deployment = deploymentOpt.get();
            logger.warn("There is already a running deployment on environment id {} that initiated by {}. Can't start another deployment at the moment.",
                        deployment.getEnvironmentId(), deployment.getUserEmail());
            return true;
        }

        return false;
    }

    private boolean isDeploymentInTheBlockerScope(Deployment deployment, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        return isEnvironmentInTheBlockerScope(deployment.getEnvironmentId(), blockerInjectableCommons, blockAvailability);
    }

    private boolean isEnvironmentInTheBlockerScope(int environmentId, BlockerInjectableCommons blockerInjectableCommons, String blockAvailability) {
        if (Objects.isNull(blockAvailability))
            return true;

        return blockerInjectableCommons.getEnvironmentDao().getEnvironment(environmentId).getAvailability()
                .equals(blockAvailability);
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
