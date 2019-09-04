'use strict';

angular.module('apollo')
    .controller('configureServiceCtrl', ['apolloApiService', '$scope', '$timeout' , '$state', 'growl',
        function (apolloApiService, $scope, $timeout, $state, growl) {
            $scope.currentStep = 'choose-service';

            $scope.editorOptions = {
                lineWrapping : true,
                lineNumbers: true,
                mode: 'yaml',
                theme: 'material'
            };

            apolloApiService.getAllServices().then(function(response) {
                $scope.allServices = response.data;
            });

            $scope.setSelectedService = function (selectedService) {
                $scope.selectedService = selectedService;
                $scope.buttonDisabled = false;
            };

            $scope.newService = function () {
                $scope.selectedService = {};
                $scope.nextStep();
            };

            // Visual change the next step
            $scope.nextStep = function() {
                if ($scope.selectedService) {
                    $scope.currentStep = 'configure';
                } else {
                    growl.error("You must select a service or create a new one!");
                }
            };

            var handleSuccess = function() {
                growl.success("Successfully configured service!", {ttl:7000});
                $scope.buttonDisabled = true;
                $scope.selectedService = null;
                $scope.currentStep = 'choose-service';
            };

            var handleError = function() {
                growl.success("Successfully configured service!", {ttl:7000});
                $scope.buttonDisabled = true;
                $scope.selectedService = null;
                $scope.currentStep = 'choose-service';
            };

            $scope.save = function() {
                var service = $scope.selectedService;
                if (service.id) {
                    apolloApiService.updateService(service.id, service.name, service.deploymentYaml, service.serviceYaml, service.ingressYaml, service.isPartOfGroup).then(handleSuccess, handleError);
                } else {
                    apolloApiService.createService(service.name, service.deploymentYaml, service.serviceYaml, service.ingressYaml, service.isPartOfGroup).then(handleSuccess, handleError);
                }
            };
        }]);