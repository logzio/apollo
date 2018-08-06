'use strict';

angular.module('apollo')
  .controller('newDeploymentCtrl', ['apolloApiService', '$scope',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService', 'DTColumnDefBuilder', 'localStorageService', "hotkeys",
            function (apolloApiService, $scope, $timeout, $state, growl, usSpinnerService, DTColumnDefBuilder, localStorageService, hotkeys) {


        var favoriteEnvironmentsLocalStorageKey = 'favorite-environments-names';
        $scope.favoriteEnvironmentsNames = localStorageService.get(favoriteEnvironmentsLocalStorageKey) || [];

        var previouseEnvironmentLocalStorageKey = 'previous-run-environment-id';


        var favoriteServicesLocalStorageKey = 'favorite-services-names';
        $scope.favoriteServicesNames = localStorageService.get(favoriteServicesLocalStorageKey) || [];

        var previouseServiceLocalStorageKey = 'previous-run-service-id';

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

        // Define the flow steps
        var deploymentSteps = ["choose-service", "choose-environment", "choose-version", "confirmation"];

        // Define validation functions.. //TODO: something better?
        var deploymentValidators = {"choose-environment" : validateEnvironment,
                                    "choose-service" : validateService,
                                    "choose-groups" : validateGroups,
                                    "choose-version" : validateVersion};

        // Scope variables
		$scope.environmentSelected = null;
		$scope.serviceSelected = null;
		$scope.possibleGroups = null;
		$scope.selectedGroups = [];
		$scope.groupNames = null;
        $scope.possibleEnvironments = null;
        $scope.selectedEnvironments = [];
        $scope.environmentNames = null;
        $scope.possibleServices = null;
        $scope.selectedServices = [];
        $scope.serviceNames = null;
		$scope.versionSelected = null;
		$scope.showNextStep = true;
		$scope.isGroupDeployment = false;

		// Angular can't ng-model to a variable which is not an object :(
		$scope.deploymentMessage = {};

		$scope.currentStep = deploymentSteps[0];

		// Class variables
        var loadedGroupsEnvironmentId;
        var loadedGroupsServiceId;

        // Scope setters
        $scope.setSelectedEnvironment = function (environmentSelected) {
            $scope.environmentSelected = environmentSelected;
        };

        $scope.setSelectedVersion = function (versionSelected) {
            $scope.versionSelected = versionSelected;
            $scope.deploymentMessage.text = versionSelected.commitMessage && versionSelected.commitMessage.split('\n')[0]
        };

        // Visual change the next step
        $scope.nextStep = function() {

            // First validate the input
            if (deploymentValidators[$scope.currentStep]()) {

                // Get the current index
                var currIndex = deploymentSteps.indexOf($scope.currentStep);

                // Increment the index
                currIndex++;

                // Choose the next step
                $scope.currentStep = deploymentSteps[currIndex];

                // Clear the search
                $scope.globalSearch = "";

                // Finish flow if in last step
                if (currIndex === deploymentSteps.length - 1) {
                    $scope.showNextStep = false;
                }

                // Load deployableVersions
                if ($scope.currentStep == "choose-version") {
                    loadDeployableVersions($scope.selectedServices.map(function (service) { return service.id; }).join(','));
                }
            }
            else {
                growl.error("You must select one!");
            }
        };

        $scope.deploy = function() {

            // Just running the validators first, to make sure nothing has changed
            angular.forEach(deploymentValidators, function(validateFunction, name) {

                  if (!validateFunction()) {
                    growl.error("Something unexpected has occurred! Try again.");
                  }
             });

            // Set spinner
            usSpinnerService.spin('deployment-spinner');

            // Now we can deploy!

            // Valid groups deployment
            if ($scope.selectedGroups.length > 0 && $scope.selectedServices.length == 1 && $scope.selectedServices[0].isPartOfGroup) {
                apolloApiService.createNewDeploymentWithGroup(
                        getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                        $scope.selectedServices[0].id,
                        $scope.selectedEnvironments.map(function (environment) { return environment.id; }).join(','),
                        $scope.deploymentMessage.text, $scope.selectedGroups.map(function (group) { return group.id; }).join(',')
                    ).then(function (response) {

                    // Wait a bit to let the deployment be in the DB
                    setTimeout(function () {
                        usSpinnerService.stop('deployment-spinner');

                        // Due to bug with angular-bootstrap and angular 1.4, the modal is not closing when redirecting.
                        // So just forcing it to :)   TODO: after the bug is fixed, remove this shit
                        $('#confirm-modal').modal('hide');
                        $('body').removeClass('modal-open');
                        $('.modal-backdrop').remove();

                        if (response.data.unsuccessful.length > 0) {
                            showBlockedDeployments(response);
                        } else if (response.data.successful.length > 0) {
                            $scope.redirectToOngoing();
                        } else {
                            growl.error("An error occurred.", {ttl: 7000});
                        }
                    }, 500);

                }, function (error) {
                    // End spinner
                    usSpinnerService.stop('deployment-spinner');

                    // 403 are handled generically on the interceptor
                    if (error.status !== 403) {
                        growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000});
                    }
                });
            }

            // Not clear if deployment is with or without groups
            else if (($scope.selectedGroups.length > 0 && (!$scope.selectedServices[0].isPartOfGroup || $scope.selectedServices.length !== 1)) ||
                    ($scope.selectedGroups.length === 0 && $scope.selectedServices.filter(function(a){return a.isPartOfGroup}).length > 0)) {
                growl.error("It is unclear if your deployment should be deployed with or without groups. Try again.");
            }

            // No-groups deployment
            else {
                apolloApiService.createNewDeployment(
                        getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                        $scope.selectedServices.map(function (service) { return service.id; }).join(','),
                        $scope.selectedEnvironments.map(function (environment) { return environment.id; }).join(','),
                        $scope.deploymentMessage.text
                    ).then(function (response) {

                    // Wait a bit to let the deployment be in the DB
                    setTimeout(function () {
                        usSpinnerService.stop('deployment-spinner');

                        // Due to bug with angular-bootstrap and angular 1.4, the modal is not closing when redirecting.
                        // So just forcing it to :)   TODO: after the bug is fixed, remove this shit
                        $('#confirm-modal').modal('hide');
                        $('body').removeClass('modal-open');
                        $('.modal-backdrop').remove();

                        $scope.successfulDeployments = response.data.successful;

                        if (response.data.unsuccessful.length > 0) {
                            showBlockedDeployments(response);
                        } else {
                            $scope.redirectToOngoing();
                        }

                    }, 500);

                }, function (error) {
                    // End spinner
                    usSpinnerService.stop('deployment-spinner');

                    // 403 are handled generically on the interceptor
                    if (error.status !== 403) {
                        growl.error(error.data, {ttl: 7000});
                    }
                });
            }

            // Set the current selection on local storage, for pre-selection on the next run
            localStorageService.set(previouseEnvironmentLocalStorageKey, $scope.selectedEnvironments);
            localStorageService.set(previouseServiceLocalStorageKey, $scope.selectedServices);
        };

        var showBlockedDeployments = function(response) {
            $scope.blockedDeployments = [];

            angular.forEach(response.data.unsuccessful, function(unsuccessfulDeployment) {
                $scope.blockedDeployments.push({
                    service: getServiceNameById(unsuccessfulDeployment.serviceId),
                    environment: getEnvironmentNameById(unsuccessfulDeployment.environmentId),
                    group: getGroupNameById(unsuccessfulDeployment.groupId),
                    reason: unsuccessfulDeployment.exception.message
                });
            });

            $('#blocked-deployments').modal('show');
        }

        var getServiceNameById = function(id) {

            if (id == undefined) {
                if ($scope.selectedServices !== undefined && $scope.selectedServices.length > 0) {
                    return $scope.selectedServices[0].name;
                }
                return '-';
            }

            return $scope.possibleServices.filter(function(a){return a.id == id})[0].name;
        }

        var getEnvironmentNameById = function(id) {

            if (id == undefined) {
                if ($scope.selectedEnvironments !== undefined && $scope.selectedEnvironments.length > 0) {
                    return $scope.selectedEnvironments[0].name;
                }
                return '-';
            }

            return $scope.possibleEnvironments.filter(function(a){return a.id == id})[0].name;
        }

        var getGroupNameById = function(id) {
            if (id == undefined)
                return '-';

            return $scope.possibleGroups.filter(function(a){return a.id == id})[0].name;
        }

        $scope.closeBlockerModal = function() {
            $('#blocked-deployments').modal('hide');
            $scope.redirectToOngoing();
        }

        $scope.redirectToOngoing = function() {
            $state.go('deployments.ongoing', {deploymentResult: $scope.successfulDeployments});
        }

        $scope.firstLine = function (multiLineString) {
            if (!multiLineString) {
                return '';
            }

            var firstLine = multiLineString.split('\n')[0];
            firstLine = firstLine.split('\r')[0];
            return firstLine;
        };

        $scope.deployableVersionFromBranch = function(branchName) {
            if (branchName === undefined) {
                branchName = $scope.branchName;
            }

            apolloApiService.getDeployableVersionFromLatestCommitOnBranch(branchName, $scope.allDeployableVersions[0].id)
                .then(function (response) {
                $scope.allDeployableVersions = [];
                $scope.allDeployableVersions[0] = response.data;
            }, function (error) {
                    growl.error("Could not get latest commit on this branch!");
                });
        };

        $scope.toggleSelectedGroup = function(group) {
            var index = $scope.selectedGroups.indexOf(group);
		    if (index > -1) {
		        $scope.selectedGroups.splice(index, 1);
		    }
		    else {
		        $scope.selectedGroups.push(group);
		    }
		    updateGroupsNames();
        };

       $scope.toggleSelectedEnvironment = function(environment) {
            var index = $scope.selectedEnvironments.indexOf(environment);
            if (index > -1) {
                $scope.selectedEnvironments.splice(index, 1);
            }
            else {
                if ($scope.isGroupDeployment) {
                    $scope.selectedEnvironments = [];
                }
                $scope.selectedEnvironments.push(environment);
            }
            if ($scope.isGroupDeployment && $scope.selectedServices !== null && $scope.selectedServices.length > 0 && $scope.selectedEnvironments.length > 0) {
                loadGroups($scope.selectedEnvironments[0].id, $scope.selectedServices[0].id);
            }
            updateEnvironmentsNames();
        };

       $scope.toggleSelectedService = function(service) {
            var index = $scope.selectedServices.indexOf(service);
            if (index > -1) {
                $scope.selectedServices.splice(index, 1);
            } else {
                if (service.isPartOfGroup) {
                    $scope.selectedServices = [];
                    deploymentSteps = ["choose-service", "choose-environment", "choose-groups", "choose-version", "confirmation"];
                    $scope.selectedServices.push(service);
                    $scope.isGroupDeployment = true;
                } else {
                    $scope.selectedServices = $scope.selectedServices.filter(function(a){return !a.isPartOfGroup});
                    $scope.selectedServices.push(service);
                    $scope.isGroupDeployment = false;
                    deploymentSteps = ["choose-service", "choose-environment", "choose-version", "confirmation"];
                }
            }
            updateServicesNames();
        };

       $scope.toggleMarkServiceAsFavorite = function(serviceName, event) {
           var indexOfService = $scope.favoriteServicesNames.indexOf(serviceName);
           if (indexOfService > -1) {
               $scope.favoriteServicesNames.splice(indexOfService, 1);
           } else {
               $scope.favoriteServicesNames.push(serviceName);
           }

           event.stopPropagation(); // To make sure the <tr> ng-click="toggleSelectedService(service)" wont take effect

           localStorageService.set(favoriteServicesLocalStorageKey, $scope.favoriteServicesNames);
       };


        $scope.toggleMarkEnvironmentAsFavorite = function(environmentName, event) {
            var indexOfEnvironment = $scope.favoriteEnvironmentsNames.indexOf(environmentName);
            if (indexOfEnvironment > -1) {
                $scope.favoriteEnvironmentsNames.splice(indexOfEnvironment, 1);
            } else {
                $scope.favoriteEnvironmentsNames.push(environmentName);
            }

            event.stopPropagation(); // To make sure the <tr> ng-click="toggleSelectedEnvironment(service)" wont take effect

            localStorageService.set(favoriteEnvironmentsLocalStorageKey, $scope.favoriteEnvironmentsNames);
        };

        $scope.selectAllGroups = function() {
            $scope.selectedGroups = [];
            angular.forEach($scope.possibleGroups, function(group) {
                $scope.selectedGroups.push(group);
            });
            updateGroupsNames();
        };

        $scope.selectAllEnvironments = function() {
            $scope.selectedEnvironments = [];
            angular.forEach($scope.possibleEnvironments, function(environment) {
                $scope.selectedEnvironments.push(environment);
            });
            updateEnvironmentsNames();
        };

        $scope.selectAllServices = function() {
            $scope.selectedServices = [];
            angular.forEach($scope.possibleServices, function(service) {
                $scope.selectedServices.push(service);
            });
            updateServicesNames();
        };

        var updateGroupsNames = function() {
            $scope.groupNames = $scope.selectedGroups.map(function (group) { return group.name; }).join(', ');
        };

        var updateEnvironmentsNames = function() {
            $scope.environmentNames = $scope.selectedEnvironments.map(function (environment) { return environment.name; }).join(', ');
        };

        var updateServicesNames = function() {
            $scope.serviceNames = $scope.selectedServices.map(function (service) { return service.name; }).join(', ');
        };

        $scope.dtOptions = {
            paginationType: 'simple_numbers',
            displayLength: 20,
            dom: '<"row"<"col-sm-6"i><"col-sm-6"f>>rt<"bottom"p>',
            order: [[ 0, "desc" ], [ 2, "asc" ]] // Mark as favorite and name
        };

        $scope.dtOptionsDeployableVersion = {
            paginationType: 'simple_numbers',
            displayLength: 10,
            dom: '<"row"<"col-sm-6"i><"col-sm-6"f>>rt<"bottom"p>',
            order: [[ 0, "desc" ]]
        };

         $scope.dtColumnDefsDeployableVersion = [
             DTColumnDefBuilder.newColumnDef([0]).withOption('type', 'date-time')
         ];

        // Validators
        function validateEnvironment() {
            return typeof $scope.selectedEnvironments !== 'undefined' && $scope.selectedEnvironments.length > 0
        }

        function validateService() {
            return typeof $scope.selectedServices !== 'undefined' && $scope.selectedServices.length > 0
        }

        function validateVersion() {
            if ($scope.versionSelected === null) {
                return false;
            }
            // TODO: add more checks here.. (service can get the version etc..)
            return true;
        }

        function validateGroups() {
            return true;
        }

        function getDeployableVersionFromCommit(sha) {
            return $scope.allDeployableVersions.filter(function(a){return a.gitCommitSha === sha})[0].id;
        }

        // Data fetching
		apolloApiService.getAllEnvironments().then(function(response) {
            $scope.possibleEnvironments = response.data;
			// Get selection from local storage
            var previousEnvironmentIds = localStorageService.get(previouseEnvironmentLocalStorageKey);

            if (previousEnvironmentIds !== null) {
                $scope.selectedEnvironments = $scope.possibleEnvironments.filter(function(a){return previousEnvironmentIds.indexOf(a.id) > -1})
            }
		});

		apolloApiService.getAllServices().then(function(response) {
            $scope.possibleServices = response.data;

        	// Get selection from local storage
            var previousServiceIds = localStorageService.get(previouseServiceLocalStorageKey);
            if (previousServiceIds !== null) {
                $scope.selectedServices = $scope.possibleServices.filter(function(a){return previousServiceIds.indexOf(a.id) > -1})
            }
        });

		function loadDeployableVersions(serviceIdsCsv) {
            apolloApiService.getDeployableVersionForMultiServices(serviceIdsCsv).then(function(response) {
                $scope.allDeployableVersions = response.data;
            });
        }

        function loadGroups(environmentId, serviceId) {
		    if (environmentId !== loadedGroupsEnvironmentId || serviceId !== loadedGroupsServiceId) {
		        loadedGroupsEnvironmentId = environmentId;
		        loadedGroupsServiceId = serviceId;
                apolloApiService.getGroupsPerServiceAndEnvironment(environmentId, serviceId).then(function (response) {
                    $scope.possibleGroups = response.data;
                });
            }
        }

        hotkeys.bindTo($scope)
            .add({
                combo: "enter",
                description: "Next Step",
                callback: function () {
                    $scope.nextStep();
                }
            });
}]);