'use strict';

angular.module('apollo')
  .controller('deploymentHistoryCtrl', ['apolloApiService', '$scope',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService', 'DTColumnBuilder', 'DTColumnDefBuilder',
            function (apolloApiService, $scope, $timeout, $state, growl, usSpinnerService, DTColumnBuilder, DTColumnDefBuilder) {

                // Kinda ugly custom sorting for datatables
                jQuery.extend( jQuery.fn.dataTableExt.oSort, {
                    "date-time-pre": function ( date ) {
                        return moment(date, 'DD/MM/YY HH:mm:ss');
                    },
                    "date-time-asc": function ( a, b ) {
                        return (a.isBefore(b) ? -1 : (a.isAfter(b) ? 1 : 0));
                    },
                    "date-time-desc": function ( a, b ) {
                        return (a.isBefore(b) ? 1 : (a.isAfter(b) ? -1 : 0));
                    }
                });

                $scope.selectedDeployment = null;

                $scope.setSelectedDeployment = function(selectedDeployment) {
                    $scope.selectedDeployment = selectedDeployment;
                   };

                $scope.getLabel = function(deploymentStatus) {
                    return apolloApiService.matchLabelToDeploymentStatus(deploymentStatus);
                };

                $scope.showDetails = function() {

                    $scope.deployableVersion = undefined;
                    usSpinnerService.spin('details-spinner');

                    apolloApiService.getDeployableVersion(
                    $scope.allDeployments.filter(function(a){return a.id === $scope.selectedDeployment.id;})[0].deployableVersionId)
                    .then(function(response) {
                        $scope.deployableVersion = response.data;
                        usSpinnerService.stop('details-spinner');
                    },
                    function(error){
                            usSpinnerService.stop('details-spinner');
                            growl.error("Could not fetch data from apollo! error: " + error.data);
                    });
                };

                $scope.showEnvStatus = function(deployment) {

                    if (typeof(deployment) === 'undefined') {
                        return;
                    }

                    if (deployment.status !== 'DONE' && deployment.status !== 'CANCELED') {
                        $scope.envStatus = 'The environment status is not available while the deployment status is "' + deployment.status + '".';
                        return;
                    }

                    $scope.envStatus = undefined;
                    $scope.envStatusWithServiceNames = {};
                    usSpinnerService.spin('details-spinner');

                    apolloApiService.getDeploymentEnvStatus(deployment.id)
                    .then(function(response) {
                        $scope.envStatus = response.data;
                        $scope.envStatus = JSON.parse($scope.envStatus);

                        for (var serviceId in $scope.envStatus) {

                            if(typeof($scope.envStatus[serviceId]) === 'object') {
                                var groupsWithNames = {};

                                for (var groupId in $scope.envStatus[serviceId]) {
                                    groupsWithNames[$scope.allGroups[groupId].name] = $scope.envStatus[serviceId][groupId];
                                }

                                $scope.envStatusWithServiceNames[$scope.allServices[serviceId].name] = groupsWithNames;

                            } else {
                                $scope.envStatusWithServiceNames[$scope.allServices[serviceId].name] = $scope.envStatus[serviceId];
                            }
                        }

                        usSpinnerService.stop('details-spinner');
                    },
                    function(error){
                            usSpinnerService.stop('details-spinner');
                            growl.error("Could not fetch environment status data! error: " + error.data);
                    });
                };

                $scope.revert = function() {

                    // Set spinner
                    usSpinnerService.spin('revert-spinner');

                    // Now we can deploy
                    apolloApiService.createNewDeployment($scope.selectedDeployment.deployableVersionId,
                        $scope.selectedDeployment.serviceId, $scope.selectedDeployment.environmentId).then(function (response) {

                        // Wait a bit to let the deployment be in the DB
                        setTimeout(function () {
                            usSpinnerService.stop('revert-spinner');

                            // Redirect user to ongoing deployments
                            $state.go('deployments.ongoing', {deploymentId: response.data.id});
                        }, 500);

                    }, function(error) {
                        // End spinner
                        usSpinnerService.stop('revert-spinner');

                        // 403 are handled generically on the interceptor
                        if (error.status !== 403) {
                            growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000})
                        }
                    });
                };

                $scope.clearFilters = function() {
                    $scope.serviceSearch = "";
                    $scope.environmentSearch = "";
                    $scope.userSearch = "";
                    $scope.statusSearch = "";
                };

                $scope.dtOptions = {
                    paginationType: 'simple_numbers',
                    displayLength: 10,
                    dom: '<"top"i>rt<"bottom"p>',
                    order: [[1, "desc" ]],
                    serverSide: true,
                    processing: true,
                    ajax: {
                        url: CONFIG.appUrl + 'deployment/datatables',
                        type: 'GET'
                    },
                    columns:[
                        { "data": "id" },
                        { "data": "lastUpdated" },
                        { "data": "serviceId" },
                        { "data": "environmentId" },
                        { "data": "groupName" },
                        { "data": "userName" },
                        { "data": "status" }
                    ]
                };

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('id', '#'),
                    DTColumnBuilder.newColumn('lastUpdated', 'Last Updated'),
                    DTColumnBuilder.newColumn('serviceId', 'Service'),
                    DTColumnBuilder.newColumn('environmentId', 'Environment'),
                    DTColumnBuilder.newColumn('groupName', 'Group'),
                    DTColumnBuilder.newColumn('userName', 'User'),
                    DTColumnBuilder.newColumn('status', 'Status'),
                    //DTColumnBuilder.newColumn('actions', 'Actions').renderWith("bla"),
                ]

                $scope.dtColumnDefs = [
                    DTColumnDefBuilder.newColumnDef([1]).withOption('type', 'date-time')
                ];

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

//                apolloApiService.getAllDeployments().then(function(response) {
//                   $scope.allDeployments = response.data;
//                });

                apolloApiService.getAllGroups().then(function(response) {
                    var tempGroups = {};
                    response.data.forEach(function(group) {
                        tempGroups[group.id] = group;
                    });

                    $scope.allGroups = tempGroups;
                });
            }]);
