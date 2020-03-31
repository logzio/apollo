package io.logz.apollo.models;

import java.time.Duration;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slave {
    private String slaveId;
    private int environmentId;
    private Date lastKeepalive;
    private static final Logger logger = LoggerFactory.getLogger(Slave.class);

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
        logger.info("$$$ environmentId - "+environmentId
            +  ", lastKeepalive - "+lastKeepalive);
        logger.info("$$$ now - "+new Date().toInstant());
        logger.info("$$$ diff in seconds - "+ Duration.between(lastKeepalive.toInstant(), new Date().toInstant()).getSeconds());
        return Duration.between(lastKeepalive.toInstant(), new Date().toInstant()).getSeconds();
    }
}
