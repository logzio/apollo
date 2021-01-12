package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.DeploymentBlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@BlockerType(name = "concurrent")
public class ConcurrencyBlocker implements DeploymentBlockerFunction {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyBlocker.class);
    private ConcurrencyBlockerConfiguration concurrencyBlockerConfiguration;
    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        concurrencyBlockerConfiguration = mapper.readValue(jsonConfiguration, ConcurrencyBlockerConfiguration.class);

    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment) {

        if (deployment.getGroupName() != null) {
            return false;
        }

        if (concurrencyBlockerConfiguration.getExcludeServices().contains(deployment.getServiceId())) {
            return false;
        }

        long runningDeployments = blockerInjectableCommons.getDeploymentDao().getAllRunningDeployments().stream()
                .filter(runningDeployment -> runningDeployment.getEnvironmentId() == deployment.getEnvironmentId())
                .count();

        if (runningDeployments >= concurrencyBlockerConfiguration.getAllowedConcurrentDeployment()) {
            return true;
        }

        return false;
    }

    public static class ConcurrencyBlockerConfiguration {
        private int allowedConcurrentDeployment;
        private List<Integer> excludeServices;

        public ConcurrencyBlockerConfiguration() {
        }

        public int getAllowedConcurrentDeployment() {
            return allowedConcurrentDeployment;
        }

        public List<Integer> getExcludeServices() { return excludeServices; }
    }
}
