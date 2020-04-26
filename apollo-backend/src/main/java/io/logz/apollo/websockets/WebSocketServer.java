package io.logz.apollo.websockets;

import com.google.inject.Injector;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.services.SlaveService;
import io.logz.apollo.websockets.exec.AuthenticationFilter;
import io.logz.apollo.websockets.exec.ContainerExecEndpoint;
import io.logz.apollo.websockets.logs.ContainerLogsEndpoint;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.InvalidWebSocketException;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

@Singleton
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private final GuiceConfigurator configurator;
    private final Server server;
    private final SlaveService slaveService;

    @Inject
    public WebSocketServer(ApolloConfiguration configuration, AuthenticationFilter authenticationFilter,
                           Injector injector, SlaveService slaveService) {
        this.configurator = new GuiceConfigurator(injector);
        this.server = createWebsocketServer(configuration, authenticationFilter);
        this.slaveService = requireNonNull(slaveService);
    }

    private Server createWebsocketServer(ApolloConfiguration configuration, AuthenticationFilter authenticationFilter) {
        try {
            Server server = new Server(configuration.getWebsocket().getPort());
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.addFilter(new FilterHolder(authenticationFilter), "/exec/*", null);
            server.setHandler(context);

            ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);
            wsContainer.setDefaultMaxSessionIdleTimeout(configuration.getWebsocket().getIdleTimeoutSeconds() * 1000);
            wsContainer.addEndpoint(createEndpointConfig(ContainerExecEndpoint.class));
            wsContainer.addEndpoint(createEndpointConfig(ContainerLogsEndpoint.class));

            return server;
        } catch (ServletException e) {
            throw new RuntimeException("Could not initialize WebSocket container!", e);
        } catch (DeploymentException e) {
            throw new RuntimeException("Could not register websocket endpoint!", e);
        }
    }

    @PostConstruct
    public void start() {
        try {
            logger.info("Starting Jetty server");
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start Jetty server!", e);
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping Jetty server");
        if (server.isStarted() || server.isStarting()) {
            try {
                server.stop();
            } catch (Exception e) {
                logger.warn("Could not stop Jetty server!", e);
            }
        } else {
            logger.warn("Jetty already stopped, or did not start. can't stop!");
        }
    }

    private ServerEndpointConfig createEndpointConfig(Class<?> endpointClass) throws DeploymentException {
        ServerEndpoint annotation = endpointClass.getAnnotation(ServerEndpoint.class);
        if (annotation == null) {
            throw new InvalidWebSocketException("Unsupported WebSocket object, missing @" +
                    ServerEndpoint.class + " annotation");
        }

        return ServerEndpointConfig.Builder.create(endpointClass, annotation.value())
                .subprotocols(Arrays.asList(annotation.subprotocols()))
                .decoders(Arrays.asList(annotation.decoders()))
                .encoders(Arrays.asList(annotation.encoders()))
                .configurator(configurator)
                .build();
    }

}
