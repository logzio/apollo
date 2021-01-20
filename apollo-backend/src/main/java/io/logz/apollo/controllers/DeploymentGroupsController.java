package io.logz.apollo.controllers;

import com.google.common.base.Splitter;
import io.logz.apollo.deployment.DeploymentHandler;
import io.logz.apollo.models.MultiDeploymentResponseObject;
import io.logz.apollo.excpetions.ApolloDeploymentException;
import io.logz.apollo.models.Deployment;
import io.logz.apollo.models.Group;
import io.logz.apollo.dao.GroupDao;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.LoggedIn;
import javax.inject.Inject;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;
import io.logz.apollo.common.HttpStatus;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class DeploymentGroupsController {

    private final DeploymentHandler deploymentHandler;
    private final GroupDao groupDao;
    private final static String GROUP_IDS_DELIMITER = ",";

    @Inject
    public DeploymentGroupsController(DeploymentHandler deploymentHandler, GroupDao groupDao) {
        this.deploymentHandler = requireNonNull(deploymentHandler);
        this.groupDao = requireNonNull(groupDao);
    }

    @LoggedIn
    @POST("/deployment-groups")
    public void addDeployment(int environmentId, int serviceId, int deployableVersionId, String groupIdsCsv, String deploymentMessage, Boolean isEmergencyDeployment, Req req) throws NumberFormatException {

        MultiDeploymentResponseObject responseObject = new MultiDeploymentResponseObject();

        try {
            deploymentHandler.checkDeploymentShouldBeBlockedByRequestBlocker(new ArrayList<Integer>() {{ add(serviceId); }}, new ArrayList<Integer>() {{ add(environmentId); }});
        } catch (ApolloDeploymentException e) {
            responseObject.addUnsuccessful(e);
        }

        Iterable<String> groupIds = Splitter.on(GROUP_IDS_DELIMITER).omitEmptyStrings().trimResults().split(groupIdsCsv);

        for (String groupIdString : groupIds) {
            int groupId = Integer.parseInt(groupIdString);
            Group group = groupDao.getGroup(groupId);

            if (group == null) {
                responseObject.addUnsuccessful(groupId, new ApolloDeploymentException("Non existing group."));
                continue;
            }

            if (group.getServiceId() != serviceId) {
                responseObject.addUnsuccessful(groupId, new ApolloDeploymentException("The deployment service ID " + serviceId + " doesn't match the group service ID " + group.getServiceId()));
                continue;
            }

            if (group.getEnvironmentId() != environmentId) {
                responseObject.addUnsuccessful(groupId, new ApolloDeploymentException("The deployment environment ID " + environmentId + " doesn't match the group environment ID " + group.getEnvironmentId()));
                continue;
            }

            try {
                Deployment deployment = deploymentHandler.addDeployment(environmentId, serviceId, deployableVersionId,
                        deploymentMessage, group.getName(), Optional.of(group), isEmergencyDeployment, req);
                responseObject.addSuccessful(groupId, deployment);
            } catch (ApolloDeploymentException e) {
                responseObject.addUnsuccessful(groupId, e);
            }
        }
        assignJsonResponseToReq(req, HttpStatus.CREATED, responseObject);
    }
}
