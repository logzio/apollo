package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeploymentConfiguration {
    private int timeoutInSeconds;

    @JsonCreator
    public DeploymentConfiguration(@JsonProperty("timeoutInSeconds") int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }
}
