'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('apollo')
	.directive('headerNotification', ['localStorageService', function(localStorageService){
		return {
			templateUrl:'scripts/directives/header/header-notification/header-notification.html',
			restrict: 'E',
			replace: true,
			controller: function($scope, $state) {

					$scope.email = localStorageService.get("email");

					$scope.logout = function() {

						localStorageService.clearAll();
						$state.go('login');
					}
			}
		}
	}]);


