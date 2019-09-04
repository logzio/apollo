'use strict';

angular.module('apollo')
  .controller('loginCtrl', ['apolloApiService', '$scope',
                                    '$timeout' , '$state', 'growl', 'usSpinnerService', 'localStorageService',
            function (apolloApiService, $scope, $timeout, $state, growl, usSpinnerService, localStorageService) {
                $scope.login = function() {
                    apolloApiService.login($scope.email, $scope.password).then(
                    function(response){
                        localStorageService.set("token", response.data.token);
                        localStorageService.set("email", $scope.email);
                        $state.go("deployments.home");
                    },
                    function(error){
                        growl.error("Email or Password are invalid..")
                    })
                }
            }]);