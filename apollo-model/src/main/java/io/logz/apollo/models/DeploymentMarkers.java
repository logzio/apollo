package io.logz.apollo.models;

import java.util.List;

public class DeploymentMarkers {
    private List<DeploymentMarker> markers;

    public DeploymentMarkers() {
    }

    public List<DeploymentMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<DeploymentMarker> markers) {
        this.markers = markers;
    }
}
