'use strict';

angular.module('apollo')
  .controller('serviceVersionStatusCtrl', ['apolloApiService', '$scope',
                                    '$timeout', '$state', '$interval', '$window','growl', 'usSpinnerService',
            function (apolloApiService, $scope, $timeout, $state, $interval, $window, growl, usSpinnerService) {

            var execTypeName = "exec";
            var logsTypeName = "logs";
            var errorMessage = "An error occurred in getting k8s status";

            $scope.currentScreen = "selectServiceAndEnvironment";
            $scope.selectedService = null;
            $scope.selectedEnvironment = null;
            $scope.selectedGroup = null;
            $scope.serviceGroups = null;
            $scope.kubernetesDeploymentStatus = [];
            $scope.selectedPodStatus = null;

            $scope.setSelectedGroup = function(group) {
                $scope.selectedGroup = group;
            }

            $scope.showAllGroupsOfService = function() {
                if ($scope.selectedService != null && $scope.selectedEnvironment != null) { //selected service and environment
                    if ($scope.selectedService.isPartOfGroup) { //service is part of group
                        apolloApiService.allGroupsStatusesOfEnvironmentAndService($scope.selectedEnvironment.id, $scope.selectedService.id).then(function (response) {
                            $scope.kubernetesDeploymentStatus = response.data;
                            $scope.currentScreen = "results";
                            }, function (error) {
                                growl.error(errorMessage, {ttl:7000});
                        });
                    } else { //some unknown error
                        growl.error("Something went wrong... Please try again", {ttl:7000});
                    }
                } else { //some unknown error
                    growl.error("Something went wrong... Please try again", {ttl:7000});
                }
            }

            $scope.showByEnvironmentAndService = function() {
                return new Promise((resolve, reject) => {
                if ($scope.selectedService === null || $scope.selectedEnvironment === null) { //unselected service or environment
                    growl.error("Please select a service and an environment!", {ttl:7000});
                    return;
                } else if ($scope.selectedService.isPartOfGroup && $scope.selectedGroup === null) { //service is part of group but no group is selected -> show available groups
                    if ($scope.currentScreen == "selectGroup") {
                        growl.error("Please select a group... :)", {ttl:7000});
                        return;
                    }
                    apolloApiService.getGroupsPerServiceAndEnvironment($scope.selectedEnvironment.id, $scope.selectedService.id).then(function (response) {
                        $scope.serviceGroups = response.data;
                        if (response.data.length === 0) {
                            growl.error("No available groups for the service and environment you selected", {ttl:7000});
                            return;
                        } else {
                            $scope.currentScreen = "selectGroup";
                            return;
                        }
                    });
                } else if ($scope.selectedService !== null && $scope.selectedEnvironment !== null) { //selected environment and service


                       function handleError(error) {
                           growl.error(errorMessage, {ttl:7000});
                           reject(null);
                       }
                      function responseReceived(response) {
                          $scope.kubernetesDeploymentStatus.push(response.data);
                          $scope.currentScreen = "results";
                          resolve();
                      }
                    if ($scope.selectedService.isPartOfGroup && $scope.selectedGroup !== null) { //service is part of group and selected group
                        apolloApiService.statusOfEnvironmentAndServiceWithGroup($scope.selectedEnvironment.id, $scope.selectedService.id, $scope.selectedGroup.name)
                        .then(responseReceived, handleError)
                    } else { //service is not part of group
                        apolloApiService.statusOfEnvironmentAndService($scope.selectedEnvironment.id, $scope.selectedService.id)
                        .then(responseReceived, handleError)
                    }
                  }
                })
            }

            $scope.selectPod = function (podStatus) {
                $scope.selectedPodStatus = podStatus;
            };

            $scope.restartPod = function (podName) {

                usSpinnerService.spin('result-spinner');

                apolloApiService.restartPod($scope.selectedEnvironment.id, podName).then(function (response) {
                    usSpinnerService.stop('result-spinner');
                    growl.success("Successfully restarted pod " + podName + "!");
                }, function (error) {
                    usSpinnerService.stop('result-spinner');
                    growl.error("Could not restart pod! got: " + error.statusText, {ttl:7000});
                });
            };

            $scope.restartAllPods = function () {

                usSpinnerService.spin('result-spinner');

                var groupName = $scope.selectedGroup === null ? null : $scope.selectedGroup.name

                apolloApiService.restartAllPods($scope.selectedEnvironment.id, $scope.selectedService.id, groupName).then(function (response) {
                    usSpinnerService.stop('result-spinner');
                    growl.success("Successfully restarted all pods!");
                }, function (error) {
                    usSpinnerService.stop('result-spinner');
                    growl.error("Could not restart the pods! got: " + error.statusText, {ttl:7000});
                });
            };

            var setSelectedStatusAfterRefresh = function() {
                $scope.selectedStatus = $scope.kubernetesDeploymentStatus.filter(status =>
                    status.environmentId === $scope.selectedStatus.environmentId && status.serviceId === $scope.selectedStatus.serviceId)[0];
            }

            $scope.refreshStatus = function() {
                $scope.kubernetesDeploymentStatus = [];
                usSpinnerService.spin('result-spinner');
                return $scope.showByEnvironmentAndService().then(() => {
                    setSelectedStatusAfterRefresh();
                    usSpinnerService.stop('result-spinner');
                    return $scope.$apply();
                }).catch(err => {
                   alert(err);
                })
            }
            $scope.startWebSocket = function (containerName) {
                setTimeout(function () {
                    $scope.term = new Terminal({
                        scrollback: 3000
                    });

                    var environmentId = $scope.selectedEnvironment.id;
                    var serviceId = $scope.selectedService.id;
                    var podName = $scope.selectedPodStatus.name;

                    var execUrl = null;

                    if ($scope.websocketScope === execTypeName) {
                        execUrl = apolloApiService.getWebsocketExecUrl(environmentId, serviceId, podName, containerName);
                    } else if ($scope.websocketScope === logsTypeName){
                        execUrl = apolloApiService.getWebsocketLogUrl(environmentId, serviceId, podName, containerName);
                    } else {
                        growl.error("Unexpected error!", {ttl:7000});
                        return;
                    }

                    $scope.websocket = new WebSocket(execUrl);

                    if ($scope.websocketScope === execTypeName) {
                        $scope.websocket.onopen = function () {
                            $scope.websocket.send("export TERM=\"xterm\"\n");
                        };
                    }

                    $scope.websocket.onerror = function (event) {
                        if (event.code) {
                            growl.error("Unknown error occurred, error code: " + event.code, {ttl: 7000});
                        } else {
                            growl.error("You don't have permissions to deploy to that service on that environment, hence no live-session!", {ttl: 7000});
                        }
                    };

                    $scope.term.open(document.getElementById('terminal'));
                    $scope.term.fit();
                    $scope.term.focus();

                    $scope.term.attach($scope.websocket, true, false);

                    if ($scope.websocketScope === execTypeName) {
                        $scope.term.writeln("Wait for the prompt, initializing...");
                    } else if ($scope.websocketScope === logsTypeName) {
                        $scope.term.writeln("Opening web socket, wait a sec!");
                    }

                }, 300);
            };

            $scope.closeWebSocket = function () {
                if ($scope.term !== null) {
                    $scope.term.detach();
                    $scope.term.destroy();
                }

                if ($scope.websocket !== null) {
                    $scope.websocket.close();
                }
            };

            $scope.setWebsocketScope = function (scope) {
                $scope.websocketScope = scope;

                if (scope === execTypeName) {
                    $scope.terminalHeader = "Live Session to " + $scope.selectedPodStatus.name;
                } else if (scope === logsTypeName) {
                    $scope.terminalHeader = "Live Tail on " + $scope.selectedPodStatus.name;
                }
            };

            $scope.openHawtio = function (podStatus) {
                $window.open(apolloApiService.getHawtioLink($scope.selectedStatus.environmentId, podStatus.name));
            };

            $scope.setSelectedStatus = function (status) {
                $scope.selectedStatus = status;

                getService(status.serviceId)
                .then(service => {
                    if (service.isPartOfGroup) {
                        getGroup(status.groupName)
                        .then(group => {
                            $scope.selectedGroup = group;
                        });
                    }

                });
            };

            var getService = function (serviceId) {
                return apolloApiService.getAllServices()
                    .then((response) => {
                        return response.data.find(currService => currService.id === serviceId);
                    });
            }

            var getGroup = function (groupName) {
                return apolloApiService.getGroupByName(groupName)
                    .then((response) => {
                        return response.data;
                    });
            }

            $scope.setSelectedService = function (service) {
                $scope.selectedService = service;
            }

            $scope.setSelectedEnvironment = function (environment) {
                $scope.selectedEnvironment = environment;
            }

            // Data fetching
            apolloApiService.getAllEnvironments().then(function(response) {
                $scope.allEnvironments = response.data;
                $scope.allEnvironments.sort(function(a, b) {
                    if (a.name < b.name) { return -1; }
                    if (a.name > b.name) { return 1; }
                    return 0;
                })
            });

            apolloApiService.getAllServices().then(function(response) {
                $scope.allServices = response.data;
                $scope.allServices.sort(function (a, b) {
                    if (a.name < b.name) { return -1; }
                    if (a.name > b.name) { return 1; }
                    return 0;
                })
            });
}]);