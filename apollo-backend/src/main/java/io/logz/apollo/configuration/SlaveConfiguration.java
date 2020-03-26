package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SlaveConfiguration {

    private int keepaliveIntervalSeconds;
    private String slaveProperty;
    private String slaveCsvEnvironments;

    @JsonCreator
    public SlaveConfiguration(@JsonProperty("keepaliveIntervalSeconds") int keepaliveIntervalSeconds,
                              @JsonProperty("slaveProperty") String slaveProperty,
                              @JsonProperty("slaveCsvEnvironments") String slaveCsvEnvironments) {
        this.keepaliveIntervalSeconds = keepaliveIntervalSeconds;
        this.slaveProperty = slaveProperty;
        this.slaveCsvEnvironments = slaveCsvEnvironments;
    }

    public int getKeepaliveIntervalSeconds() {
        return keepaliveIntervalSeconds;
    }
    
    public String getSlaveProperty() {
        return slaveProperty;
    }

    public String getSlaveCsvEnvironments() {
        return slaveCsvEnvironments;
    }
}
