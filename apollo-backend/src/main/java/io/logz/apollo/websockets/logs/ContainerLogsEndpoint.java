package io.logz.apollo.websockets.logs;

import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.logz.apollo.common.StringParser;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.kubernetes.KubernetesHandler;
import io.logz.apollo.kubernetes.KubernetesHandlerStore;
import io.logz.apollo.models.Environment;
import io.logz.apollo.websockets.WebsocketWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

@ServerEndpoint(value = "/logs/pod/{podName}/container/{containerName}")
public class ContainerLogsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ContainerLogsEndpoint.class);
    private static final String QUERY_STRING_ENVIRONMENT_KEY = "environment";

    private final LogsWebSocketSessionStore logsWebSocketSessionStore;
    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final EnvironmentDao environmentDao;

    @Inject
    public ContainerLogsEndpoint(LogsWebSocketSessionStore logsWebSocketSessionStore,
                                 KubernetesHandlerStore kubernetesHandlerStore,
                                 EnvironmentDao environmentDao) {
        this.logsWebSocketSessionStore = requireNonNull(logsWebSocketSessionStore);
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.environmentDao = requireNonNull(environmentDao);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("podName") String podName, @PathParam("containerName") String containerName) {
        int environmentId = StringParser.getIntFromQueryString(session.getQueryString(), QUERY_STRING_ENVIRONMENT_KEY);

        Environment environment = environmentDao.getEnvironment(environmentId);

        KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);

        logger.info("Opening LogWatch to container {} in pod {} in environment {}",
                containerName, podName, environment.getName());


        LogWatch logWatch = kubernetesHandler.getLogWatch(podName, containerName);
        ExecutorService executor = Executors.newFixedThreadPool(1);

        SessionLogWatchModel sessionLogWatchModel = new SessionLogWatchModel(logWatch, executor);
        openReaderThread(session, sessionLogWatchModel);

        logsWebSocketSessionStore.addSession(session, sessionLogWatchModel);
    }

    @OnClose
    public void onClose(Session session) {
        logger.info("Closing session..");
        SessionLogWatchModel sessionLogWatchModel = logsWebSocketSessionStore.getSessionLogWatchModel(session);
        sessionLogWatchModel.getExecutor().shutdownNow();
        sessionLogWatchModel.getLogWatch().close();
        logsWebSocketSessionStore.deleteSession(session);
    }

    private void openReaderThread(Session session, SessionLogWatchModel sessionLogWatchModel) {
        // TODO: if there is a leak, this might be a suspect.. close those?
        sessionLogWatchModel.getExecutor().execute(() -> WebsocketWriter.readLinesFromStreamToSession(sessionLogWatchModel.getLogWatch().getOutput(), session));
    }
}
