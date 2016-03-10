import org.quil.server.ObjectIndex
import java.io.File
import org.apache.commons.io.FilenameUtils;

println("Loading Market Data...")
ObjectIndex.add("SimpleCache", "Markets", "MarketData.Today", "/MoCo.MarketData.xml")

println("Loading Templates...")
ObjectIndex.add("SimpleCache", "Templates", "MoCo.PlainVanillaSwaption", "/Template.MoCo.PlainVanillaSwaption.xml")


println("Importing trades...")
for (file <- new File(System.getenv("QUIL_HOME")+"/sampledata/Trades").listFiles) { 
  var filename = FilenameUtils.getBaseName(file.toString())
  println("Adding trade " + filename)
  ObjectIndex.add("DocumentCache", "Trades", filename, "/Trades/"+filename+".json")
}
println("done")

