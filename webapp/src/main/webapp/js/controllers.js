'use strict';

var controllers = angular.module("controllers", []);

//for file upload
controllers.directive("fileread", [function () {
    return {
        scope: {
            fileread: "="
        },
        link: function (scope, element, attributes) {
            element.bind("change", function (changeEvent) {
                var reader = new FileReader();
                reader.onload = function (loadEvent) {
                    scope.$apply(function () {
                        scope.fileread = loadEvent.target.result;
                    });
                }
                reader.readAsText(changeEvent.target.files[0]);
            });
        }
    }
}]);

controllers.controller("MainController", ['$scope', '$interval', 'ClusterNodes', 'Tasks', 'Caches','DeployedObjects',function ($scope, $interval, ClusterNodes, Tasks, Caches, DeployedObjects) {

	$scope.clusterNodes = [];
	$scope.tasks = [];
	$scope.failedTasks = [];
	$scope.runningTasks = [];
	$scope.dataServices = [];
	$scope.deployedObjects = [];

	$interval( function() {
		$scope.clusterNodes = ClusterNodes(); 
	}, 100);

	
	$scope.updateTasks = function() {
		
		for (var i=0; i < $scope.tasks.length; i++)
		{
			if ($scope.tasks[i].status == 1) {
				$scope.runningTasks.push($scope.tasks[i]);
				$scope.tasks[i].status_text = "Running";
			}
			if ($scope.tasks[i].status == 2) {
				$scope.tasks[i].status_text = "Finished";
				var found = false;
				for (var j=0; j < $scope.finishedTasks.length; j++) {
					if ($scope.tasks[i].name == $scope.finishedTasks[j].name )
						found = true;
				}
				if (!found)
					$scope.finishedTasks.push($scope.tasks[i]);
			}
			if ($scope.tasks[i].status == 3) {
				$scope.failedTasks.push($scope.tasks[i]);
				$scope.tasks[i].status_text = "Failed";
				
				var found = false;
				for (var j=0; j < $scope.finishedTasks.length; j++) {
					if ($scope.tasks[i].name == $scope.finishedTasks[j].name )
						found = true;
				}
				if (!found)
					$scope.finishedTasks.push($scope.tasks[i]);
			}

			if ($scope.tasks[i].result == "")
				$scope.tasks[i].result = "{}";
			
			try {
				$scope.tasks[i].result = JSON.parse($scope.tasks[i].result);
			}
			catch(e) {
				//$scope.tasks[i].result = {};
			}

		}
	};
	
	$interval( function() {
		$scope.tasks = Tasks(); 
		$scope.failedTasks = [];
		$scope.runningTasks = [];


		$scope.updateTasks();

	}, 100);
	
	$interval( function() {
		$scope.dataServices = Caches(); 
	}, 100);

	$interval( function() {
		$scope.deployedObjects = DeployedObjects(); 

		for (var i=0; i < $scope.deployedObjects.length; i++)
		{
			var cacheType = "simplecache";

			for (var j=0; j < $scope.dataServices.length; j++) {
				if ($scope.dataServices[j].name == $scope.deployedObjects[i].cacheId ) {
					if ($scope.dataServices[j].type.indexOf("Doc") != -1) {
						cacheType ="documentcache";
					}
				}

			}

			$scope.deployedObjects[i].url = "/api/" + cacheType + "/"+$scope.deployedObjects[i].cacheId+"/get/" + $scope.deployedObjects[i].fileId;
		};

	}, 100);

	$scope.logTxt = "";
	var logEvents = new EventSource('/api/log/stream');

	logEvents.addEventListener("message", function(event) {
		$scope.logTxt += event.data;

	});
	
	$scope.tasks = Tasks(); 
	$scope.finishedTasks = [];
	$scope.updateTasks();
	
}]);

controllers.controller("DashboardController", ['$scope',function ($scope) {
	
	$scope.aceLoaded = function(_editor) {
		_editor.setReadOnly(true);

		$scope.ace = _editor;
	};

	$scope.aceChanged = function(e) {

		$scope.ace.navigateFileEnd();
	};
	
}]);

