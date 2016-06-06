package org.quil.interpreter.QuantLibObjects

import org.quil.interpreter._

import org.json.simple.JSONObject
/**
  * Created by d90590 on 06.06.2016.
  */
class QLObjectsInterpreter extends org.quil.interpreter.Interpreter {

  var _data:JSONObject = new JSONObject()

  def setData(data:JSONObject) = {
    _data = data
  }

  def getError(): Boolean = ???
  def getResult(): org.json.simple.JSONObject = ???
  def interpret(): Unit = ???


}
