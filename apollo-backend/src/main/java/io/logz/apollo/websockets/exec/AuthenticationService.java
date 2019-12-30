package io.logz.apollo.websockets.exec;

import io.logz.apollo.dao.DeploymentPermissionDao;
import io.logz.apollo.dao.UserDao;
import io.logz.apollo.models.DeploymentPermission;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Singleton
public class AuthenticationService {

    private final DeploymentPermissionDao deploymentPermissionDao;
    private final UserDao userDao;

    @Inject
    public AuthenticationService(DeploymentPermissionDao deploymentPermissionDao, UserDao userDao) {
        this.deploymentPermissionDao = requireNonNull(deploymentPermissionDao);
        this.userDao = requireNonNull(userDao);
    }

    public boolean isAdmin(String userName) {
        return userDao.getUser(userName).isAdmin();
    }

    public boolean isExecAllowed(String userName) {
        return userDao.getUser(userName).isExecAllowed();
    }

    public List<DeploymentPermission> getPermissionsByUser(String userName) {
        return deploymentPermissionDao.getPermissionsByUser(userName);
    }
}
