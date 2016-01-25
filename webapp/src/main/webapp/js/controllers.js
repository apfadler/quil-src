'use strict';

var controllers = angular.module("controllers", []);

controllers.controller("MainController", ['$scope', '$interval', 'ClusterNodes', 'Tasks', 'Caches',
	
	function ($scope, $interval, ClusterNodes, Tasks, Caches) {
	
		$scope.clusterNodes = [];
		$scope.tasks = [];
		$scope.dataServices = [];

		$interval( function() {
						$scope.clusterNodes = ClusterNodes(); 
				   }, 100);
				   
		$interval( function() {
						$scope.tasks = Tasks(); 
				   }, 100);
		$interval( function() {
						$scope.dataServices = Caches(); 
				   }, 100);
	}
   
]);

controllers.controller("DashboardController", ['$scope',
	function ($scope) {
	
	}]);


controllers.controller("DataController", ['$scope', function ($scope) {
   
   
   
}]);

controllers.controller("TaskController", ['$scope', '$http','$uibModal',  function ($scope, $http, $uibModal) {

	$scope.templateID = "/Template.Task.json"
	$scope.taskID = "/Task.PriceSingleTradeMoCo.json"
	
	$scope.alerts = [];

	$scope.taskFromRepository = function() {
		 $http.get('/api/repository/files'+$scope.taskID)
					.success(function(data, status, headers, config) {
						
						
						if ( data.hasOwnProperty("fileData")) {
						
							$scope.submitTask(data.fileData);
						
						 } else {
						 
							$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Task submission failed: ' + data.Msg, type : 'danger'});
							
						}
					})
					.error(function(data, status, headers, config) {
						 $scope.alerts.push({msg: 'Task submission failed: ' + status , type : 'danger'});
					});
	}

	$scope.taskFromTemplate = function() {
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "taskFromTemplateDialog.html",
		  controller: 'taskFromTemplateDialogCtrl',
		  size: 'sm',
		  resolve: {
			templateID: function () {
			  return $scope.templateID;
			},
			
			http: function() {
				return $http;
				}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
			
		}, function () {
		 
		});
	}
	
	$scope.taskFromCachedTask = function() {
	}
	
	$scope.submitTask = function(taskDescription) {
		$http.post('/api/compute/task/submit', taskDescription)
					.success(function(data, status, headers, config) {
						console.log(data);
						
						 if (data.Status == "SUCCESS") {
							$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Task submitted!', type : 'success'});
						 } else {
							$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Task submission failed: ' + Status.Msg, type : 'danger'});
						 }
					})
					.error(function(data, status, headers, config) {
						alert(status);
						console.log("Failed to get file!");
					});
	}

}]);

controllers.controller("ClusterController", ['$scope',

	function ($scope) {
	
	}
	
]);

