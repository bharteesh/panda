/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Workflow report
 * File           	WorkFlow.java
 * Created on 		Jan 12, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.format.DateValueFormatter;
import com.documentum.webcomponent.library.workflow.reportdetails.ReportDetailsSummary;
import com.documentum.webcomponent.library.workflow.savereport.SaveReport;

/**
 * Description	Fetches batch id onInit instead of  for-each-activity
 * 				generates random component name for-each-activity
 * Author		pramaswamy
 * Type			WorkFlow
 */
public class WorkFlow extends ReportDetailsSummary {

	/**
	 *
	 */
	public WorkFlow() {
		super();
	}
	/* (non-Javadoc)
	 * @see com.documentum.web.form.Control#onInit(com.documentum.web.common.ArgumentList)
	 * package is associated with this workflow instance itself
	 * so fetch it in Oninit
	 */
	public void onInit(ArgumentList argumentlist) {
		super.onInit(argumentlist);//contains workflow object_id
		m_currentworkflowid=argumentlist.get("objectId");
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI ....onInit m_currentworkflowid :"+m_currentworkflowid);
		batchfolderid = fetchBatchId();
		mesgResultSet = new Hashtable();
		mesgResultSetHtml = new Hashtable();
		randomNum = 0;
	}
	public void toggleRefresh(Control control, ArgumentList args) {
		if(refresh) refresh=false;
		else refresh=true;
	}
	public static boolean getRefresh() {
		return(refresh);
	}
	/**
	 * @return random name for included component
	 */
	public String getRandCompName(){
		String temp = "rand_component_"+(randomNum++);
		return temp;
	}
	/**
	 * @return for each activity get package from local memory
	 */
	public String getBatchId(){
		//HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI ....getBatchId batchfolderid"+batchfolderid);
		return batchfolderid;
	}

	/**
	 * @param activityName
	 * @param mesgsForActivity
	 * cached for future reference
	 */
	public void setMesgResultSet(String activityName, ArrayList mesgsForActivity){
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI .... setMesgResultSet  ");
		mesgResultSet.put(activityName,mesgsForActivity);
	}
	/**
	 * @param activityName
	 * @return list of messages in array list form
	 * (finally converted into tab separated values)
	 * for the given activity
	 */
	public ArrayList getMesgResultSet(String activityName){
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI .... getMesgResultSet activityName "+activityName);
		return (ArrayList)mesgResultSet.get(activityName);
	}

	/**
	 * @param activityName
	 * @param mesg
	 * cached for future reference
	 */
	public void setMesgHtml(String workitemId, String mesg){
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI .... setMesgHtml");
		mesgResultSetHtml.put(workitemId,mesg);
	}
	/**
	 * @param activityName
	 * @return list of messages in html form for the given activity
	 */
	public String getMesgHtml(String workitemId){
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI .... getMesgHtml activityName "+workitemId);
		return (String)mesgResultSetHtml.get(workitemId);
	}

	/**
	 * @return get package id from workflow service for
	 * one time during initialization and cache for future use
	 */
/*
	protected String fetchBatchId(){
		IWorkflowReport iWorkflowReport;
		String batchId=null;
		try {
			iWorkflowReport =
				(IWorkflowReport) m_reportService.getWFReport(
					new DfId(m_workflowId));
			batchId = ((DfId)iWorkflowReport.getDocumentObjectId(0)).getId();

		} catch (DfException e) {
			ErrorMessageService.getService().setNonFatalError(this,"Error fetching workflow package",e);
			e.printStackTrace();
		}
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI ....fetchBatchId batchId"+batchId);
		return batchId;
	}
*/

