package io.logz.apollo.models;

import java.util.ArrayList;
import java.util.List;

public class MultiDeploymentResponseObject {

    private List<SuccessfulDeploymentResponseObject> successful;
    private List<UnsuccessfulDeploymentResponseObject> unsuccessful;

    public MultiDeploymentResponseObject() {
        successful = new ArrayList<>();
        unsuccessful = new ArrayList<>();
    }

    public void addUnsuccessful(int groupId, Exception exception) {
        unsuccessful.add(new UnsuccessfulDeploymentResponseObject(groupId, exception));
    }

    public void addSuccessful(int groupId, Deployment deployment) {
        successful.add(new SuccessfulDeploymentResponseObject(groupId, deployment));
    }

    public void addUnsuccessful(int environmentId, int serviceId, Exception exception) {
        unsuccessful.add(new UnsuccessfulDeploymentResponseObject(environmentId, serviceId, exception));
    }

    public void addUnsuccessful(Exception exception) {
        unsuccessful.add(new UnsuccessfulDeploymentResponseObject(exception));
    }

    public void addSuccessful(int environmentId, int serviceId, Deployment deployment) {
        successful.add(new SuccessfulDeploymentResponseObject(environmentId, serviceId, deployment));
    }

    public List<UnsuccessfulDeploymentResponseObject> getUnsuccessful() {
        return unsuccessful;
    }

    public List<SuccessfulDeploymentResponseObject> getSuccessful() {
        return successful;
    }

    public static class SingleDeploymentResponseObject {

        private Integer groupId;
        private Integer environmentId;
        private Integer serviceId;

        SingleDeploymentResponseObject() {}

        SingleDeploymentResponseObject(Integer groupId) {
            this.groupId = groupId;
        }

        SingleDeploymentResponseObject(Integer environmentId, Integer serviceId) {
            this.environmentId = environmentId;
            this.serviceId = serviceId;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public Integer getEnvironmentId() {
            return environmentId;
        }

        public Integer getServiceId() { return serviceId; }
    }

    public static class SuccessfulDeploymentResponseObject extends SingleDeploymentResponseObject {

        private Deployment deployment;

        SuccessfulDeploymentResponseObject() {}

        SuccessfulDeploymentResponseObject(Integer groupId, Deployment deployment) {
            super(groupId);
            this.deployment = deployment;
        }

        SuccessfulDeploymentResponseObject(Integer environmentId, Integer serviceId, Deployment deployment) {
            super(environmentId, serviceId);
            this.deployment = deployment;
        }

        public Deployment getDeployment() {
            return deployment;
        }
    }

    public static class UnsuccessfulDeploymentResponseObject extends SingleDeploymentResponseObject {

        private Exception exception;

        UnsuccessfulDeploymentResponseObject() {}

        UnsuccessfulDeploymentResponseObject(Exception exception) {
            this.exception = exception;
        }

        UnsuccessfulDeploymentResponseObject(Integer groupId, Exception exception) {
            super(groupId);
            this.exception = exception;
        }

        UnsuccessfulDeploymentResponseObject(Integer environmentId, Integer serviceId, Exception exception) {
            super(environmentId, serviceId);
            this.exception = exception;
        }

        public Exception getException() {
            return exception;
        }
    }
}


