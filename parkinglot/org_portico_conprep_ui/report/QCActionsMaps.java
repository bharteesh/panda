/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	QC Action Report
 * File           	QCActionsMaps.java
 * Created on 		Mar 8, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.helper.HelperClass;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * Description	This class does 2 things
 * 					reads process code display values into memory
 * 						because, action mapping xml file has process code labels
 * 					reads action mapping configuration file into memory
 * 				This object is an singleton
 * Author		pramaswamy
 * Type			QCActionsMaps
 */
public class QCActionsMaps {

	/**
	 * gives static reference to the instance
	 */
	public static QCActionsMaps getInstance(IDfSession iDfSession,String mapFile)  throws ParserConfigurationException, SAXException, IOException, DfException {
	   if(qCActionsMapsSingleInstance == null) {
		qCActionsMapsSingleInstance = new QCActionsMaps(iDfSession,mapFile);
	   }
	   return qCActionsMapsSingleInstance;
	}

	/**
	 * constructor to fetch data
	 */
	private QCActionsMaps(IDfSession iDfSession,String mapFile) throws ParserConfigurationException, SAXException, IOException, DfException {
	  try {
	    String actionconfigdborfs = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigdborfs");
	    if(actionconfigdborfs.equals("fs")) fetchActionMapsFromFS(iDfSession, mapFile);
	    else fetchActionMaps(iDfSession, mapFile);
		cuToggles = new Hashtable();
		inspectToggles = new Hashtable();
	  } catch(Exception exception)
		{
		    exception.printStackTrace();
		}
	}

	/**
	 * returns list of actions for the given context(ie object type) and code
	 */
	public Object[] getActions(String code){
		HelperClass.porticoOutput(0, "in QCActionsMaps.getActions(code="+code+")");
		OutcomeAction outcomeActionObj = getOutcomeActionObject(code);
		if(outcomeActionObj!=null){
			HelperClass.porticoOutput(0, outcomeActionObj.getActions().toString());
			return (Object[])(outcomeActionObj.getActions().keySet().toArray());
		}
		return new String[0];
	}
	public Object[] getReentryPt(String code){
		HelperClass.porticoOutput(0, "in QCActionsMaps.getReentryPt(code="+code+")");
		OutcomeAction outcomeActionObj = getOutcomeActionObject(code);
		if(outcomeActionObj!=null){
			HelperClass.porticoOutput(0, outcomeActionObj.getActions().values().toString());
			return (Object[])(outcomeActionObj.getActions().values().toArray());
		}
		return new String[0];
	}

	/**
	 * returns display label for process code
	private String getCodeValue(String code, String context){
		Hashtable ht = (Hashtable)codeValues.get(context);
		return (String)ht.get(code);

	}
	 */

	/**
	 * returns Action Item object for the given context(ie object type) and code
	 */
	public OutcomeAction getOutcomeActionObject(String code) {
		//HelperClass.porticoOutput(0, "in QCActionsMaps.getOutcomeActionObject(code="+code+")");
		boolean found = false;
		OutcomeAction outcomeActionObj = null;
		int outcomeActionCount = outcomeActionObjects.size();
		for(int k=0;k<outcomeActionCount;k++){
			outcomeActionObj = (OutcomeAction)outcomeActionObjects.get(k);
			//HelperClass.porticoOutput(0, "retrieved code="+outcomeActionObj.getCode());
			if(outcomeActionObj.getCode().equals(code)){
				found = true;
				break;
			}
		}
		if(found) return outcomeActionObj;
		return null;
	}