	protected String fetchBatchId(){
		String batchId=null;
        IDfCollection tIDfCollection = null; // m_currentworkflowid
        try
		{
			// Good to give r_object_id when any repeating Or internal attribute is fetched
			// Distinct and other aggregate calls could slow down query
            String attrNames = "r_object_id,r_component_id";
            String packageType = "p_batch";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM dmi_package "+
                               " where r_workflow_id="+ "'" + m_currentworkflowid + "'"+
                               " and r_package_type="+ "'"+ packageType +"'";

            HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI-fetchBatchId()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					batchId = tIDfCollection.getString("r_component_id");
   					HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI-fetchBatchId-batchId="+batchId);
   					break;
				}
   	    	}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in WorkFlow-ConPrep UI-fetchBatchId="+e.getMessage());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI-fetchBatchId CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in WorkFlow-ConPrep UI-fetchBatchId-close" + e.getMessage());
			}
            HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI-fetchBatchId-Call-finally");
		}
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI ....fetchBatchId batchId="+batchId);

		return batchId;
	}

	/**
	 * Overridden to append user message details
	 */
	protected void saveReportContent(StringBuffer stringbuffer)	{
		int i = s_exportColumnNames.length;
		m_scrollResultSet.setCursor(-1);
		DateValueFormatter datevalueformatter = new DateValueFormatter();
		datevalueformatter.setType("short");
		for(; m_scrollResultSet.next(); stringbuffer.append(SaveReport.s_eol))	{
			String activityName="";
			for(int j = 0; j < i; j++)	{
				String s = s_exportColumnNames[j];
				int k = m_reportResultSet.findColumn(s);
				Object obj = m_scrollResultSet.getObject(k);
				String s1 = obj == null ? "" : obj.toString();
				if(j==0) {
					activityName=s1;
				}
				s1 = s1.replace(SaveReport.s_eol, SaveReport.s_linebreak);
				if((s.equals("complete_date") || s.equals("receive_date")) && s1.length() > 0)
					s1 = datevalueformatter.format(s1);
				stringbuffer.append(s1);
				stringbuffer.append(SaveReport.s_delim);
			}
			saveReportMesg(stringbuffer,activityName);
		}
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI .... mesgResultSet "+mesgResultSet);
	}

	/**
	 * @param stringbuffer
	 * @param activityName
	 * created tab separated values of user message details
	 */
	private void saveReportMesg(StringBuffer stringbuffer, String activityName){
		ArrayList mesgsForCurrentActivity = (ArrayList)mesgResultSet.get(activityName);
		if(mesgsForCurrentActivity!=null){
			int k=mesgsForCurrentActivity.size();
			for(int i=0;i<k;i++){
				Hashtable mesg = (Hashtable)mesgsForCurrentActivity.get(i);;
				stringbuffer.append(SaveReport.s_eol);
				Enumeration keys = mesg.keys();
				while(keys.hasMoreElements()){
					String key = (String)keys.nextElement();
					key = key.replace(SaveReport.s_eol, SaveReport.s_linebreak);
					String value = (String)mesg.get(key);
					value = value.replace(SaveReport.s_eol, SaveReport.s_linebreak);
					stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);stringbuffer.append(SaveReport.s_delim);
					stringbuffer.append(key);
					stringbuffer.append(SaveReport.s_delim);
					stringbuffer.append(value);
					stringbuffer.append(SaveReport.s_eol);
				}//for each key,value
			}// for each mesg
		}//if CurrentActivity has mesg
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onExit()
	 * release all data
	 */
	public void onExit() {
		super.onExit();
		HelperClass.porticoOutput(0, "WorkFlow-ConPrep UI ....onExit");
		mesgResultSet.clear();
		mesgResultSetHtml.clear();
	}

	protected static final String s_exportColumnNames[] = {
		"task_name", "status", "action", "performer", "comment", "receive_date", "complete_date", "overdue", "attachments"
	};
	private static boolean refresh=false;
	private String m_currentworkflowid;
	private String batchfolderid;
	private int randomNum;
	private Hashtable mesgResultSet;//contains ArrayList
	private Hashtable mesgResultSetHtml;//contains String
}

