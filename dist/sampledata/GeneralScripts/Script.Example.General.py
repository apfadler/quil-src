import sys
print "hello world 1"

from org.quil.server.Tasks import Task
from org.quil.server.Tasks import TaskRunner

print "hello world 2"

taskDescription  = """ 
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
} """

T = Task.fromString(taskDescription)
TaskRunner.runTaskAndWait(T)
