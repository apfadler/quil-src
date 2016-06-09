package org.quil.objects {

  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.native.Serialization
  import org.json4s.FieldSerializer._
  import org.json4s.native.JsonMethods._
  import org.json4s.native.Serialization.{read, write}
  import org.quil.objects.macros.SQObj
  import scala.reflect.runtime._
  import org.quantlib.{Array=>QArray, _}

  abstract class Base

  class Context {
    var objects: Map[String, SQObject] = Map[String, SQObject]()

    def get[T](ID: String): T = {

      val tuple = objects.find(_._1 == ID) getOrElse {
        null
      }
      if (tuple != null) tuple._2.asInstanceOf[T] else null.asInstanceOf[T]
    }
  }

  abstract class SQObject(_TypeName:String) (implicit ctx:Context) {
    val ID:String
    val TypeName = _TypeName
  }

  object Graph {

    def visit[T <: SQObject, R](obj:T, func: (T => R))  (implicit ctx:Context) : List[(String,R)] = {



      var out = List[(String,R)]()

      obj.getClass.getDeclaredFields.foreach(field => {
        field.getGenericType.toString match {

          case "class org.quil.objects.SQReference" => {

            field.setAccessible(true)
            if (field.get(obj).asInstanceOf[SQReference].RefID != "")
              out ++= visit(field.get(obj).asInstanceOf[SQReference].follow, func)
          }

          case "scala.collection.immutable.List<org.quil.objects.SQReference>" => {

            field.setAccessible(true)
            val list = field.get(obj).asInstanceOf[List[SQReference]]
            list.foreach(item => if (item.RefID != "") out ++= visit(item.follow, func) )
          }
          case _ => null
        }}
      )

      val res = func(obj)
      if (res != null)  out ++= List((obj.ID,res))

      out
    }

    def applyOnce[T <: SQObject, R](obj:T, func: (T => R))  (implicit ctx:Context) : List[(String,R)] = {
      var visited  = scala.collection.mutable.Map[String,Boolean]()
      visitOnce(obj, func,visited)
    }

    def visitOnce[T <: SQObject, R](obj:T, func: (T => R), visited:scala.collection.mutable.Map[String,Boolean])  (implicit ctx:Context) : List[(String,R)] = {

      var out = List[(String,R)]()

      if (visited.contains(obj.ID)) return out

      obj.getClass.getDeclaredFields.foreach(field => {
        field.getGenericType.toString match {

          case "class org.quil.objects.SQReference" => {

            field.setAccessible(true)
            if (field.get(obj).asInstanceOf[SQReference].RefID != "")
              out ++= visitOnce(field.get(obj).asInstanceOf[SQReference].follow, func, visited)
          }

          case "scala.collection.immutable.List<org.quil.objects.SQReference>" => {

            field.setAccessible(true)
            val list = field.get(obj).asInstanceOf[List[SQReference]]
            list.foreach(item => if (item.RefID != "") out ++= visitOnce(item.follow, func, visited) )
          }
          case _ => null
        }}
      )

      val res = func(obj)
      if (res != null)  out ++= List((obj.ID,res))
      visited += obj.ID -> true

      out
    }

  }

  trait SQRegistrable {

    implicit val ctx:Context
    val ID:String

    ctx.objects += ID -> this.asInstanceOf[SQObject]
  }

  //Type for References
  case class SQReference(RefID:String, RefType:String)  extends Base {
    def follow[T]()(implicit ctx:Context) : T = {
      val res = ctx.get[T](RefID)
      if (res == null) throw new Exception("Object " + RefID + " does not exists in context.") else res
    }
  }

  import scala.reflect.runtime.{universe => u}

  class DefaultSchema {

    var classList = List[Class[_]](classOf[SQReference])

    def serializationFormat() = {

      ( Serialization.formats(ShortTypeHints(classList))
        + FieldSerializer[SQObject] (ignore("ctx"))
        + FieldSerializer[SQReference] (ignore("ctx")) )
    }

  }


  object Schema extends DefaultSchema  {

    //implicit var ctx = new Context

    //Schema Objects

    case class Fixing(Date:String, Value:Double)

    //TODO Fixings need a date
    @SQObj
    case class Index(ID: String,
                     Fixings: List[Fixing])

    @SQObj
    case class DepositInstrument(ID: String,
                                 Quote: Double,
                                 PeriodName: String,
                                 CalendarName: String,
                                 BDConventionName: String ,
                                 DayCountName: String)

    @SQObj
    case class VanillaLiborSwapInstrument(ID: String,
                                          Quote: Double,
                                          PeriodName: String,
                                          CalendarName : String,
                                          FrequencyName : String,
                                          BDConventionName : String,
                                          DayCountName : String,
                                          Index : SQReference)

    @SQObj
    case class VanillaOISSwapInstrument(ID: String,
                                        Quote: Double,
                                        PeriodName: String,
                                        CalendarName : String,
                                        FrequencyName : String,
                                        BDConventionName : String,
                                        DayCountName : String,
                                        Index : SQReference)
    @SQObj
    case class CollateralizedVanillaSwapInstrument(ID: String,
                                                   Quote: Double,
                                                   PeriodName: String,
                                                   CalendarName : String,
                                                   FrequencyName : String,
                                                   BDConventionName : String,
                                                   DayCountName : String,
                                                   Index : SQReference,
                                                   DiscountingCurve:SQReference)

    @SQObj
    case class YieldCurve(ID: String,
                          CalendarName: String,
                          DayCountName:String,
                          Instruments:List[SQReference])

    case class Schedule(StartDate:String,
                        EndDate:String,
                        Tenor:String,
                        CalendarName:String,
                        BDConventionName:String,
                        EndBDConventionName:String,
                        Rule: String,
                        EndOfMonth: Boolean)

    sealed trait Leg

    case class FixedLeg(Ccy : String,
                        Rate: Double,
                        Sched:Schedule,
                        DayCountName: String) extends Leg

    case class FloatLeg(Ccy: String,
                        Index: SQReference,
                        Sched:Schedule,
                        Spread: Double,
                        DayCountName: String) extends Leg

    @SQObj
    case class SwapTrade(ID: String,
                         PorR:String,
                         Notional:Double,
                         FixedLeg:FixedLeg,
                         FloatLeg:FloatLeg,
                         CSA: String,
                         var PricingConfig:SQReference,
                         var ConfigLinks:List[SQReference])


    @SQObj
    case class Mapping(ID:String, Pairs: Map[String,String])

    case class ConditionalReference(SelectorID:String)


    // Schema Functions

    import QLTypeMapping._

    def determineConfig (obj:SQObject)(implicit ctx:Context) = {
      obj match {
        case x:SwapTrade => {

          x.CSA match {
            case "EUR_CASH_COLLATERAL" => {
              val configObj = new SQReference("OISEurCollateral", "Mapping")
              x.ConfigLinks ::= SQReference(configObj.follow.asInstanceOf[Mapping].Pairs("DiscountCurve_EUR"), "YieldCurve")
              x.ConfigLinks ::= SQReference(configObj.follow.asInstanceOf[Mapping].Pairs("ForwardCurve_Euribor6M"), "YieldCurve")
              x.PricingConfig = configObj
            }
            case _ =>
          }
        }
        case _ => throw new Exception("Unsupported trade type " + obj.TypeName)
      }
    }

    def loadFixings (obj:SQObject)(implicit ctx:Context) = {
      obj match {
        case x:Index => {

          val idx:InterestRateIndex = parseIndex(x.ID)

          x.Fixings foreach (f =>  {
            idx.addFixing(f.Date, f.Value)
          })

        }
      }
    }

    def buildCurve (obj:SQObject)(implicit ctx:Context) : YieldTermStructure = {

      var instrIndex=0

      val rateHelpers = Graph.visit(obj, (obj:SQObject) => {
        instrIndex += 1
        obj match {
          case x:DepositInstrument => {
            new DepositRateHelper(new QuoteHandle(new SimpleQuote(x.Quote)),
              x.PeriodName,
              instrIndex,
              x.CalendarName,
              x.DayCountName,
              true,
              x.DayCountName)
          }
          case x:VanillaOISSwapInstrument => {
            new OISRateHelper(instrIndex,x.PeriodName,
              new QuoteHandle(new SimpleQuote(x.Quote)),
              parseONIndex(x.Index.RefID))
          }
          case x:VanillaLiborSwapInstrument => {
            new SwapRateHelper(new QuoteHandle(new SimpleQuote(x.Quote)),
              x.PeriodName,
              x.CalendarName,
              x.FrequencyName,
              x.BDConventionName,
              x.DayCountName,
              parseIndex(x.Index.RefID).asInstanceOf[IborIndex])
          }
          case x:CollateralizedVanillaSwapInstrument => {
            new SwapRateHelper(new QuoteHandle(new SimpleQuote(x.Quote)),
              x.PeriodName,
              x.CalendarName,
              x.FrequencyName,
              x.BDConventionName,
              x.DayCountName, parseIndex(x.Index.RefID).asInstanceOf[IborIndex],
              x.Index.follow.asInstanceOf[Index].Fixings.last.Value,
              "0D", QLSession.get[YieldTermStructureHandle](x.DiscountingCurve.RefID))
          }
          case _ => null
        }
      })

      val qlRateHelpers = new RateHelperVector()

      rateHelpers.foreach( r => qlRateHelpers.add(r._2))


      val curve = new PiecewiseCubicZero(1, obj.asInstanceOf[YieldCurve].CalendarName,
        qlRateHelpers, obj.asInstanceOf[YieldCurve].DayCountName)

      QLSession.store(obj.ID, new YieldTermStructureHandle(curve))

      curve
    }


    def buildTrade(trade:SQObject)(implicit ctx:Context) : org.quantlib.Instrument = {

      val result = trade match {

        case x:SwapTrade => {

          println("Adding QL Instrument obj for " + x.ID)

          val discountCurveID = x.PricingConfig.follow.asInstanceOf[Mapping]
            .Pairs("DiscountCurve_EUR");
          val fwdCurveID = x.PricingConfig.follow.asInstanceOf[Mapping]
            .Pairs("ForwardCurve_" + x.FloatLeg.Index.RefID);

          val idx = parseIndex(x.FloatLeg.Index.RefID, QLSession.get[YieldTermStructureHandle](fwdCurveID))
            .asInstanceOf[IborIndex]

          val swap = new VanillaSwap(x.PorR, x.Notional,
            x.FixedLeg.Sched,
            x.FixedLeg.Rate,
            x.FixedLeg.DayCountName,
            x.FloatLeg.Sched,
            idx,
            x.FloatLeg.Spread,
            x.FloatLeg.DayCountName)

          println("Adding QL PricingEngine for " + x.ID)

          val engine = new DiscountingSwapEngine(QLSession.get[YieldTermStructureHandle](discountCurveID))

          swap.setPricingEngine(engine)

          swap
        }
        case _ => throw new Exception("Unsupported trade type")
      }


      QLSession.store(trade.ID, result)

      result
    }

    def loadFromString(jsonstr:String)(implicit ctx:Context) : List[String] = {

      implicit val formats = serializationFormat()

      var idsLoaded = List[String]()

      parse(jsonstr).extract[Map[String,SQObject]] foreach {
        case x:(String,SQObject) => {
          println("Adding " + x._1 + " = " + x._2)
          ctx.objects += x._1 -> x._2
          idsLoaded ::= x._1
        }
      }

      idsLoaded
    }
  }

  object QLTypeMapping {

    implicit class Regex(sc: StringContext) {
      def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
    }

    implicit def parseCalendar(Name: String) : Calendar = Name match  {
      case "TARGET" => new TARGET
      case _ => new TARGET
    }

    implicit def parseBDConvention(Name: String) : BusinessDayConvention = Name match  {
      case "Unadjusted" => BusinessDayConvention.Unadjusted
      case _ =>  BusinessDayConvention.Unadjusted
    }

    implicit def parseDayCount(Name: String) : DayCounter = Name match  {
      case "Actual360" => new Actual360
      case "Actual365Fixed" => new Actual365Fixed
      case "AcutalActual" => new ActualActual
      case _ => new ActualActual
    }

    implicit def parsePeriod(Name: String) : Period = Name match {
      case r"(\d+)${num}D" => new Period(num.toInt, TimeUnit.Days)
      case r"(\d+)${num}W" => new Period(num.toInt, TimeUnit.Weeks)
      case r"(\d+)${num}M" => new Period(num.toInt, TimeUnit.Months)
      case r"(\d+)${num}Y" => new Period(num.toInt, TimeUnit.Years)
    }

    implicit def parseFrequency(Name: String) : Frequency = Name match  {
      case "Annual" => Frequency.Annual
      case "Daily" => Frequency.Daily
      case "Weekly" => Frequency.Weekly
      case "Seminannual" => Frequency.Semiannual
      case "Monthly" => Frequency.Monthly
      case "Quarterly" => Frequency.Quarterly
      case _ => Frequency.NoFrequency
    }

    implicit def parseIndex(Name: String) : InterestRateIndex = Name match {
      case "Euribor1M" => new Euribor1M
      case "Euribor3M" => new Euribor3M
      case "Euribor6M" => new Euribor6M
      case "Euribor1Y" => new Euribor1Y
      case "EONIA" => new Eonia
      case "SONIA" => new Sonia
      case "FEDFUNDS" =>  new FedFunds
      case _ => throw new Exception("Invalid Index specified:" + Name)
    }

    implicit def parseIndex(Name: String, yts: YieldTermStructureHandle) : InterestRateIndex = Name match {
      case "Euribor1M" => new Euribor1M(yts)
      case "Euribor3M" => new Euribor3M(yts)
      case "Euribor6M" => new Euribor6M(yts)
      case "Euribor1Y" => new Euribor1Y(yts)
      case "EONIA" => new Eonia(yts)
      case "SONIA" => new Sonia(yts)
      case "FEDFUNDS" =>  new FedFunds(yts)
      case _ => throw new Exception("Invalid Index specified:" + Name)
    }

    implicit def parseONIndex(Name: String) : OvernightIndex = Name match {
      case "EONIA" => new Eonia
      case "SONIA" => new Sonia
      case "FEDFUNDS" =>  new FedFunds
      case _ => throw new Exception("Invalid Index specified:" + Name)
    }

    implicit def toQuoteHandle(quote: Double) : QuoteHandle = new QuoteHandle(new SimpleQuote(quote))

    implicit def parsePorR(Name:String) : _VanillaSwap.Type  = Name match {
      case "Payer" => VanillaSwap.Payer
      case "Reciever" => VanillaSwap.Receiver
      case _ => throw new Exception("Invalid value for Pay/Receive Flag.")
    }

    implicit def parseRule(Name:String) : DateGeneration.Rule  = Name match {
      case "Forward" => DateGeneration.Rule.Forward
      case _ => throw new Exception("Invalid date generation rule.")
    }

    implicit def parseDate(date:String) : Date = org.quantlib.DateParser.parseISO(date)

    implicit def toQLSched(s:Schema.Schedule) : org.quantlib.Schedule = {
      new org.quantlib.Schedule(
        s.StartDate,
        s.EndDate,
        s.Tenor,
        s.CalendarName,
        s.BDConventionName,
        s.EndBDConventionName,
        s.Rule,
        s.EndOfMonth
      )
    }
  }

  object QLSession {
    var objects : Map[String,Object] = Map()

    def store(ID:String, o:Object) = {
      objects += ID->o
    }

    def get[T](ID:String) : T = {
      objects(ID).asInstanceOf[T]
    }
  }

}