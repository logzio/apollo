package io.logz.apollo.services;

import com.google.common.base.Splitter;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.dao.SlaveDao;
import io.logz.apollo.models.Slave;
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

@Singleton
public class SlaveService {
    private static final Logger logger = LoggerFactory.getLogger(SlaveService.class);

    public static final String SLAVE_PROPERTY = "slave";
    public static final String SLAVE_CSV_ENVIRONMENTS = "environments";
    private final String slaveId;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Boolean isSlave;
    private final List<Integer> environmentIds;
    private final ApolloConfiguration apolloConfiguration;
    private final SlaveDao slaveDao;

    private boolean started = false;

    @Inject
    public SlaveService(ApolloConfiguration apolloConfiguration, SlaveDao slaveDao) {
        this.slaveDao = requireNonNull(slaveDao);
        this.apolloConfiguration = requireNonNull(apolloConfiguration);

        isSlave = Boolean.parseBoolean(System.getenv(SLAVE_PROPERTY));
        environmentIds = parseEnvironmentIds();

        if (isSlave && environmentIds == null) {
            logger.error("Slave must be bundled with a valid list of environments! Bailing..");
            throw new RuntimeException("Could not understand slaves params");
        }

        slaveId = UUID.randomUUID().toString();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void start() {
        cleanupUnusedSlaves();

        if (isSlave) {
            claimSlave();
            scheduledExecutorService.scheduleWithFixedDelay(this::keepAlive,
                    0, apolloConfiguration.getSlave().getKeepaliveIntervalSeconds(),
                    TimeUnit.SECONDS);

            started = true;
        }
    }

    @PreDestroy
    public void stop() {
        started = false;
        scheduledExecutorService.shutdownNow();
        slaveDao.getAllSlaves()
                .stream()
                .filter(slave -> slave.getSlaveId().equals(slaveId))
                .findFirst()
                .ifPresent(slave -> slaveDao.removeAllSlavesById(slave.getSlaveId()));
    }

    public Boolean getSlave() {
        return isSlave;
    }

    public List<Integer> getEnvironmentIds() {
        return environmentIds;
    }

    public List<Integer> getAllValidSlavesEnvironmentIds() {
        return slaveDao.getAllSlaves().stream()
                .filter(slave -> slave.getSecondsSinceLastKeepalive()
                        >= apolloConfiguration.getSlave().getKeepaliveIntervalSeconds() * 2)
                .map(Slave::getEnvironmentId)
                .collect(Collectors.toList());
    }

    public boolean isStarted() {
        return started;
    }

    private void claimSlave() {
        environmentIds.forEach(environmentId -> {
            Slave slave = new Slave();
            slave.setSlaveId(slaveId);
            slave.setEnvironmentId(environmentId);
            slave.setLastKeepalive(new Date());
            slaveDao.addSlave(slave);
        });
    }

    private void keepAlive() {
        slaveDao.getAllSlaves()
                .stream()
                .filter(slave -> slave.getSlaveId().equals(slaveId))
                .forEach(slave -> slaveDao.keepalive(slave.getSlaveId()));
    }

    private List<Integer> parseEnvironmentIds() {
        try {
            String envVar = System.getenv(SLAVE_CSV_ENVIRONMENTS);
            if (envVar == null) {
                return null;
            }

            return Splitter.on(",").trimResults()
                    .splitToList(envVar)
                    .stream().map(Integer::parseInt)
                    .collect(Collectors.toList());

        } catch (NumberFormatException e) {
            logger.error("Could not parse int list from {}", SLAVE_CSV_ENVIRONMENTS);
            return null;
        }
    }

    private void cleanupUnusedSlaves() {
        slaveDao.getAllSlaves().stream().filter(slave -> slave.getSecondsSinceLastKeepalive() >=
                apolloConfiguration.getSlave().getKeepaliveIntervalSeconds() * 4)
                .forEach(slave -> slaveDao.removeAllSlavesById(slave.getSlaveId()));
    }
}
