package org.quil.server.Tasks

import org.apache.ignite.cache.CacheMode
import org.apache.ignite.{Ignite, Ignition}
import org.apache.ignite.configuration.CacheConfiguration
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.quil.interpreter.Interpreter
import org.quil.server.ResultsCache

/**
  * Created by d90590 on 07.06.2016.
  */

object RunQLObjectsApplication {

}

class RunQLObjectsApplication(val taskName:String, val taskDescription:String) extends
  org.quil.server.Tasks.Task(taskName,taskDescription) {


  def run() = {

    val parser: JSONParser = new JSONParser

    val taskDescription: JSONObject = parser.parse(_taskDescription).asInstanceOf[JSONObject]

    val interpreter: Interpreter = Class.forName(taskDescription.get("Interpreter").asInstanceOf[String]).newInstance.asInstanceOf[Interpreter]
    interpreter.setData(taskDescription)
    interpreter.interpret

    Task.updateResult(_taskName, interpreter.getResult.toJSONString)
    import scala.collection.JavaConversions._
    for (r <- interpreter.getResult.keySet) {
      val key: String = r.asInstanceOf[String]
      var doubleVal: Double = 0.0
      var intVal: Int = 0
      var strVal: String = ""
      try {
        doubleVal = interpreter.getResult.get(key).asInstanceOf[String].toDouble
        intVal = interpreter.getResult.get(key).asInstanceOf[String].toInt
        strVal = interpreter.getResult.get(key).asInstanceOf[String]
      }
      catch {
        case e: Exception => {
        }
      }
      ResultsCache.add(_taskName, _taskTag, 0, key, strVal, doubleVal, intVal)
    }

    if (interpreter.getError) throw new Exception("Error during interpretation in task PriceTrade.")
  }
}