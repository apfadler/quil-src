println("This is an example Quil Scala Script")

var taskDescription  = """

{
	"Interpreter" : "org.quil.interpreter.QuantLibTemplates.QuantLibXmlTemplateInterpreter",
	"Task"	: "PriceTrade",
	"Template" : "Trade.Swap",
	"Repository" : "Templates",
	"MarketData" : { 
		"Base" :"ExampleMarket",
		"Additional" : {
			"EUR_Swap_1Y" : "0.05"
		}
	},
	"TradeData" : {
		"ID" : "1",
		"Notional" : "1000.0",
		"Cpty" : "A"
	}
}

""";

var T = Task.fromString(taskDescription);
TaskRunner.runTask(T);

var allTasks = Task.allTasks();


println ("Task ID\tResult\tStatus")
println ("------------------------------")
for ( (id,task) <- allTasks) {
	println (id+ "\t" + task.getResult + "\t" + task.getStatus)
	
}
println ("------------------------------")