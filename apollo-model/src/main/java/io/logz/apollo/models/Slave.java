package io.logz.apollo.models;

import java.time.Duration;
import java.util.Date;

public class Slave {
    private String slaveId;
    private int environmentId;
    private Date lastKeepalive;

    public Slave() {
    }

    public String getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(String slaveId) {
        this.slaveId = slaveId;
    }

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public Date getLastKeepalive() {
        return lastKeepalive;
    }

    public void setLastKeepalive(Date lastKeepalive) {
        this.lastKeepalive = lastKeepalive;
    }

    public long getSecondsSinceLastKeepalive() {
        return Duration.between(lastKeepalive.toInstant(), new Date().toInstant()).getSeconds();
    }
}
