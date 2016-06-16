package org.quil.server.Tasks

import org.apache.ignite.cache.CacheMode
import org.apache.ignite.{Ignite, Ignition}
import org.apache.ignite.configuration.CacheConfiguration
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.quil.interpreter.Interpreter
import org.quil.server.ResultsCache
import org.quil.server.Tasks.Task.Status
import org.slf4j.LoggerFactory

/**
  * Created by d90590 on 07.06.2016.
  */

class RunQLObjectsApplication(val taskName:String, val taskDescription:String) extends
  org.quil.server.Tasks.Task(taskName,taskDescription) {

  val logger = LoggerFactory.getLogger(classOf[RunQLObjectsApplication])

  def run() = {

    Task.updateStatus(_taskName, Status.RUNNING)

    val parser: JSONParser = new JSONParser

    val taskDescription: JSONObject = parser.parse(_taskDescription).asInstanceOf[JSONObject]

    val interpreter: Interpreter = Class.forName(taskDescription.get("Interpreter").asInstanceOf[String]).newInstance.asInstanceOf[Interpreter]
    interpreter.setData(taskDescription)
    try {
      interpreter.interpret
    }catch {
      case e:Exception => logger.error("Interpretation failed:" + e.getMessage);
    }

    Task.updateResult(_taskName, interpreter.getResult.toJSONString)

    if (interpreter.getError) throw new Exception("Error during interpretation in task PriceTrade.")

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
  }
}
