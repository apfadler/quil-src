import org.quil.server.Deployments

println("Initial Deployment...")

Deployments.add("SimpleCache", "MoCoMarkets", "MarketData.Today", "/MoCo.MarketData.xml")
Deployments.add("SimpleCache", "Templates", "MoCo.PlainVanillaSwaption", "/Template.MoCo.PlainVanillaSwaption.xml")

println("...done")