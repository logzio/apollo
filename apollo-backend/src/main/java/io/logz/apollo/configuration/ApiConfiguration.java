package io.logz.apollo.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiConfiguration {

    private int port;
    private String listen;
    private String secret;
    private boolean disableApiServer;

    @JsonCreator
    public ApiConfiguration(@JsonProperty("port") int port,
                            @JsonProperty("listen") String listen,
                            @JsonProperty("secret") String secret,
                            @JsonProperty("disableApiServer") boolean disableApiServer) {
        this.port = port;
        this.listen = listen;
        this.secret = secret;
        this.disableApiServer = disableApiServer;
    }

    public int getPort() {
        return port;
    }

    public String getListen() {
        return listen;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isDisableApiServer() {
        return disableApiServer;
    }
}
