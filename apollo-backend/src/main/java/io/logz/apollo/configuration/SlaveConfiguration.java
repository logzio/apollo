package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlaveConfiguration {

    private int keepaliveIntervalSeconds;
    private boolean isSlave;
    private String slaveCsvEnvironments;

    @JsonCreator
    public SlaveConfiguration(@JsonProperty("keepaliveIntervalSeconds") int keepaliveIntervalSeconds,
                              @JsonProperty("isSlave") boolean isSlave,
                              @JsonProperty("slaveCsvEnvironments") String slaveCsvEnvironments) {
        this.keepaliveIntervalSeconds = keepaliveIntervalSeconds;
        this.isSlave = isSlave;
        this.slaveCsvEnvironments = slaveCsvEnvironments;
    }

    public int getKeepaliveIntervalSeconds() {
        return keepaliveIntervalSeconds;
    }
    
    public boolean isSlave() {
        return isSlave;
    }

    public String getSlaveCsvEnvironments() {
        return slaveCsvEnvironments;
    }
}
