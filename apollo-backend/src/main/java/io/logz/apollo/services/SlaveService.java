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
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isEmpty;

@Singleton
public class SlaveService {
    private static final Logger logger = LoggerFactory.getLogger(SlaveService.class);

    private final String slaveId;
    private final ScheduledExecutorService keepaliveExecutorService;
    private final Boolean isSlave;
    private final Set<Integer> environmentIds;
    private final ApolloConfiguration apolloConfiguration;
    private final SlaveDao slaveDao;
    private final EnvironmentDao environmentDao;

    private AtomicBoolean isStarted = new AtomicBoolean(false);

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
        logger.info("Starting slave.. slaveId - {}, environmentIds - {}", slaveId, environmentIds);

        cleanupUnusedSlaves();

        if (isSlave && isStarted.compareAndSet(false, true)) {
            claimSlaveEnvironments();
            keepaliveExecutorService.scheduleWithFixedDelay(this::keepAlive,
                    0, apolloConfiguration.getSlave().getKeepaliveIntervalSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void stop() {
        if (isStarted.compareAndSet(true, false)) {
            keepaliveExecutorService.shutdownNow();
            slaveDao.removeAllSlavesById(slaveId);
        }
    }

    public boolean isRunningInSlaveMode() {
        return isSlave;
    }

    public Set<Integer> getEnvironmentIds() {
        return environmentIds;
    }

    public boolean getIsStarted() {
        return isStarted.get();
    }

    @VisibleForTesting
    public Set<Integer> getScopedEnvironments() {
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
                                                  .collect(Collectors.toSet());
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

    private Set<Integer> parseEnvironmentIds() {
        try {
            String envVar = apolloConfiguration.getSlave().getSlaveCsvEnvironments();
            if (envVar == null) {
                return null;
            }

            return Splitter.on(",").trimResults()
                    .splitToList(envVar)
                    .stream().map(Integer::parseInt)
                    .collect(Collectors.toSet());

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