controllers.controller("RepositoryController", ['$scope', '$http' , '$uibModal', function ($scope, $http, $uibModal) {

    $scope.treeModel = [];
	$scope.fileContent = "";
	$scope.currentFile = "New File *";
	
	$scope.nodeSelected = function(e, eventData) {
	
	    if (eventData.node.original.type=="file") {
		
		  $scope.currentFile = eventData.node.id;
		
		    
		  $http.get('/api/repository/files'+eventData.node.id)
					.success(function(data, status, headers, config) {
						$scope.fileContent = data.fileData;
					})
					.error(function(data, status, headers, config) {
					
						console.log("Failed to get file!");
					});
        }
		
      };
	  
	$scope.saveFile = function() {
	
	    $http.post("/api/repository/files"+ $scope.currentFile + "/put", $scope.fileContent).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.loadRepository();

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			});
		
      };
	  
	$scope.newFileWindow = function (template, fileName, base) {

		if (base == "Repository") base = "/";
	
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "newFileDialog.html",
		  controller: 'newFileDialogCtrl',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		  $http.post("/api/repository/files/"+ base + selectedItem + "/put").
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.loadRepository();

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			});
			
		}, function () {
		 
		});
	};
	
	$scope.deleteFileWindow = function (template, fileName, base) {

		if (base == "Repository") base = "/";
	
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "deleteFileDialog.html",
		  controller: 'deleteFileDialogCtrl',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		  $http.post("/api/repository/files/"+ $scope.currentFile + "/delete").
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.loadRepository();
				$scope.fileContent = "";
				$scope.currentFile = "New File *";
				

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			});
			
		}, function () {
		
		});
	};
	
	$scope.uploadFileWindow = function () {

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "uploadFileDialog.html",
		  controller: 'uploadFileDialogCtrl',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return "asd";
			},
			
			tree: function () {
			
			  return $scope.treeModel;
			}
			
			
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		 
			
		}, function () {
		
		});
	};
	
	$scope.newFolderWindow = function (template) {

		if (base == "Repository") base = "/";
	
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "taskFromTemplateDialog.html",
		  controller: 'taskFromTemplateDialogCtrl',
		  size: 'sm',
		  resolve: {
			templateID: function () {
			  return folderName;
			}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		  $http.post("/api/repository/folders/"+ base + selectedItem + "/put").
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.loadRepository();

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			});
			
		}, function () {
		 
		});
	};
	
	$scope.uploadToCacheWindow = function () {

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "uploadToCacheDialog.html",
		  controller: 'uploadToCacheDialogCtrl',
		  size: 'sm',
		  resolve: {
			cacheKey: function () {
			  return $scope.currentFile;
			}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
			
		}, function () {
		 
		});
	};
						
	$scope.loadRepository = function() { $http.get('/api/repository/content').
				success(function(data, status, headers, config) {
					// this callback will be called asynchronously
					// when the response is available
					
					$scope.treeModel  = data.children;
					

				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get repo!");

				}
			);
	}
	
	
	$scope.loadRepository();
}]);




controllers.controller('newFileDialogCtrl', function ($scope, $uibModalInstance, newFileName) {

  $scope.fileName = newFileName;

  $scope.ok = function () {
    $uibModalInstance.close($scope.fileName);
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
});


controllers.controller('newFolderDialogCtrl', function ($scope, $uibModalInstance, newFolderName) {

  $scope.folderName = newFolderName;

  $scope.ok = function () {
    $uibModalInstance.close($scope.folderName);
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
});


controllers.controller('deleteFileDialogCtrl', function ($scope, $uibModalInstance, newFileName) {

  $scope.ok = function () {
    $uibModalInstance.close('ok');
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
});

controllers.controller('uploadFileDialogCtrl',function ($scope, $uibModalInstance, newFileName,tree) {

  $scope.theTree = tree;
 
  $scope.ok = function () {
    $uibModalInstance.close('ok');
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
  

});

controllers.controller('uploadToCacheDialogCtrl',function ($scope, $uibModalInstance, cacheKey) {

  $scope.cacheKey = cacheKey;
 
  $scope.ok = function () {
    $uibModalInstance.close('ok');
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
  

});


controllers.controller('taskFromTemplateDialogCtrl',function ($scope, $uibModalInstance, templateID, http) {

  $scope.templateID = templateID;
  
  $scope.obj = {data: {bla:"bla"}, options: { mode: 'tree' }};
  
   $scope.schema = {};
  
  http.get('/api/repository/files'+templateID)
					.success(function(data, status, headers, config) {
					
						console.log(data);

						$scope.schema = JSON.parse(data.fileData);
					})
					.error(function(data, status, headers, config) {
						alert(status);
						console.log("Failed to get file!");
					});
  
 

  $scope.form = [
    "*",
    {
      
    }
  ];

  $scope.model = {};

  $scope.btnClick = function() {
    $scope.obj.options.mode = 'code'; //should switch you to code view
  }
 
  $scope.ok = function () {
    $uibModalInstance.close('ok');
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
  

});
