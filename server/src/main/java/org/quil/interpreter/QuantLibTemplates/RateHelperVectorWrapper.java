package org.quil.interpreter.QuantLibTemplates;

import org.quantlib.RateHelper;
import org.quantlib.RateHelperVector;

public class RateHelperVectorWrapper {

	private RateHelperVector vector;
	
	public RateHelperVectorWrapper(RateHelper[] helpers)
	{
		vector = new RateHelperVector();
		for (RateHelper h : helpers)
		{
			vector.add(h);
		}
	}
	
	public RateHelperVector get()
	{
		return vector;
	}
	
}
