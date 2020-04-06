package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SlaveConfiguration {

    private String slaveId;
    private int keepaliveIntervalSeconds;
    private boolean isSlave;
    private String slaveCsvEnvironments;

    @JsonCreator
    public SlaveConfiguration(@JsonProperty("slaveId") String slaveId,
                              @JsonProperty("keepaliveIntervalSeconds") int keepaliveIntervalSeconds,
                              @JsonProperty("isSlave") boolean isSlave,
                              @JsonProperty("slaveCsvEnvironments") String slaveCsvEnvironments) {
        if (isBlank(slaveId)) {
            this.slaveId = UUID.randomUUID().toString();
        } else {
            this.slaveId = slaveId;
        }
        if (!isSlave | isNotBlank(slaveCsvEnvironments)) {
            this.keepaliveIntervalSeconds = keepaliveIntervalSeconds;
            this.isSlave = isSlave;
            this.slaveCsvEnvironments = slaveCsvEnvironments;
        }
    }

    public String getSlaveId() {
        return slaveId;
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
