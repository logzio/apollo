'use strict';
/**
 * @ngdoc function
 * @name apollo.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the apollo
 */
angular.module('apollo')
  .controller('ChartCtrl', ['$scope', '$timeout', function ($scope, $timeout) {

    $scope.ram = {
    	labels: ["Used RAM", "Free RAM"],
    	data: [1024, 4096]
    };

	$scope.cpu = {
        	labels: ["Used Shares", "Free Shares"],
        	data: [30, 51]
    };
}]);