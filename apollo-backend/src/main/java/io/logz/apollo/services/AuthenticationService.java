package io.logz.apollo.services;

import io.logz.apollo.dao.DeploymentPermissionDao;
import io.logz.apollo.dao.UserDao;
import io.logz.apollo.models.DeploymentPermission;
import io.logz.apollo.models.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.Objects.requireNonNull;

//Class in progress
@Singleton
public class AuthenticationService {

    private final DeploymentPermissionDao deploymentPermissionDao;
    private final UserDao userDao;

    @Inject
    public AuthenticationService(DeploymentPermissionDao deploymentPermissionDao, UserDao userDao) {
        this.deploymentPermissionDao = requireNonNull(deploymentPermissionDao);
        this.userDao = requireNonNull(userDao);
    }

    public User getUser(String userName) {
        return userDao.getUser(userName);
    }

    public List<DeploymentPermission> getPermissionsByUser(String userName) {
        return deploymentPermissionDao.getPermissionsByUser(userName);
    }
}
