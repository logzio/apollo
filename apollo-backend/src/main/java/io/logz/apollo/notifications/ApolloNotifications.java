package io.logz.apollo.notifications;

import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.NotificationDao;
import io.logz.apollo.dao.ServiceDao;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Notification;
import io.logz.apollo.models.Service;
import io.logz.apollo.notifications.senders.MarkersSender;
import io.logz.apollo.notifications.senders.NotificationSender;
import io.logz.apollo.notifications.senders.SlackSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Singleton
public class ApolloNotifications {

    private static final Logger logger = LoggerFactory.getLogger(ApolloNotifications.class);

    private static final int TIMEOUT_TERMINATION = 20;

    private static final int RETRIES = 3;
    private static final Duration SLEEP_BETWEEN_RETRIES = Duration.ofSeconds(1);

    private final ServiceDao serviceDao;
    private final EnvironmentDao environmentDao;
    private final NotificationDao notificationDao;

    private final BlockingQueue<NotificationTemplateMetadata> queue;
    private final ExecutorService executor;

    @Inject
    public ApolloNotifications(ServiceDao serviceDao, EnvironmentDao environmentDao, NotificationDao notificationDao) {
        this.serviceDao = requireNonNull(serviceDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.notificationDao = requireNonNull(notificationDao);

        queue = new LinkedBlockingQueue<>();
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this::processQueue);
    }

    public void notify(Deployment.DeploymentStatus status, Deployment deployment) {
        Environment environment = environmentDao.getEnvironment(deployment.getEnvironmentId());
        Service service = serviceDao.getService(deployment.getServiceId());

        notificationDao.getAllNotifications()
                .stream()
                .filter(notification -> isNotificationInScope(notification, deployment))
                .filter(notification -> filterNotificationByType(notification, status))
                .forEach(notification -> {
                    NotificationTemplateMetadata notificationTemplateMetadata = new NotificationTemplateMetadata(deployment.getLastUpdate(),
                            status.toString(), service.getName(), environment.getName(),
                            deployment.getUserEmail(), deployment.getId(), deployment.getGroupName(),
                            deployment.getDeploymentMessage(), notification.getType(),
                            notification.getNotificationJsonConfiguration());

                    queue.add(notificationTemplateMetadata);
                });
    }

    @PreDestroy
    private void close() {
        try {
            logger.info("Stopping apollo notifications thread");
            executor.shutdown();
            executor.awaitTermination(TIMEOUT_TERMINATION, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Got interrupt while waiting for orderly termination of the notification thread, force close.");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void processQueue() {
        while(true) {
            try {
                NotificationSender notificationSender;
                NotificationTemplateMetadata notificationTemplateMetadata = queue.take();
                switch (notificationTemplateMetadata.getType()) {
                    case SLACK:
                        notificationSender = new SlackSender(notificationTemplateMetadata.getNotificationJsonConfiguration());
                        break;
                    case MARKER:
                        notificationSender = new MarkersSender(notificationTemplateMetadata.getNotificationJsonConfiguration());
                        break;
                    default:
                        continue;
                }

                boolean success = false;
                for (int i = 0; i < RETRIES; i++) {
                    success = notificationSender.send(notificationTemplateMetadata);
                    if(success) break;
                    Thread.sleep(SLEEP_BETWEEN_RETRIES.toMillis());
                }
                if(!success) {
                    logger.warn("Failed to send a notification to {} after {} retries ", notificationTemplateMetadata.getType(), RETRIES);
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for an incoming notification", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (IOException e) {
                logger.warn("Got exception while sending or constructing notification sender class", e);
            }
        }
    }

    private boolean isNotificationInScope(Notification notification, Deployment deployment) {
        if (notification.getEnvironmentId() == null && notification.getServiceId() == null) {
            return true;
        }

        if (notification.getEnvironmentId() == null && notification.getServiceId().equals(deployment.getServiceId())) {
            return true;
        }

        if (notification.getServiceId() == null && notification.getEnvironmentId().equals(deployment.getEnvironmentId())) {
            return true;
        }

        if (notification.getEnvironmentId() != null && notification.getServiceId() != null) {
            if (notification.getEnvironmentId().equals(deployment.getEnvironmentId()) && notification.getServiceId().equals(deployment.getServiceId())) {
                return true;
            }
        }
        return false;
    }

    private boolean filterNotificationByType(Notification notification, Deployment.DeploymentStatus status) {
        if (notification.getType() == Notification.NotificationType.SLACK && status == Deployment.DeploymentStatus.STARTED) {
            return false;
        }

        if (notification.getType() == Notification.NotificationType.MARKER && status != Deployment.DeploymentStatus.STARTED) {
            return false;
        }

        return true;
    }
}
