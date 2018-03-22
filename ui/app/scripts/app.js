'use strict';
/**
 * @ngdoc overview
 * @name apollo
 * @description
 * # apollo
 *
 * Main module of the application.
 */
angular
  .module('apollo', [
    'oc.lazyLoad',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
    'ngAnimate',
    'angular-growl',
    'angularSpinner',
    'ngSanitize',
    'angular.filter',
    'LocalStorageModule',
    'ui.gravatar',
    'ui.codemirror',
    'datatables',
    'cfp.hotkeys'
  ])
  .config(['$stateProvider','$urlRouterProvider','$ocLazyLoadProvider',function ($stateProvider,$urlRouterProvider,$ocLazyLoadProvider) {
    
    $ocLazyLoadProvider.config({
      debug:false,
      events:true
    });

    $urlRouterProvider.otherwise('/deployments/home');

    $stateProvider
      .state('deployments', {
        url:'/deployments',
        templateUrl: 'views/deployments/main.html',
        resolve: {
            loadMyDirectives:function($ocLazyLoad){
                return $ocLazyLoad.load(
                {
                    name:'apollo',
                    files:[
                    'scripts/directives/header/header.js',
                    'scripts/directives/header/header-notification/header-notification.js',
                    'scripts/directives/sidebar/sidebar.js',
                    'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                    ]
                }),
                $ocLazyLoad.load(
                {
                   name:'toggle-switch',
                   files:["bower_components/angular-toggle-switch/angular-toggle-switch.min.js",
                          "bower_components/angular-toggle-switch/angular-toggle-switch.css"
                      ]
                }),
                $ocLazyLoad.load(
                {
                  name:'ngAnimate',
                  files:['bower_components/angular-animate/angular-animate.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngCookies',
                  files:['bower_components/angular-cookies/angular-cookies.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngResource',
                  files:['bower_components/angular-resource/angular-resource.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngSanitize',
                  files:['bower_components/angular-sanitize/angular-sanitize.js']
                }),
                $ocLazyLoad.load(
                {
                  name:'ngTouch',
                  files:['bower_components/angular-touch/angular-touch.js']
                })
            }
        }
    })
      .state('deployments.home',{
        url:'/home',
        templateUrl:'views/deployments/home.html',
        resolve: {
          loadMyFiles:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'apollo',
              files:[
              'scripts/controllers/main.js',
              'scripts/controllers/chartContoller.js',
              'scripts/directives/notifications/notifications.js',
              'scripts/directives/dashboard/stats/stats.js'
              ]
            }),
            $ocLazyLoad.load({
              name:'chart.js',
              files:[
                'bower_components/angular-chart.js/dist/angular-chart.min.js',
                'bower_components/angular-chart.js/dist/angular-chart.css'
              ]
            }),
            $ocLazyLoad.load({
              name:'ngAnimate',
              files:['bower_components/angular-animate/angular-animate.js']
            })
          }
        }
      })
      .state('deployments.new',{
        templateUrl:'views/deployments/new.html',
        controller: 'newDeploymentCtrl',
        url:'/new',
        resolve: {
          loadMyFiles:function($ocLazyLoad) {
            return $ocLazyLoad.load({
              name:'apollo',
              files:[
              'scripts/services/apolloApiService.js',
              'scripts/controllers/newDeploymentCtrl.js'
              ]
            })
          }
        }
    })
      .state('deployments.ongoing',{
        templateUrl:'views/deployments/ongoing.html',
        controller: 'ongoingDeploymentCtrl',
        url:'/ongoing?deploymentId',
        resolve: {
                  loadMyFiles:function($ocLazyLoad) {
                    return $ocLazyLoad.load({
                      name:'apollo',
                      files:[
                      'scripts/services/apolloApiService.js',
                      'scripts/controllers/ongoingDeploymentCtrl.js'
                      ]
                    })
                  }
                },
        params: {
            deploymentResult: null
        }
    })
      .state('deployments.history',{
            templateUrl:'views/deployments/history.html',
            controller: 'deploymentHistoryCtrl',
            url:'/history',
            resolve: {
                      loadMyFiles:function($ocLazyLoad) {
                        return $ocLazyLoad.load({
                          name:'apollo',
                          files:[
                          'scripts/services/apolloApiService.js',
                          'scripts/controllers/deploymentHistoryCtrl.js'
                          ]
                        })
                      }
                    }
    });

    $stateProvider
          .state('service', {
            url:'/service',
            templateUrl: 'views/service/main.html',
            resolve: {
                loadMyDirectives:function($ocLazyLoad){
                    return $ocLazyLoad.load(
                    {
                        name:'apollo',
                        files:[
                        'scripts/directives/header/header.js',
                        'scripts/directives/header/header-notification/header-notification.js',
                        'scripts/directives/sidebar/sidebar.js',
                        'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                        ]
                    }),
                    $ocLazyLoad.load(
                    {
                       name:'toggle-switch',
                       files:["bower_components/angular-toggle-switch/angular-toggle-switch.min.js",
                              "bower_components/angular-toggle-switch/angular-toggle-switch.css"
                          ]
                    }),
                    $ocLazyLoad.load(
                    {
                      name:'ngAnimate',
                      files:['bower_components/angular-animate/angular-animate.js']
                    }),
                    $ocLazyLoad.load(
                    {
                      name:'ngCookies',
                      files:['bower_components/angular-cookies/angular-cookies.js']
                    }),
                    $ocLazyLoad.load(
                    {
                      name:'ngResource',
                      files:['bower_components/angular-resource/angular-resource.js']
                    }),
                    $ocLazyLoad.load(
                    {
                      name:'ngSanitize',
                      files:['bower_components/angular-sanitize/angular-sanitize.js']
                    }),
                    $ocLazyLoad.load(
                    {
                      name:'ngTouch',
                      files:['bower_components/angular-touch/angular-touch.js']
                    })
                }
            }
        })
      .state('service.status',{
            templateUrl:'views/service/status.html',
            controller: 'serviceVersionStatusCtrl',
            url:'/status',
            resolve: {
                      loadMyFiles:function($ocLazyLoad) {
                        return $ocLazyLoad.load({
                          name:'apollo',
                          files:[
                          'scripts/services/apolloApiService.js',
                          'scripts/controllers/serviceVersionStatusCtrl.js'
                          ]
                        })
                      }
                    }
    })
      .state('service.configure',{
            templateUrl:'views/service/configure.html',
            controller: 'configureServiceCtrl',
            url:'/configure',
            resolve: {
              loadMyFiles:function($ocLazyLoad) {
                  return $ocLazyLoad.load({
                      name:'apollo',
                      files:[
                          'scripts/services/apolloApiService.js',
                          'scripts/controllers/configureServiceCtrl.js'
                      ]
                  })
              }
          }
    });

     $stateProvider
           .state('blocker', {
             url:'/blocker',
             templateUrl: 'views/blocker/main.html',
             resolve: {
                 loadMyDirectives:function($ocLazyLoad){
                     return $ocLazyLoad.load(
                     {
                         name:'apollo',
                         files:[
                         'scripts/directives/header/header.js',
                         'scripts/directives/header/header-notification/header-notification.js',
                         'scripts/directives/sidebar/sidebar.js',
                         'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                         ]
                     }),
                     $ocLazyLoad.load(
                     {
                        name:'toggle-switch',
                        files:["bower_components/angular-toggle-switch/angular-toggle-switch.min.js",
                               "bower_components/angular-toggle-switch/angular-toggle-switch.css"
                           ]
                     }),
                     $ocLazyLoad.load(
                     {
                       name:'ngAnimate',
                       files:['bower_components/angular-animate/angular-animate.js']
                     }),
                     $ocLazyLoad.load(
                     {
                       name:'ngCookies',
                       files:['bower_components/angular-cookies/angular-cookies.js']
                     }),
                     $ocLazyLoad.load(
                     {
                       name:'ngResource',
                       files:['bower_components/angular-resource/angular-resource.js']
                     }),
                     $ocLazyLoad.load(
                     {
                       name:'ngSanitize',
                       files:['bower_components/angular-sanitize/angular-sanitize.js']
                     }),
                     $ocLazyLoad.load(
                     {
                       name:'ngTouch',
                       files:['bower_components/angular-touch/angular-touch.js']
                     })
                 }
             }
         })
      .state('blocker.configure',{
            templateUrl:'views/blocker/configure.html',
            controller: 'blockersCtrl',
            url:'/configure',
            resolve: {
                      loadMyFiles:function($ocLazyLoad) {
                        return $ocLazyLoad.load({
                          name:'apollo',
                          files:[
                          'scripts/services/apolloApiService.js',
                          'scripts/controllers/blockersCtrl.js'
                          ]
                        })
                      }
                    }
    });
    $stateProvider
               .state('auth', {
                 url:'/auth',
                 templateUrl: 'views/auth/main.html',
                 resolve: {
                     loadMyDirectives:function($ocLazyLoad){
                         return $ocLazyLoad.load(
                         {
                             name:'apollo',
                             files:[
                             'scripts/directives/header/header.js',
                             'scripts/directives/header/header-notification/header-notification.js',
                             'scripts/directives/sidebar/sidebar.js',
                             'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                             ]
                         }),
                         $ocLazyLoad.load(
                         {
                            name:'toggle-switch',
                            files:["bower_components/angular-toggle-switch/angular-toggle-switch.min.js",
                                   "bower_components/angular-toggle-switch/angular-toggle-switch.css"
                               ]
                         }),
                         $ocLazyLoad.load(
                         {
                           name:'ngAnimate',
                           files:['bower_components/angular-animate/angular-animate.js']
                         }),
                         $ocLazyLoad.load(
                         {
                           name:'ngCookies',
                           files:['bower_components/angular-cookies/angular-cookies.js']
                         }),
                         $ocLazyLoad.load(
                         {
                           name:'ngResource',
                           files:['bower_components/angular-resource/angular-resource.js']
                         }),
                         $ocLazyLoad.load(
                         {
                           name:'ngSanitize',
                           files:['bower_components/angular-sanitize/angular-sanitize.js']
                         }),
                         $ocLazyLoad.load(
                         {
                           name:'ngTouch',
                           files:['bower_components/angular-touch/angular-touch.js']
                         })
                     }
                 }
             })
          .state('auth.signup',{
                templateUrl:'views/auth/signup.html',
                url:'/signup',
                controller: 'signupCtrl',
                resolve: {
                          loadMyFiles:function($ocLazyLoad) {
                            return $ocLazyLoad.load({
                              name:'apollo',
                              files:[
                              'scripts/services/apolloApiService.js',
                              'scripts/controllers/signupCtrl.js'
                              ]
                            })
                          }
                        }
        })
      .state('login',{
        templateUrl:'views/auth/login.html',
        url:'/login',
        controller: 'loginCtrl',
        resolve: {
                  loadMyFiles:function($ocLazyLoad) {
                    return $ocLazyLoad.load({
                      name:'apollo',
                      files:[
                      'scripts/services/apolloApiService.js',
                      'scripts/controllers/loginCtrl.js'
                      ]
                    })
                  }
                }
    })

  }]);

angular
  .module('apollo')
  .config(['growlProvider', function(growlProvider) {
    growlProvider.globalTimeToLive(3000);
    growlProvider.globalReversedOrder(true);
  }]);

angular
  .module('apollo')
  .config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('apolloApiInterceptor');
  }]);

angular
  .module('apollo')
  .config(function (localStorageServiceProvider) {
    localStorageServiceProvider
      .setPrefix('apollo');
  });