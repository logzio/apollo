'use strict';

angular.module('apollo')
  .controller('ongoingDeploymentCtrl', ['apolloApiService', '$scope', '$stateParams', '$interval',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService',
            function (apolloApiService, $scope, $stateParams, $interval, $timeout, $state, growl, usSpinnerService) {

                $scope.myDeployments = $stateParams.deploymentResult == null ? [] : $stateParams.deploymentResult.map(function (successful) { return successful.deployment.id; });
                $scope.selectedDeployment = null;
                $scope.selectedDeploymentContainers = [];
                $scope.selectedDeploymentLatestPod = null;
                $scope.websocket = null;
                $scope.term = null;

                $scope.setSelectedDeployment = function(selectedDeployment) {
                    $scope.selectedDeployment = selectedDeployment;
                };

                $scope.revert = function() {
                    usSpinnerService.spin('ongoing-spinner');
                    apolloApiService.revertDeployment($scope.selectedDeployment.id).then(function(response) {
                        usSpinnerService.stop('ongoing-spinner');
                        growl.success("Successfully reverted deployment!", {ttl: 7000});

                    }, function(error) {
                        usSpinnerService.stop('ongoing-spinner');
                        growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000});
                    });

                };

                $scope.getContainersOfSelectedDeployment = function () {
                  apolloApiService.latestCreatedPod($scope.selectedDeployment.environmentId,
                      $scope.selectedDeployment.serviceId).then(function (latestPodResponse) {
                          $scope.selectedDeploymentLatestPod = latestPodResponse.data;
                          apolloApiService.podContainers($scope.selectedDeployment.environmentId,
                              latestPodResponse.data).then(function (containersResponse) {
                                  $scope.selectedDeploymentContainers = containersResponse.data;
                          }, function (containerError) {
                              growl.error("Could not fetch the containers of the latest running pod, try again!");
                          });

                  }, function(error) {
                      growl.error("Could not find latest running pod for that deployment, try again!");
                    });
                };

                $scope.getContainersOfSelectedDeploymentWithGroup = function () {
                    $scope.hideStatusPerGroupModal();
                    apolloApiService.latestCreatedPodWithGroup($scope.selectedDeployment.environmentId,
                      $scope.selectedDeployment.serviceId, $scope.selectedDeployment.groupName).then(function (latestPodResponse) {
                          $scope.selectedDeploymentLatestPod = latestPodResponse.data;
                          apolloApiService.podContainers($scope.selectedDeployment.environmentId,
                              latestPodResponse.data).then(function (containersResponse) {
                                  $scope.selectedDeploymentContainers = containersResponse.data;
                          }, function (containerError) {
                              growl.error("Could not fetch the containers of the latest running pod, try again!");
                          });

                  }, function(error) {
                      growl.error("Could not find latest running pod for that deployment, try again!");
                    });
                };

                $scope.hideStatusPerGroupModal = function () {
                    $('#status-per-group').modal('hide');
                };

                $scope.startLogsWebsocket = function (containerName) {
                    setTimeout(function () {
                    $scope.term = new Terminal({
                        scrollback: 3000
                    });

                    var environmentId = $scope.selectedDeployment.environmentId;
                    var serviceId = $scope.selectedDeployment.serviceId;
                    var podName = $scope.selectedDeploymentLatestPod;
                    var execUrl = apolloApiService.getWebsocketLogUrl(environmentId, serviceId, podName, containerName);

                    $scope.websocket = new WebSocket(execUrl);

                    $scope.websocket.onerror = function (event) {
                        if (event.code) {
                            growl.error("Unknown error occurred, error code: " + event.code, {ttl: 7000});
                        }
                    };

                    $scope.term.open(document.getElementById('terminal'));
                    $scope.term.fit();
                    $scope.term.focus();

                    $scope.term.attach($scope.websocket, true, false);
                    $scope.term.writeln("Opening web socket, wait a sec!");

                }, 300);
                };

                $scope.stopLogsWebsocket = function () {
                    if ($scope.term !== null) {
                    $scope.term.detach();
                    $scope.term.destroy();
                }

                if ($scope.websocket !== null) {
                    $scope.websocket.close();
                }
                };

                $scope.getLabel = function(deploymentStatus) {
                    return apolloApiService.matchLabelToDeploymentStatus(deploymentStatus);
                };

                $scope.getRevertClass = function(deploymentStatus) {
                    if (apolloApiService.isRevertDisabledBasedOnStatus(deploymentStatus)) {
                        return "disabled";
                    }
                    return "";
                };

                $scope.isRevertDisabledBasedOnStatus = function(deploymentStatus) {
                    return apolloApiService.isRevertDisabledBasedOnStatus(deploymentStatus);
                };

                $scope.dtOptions = {
                    paginationType: 'simple_numbers',
                    displayLength: 20,
                    dom: '<"top"i>rt<"bottom"p>',
                    order: [[ 0, "desc" ]]
                };

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

                apolloApiService.getAllUsers().then(function(response) {
                    var tempUsers = {};
                    response.data.forEach(function(user) {
                        tempUsers[user.userEmail] = user;
                    });
                    $scope.allUsers = tempUsers;
                });

                function refreshDeployments() {

                    apolloApiService.getRunningAndJustFinishedDeployments().then(function (response) {
                        $scope.runningAndJustFinishedDeployments = response.data;

                        var unitedDeployments = [];

                        $scope.runningAndJustFinishedDeployments.forEach(function (deployment) {
                            if (deployment.groupName === null) {
                                deployment.nestedDeployments = [];
                                unitedDeployments.push(deployment);
                            } else {
                                var first = true;
                                unitedDeployments.forEach(function (existingDeployment) {
                                    if (existingDeployment.environmentId === deployment.environmentId &&
                                        existingDeployment.serviceId === deployment.serviceId &&
                                        existingDeployment.deployableVersionId === deployment.deployableVersionId) {

                                        first = false;
                                        existingDeployment.nestedDeployments.push(deployment);
                                    }
                                });
                                if (first) {
                                    deployment.nestedDeployments = [deployment];
                                    unitedDeployments.push(deployment);
                                }
                            }
                        });

                        $scope.runningAndJustFinishedDeployments = unitedDeployments;
                    });
                }

                refreshDeployments();
                var interval = $interval(refreshDeployments, 5000);

                $scope.$on('$destroy', function () {
                    $interval.cancel(interval);
                });

            }]);