'use strict';

angular.module('apollo')
  .controller('signupCtrl', ['apolloApiService', '$scope',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService',
            function (apolloApiService, $scope, $timeout, $state, growl, usSpinnerService) {

            apolloApiService.getAllDeploymentRoles().then(
            function(response) {
                $scope.roles = response.data;
            });

            $scope.signup = function() {
                apolloApiService.signup($scope.userEmail, $scope.firstName, $scope.lastName, $scope.password).then(
                function(response){
                    addRoleToUser($scope.userEmail, $scope.role.id);
                },
                function(error){
                    growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000});
                })
            }

            var addRoleToUser = function(userEmail, roleId) {
                apolloApiService.addUserToDeploymentRole(userEmail, roleId).then(
                function(response) {
                    growl.success("Successfully created new user!", {ttl:7000});
                    $scope.buttonDisabled = true;
                },
                function(error) {
                    console.log("error!");
                    growl.error("Got from apollo API: " + error.status + " (" + error.statusText + ")", {ttl: 7000});
                });
            }
}]);