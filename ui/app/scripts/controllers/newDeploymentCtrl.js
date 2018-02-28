'use strict';

angular.module('apollo')
  .controller('newDeploymentCtrl', ['apolloApiService', '$scope',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService', 'DTColumnDefBuilder', 'localStorageService', "hotkeys",
            function (apolloApiService, $scope, $timeout, $state, growl, usSpinnerService, DTColumnDefBuilder, localStorageService, hotkeys) {


        var previouseEnvironmentLocalStorageKey = 'previous-run-environment-id';
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
        var deploymentSteps = ["choose-environment", "choose-service", "choose-version", "confirmation"];

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
		$scope.versionSelected = null;
		$scope.showNextStep = true;

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

        $scope.setSelectedService = function (serviceSelected) {
            if ($scope.serviceSelected !== serviceSelected) {
                $scope.serviceSelected = serviceSelected;
                loadDeployableVersions(serviceSelected.id);
            }

            if (serviceSelected !== undefined && serviceSelected.isPartOfGroup) {
                deploymentSteps = ["choose-environment", "choose-service", "choose-groups", "choose-version", "confirmation"];
                if ($scope.environmentSelected !== undefined) {
                    loadGroups($scope.environmentSelected.id, $scope.serviceSelected.id);
                }
            } else {
                deploymentSteps = ["choose-environment", "choose-service", "choose-version", "confirmation"];
            }
        };

        $scope.setSelectedVersion = function (versionSelected) {
            $scope.versionSelected = versionSelected;
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
            if ($scope.selectedGroups.length > 0 && $scope.serviceSelected.isPartOfGroup) {
                apolloApiService.createNewDeploymentWithGroup(getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                    $scope.serviceSelected.id, $scope.environmentSelected.id, $scope.deploymentMessage.text, $scope.selectedGroups.map(function (group) { return group.id; }).join(','))
                    .then(function (response) {

                    // Wait a bit to let the deployment be in the DB
                    setTimeout(function () {
                        usSpinnerService.stop('deployment-spinner');

                        // Due to bug with angular-bootstrap and angular 1.4, the modal is not closing when redirecting.
                        // So just forcing it to :)   TODO: after the bug is fixed, remove this shit
                        $('#confirm-modal').modal('hide');
                        $('body').removeClass('modal-open');
                        $('.modal-backdrop').remove();

                        // Redirect user to ongoing deployments
                        $state.go('deployments.ongoing', {deploymentId: response.data.id});
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
            else if (($scope.selectedGroups.length > 0 && !$scope.serviceSelected.isPartOfGroup) ||
                    ($scope.selectedGroups.length === 0 && $scope.serviceSelected.isPartOfGroup)) {
                growl.error("It is unclear if your deployment should be deployed with or without groups. Try again.");
            }

            // No-groups deployment
            else {
                apolloApiService.createNewDeployment(getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                    $scope.serviceSelected.id, $scope.environmentSelected.id, $scope.deploymentMessage.text).then(function (response) {

                    // Wait a bit to let the deployment be in the DB
                    setTimeout(function () {
                        usSpinnerService.stop('deployment-spinner');

                        // Due to bug with angular-bootstrap and angular 1.4, the modal is not closing when redirecting.
                        // So just forcing it to :)   TODO: after the bug is fixed, remove this shit
                        $('#confirm-modal').modal('hide');
                        $('body').removeClass('modal-open');
                        $('.modal-backdrop').remove();

                        // Redirect user to ongoing deployments
                        $state.go('deployments.ongoing', {deploymentId: response.data.id});
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
            localStorageService.set(previouseEnvironmentLocalStorageKey, $scope.environmentSelected.id);
            localStorageService.set(previouseServiceLocalStorageKey, $scope.serviceSelected.id);
        };

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

        $scope.selectAllGroups = function() {
            $scope.selectedGroups = [];
            angular.forEach($scope.possibleGroups, function(group) {
                $scope.selectedGroups.push(group);
            });
            updateGroupsNames();
        };

        var updateGroupsNames = function() {
            $scope.groupNames = $scope.selectedGroups.map(function (group) { return group.name; }).join(', ');
        };

        $scope.dtOptions = {
            paginationType: 'simple_numbers',
            displayLength: 20,
            dom: '<"row"<"col-sm-6"i><"col-sm-6"f>>rt<"bottom"p>',
            order: [[ 0, "asc" ]]
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
            return $scope.environmentSelected !== null;
        }

        function validateService() {
            return $scope.serviceSelected !== null;
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
			$scope.allEnvironments = response.data;

			// Get selection from local storage
            var previousEnvironmentId = localStorageService.get(previouseEnvironmentLocalStorageKey);
            if (previousEnvironmentId !== undefined) {
                $scope.setSelectedEnvironment($scope.allEnvironments.filter(function(a){return a.id === previousEnvironmentId})[0]);
            }
		});

		apolloApiService.getAllServices().then(function(response) {
        	$scope.allServices = response.data;

        	// Get selection from local storage
            var previousServiceId = localStorageService.get(previouseServiceLocalStorageKey);
            if (previousServiceId !== undefined) {
                $scope.setSelectedService($scope.allServices.filter(function(a){return a.id === previousServiceId})[0]);
            }
        });

		function loadDeployableVersions(serviceId) {
            apolloApiService.getLatestDeployableVersionsByServiceId(serviceId).then(function(response) {
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