package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.TimeUnit;

public class CancelDeploymentConfiguration {
    private TimeUnit timeUnit;
    private int timeout;

    @JsonCreator
    public CancelDeploymentConfiguration(@JsonProperty("timeUnit") String timeUnit,
                                         @JsonProperty("timeout") int timeout) {
        try {
            this.timeUnit = TimeUnit.valueOf(timeUnit);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Please pass timeUnit parameter in TimeUnit type template", e.getCause());
        }
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
