package io.logz.apollo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by roiravhon on 1/17/17.
 */
@Singleton
public class LockService {

    private static final Logger logger = LoggerFactory.getLogger(LockService.class);
    private Map<String, Boolean> locks;

    public LockService() {
        locks = new ConcurrentHashMap<>();
    }

    public synchronized boolean getAndObtainLock(String lockName) {
        Boolean lockStatus = locks.get(lockName);
        if (lockStatus == null || !lockStatus) {
            logger.debug("Got request to lock for key {}, granted.", lockName);
            locks.put(lockName, true);
            return true;

        } else {
            logger.debug("Got request to lock for key {}, denied.", lockName);
            return false;
        }
    }

    public synchronized void releaseLock(String lockName) {
        Boolean lockStatus = locks.get(lockName);
        if (lockStatus == null) {
            logger.debug("Got request to release a lock for key {}, but its not set. Ignoring..", lockName);
        } else {
            logger.debug("Got request to release a lock for key {}. released.", lockName);
            locks.put(lockName, false);
        }
    }

    public String getDeploymentLockName(int serviceId, String availability) {
        return "lock-service-" + serviceId + "-availability-" + availability;
    }

    public String getDeploymentLockName(int serviceId, int environmentId) {
        return "lock-service-" + serviceId + "-environment-" + environmentId;
    }

    public String getDeploymentCancelationName(int deploymentId) {
        return "lock-deployment-cancelation-id-" + deploymentId;
    }
}
