package org.quil.interpreter.strata;

/**
 * Created by d90590 on 28.06.2016.
 */

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.calc.*;
import com.opengamma.strata.calc.runner.CalculationFunctions;
import com.opengamma.strata.collect.io.IniFile;
import com.opengamma.strata.collect.io.ResourceLocator;
import com.opengamma.strata.data.ImmutableMarketData;
import com.opengamma.strata.data.ImmutableMarketDataBuilder;
import com.opengamma.strata.data.MarketData;
import com.opengamma.strata.measure.AdvancedMeasures;
import com.opengamma.strata.measure.Measures;
import com.opengamma.strata.measure.StandardComponents;
import com.opengamma.strata.product.Trade;
import com.opengamma.strata.product.TradeAttributeType;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.swap.SwapTrade;
import com.opengamma.strata.product.swap.type.FixedIborSwapConventions;
import com.opengamma.strata.report.ReportCalculationResults;
import com.opengamma.strata.report.trade.TradeReport;
import com.opengamma.strata.report.trade.TradeReportTemplate;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteFileSystem;
import org.apache.ignite.Ignition;
import org.apache.ignite.igfs.IgfsPath;
import org.apache.log4j.net.SyslogAppender;
import org.joda.beans.ser.JodaBeanSer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.interpreter.QuantLibScript.QuantLibScript;
import org.quil.interpreter.QuantLibTemplates.Market;
import org.quil.server.DocumentCache;
import org.quil.server.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.interpreter.QuantLibTemplates.Market;
import org.quil.server.DocumentCache;
import org.quil.server.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.ByteArrayOutputStream;
import java.sql.Ref;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class StrataInterpreter implements Interpreter {

    private boolean _error = false;

    final static Logger logger = LoggerFactory.getLogger(org.quil.interpreter.strata.StrataInterpreter.class);

    protected JSONObject _data = new JSONObject();
    protected JSONObject _result = new JSONObject();

    static HashMap<String, OGCacheItem> OGCache = new HashMap<String, OGCacheItem>();

    public StrataInterpreter() {
    }

    synchronized public static OGCacheItem getMD(String resourceRoot, LocalDate valuationDate) {

        Boolean useFromCache =  OGCache.containsKey(resourceRoot);

        Ignite ignite = Ignition.ignite();
        IgniteFileSystem fs = ignite.fileSystem("quil-igfs");


        if (useFromCache) {

            logger.info("Cache lookup.");

            OGCacheItem cache = OGCache.get(resourceRoot);

            if (fs.info(new IgfsPath(resourceRoot)).modificationTime() <= cache._lastModified) {
                logger.info("Using cache.");
                useFromCache = true;
            }

        }

        if (!useFromCache)
        {

            QuilMarketDataBuilder builder = QuilMarketDataBuilder.ofIgfsPath(new IgfsPath(resourceRoot));
            MarketData ogMarketData = builder.buildSnapshot(valuationDate);
            CalculationFunctions functions = StandardComponents.calculationFunctions();
            CalculationRules rules = CalculationRules.of(functions, builder.ratesLookup(valuationDate));
            ReferenceData refData = ReferenceData.standard();

            OGCacheItem cache = new OGCacheItem();

            cache._lastModified = fs.info(new IgfsPath(resourceRoot)).modificationTime();
            cache._md = ogMarketData;
            cache._fun = functions;
            cache._rules = rules;
            cache._refData = refData;

            OGCache.put(resourceRoot, cache);

            return cache;

        } else {

            OGCacheItem cache = OGCache.get(resourceRoot);

            return cache;
        }
    }

    @Override
    public void interpret() throws Exception {

        logger.info("Running task: " +_data.toJSONString());

        long start = System.currentTimeMillis();

        String resourceRoot = (String) _data.get("Resources");
        if (resourceRoot == null) {
            throw new Exception("Empty resourceRoot in task definition.");
        }

        String outputTemplate = (String) _data.get("OutputTemplate");
        if (outputTemplate == null) {
            throw new Exception("Empty outputTemplate in task definition.");
        }

        String valuationDateString = (String) _data.get("ValuationDate");
        if (valuationDateString == null) {
            throw new Exception("Empty resourceRoot in task definition.");
        }

        List<Trade> trades = new ArrayList<Trade>();

        String tradeData = (String) _data.get("TradeData");
        if (tradeData != null) {

            logger.info("Injecting trade parameters.");
            Trade trade = (Trade)JodaBeanSer.COMPACT.jsonReader().read(tradeData);
            trades.add(trade);
        }

        try(CalculationRunner runner = CalculationRunner.ofMultiThreaded()) {

            LocalDate valuationDate = LocalDate.parse(valuationDateString, DateTimeFormatter.ISO_LOCAL_DATE);

            List<Column> columns = ImmutableList.of(
                    Column.of(Measures.LEG_INITIAL_NOTIONAL),
                    Column.of(Measures.PRESENT_VALUE),
                    Column.of(Measures.LEG_PRESENT_VALUE),
                    Column.of(Measures.PV01_CALIBRATED_SUM),
                    Column.of(Measures.PAR_RATE),
                    Column.of(Measures.ACCRUED_INTEREST),
                    Column.of(Measures.PV01_CALIBRATED_BUCKETED));

            MarketData ogMarketData;
            CalculationFunctions functions;
            CalculationRules rules;
            ReferenceData refData;

            OGCacheItem cache = getMD(resourceRoot, valuationDate);
            ogMarketData = cache._md;
            functions = cache._fun;
            rules = cache._rules;
            refData = cache._refData;

            logger.info("OG Init took " + (System.currentTimeMillis()-start) + " ms");

            // calculate the results
            start = System.currentTimeMillis();
            Results results = runner.calculate(rules, trades, columns, ogMarketData, refData);
            logger.info("OG calc took " + (System.currentTimeMillis()-start) + " ms");

            // use the report runner to transform the engine results into a trade report
            ReportCalculationResults calculationResults =
                    ReportCalculationResults.of(valuationDate, trades, columns, results, functions, refData);

            ResourceLocator resourceLocator = ResourceLocator.ofIgfsFile(outputTemplate);
            IniFile ini = IniFile.of(resourceLocator.getCharSource());
            TradeReportTemplate tpl = TradeReportTemplate.load(ini);
            TradeReport tradeReport = TradeReport.of(calculationResults, tpl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tradeReport.writeCsv(baos);

            String[] lines = baos.toString().split("\\r?\\n");

            if (lines.length == 0) {
                throw new Exception("No result data.");
            }

            String header = lines[0];
            String[] cols = header.split(";|,");

            if (lines.length > 1) {
                for (int j=1; j < lines.length; j++) {
                    String[] values = lines[j].split(";|,");
                    JSONObject obj = new JSONObject();
                    for (int i=0; i < Math.min(values.length,cols.length); i++)
                    {
                        _result.put(cols[i],values[i]);
                    }
                }
            }


        } catch(Exception e) {
            _error = true;
            _result.put("ERROR", e.getStackTrace().toString());
        }
    }

    @Override
    public void setData(JSONObject data) {
        _data = data;
    }

    @Override
    public JSONObject getResult() {
        return _result;
    }

    @Override
    public boolean getError() {
        // TODO Auto-generated method stub
        return _error;
    }
}
