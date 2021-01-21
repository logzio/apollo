package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.BlockerTypeName;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.RequestBlockerResponse;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@BlockerType(name = BlockerTypeName.SINGLE_REGION)
public class SingleRegionBlocker implements RequestBlockerFunction, DeploymentBlockerFunction {
    private static final Logger logger = LoggerFactory.getLogger(SingleRegionBlocker.class);
    public static final String BLOCKER_NAME = "SingleRegionBlocker";
    private SingleRegionBlockerConfiguration singleRegionBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        singleRegionBlockerConfiguration = mapper.readValue(jsonConfiguration, SingleRegionBlockerConfiguration.class);
    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment) {
        List<Integer> runningServices = getAllOngoingDeploymentsByServiceId(deployment.getServiceId(), blockerInjectableCommons);
        if (!runningServices.isEmpty()) {
            logger.warn("There are already running deployments for the service {}", deployment.getServiceId());
            return true;
        }
        return false;
    }

    @Override
    public RequestBlockerResponse shouldBlock(List<Integer> serviceIds, int numOfEnvironments) {
        List<Integer> serviceIdsToCheck = getServicesWithinSingleRegionBlocker(serviceIds);
        if (!serviceIdsToCheck.isEmpty()) {
            if (numOfEnvironments > 1) {
                return new RequestBlockerResponse(true, BLOCKER_NAME, serviceIdsToCheck);
            }
        }
        return new RequestBlockerResponse(false, BLOCKER_NAME);
    }

    private List<Integer> getServicesWithinSingleRegionBlocker(List<Integer> serviceIds) {
        return serviceIds.stream()
                .distinct()
                .filter(singleRegionBlockerConfiguration.getServiceIds()::contains)
                .collect(Collectors.toList());
    }

    private List<Integer> getAllOngoingDeploymentsByServiceId(int serviceId, BlockerInjectableCommons blockerInjectableCommons) {
        return blockerInjectableCommons.getDeploymentDao().getAllOngoingDeploymentsByServiceId(serviceId).stream()
                .map(deployment -> deployment.getServiceId())
                .collect(Collectors.toList());
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
