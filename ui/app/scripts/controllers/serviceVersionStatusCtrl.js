'use strict';

angular.module('apollo')
  .controller('serviceVersionStatusCtrl', ['apolloApiService', '$scope',
                                    '$timeout', '$state', '$interval', '$window','growl', 'usSpinnerService',
            function (apolloApiService, $scope, $timeout, $state, $interval, $window, growl, usSpinnerService) {

            var execTypeName = "exec";
            var logsTypeName = "logs";

            $scope.currentScreen = "selectServiceAndEnvironment";
            $scope.selectedService = null;
            $scope.selectedEnvironment = null;
            $scope.selectedGroup = null;
            $scope.serviceGroups = null;
            $scope.kubernetesDeploymentStatus = null;
            $scope.selectedPodStatus = null;

            $scope.showByEnvironmentAndService = function() {

                if ($scope.selectedService == null || $scope.selectedEnvironment == null) {
                    growl.error("Please select a service and an environment!");
                    return;
                }

                if ($scope.selectedService.isPartOfGroup && $scope.selectedGroup == null) {

                    apolloApiService.getGroupsPerServiceAndEnvironment($scope.selectedService.id, $scope.selectedEnvironment.id).then(function (response) {
                        $scope.serviceGroups = response.data;
                    });

                    $scope.currentScreen = "selectGroup";
                    return;
                }

                if ($scope.selectedService != null && $scope.selectedEnvironment != null) {

                    if ($scope.selectedService.isPartOfGroup && $scope.selectedGroup != null) {
                        apolloApiService.statusOfEnvironmentAndServiceWithGroup($scope.selectedEnvironment.id, $scope.selectedService.id, $scope.selectedGroup.name).then(
                            function (response) {
                                $scope.kubernetesDeploymentStatus = response.data;
                                $scope.currentScreen = "result";
                            },
                            function (error) {
                                growl.error("An error occurred in getting k8s status");
                            });
                    } else {
                        apolloApiService.statusOfEnvironmentAndService($scope.selectedEnvironment.id, $scope.selectedService.id).then(
                            function (response) {
                                $scope.kubernetesDeploymentStatus = response.data;
                                $scope.currentScreen = "result";
                            },
                            function (error) {
                                growl.error("An error occurred in getting k8s status");
                            });
                    }
                }
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
                    growl.error("Could not restart pod! got: " + error.statusText);
                });
            };

            $scope.restartAllPods = function () {

                console.log("serviceID: " + $scope.selectedService.id + ", envID: " + $scope.selectedEnvironment.id + ", group name: " + $scope.selectedGroup.name);

                usSpinnerService.spin('result-spinner');

                apolloApiService.restartAllPods($scope.selectedEnvironment.id, $scope.selectedService.id, $scope.selectedGroup.name).then(function (response) {
                    usSpinnerService.stop('result-spinner');
                    growl.success("Successfully restarted all pods!");
                }, function (error) {
                    usSpinnerService.stop('result-spinner');
                    growl.error("Could not restart the pods! got: " + error.statusText);
                });
            };

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
                        growl.error("Unexpected error!");
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

            function populateDeployableVersions() {
                $scope.deployableVersion;
                if ($scope.kubernetesDeploymentStatus !== null) {
                    apolloApiService.getDeployableVersionBasedOnSha($scope.kubernetesDeploymentStatus.gitCommitSha, $scope.kubernetesDeploymentStatus.serviceId).then(function (response) {
                        $scope.deployableVersion = response.data;
                    });
                }
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
            });

            apolloApiService.getAllServices().then(function(response) {
                $scope.allServices = response.data;
            });
}]);