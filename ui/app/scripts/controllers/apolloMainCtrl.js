angular.module('apollo')
  .controller('apolloMainCtrl', ['$scope', '$state', 'hotkeys', function ($scope, $state, hotkeys) {

      hotkeys.add({
          combo: 'ctrl+n',
          description: "New Deployment",
          callback: function () {
              $state.go('deployments.new')
          }
      });
      hotkeys.add({
          combo: 'ctrl+o',
          description: "Ongoing Deployments",
          callback: function () {
              $state.go('deployments.ongoing')
          }
      });
      hotkeys.add({
          combo: 'ctrl+h',
          description: "Deployment History",
          callback: function () {
              $state.go('deployments.history')
          }
      });
      hotkeys.add({
          combo: 'ctrl+v',
          description: "Service Versions",
          callback: function () {
              $state.go('service.status')
          }
      });
}]);