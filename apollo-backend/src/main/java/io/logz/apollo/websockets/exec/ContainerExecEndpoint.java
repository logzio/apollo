package io.logz.apollo.websockets.exec;

import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.logz.apollo.common.StringParser;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.kubernetes.KubernetesHandler;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Service;
import io.logz.apollo.websockets.WebsocketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 5/23/17.
 */
@ServerEndpoint(value = "/exec/pod/{podName}/container/{containerName}")
public class ContainerExecEndpoint {

    static final String QUERY_STRING_ENVIRONMENT_KEY = "environment";
    static final String QUERY_STRING_SERVICE_KEY = "service";

    private static final Logger logger = LoggerFactory.getLogger(ContainerExecEndpoint.class);
    private final ExecWebSocketSessionStore execWebSocketSessionStore;
    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final EnvironmentDao environmentDao;
    private final ServiceDao serviceDao;

    @Inject
    public ContainerExecEndpoint(ExecWebSocketSessionStore execWebSocketSessionStore,
                                 KubernetesHandlerStore kubernetesHandlerStore,
                                 EnvironmentDao environmentDao, ServiceDao serviceDao) {
        this.execWebSocketSessionStore = requireNonNull(execWebSocketSessionStore);
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.environmentDao = requireNonNull(environmentDao);
        this.serviceDao = requireNonNull(serviceDao);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("podName") String podName, @PathParam("containerName") String containerName) {
        int environmentId = StringParser.getIntFromQueryString(session.getQueryString(), QUERY_STRING_ENVIRONMENT_KEY);
        int serviceId = StringParser.getIntFromQueryString(session.getQueryString(), QUERY_STRING_SERVICE_KEY);

        Environment environment = environmentDao.getEnvironment(environmentId);
        Service service = serviceDao.getService(serviceId);

        // Get default shell
        String defaultShell = Optional.ofNullable(service.getDefaultShell()).orElse("/bin/bash");

        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        logger.info("Opening ExecWatch to container {} in pod {} in environment {} related to service {}",
                containerName, podName, environment.getName(), service.getName());

        ExecWatch execWatch = kubernetesHandler.getExecWatch(podName, containerName,defaultShell);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        SessionExecModel sessionExecModel = new SessionExecModel(execWatch, executor);
        openReaderThreads(session, sessionExecModel);

        // Initialize the ExecWatch against kubernetes handler
        execWebSocketSessionStore.addSession(session, sessionExecModel);
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("Closing session..");
        SessionExecModel sessionExecModel = execWebSocketSessionStore.getSessionExecModel(session);
        sessionExecModel.getExecutor().shutdownNow();
        sessionExecModel.getExecWatch().close();
        execWebSocketSessionStore.deleteSession(session);
    }

    @OnMessage
    public void onMessageReceived(Session session, String command) {
        SessionExecModel sessionModel = execWebSocketSessionStore.getSessionExecModel(session);
        ExecWatch execWatch = sessionModel.getExecWatch();

        if (execWatch == null) {
            logger.info("Got message to an unknown ExecWatch, ignoring!");
            return;
        }
        try {
            execWatch.getInput().write(command.getBytes());

        } catch (IOException e) {
            logger.warn("Got IO Exception while writing to the ExecWatch!", e);
        }
    }

    private void openReaderThreads(Session session, SessionExecModel sessionExecModel) {
        // TODO: if there is a leak, this might be a suspect.. close those?
        sessionExecModel.getExecutor().execute(() -> WebsocketWriter.readCharsFromStreamToSession(sessionExecModel.getExecWatch().getOutput(), session));
        sessionExecModel.getExecutor().execute(() -> WebsocketWriter.readCharsFromStreamToSession(sessionExecModel.getExecWatch().getError(), session));
    }
}
