'use strict';
angular
    .module('apollo')
    .service('apolloApiService', [
        '$q', '$http',
        ApiService
    ]);


function ApiService($q, $http){

    var getAllUsers = function() {
        return $http.get(CONFIG.appUrl + 'users/');
    };

    var getAllEnvironments = function() {
        return $http.get(CONFIG.appUrl + 'environment/');
    };

    var getAllServices = function() {
        return $http.get(CONFIG.appUrl + 'service/');
    };

    var getAllDeployableVersions = function() {
        return $http.get(CONFIG.appUrl + 'deployable-version/');
    };

    var getAllGroups = function() {
        return $http.get(CONFIG.appUrl + 'group/');
    };

    var getGroupsPerServiceAndEnvironment = function (environmentId, serviceId) {
        return $http.get(CONFIG.appUrl + 'group/environment/' + environmentId + '/service/' + serviceId);
    };

    var getDeployableVersionBasedOnSha = function (sha, serviceId) {
        return $http.get(CONFIG.appUrl + 'deployable-version/sha/' + sha + '/service/' + serviceId);
    };

    var getDeployableVersionForMultiServices = function (serviceIdsCsv) {
        return $http.get(CONFIG.appUrl + 'deployable-version/multi-service/' + serviceIdsCsv);
    };

    var getAllRunningDeployments = function() {
        return $http.get(CONFIG.appUrl + 'running-deployments/');
    };

    var getRunningAndJustFinishedDeployments = function() {
        return $http.get(CONFIG.appUrl + 'running-and-just-finished-deployments/');
    };

    var getAllDeployments = function() {
        return $http.get(CONFIG.appUrl + 'deployment/');
    };

    var getDeployment = function(deploymentId) {
        return $http.get(CONFIG.appUrl + 'deployment/' + deploymentId + '/');
    };

    var getService = function(serviceId) {
        return $http.get(CONFIG.appUrl + 'service/' + serviceId + "/");
    };

    var getEnvironment = function(environmentId) {
        return $http.get(CONFIG.appUrl + 'environment/' + environmentId + "/");
    };

    var getUser = function(userId) {
        return $http.get(CONFIG.appUrl + 'user/' + userId + "/");
    };

    var getDeployableVersion = function(deployableVerisonId) {
        return $http.get(CONFIG.appUrl + 'deployable-version/' + deployableVerisonId + "/");
    };

    var getGroup = function(groupId) {
        return $http.get(CONFIG.appUrl + 'group/' + groupId + "/");
    };

    var getGroupByName = function(groupName) {
        return $http.get(CONFIG.appUrl + 'group/name/' + groupName + "/");
    };

    var getDeploymentEnvStatus = function (deploymentId) {
        return $http.get(CONFIG.appUrl + 'deployment/' + deploymentId + '/envstatus');
    };

    var getLatestDeployments = function() {
        return $http.get(CONFIG.appUrl + 'latest-deployments/');
    };

    var createNewDeployment = function(deployableVersionId, deployedService, deployedEnvironment, deploymentMessage) {
        return $http.post(CONFIG.appUrl + "deployment/", {
            serviceIdsCsv: deployedService,
            environmentIdsCsv: deployedEnvironment,
            deployableVersionId: deployableVersionId,
            deploymentMessage: deploymentMessage
        });
    };

    var createNewDeploymentWithGroup = function(deployableVersionId, deployedService, deployedEnvironment,
                                                deploymentMessage, groupIdsCsv) {
        return $http.post(CONFIG.appUrl + "deployment-groups/", {
            serviceId: deployedService,
            environmentId: deployedEnvironment,
            deployableVersionId: deployableVersionId,
            deploymentMessage: deploymentMessage,
            groupIdsCsv: groupIdsCsv
        });
    };

    var revertDeployment = function(deploymentId) {
        return $http.delete(CONFIG.appUrl + "deployment/" + deploymentId + "/");
    };

    var matchLabelToDeploymentStatus = function(deploymentStatus) {
        var statusToLabel = {
            "PENDING": "label-default",
            "PENDING_CANCELLATION": "label-default",
            "STARTED": "label-primary",
            "CANCELING": "label-warning",
            "DONE": "label-success",
            "CANCELED": "label-danger"
        };

        return statusToLabel[deploymentStatus];
    };

    var isRevertDisabledBasedOnStatus = function(deploymentStatus) {
        return deploymentStatus === "DONE" || deploymentStatus === "CANCELED";
    };

    var signup = function(userEmail, firstName, lastName, password) {
        return $http.post(CONFIG.appUrl + "signup/", {
            userEmail: userEmail,
            firstName: firstName,
            lastName: lastName,
            password: password
        })
    };

    var login = function(email, password) {
        return $http.post(CONFIG.appUrl + "_login/", {
            username: email,
            password: password
        });
    };

    var serviceStatus = function (serviceId) {
        return $http.get(CONFIG.appUrl + "status/service/" + serviceId + "/");
    };

    var environmentStatus = function (environmentId) {
        return $http.get(CONFIG.appUrl + "status/environment/" + environmentId + "/");
    };

    var latestCreatedPod = function (environmentId, serviceId) {
        return $http.get(CONFIG.appUrl + "status/environment/" + environmentId + "/service/" + serviceId + "/latestpod");
    };

    var latestCreatedPodWithGroup = function (environmentId, serviceId, groupName) {
        return $http.get(CONFIG.appUrl + "status/environment/" + environmentId + "/service/" + serviceId + "/group/" + groupName + "/latestpod");
    };

    var podContainers = function (environmentId, podName) {
        return $http.get(CONFIG.appUrl + "status/environment/" + environmentId + "/pod/" + podName + "/containers");
    };

    var restartPod = function (environmentId, podName) {
        return $http.post(CONFIG.appUrl + "k8s/pod/restart", {
            environmentId: environmentId,
            podName: podName
        });
    };

    var restartAllPods = function (environmentId, serviceId, groupName) {
        return $http.post(CONFIG.appUrl + "k8s/pod/restart-all", {
            environmentId: environmentId,
            serviceId: serviceId,
            groupName: groupName
        });
    };

    var createService = function(name, deploymentYaml, serviceYaml, ingressYaml, isPartOfGroup) {
        return $http.post(CONFIG.appUrl + "service/", {
            name: name,
            deploymentYaml: deploymentYaml,
            serviceYaml: serviceYaml,
            ingressYaml: ingressYaml,
            isPartOfGroup: !!isPartOfGroup
        });
    };

    var updateService = function(id, name, deploymentYaml, serviceYaml, ingressYaml, isPartOfGroup) {
        return $http.put(CONFIG.appUrl + "service/" + id, {
            name: name,
            deploymentYaml: deploymentYaml,
            serviceYaml: serviceYaml,
            ingressYaml: ingressYaml,
            isPartOfGroup: !!isPartOfGroup
        });
    };

    var createGroup = function(name, serviceId, environmentId, scalingFactor, jsonParams) {
        return $http.post(CONFIG.appUrl + "group/", {
            name: name,
            serviceId: serviceId,
            environmentId: environmentId,
            scalingFactor: scalingFactor,
            jsonParams: jsonParams
        });
    };

    var updateGroup = function(id, name, serviceId, environmentId, scalingFactor, jsonParams) {
        return $http.put(CONFIG.appUrl + "group/" + id, {
            name: name,
            serviceId: serviceId,
            environmentId: environmentId,
            scalingFactor: scalingFactor,
            jsonParams: jsonParams
        });
    };

    var deleteGroup = function(id) {
        return $http.delete(CONFIG.appUrl + "group/" + id);
    }

    var updateScalingFactorForGroup = function (groupId, scalingFactor) {
        return $http.put(CONFIG.appUrl + "scaling/" + groupId, {
           scalingFactor: scalingFactor
        });
    };

    var getApolloScalingFactorForGroup = function (groupId) {
        return $http.get(CONFIG.appUrl + "scaling/apollo-factor/" + groupId);
    };

    var getK8sScalingFactorForGroup = function (groupId) {
        return $http.get(CONFIG.appUrl + "scaling/kubernetes-factor/" + groupId);
    };

    var getLatestDeployableVersionsByServiceId = function (serviceId) {
        return $http.get(CONFIG.appUrl + "deployable-version/latest/service/" + serviceId);
    };

    var getDeployableVersionFromLatestCommitOnBranch = function (branchName, sourceDeployableVersion) {
        // Double encoding, as nginx is opening the first one
        return $http.get(CONFIG.appUrl + "deployable-version/latest/branch/" + encodeURIComponent(encodeURIComponent(branchName)) + "/repofrom/" + sourceDeployableVersion);
    };
    
    var getWebsocketExecUrl = function (environment, service, podName, containerName) {
      return CONFIG.wsUrl + "exec/pod/" + podName + "/container/" + containerName + "?environment=" + environment + "&service=" + service;
    };

    var getWebsocketLogUrl = function (environment, service, podName, containerName) {
      return CONFIG.wsUrl + "logs/pod/" + podName + "/container/" + containerName + "?environment=" + environment;
    };

    var getAllBlockers = function () {
        return $http.get(CONFIG.appUrl + "blocker-definition");
    };
    
    var addBlocker = function (name, environmentId, serviceId, isActive, blockerTypeName, blockerJsonConfiguration) {
        return $http.post(CONFIG.appUrl + "blocker-definition", {
            name: name,
            environmentId: environmentId,
            serviceId: serviceId,
            isActive: isActive,
            blockerTypeName: blockerTypeName,
            blockerJsonConfiguration: blockerJsonConfiguration
        });
    };

    var updateBlocker = function (id, name, environmentId, serviceId, isActive, blockerTypeName, blockerJsonConfiguration) {
        return $http.put(CONFIG.appUrl + "blocker-definition/" + id, {
            name: name,
            environmentId: environmentId,
            serviceId: serviceId,
            isActive: isActive,
            blockerTypeName: blockerTypeName,
            blockerJsonConfiguration: blockerJsonConfiguration
        });
    };

    var deleteBlocker = function (id) {
        return $http.delete(CONFIG.appUrl + "blocker-definition/" + id);
    };

    var getHawtioLink = function (environmentId, podName) {
        var port = document.location.port;
        var host = document.location.hostname;
        var scheme = document.location.protocol.split(":")[0];
        var prefix = "";
        if (port === "") {
            // If running on nginx
            prefix = "api/";
            if (document.location.protocol === "https:") {
                port = "443";
            } else {
                port = "80";
            }
        }

        return CONFIG.hawtioUrl + "jvm/connect?name=" + podName +"&host=" + host + "&port=" + port + "&scheme="+ scheme + "&path=" + prefix + "jolokia/environment/" + environmentId + "/pod/" + podName;
    };

    return {
        getAllUsers: getAllUsers,
        getAllEnvironments: getAllEnvironments,
        getAllServices: getAllServices,
        getAllDeployableVersions: getAllDeployableVersions,
        getAllGroups: getAllGroups,
        getGroupsPerServiceAndEnvironment: getGroupsPerServiceAndEnvironment,
        getDeployableVersionBasedOnSha: getDeployableVersionBasedOnSha,
        getDeployableVersionForMultiServices : getDeployableVersionForMultiServices,
        getLatestDeployableVersionsByServiceId: getLatestDeployableVersionsByServiceId,
        createNewDeployment: createNewDeployment,
        createNewDeploymentWithGroup: createNewDeploymentWithGroup,
        getAllRunningDeployments: getAllRunningDeployments,
        getRunningAndJustFinishedDeployments: getRunningAndJustFinishedDeployments,
        getAllDeployments: getAllDeployments,
        getDeployment: getDeployment,
        getService: getService,
        getEnvironment: getEnvironment,
        getUser: getUser,
        getDeployableVersion: getDeployableVersion,
        getGroup: getGroup,
        getGroupByName: getGroupByName,
        getDeploymentEnvStatus: getDeploymentEnvStatus,
        getLatestDeployments: getLatestDeployments,
        revertDeployment: revertDeployment,
        matchLabelToDeploymentStatus: matchLabelToDeploymentStatus,
        isRevertDisabledBasedOnStatus: isRevertDisabledBasedOnStatus,
        signup: signup,
        login: login,
        serviceStatus: serviceStatus,
        environmentStatus: environmentStatus,
        latestCreatedPod: latestCreatedPod,
        latestCreatedPodWithGroup: latestCreatedPodWithGroup,
        podContainers: podContainers,
        restartPod: restartPod,
        restartAllPods: restartAllPods,
        createService: createService,
        updateService: updateService,
        createGroup: createGroup,
        updateGroup: updateGroup,
        deleteGroup: deleteGroup,
        updateScalingFactorForGroup: updateScalingFactorForGroup,
        getApolloScalingFactorForGroup: getApolloScalingFactorForGroup,
        getK8sScalingFactorForGroup: getK8sScalingFactorForGroup,
        getDeployableVersionFromLatestCommitOnBranch: getDeployableVersionFromLatestCommitOnBranch,
        getWebsocketExecUrl: getWebsocketExecUrl,
        getWebsocketLogUrl: getWebsocketLogUrl,
        getAllBlockers: getAllBlockers,
        addBlocker: addBlocker,
        updateBlocker: updateBlocker,
        deleteBlocker: deleteBlocker,
        getHawtioLink: getHawtioLink
    };
}
