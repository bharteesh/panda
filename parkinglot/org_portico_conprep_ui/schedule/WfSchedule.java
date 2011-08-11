
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
 * Created on 		Dec 6, 2004
 *
 */
package org.portico.conprep.ui.schedule;

import java.util.Calendar;
import java.util.Date;

import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.DateTime;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;

/**
 * Description	Component for scheduling a batch
 * Author		pramaswamy
 * Type			WfSchedule
 */
public class WfSchedule extends Component
{

    public void WfSchedule() {
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
		objectId = argumentlist.get("objectId");
		// setContainerInfo();
    }
	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onRender()
	 */
	public void onRender() {
		super.onRender();

		if(retry_sw) {
			retry_sw=false;
			Label label = (Label)getControl("MSG_INVALID_DATE", com.documentum.web.form.control.Label.class);
			label.setLabel("Invalid date/time, retry");
		}
		DateTime startDateTime = (DateTime)getControl("start_date", com.documentum.web.form.control.DateTime.class);
		startDateTime.clear();
		Calendar rightNow = Calendar.getInstance();
		startDateTime.setDay(rightNow.get(Calendar.DAY_OF_MONTH));//index starts from 0 // get(Calendar.DAY_OF_WEEK)+1
		startDateTime.setMonth(rightNow.get(Calendar.MONTH)+1);//index starts from 0
		startDateTime.setYear(rightNow.get(Calendar.YEAR));
		startDateTime.setHour(rightNow.get(Calendar.HOUR_OF_DAY));
		startDateTime.setMinute(rightNow.get(Calendar.MINUTE));
		startDateTime.setSecond(rightNow.get(Calendar.SECOND));
	}

    public boolean onCommitChanges()
    {
    	super.onCommitChanges();
        boolean flag = false;
		DateTime startDateTime = (DateTime)getControl("start_date", com.documentum.web.form.control.DateTime.class);
		Date startDate = startDateTime.toDate();
		IDfTime dftime = new DfTime(startDate);
// CONPREP-2234, merged. LATEST CHANGES FROM rel-1-1-8 (with production fix) has been incorporated into REL-1-1 for the REL-1-1-9 release
// Sometimes, if we open the schedule page(which populates the current date/time) but do not take action for
// a few minutes, selected 'startDate' would be after the current Date, so this call fails.
//		if((dftime.isValid()) && (!dftime.isNullDate()) && (startDate.after(new Date()))) {
		if((dftime.isValid()) && (!dftime.isNullDate())) {
			try{
					//if performance problems arise in future, use query update
					//UPDATE ptc_batch OBJECTS SET "p_state"='SUBMITTED' WHERE "r_object_id"='0b0152d480008a60'
					IDfSysObject batchObj  = (IDfSysObject)getDfSession().getObjectByQualification("p_batch where r_object_id='"+objectId+"'");

					if(batchObj!=null) {
						batchObj.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.QUEUED);
						batchObj.setTime("p_sched_timestamp", dftime);
						// This must not be set, it will be set when a 'Claim' is performed
						//      from a 'PROBLEM' or 'QC' state
						// batchObj.setString("p_performer", getDfSession().getLoginUserName());
						batchObj.save();
					}

				/* Suku: The following code which creates a job instance is no longer needed as we
				 * have now a dedicated Workflow Scheduler that polls every pre-configured time units.
				 */
				//if (false)
				//{
				//	IDfSysObject dm_job  = (IDfSysObject)getDfSession().newObject("dm_job");
				//	dm_job.setObjectName(WfSchedule.JOB_NAME_PREFIX + objectId);
				//	dm_job.setString("method_name", "InitiateWfInstance");
				//	dm_job.setRepeatingString("method_arguments",0,"-docbase_name "+getCurrentDocbase());
				//	dm_job.setRepeatingString("method_arguments",1,"-ticket "+ getDfSession().getLoginTicket());
				//	dm_job.setRepeatingString("method_arguments",2,"-user "+getDfSession().getUser(getCurrentLoginUsername()).getUserOSName());
				//	dm_job.setRepeatingString("method_arguments",3,"-packageId "+objectId);
				//	dm_job.setRepeatingString("method_arguments",4,"-jobId "+dm_job.getObjectId().toString());
				//	dm_job.setInt("max_iterations",1);
				//	//dm_job.setBoolean("run_now",true);
				//	dm_job.setTime("start_date",dftime);
				//	dm_job.setTime("a_next_invocation",dftime);
				//	dm_job.setInt("run_interval",1);
				//	dm_job.setInt("run_mode",1);
				//	dm_job.save();
				//}
					flag = true;
			}
			catch (DfException e) {
				ErrorMessageService.getService().setNonFatalError(getForm(),null,e);
			}
		}
		else {
			//Label label = (Label)getControl("MSG_INVALID_DATE", com.documentum.web.form.control.Label.class);
			//label.setLabel("Invalid date/time");
			retry_sw=true;
			onRender();
		}
        return flag;
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
    public static final String DATE_VALIDATOR_PANEL = "datevalidatorpanel";

    // Suku: The prefix is no longer needed.
    //private static String JOB_NAME_PREFIX="Portico Batch Wf Job: ";

    String objectId = null;
    boolean retry_sw=false;
}