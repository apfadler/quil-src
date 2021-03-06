package org.quil.server.Tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.server.DocumentCache;
import org.quil.server.ResultsCache;

public class PricePortfolio extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PricePortfolio(String taskName, String taskXML) {
		super(taskName, taskXML);
	}

	@Override
	public void run() throws Exception {

		Task.updateStatus(_taskName, Status.RUNNING);

		JSONParser parser = new JSONParser();

		final JSONObject taskDescription = (JSONObject) parser.parse(_taskDescription);

		String trades = (String) taskDescription.get("Trades");
		String predicate = (String) taskDescription.get("Where");

		List<Document> tradeList = DocumentCache.getOrCreate(trades).filter(predicate);
		
		ArrayList<IgniteCallable<JSONObject>> jobs = new ArrayList<IgniteCallable<JSONObject>> ();

		for (final Document trade : tradeList) {

			jobs.add (new IgniteCallable<JSONObject>() {
				@Override
				public JSONObject call() throws Exception {
					Interpreter interpreter = (Interpreter) Class.forName((String) taskDescription.get("Interpreter")).newInstance();

					JSONObject subTask = new JSONObject();
					subTask.put("Template", (String)taskDescription.get("Template"));
					subTask.put("PricerScript", (String)taskDescription.get("PricerScript"));
					subTask.put("Repository", (String)taskDescription.get("Repository"));
					subTask.put("MarketData", taskDescription.get("MarketData"));
					subTask.put("TradeData", trade.toString());
					subTask.put("Resources", (String)taskDescription.get("Resources"));
					subTask.put("ValuationDate", (String)taskDescription.get("ValuationDate"));
					subTask.put("OutputTemplate", (String)taskDescription.get("OutputTemplate"));

					interpreter.setData(subTask);
					interpreter.interpret();
					
					if (interpreter.getError()) {
						Task.updateStatus(_taskName, Task.Status.ERROR);
					}

					String Id = null;
					try {
						Id = (String)trade.apply("Id");
					}catch (Exception e) {
					}

					if (Id == null) {
						try {
							Id = (String)trade.apply("ID");
						}catch (Exception e) {
						}
					}

					if (Id == null) {
						try {
							Id = (String)trade.apply("id");
						}catch (Exception e) {
						}
					}

					if (Id == null) Id = "";

					JSONObject res = interpreter.getResult();

					if (Id == "") {
						Id = (String)res.get("id");
						if (Id == null)
							Id = (String)res.get("ID");
						if (Id == null)
							Id = (String)res.get("Id");
					}

					res.put("Id", Id);
					return res;
				}
			});
		}
		
		Ignite ignite = Ignition.ignite();
        Collection<JSONObject> results = ignite.compute(ignite.cluster().forAttribute("ROLE", "worker")).call(jobs);
        
        int idx=1;
        Vector<String> resultsStr = new Vector<String>();
        for (JSONObject r : results) {
        	resultsStr.add(r.toJSONString());

			String Id = (String)r.get("Id");
        	
        	for (Object rr : r.keySet()) {
    			
    			String key = (String)rr;
    			double doubleVal = 0.0;
    			int intVal = 0;
    			String strVal = "";
    			try {
    				strVal = (String)r.get(key);
    				doubleVal = Double.parseDouble((String)r.get(key));
    				intVal = Integer.parseInt((String)r.get(key));

    			}catch(Exception e) {
    				
    			}
    			
    			ResultsCache.add(_taskName,  _taskTag, idx, Id,
    							  key, strVal,doubleVal,intVal);
    		}
        	
        	idx++;
        }

		Task.updateResult(_taskName, "[" + org.springframework.util.StringUtils.collectionToDelimitedString(results, ",") + "]");
	}

}
