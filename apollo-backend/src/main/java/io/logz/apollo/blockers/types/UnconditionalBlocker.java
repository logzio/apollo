package io.logz.apollo.blockers.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.logz.apollo.blockers.Blocker;
import io.logz.apollo.blockers.BlockerFunction;
import io.logz.apollo.blockers.BlockerInjectableCommons;
import io.logz.apollo.blockers.BlockerType;
import io.logz.apollo.models.Deployment;

import java.io.IOException;
import java.util.List;

/**
 * Created by roiravhon on 6/4/17.
 */
@BlockerType(name = "unconditional")
public class UnconditionalBlocker implements BlockerFunction {

    private static UnconditionalBlocker.UnconditionalBlockerConfiguration unconditionalBlockerConfiguration;

    @Override
    public void init(String jsonConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        unconditionalBlockerConfiguration = mapper.readValue(jsonConfiguration, UnconditionalBlocker.UnconditionalBlockerConfiguration.class);
    }

    @Override
    public boolean shouldBlock(BlockerInjectableCommons blockerInjectableCommons, Deployment deployment, Blocker blocker) {

        List<Integer> exceptionServiceIds = unconditionalBlockerConfiguration.getExceptionServiceIds();
        List<Integer> exceptionEnvironmentIds = unconditionalBlockerConfiguration.getExceptionEnvironmentIds();

        if (exceptionServiceIds == null && exceptionEnvironmentIds == null) {
            return true;
        }

        if (((exceptionServiceIds != null && exceptionServiceIds.contains(deployment.getServiceId())) || blocker.getServiceId() != null)
            && ((exceptionEnvironmentIds != null && exceptionEnvironmentIds.contains(deployment.getEnvironmentId())) || blocker.getEnvironmentId() != null)) {
            return false;
        }

        return true;
    }

    public static class UnconditionalBlockerConfiguration {

        private List<Integer> exceptionServiceIds;
        private List<Integer> exceptionEnvironmentIds;

        public UnconditionalBlockerConfiguration() {
        }

        List<Integer> getExceptionServiceIds() {
            return exceptionServiceIds;
        }

        List<Integer> getExceptionEnvironmentIds() {
            return exceptionEnvironmentIds;
        }

        void setExceptionServiceIds (List<Integer> exceptionServiceIds) {
            this.exceptionServiceIds = exceptionServiceIds;
        }

        void setExceptionEnvironmentIds (List<Integer> exceptionEnvironmentIds) {
            this.exceptionEnvironmentIds = exceptionEnvironmentIds;
        }
    }
}
