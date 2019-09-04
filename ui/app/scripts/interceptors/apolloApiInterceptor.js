'use strict';
angular
  .module('apollo')
  .factory('apolloApiInterceptor', ['$log', '$location', '$q', 'localStorageService', 'growl', function($log, $location, $q, localStorageService, growl) {

    var apolloInterceptor = {

        request: function(config) {
            if(config.url.startsWith(CONFIG.appUrl)) {

                if(!config.url.endsWith("login/")) {

                    if(localStorageService.get('token') != undefined) {
                        config.headers['Authorization'] = "Token " + localStorageService.get("token");
                    }
                    else {
                        // Redirect to login page
                        $location.path("/login");
                    }
                }
            }
            return config;
        },

        response: function(response) {

            if(response.config.url.startsWith(CONFIG.appUrl)) {
                if (response.status == 401) {
                    $location.path("/login");
                }
            }

            return response;
        },

        responseError: function (responseError) {
            if(responseError.config.url.startsWith(CONFIG.appUrl)) {
                if (responseError.status == 403) {
                    growl.error("Got Forbidden! You either do not have permissions, or your session has died. Try to sign in again. <br>" + responseError.data.error, {ttl: 7000});
                }
            }

            return $q.reject(responseError);
        }
    };

    return apolloInterceptor;
}]);