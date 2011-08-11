/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Component class for QC Action Report
 * File           	QC.java
 * Created on 		Feb 15, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.DialogContainer;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.role.RoleService;


/**
 * Description	Does the following things
 * 					initiates fetching of action mapping xml
 * 					intiates fetching of qc report data
 * Author		pramaswamy
 * Type			QC
 */
public class QC extends com.documentum.webtop.webcomponent.objectlist.ObjectList {

	/**
	 *
	 */
	public QC() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onInit(com.documentum.web.common.ArgumentList)
	 */
	public void onInit(ArgumentList arg0) {
		batchObjId=arg0.get("objectId");
		m_callercomponent=arg0.get("callerComponent");
		HelperClass.porticoOutput("QC-onInit()-batchObjId,m_callercomponent="+batchObjId+","+m_callercomponent);
	    batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), batchObjId);
		Datagrid datagrid = (Datagrid)getControl(QC.DATAGRID_1, com.documentum.web.form.control.databound.Datagrid.class);
		dataProvider = datagrid.getDataProvider();
		m_rebuildAll = false;
		super.onInit(arg0);
		try {
			procList();
		    String actionconfigdborfs = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigdborfs");
		    if(actionconfigdborfs.equals("fs")) {
			    String actionconfigfilesysloc = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigfilesysloc");
				qCActionsMaps = QCActionsMaps.getInstance(getDfSession(),actionconfigfilesysloc);
		    } else {
		    	String actionconfigdocbaseloc = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigdocbaseloc");
		    	qCActionsMaps = QCActionsMaps.getInstance(getDfSession(),actionconfigdocbaseloc);
		    }
			qCResultSet = new QCResultSet(arg0.get("objectId"), batchStatus, reportType, getDfSession());
			dataProvider.setScrollableResultSet(qCResultSet.getTableResultSet());
			numberArticles=qCResultSet.getTotalArticleCount();
			HelperClass.porticoOutput("QC-onInit()-numberArticles="+numberArticles);
			inspectNumberArticles=qCResultSet.getInspectArticleCount();
			HelperClass.porticoOutput("QC-onInit()-inspectNumberArticles="+inspectNumberArticles);
			Control control = getContainer();
			if(control instanceof Form)
				((Form)control).setModal(false);
			setModal(false);
		} catch (DfException e) {
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching QC report data",e);
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching QC report data",e);
		}
	}

	public void onRender()
	{
		HelperClass.porticoOutput(0, "QC onRender()");
		try
		{
		    batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), batchObjId);
		    if(m_rebuildAll == false)
		    {
				HelperClass.porticoOutput(0, "QC-onRender()-m_rebuildAll="+m_rebuildAll);
				m_rebuildAll = true;
			}
            else
            {
				HelperClass.porticoOutput(0, "QC-onRender()-m_rebuildAll="+m_rebuildAll);
				if(qCResultSet != null)
				{
					if(batchStatus.equals(HelperClassConstants.INSPECT) || batchStatus.equals(HelperClassConstants.INSPECTING) || batchStatus.equals(HelperClassConstants.INSPECTED))
					{
			    	    qCResultSet.buildTreeAndSetTableResultSet();
				    }
				    else
				    {
						qCResultSet.displayErrorAndSetTableResultSet(QC_STATUS_NOT_IN_VALID_STATE);
					}
			    	dataProvider.setScrollableResultSet(qCResultSet.getTableResultSet());
			    }
			    else
			    {
					HelperClass.porticoOutput(1, "QC-onRender()-qCResultSet is NULL");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching QC report data",e);
		}
		finally
		{
		}
	}

    public boolean roleCheck()
    {
    	try
    	{
        	ArgumentList argumentList = new ArgumentList();
        	Context context = Context.getSessionContext();
	    	if(RoleService.isUserAssignedRole(getDfSession().getLoginUserName(),"conprep_inspector_role",argumentList,context))
	    	{
			    return true;
		    }
    	}
    	catch (Exception e)
    	{
    		HelperClass.porticoOutput("Exception in roleCheck-"+e.toString());
    	}

		return false;

    }

    public String getBatchPerformer()
    {
		String batchPerformer = "";

    	try
    	{
      		IDfSysObject sysObj = (IDfSysObject)getDfSession().getObject(new DfId(batchObjId));
      		batchPerformer = sysObj.getString("p_performer");
        }
        catch(DfException e)
        {
        	HelperClass.porticoOutput("Exception in getBatchPerformer-"+e.toString());
        }

        return batchPerformer;
	}

    public boolean performerCheck()
    {
		boolean isValidBatchPerformer = false;

    	try
    	{
        	if(getBatchPerformer().equals(getDfSession().getLoginUserName()))
        	{
        		isValidBatchPerformer = true;
        	}
        }
        catch(DfException e)
        {
        	HelperClass.porticoOutput("Exception in performerCheck-"+e.toString());
        }

        return isValidBatchPerformer;
    }

	public void procList() {
		Hashtable test = new Hashtable();
		test.put("Simple","");
		List list = new ArrayList();
		list.addAll(test.keySet());
		int i=list.size();
		Iterator it=list.iterator();
		while(it.hasNext()) {
			String a1=it.next().toString();
			HelperClass.porticoOutput(0, "list item="+a1);
		}
	}

	public String getReportType() {
		return(reportType);
	}

	public void toggleRadio1(Control control, ArgumentList args) {
		if (!reportType.equals("entire")) {
			reportType="entire";
			HelperClass.porticoOutput(0, "QC-toggleRadio1() new reportType="+reportType);
			qCResultSet.setReportType(reportType);
			m_rebuildAll = true;
		}
	}

	public void toggleRadio2(Control control, ArgumentList args) {
		if (!reportType.equals("inspect")) {
			reportType="inspect";
			HelperClass.porticoOutput(0, "QC-toggleRadio2() new reportType="+reportType);
			qCResultSet.setReportType(reportType);
			m_rebuildAll = true;
		}
	}

	public String getTogVal()
	{
		String objectId = dataProvider.getDataField("r_object_id");
		HelperClass.porticoOutput(0, "QC-getTogVal() objectId="+objectId);
		Boolean bool = Boolean.valueOf(qCResultSet.getObjectInspectionState(objectId));
		return bool.toString();
	}

	public void onTogInspectControlSafe(Control control, ArgumentList args) throws Exception
	{
		String controlSafeobjectId = control.getName();
        boolean value = ((Checkbox)control).getValue();
        String objectId = qCResultSet.getObjectIdFromControlSafeObjectId(controlSafeobjectId);
		HelperClass.porticoOutput(0, "QC-onTogInspectControlSafe() objectId,value="+objectId+","+value);

        // Does not affect the display tree
		qCResultSet.setObjectInspectionStatus(objectId, value);
		m_rebuildAll = false;
	}

	public void onToggle(Control control, ArgumentList args)
	{
		String objectId = args.get("objectId");
		HelperClass.porticoOutput(0, "QC-onToggle() objectId="+objectId);

        // Affects the display tree
		qCResultSet.setObjectExpandStatus(objectId);
		m_rebuildAll = true;
	}

	/*
	 * for current row -
	 * gives indentation to display assets as content tree
	 */
	public String getSourcePrefix() {
		String pre = "";
		Boolean boolObj = Boolean.valueOf(dataProvider.getDataField("isOrphan"));
		if(!boolObj.booleanValue()) {
			String currentObjectType = dataProvider.getDataField("r_object_type");
			pre=gSP(currentObjectType);
		}
		else {
			pre = "<I>";
		}
		return pre;
	}

	public String gSP(String type) {
		String pre = "";
		if(type.equals("p_fu"))
			pre = "&nbsp;&nbsp;&nbsp;&nbsp;";
		else if(type.equals("p_su"))
			pre = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		//else if(currentObjectType.equals("p_file_ref"))
		//	pre = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		return pre;
	}

	/**
	 * current row's code
	 */
	public String getCode() {
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return dataProvider.getDataField("p_code");
	}
	public boolean isValidBatchStatus() {
		if(batchStatus.equals(HelperClassConstants.RESOLVING_PROBLEM) || batchStatus.equals(HelperClassConstants.INSPECTING)) // batchStatus.equals("INSPECTED"))
			return true;
		else
			return false;
	}

	public String getBatchStatus()
	{
		return batchStatus;
	}

	public boolean isReadOnlyStatus() {
		if(batchStatus.equals(HelperClassConstants.INSPECTED))
			return true;
		else
			return false;
	}
	public String getSeverity() {
		//HelperClass.porticoOutput(0, "in QC.getSeverity()");
		return dataProvider.getDataField("Severity");
	}
	public String getBatchInfo() throws Exception{
		String temp=qCResultSet.getBatchInfo(batchObjId);
		if (reportType.equals("inspect")) return temp.concat("<br/>Inspection article count: "+inspectNumberArticles);
		else return temp.concat("<br/>Entire article count: "+numberArticles);
	}
	public String getObjId() {
		return dataProvider.getDataField("r_object_id");
	}
	public String getControlSafeObjectId() {
		return qCResultSet.getControlSafeObjectIdFromObjectId(dataProvider.getDataField("r_object_id"));
	}
	public String getObjType() {
		return dataProvider.getDataField("r_object_type");
	}
	public String getObjName() {
		return dataProvider.getDataField("object_name");
	}
	public boolean isVirusChecked() throws Exception{
		return qCResultSet.isVirusChecked(dataProvider.getDataField("r_object_id"));
	}
	public String getSuObjId() throws Exception {
		return qCResultSet.getSuObjId(dataProvider.getDataField("r_object_id"));
	}
	public String getBatchFolder() {
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return batchObjId;
	}
	public String getMessageObjectTypeFromCategory()
	{
		// Contains the message object type, ie the object type of the context object
		return dataProvider.getDataField("Category");
	}
	public String getMessageContextDisplayLabelFromText()
	{
		// Contains the message object type
		return dataProvider.getDataField("Text");
	}

	public Hashtable getAction(){
		String temp=getCode();
		Hashtable acthash = new Hashtable();
		while (temp.indexOf("/")>0) {
			String code=temp.substring(0,temp.indexOf("/"));
			Object[] actions=qCActionsMaps.getActions(code);
			Object[] reentry=qCActionsMaps.getReentryPt(code);
			for (int i=0;i<actions.length;i++) {
				String action=(String)actions[i];
				String reent=(String)reentry[i];
				HelperClass.porticoOutput(0, "storing action/reent="+action+"/"+reent);
				acthash.put(action,reent);
			}
			String temp1=temp.substring(temp.indexOf("/")+1);
			temp=temp1;
		}
		Object[] actions=qCActionsMaps.getActions(temp);
		Object[] reentry=qCActionsMaps.getReentryPt(temp);
		for (int i=0;i<actions.length;i++) {
			String action=(String)actions[i];
			String reent=(String)reentry[i];
			HelperClass.porticoOutput(0, "storing action/reent="+action+"/"+reent);
			acthash.put(action,reent);
		}
		HelperClass.porticoOutput(0, "hash-content="+acthash);
		if(acthash.size()>0) return acthash;
		else return acthash;
	}

	public String getCallerComponent()
	{
		return m_callercomponent;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onExit()
	 */
	public void onExit() {
		super.onExit();
		if(qCResultSet!=null){
			qCResultSet.clearData();
		}
	}

	public void closeThisComponent(Control control,ArgumentList args)
	{
		((DialogContainer)getContainer()).onClose(control, args);
	}

// Others

	public void onClickObject(Control control, ArgumentList args) {
		HelperClass.porticoOutput(0, "onClickObject() objId="+args.get("objectId")+" type="+args.get("type")+" cufusu="+args.get("cufusu"));
		super.onClickObject(control,args);
	}

	public void onClearButton(Control control, ArgumentList argumentlist) throws Exception {
		HelperClass.porticoOutput(0, "onClearButton()");
		String tempMsgId=argumentlist.get("msgObjectId");
		String severityText=argumentlist.get("severityText");
		HelperClass.porticoOutput(0, "onClearButton() tempMsgId="+tempMsgId);
		boolean isSuccessful = QcHelperClass.setUserMessage(getDfSession(),tempMsgId,batchObjId,"",QCResultSet.getSeverityFromSeverityText(severityText));
		onRender();
    }

	public void onClearAllWarnings(Control control, ArgumentList argumentlist) throws Exception {
		HelperClass.porticoOutput(0, "onClearAllWarnings()");
		if(QCResultSet.clearAllWarnings(batchObjId, getDfSession())) onRender();
    }


	/**
	 * stored per instance to facilitate row-wise navigation
	 */
	private QCActionsMaps qCActionsMaps;
	/**
	 * stored per instance to facilitate row-wise navigation
	 */
	private DataProvider dataProvider;
	/**
	 * ResultSet object used by data grid
	 */
	private QCResultSet qCResultSet;
	/**
	 * list of columns in collection object
	 */
	protected static final String colNames[] = {
		"r_object_id",
		"object_name",
		"r_object_type",
		"p_code",
		"Text",
		"Severity",
		"Category",
		"cufusu",
		"msgObjId",
		"isOrphan",
	};

	private boolean m_rebuildAll;
	private String reportType="inspect"; // "entire";

	/**
	 * datagrid control name in jsp page
	 */
	public static final String DATAGRID_1 = "DATAGRID_1";
	private String batchObjId;
	private String batchStatus;
	private int numberArticles;
	private int inspectNumberArticles;
	private String m_callercomponent;
	private final String QC_STATUS_NOT_IN_VALID_STATE = "Batch Status not in Inspect OR Inspecting State!";
}














