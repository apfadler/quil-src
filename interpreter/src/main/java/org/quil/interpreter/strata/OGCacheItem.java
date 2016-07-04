package org.quil.interpreter.strata;

import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.calc.CalculationRules;
import com.opengamma.strata.calc.runner.CalculationFunctions;
import com.opengamma.strata.data.MarketData;

/**
 * Created by d90590 on 04.07.2016.
 */
public  class OGCacheItem {
    long _lastModified;
    MarketData _md;
    CalculationFunctions _fun;
    CalculationRules _rules;
    ReferenceData _refData;
}