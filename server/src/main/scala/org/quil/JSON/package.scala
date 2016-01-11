package org.quil.JSON {

import util.parsing.json.JSON
import util.parsing.json.JSONObject;
import io.Source
 
import scala.language.dynamics
 
 
class Parser {
	def parse(json:String) : Document = {
		return Document.parse(json).get;
	}
}
 
 
trait Document extends Dynamic{ self =>
  def selectDynamic(field: String) : Document = EmptyElement
  def applyDynamic(field: String)(i: Int) : Document = EmptyElement
  def toList : List[String] = sys.error(s"$this is not a list.")
  def asString: String = sys.error(s"$this has no string representation.")
  def length$ : Int = sys.error(s"$this has no length")
}
 
 
object Document{
 
  def ^(s: String) = {
    require(!s.isEmpty, "Element is empty")
    s
  }
 
  implicit def toString(e: Document) : String = e.asString
  implicit def toBoolean(e: Document) : Boolean = (^(e.asString)).toBoolean
  implicit def toBigDecimal(e: Document) : BigDecimal = BigDecimal(^(e.asString))
  implicit def toDouble(e: Document) : Double = ^(e.asString).toDouble
  implicit def toFloat(e: Document) : Float = ^(e.asString).toFloat
  implicit def toByte(e: Document) : Byte = ^(e.asString).stripSuffix(".0").toByte
  implicit def toShort(e: Document) : Short = ^(e.asString).stripSuffix(".0").toShort
  implicit def toInt(e: Document) : Int = ^(e.asString).stripSuffix(".0").toInt
  implicit def toLong(e: Document) : Long = ^(e.asString).stripSuffix(".0").toLong
  implicit def toList(e: Document) : List[String] = e.toList
 
 
  def parse(json: String) = JSON.parseFull(json) map (Document(_))
 
  def apply(any : Any) : Document = any match {
    case x : Seq[Any] => new ArrayElement(x)
    case x : Map[String, Any] => new ComplexElement(x)
    case x => new PrimitiveElement(x)
  }
}
 
case class PrimitiveElement(x: Any) extends Document{
  override def asString = x.toString
}
 
case object EmptyElement extends Document{
  override def asString = ""
  override def toList = Nil
}
 
case class ArrayElement(private val x: Seq[Any]) extends Document{
  private lazy val elements = x.map((Document(_))).toArray
 
  override def applyDynamic(field: String)(i: Int) : Document = elements.lift(i).getOrElse(EmptyElement)
  override def toList : List[String] = elements map (_.asString) toList
  override def length$ : Int = elements.length
}
 
case class ComplexElement(private val fields : Map[String, Any]) extends Document{
  override def selectDynamic(field: String) : Document = fields.get(field) map(Document(_)) getOrElse(EmptyElement)
  
  def toJSONString() : String = {
    
    return (new JSONObject(fields)).toString();
  }
  
  override def toString() : String = {
    
    return (new JSONObject(fields)).toString();
  }
  
  
  
}
 
}