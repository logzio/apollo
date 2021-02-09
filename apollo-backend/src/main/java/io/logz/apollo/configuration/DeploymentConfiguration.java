package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeploymentConfiguration {
    private int forceCancelTimeoutSeconds;

    @JsonCreator
    public DeploymentConfiguration(@JsonProperty("timeoutInSeconds") int forceCancelTimeoutSeconds) {
        this.forceCancelTimeoutSeconds = forceCancelTimeoutSeconds;
    }

    public int getForceCancelTimeoutSeconds() {
        return forceCancelTimeoutSeconds;
    }

    public void setForceCancelTimeoutSeconds(int forceCancelTimeoutSeconds) {
        this.forceCancelTimeoutSeconds = forceCancelTimeoutSeconds;
    }
}
