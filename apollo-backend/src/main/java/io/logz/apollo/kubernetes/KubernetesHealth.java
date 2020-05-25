package io.logz.apollo.kubernetes;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.dao.EnvironmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class KubernetesHealth {

    private Map<Integer, Boolean> environmentsHealthMap = new HashMap<>();

    private static final int TIMEOUT_TERMINATION = 60;
    public static final String LOCAL_RUN_PROPERTY = "localrun";

    private static final Logger logger = LoggerFactory.getLogger(KubernetesMonitor.class);
    private final ScheduledExecutorService scheduledExecutorService;
    private final KubernetesHandlerStore kubernetesHandlerStore;
    private final ApolloConfiguration apolloConfiguration;
    private final EnvironmentDao environmentDao;

    @Inject
    public KubernetesHealth(ApolloConfiguration apolloConfiguration, EnvironmentDao environmentDao, KubernetesHandlerStore kubernetesHandlerStore) {
        this.kubernetesHandlerStore = requireNonNull(kubernetesHandlerStore);
        this.apolloConfiguration = requireNonNull(apolloConfiguration);
        this.environmentDao = requireNonNull(environmentDao);

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("kubernetes-health-%d").build();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
    }

    @PostConstruct
    public void start() {
        try {
            if (isLocalRun()) {
                logger.info("Running in local-mode, kubernetes health thread is not up.");
                return;
            }
            logger.info("Starting kubernetes health thread");
            int healthThreadFrequency = apolloConfiguration.getKubernetes().getHealthFrequencySeconds();
            scheduledExecutorService.scheduleWithFixedDelay(this::monitorHealth, 0, healthThreadFrequency, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Could not start kubernetes health thread! Bailing..", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (isLocalRun()) return;
        try {
            logger.info("Stopping kubernetes health thread");
            scheduledExecutorService.shutdown();
            scheduledExecutorService.awaitTermination(TIMEOUT_TERMINATION, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Got interrupt while waiting for ordinarily termination of the health thread, force close.");
            Thread.currentThread().interrupt();
        } finally {
            scheduledExecutorService.shutdownNow();
        }
    }

    private void monitorHealth() {
        environmentDao.getAllActiveEnvironments().forEach(environment -> {
            if(environment.getRequireHealthCheck()) {
                KubernetesHandler kubernetesHandler = kubernetesHandlerStore.getOrCreateKubernetesHandler(environment);
                environmentsHealthMap.put(environment.getId(), kubernetesHandler.isEnvironmentHealthy());
            }
        });
    }

    public Map<Integer, Boolean> getEnvironmentsHealthMap() {
        return environmentsHealthMap;
    }

    private boolean isLocalRun() {
        return Boolean.valueOf(System.getenv(LOCAL_RUN_PROPERTY)) || Boolean.valueOf(System.getProperty(LOCAL_RUN_PROPERTY));
    }
}