package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SlaveConfiguration {

    private String slaveId;
    private int keepaliveIntervalSeconds;
    private boolean isSlave;
    private String slaveCsvEnvironments;
    private boolean disableApiServer;

    @JsonCreator
    public SlaveConfiguration(@JsonProperty("slaveId") String slaveId,
                              @JsonProperty("keepaliveIntervalSeconds") int keepaliveIntervalSeconds,
                              @JsonProperty("isSlave") boolean isSlave,
                              @JsonProperty("slaveCsvEnvironments") String slaveCsvEnvironments,
                              @JsonProperty("disableApiServer") boolean disableApiServer) {
        Preconditions.checkArgument(!isSlave | isNotBlank(slaveCsvEnvironments));

        if (isBlank(slaveId)) {
            this.slaveId = UUID.randomUUID().toString();
        } else {
            this.slaveId = slaveId;
        }

        this.keepaliveIntervalSeconds = keepaliveIntervalSeconds;
        this.isSlave = isSlave;
        this.slaveCsvEnvironments = slaveCsvEnvironments;
        this.disableApiServer = disableApiServer;
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

    public boolean isDisableApiServer() {
        return disableApiServer;
    }
}
