'use strict';

angular.module('apollo')
  .controller('deploymentHistoryCtrl', ['apolloApiService', '$scope', '$compile',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService',
                                    'DTColumnBuilder', 'DTColumnDefBuilder', 'DTOptionsBuilder',
            function (apolloApiService, $scope, $compile, $timeout, $state, growl, usSpinnerService, DTColumnBuilder, DTColumnDefBuilder, DTOptionsBuilder) {

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

                $scope.selectedDeploymentId = null;

                $scope.setSelectedDeploymentId = function(selectedDeploymentId) {
                    $scope.selectedDeploymentId = selectedDeploymentId;
                };

                $scope.getLabel = function(deploymentStatus) {
                    return apolloApiService.matchLabelToDeploymentStatus(deploymentStatus);
                };

                $scope.showDetails = function(deployableVersionId) {

                    $scope.deployableVersion = undefined;
                    usSpinnerService.spin('details-spinner');

                    apolloApiService.getDeployableVersion(deployableVersionId)
                    .then(function(response) {
                        $scope.deployableVersion = response.data;
                        usSpinnerService.stop('details-spinner');
                    },
                    function(error){
                            usSpinnerService.stop('details-spinner');
                            growl.error("Could not fetch data from apollo! error: " + error.data);
                    });
                };

                $scope.showEnvStatus = function(deploymentId) {
                    $scope.envStatus = undefined;
                    $scope.envStatusWithServiceNames = {};
                    usSpinnerService.spin('details-spinner');

                    apolloApiService.getDeploymentEnvStatus(deploymentId)
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

                    if ($scope.selectedDeploymentId == null) {
                        $growl.error("Could not understand which deployment you are talking about, bailing..");
                        return;
                    }

                    apolloApiService.getDeployment($scope.selectedDeploymentId).then(function(response) {
                        var selectedDeployment = response.data;

                        // Set spinner
                        usSpinnerService.spin('revert-spinner');

                        // Now we can deploy
                        apolloApiService.createNewDeployment(selectedDeployment.deployableVersionId,
                            selectedDeployment.serviceId, selectedDeployment.environmentId).then(function (response) {

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
                    });
                };

                $scope.dtOptions = DTOptionsBuilder.newOptions()
                    .withOption('ajax', {
                        url: CONFIG.appUrl + 'deployment/datatables',
                        type: 'GET'
                    })
                    .withDataProp('data')
                    .withOption('processing', true)
                    .withOption('serverSide', true)
                    .withOption('dom', '<"top"i>frt<"bottom"p>')
                    .withOption('order', [[1, "desc" ]])
                    .withOption('createdRow', function(row, data, dataIndex) {
                        $compile(angular.element(row).contents())($scope);
                    })
                    .withDisplayLength(50)
                    .withPaginationType('simple_numbers');

                $scope.dtColumns = [
                    DTColumnBuilder.newColumn('id').withTitle('#'),
                    DTColumnBuilder.newColumn('lastUpdate').withTitle('Last Update').notSortable().renderWith(function(data, type, full) {
                        return moment.unix(data / 1000).format("DD/MM/YY HH:mm:ss")
                    }),
                    DTColumnBuilder.newColumn('serviceName').withTitle('Service').notSortable(),
                    DTColumnBuilder.newColumn('environmentName').withTitle('Environment').notSortable(),
                    DTColumnBuilder.newColumn('groupName').withTitle('Group').notSortable(),
                    DTColumnBuilder.newColumn('userEmail').withTitle('User').notSortable(),
                    DTColumnBuilder.newColumn('status').withTitle('Status').notSortable().renderWith(function(data, type, full) {
                        return `
                            <span class="label ${$scope.getLabel(full.status)}">${full.status}</span>
                        `
                    }),
                    DTColumnBuilder.newColumn('actions').withTitle('Actions').notSortable().renderWith(renderActions)
                ];

                function renderActions(data, type, full) {
                    return `
                                            <div class="row">
                                                <button type="button" class="btn btn-primary btn-circle" uib-tooltip="Details"
                                                        ng-click="showDetails(${full.deployableVersionId})"
                                                        data-toggle="modal" data-target="#show-details">
                                                    <i class="fa fa-info"></i>
                                                </button>
                                                <button type="button" class="btn btn-danger btn-circle" uib-tooltip="Back to this version"
                                                        ng-click="setSelectedDeploymentId(${full.id})"
                                                        data-toggle="modal" data-target="#confirm-revert">
                                                    <i class="fa fa-undo"></i>
                                                </button>
                                                <button type="button" class="btn btn-success btn-circle" uib-tooltip="Environment status"
                                                        ng-click="showEnvStatus(${full.id})"
                                                        data-toggle="modal" data-target="#show-env-status">
                                                    <i class="fa fa-eye"></i>
                                                </button>
                                                <button type="button" class="btn btn-circle disabled" uib-tooltip="${full.deploymentMessage || 'No message was provided'}">
                                                    <i class="fa fa-comments"></i>
                                                </button>
                                            <div>
                    `
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

                apolloApiService.getAllGroups().then(function(response) {
                    var tempGroups = {};
                    response.data.forEach(function(group) {
                        tempGroups[group.id] = group;
                    });

                    $scope.allGroups = tempGroups;
                });
            }]);
