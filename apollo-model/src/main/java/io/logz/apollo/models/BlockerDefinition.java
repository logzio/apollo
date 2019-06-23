package io.logz.apollo.models;

import java.util.Date;

/**
 * Created by roiravhon on 6/4/17.
 */
public class BlockerDefinition {

    private int id;
    private String name;
    private Integer serviceId;
    private Integer environmentId;
    private Integer stackId;
    private Date startedAt;
    private Boolean isActive;
    private String blockerTypeName;
    private String blockerJsonConfiguration;

    public BlockerDefinition() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public Integer getStackId() {
        return stackId;
    }

    public void setStackId(Integer stackId) {
        this.stackId = stackId;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getBlockerTypeName() {
        return blockerTypeName;
    }

    public void setBlockerTypeName(String blockerTypeName) {
        this.blockerTypeName = blockerTypeName;
    }

    public String getBlockerJsonConfiguration() {
        return blockerJsonConfiguration;
    }

    public void setBlockerJsonConfiguration(String blockerJsonConfiguration) {
        this.blockerJsonConfiguration = blockerJsonConfiguration;
    }
}
