
/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Workflow Schedule
 * File           	WfSchedule.java
 * Created on 		Dec 3, 2004
 *
 */
package org.portico.conprep.ui.schedule;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;


/**
 * Description	Component for unscheduling a batch
 * Author		pramaswamy
 * Type			WfUnSchedule
 */
public class WfUnSchedule extends Component {
	/**
	 *
	 */
	public WfUnSchedule() {
	}
	public void onInit(ArgumentList argumentlist)
	{
		super.onInit(argumentlist);
		objectId = argumentlist.get("objectId");
		IDfTime dftime = null;
		try {
			clientx = new DfClientX();
			iDfQuery = clientx.getQuery();
			StringBuffer sb = new StringBuffer();
			sb.append("select p_sched_timestamp from p_batch where r_object_id = '");
			sb.append(objectId);
			sb.append("'");
			DfLogger.debug(this,"ConPrep UI ....wf start date query :"+sb.toString(),null,null);
			iDfQuery.setDQL(sb.toString());
			idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
			while(idfcollection.next()){
				dftime = idfcollection.getTime("p_sched_timestamp");
			}
			idfcollection.close();
			sb.delete(0,sb.capacity());
			idfcollection=null;

		} catch (DfException e) {
			ErrorMessageService.getService().setNonFatalError(getForm(),null,e);
		}

		Label label = (Label)getControl("SCHEDULED_DATE", com.documentum.web.form.control.Label.class);
		if(dftime!=null){
			label.setLabel(dftime.toString());
		}

		// setContainerInfo();

	}

	public boolean onCommitChanges()
	{
		super.onCommitChanges();
		try {
			/*
			iDfQuery.setDQL("UPDATE dm_job OBJECTS SET \"max_iterations\"=0 WHERE any \"method_arguments\" like '%"
				+ objectId
				+ "'");
			idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.QUERY);
			*/

			// update batch status
			IDfSysObject batchObj  = (IDfSysObject)getDfSession().getObjectByQualification("p_batch where r_object_id='"+objectId+"'");
			if(batchObj!=null) {
				batchObj.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.LOADED);
				batchObj.setTime("p_sched_timestamp", null);
				batchObj.save();
			}

			// delete job
			StringBuffer sb = new StringBuffer();
			// sb.append("DELETE \"dm_job\" OBJECTS WHERE any \"method_arguments\" like '%");
			sb.append("DELETE \"dm_job\" OBJECTS WHERE object_name ='");
			sb.append(JOB_NAME_PREFIX+objectId);
			sb.append("'");
			iDfQuery.setDQL(sb.toString());
			HelperClass.porticoOutput(0, "ConPrep UI ....UnSchedule Job job details query="+sb.toString());
			DfLogger.debug(this,"ConPrep UI ....delete job query :"+sb.toString(),null,null);
			idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.QUERY);
			idfcollection.close();
			sb.delete(0,sb.capacity());
			idfcollection=null;
		} catch (DfException e) {
			ErrorMessageService.getService().setNonFatalError(getForm(),null,e);
		}
		return true;
	}

// This method helps to return to the specified component
/*
	public void setContainerInfo()
	{
		Control container = getContainer();
		if(container != null)
		{
			if(container instanceof PorticoDialogContainer)
			{
	   		    ArgumentList returnArgumentList = new ArgumentList();
	   		    returnArgumentList.add("returnComponentId", "objectlist");
	   		    returnArgumentList.add("returnObjectId", QcHelperClass.getParentFolderId(getDfSession(), objectId));
	   	    	((PorticoDialogContainer)container).setReturnComponentArgumentList(returnArgumentList);
		    }
		}
	}
*/
	String objectId = null;
	private DfClientX clientx =null;
	private IDfQuery iDfQuery = null;
	private IDfCollection idfcollection = null;
	private static String JOB_NAME_PREFIX="Portico Batch Wf Job: ";
}
