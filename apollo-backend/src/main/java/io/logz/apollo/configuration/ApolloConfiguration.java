package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApolloConfiguration {

    private ApiConfiguration api;
    private DatabaseConfiguration database;
    private KubernetesConfiguration kubernetes;
    private ScmConfiguration scm;
    private WebsocketConfiguration websocket;
    private SlaveConfiguration slave;
    private CancelDeploymentConfiguration cancelDeployment;

    @JsonCreator
    public ApolloConfiguration(@JsonProperty("api") ApiConfiguration api,
                               @JsonProperty("db") DatabaseConfiguration database,
                               @JsonProperty("kubernetes") KubernetesConfiguration kubernetes,
                               @JsonProperty("scm") ScmConfiguration scm,
                               @JsonProperty("websocket") WebsocketConfiguration websocket,
                               @JsonProperty("slave") SlaveConfiguration slave,
                               @JsonProperty("cancelDeployment") CancelDeploymentConfiguration cancelDeployment) {
        this.api = api;
        this.database = database;
        this.kubernetes = kubernetes;
        this.scm = scm;
        this.websocket = websocket;
        this.slave = slave;
        this.cancelDeployment = cancelDeployment;
    }

    public ApiConfiguration getApi() {
        return api;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public KubernetesConfiguration getKubernetes() {
        return kubernetes;
    }

    public ScmConfiguration getScm() {
        return scm;
    }

    public WebsocketConfiguration getWebsocket() {
        return websocket;
    }

    public SlaveConfiguration getSlave() {
        return slave;
    }

    public CancelDeploymentConfiguration getCancelDeployment() {
        return cancelDeployment;
    }
}
