/**
  * Created by d90590 on 02.06.2016.
  */

import org.quil.objects._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{read, write}
import org.scalatest._

class SerializationSpec extends FlatSpec {

  it should "Output some json" in {

    //////
    implicit val ctx = new Context
    import Schema._

    DepositInstrument("EUR_OIS_MM_1D", 0.001, "1D", "TARGET", "ModifiedFollowing", "Actual360")
    DepositInstrument("EUR_OIS_MM_2D", 0.001, "2D", "TARGET", "ModifiedFollowing", "Actual360")

    YieldCurve("EUR_OIS", "TARGET", "Actual365",
                                 List(SQReference("EUR_OIS_MM_1D", "DepositInstrument"),
                                      SQReference("EUR_OIS_MM_2D", "DepositInstrument")))

    object initObject extends SQObject("InitObject") {
      val ID = "InitObject"
      val curves = List(SQReference("EUR_OIS","YieldCurve"))
    }

    /////
    implicit val formats = serializationFormat()
    println(pretty(render(parse(write(ctx.objects)))))
    println(parse(write(ctx.objects)).extract[Map[String,SQObject]])


    /////

    import QLTypeMapping._
    import Graph._

    val buildCurve = (obj:SQObject) => {

      println("Building " + obj.ID + "(" + obj.TypeName+ ")")

      val rateHelpers = visit(obj, (obj:SQObject) => {
        obj match {
          case x:DepositInstrument => x.CalendarName
          case _ => null
        }
      })
    }

    val curves = visit(initObject, (obj: SQObject) => {

      println("visit: " + obj)

      obj match {
        case x:YieldCurve => buildCurve(x)
        case _ => null
      }

    })

    println(curves)
  }
}