/**
  * Created by d90590 on 03.06.2016.
  */

import scala.io.Source
import org.quantlib.{Array=>QArray,_}
import org.quil.objects._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{write}


object Application {


  def main(args: Array[String]) = {

    //////
    import org.quil.objects.Schema._

    println("Loading configuration...")
    val configStr = Source.fromFile("config.json").getLines.mkString
    loadFromString(configStr)

    System.loadLibrary("QuantLibJNI")
    Settings.instance().setEvaluationDate(QLTypeMapping.parseDate("2016-06-03"))

    println("Loading trades...")
    val tradeStr = Source.fromFile("trades.json").getLines.mkString
    val idsLoaded = loadFromString(tradeStr)

    // Add trades to initial object
    object initObject extends SQObject("InitObject") {
      val ID = "InitObject"
      val trades = idsLoaded.map( x=> SQReference(x, "SwapTrade"));
    }

    // Determine Pricing Config for all trades
    Graph.applyOnce(initObject, (obj: SQObject) =>  {

      obj match {
        case x:SwapTrade =>  {
          println("Preparing  trade " + x.ID)
          determineConfig(x)}

        case _ =>
      }

    })

    // Load Fixings, build curves and price the trades
    val res = Graph.applyOnce(initObject, (obj: SQObject) =>  {

      obj match {
        case x:Index =>      {println("Loading fixings for " + x.ID)
                              loadFixings(x) }

        case x:YieldCurve => {println("Building  curve " + x.ID)
                              buildCurve(x) }

        case x:SwapTrade =>  {println("Building trade " + x.ID)
                              buildTrade(x)
                              println("Pricing trade " + x.ID)
                              QLSession.get[Instrument](x.ID).NPV()}
        case _ => null
      }

    })

    res foreach println _
  }
}



/* val dayCounterACT360 = new Actual360
 val calTarget = new TARGET
 val todaysDate = new Date(23, Month.May, 2006)
 val settlementDate = new Date(25, Month.May, 2006)

 val period1D = new Period(1, TimeUnit.Days)
 val period1Y = new Period(1, TimeUnit.Years)
 val period2Y = new Period(2, TimeUnit.Years)

 val quote_mm_1d = new SimpleQuote(0.02)
 val quote_mm_1d_handle = new QuoteHandle(quote_mm_1d)

 val rateHelper_mm_1d = new DepositRateHelper(quote_mm_1d_handle,
   period1D,
   1,
   calTarget,
   BusinessDayConvention.ModifiedFollowing,
   true,
   dayCounterACT360)

 val quote_swp_1y = new SimpleQuote(0.04)
 val quote_swp_1y_handle = new QuoteHandle(quote_swp_1y)

 val rateHelper_swp_1y = new SwapRateHelper(quote_swp_1y_handle,
   period1Y,
   calTarget,
   Frequency.Annual,
   BusinessDayConvention.Unadjusted,
   dayCounterACT360, new Euribor6M)

 val quote_swp_2y = new SimpleQuote(0.05)
 val quote_swp_2y_handle = new QuoteHandle(quote_swp_2y)

 val rateHelper_swp_2y = new SwapRateHelper(quote_swp_2y_handle,
   period2Y,
   calTarget,
   Frequency.Annual,
   BusinessDayConvention.Unadjusted,
   dayCounterACT360, new Euribor6M)


 var euribor3M = new Euribor3M

 val rateHelpers = new RateHelperVector

 rateHelpers.add(rateHelper_mm_1d)
 rateHelpers.add(rateHelper_swp_1y)
 rateHelpers.add(rateHelper_swp_2y)

 val quoteHandles = new QuoteHandleVector
 quoteHandles.add(quote_swp_1y_handle)
 quoteHandles.add(quote_swp_2y_handle)


 val curve = new PiecewiseLinearZero(1, calTarget,
   rateHelpers,
   dayCounterACT360)

*/
/* 	SwapRateHelper (const Handle< Quote > &rate,
 const Period &tenor,
 const Calendar &calendar,
 Frequency fixedFrequency,
 BusinessDayConvention fixedConvention,
 const DayCounter &fixedDayCount,
 const boost::shared_ptr< IborIndex > &iborIndex,
 const Handle< Quote > &spread=Handle< Quote >(),
 const Period &fwdStart=0 *Days, const Handle< YieldTermStructure > &discountingCurve=Handle< YieldTermStructure >(),
 Natural settlementDays=Null< Natural >(),
 Pillar::Choice pillar=Pillar::LastRelevantDate, Date customPillarDate=Date()))*/

