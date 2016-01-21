package org.quil.interpreter.MoCoTemplates;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.persistence.sessions.serializers.XMLSerializer;
import org.json.simple.JSONObject;
import org.quil.JSON.Document;
import org.quil.interpreter.Interpreter;
import org.quil.server.SimpleCache;
import org.quil.server.Tasks.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.json.simple.JSONArray;

import com.dfine.moco.*;

public class MoCoXmlTemplateInterpreter implements Interpreter {

	private boolean _error = false;


	final static Logger logger = LoggerFactory.getLogger(TaskRunner.class);

	protected JSONObject _data = new JSONObject();
	protected JSONObject _result = new JSONObject();

	public MoCoXmlTemplateInterpreter() {
	}

	@Override
	public void interpret() throws Exception {
		logger.info("Running task: " +_data.toJSONString());

		String repository = (String) _data.get("Repository");
		if (repository == null) {
			throw new Exception("Empty repository in task definition.");
		}

		String template = (String) _data.get("Template");
		if (template == null) {
			throw new Exception("Empty Template in task definition.");
		}

		String templateContent = SimpleCache.getOrCreate(repository).get(template);
		if (templateContent == null) {
			throw new Exception("No such template");
		}


		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.parse(new InputSource( new StringReader( templateContent ) ));
		doc.getDocumentElement().normalize();
		
		HashMap<String, Node> inputParameter = new HashMap<String, Node>();
		HashMap<String,String> inputTypes = new HashMap<String,String>();
		NodeList nodes = doc.getElementsByTagName("InputParameter");
		for (int i = 0; i < nodes.getLength(); i++) {
			
			Element nameNode = getDirectChild((Element)(nodes.item(i)),"Name");
			Element defaultNode = getDirectChild((Element)nodes.item(i),"Default");
			Element typeNode = getDirectChild((Element)nodes.item(i),"Type");
			
			inputParameter.put(nameNode.getFirstChild().getTextContent(), defaultNode);
			inputTypes.put(nameNode.getFirstChild().getTextContent(), typeNode.getFirstChild().getTextContent());
		
			logger.info("Found Parameter:" + nameNode.getFirstChild().getTextContent() );
		}
		
		JSONObject tradeData = (JSONObject) _data.get("TradeData");
		if (tradeData != null) {

			nodes = doc.getElementsByTagName("InjectParameter");
			for (int i = 0; i < nodes.getLength(); i++) {
	
				for(Iterator iterator = tradeData.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					
					if (key.compareTo(nodes.item(i).getTextContent()) == 0) {
						logger.info("Found target: " + nodes.item(i).getTextContent() );
						
						if (tradeData.get(key) == null ) {
							
							logger.info("Setting value to default.");
							nodes.item(i).getParentNode().replaceChild(inputParameter.get(nodes.item(i).getTextContent()), nodes.item(i));
						
						} else {
							
							if (inputTypes.get(key).compareTo("Double") == 0)
							{
								logger.info("Setting value to double " + tradeData.get(key).toString());
								nodes.item(i).getParentNode().replaceChild(JSONObjectToXMLDoubleParameter(doc, (Double) tradeData.get(key)), nodes.item(i) );
							}
							
							if (inputTypes.get(key).compareTo("Integer") == 0)
							{
								logger.info("Setting value to integer " + tradeData.get(key).toString());
								nodes.item(i).getParentNode().replaceChild(JSONObjectToXMLIntegerParameter(doc, (Integer) tradeData.get(key)), nodes.item(i) );
							}
							
							if (inputTypes.get(key).compareTo("Boolean") == 0)
							{
								logger.info("Setting value to double " + tradeData.get(key).toString());
								nodes.item(i).getParentNode().replaceChild(JSONObjectToXMLBooleanParameter(doc, (Boolean) tradeData.get(key)), nodes.item(i) );
							}
							
							if (inputTypes.get(key).compareTo("String") == 0)
							{
								logger.info("Setting value to string " + tradeData.get(key).toString());
								nodes.item(i).getParentNode().replaceChild(JSONObjectToXMLStringParameter(doc, (String) tradeData.get(key)), nodes.item(i) );
							}
							
							if (inputTypes.get(key).compareTo("Matrix") == 0)
							{
								logger.info("Setting value to string " + tradeData.get(key).toString());
								
								nodes.item(i).getParentNode().replaceChild(JSONObjectToXMLMatrixParameter(doc, (JSONArray) tradeData.get(key)), nodes.item(i) );
								//ArrayList<Node> nodeList = JSONObjectToXMLMatrixParameter(doc, (JSONArray) tradeData.get(key));
								
								/*Node parentNode = nodes.item(i).getParentNode();
								if (nodeList.size() > 0)
									parentNode.replaceChild(nodeList.get(0), nodes.item(i) );
								
								for (int j=1; j < nodeList.size(); j++)
								{
									parentNode.appendChild(nodeList.get(j));
								}*/
								
							}
							
						}
					}
				}       
			}
		}
		
		removeElements(doc,"InputParameter");
		removeElements(doc,"OutputParameter");

		ByteArrayOutputStream outputXMLWriter = new ByteArrayOutputStream();
		printDocument(doc, outputXMLWriter);
		printDocument(doc, System.out);

		try {
			MoCoLoader.loadMoco();
			long sessID = MoCoSessionWrapper.createSession();


			JSONObject marketData = (JSONObject) _data.get("MarketData");
			if (marketData != null) {

				logger.info("Storing MoCo Market Data");

				String key =  (String) marketData.get("Key");
				if (key == null)  {
					throw new Exception("No 'Key' property");
				}

				String markets =  (String) marketData.get("Repository");
				if (key == null)  {
					throw new Exception("No 'Repository' property");
				}

				String moCoMarketData = SimpleCache.getOrCreate(markets).get(key);
				if (moCoMarketData == null)  {
					throw new Exception("No market data with ID '"+key+"' found.");
				}

				MoCoXmlLogWrapper.runMoCoXML(sessID, moCoMarketData);


				JSONObject overrideMarketData = (JSONObject) marketData.get("Additional");
				if (overrideMarketData != null) {
					for(Iterator iterator = overrideMarketData.keySet().iterator(); iterator.hasNext();) {
						String quote = (String) iterator.next();

						// TODO implement

						logger.info( "Market delta: " + key + " = " + (String)overrideMarketData.get(quote));
					}
				}

			}

			System.out.println(outputXMLWriter.toString());
			_result.put("MoCoResult", MoCoXmlLogWrapper.runMoCoXML(sessID, outputXMLWriter.toString()));

			MoCoSessionWrapper.deleteSession(sessID);

		} catch (Exception e) {
			_error = true;
			_result.put("ERROR", "Could not run MoCo Template.");
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

	
	public Element getDirectChild(Element parent, String name)
	{
	    for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
	    {
	        if(child instanceof Element && name.equals(child.getNodeName())) return (Element) child;
	    }
	    return null;
	}
	
	public static void printDocument(org.w3c.dom.Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	private Element JSONObjectToXMLStringParameter(org.w3c.dom.Document doc, String  val)
	{
		Element v = doc.createElement("v");
		v.appendChild(doc.createTextNode(val));
		v.setAttribute("type", "string");
		
		return v;
	}
	
	private Element JSONObjectToXMLIntegerParameter(org.w3c.dom.Document doc, Integer val)
	{
		Element v = doc.createElement("v");
		v.appendChild(doc.createTextNode(val.toString()));
		v.setAttribute("type", "int");
		
		return v;
	}
	
	private Element JSONObjectToXMLDoubleParameter(org.w3c.dom.Document doc, Double val)
	{
		Element v = doc.createElement("v");
		v.appendChild(doc.createTextNode(val.toString()));
		v.setAttribute("type", "string");

		return v;
	}
	
	private Element JSONObjectToXMLBooleanParameter(org.w3c.dom.Document doc, Boolean val)
	{
		Element v = doc.createElement("v");
		v.appendChild(doc.createTextNode(val.toString()));
		v.setAttribute("type", "bool");

		return v;
	}
	
	
	private Element JSONObjectToXMLMatrixParameter(org.w3c.dom.Document doc, JSONArray val)
	{
		//TODO Implement
		
		ArrayList<Node> returnList = new ArrayList<Node> ();
		
		JSONArray rows = (JSONArray) val;
		Integer colNum=0; Integer rowNum=0;
		for (Iterator rowIterator = val.iterator(); rowIterator.hasNext(); ) {
			rowNum++;
			
			JSONArray cols = (JSONArray) rowIterator.next();
			
			Element r = doc.createElement("r");
			
			for (Iterator colIterator = cols.iterator(); colIterator.hasNext(); ) {
				colNum++;
				
				Double colValue = (Double) colIterator.next();
				
				Element c = doc.createElement("c");
				c.appendChild(doc.createTextNode(colValue.toString()));
				
				r.appendChild(c);
			}
			
			returnList.add(r);
		}
		
		Element v = doc.createElement("Parameter");
		Attr type = doc.createAttribute("type");
		type.setValue("matrix");
		Attr name = doc.createAttribute("name");
		name.setValue("matr");
		Attr colsize = doc.createAttribute("cols");
		colsize.setValue(colNum.toString());
		Attr rowsize = doc.createAttribute("rows");
		rowsize.setValue(rowNum.toString());
		
		v.setAttribute("type", "matrix");
		v.setAttribute("name", "matr");
		v.setAttribute("cols", colNum.toString());
		v.setAttribute("rows", rowNum.toString());
		
		for (Node n : returnList)
			v.appendChild(n);
		
		return v;
	}
	
	private void removeElements( Node parent, String filter ){
		NodeList children = parent.getChildNodes();

		for( int i=0; i < children.getLength(); i++ ){
			Node child = children.item( i );

			// only interested in elements
			if( child.getNodeType() == Node.ELEMENT_NODE ){

				// remove elements whose tag name  = filter
				// otherwise check its children for filtering with a recursive call
				if( child.getNodeName().equals(filter) ){
					parent.removeChild( child );
				} else {
					removeElements( child, filter );
				}
			}
		}
	}

}