	/**
	 * fetches actionMapping.xml content and parse it as DOM tree
	 * to get details of each action item
	 */
	private void fetchActionMapsFromFS(IDfSession iDfSession,String mapFile) throws ParserConfigurationException, SAXException, IOException, DfException{
		try {
			HelperClass.porticoOutput("QCAM fetchActionMapsFromFS() start");
			HelperClass.porticoOutput(0, "fetchActionMapsFromFS() mapFile="+mapFile);
			File f1 = new File(mapFile);
			HelperClass.porticoOutput(0, "fetchActionMapsFromFS() retrieved mapFile file");
			HelperClass.porticoOutput(0, "fetchActionMapsFromFS() mapFile filesize="+f1.length());
			if(f1.length()>0){
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(f1);
				procDoc(doc);
			}//actionMapsObj has content
			HelperClass.porticoOutput("QCAM fetchActionMapsFromFS() end");
		}
		catch(ParserConfigurationException parserconfigurationexception) {
			throw parserconfigurationexception;
		}
		catch(SAXException saxexception) {
			throw saxexception;
		}
		catch(IOException ioexception) {
			throw ioexception;
		}
	}
	private void fetchActionMaps(IDfSession iDfSession,String mapFile) throws ParserConfigurationException, SAXException, IOException, DfException{
		ByteArrayInputStream bais = null;
		try {
			HelperClass.porticoOutput("QCAM fetchActionMaps() start");
			HelperClass.porticoOutput(0, "fetchActionMaps() mapFile="+mapFile);
			IDfDocument actionMapsObj = (IDfDocument)iDfSession.getObjectByPath(mapFile);
			HelperClass.porticoOutput(0, "fetchActionMaps() retrieved mapFile object");
			HelperClass.porticoOutput(0, "fetchActionMaps() mapFile objectsize="+actionMapsObj.getContentSize());
			if(actionMapsObj.getContentSize()>0){
				bais = actionMapsObj.getContent();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(bais);
				procDoc(doc);
			}//actionMapsObj has content
			HelperClass.porticoOutput("QCAM fetchActionMaps() end");
		}
		catch (DfException e) {
			throw e;
		}
		catch(ParserConfigurationException parserconfigurationexception) {
			throw parserconfigurationexception;
		}
		catch(SAXException saxexception) {
			throw saxexception;
		}
		catch(IOException ioexception) {
			throw ioexception;
		}
		finally{
			try {
				if(bais!=null){
					bais.close();
				}
			} catch (IOException e1) {
				throw e1;
			}
		}
	}
//begin Action processing
	private void procDoc(Document document) {
		outcomeActionObjects = new ArrayList();
	    maction = new Hashtable();
	    reentry = new Hashtable();
				NodeList entries = document.getElementsByTagName("Action");
				int entriesCnt = entries.getLength();
				for(int k=0;k<entriesCnt;k++) {
					Node Entry = entries.item(k);
					reentry.put(getAttr(Entry,"Name"),getAttr(Entry,"AutoProcReentryActivity"));
				}//for each actionMap
				HelperClass.porticoOutput(0, "ConPrep UI ....QCReentryPts="+reentry.toString());
//begin GroupAction processing
				entries = document.getElementsByTagName("GroupAction");
				entriesCnt = entries.getLength();
				String name;
				String apra;
				for(int k=0;k<entriesCnt;k++) {
//	foreach GroupAction(s)
					Node Entry = entries.item(k);
					name = getAttr(Entry,"Name");
					apra = getAttr(Entry,"AutoProcReentryActivity");
					//outcomeActionObj.setActions(getChildValues(Entry,"ActionRef"));
					NodeList actrefs = Entry.getChildNodes();
					int actrefsCnt = actrefs.getLength();
					StringBuffer value = new StringBuffer();
					value.append("[");
					for (int l=0;l<actrefsCnt;l++) {
//  foreach ActionRef within selected GroupAction
						Node actref = actrefs.item(l);
						if (actref.getNodeName().equals("ActionRef")) {
							String temp = getAttr(actref,"Reference");
							value.append(temp);
							value.append("<*>");
						}
					}
					value.append(apra+"]");
					maction.put(name,value.toString());
				}//for each actionMap
				HelperClass.porticoOutput(0, "ConPrep UI ....QCMultiAction="+maction.toString());
//end GroupAction processing
//begin Entry processing
				entries = document.getElementsByTagName("ActionableMessage");
				entriesCnt = entries.getLength();
				String temp;
				for(int k=0;k<entriesCnt;k++) {
					Node Entry = entries.item(k);
					OutcomeAction outcomeActionObj = new OutcomeAction();
					outcomeActionObj.setContext(getAttr(Entry,"Context"));
					outcomeActionObj.setCode(getAttr(Entry,"Code"));
					outcomeActionObj.setActions(getChildValues(Entry,"ActionRef"));
					outcomeActionObjects.add(outcomeActionObj);
				}//for each actionMap
				HelperClass.porticoOutput(0, "ConPrep UI .....QCActionsMaps-fetchCodeValues code-values= "+outcomeActionObjects.toString());
	}

