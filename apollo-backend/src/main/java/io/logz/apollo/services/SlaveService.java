package io.logz.apollo.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.dao.SlaveDao;
import io.logz.apollo.models.Environment;
import io.logz.apollo.models.Slave;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;

@Singleton
public class SlaveService {
    private static final Logger logger = LoggerFactory.getLogger(SlaveService.class);

    private final String slaveId;
    private final ScheduledExecutorService keepaliveExecutorService;
    private final Boolean isSlave;
    private final List<Integer> environmentIds;
    private final ApolloConfiguration apolloConfiguration;
    private final SlaveDao slaveDao;
    private final EnvironmentDao environmentDao;

    private boolean started = false;

    @Inject
    public SlaveService(ApolloConfiguration apolloConfiguration, SlaveDao slaveDao, EnvironmentDao environmentDao) {
        this.slaveDao = requireNonNull(slaveDao);
        this.environmentDao = requireNonNull(environmentDao);
        this.apolloConfiguration = requireNonNull(apolloConfiguration);

        isSlave = apolloConfiguration.getSlave().isSlave();
        environmentIds = parseEnvironmentIds();

        if (isSlave && isEmpty(environmentIds)) {
            logger.error("Slave must be bundled with a valid list of environments! Bailing..");
            throw new RuntimeException("Could not understand slaves params");
        }

        slaveId = apolloConfiguration.getSlave().getSlaveId();
        keepaliveExecutorService = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder()
                                            .namingPattern("slave-keepalive-pinger")
                                            .build());
    }

    @PostConstruct
    public void start() {
        cleanupUnusedSlaves();

        if (isSlave) {
            claimSlaveEnvironments();
            keepaliveExecutorService.scheduleWithFixedDelay(this::keepAlive,
                    0, apolloConfiguration.getSlave().getKeepaliveIntervalSeconds(),
                    TimeUnit.SECONDS);

            started = true;
        }
    }

    @PreDestroy
    public void stop() {
        started = false;
        keepaliveExecutorService.shutdownNow();
        slaveDao.removeAllSlavesById(slaveId);
    }

    public Boolean isRunningInSlaveMode() {
        return isSlave;
    }

    public List<Integer> getEnvironmentIds() {
        return environmentIds;
    }

    public boolean isStarted() {
        return started;
    }

    @VisibleForTesting
    public List<Integer> getScopedEnvironments() {
        if (isSlave) {
            return getEnvironmentIds();
        } else { // I am the master, need all unattended environments
            List<Integer> ownedEnvironments = slaveDao.getAllSlaves().stream()
                                                      .filter(slave -> slave.getSecondsSinceLastKeepalive()
                                                              <= apolloConfiguration.getSlave().getKeepaliveIntervalSeconds() * 2)
                                                      .map(Slave::getEnvironmentId)
                                                      .collect(Collectors.toList());
            return environmentDao.getAllEnvironments()
                                                  .stream()
                                                  .map(Environment::getId)
                                                  .filter(id -> !ownedEnvironments.contains(id))
                                                  .collect(Collectors.toList());
        }
    }

    private void claimSlaveEnvironments() {
        environmentIds.forEach(environmentId -> {
            Slave slave = new Slave();
            slave.setSlaveId(slaveId);
            slave.setEnvironmentId(environmentId);
            slave.setLastKeepalive(new Date());
            slaveDao.addSlave(slave);
        });
    }

    private void keepAlive() {
        slaveDao.keepalive(slaveId, new Date());
    }

    private List<Integer> parseEnvironmentIds() {
        try {
            String envVar = apolloConfiguration.getSlave().getSlaveCsvEnvironments();
            if (envVar == null) {
                return null;
            }

            return Splitter.on(",").trimResults()
                    .splitToList(envVar)
                    .stream().map(Integer::parseInt)
                    .collect(Collectors.toList());

        } catch (NumberFormatException e) {
            logger.error("Could not parse int list from {}", apolloConfiguration.getSlave().getSlaveCsvEnvironments());
            return null;
        }
    }

    private void cleanupUnusedSlaves() {
        slaveDao.getAllSlaves().stream().filter(slave -> slave.getSecondsSinceLastKeepalive() >=
                apolloConfiguration.getSlave().getKeepaliveIntervalSeconds() * 4)
                .forEach(slave -> slaveDao.removeEnvironmentFromSlave(slave.getSlaveId(), slave.getEnvironmentId()));
    }
}
