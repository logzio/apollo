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
		$scope.possibleGroups = [];
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
        $scope.possibleEnvironmentsStacks = null;
        $scope.possibleServicesStacks = null;
        $scope.servicesAndStackForDisplay = null;
        $scope.environmentsAndStackForDisplay = null;
        $scope.selectedServicesAndStacks = [];
        $scope.selectedEnvironmentsAndStacks = [];
        $scope.servicesNamesOfStacks = [];
        $scope.environmentsNamesOfStacks = [];
        $scope.servicesInStack = [];
        $scope.environmentsInStack = [];
        $scope.findMyCommitFlag = false;

        var groupsPromises = [];
        var servicesAndStackForDisplayWorking = [];
        var environmentsAndStackForDisplayWorking = [];
        var allServices = [];
        var allEnvironments = [];

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

                $scope.$apply();
            }
            else {
                growl.error("You must select one!");
            }
        };

        function afterCreatingNewDeploymentWithGroup(response) {
            // Wait a bit to let the deployment be in the DB
            return new Promise((resolve, reject) =>  {
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
                        reject("An error occurred")
                    }
                    resolve("done");
                }, 500);
            })
        }

        function afterCreatingNewDeploymentWithGroupInCaseOfError(error) {
            usSpinnerService.stop('deployment-spinner');

           // 403 are handled generically on the interceptor
           if (error.status !== 403) {
               growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000});
           }
        }

        $scope.deploy = function(isEmergencyRollback) {

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
                $scope.selectedEnvironments.forEach(function(environment) {
                    var groups = [];
                    $scope.selectedGroups.forEach(function(group) {
                        if(group.environmentId === environment.id) {
                            groups.push(group.id);
                        }
                    });
                    if (!isEmergencyRollback) {
                        apolloApiService.createNewDeploymentWithGroup(
                            getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                            $scope.selectedServices[0].id,
                            environment.id,
                            $scope.deploymentMessage.text,
                            groups.join(','),
                            isEmergencyRollback)
                            .then(afterCreatingNewDeploymentWithGroup)
                            .catch(afterCreatingNewDeploymentWithGroupInCaseOfError)
                    }
                    else {
                        apolloApiService.createNewDeploymentWithGroup(
                            getDeployableVersionFromCommit($scope.versionSelected.gitCommitSha),
                            $scope.selectedServices[0].id,
                            environment.id,
                            $scope.deploymentMessage.text,
                            groups.join(','),
                            isEmergencyRollback)
                            .then(afterCreatingNewDeploymentWithGroup)
                            .catch(afterCreatingNewDeploymentWithGroupInCaseOfError)
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
                        $scope.deploymentMessage.text,
                        isEmergencyRollback 
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

            return $scope.possibleServices[id].name;
        }

        var getEnvironmentNameById = function(id) {

            if (id == undefined) {
                if ($scope.selectedEnvironments !== undefined && $scope.selectedEnvironments.length > 0) {
                    return $scope.selectedEnvironments[0].name;
                }
                return '-';
            }

            return $scope.possibleEnvironments[id].name;
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

        $scope.getGroupsNamesPerEnvironment = function(environmentId) {
             var relevantGroups = $scope.selectedGroups.filter(group => group.environmentId === environmentId);
             var groupsNamesPerEnvironment = relevantGroups.map(x => x.name).join(', ');
             return groupsNamesPerEnvironment;
        }

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
            var index = $scope.selectedEnvironmentsAndStacks.indexOf(environment);
            if (index > -1) {
            //already exists
                $scope.selectedEnvironmentsAndStacks.splice(index, 1);
                if(environment.isStack == true) { //stack case
                    environment.environments.forEach(function(environmentId) {
                         var environmentInStack = allEnvironments.find(x => x.id === environmentId);
                         var index = $scope.selectedEnvironments.indexOf(environmentInStack);
                         $scope.selectedEnvironments.splice(index, 1);
                         index = $scope.selectedEnvironmentsAndStacks.indexOf(environmentInStack);
                         $scope.selectedEnvironmentsAndStacks.splice(index, 1);
                    });
                }
                else { //regular environment case
                    var index = $scope.selectedEnvironments.indexOf(environment);
                    if(index > -1) {
                        $scope.selectedEnvironments.splice(index, 1);
                    }
                }
            }
            else {
            //doesn't exist - want to add it
                if(environment.isStack == true) { //stack case
                    var stack = environment;
                    var environmentsNames = [];
                    $scope.selectedEnvironmentsAndStacks.push(stack);
                    stack.environments.forEach(function(environmentId) {
                        var environment = allEnvironments.find(x => x.id === environmentId);
                        var environmentName = environment.name;
                        environmentsNames = environmentsNames.concat(environmentName);
                        if(($scope.selectedEnvironments).includes(environment) == false) {
                            $scope.selectedEnvironments.push(environment);
                            $scope.selectedEnvironmentsAndStacks.push(environment);
                        }
                    });
                    $scope.environmentsNamesOfStacks[environment.id] = environmentsNames;
                }
                else { //regular environment case
                    var index = $scope.selectedEnvironments.indexOf(environment);
                    if(index <= -1) {
                        $scope.selectedEnvironmentsAndStacks.push(environment);
                        $scope.selectedEnvironments.push(environment);
                    }
                }
            }
            if ($scope.isGroupDeployment && $scope.selectedServices !== null && $scope.selectedServices.length > 0 && $scope.selectedEnvironments.length > 0) {
                $scope.selectedEnvironments.forEach(function(environment) {
                    groupsPromises.push(loadGroups(environment.id, $scope.selectedServices[0].id));
                });
            }
            updateEnvironmentsNames();
        };

        $scope.waitForGroupsBeforeNextStep = function() {
            usSpinnerService.spin('deployment-spinner');
            return Promise.all(groupsPromises).then(function (response) {
                usSpinnerService.stop('deployment-spinner');
            });
        };

        var filterGroups = function() {
            var selectedEnvsIds = new Set($scope.selectedEnvironments.map(environment => environment.id));
            var filteredGroupsByEnvs = $scope.possibleGroups.filter(group => selectedEnvsIds.has(group.environmentId));

            var distinctGroups = [];
            var mapGroups = new Map();

            for (var group of filteredGroupsByEnvs) {
                if(!mapGroups.has(group.id)){
                    mapGroups.set(group.id, true);
                    distinctGroups.push(group);
                }
            }

            $scope.possibleGroups = distinctGroups;
        }

       $scope.handleEnvironmentDbClick = function() {
           $scope.waitForGroupsBeforeNextStep().then(function() {
                filterGroups();
                $scope.nextStep();
           });
       }

       $scope.toggleSelectedServiceOrStack = function(service) {
            var index = $scope.selectedServicesAndStacks.indexOf(service);
            if (index > -1) {
            //already exists
                $scope.selectedServicesAndStacks.splice(index, 1);
                if(service.isStack == true) { //stack case
                    service.services.forEach(function(serviceId) {
                         var serviceInStack = allServices.find(x => x.id === serviceId);
                         var index = $scope.selectedServices.indexOf(serviceInStack);
                         if(index > -1) {
                            $scope.selectedServices.splice(index, 1);
                         }
                         index = $scope.selectedServicesAndStacks.indexOf(serviceInStack);
                         if(index > -1) {
                            $scope.selectedServicesAndStacks.splice(index, 1);
                         }
                    });
                }
                else { //regular service case
                    var index = $scope.selectedServices.indexOf(service);
                    if(index > -1) {
                        $scope.selectedServices.splice(index, 1);
                    }
                    $scope.isGroupDeployment = false;
                }

            } else {
            //doesn't exist - want to add it
                if(service.isStack == true) { //stack case
                    if($scope.isGroupDeployment == false) { //didn't select group service
                        var stack = service;
                        var servicesNames = [];
                        $scope.selectedServicesAndStacks.push(stack);
                        stack.services.forEach(function(serviceId) {
                          var service = allServices.find(x => x.id === serviceId);
                          var serviceName = service.name;
                          servicesNames = servicesNames.concat(serviceName);
                          if(($scope.selectedServices).includes(service) == false) {
                              var index = $scope.selectedServicesAndStacks.indexOf(stack);
                              toggleSelectedService(service, index);
                          }
                        });
                    }
                }
                else { //regular service case
                    if((service.isPartOfGroup === false && $scope.selectedServices.length >= 0 && $scope.isGroupDeployment === false) || (service.isPartOfGroup && $scope.selectedServices.length === 0)) {
                        var index = $scope.selectedServices.indexOf(service);
                        if(index <= -1) {
                            var index = $scope.selectedServicesAndStacks.indexOf(service);
                            toggleSelectedService(service, index);
                        }
                    }
                }
            }
            $scope.servicesNamesOfStacks[service.id] = servicesNames;
            updateServicesNames();
        };

        function toggleSelectedService(service, index) {
            if($scope.isGroupDeployment == false) {
                if (service.isPartOfGroup) {
                    if($scope.selectedServices.length == 0) {
                        $scope.selectedServices = [];
                        deploymentSteps = ["choose-service", "choose-environment", "choose-groups", "choose-version", "confirmation"];
                        $scope.selectedServicesAndStacks.push(service);
                        $scope.selectedServices.push(service);
                        $scope.isGroupDeployment = true;
                    }
                    else {
                        $scope.selectedServicesAndStacks.splice(index, 1);
                    }

                } else {
                    $scope.selectedServices = $scope.selectedServices.filter(function(a){return !a.isPartOfGroup});
                    $scope.selectedServices.push(service);
                    $scope.selectedServicesAndStacks.push(service);
                    deploymentSteps = ["choose-service", "choose-environment", "choose-version", "confirmation"];
                }
            }
            else {
                if($scope.selectedServices.length == 0) {
                    $scope.isGroupDeployment = false;
                    toggleSelectedService(service, index);
                }
                else {
                    $scope.selectedServicesAndStacks.splice(index, 1);
                }
            }
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
            order: [[ 0, "asc" ], [1, "desc"], [ 3, "asc" ]]
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

        apolloApiService.getAllEnvironments()
         .then(parseAllEnvironments)
         .then(() => {
		     apolloApiService.getAllEnvironmentsStacks().then(function(response) {
             var tempEnvironmentsStack = {};
		     response.data.forEach(function(environmentsStack) {
                tempEnvironmentsStack[environmentsStack.id] = environmentsStack;
                addStackToDisplayEnvironments(environmentsStack);

                var environmentsNames = [];
                environmentsStack.environments.forEach(function(environmentId) {
                    var environment = allEnvironments.find(x => x.id === environmentId);
                    var environmentName = environment.name;
                    environmentsNames = environmentsNames.concat(environmentName);
                });

                $scope.environmentsInStack[environmentsStack.id] = environmentsNames;
             });
             $scope.possibleEnvironmentsStacks = tempEnvironmentsStack;
             updateFinalDisplayEnvironmentsAndStacks();
        });
        })

        function parseAllEnvironments(response) {
            var tempEnvironment = {};
            response.data.forEach(function(environment) {
                tempEnvironment[environment.id] = environment;
                addEnvironmentToDisplayEnvironments(environment);
            });
            $scope.possibleEnvironments = tempEnvironment;
            allEnvironments = response.data;
            updateFinalDisplayEnvironmentsAndStacks();

           return Promise.resolve();
        }

		function addStackToDisplayEnvironments(stack) {
            stack.isStack = true;
            stack.orderId = -1;
            environmentsAndStackForDisplayWorking = environmentsAndStackForDisplayWorking.concat(stack);
        }

        function addEnvironmentToDisplayEnvironments(environment) {
            environment.isStack = false;
            environment.orderId = 1;
            environmentsAndStackForDisplayWorking = environmentsAndStackForDisplayWorking.concat(environment);
        }

         function updateFinalDisplayEnvironmentsAndStacks() {
            if ($scope.possibleEnvironments !== null && $scope.possibleEnvironmentsStacks !== null) {
                    $scope.environmentsAndStackForDisplay = environmentsAndStackForDisplayWorking;
            }
         }

		apolloApiService.getAllServices()
		    .then(parseAllServices)
		    .then(() => {
		        apolloApiService.getAllServicesStacks().then(function(response) {
                    var tempServicesStack = {};
                    response.data.forEach(function(servicesStack) {
                       tempServicesStack[servicesStack.id] = servicesStack;
                       addStackToDisplayServices(servicesStack);

                       var servicesNames = [];
                       servicesStack.services.forEach(function(serviceId) {
                            var service = allServices.find(x => x.id === serviceId);
                            var serviceName = service.name;
                            servicesNames = servicesNames.concat(serviceName);
                       });

                       $scope.servicesInStack[servicesStack.id] = servicesNames;
                    });
                    $scope.possibleServicesStacks = tempServicesStack;
                    updateFinalDisplayServicesAndStacks();
		    })
        })

        function parseAllServices(response) {
            var tempServices = {};
            response.data.forEach(function(service) {
                tempServices[service.id] = service;
                addServiceToDisplayServices(service);
            });
            $scope.possibleServices = tempServices;
            allServices = response.data;
            updateFinalDisplayServicesAndStacks();

            return Promise.resolve();
        }

        function addStackToDisplayServices(stack) {
            stack.isStack = true;
            stack.orderId = -1;
            servicesAndStackForDisplayWorking = servicesAndStackForDisplayWorking.concat(stack);
        }

        function addServiceToDisplayServices(service) {
            service.isStack = false;
            service.orderId = 1;
            servicesAndStackForDisplayWorking = servicesAndStackForDisplayWorking.concat(service);
        }

        function updateFinalDisplayServicesAndStacks() {
            if ($scope.possibleServices !== null && $scope.possibleServicesStacks !== null) {
                    $scope.servicesAndStackForDisplay = servicesAndStackForDisplayWorking;
            }
        }

        $scope.getSuitableDeployableVersionsFromPartialSha = function(commitSha) {
            apolloApiService.getSuitableDeployableVersionsFromPartialSha(commitSha).then(function(response) {
                $scope.findMyCommitFlag = true;
                $scope.allDeployableVersions = [];
                if (response.data.length > 0) {
                    $scope.allDeployableVersions.push(response.data[0]);
                }
            })
        }

		function loadDeployableVersions(serviceIdsCsv) {
            apolloApiService.getDeployableVersionForMultiServices(serviceIdsCsv).then(function(response) {
                if (!$scope.findMyCommitFlag) {
                    $scope.allDeployableVersions = response.data;
                }
            });
        }

        function loadGroups(environmentId, serviceId) {
		    if (environmentId !== loadedGroupsEnvironmentId || serviceId !== loadedGroupsServiceId) {
		        loadedGroupsEnvironmentId = environmentId;
		        loadedGroupsServiceId = serviceId;
                return apolloApiService.getGroupsPerServiceAndEnvironment(environmentId, serviceId).then(function (response) {
                    response.data.forEach(function(group) {
                        $scope.possibleGroups.push(group);
                    });
                });
            }

            return Promise.resolve();
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