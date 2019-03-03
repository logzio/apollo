'use strict';

angular.module('apollo')
  .controller('serviceVersionStatusCtrlOriginal', ['apolloApiService', '$scope',
                                    '$timeout', '$state', '$interval', '$window','growl', 'usSpinnerService',
            function (apolloApiService, $scope, $timeout, $state, $interval, $window, growl, usSpinnerService) {

            var execTypeName = "exec";
            var logsTypeName = "logs";

            $scope.filteredResults = [];
            $scope.selectedStatus = null;
            $scope.selectedPodStatus = null;
            $scope.currentScreen = "filters";
            $scope.showingBy = null;
            $scope.showingByValue = null;
            $scope.websocket = null;
            $scope.term = null;
            $scope.websocketScope = null;
            $scope.terminalHeader = null;

            $scope.showByService = function(service) {
                usSpinnerService.spin('result-spinner');
                $scope.filteredResults = [];
                apolloApiService.serviceStatus(service.id).then(function (response) {
                    $scope.filteredResults = response.data;
                    $scope.filteredResults = $scope.filteredResults.filter(function(x){ return x !== null; });
                    populateDeployableVersions();
                    refreshPreSelectedStatus();

                    if (service.isPartOfGroup) {
                        var groupedByEnvironment = [];
                        $scope.filteredResults.forEach(function (kubeDeploymentStatus) {
                            var first = true;
                            groupedByEnvironment.forEach(function (existingKubeDeploymentStatus) {
                                if (existingKubeDeploymentStatus.environmentId === kubeDeploymentStatus.environmentId) {
                                    first = false;
                                    existingKubeDeploymentStatus.nestedKubeDeploymentStatuses.push(kubeDeploymentStatus);
                                }
                            });
                            if (first) {
                                kubeDeploymentStatus.nestedKubeDeploymentStatuses = [kubeDeploymentStatus];
                                groupedByEnvironment.push(kubeDeploymentStatus);
                            }
                        });
                        $scope.filteredResults = groupedByEnvironment;
                    } else {
                        $scope.filteredResults.forEach(function (kubeDeploymentStatus) {
                            kubeDeploymentStatus.nestedKubeDeploymentStatuses = [];
                        });
                    }

                    usSpinnerService.stop('result-spinner');
                });
                $scope.currentScreen = "results";
                $scope.showingBy = "service";
                $scope.showingByValue = service;
            };

            $scope.showByEnvironment = function(environmentId) {
                usSpinnerService.spin('result-spinner');
                $scope.filteredResults = [];
                apolloApiService.environmentStatus(environmentId).then(function (response) {
                    $scope.filteredResults = response.data;
                    $scope.filteredResults = $scope.filteredResults.filter(function(x){ return x !== null; });
                    populateDeployableVersions();
                    refreshPreSelectedStatus();

                    var groupedByService = [];
                    $scope.filteredResults.forEach(function (kubeDeploymentStatus) {
                        if (kubeDeploymentStatus.groupName === null) {
                            kubeDeploymentStatus.nestedKubeDeploymentStatuses = [];
                            groupedByService.push(kubeDeploymentStatus);
                        } else {
                            var first = true;
                            groupedByService.forEach(function (existingKubeDeploymentStatus) {
                                if (existingKubeDeploymentStatus.serviceId === kubeDeploymentStatus.serviceId) {
                                    first = false;
                                    existingKubeDeploymentStatus.nestedKubeDeploymentStatuses.push(kubeDeploymentStatus);
                                }
                            });
                            if (first) {
                                kubeDeploymentStatus.nestedKubeDeploymentStatuses = [kubeDeploymentStatus];
                                groupedByService.push(kubeDeploymentStatus);
                            }
                        }
                    });
                    $scope.filteredResults = groupedByService;
                    usSpinnerService.stop('result-spinner');
                });
                $scope.currentScreen = "results";
                $scope.showingBy = "environment";
                $scope.showingByValue = environmentId;
            };

            $scope.backToFilter = function() {
                $scope.currentScreen = "filters";
            };

            $scope.setSelectedStatus = function (status) {
                $scope.selectedStatus = status;
            };

            $scope.setWebsocketScope = function (scope) {
                $scope.websocketScope = scope;

                if (scope === execTypeName) {
                    $scope.terminalHeader = "Live Session to " + $scope.selectedPodStatus.name;
                } else if (scope === logsTypeName) {
                    $scope.terminalHeader = "Live Tail on " + $scope.selectedPodStatus.name;
                }
            };

            $scope.restartPod = function (podName) {

                usSpinnerService.spin('result-spinner');

                apolloApiService.restartPod($scope.selectedStatus.environmentId, podName).then(function (response) {
                    usSpinnerService.stop('result-spinner');
                    growl.success("Successfully restarted pod " + podName + "!");
                }, function (error) {
                    usSpinnerService.stop('result-spinner');
                    growl.error("Could not restart pod! got: " + error.statusText);
                });
            };

            $scope.restartAllPods = function (environmentId, serviceId, groupName) {

                console.log("serviceID: " + serviceId + ", envID: " + environmentId + ", group name: " + groupName);

                usSpinnerService.spin('result-spinner');

                apolloApiService.restartAllPods(environmentId, serviceId, groupName).then(function (response) {
                    usSpinnerService.stop('result-spinner');
                    growl.success("Successfully restarted all pods!");
                }, function (error) {
                    usSpinnerService.stop('result-spinner');
                    growl.error("Could not restart the pods! got: " + error.statusText);
                });
            };

            $scope.refreshStatus = function () {
                if ($scope.showingBy === "service") {
                    $scope.showByService($scope.showingByValue);
                } else {
                    $scope.showByEnvironment($scope.showingByValue);
                }
            };

            $scope.selectPod = function (podStatus) {
                $scope.selectedPodStatus = podStatus;
            };

            $scope.startWebSocket = function (containerName) {
                setTimeout(function () {
                    $scope.term = new Terminal({
                        scrollback: 3000
                    });

                    var environmentId = $scope.selectedStatus.environmentId;
                    var serviceId = $scope.selectedStatus.serviceId;
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

            $scope.openHawtio = function (podStatus) {
                $window.open(apolloApiService.getHawtioLink($scope.selectedStatus.environmentId, podStatus.name));
            };

            function refreshPreSelectedStatus() {
                if ($scope.selectedStatus) {

                    // Refresh the view for the user
                    $scope.selectedStatus = $scope.filteredResults.filter(function (filtered) {
                        if (filtered) {
                            return (filtered.serviceId === $scope.selectedStatus.serviceId && filtered.environmentId === $scope.selectedStatus.environmentId);
                        }
                    })[0];
                }
            }

            function fetchLatestLogs() {
                apolloApiService.logsFromStatus($scope.selectedStatus.environmentId, $scope.selectedStatus.serviceId).then(function(response) {
                    $scope.dockerLogs = response.data;
                });
            }

            function populateDeployableVersions(service) {
                $scope.deployableVersions = [];
                $scope.filteredResults.forEach(function (status) {
                    if (status !== null) {
                        apolloApiService.getDeployableVersionBasedOnSha(status.gitCommitSha, status.serviceId).then(function (response) {
                            $scope.deployableVersions[response.data.gitCommitSha] = response.data;
                        });
                    }
                });
            }

            // Data fetching
            apolloApiService.getAllEnvironments().then(function(response) {
                var tempEnvironment = {};
                response.data.forEach(function(environment) {
                    tempEnvironment[environment.id] = environment;
                });

                $scope.allEnvironments = tempEnvironment;
            });

            apolloApiService.getAllServices().then(function(response) {
                var tempServices = {};
                response.data.forEach(function(service) {
                    tempServices[service.id] = service;
                });

                $scope.allServices = tempServices;
            });
}]);