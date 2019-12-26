package io.logz.apollo.auth;

import io.logz.apollo.models.DeploymentPermission;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by roiravhon on 1/12/17.
 */
public class PermissionsValidator {

    public static boolean isAllowedToOpenExec(int serviceId, int environmentId, List<DeploymentPermission> userDeploymentPermissions, boolean isAdmin, boolean isExecAllowed) {

        boolean isAllowedToDeploy = isAllowedToDeploy(serviceId, environmentId, userDeploymentPermissions);

        return isAllowedToDeploy && (isAdmin || isExecAllowed);
    }


    public static boolean isAllowedToDeploy(int serviceId, int environmentId, List<DeploymentPermission> userDeploymentPermissions) {

        boolean isAllowed = false;

        // Check if there are wildcard permissions, either on service or environment (with the other one null)
        Optional<Boolean> isAllowedOnWildcard = isAllowedWildcard(serviceId, environmentId, userDeploymentPermissions);

        // Check if there are specific permissions for service + environment pair
        Optional<Boolean> isAllowedOnSpecific = isAllowedSpecific(serviceId, environmentId, userDeploymentPermissions);

        if (isAllowedOnWildcard.isPresent()) {
            isAllowed = isAllowedOnWildcard.get();
        }

        if (isAllowedOnSpecific.isPresent()) {
            isAllowed = isAllowedOnSpecific.get();
        }

        return isAllowed;
    }

    private static Optional<Boolean> isAllowedWildcard(int serviceId, int environmentId, List<DeploymentPermission> userDeploymentPermissions) {
        return userDeploymentPermissions
                .stream()
                .filter(deploymentPermission -> (Objects.equals(deploymentPermission.getServiceId(), serviceId) &&
                                                    Objects.equals(deploymentPermission.getEnvironmentId(), null)) ||
                                                (Objects.equals(deploymentPermission.getServiceId(), null) &&
                                                    Objects.equals(deploymentPermission.getEnvironmentId(), environmentId)))
                .sorted(PermissionsValidator::sortPermissionsByAllowDeny)
                .map(deploymentPermission -> deploymentPermission.getPermissionType() == DeploymentPermission.PermissionType.ALLOW)
                .reduce((result, permission) -> permission);
    }

    private static Optional<Boolean> isAllowedSpecific(int serviceId, int environmentId, List<DeploymentPermission> userDeploymentPermissions) {
        return userDeploymentPermissions
                .stream()
                .filter(deploymentPermission -> Objects.equals(deploymentPermission.getServiceId(), serviceId) &&
                                                Objects.equals(deploymentPermission.getEnvironmentId(), environmentId))
                .sorted(PermissionsValidator::sortPermissionsByAllowDeny)
                .map(deploymentPermission -> deploymentPermission.getPermissionType() == DeploymentPermission.PermissionType.ALLOW)
                .reduce((permission, result) -> permission);
    }

    // Deny permissions are always stronger than the allow ones, so this sort return the allow first, and the deny after
    public static int sortPermissionsByAllowDeny(DeploymentPermission deploymentPermission1, DeploymentPermission deploymentPermission2) {
        if (deploymentPermission1.getPermissionType().equals(deploymentPermission2.getPermissionType())) {
            return 0;
        } else if (deploymentPermission1.getPermissionType().equals(DeploymentPermission.PermissionType.ALLOW)) {
            return -1;
        } else {
            return 1;
        }
    }
}
