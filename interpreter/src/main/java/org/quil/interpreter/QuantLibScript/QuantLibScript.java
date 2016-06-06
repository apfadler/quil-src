package org.quil.interpreter.QuantLibScript;

import org.json.simple.JSONObject;
import org.quil.interpreter.QuantLibTemplates.Market;
import org.quil.JSON.Document;

public class QuantLibScript {

	//TODO change to Document
	protected Document tradeData;
	protected Market marketData = new Market();
	
	public void setTradeData(Document tradeData) {
		this.tradeData = tradeData;
	}
	
	public void setMarketData(Market marketData) {
		this.marketData = marketData;
	}

	public org.quil.JSON.Document run() {
		return null;
	}
}
