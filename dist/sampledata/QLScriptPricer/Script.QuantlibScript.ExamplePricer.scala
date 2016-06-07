import org.quil.JSON._
import org.quil.interpreter.QuantLibScript._
import org.quantlib._


class Script extends QuantLibScript {

    override def run : Document = {
    
        val dayCounter     = new ActualActual();
        val settlementDate = new Date(27, Month.December, 2004)
        Settings.instance setEvaluationDate settlementDate

        val exerciseDate = new Date(28, Month.March, 2005)
        val maturity = dayCounter.yearFraction(settlementDate, exerciseDate)

        val payoff = new PlainVanillaPayoff(Option.Type.Call, 1.05)
        val exercise = new EuropeanExercise(exerciseDate)
        val vanillaOption = new VanillaOption(payoff, exercise)

        val rTS = new YieldTermStructureHandle(
            new FlatForward(settlementDate, 0.0225, dayCounter))

        val divTS = new YieldTermStructureHandle(
            new FlatForward(settlementDate, 0.02, dayCounter))
       
        val s0 = new QuoteHandle(new SimpleQuote(1.0))

        val v0    =  0.1
        val kappa =  3.16
        val theta =  0.09
        val sigma =  0.4
        val rho   = -0.2

        val hestonProcess = new HestonProcess(rTS, divTS, s0, v0, 
                                              kappa, theta, sigma, rho)

        val hestonModel = new HestonModel(hestonProcess)
        val analyticEngine = new AnalyticHestonEngine(hestonModel)
        vanillaOption.setPricingEngine(analyticEngine)
        
        println("Notional = " + tradeData.Notional.asString.toDouble)
        
        
        Document(
            Map(
                "PV" -> (vanillaOption.NPV() )
            )
        )
    
    }

}