package io.logz.apollo.controllers;

import io.logz.apollo.dao.DeploymentRoleDao;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.auth.PasswordManager;
import io.logz.apollo.models.DeploymentRole;
import io.logz.apollo.models.User;
import io.logz.apollo.common.HttpStatus;
import io.logz.apollo.dao.DeploymentPermissionDao;
import io.logz.apollo.dao.UserDao;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.annotation.PUT;
import org.rapidoid.http.Req;
import org.rapidoid.security.annotation.Administrator;
import org.rapidoid.security.annotation.LoggedIn;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.logz.apollo.common.ControllerCommon.assignJsonResponseToReq;
import static java.util.Objects.requireNonNull;

/**
 * Created by roiravhon on 11/21/16.
 */
@Controller
public class AuthController {

    private final DeploymentPermissionDao deploymentPermissionDao;
    private final DeploymentRoleDao deploymentRoleDao;
    private final UserDao userDao;

    @Inject
    public AuthController(DeploymentPermissionDao deploymentPermissionDao,
                          DeploymentRoleDao deploymentRoleDao,
                          UserDao userDao) {
        this.deploymentPermissionDao = requireNonNull(deploymentPermissionDao);
        this.deploymentRoleDao = requireNonNull(deploymentRoleDao);
        this.userDao = requireNonNull(userDao);
    }

    @LoggedIn
    @GET("/users")
    public List<User> getAllUsers() {
        return userDao.getAllUsers().stream()
                .map(this::maskPassword)
                .collect(Collectors.toList());
    }

    @LoggedIn
    @GET("/users/{email}")
    public User getUser(String email) { return maskPassword(userDao.getUser(email)); }

    @Administrator
    @POST("/signup")
    public User addUser(String userEmail, String firstName, String lastName, String password) {
        // TODO: validate input
        User newUser = new User();
        newUser.setUserEmail(userEmail);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setHashedPassword(PasswordManager.encryptPassword(password));
        newUser.setAdmin(false);
        newUser.setEnabled(true);
        userDao.addUser(newUser);

        return newUser;
    }

    @Administrator
    @PUT("/users")
    public User updateUser(String userEmail, String firstName, String lastName, String password, Boolean isAdmin, Boolean isEnabled) {
        User user = getUser(userEmail);
        user.setUserEmail(userEmail);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setHashedPassword(PasswordManager.encryptPassword(password));
        user.setAdmin(isAdmin);
        user.setEnabled(isEnabled);
        userDao.updateUser(user);

        return user;
    }

    @Administrator
    @GET("/deployment-roles")
    public List<DeploymentRole> getAllDeploymentRoles(Req req) {
        return deploymentRoleDao.getAllDeploymentRoles();
    }

    @Administrator
    @GET("/deployment-roles/{id}")
    public DeploymentRole getDeploymentRole(int id, Req req) {
        return deploymentRoleDao.getDeploymentRole(id);
    }

    @Administrator
    @POST("/deployment-roles")
    public void addDeploymentRole(String name, Req req) {
        DeploymentRole newDeploymentRole = new DeploymentRole();
        newDeploymentRole.setName(name);

        deploymentRoleDao.addDeploymentRole(newDeploymentRole);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newDeploymentRole);
    }

    @Administrator
    @GET("/deployment-permission")
    public List<DeploymentPermission> getAllDeploymentPermissions(Req req) {
        return deploymentPermissionDao.getAllDeploymentPermissions();
    }

    @Administrator
    @GET("/deployment-permission/{id}")
    public DeploymentPermission getDeploymentPermission(int id, Req req) {
        return deploymentPermissionDao.getDeploymentPermission(id);
    }

    @Administrator
    @POST("/deployment-permission")
    public void addDeploymentPermission(String name, String serviceId, String  environmentId, DeploymentPermission.PermissionType permissionType, Req req) {
        DeploymentPermission newDeploymentPermission = new DeploymentPermission();
        newDeploymentPermission.setName(name);

        // This is how rapidoid represent nulls. also has to be string to not have a NumberFormatException
        if (serviceId.equals("null") && environmentId.equals("null")) {
            req.response().code(HttpStatus.BAD_REQUEST);
            req.response().json("One of serviceId or environmentId must be present!");
        } else {
            if (!serviceId.equals("null")) {
                newDeploymentPermission.setServiceId(Integer.parseInt(serviceId));
            }
            if (!environmentId.equals("null")) {
                newDeploymentPermission.setEnvironmentId(Integer.parseInt(environmentId));
            }
        }

        newDeploymentPermission.setPermissionType(permissionType);

        deploymentPermissionDao.addDeploymentPermission(newDeploymentPermission);
        assignJsonResponseToReq(req, HttpStatus.CREATED, newDeploymentPermission);
    }

    @Administrator
    @POST("/deployment-roles/add-user")
    public void addUserToDeploymentRole(String userEmail, int deploymentRoleId, Req req) {
        deploymentRoleDao.addUserToDeploymentRole(userEmail, deploymentRoleId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, "ok");
    }

    @Administrator
    @POST("/deployment-roles/add-deployment-permission")
    public void addDeploymentPermissionToDeploymentRole(int deploymentRoleId, int deploymentPermissionId, Req req) {
        deploymentRoleDao.addDeploymentPermissionToDeploymentRole(deploymentRoleId, deploymentPermissionId);
        assignJsonResponseToReq(req, HttpStatus.CREATED, "ok");
    }

    private User maskPassword(User user) {
        user.setHashedPassword("******");
        return user;
    }

}