/*
   val discountingCurve = new YieldTermStructureHandle(curve)


    val rateHelper_swp_1y_dep = new SwapRateHelper(quote_swp_2y_handle,
      period2Y,
      calTarget,
      Frequency.Annual,
      BusinessDayConvention.Unadjusted,
      dayCounterACT360, new Euribor6M,
      new QuoteHandle(new SimpleQuote(0.0)),
      new Period(0, TimeUnit.Days), discountingCurve)

    val rateHelpersFwd = new RateHelperVector

    rateHelpersFwd.add(rateHelper_swp_1y_dep)


    val curveFwd = new PiecewiseCubicZero(1, calTarget,
      rateHelpersFwd,
      dayCounterACT360)
*/

/*
    results.foreach( R =>  {println(R); R._2 match {

        case x: YieldTermStructure => {
          println("Curve: " + R._1)

          (0.0 to 5.0 by 0.1).foreach(
            t =>
              print(t + "\t" + x.zeroRate(t, Compounding.Continuous, Frequency.Annual, true).rate + "\n")
          )
      }
      case x:org.quantlib.VanillaSwap => {
        println(x)
      }
    }})*/


/*
Index(ID = "Euribor6M", Fixings = List(
        Fixing("2016-05-30",0.001)
      )
    )
    Index(ID = "EONIA", Fixings = List(
        Fixing("2016-05-30",0.001)
      )
    )

    DepositInstrument(ID = "EUR_OIS_MM_1D",
                      Quote = 0.001,
                      PeriodName =  "1D",
                      CalendarName = "TARGET",
                      BDConventionName =  "ModifiedFollowing",
                      DayCountName = "Actual360")


    VanillaOISSwapInstrument(ID = "EUR_OIS_SWP_1M",
                             Quote = 0.002,
                             PeriodName = "1M",
                             CalendarName = "TARGET",
                             FrequencyName = "Annual",
                             BDConventionName =  "ModifiedFollowing",
                             DayCountName = "Actual360",
                             Index = SQReference("EONIA", ""))

    VanillaOISSwapInstrument(ID = "EUR_OIS_SWP_1Y",
                             Quote = 0.01,
                             PeriodName = "1Y",
                             CalendarName = "TARGET",
                             FrequencyName = "Annual",
                             BDConventionName =  "ModifiedFollowing",
                             DayCountName = "Actual360",
                             Index = SQReference("EONIA", ""))

    VanillaOISSwapInstrument(ID = "EUR_OIS_SWP_2Y",
                             Quote = 0.02,
                             PeriodName = "2Y",
                             CalendarName = "TARGET",
                             FrequencyName = "Annual",
                             BDConventionName =  "ModifiedFollowing",
                             DayCountName = "Actual360",
                             Index = SQReference("EONIA", ""))

    VanillaOISSwapInstrument(ID = "EUR_OIS_SWP_5Y",
                             Quote = 0.03,
                             PeriodName = "5Y",
                             CalendarName = "TARGET",
                             FrequencyName = "Annual",
                             BDConventionName =  "ModifiedFollowing",
                             DayCountName = "Actual360",
                             Index = SQReference("EONIA", ""))

    CollateralizedVanillaSwapInstrument(ID = "EUR_6M_SWP_1Y",
                                        Quote = 0.01,
                                        PeriodName = "1Y",
                                        CalendarName = "TARGET",
                                        FrequencyName = "Annual",
                                        BDConventionName =  "ModifiedFollowing",
                                        DayCountName = "Actual360",
                                        Index = SQReference("Euribor6M", ""),
                                        DiscountingCurve = SQReference("EUR_OIS", ""))

    CollateralizedVanillaSwapInstrument(ID = "EUR_6M_SWP_2Y",
                                        Quote = 0.02,
                                        PeriodName = "2Y",
                                        CalendarName = "TARGET",
                                        FrequencyName = "Annual",
                                        BDConventionName =  "ModifiedFollowing",
                                        DayCountName = "Actual360",
                                        Index = SQReference("Euribor6M", ""),
                                        DiscountingCurve = SQReference("EUR_OIS", ""))

    CollateralizedVanillaSwapInstrument(ID = "EUR_6M_SWP_5Y",
                                        Quote = 0.03,
                                        PeriodName = "5Y",
                                        CalendarName = "TARGET",
                                        FrequencyName = "Annual",
                                        BDConventionName =  "ModifiedFollowing",
                                        DayCountName = "Actual360",
                                        Index = SQReference("Euribor6M", ""),
                                        DiscountingCurve = SQReference("EUR_OIS", ""))


    YieldCurve(ID = "EUR_OIS",
               CalendarName = "TARGET",
               DayCountName = "Actual365Fixed",
               Instruments = List(SQReference("EUR_OIS_MM_1D", "DepositInstrument"),
                                  SQReference("EUR_OIS_SWP_1M", "VanillaOISSwapInstrument"),
                                  SQReference("EUR_OIS_SWP_1Y", "VanillaOISSwapInstrument"),
                                  SQReference("EUR_OIS_SWP_2Y", "VanillaOISSwapInstrument"),
                                  SQReference("EUR_OIS_SWP_5Y", "VanillaOISSwapInstrument")))

    YieldCurve(ID = "EUR_FWD_6M",
               CalendarName = "TARGET",
               DayCountName = "Actual365Fixed",
               Instruments = List(SQReference("EUR_OIS_MM_1D", "DepositInstrument"),
                                  SQReference("EUR_6M_SWP_1Y", "VanillaSwapInstrument"),
                                  SQReference("EUR_6M_SWP_2Y", "VanillaSwapInstrument"),
                                  SQReference("EUR_6M_SWP_5Y", "VanillaSwapInstrument")))

    Mapping(ID = "OISEurCollateral",
      Pairs = Map(
        "DiscountCurve_EUR" -> "EUR_OIS",
        "ForwardCurve_Euribor6M" -> "EUR_FWD_6M"
      )
    )


    val trade = SwapTrade(ID = "Swap_1",
              PorR = "Payer",
              Notional = 1000000.0,
              FixedLeg(
                Ccy = "EUR",
                Rate = 0.04,
                DayCountName = "Actual360",
                Sched = Schedule(StartDate = "2016-06-01", EndDate = "2020-06-01",
                                 Tenor = "1Y",
                                 CalendarName =  "TARGET",
                                 BDConventionName = "Unadjusted",
                                 EndBDConventionName = "Undajusted",
                                 Rule = "Forward",
                                 EndOfMonth = false)
              ),
              FloatLeg(
                Ccy = "EUR",
                Index = SQReference("Euribor6M","Index"),
                DayCountName = "Actual360",
                Sched = Schedule(StartDate = "2016-06-01", EndDate = "2020-06-01",
                  Tenor = "6M",
                  CalendarName =  "TARGET",
                  BDConventionName = "Unadjusted",
                  EndBDConventionName = "Undajusted",
                  Rule = "Forward",
                  EndOfMonth = false),
                Spread = 0.0001
              ),
      CSA = "EUR_CASH_COLLATERAL",
      PricingConfig = SQReference("", ""),
      ConfigLinks = List()
    )
 */