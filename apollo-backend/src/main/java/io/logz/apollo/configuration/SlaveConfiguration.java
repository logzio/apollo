package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlaveConfiguration {

    private int keepaliveIntervalSeconds;

    @JsonCreator
    public SlaveConfiguration(@JsonProperty("keepaliveIntervalSeconds") int keepaliveIntervalSeconds) {
        this.keepaliveIntervalSeconds = keepaliveIntervalSeconds;
    }

    public int getKeepaliveIntervalSeconds() {
        return keepaliveIntervalSeconds;
    }
}
