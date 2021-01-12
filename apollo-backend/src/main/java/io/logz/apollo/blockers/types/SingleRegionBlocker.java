package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.blockers.RequestBlockerFunction;
import io.logz.apollo.blockers.SingleRegionBlockerResponse;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
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

    public SingleRegionBlockerResponse shouldBlock(List<Integer> serviceIdsFromReq, int numOfEnvironments, List<Deployment> runningDeployments) {
        Optional<Integer> exceptionServiceIdOpt = getExceptionServiceId(serviceIdsFromReq);
        if (exceptionServiceIdOpt.isPresent()) {
            int serviceId = exceptionServiceIdOpt.get();
            if (numOfEnvironments > 1) {
                return new SingleRegionBlockerResponse(true, serviceId, true);
            } else if (isServiceRunsAlready(runningDeployments, serviceId)) {
                return new SingleRegionBlockerResponse(true, serviceId, false);
            }
        }

        return new SingleRegionBlockerResponse(false);
    }

    private Optional<Integer> getExceptionServiceId(List<Integer> serviceIds) {
        List<Integer> result = serviceIds.stream()
                .distinct()
                .filter(singleRegionBlockerConfiguration.getServiceIds()::contains)
                .collect(Collectors.toList());

        return Optional.ofNullable(result.get(0));
    }

    private boolean isServiceRunsAlready(List<Deployment> runningDeployments, int serviceId) {
        Optional<Deployment> deploymentOpt = runningDeployments.stream()
                .filter(deployment -> deployment.getServiceId() == serviceId)
                .findFirst();

        if (deploymentOpt.isPresent()) {
            Deployment deployment = deploymentOpt.get();
            logger.warn("There is already a running deployment on environment id {} that initiated by {}. Can't start another deployment at the moment.",
                        deployment.getEnvironmentId(), deployment.getUserEmail());
            return true;
        }

        return false;
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
