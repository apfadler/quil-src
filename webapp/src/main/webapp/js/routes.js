/*app.config(['$routeProvider',
            function ($routeProvider) {
               $routeProvider
                      .when('/', {
                              templateUrl: 'partials/dashboard.html',
                              controller: 'DashboardController'
                           })
					  .when('/data', {
                              templateUrl: 'partials/data_management.html',
                              controller: 'DataController'
                           })
					  .when('/tasks', {
                              templateUrl: 'partials/task_management.html',
                              controller: 'TaskController'
                           })
					  .when('/cluster', {
                              templateUrl: 'partials/cluster_status.html',
                              controller: 'ClusterController'
                           })
					   .when('/repository', {
                              templateUrl: 'partials/repository.html',
                              controller: 'RepositoryController'
                           })
                     .otherwise({
                                   redirectTo: '/'
                                });
            }]);*/
			
			
app.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider) {
	$urlRouterProvider.otherwise('/');

    $stateProvider
        .state('home', {
            url:'/',
			views : {
				'content' : { 
					templateUrl: 'partials/dashboard.html',
					controller: 'DashboardController'
				}
			}
        })
		.state('data', {
            url:'/data',
			
			views : {
				'content' : { 
					templateUrl: 'partials/data_management.html',
					controller: 'DataController'
				}
			}
        })
		.state('tasks', {
            url:'/tasks',
			abstract:true,
			views  : {
			
				'content' : {
					templateUrl: 'partials/task_management.html',
					controller: 'TaskController'
				}
			
			}

        })
		.state('tasks.main', {
            url:'/main',
            views  : {
			
				'task_forms@tasks' : {
					templateUrl: 'partials/task_actions.html',
					controller: 'TaskActionController',
				}
				
				
			}

        })
		.state('tasks.define', {
            url:'/define',
            views  : {
			
				'task_forms@tasks' : {
					templateUrl: 'partials/task_definition.html',
					controller: 'TaskDefinitionController',
				}
				
				
			}

        })
		.state('cluster', {
            url:'/cluster',
			views  : {
			
				'content' : {
					templateUrl: 'partials/cluster_status.html',
					controller: 'ClusterController'
				}
			}
        })
		.state('repository', {
            url:'/repository',
			views  : {
			
				'content' : {		
					templateUrl: 'partials/repository.html',
					controller: 'RepositoryController'
				}
			}
        })
		
		.state('objects', {
            url:'/objects',
			views  : {
			
				'content' : {		
					templateUrl: 'partials/deployed_objects.html',
					controller: 'ObjectsController'
				}
			}
        })
        
 
}]);