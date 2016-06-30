package org.quil.JSON {

	import util.parsing.json._;
	import io.Source
	import scala.language.dynamics
	import org.json4s._
	import org.json4s.JsonDSL._
	import org.json4s.native.Serialization
	import org.json4s.FieldSerializer._
	import org.json4s.native.JsonMethods._
	import org.json4s.native.Serialization.{read, write}

	object DocumentConversions {
		implicit def documentToMap(d: Document): Map[String, Any] = d.fields
	}

	class Document(private var jsonStr:String) {

		implicit val formats = org.json4s.DefaultFormats

		var fields = parse(jsonStr).extract[Map[String,Any]]

		override def toString :String = jsonStr

		def toJSONString : String = jsonStr

		def asString : String = jsonStr

		def apply(field:String):Any = fields(field)

		def apply(fields:Map[String,Any])  = {
			jsonStr = write(fields)
			this.fields = fields
		}
	}

	class Parser {
		def parse(json:String) : Document = {
				return new Document(json);
		}
	}



}