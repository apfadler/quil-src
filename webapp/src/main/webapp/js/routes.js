
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
		.state('repository', {
            url:'/repository',
			views  : {
			
				'content' : {		
					templateUrl: 'partials/repository.html',
					controller: 'RepositoryController'
				}
			}
        })
}]);