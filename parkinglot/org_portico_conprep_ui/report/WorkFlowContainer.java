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

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.web.common.ArgumentList;
import com.documentum.webcomponent.library.workflow.reportdetailscontainer.ReportDetailsContainer;

/**
 * Description	Fetches batch id onInit instead of  for-each-activity
 * 				generates random component name for-each-activity
 * Author		pramaswamy
 * Type			WorkFlow
 */
public class WorkFlowContainer extends ReportDetailsContainer {

	/**
	 *
	 */
	public WorkFlowContainer() {
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
		m_callercomponent=argumentlist.get("callerComponent");
		HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI ....onInit m_currentworkflowid,callerComponent :"+m_currentworkflowid+","+m_callercomponent);
		batchfolderid = fetchBatchId();
	}
	/**
	 * @return for each activity get package from local memory
	 */
	public String getBatchId(){
		//HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI ....getBatchId batchfolderid"+batchfolderid);
		return batchfolderid;
	}

	private String fetchBatchId(){
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

            HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI-fetchBatchId()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					batchId = tIDfCollection.getString("r_component_id");
   					HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI-fetchBatchId-batchId="+batchId);
   					break;
				}
   	    	}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in WorkFlowContainer-ConPrep UI-fetchBatchId="+e.getMessage());
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
           		HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI-fetchBatchId CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in WorkFlowContainer-ConPrep UI-fetchBatchId-close" + e.getMessage());
			}
            HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI-fetchBatchId-Call-finally");
		}
		HelperClass.porticoOutput(0, "WorkFlowContainer-ConPrep UI ....fetchBatchId batchId="+batchId);

		return batchId;
	}

	public String getCallerComponent()
	{
		return m_callercomponent;
	}

	private String m_currentworkflowid;
	private String batchfolderid;
	private String m_callercomponent;
}

