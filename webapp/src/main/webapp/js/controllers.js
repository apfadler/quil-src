'use strict';

var controllers = angular.module("controllers", []);

//for file upload
controllers.directive("fileread", [function () {
    return {
        restrict : 'AE',
        scope: {
            fileread: "=",
            filename: "="
        },
        link: function (scope, element, attributes) {
            element.bind("change", function (changeEvent) {
                console.log(scope)
                var reader = new FileReader();
                reader.onload = function (loadEvent) {

                    scope.$apply(function () {
                        scope.fileread = loadEvent.target.result;
                    });
                }
                var files = event.target.files;
                var file = files[0];
                //scope.filename = file ? file.name : undefined;
                scope.$apply();
                reader.readAsText(changeEvent.target.files[0]);

            });
        }
    }
}]);


controllers.controller("MainController", ['$scope', '$interval', 'ClusterNodes', 'Tasks', 'Caches',function ($scope, $interval, ClusterNodes, Tasks, Caches) {

	$scope.clusterNodes = [];
	$scope.tasks = [];
	$scope.failedTasks = [];
	$scope.runningTasks = [];
	$scope.dataServices = [];
	$scope.deployedObjects = [];

	$scope.updateTasks = function() {
		
		for (var i=0; i < $scope.tasks.length; i++)
		{
		    if ($scope.tasks[i].status == 0) {
                $scope.runningTasks.push($scope.tasks[i]);
                $scope.tasks[i].status_text = "Pending";
            }
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
			}

		}
	};
	
	$interval( function() {
		$scope.tasks = Tasks(); 
		$scope.failedTasks = [];
		$scope.runningTasks = [];


		$scope.updateTasks();
		$scope.clusterNodes = ClusterNodes();
		$scope.dataServices = Caches();

	}, 1000);
	

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

controllers.controller("DataController", ['$scope', '$http', '$uibModal', function ($scope, $http, $uibModal) {
   
   $scope.newDocumentCacheID = "";
   $scope.newSimpleCacheID = "";
   $scope.newCSVCacheID = "";
   $scope.genericCSVfileContent = "";
   
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

   
   $scope.inspectObject = function (object) {
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "inspectObject.html",
		  controller: 'inspectObjectCtrl',
		  windowClass: 'app-modal-window-inspect',
		  size: 'sm',
		  resolve: {
			obj: function () {
			  return object;
			}
			
		  }
		});
	};


   $scope.query = function(queryStr) {

	   $http({method : "POST",
		      url : "/api/objects/query",
		      data : queryStr,
		      headers : {'Content-Type' : 'text/plain'}
	   }).
		success(function(data, status, headers, config) {
			console.log('post success');

			if (Object.prototype.toString.call( data ) === '[object Array]') {

			 $scope.showQueryResult = true;
			
			 for (var i=0; i < data.length;i++) {
				 for (var j=0; j < data[i].length;j++) {
					 if (data[i][j][0] == "[" || data[i][j][0] == "{") {
						 data[i][j] = { "data" : JSON.parse(data[i][j])  };
					 }
				 }
			 }
			 
			 $scope.lastQuery = data;

			} else {
			    $scope.alerts.splice(0, 1);
                						$scope.alerts.push({msg: 'Query Error:' + data.Msg, type : 'danger'});
			}

		}).
		error(function(data, status, headers, config) {
			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
			$scope.alerts.splice(0, 1);
						$scope.alerts.push({msg: 'Error executing query.', type : 'danger'});
		});
	   
   }

   $scope.downloadQuery = function(queryStr) {

   	   $http({method : "POST",
   		      url : "/api/objects/query",
   		      data : queryStr,
   		      headers : {'Content-Type' : 'text/plain'}
   	   }).
   		success(function(data, status, headers, config) {
   			console.log('post success');

   			if (Object.prototype.toString.call( data ) === '[object Array]') {

   			 var csv = ""

   			 for (var i=0; i < data.length;i++) {
   				 for (var j=0; j < data[i].length;j++) {
   					 csv += data[i][j] + ";";
   				 }
   				 csv+="\r\n";
   			 }

   			  var file = new File([csv], "query_result.csv", {type: "text/plain;charset=utf-8"});
              saveAs(file);

   			} else {
   			    $scope.alerts.splice(0, 1);
                   						$scope.alerts.push({msg: 'Query Error:' + data.Msg, type : 'danger'});
   			}

   		}).
   		error(function(data, status, headers, config) {
   			console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
   			$scope.alerts.splice(0, 1);
   						$scope.alerts.push({msg: 'Error executing query.', type : 'danger'});
   		});

      }

   $scope.uploadFile = "";

   $scope.upload = function() {
	   var url="";
		if ($scope.cacheType == "documentcache")
			url = "/api/documentcache/"+ $scope.cacheId + "/addFromCSV";

		if ($scope.cacheType == "documentcachearray")
        			url = "/api/documentcache/"+ $scope.cacheId + "/addJSONArray";

		if ($scope.cacheType == "documentcachesingle")
			url = "/api/documentcache/"+ $scope.cacheId + "/put/"+ $scope.cacheKey;
		
		if ($scope.cacheType == "simplecache")
			url = "/api/simplecache/"+ $scope.cacheId + "/addFromCSV";
		
		if ($scope.cacheType == "simplecachesingle")
			url = "/api/simplecache/"+ $scope.cacheId + "/put/"+ $scope.cacheKey;

		   $http({method : "POST",
			      url : url,
			      data : $scope.uploadFile,
			      headers : {'Content-Type' : 'text/plain'}
		   }).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Data uploaded.', type : 'success'});

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Error uploading data', type : 'danger'});
			});
   }
   
   $scope.queryString = "SELECT * FROM \"Results\".ResultItem LIMIT 0,10 ";
   $scope.lastQuery = [["Column"],["No Query submitted yet."]];
   $scope.showQueryResult = false;
   
   $scope.alerts = [];

   $scope.closeAlert = function(index) {
	   $scope.alerts.splice(index, 1);
   }
   
   $scope.parsed = function(str) { 
	   var res=""; 
	   try {
		   res=JSON.parse(str); 
		   if (res == undefined) return str; else return res;
	   }
	   catch(Exception) {
		   return str;
	   } 
	   return res;
   }
   
}]);

