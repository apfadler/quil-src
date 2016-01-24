app.config(['$routeProvider',
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
            }]);