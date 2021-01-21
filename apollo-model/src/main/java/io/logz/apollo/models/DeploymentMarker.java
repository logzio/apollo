package io.logz.apollo.models;

import java.util.Map;

public class DeploymentMarker {
    private String description;
    private String title;
    private Map<String, String> metadata;
    private String tag = "DEPLOYMENT";

    public DeploymentMarker() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