controllers.controller("TaskController", ['$scope', '$http','$uibModal', '$state',  function ($scope, $http, $uibModal, $state) {
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

    $scope.viewResult = function (object) {

        		var modalInstance = $uibModal.open({
        		  animation: true,
        		  templateUrl: "inspectTaskResult.html",
        		  controller: 'inspectTaskCtrl',
        		  windowClass: 'app-modal-window-inspect',
        		  size: 'sm',
        		  resolve: {
        			obj: function () {
        			  return object;
        			}

        		  }
        		});
        	};

}]);

controllers.controller("RepositoryController", ['$scope', '$http' , '$uibModal', function ($scope, $http, $uibModal) {

    $scope.treeModel = [];
	$scope.fileContent = "";
	$scope.currentFile = "New File *";
	$scope.alerts = [];

	$scope.aceLoaded = function(_editor) {
		$scope.ace = _editor;
	};

	$scope.aceChanged = function(e) {
	};
	
	
	$scope.nodeSelected = function(e, eventData) {
	
	    if (eventData.node.original.type=="file") {
		
		  $scope.currentFile = eventData.node.id;
		
		    
		  $http.get('/api/repository/files'+eventData.node.id)
					.success(function(data, status, headers, config) {
						$scope.fileContent = data.fileData;
						
						if ($scope.currentFile.indexOf("scala") != -1)
							$scope.ace.getSession().setMode("ace/mode/scala");
						else if ($scope.currentFile.indexOf("xml") != -1)
							$scope.ace.getSession().setMode("ace/mode/xml");
						else if ($scope.currentFile.indexOf("json") != -1)
							$scope.ace.getSession().setMode("ace/mode/json");
						else if ($scope.currentFile.indexOf("py") != -1)
                        	$scope.ace.getSession().setMode("ace/mode/python");
						else
							$scope.ace.getSession().setMode("ace/mode/plain_text");
			
					})
					.error(function(data, status, headers, config) {
					
						console.log("Failed to get file!");
					});
        } else {
        	$scope.currentFolder = eventData.node.id;
        }
		
      };
      
    $scope.contextMenu = function(e, eventData) {
    	alert("rightclick");
    };
	  
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	}
	  
	$scope.saveFile = function() {
	
	    $http.post("/api/repository/files"+ $scope.currentFile, $scope.fileContent).
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

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "newFileDialog.html",
		  controller: 'newFileDialogCtrl',
		  windowClass: 'app-modal-window',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		  $http.post("/api/repository/files/"+ base + "/" + selectedItem).
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

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "deleteFileDialog.html",
		  controller: 'deleteFileDialogCtrl',
		  windowClass: 'app-modal-window',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
			
		  }
		});
		
		modalInstance.result.then(function () {
	
		  $http.post("/api/repository/files"+ fileName + "/delete").
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
	
	$scope.deleteFolderWindow = function (template, fileName, base) {

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "deleteFolderDialog.html",
		  controller: 'deleteFileDialogCtrl',
		  windowClass: 'app-modal-window',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
			
		  }
		});
		
		modalInstance.result.then(function () {
	
		  $http.post("/api/repository/folders"+ fileName + "/delete").
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
	
	$scope.uploadFileWindow = function () {

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "uploadFileDialog.html",
		  controller: 'uploadFileDialogCtrl',
		  windowClass: 'app-modal-window',
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
		
		modalInstance.result.then(function (fileInfo) {
		
			$http({method : "POST",
			      url : "/api/repository/files"+ fileInfo.fileName ,
			      data : fileInfo.fileData,
			      headers : {'Content-Type' : 'text/plain'}
		   }).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.alerts.splice(0, 1);
				$scope.alerts.push({msg: 'File upload successful.', type : 'success'});

				$scope.loadRepository();
			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Error uploading file.', type : 'danger'});
			});
			
		}, function () {
		
		});
	};
	
	$scope.newFolderWindow = function (template, fileName, base) {

		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "newFolderDialog.html",
		  controller: 'newFileDialogCtrl',
		  windowClass: 'app-modal-window',
		  size: 'sm',
		  resolve: {
			newFileName: function () {
			  return fileName;
			}
			
		  }
		});
		
		modalInstance.result.then(function (selectedItem) {
	
		  $http.post("/api/repository/folders/"+ base + "/" + selectedItem).
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
		  windowClass: 'app-modal-window',
		  size: 'sm',
		  resolve: {
			cacheKey: function () {
			  return $scope.currentFile;
			}
			
		  }
		});
		
		modalInstance.result.then(function (data) {
	
			var url="";
			if (data.cacheType == "documentcache")
				url = "/api/documentcache/"+ data.cacheId + "/addFromCSV";

			if (data.cacheType == "documentcachearray")
            				url = "/api/documentcache/"+ data.cacheId + "/addJSONArray";

			if (data.cacheType == "documentcachesingle")
				url = "/api/documentcache/"+ data.cacheId + "/put/"+ data.cacheKey;
			
			if (data.cacheType == "simplecache")
				url = "/api/simplecache/"+ data.cacheId + "/addFromCSV";
			
			if (data.cacheType == "simplecachesingle")
				url = "/api/simplecache/"+ data.cacheId + "/put/"+ data.cacheKey;
			
			
			   $http({method : "POST",
				      url : url,
				      data : $scope.fileContent,
				      headers : {'Content-Type' : 'text/plain'}
			   }).
				success(function(data, status, headers, config) {
					console.log('post success');
					
					$scope.alerts.splice(0, 1);
								$scope.alerts.push({msg: 'Data uploaded.', type : 'success'});

				}).
				error(function(data, status, headers, config) {
					console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
					$scope.alerts.splice(0, 1);
								$scope.alerts.push({msg: 'Error uploading data', type : 'danger'});
				});
			
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
		
		if (e.icon.indexOf("folder") != -1) {
			return { 
				"NewFolder": {
					"label" : "New Folder...",
					"action" : function(obj) { 
						$scope.newFolderWindow("", "NewFile" , $scope.currentFolder);
					}
				},
				"NewFile": {
					"label" : "New file...",
					"action" : function(obj) { 
						$scope.newFileWindow("", "NewFile" , $scope.currentFolder);
					}
				},
				"DeleteFolder": {
					"label" : "Delete",
					"action" : function(obj) { 
						$scope.deleteFolderWindow("", $scope.currentFolder ,"");
					}
				}
			}
		}
		return { 
			"DeleteFile": {
				"label" : "DeleteFile",
				"action" : function(obj) { 
					$scope.deleteFileWindow("", $scope.currentFile ,"");
				}
			}
		}
	};
	
	$scope.loadRepository();
}]);

