'use strict';


var app = angular.module('app', [
   'ui.router',
   'controllers',
   'jsTree.directive',
   'ui.ace', 'ui.bootstrap','ng.jsoneditor','schemaForm','ngTablescroll'
]);


app.service('TaskService', [
  
	function(){
    
		this.sayHello = function() { return "Hello, World!"; };
	
  }]);

  
  app.service('DataService', [
  
	function(){
    
		this.sayHello = function() { return "Hello, World!"; };
	
  }]);

  
  app.service('RepositoryService', [
  
	function(){
    
		this.sayHello = function() { return "Hello, World!"; };
	
  }]);
  
   app.factory('Tasks', ['$interval', '$http',
  
	function($interval,$http){
	
		var tasks = {};
    
		function getTasks() 
		{
			$http.get('/api/compute/tasks').
				success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					
					tasks = data;
					

				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get tasks!");

				}
			);
			
			console.log(tasks);
		};
		
		$interval(getTasks, 1000);
		
		return function() {
			return tasks;
		}
  }]);

  
  app.factory('ClusterNodes', ['$interval', '$http',
  
	function($interval,$http){
	
		var nodeStates = {};
    
		function getNodeStates() 
		{
			$http.get('/api/cluster/nodes').
				success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					
					nodeStates = data;
					

				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get clusterInfo!");

				}
			);
			
			console.log(nodeStates);
		};
		
		$interval(getNodeStates, 1000);
		
		return function() {
			return nodeStates;
		}
  }]);
  
  app.factory('Caches', ['$interval', '$http',
  
	function($interval,$http){
	
		var caches = {};
    
		function getCaches() 
		{
			$http.get('/api/cluster/caches').
				success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					
					caches = data;
					

				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get tasks!");

				}
			);
			
			console.log(caches);
		};
		
		$interval(getCaches, 10000);
		
		return function() {
			return caches;
		}
  }]);

  
  app.factory('DeployedObjects', ['$interval', '$http',
  
	function($interval,$http){
	
		var objects = {};
    
		function getDeployedObjects() 
		{
			$http.get('/api/deployments/all').
				success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					
					objects = data;
					

				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get tasks!");

				}
			);
			
			console.log(objects);
		};
		
		$interval(getDeployedObjects, 10000);
		
		return function() {
			return objects;
		}
  }]);
