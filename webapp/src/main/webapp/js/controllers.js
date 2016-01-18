'use strict';

var controllers = angular.module("controllers", []);

controllers.controller("DashboardController", ['$scope', function ($scope) {
   $scope.title = 'Hello world!';
}]);


controllers.controller("DataController", ['$scope', function ($scope) {
   $scope.title = 'Hello world!';
   
   $scope.dataServices = [
	{ id : "Name", "type" : "Document Cache", size : 36 }
   ]
   
}]);

controllers.controller("TaskController", ['$scope', function ($scope) {
   $scope.title = 'Hello world!';
   
   $scope.tasks = [
	{ name : "Name", status : "Running" , result : ""}
   ]
}]);

controllers.controller("ClusterController", ['$scope', function ($scope) {
   $scope.title = 'Hello world!';
   
   
    $scope.clusterNodes = [
	{ id : "Name", cpu : 30.0 , mem : 100.0, nocores : 4, activeJobs : 23 }
   ]
}]);