controllers.controller("DataController", ['$scope', '$http', function ($scope, $http) {
   
   $scope.newDocumentCacheID = "";
   $scope.newSimpleCacheID = "";
   $scope.newCSVCacheID = "";
   $scope.genericCSVfileContent = "";
   
   $scope.uploadGenericCSVFile = function() {
	   
	   var content = $scope.genericCSVfileContent;
	   console.log(content);
	   
	   $http({method : "POST",
		      url : "/api/documentcache/"+ $scope.newCSVCacheID + "/addFromCSV",
		      data : content,
		      headers : {'Content-Type' : 'text/plain'}
	   }).
		success(function(data, status, headers, config) {
			console.log('post success');
			
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'File uploaded.', type : 'success'});

		}).
		error(function(data, status, headers, config) {
			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Error uploading file', type : 'danger'});
		});
   };
   
   
   $scope.uploadJSONFile = function () {
	   var content = $scope.jsonFile;
	   console.log(content);
	   
	   $http({method : "POST",
		      url : "/api/documentcache/"+ $scope.newDocumentCacheID + "/addJSONObject",
		      data : content,
		      headers : {'Content-Type' : 'text/plain'}
	   }).
		success(function(data, status, headers, config) {
			console.log('post success');
			
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'File uploaded.', type : 'success'});

		}).
		error(function(data, status, headers, config) {
			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Error uploading file', type : 'danger'});
		});
   };
   
   $scope.uploadKeyValueCSVFile = function() {
	   var content = $scope.keyValueCSVFile;
	   console.log(content);
	   
	   $http({method : "POST",
		      url : "/api/simplecache/"+ $scope.newSimpleCacheID + "/addFromCSV",
		      data : content,
		      headers : {'Content-Type' : 'text/plain'}
	   }).
		success(function(data, status, headers, config) {
			console.log('post success');
			
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'File uploaded.', type : 'success'});

		}).
		error(function(data, status, headers, config) {
			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Error uploading file', type : 'danger'});
		});
	  
   };
   

	$scope.uploadKeyValueObjectFile = function() {
		   var content = $scope.objectFile;
		   console.log(content);
		   
		   $http({method : "POST",
			      url : "/api/simplecache/"+ $scope.newDocKeyValueCacheID + "/put/"+$scope.newDocKey,
			      data : content,
			      headers : {'Content-Type' : 'text/plain'}
		   }).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'File uploaded.', type : 'success'});

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Error uploading file', type : 'danger'});
			});
		  
	   };
   
   $scope.removeCache = function (id,type) {
	   var content = $scope.jsonFile;
	   console.log(content);
	   
	   var typeurl = "";
	   if (type.indexOf("Doc") != -1)
		   typeurl ="documentcache";
	   else
		   typeurl ="simplecache";
	   
	   $http({method : "POST",
		      url : "/api/"+typeurl+"/"+ id + "/removeAll",
		      data : "",
		      headers : {'Content-Type' : 'text/plain'}
	   }).
		success(function(data, status, headers, config) {
			console.log('post success');
			
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Cache removed.', type : 'success'});

		}).
		error(function(data, status, headers, config) {
			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Error removing cache.', type : 'danger'});
		});
   };

   $scope.alerts = [];

   $scope.closeAlert = function(index) {
	   $scope.alerts.splice(index, 1);
   }
   
}]);

controllers.controller("TaskController", ['$scope', '$http','$uibModal', '$state',  function ($scope, $http, $uibModal, $state) {

	$scope.templateID = "/Template.MoCo.PlainVanillaSwaption.xml"
	$scope.taskID = "/Task.PriceSingleTradeMoCo.json"
	
	$scope.alerts = [];
	
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	}
	
	$scope.pretty = function (obj) {
        return angular.toJson(JSON.parse(obj), true);
    }
	
	$scope.parse = function (obj) {
        return JSON.parse(obj);
    }
}]);

controllers.controller("RepositoryController", ['$scope', '$http' , '$uibModal', function ($scope, $http, $uibModal) {

    $scope.treeModel = [];
	$scope.fileContent = "";
	$scope.currentFile = "New File *";
	
	$scope.alerts = [];

	
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
      
    $scope.contextMenu = function(e, eventData) {
    	alert("rightclick");
    };
	  
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	}
	  
	$scope.saveFile = function() {
	
	    $http.post("/api/repository/files"+ $scope.currentFile + "/put", $scope.fileContent).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'File saved.', type : 'success'});
				
				$scope.loadRepository();
				
				

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Error saving file', type : 'danger'});
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
	
		  $http.post("/api/repository/files"+ $scope.currentFile + "/delete").
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
	
	$scope.newFolderWindow = function (template, fileName, base) {

		if (base == "Repository") base = "/";
	
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "newFolderDialog.html",
		  controller: 'newFileDialogCtrl',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
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
					$scope.treeModel  = data;
				}).
				error(function(data, status, headers, config) {
					console.log("Failed to get repo!");
				}
			);
	}
	
	
	$scope.actions = function(e){
		console.log('context menu ' + e.parent);
		console.log(e);
		if (e.icon.indexOf("folder") != -1) {
			return { 
				"NewFolder": {
					"label" : "New Folder...",
					"action" : function(obj) { 
						
					}
				},
				"NewFile": {
					"label" : "New file...",
					"action" : function(obj) { 
						
					}
				},
				"DeleteFolder": {
					"label" : "Delete",
					"action" : function(obj) { 
						
					}
				}
			}
		}
		return { 
			"DeleteFile": {
				"label" : "DeleteFile",
				"action" : function(obj) { 
					alert('child')
				}
			}
		}
	};
	
	$scope.loadRepository();
}]);

controllers.controller("TaskActionController", ['$scope', '$state', '$http', 	function ($scope, $state, $http) {
	
	$scope.taskType = "PriceTrade";
	$scope.definedTasks = [];
	
	$scope.submitTask = function(taskDescription,type) {
		
		$http.post('/api/compute/task/submit'+type, taskDescription)
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
	
	$scope.taskFromRepository = function() {
		 $http.get('/api/repository/files'+$scope.taskID)
					.success(function(data, status, headers, config) {
						
						var type="";
						if ($scope.taskID.indexOf("scala") != -1)
							type="Script";
						
						if ( data.hasOwnProperty("fileData")) {
						
							$scope.submitTask(data.fileData, type);
						
						 } else {
						 
							$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Task submission failed: ' + data.Msg, type : 'danger'});
							
						}
					})
					.error(function(data, status, headers, config) {
						 $scope.alerts.push({msg: 'Task submission failed: ' + status , type : 'danger'});
					});
	}
	
	$scope.loadDefinedTasks = function() { $http.get('/api/repository/content').
				success(function(data, status, headers, config) {
					for (var i=0; i < data.children.length; i++) {
					
						if (data.children[i].id.indexOf("Task.") != -1 || data.children[i].id.indexOf(".scala") != -1)
							$scope.definedTasks.push(data.children[i]);
					}
				}).
				error(function(data, status, headers, config) {
					console.log("Failed to get repo!");
				}
			);
	}
	
	$scope.loadDefinedTasks();
		
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
