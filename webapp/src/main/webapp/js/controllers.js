'use strict';

var controllers = angular.module("controllers", []);

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
				   
		$interval( function() {
						$scope.tasks = Tasks(); 
						$scope.failedTasks = [];
						$scope.runningTasks = [];
						
						
						for (var i=0; i < $scope.tasks.length; i++)
						{
							if ($scope.tasks[i].status == 1) {
								$scope.runningTasks.push($scope.tasks[i]);
								$scope.tasks[i].status_text = "Running";
							}
							if ($scope.tasks[i].status == 2) {
								$scope.runningTasks.push($scope.tasks[i]);
								$scope.tasks[i].status_text = "Finished";
							}
							if ($scope.tasks[i].status == 3) {
								$scope.failedTasks.push($scope.tasks[i]);
								$scope.tasks[i].status_text = "Failed";
							}
							
							
							
						}
						
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

controllers.controller("ObjectsController", ['$scope', function ($scope) {
}]);

controllers.controller("TaskController", ['$scope', '$http','$uibModal', '$state',  function ($scope, $http, $uibModal, $state) {

	$scope.templateID = "/Template.MoCo.PlainVanillaSwaption.xml"
	$scope.taskID = "/Task.PriceSingleTradeMoCo.json"
	
	$scope.alerts = [];
	
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
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

controllers.controller("TaskDefinitionController", ['$scope', '$state', '$http', function ($scope, $state, $http) {
	
	
    $scope.schema = {};
	$scope.model = { FileName : "/Task.New.json", Template : $scope.templateID, Interpreter : "MoCo", Task: "PriceTrade" };
  
    $http.get('/api/repository/files'+$scope.templateID)
					.success(function(data, status, headers, config) {
					
						console.log(data);

						$scope.generateForm(data);
					})
					.error(function(data, status, headers, config) {
						alert(status);
						console.log("Failed to get file!");
					});
  
  
	$scope.generateForm = function(data) { 
	
		var deployedTemplates = [];
		var deployedMarkets = [];
		var deployedTrades = [];
		
		for (var i=0; i < $scope.deployedObjects.length; i++)
		{
			if ($scope.deployedObjects[i].cacheId == "Markets") {
				deployedMarkets.push($scope.deployedObjects[i].cacheId+":"+$scope.deployedObjects[i].fileId);
			}
			
			if ($scope.deployedObjects[i].cacheId == "Templates") {
				deployedTemplates.push($scope.deployedObjects[i].cacheId+":"+$scope.deployedObjects[i].fileId);
			}
			
			if ($scope.deployedObjects[i].cacheId == "Trades") {
				deployedTrades.push($scope.deployedObjects[i].cacheId+":"+$scope.deployedObjects[i].fileId);
			}
		}
		
		deployedMarkets.push("");
		deployedTemplates.push("");
		deployedTrades.push("");
	
		var schema = {
			type: "object",
			properties : {
			
				FileName : {type:"string"},
			
				Interpreter : { type : "string", enum : ["QuantLib", "MoCo"], default : "QuantLib"},
				
				Task : { type : "string", enum : ["PriceTrade", "PricePortfolio"], default : "PriceTrade"},
				
				Template : { type : "string", enum:deployedTemplates },
				
				Market : { type : "string", enum:deployedMarkets }, 
				
				Trade :{ type:"string" , enum:deployedTrades}
				
				

			}
		};
		
		if (deployedTemplates.length >0)
			$scope.model.Template = deployedTemplates[0];
			
		if (deployedMarkets.length >0)
			$scope.model.Market = deployedMarkets[0];
			
		if (deployedTemplates.length >0)
			$scope.model.Trade = deployedTrades[0];
		
		
		/*var x2js = new X2JS();
		var template = x2js.xml_str2json( data.fileData );
		
		$scope.model.TradeData = {};
		for (var i=0; i < template.InputFile.InputParameter.length; i++)
		{
			schema.properties.TradeData.properties[template.InputFile.InputParameter[i].Name] = {type : "string"}; 
			
			//$scope.model.TradeData[template.InputFile.InputParameter[i].Name] = "1";
		} */
		
		
		
		
		$scope.schema = schema;//JSON.parse(data.fileData);
	}
	
	$scope.saveTask = function() {
	
		var model = $scope.model;
		var fileName = model.FileName;
		delete model.FileName;
		
		var parts = model.Template.split(":");
		var templateRepo = parts[0];
		var template = parts[1];
		
		parts = model.Market.split(":");
		var marketRepo = parts[0];
		var market = parts[1];
		
		parts = model.Trade.split(":");
		var tradeRepo = parts[0];
		var trade = parts[1];
		
		model.Template = template;
		model.Repository = templateRepo;
		model.MarketData = { Repository : marketRepo, Key : market};
		
		model.TradeData = { Repository : tradeRepo, Key : trade};
		
		delete model.Trade;delete model.Market;
		
		if (model.Interpreter == "MoCo")
			model.Interpreter = "org.quil.interpreter.MoCoTemplates.MoCoXmlTemplateInterpreter";
	
		 $http.post("/api/repository/files"+ fileName + "/put", model).
			success(function(data, status, headers, config) {
				console.log('post success');
				
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Task saved.', type : 'success'});
				
				$state.go('tasks.main');
				

			}).
			error(function(data, status, headers, config) {
				console.log("\r\n" + "ERROR::HTTP POST returned status " + status + "\r\n");
				$scope.alerts.splice(0, 1);
							$scope.alerts.push({msg: 'Error saving file', type : 'danger'});
			
			});
	
	}

}]);

controllers.controller("TaskActionController", ['$scope', '$state', '$http', 	function ($scope, $state, $http) {
	
	$scope.taskType = "PriceTrade";
	$scope.definedTasks = [];
	
	$scope.newTask = function() {
			
		$state.go('tasks.define'); 
	}
	
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
					// this callback will be called asynchronously
					// when the response is available
					
					
					for (var i=0; i < data.children.length; i++) {
					
						if (data.children[i].id.indexOf("Task.") != -1 || data.children[i].id.indexOf(".scala") != -1)
							$scope.definedTasks.push(data.children[i]);
					
					}
					
				}).
				error(function(data, status, headers, config) {
					// called asynchronously if an error occurs
					// or server returns response with an error status.
					
					console.log("Failed to get repo!");

				}
			);
	}
	
	
	$scope.loadDefinedTasks();
		
}]);

controllers.controller('taskFromTemplateDialogCtrl',function ($scope, $uibModalInstance, templateID, http) {

  $scope.templateID = templateID;
  
  $scope.obj = {data: {bla:"bla"}, options: { mode: 'tree' }};
  
  $scope.schema = {};
  
  http.get('/api/repository/files'+templateID)
					.success(function(data, status, headers, config) {
					
						console.log(data);

						$scope.generateForm(data);
					})
					.error(function(data, status, headers, config) {
						alert(status);
						console.log("Failed to get file!");
					});
  
  
  $scope.generateForm =function(data) {
  
	var schema = {
			type: "object",
			properties : {
				Interpreter : { type : "string", enum : ["QuantLib", "MoCo"], default : "QuantLib"},
				
				Task : { type : "string", enum : ["PriceTrade", "PricePortfolio"], default : "PriceTrade"},
				
				Template : { type : "string" },
				Repository : { type : "string" },
				
				MarketData : {
				
					type : "object",
					properties : {
					
						Market_Data_Repository : { type : "string" },
						Market_ID : { type : "string" }
					}
				
				},
				
				TradeData : {
				
					type : "object",
					properties : {
					}
				
				}

			}
	};
	
	
	var x2js = new X2JS();
	var template = x2js.xml_str2json( data.fileData );
	
	for (var i=0; i < template.InputFile.InputParameter.length; i++)
	{
		schema.properties.TradeData.properties[template.InputFile.InputParameter[i].Name] = {type : "string"}; 
	}
	
	$scope.schema = schema;//JSON.parse(data.fileData);
	$scope.obj.data = schema // JSON.parse(data.fileData);
	
	
  }
 

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


/*
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
	
	$scope.newTask = function() {
			
		$state.go('tasks.define'); 
	}

	$scope.taskFromTemplate = function() {
		
		var modalInstance = $uibModal.open({
		  animation: true,
		  templateUrl: "taskFromTemplateDialog.html",
		  controller: 'taskFromTemplateDialogCtrl',
		  size: 'sm',
		  windowClass : 'task-template-modal-window',
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
	}*/