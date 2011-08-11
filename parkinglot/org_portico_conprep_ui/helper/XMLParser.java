/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	XMLParser.java
 * Created on 		Jan 24, 2005
 *
 */
package org.portico.conprep.ui.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Description	Utility class for parsing XML
 * 				includes a standlone tester method also
 * Author		pramaswamy
 * Type			XMLParser
 */
public class XMLParser {
	/**
	 *
	 */
	public static void main(String arg[]) {
		Hashtable mesgResultSet = new Hashtable();
		HelperClass.porticoOutput(1, "" + (String)mesgResultSet.get("trial"));
//		try {
//			 InputStream is = new BufferedInputStream(
//				 new FileInputStream("C:/Documents and Settings/pramaswamy/My Documents/priya/ConPrep/07/usermessagewiley.xml"));
//			if(is.available()>0) {
//				HelperClass.porticoOutput(1, "available >0 ");
//			}
			//fetch all values of given element
			//ArrayList arrayList =  XMLParser.lookupMultipleNodeValues(is,"MessageSet.Message.Text");
			//fetch all children of given node+attr
//			ArrayList arrayList =  XMLParser.lookupMultipleNodeValues(is,"MessageSet.Message[Id=TOOL-CHECKSUM-2]");
			//fetch all values of given attr
			//ArrayList arrayList =  XMLParser.lookupMultipleNodeValues(is,"MessageSet.Message[Id]");
//			HelperClass.porticoOutput(1, "MessageSet.Message[Id] " + arrayList.toString());
//			is.close();//calling program is responsible for closing the stream
//		 } catch (IOException e) {
//		 	e.printStackTrace();
//		 }
	}


	public static void godeep(NodeList nodeList,String token, ListIterator li,ArrayList arrayList){
		if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "godeep nodeList "+nodeList +" token "+token+" st "+li.nextIndex());
			int nodesCount = nodeList.getLength();
			//HelperClass.porticoOutput(1, "nodesCount "+nodesCount);
			for(int k=0;k<nodesCount;k++){
				Node node = nodeList.item(k);
				if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\tfor k="+k+" token "+token+" nodename "+node.getNodeName());
				if(node.getNodeName().equals(token)){
					if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\tif 1 token matched");
					if(li.hasNext()){
						if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\t\tif 2 has more tokens - go deep");
						NodeList newNodeList = node.getChildNodes();
						godeep(newNodeList,(String)li.next(),li,arrayList);
					}// has more tokens, go deep
					else {
						String temp=node.getFirstChild().getNodeValue();
						arrayList.add(temp);
						if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\t\telse 2 last token, going up, fetched data - nodename "+node.getNodeName()+" values "+temp);
						NodeList newNodeList = node.getChildNodes();
						godeep(newNodeList,(String)li.previous(),li,arrayList);
					}//no more tokens, fetch data, go up
				}//if token matched
				else {
					int startIdx=token.indexOf('[');
					int endIdx=token.indexOf(']',startIdx);
					if(startIdx>-1 && endIdx>-1){
						String nodeName = token.substring(0,startIdx);
						if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "nodeName "+nodeName+ " token "+token  + " startIdx "+startIdx+" endIdx "+endIdx);
						if((node.getNodeName().equals(nodeName)) && (node.hasAttributes()) ){
							String keyvalue = token.substring(startIdx+1,endIdx);
							int valueIdx = keyvalue.indexOf('=');
							if(valueIdx>-1){
								String key=keyvalue.substring(0,valueIdx);
								String value= keyvalue.substring(valueIdx+1);
								NamedNodeMap namedNodeMap = node.getAttributes();
								Node attrNode = namedNodeMap.getNamedItem(key);
								if(attrNode.getNodeValue().equals(value)){
									if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\tif 3 token matched, attr matched "+attrNode + " value "+value);
									Hashtable hashtable = new Hashtable();
									hashtable.put(key,value);
									fetchAllChildren(node,hashtable);
									arrayList.add(hashtable);
								}//attr value matches
							}//searching for attr+value
							else {
								NamedNodeMap namedNodeMap = node.getAttributes();
								Node attrNode = namedNodeMap.getNamedItem(keyvalue);
								arrayList.add(attrNode.getNodeValue());
								if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\tif 4 token matched, attr matched "+attrNode + " value "+attrNode.getNodeValue());
							}//searching for values of an attr
						}//node name matches & has attr
					}//searching for attr
				}//if token not matched
			}//for each node
	}//godeep
	public static void fetchAllChildren(Node node,Hashtable hashtable){
		if(node.hasChildNodes()){
			NodeList nodeList = node.getChildNodes();
			int count = nodeList.getLength();
			for(int k=0;k<count;k++){
				Node child = nodeList.item(k);
				if((child.getNodeType()==1) && (child.hasChildNodes())){
					hashtable.put(child.getNodeName(),child.getFirstChild().getNodeValue());
					if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "\t\t\tfetchAllChildren k "+k+" "+ child.getNodeName() + " type "+ child.getNodeType() + " child val " + child.getFirstChild().getNodeValue());
				}//actual data node
			}// for each child
		}//has children
	}
	public static ArrayList lookupMultipleNodeValues(InputStream is,String lookupstr){
		ArrayList arrayList = new ArrayList();
		try {
			if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "in XMLParser lookupMultipleNodeValues lookupstr "+lookupstr);
			DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory.newInstance();
			documentbuilderfactory.setNamespaceAware(true);
			DocumentBuilder documentbuilder = documentbuilderfactory.newDocumentBuilder();
			InputSource inputsource = new InputSource(is);
			Document document = documentbuilder.parse(inputsource);

			StringTokenizer st = new StringTokenizer(lookupstr,".");
			List list = Collections.synchronizedList(new ArrayList());

			while(st.hasMoreElements()){
				list.add(st.nextElement());
			}
			if(XMLParser.debugRecursion) HelperClass.porticoOutput(1, "tokens of lookup string "+list);
			ListIterator li = list.listIterator();
			NodeList nodeList = document.getChildNodes();
			godeep(nodeList,(String)li.next(),li,arrayList);
		}
		catch(ParserConfigurationException parserconfigurationexception) {
			parserconfigurationexception.printStackTrace();
		}
		catch(SAXException saxexception) {
			saxexception.printStackTrace();
		}
		catch(IOException ioexception) {
			ioexception.printStackTrace();
		}finally{
		}
		return arrayList;
	}
	public static boolean debugRecursion = false;

}

//com.documentum.web.formext.config.ConfigFile
//ErrorListener errorlistener = new ErrorListener();
//documentbuilder.setErrorHandler(errorlistener);
//if(errorlistener.getNumberOfErrors() > 0 || errorlistener.getNumberOfWarnings() > 0)
//throw new IllegalStateException(errorlistener.getWarningErrorMessages());
