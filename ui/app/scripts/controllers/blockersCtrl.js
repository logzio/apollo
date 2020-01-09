'use strict';

angular.module('apollo')
  .controller('blockersCtrl', ['apolloApiService', '$scope', '$filter',
                                    '$timeout', '$state', '$interval', 'growl', 'usSpinnerService',
            function (apolloApiService, $scope, $filter, $timeout, $state, $interval, growl, usSpinnerService) {

      $scope.tabsIndices = ["unconditional", "timebased", "branch"];
      $scope.days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
      $scope.selectedDays = [];
      $scope.timeBasedBlockerStartTime = {date: new Date()};
      $scope.timeBasedBlockerEndTime = {date: new Date()};
      $scope.allStacks = [];
      $scope.allAvailabilitiesWithoutDuplicates = [];
      $scope.allBlockersAvailabilitiesList = [];

      $scope.toggleSelectedDay = function toggleSelectedDay(day) {
          var id = $scope.selectedDays.indexOf(day);
          if (id > -1) {
              $scope.selectedDays.splice(id, 1);
          }
          else {
            $scope.selectedDays.push(day);
          }
      };
      $scope.setBlockerTypeName = function (name) {
          $scope.blockerTypeName = name;
      };

      $scope.newBlocker = function () {
          $scope.currentBlocker = null;
          $scope.blockerActive = true;
      };

      $scope.setCurrentBlocker = function (blocker) {
          $scope.currentBlocker = blocker;

          // Fill in ng-models for easy edit
          $scope.blockerName = blocker.name;
          $scope.blockerService = $scope.allServices[blocker.serviceId];
          $scope.blockerEnvironment = $scope.allEnvironments[blocker.environmentId];
          $scope.blockerStack = $scope.allStacks[blocker.stackId];
          $scope.blockerAvailability = $scope.allBlockersAvailabilitiesList[blocker.id];
          $scope.blockerActive = blocker.active;

          $scope.blockerTypeName = blocker.blockerTypeName;
          $scope.blockerTypeNameIndex = $scope.tabsIndices.indexOf($scope.blockerTypeName);

          var jsonParams = JSON.parse(blocker.blockerJsonConfiguration);

          switch ($scope.blockerTypeName) {
              case "timebased":
                  var startHour = jsonParams["startTimeUtc"].split(":")[0];
                  var startMinute = jsonParams["startTimeUtc"].split(":")[1];

                  var endHour = jsonParams["endTimeUtc"].split(":")[0];
                  var endMinute = jsonParams["endTimeUtc"].split(":")[1];

                  $scope.timeBasedBlockerStartTime.date = new Date(1970, 1, 1, startHour, startMinute, 1, 1);
                  $scope.timeBasedBlockerEndTime.date = new Date(1970, 1, 1, endHour, endMinute, 1, 1);

                  $scope.selectedDays = [];

                  angular.forEach(jsonParams["daysOfTheWeek"], function (value) {
                      $scope.selectedDays.push($scope.days[value - 1]);
                  });

                  break;
              case "branch":
                  $scope.branchBlockerName = jsonParams["branchName"];
                  break;
          }
      };

      $scope.deleteBlocker = function (blocker) {
          apolloApiService.deleteBlocker(blocker.id).then(function (response) {
              usSpinnerService.stop('blocker-spinner');
              growl.success("Successfully deleted blocker " + blocker.name + "!");
              updateBlockers();
          }, function (error) {
              usSpinnerService.stop('blocker-spinner');
              growl.error("Could not delete blocker! got: " + error.statusText)
          });

          updateBlockers();
      };

      $scope.saveBlocker = function () {

          var serviceId = null;
          var environmentId = null;
          var stackId = null;
          var availability = $scope.blockerAvailability;
          var isActive = $scope.blockerActive;
          var blockerName = $scope.blockerName;
          var blockerTypeName = $scope.blockerTypeName;
          var jsonParams = null;

          if ($scope.blockerService) {
              serviceId = $scope.blockerService.id;
          }

          if ($scope.blockerEnvironment) {
              environmentId = $scope.blockerEnvironment.id;
          }

          if ($scope.blockerStack) {
              stackId = $scope.blockerStack.id;
          }

          switch (blockerTypeName){
              case "unconditional":
                  jsonParams = {};
                  break;

              case "timebased":

                  var days_indices = [];

                  // We need to convert the days to their respective number (Mon=1, ...Sun=7)
                  angular.forEach($scope.selectedDays, function (value) {
                      days_indices.push($scope.days.indexOf(value) + 1);
                  });

                  jsonParams = {
                      startTimeUtc: $filter('date')($scope.timeBasedBlockerStartTime.date, "HH:mm"),
                      endTimeUtc: $filter('date')($scope.timeBasedBlockerEndTime.date, "HH:mm"),
                      daysOfTheWeek: days_indices
                  };
                  break;

              case "branch":
                  jsonParams = {
                      "branchName": $scope.branchBlockerName
                  };
                  break;
          }

          if (!$scope.currentBlocker) {
              apolloApiService.addBlocker(blockerName, environmentId, serviceId, stackId, availability, isActive, blockerTypeName, JSON.stringify(jsonParams)).then(function (response) {
                  usSpinnerService.stop('blocker-spinner');
                  growl.success("Successfully added blocker " + blockerName + "!");
                  updateBlockers();
              }, function (error) {
                  usSpinnerService.stop('blocker-spinner');
                  growl.error("Could not add blocker! got: " + error.statusText)
              });
          } else {
              apolloApiService.updateBlocker($scope.currentBlocker.id, blockerName, environmentId, serviceId, stackId, availability, isActive, blockerTypeName, JSON.stringify(jsonParams)).then(function (response) {
                  usSpinnerService.stop('blocker-spinner');
                  growl.success("Successfully updated blocker " + blockerName + "!");
                  updateBlockers();
              }, function (error) {
                  usSpinnerService.stop('blocker-spinner');
                  growl.error("Could not update blocker! got: " + error.statusText)
              });
          }
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
              $scope.allAvailabilitiesWithoutDuplicates[environment.id] = environment.availability;
          });

          $scope.allAvailabilitiesWithoutDuplicates = $scope.allAvailabilitiesWithoutDuplicates.filter((x, i, a) => a.indexOf(x) == i)
          $scope.allEnvironments = tempEnvironment;
      });
                
      apolloApiService.getAllServices().then(function(response) {
          var tempServices = {};
          response.data.forEach(function(service) {
              tempServices[service.id] = service;
          });
        
          $scope.allServices = tempServices;
      });

      apolloApiService.getAllStacks().then(function(response) {
          var tempStacks = {};
          response.data.forEach(function(stack) {
            tempStacks[stack.id] = stack;
          });

        $scope.allStacks = tempStacks;
      });

      function updateBlockers() {
          apolloApiService.getAllBlockers().then(function(response) {
              var tempBlockers = {};
              var orderedAvailabilitiesByBlockersIds = [];
              response.data.forEach(function(blocker) {
                  tempBlockers[blocker.id] = blocker;
                  orderedAvailabilitiesByBlockersIds[blocker.id] = blocker.availability;
              });

              $scope.allBlockers = tempBlockers;
              $scope.allBlockersAvailabilitiesList = orderedAvailabilitiesByBlockersIds;
          });
      }

      updateBlockers();
}]);