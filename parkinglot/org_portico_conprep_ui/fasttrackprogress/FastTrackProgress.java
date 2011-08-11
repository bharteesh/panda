/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Component class for Fast Track Progress
 * File           	FastTrackProgress.java
 * Created on 		Jan 11, 2008
 *
 */
package org.portico.conprep.ui.fasttrackprogress;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.DialogContainer;

/**
 * Description	Does the following things
 * 					initiates fetching of track progess(fast way)
 * Author		srn
 * Type		FastTrackProgress
 */
public class FastTrackProgress extends com.documentum.webtop.webcomponent.objectlist.ObjectList
{
	/**
	 *
	 */
	public FastTrackProgress()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onInit(com.documentum.web.common.ArgumentList)
	 */
	public void onInit(ArgumentList arg0)
	{
		batchObjId=arg0.get("objectId");
		m_callercomponent=arg0.get("callerComponent");
		HelperClass.porticoOutput("FastTrackProgress onInit() for BatchId,m_callercomponent="+batchObjId+","+m_callercomponent);

	    batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), batchObjId);
	    batchName = HelperClass.getObjectName(getDfSession(), batchObjId, DBHelperClass.BATCH_TYPE);

		Datagrid datagrid = (Datagrid)getControl(FastTrackProgress.DATAGRID_1, com.documentum.web.form.control.databound.Datagrid.class);
		dataProvider = datagrid.getDataProvider();
		onInitSw = true;
		super.onInit(arg0);
		try
		{
			fastTrackProgressResultSet = new FastTrackProgressResultSet(arg0.get("objectId"), batchStatus, batchName, getDfSession());
			dataProvider.setScrollableResultSet(fastTrackProgressResultSet.getTableResultSet());
			Control control = getContainer();
			if(control instanceof Form)
				((Form)control).setModal(false);
			setModal(false);
		}
		catch (DfException e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching FastTrackProgress report data",e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching FastTrackProgress report data",e);
		}
	}

	public String getBatch()
	{
		return batchObjId;
	}

	public void onRender()
	{
	  if (onInitSw)
	  {
	  		onInitSw = false;
			HelperClass.porticoOutput(0, "FastTrackProgress-onRender() bypass tree-walk");
			super.onRender();
	  }
	  else
	  {
		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-onRender() tree-walk");
		try
		{
			HelperClass.porticoOutput(0, "FastTrackProgress-onRender-call new fastTrackProgressResultSet");
	    	fastTrackProgressResultSet = new FastTrackProgressResultSet(batchObjId, batchStatus, batchName, getDfSession());
	    	dataProvider.setScrollableResultSet(fastTrackProgressResultSet.getTableResultSet());
			super.onRender();
		}
		catch (DfException e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching FastTrackProgressResultSet report data",e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching FastTrackProgressResultSet report data",e);
		}
	  }
	}

	public String getBatchInfo() throws Exception
	{
		return FastTrackProgressResultSet.getBatchInfo(getDfSession(),batchObjId,batchStatus);
	}

	/**
	 * current row's code
	 */
	public String getCode()
	{
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return dataProvider.getDataField("p_code");
	}

	public String getObjId()
	{
		return dataProvider.getDataField("r_object_id");
	}

	public String getObjectName()
	{
		return dataProvider.getDataField("object_name");
	}


	public String getBatchStatus()
	{
		return batchStatus;
	}

	public String getObjType()
	{
		return dataProvider.getDataField("r_object_type");
	}

	public String getBatchFolder()
	{
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return batchObjId;
	}

	public String getCallerComponent()
	{
		return m_callercomponent;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onExit()
	 */
	public void onExit()
	{
		super.onExit();
		if(fastTrackProgressResultSet!=null)
		{
			fastTrackProgressResultSet.clearData();
		}
	}

	public void closeThisComponent(Control control,ArgumentList args)
	{
		((DialogContainer)getContainer()).onClose(control, args);
	}

	public void callRefresh(Control control,ArgumentList args)
	{
	}

	/**
	 * stored per instance to facilitate row-wise navigation
	 */
	private DataProvider dataProvider;
	/**
	 * ResultSet object used by data grid
	 */
	private FastTrackProgressResultSet fastTrackProgressResultSet;
	/**
	 * list of columns in collection object
	 */
	public static final String colNames[] = {
		"r_object_id",
		"r_act_seqno",
		"object_name",
		"r_runtime_state",
		"r_creation_date",
		"complete_date",
		"r_performer_name",
		"sortKey",
	};

	boolean onInitSw;

	/**
	 * datagrid control name in jsp page
	 */
	public static final String DATAGRID_1 = "DATAGRID_1";

	private String batchObjId;
	private String batchStatus;
	private String batchName;
	private String m_callercomponent;
}


/*

Here is query that lists all the information needed to generate the track progress report. Just replace the r_component_id with the batch id.



SELECT

a.r_object_id, a.r_workflow_id, a.r_act_seqno,

   a.r_performer_name, a.r_creation_date, a.r_runtime_state,

   a.r_act_def_id, b.object_name

FROM dmi_workitem a, dm_activity b WHERE a.r_workflow_id IN (SELECT distinct r_workflow_id FROM dmi_package WHERE ANY r_component_id = '0b00f759802b2d1b') AND a.r_act_def_id = b.r_object_id ORDER BY a.r_act_seqno

*/











