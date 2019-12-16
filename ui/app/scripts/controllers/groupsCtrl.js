'use strict';

angular.module('apollo')
  .controller('groupsCtrl', ['apolloApiService', '$scope', '$filter',
                                    '$timeout', '$state', '$interval', 'growl', 'usSpinnerService',
            function (apolloApiService, $scope, $filter, $timeout, $state, $interval, growl, usSpinnerService) {

      var defaultScalingFactor = 1;

      $scope.newGroup = function () {
          $scope.currentGroup = null;
      };

      $scope.setCurrentGroup = function (group) {
          $scope.currentGroup = group;
          // Fill in ng-models for easy edit
          $scope.groupName = group.name;
          $scope.groupService = $scope.allServices[group.serviceId];
          $scope.groupEnvironment = $scope.allEnvironments[group.environmentId];
          $scope.groupJsonParams = group.jsonParams;
          $scope.groupScalingFactor = group.scalingFactor;
      };

      $scope.deleteGroup = function () {
          apolloApiService.deleteGroup($scope.currentGroup.id).then(function (response) {
              usSpinnerService.stop('group-spinner');
              growl.success("Successfully deleted group " + $scope.groupName + "!");
              updateGroups();
          }, function (error) {
              usSpinnerService.stop('group-spinner');
              growl.error("Could not delete group! got: " + error.statusText)
          });

          updateGroups();
      };

      $scope.saveGroup= function () {

          var serviceId = null;
          var environmentId = null;
          var groupName = $scope.groupName;
          var jsonParams = $scope.groupJsonParams;

          if ($scope.groupService) {
              serviceId = $scope.groupService.id;
          }

          if ($scope.groupEnvironment) {
              environmentId = $scope.groupEnvironment.id;
          }

          if (!$scope.currentGroup) {
              apolloApiService.createGroup(groupName, serviceId, environmentId, defaultScalingFactor, jsonParams).then(function (response) {
                  usSpinnerService.stop('group-spinner');
                  growl.success("Successfully added group " + groupName + "!");
                  updateGroups();
              }, function (error) {
                  usSpinnerService.stop('group-spinner');
                  growl.error("Could not add group! got: " + error.statusText)
              });
          } else {
              apolloApiService.updateGroup($scope.currentGroup.id, groupName, serviceId, environmentId, $scope.currentGroup.scalingFactor, jsonParams).then(function (response) {
                  usSpinnerService.stop('group-spinner');
                  growl.success("Successfully updated group " + groupName + "!");
                  updateGroups();
              }, function (error) {
                  usSpinnerService.stop('group-spinner');
                  growl.error("Could not update group! got: " + error.statusText)
              });
          }
      };

      $scope.changeScalingFactor = function () {
        apolloApiService.updateScalingFactorForGroup($scope.currentGroup.id, $scope.newScalingFactor).then(function(response) {
            usSpinnerService.stop('group-spinner');
            growl.success("Successfully updated scaling factor of " + $scope.groupName + " to " + $scope.newScalingFactor);
            updateGroups();
        }, function (error) {
            usSpinnerService.stop('group-spinner');
            growl.error("Could not update scaling factor! got: " + error.statusText)
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

      function updateGroups() {
          apolloApiService.getAllGroups().then(function(response) {
              var tempGroups = {};
              response.data.forEach(function(group) {
                  tempGroups[group.id] = group;
              });

              $scope.allGroups = tempGroups;
              console.log()
          });
      }

      updateGroups();
}]);