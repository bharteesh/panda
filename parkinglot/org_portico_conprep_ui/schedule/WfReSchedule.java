
/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Workflow Schedule
 * File           	WfReSchedule.java
 * Created on 		Dec 6, 2004
 *
 */
package org.portico.conprep.ui.schedule;
import java.util.Calendar;
import java.util.Date;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.DateTime;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;


/**
 * Description	Component for rescheduling a batch
 * Author		pramaswamy
 * Type			WfReSchedule
 */
public class WfReSchedule extends Component  {

	/**
	 *
	 */
	public WfReSchedule() {
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
			//sb.append("select start_date from dm_job where any method_arguments like '%");
			//sb.append(objectId);
			//sb.append("'");
			sb.append("select p_sched_timestamp from p_batch where r_object_id = '");
			sb.append(objectId);
			sb.append("'");
			DfLogger.debug(this,"ConPrep UI ....get job start date query :"+sb.toString(),null,null);
			iDfQuery.setDQL(sb.toString());
			idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
			while(idfcollection.next()){
				dftime = idfcollection.getTime("p_sched_timestamp");
			}
			sb.delete(0,sb.capacity());
			idfcollection.close();
			idfcollection=null;

		} catch (DfException e) {
			ErrorMessageService.getService().setNonFatalError(getForm(),null,e);
		}

		Label label = (Label)getControl("SCHEDULED_DATE", com.documentum.web.form.control.Label.class);
		if(dftime!=null){
			label.setLabel(dftime.toString());
		}
		DateTime startDateTime = (DateTime)getControl("start_date", com.documentum.web.form.control.DateTime.class);
		startDateTime.clear();
		Calendar rightNow = Calendar.getInstance();
		//DfLogger.getInstance().debug(this,"now : " +rightNow.get(Calendar.DAY_OF_WEEK) +"/"+rightNow.get(Calendar.MONTH)+"/"+rightNow.get(Calendar.YEAR),null,null);
		startDateTime.setDay(rightNow.get(Calendar.DAY_OF_MONTH));//index starts from 0
		startDateTime.setMonth(rightNow.get(Calendar.MONTH)+1);//index starts from 0
		startDateTime.setYear(rightNow.get(Calendar.YEAR));
		startDateTime.setHour(rightNow.get(Calendar.HOUR_OF_DAY));
		startDateTime.setMinute(rightNow.get(Calendar.MINUTE));//time lag between server and client machines-No problem
		startDateTime.setSecond(rightNow.get(Calendar.SECOND));
		// setContainerInfo();
	}

	public boolean onCommitChanges()
	{
		super.onCommitChanges();
		try {

			DateTime startDateTime = (DateTime)getControl("start_date", com.documentum.web.form.control.DateTime.class);
			Date startDate = startDateTime.toDate();
			IDfTime dftime = new DfTime(startDate);
			StringBuffer sbQuery = new StringBuffer();
			HelperClass.porticoOutput(0, "ConPrep UI ....ReSchedule onCommitChanges ENTERED-1");
			if((dftime.isValid()) && (!dftime.isNullDate()) && (startDate.after(new Date()))) {
				HelperClass.porticoOutput(0, "ConPrep UI ....ReSchedule dftime.isValid ENTERED-2");
				//set iterations as 1 and start date in dm_job
				sbQuery.append("UPDATE dm_job OBJECTS SET \"max_iterations\"=1 , SET \"start_date\"=DATE('");
				sbQuery.append(dftime.asString(IDfTime.DF_TIME_PATTERN44));
				sbQuery.append("','");
				sbQuery.append(IDfTime.DF_TIME_PATTERN44);
				sbQuery.append("')");
				sbQuery.append(" , SET \"a_next_invocation\"=DATE('");
				sbQuery.append(dftime.asString(IDfTime.DF_TIME_PATTERN44));
				sbQuery.append("','");
				sbQuery.append(IDfTime.DF_TIME_PATTERN44);
				sbQuery.append("')");

				// sbQuery.append("') WHERE any \"method_arguments\" like '%");
				sbQuery.append(" WHERE object_name ='");
				sbQuery.append(WfReSchedule.JOB_NAME_PREFIX+objectId);
				sbQuery.append("'");
				HelperClass.porticoOutput(0, "ConPrep UI ....ReSchedule Job job details query="+sbQuery.toString());
				iDfQuery.setDQL(sbQuery.toString());
				DfLogger.debug(this,"ConPrep UI ....set job details query :"+sbQuery.toString(),null,null);
				idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.QUERY);
				sbQuery.delete(0,sbQuery.length());
				//set p_state as HelperClassConstants.QUEUED in ptc_batch
				//sbQuery.append("UPDATE p_batch OBJECTS SET \"p_state\"='SCHEDULED' WHERE \"r_object_id\"='");
				//sbQuery.append(objectId);
				//sbQuery.append("'");
				//iDfQuery.setDQL(sbQuery.toString());
				//DfLogger.getInstance().debug(this,"ConPrep UI ....set batch status query :"+sbQuery.toString(),null,null);
				//idfcollection = iDfQuery.execute(getDfSession(), IDfQuery.QUERY);
				//sbQuery.delete(0,sbQuery.length());
				idfcollection.close();
				idfcollection=null;

				IDfSysObject batchObj  = (IDfSysObject)getDfSession().getObjectByQualification("p_batch where r_object_id='"+objectId+"'");
				if(batchObj!=null) {
					batchObj.setTime("p_sched_timestamp", dftime);
					batchObj.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.QUEUED);
					batchObj.save();
				}

			}else {
				Label label = (Label)getControl("MSG_INVALID_DATE", com.documentum.web.form.control.Label.class);
				label.setLabel("Invalid date/time");
				return false;
			}
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
