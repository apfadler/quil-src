package org.quil.interpreter.QuantLibObjects

import java.io.{ByteArrayOutputStream, PrintStream, PrintWriter, StringWriter}

import org.apache.ignite.Ignition
import org.quil.interpreter._
import org.json.simple._
import org.json.simple.parser.JSONParser
import org.quil.repository.CachedFileSystemRepository
import org.quil.objects._
import org.slf4j.LoggerFactory

import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results.Result
import org.json4s.native.Serialization.write
import org.quil.interpreter.QuantLibTemplates.Market

/**
  *
  * Created by d90590 on 06.06.2016.
  */
class QLObjectsInterpreter extends Interpreter {

  val logger = LoggerFactory.getLogger(classOf[QLObjectsInterpreter])

  var _data:JSONObject = new JSONObject()
  var _result:JSONObject = new JSONObject()
  var _error = false

  def setData(data:JSONObject) = {
    _data = data
  }

  def getError(): Boolean = _error

  def getResult(): org.json.simple.JSONObject = _result

  def interpret(): Unit = {

    val ignite = Ignition.ignite()

    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val ps: PrintStream = new PrintStream(baos)
    val out: StringWriter = new StringWriter
    val stream: PrintWriter = new PrintWriter(out)

    try {

      logger.info("Loading Controller")
      val controllerFileName = _data.get("Controller").asInstanceOf[String]
      val controllerScript = CachedFileSystemRepository.instance().getFile(controllerFileName)

      val paramObjects = _data.get("Parametrization").asInstanceOf[JSONArray]

      implicit var ctx = new Context
      import Schema._

      logger.info("Loading Parametrization")
      (0 to paramObjects.size()-1).foreach { i=>
        loadFromString(CachedFileSystemRepository.instance().getFile(paramObjects.get(i).asInstanceOf[String]))
      }

      logger.info("Loading Trades")
      val trades = _data.get("Trades").asInstanceOf[JSONObject]
      val from = trades.get("From").asInstanceOf[String]

      var idsLoaded = List[String]()
      if (from.contains("/")) {
        idsLoaded = loadFromString(CachedFileSystemRepository.instance().getFile(from))
        //TODO WHERE predicate
      } else {
        //TODO trades from other cache
        //TODO WHERE predicate
      }

      // Add trades to initial object
      object initObject extends SQObject("InitObject") {
        val ID = "InitObject"
        val trades = idsLoaded.map( x=> SQReference(x, "SwapTrade"));
      }

      //Load MD
      val MD: Market = new Market
      val marketData: JSONObject = _data.get("MarketData").asInstanceOf[JSONObject]
      if (marketData != null) {
        logger.info("Injecting market parameters.")
        val base: String = marketData.get("Base").asInstanceOf[String]
        MD.setBase(base)
        val overrideMarketData: JSONObject = marketData.get("Additional").asInstanceOf[JSONObject]
        if (overrideMarketData != null) {
          val iterator = overrideMarketData.keySet.iterator
          while (iterator.hasNext) {
            {
              val key: String = iterator.next.asInstanceOf[String]
              MD.set(key, overrideMarketData.get(key).asInstanceOf[String])
              logger.info("Market delta: " + key + " = " + overrideMarketData.get(key).asInstanceOf[String])
            }
          }
        }

      }

      //Load steps by compiling the scala script
      val script: String = _data.get("Script").asInstanceOf[String]
      val init: String = """import scala.collection.JavaConversions._;
                            import org.quil.objects._;

                            implicit val ctx:Context = ctx_;

                            import org.quil.objects.Schema._;
                            import org.quantlib.{Array=>QArray, Index=>QIndex, _}; """

      Thread.currentThread.setContextClassLoader(this.getClass.getClassLoader)
      val settings: Settings = new Settings
      settings.usejavacp.tryToSetFromPropertyValue("true")
      settings.embeddedDefaults(Thread.currentThread().getContextClassLoader)

      val imain: IMain = new IMain(settings, stream)

      println(imain.bind("ctx_", "org.quil.objects.Context", ctx))

      imain.interpret(init)

      var r: Result = imain.bind("MD", "org.quil.interpreter.QuantLibTemplates.Market", MD)
      println(r)

      // Execute all steps
      var oldOut = Console.out
      imain.eval("Console").asInstanceOf[Console.type].setOut(baos)
      imain.interpret("var Steps = List[(String,(SQObject=>Any))]()\n" + controllerScript)
      val steps = imain.eval("Steps").asInstanceOf[List[(String,(org.quil.objects.SQObject=>Any))]]
      var res = List[(String,Any)]()
      steps.reverse.foreach {step =>
        logger.info("Executing Step '"+step._1+"'")
        res :::= Graph.applyOnce(initObject, step._2)
      }
      Console.setOut(oldOut)

      // Return results
      // TODO need a replacement for stupid org.json.simple
      implicit val formats = serializationFormat()
      val resStr = s"""{ "EvaluationResult" : ${write(res)},
                          "Output" : ${write(baos.toString)},
                          "ReplOutput" :  ${write(out.toString)}
                   }"""

      println (resStr);

      _result = new JSONParser().parse(resStr).asInstanceOf[JSONObject]

    } catch  {
      case e:javax.script.ScriptException => {

        _error = true

        implicit  val formats = Schema.serializationFormat()

        val resStr = s"""{ "error" : ${write(e.getMessage+"\n"+baos.toString+"\n"+out.toString)} }"""

        _result = new JSONParser().parse(resStr).asInstanceOf[JSONObject]

        throw new Exception("Script Error: " + _result.get("error"))
      }
      case e:Exception => {
        _error = true
         throw e }
    }

  }
}