controllers.controller("TaskActionController", ['$scope', '$state', '$http', '$uibModal',	function ($scope, $state, $http, $uibModal) {
	
	$scope.taskType = "PriceTrade";
	$scope.definedTasks = [];
	
	$scope.submitTask = function(taskDescription,type) {
		
		console.log(taskDescription);
		
		if (type != "Script" && type != "PythonScript") {
			//add a tag
			var taskObj = JSON.parse(taskDescription);
			taskObj["Tag"] = $scope.taskTag;
			taskDescription = JSON.stringify(taskObj);
		}
		
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

						if ($scope.taskID.indexOf("py") != -1)
                        	type="PythonScript";
						
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

				    var findTasks = function(node) {

				        for (var i=0; i < node.children.length; i++) {
                        	if (node.children[i].id.indexOf("Task.") != -1
                        	    || node.children[i].id.indexOf(".scala") != -1
                        	    || node.children[i].id.indexOf(".py") != -1)
                                    $scope.definedTasks.push(node.children[i]);

                            if (node.children[i].icon.indexOf("folder")!=-1)
                                findTasks(node.children[i])
                        }
				    };

				    findTasks(data);

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
 
  $scope.nodeSelected = function(e, eventData) {
		
	    if (eventData.node.original.type=="dir") {
		  $scope.selectedFolder = eventData.node.id;
	    }
    };
  
  $scope.ok = function () {
	 
	  $scope.error = undefined;
	  
	  if ($scope.selectedFolder == undefined)
		  $scope.error = "Please select a target folder";
	  
	  if ($scope.file == undefined)
		  $scope.error = "Please select a file";
	  
	  if (!$scope.error) {
		  if ($scope.selectedFolder != "/")
			  $uibModalInstance.close({fileName : $scope.selectedFolder + "/" + $scope.file, fileData : $scope.fileData});
		  else
			  $uibModalInstance.close({fileName : "/" + $scope.file, fileData : $scope.fileData});
	  }
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
  

});

controllers.controller('uploadToCacheDialogCtrl',function ($scope, $uibModalInstance, cacheKey) {

  $scope.cacheKey = cacheKey;
 
  $scope.ok = function () {
	
	  $scope.error = undefined;
	  
	  if ($scope.cacheId == undefined)
		  $scope.error = "Please enter a Cache ID";
	  
	  if ($scope.cacheType == undefined)
		  $scope.error = "Please choose a cache type";
	  
	  if ($scope.cacheType.indexOf("single") != -1 && $scope.cacheKey == undefined)
		  $scope.error = "Please enter a key.";

	  
	  if (!$scope.error) 
		  $uibModalInstance.close({cacheId : $scope.cacheId, cacheKey : $scope.cacheKey, cacheType : $scope.cacheType});
  };

  $scope.cancel = function () {
   $uibModalInstance.dismiss('cancel');
  };
  

});

controllers.controller('inspectObjectCtrl',['$scope', '$uibModalInstance','$http','obj', function ($scope, $uibModalInstance, $http, obj) {

	  $scope.objectData = {};
	  $scope.objIndex = obj;
	  
	  $http.get(obj.url)
		.success(function(data, status, headers, config) {
			$scope.objectData = data;
		})
		.error(function(data, status, headers, config) {
		
			console.log("Failed to get data!");
		});
	 
	  $scope.ok = function () {
	    $uibModalInstance.close('ok');
	  };

	  $scope.cancel = function () {
	   $uibModalInstance.dismiss('cancel');
	  };
	  

	}])

.filter('parse', function() {
  return function(input, uppercase) {
   return JSON.stringify(input);
  };
})
.filter('parse', function() {
  return function(input, uppercase) {
   return JSON.parse(input);
  };
});


controllers.controller('inspectTaskCtrl',['$scope', '$uibModalInstance','$http','obj', function ($scope, $uibModalInstance, $http, obj) {

	  $scope.resultData = {};
	  $scope.task = obj;
	  $scope.readingData = true;

	  $http.get("/api/compute/tasks/"+obj.name+"/result")
		.success(function(data, status, headers, config) {
			$scope.resultData = data;
			$scope.readingData = false;
		})
		.error(function(data, status, headers, config) {
            $scope.resultData = {Error: "Failed to retrieve task result data."}
            $scope.readingData = false;
			console.log("Failed to get data!");
		});

	  $scope.ok = function () {
	    $uibModalInstance.close('ok');
	  };

	  $scope.cancel = function () {
	   $uibModalInstance.dismiss('cancel');
	  };


	}])

.filter('parse', function() {
  return function(input, uppercase) {
   return JSON.stringify(input);
  };
})
.filter('parse', function() {
  return function(input, uppercase) {
   return JSON.parse(input);
  };
});