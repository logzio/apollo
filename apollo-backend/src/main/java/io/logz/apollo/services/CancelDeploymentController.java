package io.logz.apollo.services;

import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.DeploymentDao;
import io.logz.apollo.models.Deployment;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

@Controller
public class CancelDeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentService.class);
    private final DeploymentDao deploymentDao;

    @Inject
    public CancelDeploymentController(DeploymentDao deploymentDao) {
        this.deploymentDao = requireNonNull(deploymentDao);
    }

    @LoggedIn
    @GET("/should-cancel-deployment/{id}/time-unit/{timeUnit}/timeout/{timeout}")
    public void isDeploymentShouldBeCanceled(int id, String timeUnit, Integer timeout, Req req) {
        TimeUnit timeUnitEnum;
        try {
            timeUnitEnum = TimeUnit.valueOf(timeUnit);
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.error("Couldn't parse the time unit - {}, set time unit to be MINUTES by default.", timeUnit, e);
            timeUnitEnum = TimeUnit.MINUTES;
        }
        Deployment deployment = deploymentDao.getDeployment(id);
        switch (deployment.getStatus()) {
            case PENDING:
            case STARTED:
            case CANCELING:
            case PENDING_CANCELLATION:
                if (isDeploymentTimeoutPassed(deployment, timeUnitEnum, timeout)) {
                    logger.info("Deployment {} is timed out, got stuck on {} status.", deployment.getId(), deployment.getStatus());
                    assignJsonResponseToReq(req, HttpStatus.OK, true);
                    return;
                }
        }
        assignJsonResponseToReq(req, HttpStatus.OK, false);
    }

    @LoggedIn
    @PUT("/cancel-deployment")
    public void cancelDeployment(Integer id, Req req) {
        logger.info("Canceling deployment {}", id);
        deploymentDao.updateDeploymentStatus(id, Deployment.DeploymentStatus.CANCELED);
        assignJsonResponseToReq(req, HttpStatus.OK, deploymentDao.getDeployment(id));
    }

    private boolean isDeploymentTimeoutPassed(Deployment deployment, TimeUnit timeUnit, Integer timeout) {
        timeout = (timeout==null) ? 30 : timeout;
        timeUnit = (timeUnit==null) ? TimeUnit.MINUTES : timeUnit;
        return getTimeDiffFromNowInMinutes(deployment.getLastUpdate(), timeUnit) >= timeout;
    }

    private long getTimeDiffFromNowInMinutes(Date date, TimeUnit timeUnit) {
        return timeUnit
                .convert(Duration.between(date.toInstant().atZone(ZoneId.of("UTC")),
                        (new Date()).toInstant().atZone(ZoneId.of("UTC"))).toMillis(),
                        TimeUnit.MILLISECONDS);
    }
}