	/**
	 * traverse the DOM tree to get action and postaction element
	 * this code reflects the structure of the xml
	 */
	private Hashtable getChildValues(Node parent,String attrName){
		Hashtable hashtable = new Hashtable();
		NodeList nodeList = parent.getChildNodes();
		int size = nodeList.getLength();
		String temp = "";
		String temp1 = "";
		for(int i=0;i<size;i++) {
			Node node = nodeList.item(i);
			NodeList nodeList2 = node.getChildNodes();
			int size2 = nodeList2.getLength();
			for(int j=0;j<size2;j++) {
				Node node2 = nodeList2.item(j);
				if(node2.getNodeName().equals(attrName)) {
					temp = getAttr(node2,"Reference");
					temp1 = "*";
					//HelperClass.porticoOutput(0, "billtest temp="+temp);
					if (reentry.containsKey(temp)) {
						temp1 = (String)reentry.get(temp); }
					else {
						if (maction.containsKey(temp)) {
							temp1 = (String)maction.get(temp); }
					}
					//HelperClass.porticoOutput(0, "temp1="+temp2);
					hashtable.put(temp,temp1);
				}//list of actions
			}//for each node
		}//for each node
		return hashtable;
	}

	/**
	 * reads attribute value for the given node and attribute name
	 */
	private String getAttr(Node node,String attrName){
		String attrValue = "";
		if(node.hasAttributes()){
			NamedNodeMap namedNodeMap = node.getAttributes();
			if(namedNodeMap!=null){
				Node attrNode = namedNodeMap.getNamedItem(attrName);
				if(attrNode!=null){
					attrValue=attrNode.getNodeValue();
				}
			}
		}
		return attrValue;
	}
	protected static void updateCuTog(String objId,String value){
		cuToggles.remove(objId);
		cuToggles.put(objId,value);
	}
	protected static String getCuTog(String objId) {
		return (String)cuToggles.get(objId);
	}
	protected static boolean hasCuTog(String objId) {
		return cuToggles.containsKey(objId);
	}
	protected static void putCuTog(String objId,String value) {
		cuToggles.put(objId,value);
	}

	protected static void updateInspectTog(String objId,String value){
		inspectToggles.remove(objId);
		inspectToggles.put(objId,value);
		//int i=0;
		//if(inspectToggles.containsValue("false")) {
		//	String temp=inspectToggles.toString();
		//	while (temp.indexOf("false")>0) {
		//	  String temp1=temp.substring(temp.indexOf("false")+5);
		//	  i++;
		//	  temp=temp1;
		//	}
		//}
		//return i;
	}
	protected static String getInspectTog(String objId) {
		return (String)inspectToggles.get(objId);
	}
	protected static boolean hasInspectTog(String objId) {
		return inspectToggles.containsKey(objId);
	}
	protected static void putInspectTog(String objId,String value) {
		inspectToggles.put(objId,value);
	}
	protected static void logInspectTog() {
		HelperClass.porticoOutput(0, "inspectToggles="+inspectToggles.toString());
	}
	public ArrayList getOutcomeActionObjects() {
		return outcomeActionObjects;
	}

	/**
	 * in memory representation of process code values for different contexts (ie object types)
	 */
	private ArrayList outcomeActionObjects;
	private Hashtable maction;
	private Hashtable reentry;
	private static Hashtable cuToggles;
	private static Hashtable inspectToggles;
	private static QCActionsMaps qCActionsMapsSingleInstance = null;
}
