
// Determine Pricing Config for all trades
Steps ::= "Step_1" -> ((obj: SQObject) =>  {

    obj match {
        case x:SwapTrade =>  {
            println("Preparing  trade " + x.ID)
            determineConfig(x)}

        case _ =>
    }

})

// Load Fixings, build curves and price the trades
Steps ::= "Step_2" -> ((obj: SQObject) =>  {

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