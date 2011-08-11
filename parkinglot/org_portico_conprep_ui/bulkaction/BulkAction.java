/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * File           	BulkAction.java
 * Created on 		Feb 09, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TreeMap;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DateTime;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.TextArea;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

/**
 * Description	Does the following things
 * 					initiates bulk action
 * 					intiates fetching bulk action data
 * Author		Ranga
 * Type		BulkAction
 */
public class BulkAction extends Component
{
	/**
	 *
	 */
	public BulkAction()
	{
		m_object = null;
		m_annotateText = null;
		m_annotateList = null;
		m_startDateTime = null;
		m_prefixText = null;
		m_hiddenlabel = null;
		m_priorityList = null;
		m_changeProfileList = null;
		m_workflowQueueList = null;
		m_workflowQueueArrayList = new ArrayList();
		m_actionType = "";
		m_errorString = "";
		m_preconditionErrorString = "";
		addlnData = new Hashtable();
		HelperClass.porticoOutput("BulkAction() constructor");
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onInit(com.documentum.web.common.ArgumentList)
	 */
	public void onInit(ArgumentList args)
	{
		super.onInit(args);

		m_actionType=args.get("actiontype");
		HelperClass.porticoOutput("BulkAction onInit() for actionType="+m_actionType);

		initializeCommonInfo(args);
		initializeCommonControls();

		try
		{
			if(m_preconditionErrorString != null && !m_preconditionErrorString.equals(""))
			{
				// Initialization has failed, do NOT go further
			}
			else
			{
			    if(m_actionType.equals("Annotate Batches"))
			    {
			        m_object = (Object)(new BulkActionAnnotateBatches(m_actionType, getDfSession()));
		        }
		        else if(m_actionType.equals("Put Batches on Hold"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesOnHold(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Remove Batches from Hold"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesOffHold(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Schedule Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesSchedule(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Reschedule Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesReSchedule(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Execute StartAllOver"))
		        {
			    	// m_object = (Object)(new BulkActionSetBatchesExecuteStartAllOver(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Rename Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesRename(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Delete Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesDelete(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Adjust Batch Priority"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesAdjustPriority(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Update Custom Queue"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesUpdateCustomQueue(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Reset Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesReset(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Unschedule Batches"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesUnSchedule(m_actionType, getDfSession()));
			    }
		        else if(m_actionType.equals("Change Profile"))
		        {
			    	m_object = (Object)(new BulkActionSetBatchesChangeProfile(m_actionType, getDfSession()));
			    }
		    }
		}
		catch (Exception e)
		{
			HelperClass.porticoOutput(1, "BulkAction onInit() Exception="+e.toString());
			e.printStackTrace();
			// ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching BulkAction data",e);
		}
	}

	public void	initializeCommonInfo(ArgumentList args)
	{
		String role = "";
		currentProcessType = PRECONDITION;

		try
		{
		    if(m_actionType.equals("Schedule Batches") ||
		        m_actionType.equals("Reschedule Batches") ||
		        m_actionType.equals("Execute StartAllOver") ||
		        m_actionType.equals("Unschedule Batches") ||
		        m_actionType.equals("Change Profile"))
		    {
		    	role = "conprep_inspector_role";
		    }
		    else if(m_actionType.equals("Delete Batches"))
		    {
    	    	role = (String)LdapUtil.getAttribute("dc=ui", "cn=deletebatchesbulkaction", "role");
    	    	if(role == null || role.equals(""))
    	    	{
		    		// This action cannot be performed, since it is specific to an env, LDAP entry is a must.
		    		m_preconditionErrorString = " Error: This environment does NOT support this action = "+ m_actionType;
		    	}
		    }
		    else if(m_actionType.equals("Adjust Batch Priority"))
		    {
		    	// LDAP entry would decide if this action is valid in this env.
    	    	String checkForEntry = (String)LdapUtil.getAttribute("dc=ui", "cn=adjustbatchprioritybulkaction", "role");
    	    	if(checkForEntry == null)
    	    	{
		    		// This action cannot be performed, since it is specific to an env, LDAP entry is a must.
		    		m_preconditionErrorString = " Error: This environment does NOT support this action = "+ m_actionType;
		    	}
		    }
		    else if(m_actionType.equals("Update Custom Queue"))
		    {
		    	// LDAP entry would decide if this action is valid in this env.
    	    	String checkForEntry = (String)LdapUtil.getAttribute("dc=ui", "cn=updatecustomqueuebulkaction", "role");
    	    	if(checkForEntry == null)
    	    	{
		    		// This action cannot be performed, since it is specific to an env, LDAP entry is a must.
		    		m_preconditionErrorString = " Error: This environment does NOT support this action = "+ m_actionType;
		    	}
		    	else
		    	{
					String loginUser = getDfSession().getLoginUserName();
					String firstName = loginUser;
					int indexOfPeriod = loginUser.indexOf(".");
					if(indexOfPeriod != -1)
					{
					    firstName = loginUser.substring(0, indexOfPeriod);
					}
					String multiLevelSubContext = "dc=workflow, dc="+firstName+", dc=developer";
        	    	checkForEntry = (String)LdapUtil.getAttribute(multiLevelSubContext, "cn=workflowlistener", "queue");
        	    	if(checkForEntry != null)
        	    	{
						HelperClass.porticoOutput(0, "BulkAction initializeCommonInfo() Found for firstName="+checkForEntry);
				       	m_workflowQueueArrayList.add(checkForEntry);
				    }
				}
		    }
		    if(role != null && !role.equals(""))
		    {
		    	if(!HelperClass.roleCheck(this, args, getContext(), role))
		    	{
	        	    m_preconditionErrorString = " Error: User does NOT belong to this role = "+role;
		        }
	        }
	    }
	    catch(Exception e)
	    {
			m_preconditionErrorString = " Exception: " + e.toString();
			HelperClass.porticoOutput(1, "BulkAction initializeCommonInfo() Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
		}
    }

    public void initializeCommonControls()
    {
		m_hiddenlabel = (Label)getControl(VALIDATION_STATUS_CONTROL, com.documentum.web.form.control.Label.class);
		m_hiddenlabel.setLabel(Boolean.toString(false));
		m_annotateText = (TextArea)getControl(ANNOTATE_TEXT_CONTROL, com.documentum.web.form.control.TextArea.class);
        m_annotateList = (DropDownList)getControl(ANNOTATE_DROPDOWN_CONTROL, com.documentum.web.form.control.DropDownList.class);
     	m_annotateList.setMutable(true);
		m_annotateList.clearOptions();

   		Option option = new Option();
	    option.setValue("");
        option.setLabel("");
        m_annotateList.addOption(option);

   		option = new Option();
	    option.setValue("Batch deleted");
        option.setLabel("Batch deleted");
        m_annotateList.addOption(option);

   		option = new Option();
	    option.setValue("Batch reset");
        option.setLabel("Batch reset");
        m_annotateList.addOption(option);

   		option = new Option();
	    option.setValue("Batch renamed");
        option.setLabel("Batch renamed");
        m_annotateList.addOption(option);

        if(m_actionType.equals("Schedule Batches") || m_actionType.equals("Reschedule Batches"))
        {
		    m_startDateTime = (DateTime)getControl("start_date_control", com.documentum.web.form.control.DateTime.class);
		    m_startDateTime.clear();
		    Calendar rightNow = Calendar.getInstance();
		    m_startDateTime.setDay(rightNow.get(Calendar.DAY_OF_MONTH));//index starts from 0 // get(Calendar.DAY_OF_WEEK)+1
		    m_startDateTime.setMonth(rightNow.get(Calendar.MONTH)+1);//index starts from 0
		    m_startDateTime.setYear(rightNow.get(Calendar.YEAR));
		    m_startDateTime.setHour(rightNow.get(Calendar.HOUR_OF_DAY));
		    m_startDateTime.setMinute(rightNow.get(Calendar.MINUTE));
		    m_startDateTime.setSecond(rightNow.get(Calendar.SECOND));
		}
		else if(m_actionType.equals("Rename Batches"))
		{
    		m_prefixText = (Text)getControl(PREFIX_TEXT_CONTROL, com.documentum.web.form.control.Text.class);
		}
		else if(m_actionType.equals("Adjust Batch Priority"))
		{
    		m_priorityList = (DropDownList)getControl(PRIORITY_DROPDOWN_CONTROL, com.documentum.web.form.control.DropDownList.class);
      	    m_priorityList.setMutable(true);
    		m_priorityList.clearOptions();

   		    option = new Option();
	        option.setValue(LOW_PRIORITY);
            option.setLabel(LOW_PRIORITY);
            m_priorityList.addOption(option);

   		    option = new Option();
	        option.setValue(HIGH_PRIORITY);
            option.setLabel(HIGH_PRIORITY);
            m_priorityList.addOption(option);
		}
        else if(m_actionType.equals("Update Custom Queue"))
        {
    		m_workflowQueueList = (DropDownList)getControl(WORKFLOW_QUEUE_DROPDOWN_CONTROL, com.documentum.web.form.control.DropDownList.class);
			m_workflowQueueList.setMutable(true);
			m_workflowQueueList.clearOptions();

   		    option = new Option();
	        option.setValue(""); // This will set the 'p_workflow_queue' value to "", so it becomes a Normal queue.
            option.setLabel("Regular");
            m_workflowQueueList.addOption(option);
            if(m_workflowQueueArrayList != null && m_workflowQueueArrayList.size() > 0)
            {
				for(int qindx=0; qindx < m_workflowQueueArrayList.size(); qindx++)
				{
					String currentWorkflowQueueString = (String)m_workflowQueueArrayList.get(qindx);
   		            option = new Option();
	                option.setValue(currentWorkflowQueueString);
                    option.setLabel(currentWorkflowQueueString);
                    m_workflowQueueList.addOption(option);
		        }
		    }
		}
		else if(m_actionType.equals("Change Profile"))
		{
   		    m_changeProfileList = (DropDownList)getControl(CHANGE_PROFILE_DROPDOWN_CONTROL, com.documentum.web.form.control.DropDownList.class);
      	    m_changeProfileList.setMutable(true);
    		m_changeProfileList.clearOptions();

            IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
			String providerId = BulkActionResultSet.readCombinedCookie(ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE),
			                                            SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER);
            // Check if it is a valid providerId and also not an "All"(""), this action cannot be applied to mixed providers
            if(providerId != null && !providerId.equals(""))
            {
        		ArrayList profileIdList = SubmissionBatchObjectListWithMyBatches.getProfileIdsForProvider(providerId);
        		if(profileIdList != null && profileIdList.size() > 0)
        		{
    				for(int profileIndx=0; profileIndx < profileIdList.size(); profileIndx++)
    				{
    					String str = (String)profileIdList.get(profileIndx);
     		            option = new Option();
    	                option.setValue(str);
                        option.setLabel(str);
                        m_changeProfileList.addOption(option);
    		        }
    		    }
		    }
		    else
		    {
				m_preconditionErrorString += " Please select a single provider, this action cannot be applied to batches belonging to multiple provider(s)";
			}
		}
	}

	public void onRender()
	{
		super.onRender();
	}

	public void onClickUpdate(Control control, ArgumentList argumentlist)
	{
		// reset error string
    	m_errorString = "";

		boolean isValid = validateInputData();

		if(isValid == true)
		{
    		if(m_object != null)
		    {
		    	currentProcessType = UPDATE;
                if(m_actionType.equals("Annotate Batches"))
    			{
    			    isValid = ((BulkActionAnnotateBatches)m_object).processUpdate(addlnData);
    		    }
    		    else if(m_actionType.equals("Put Batches on Hold"))
    		    {
    				isValid = ((BulkActionSetBatchesOnHold)m_object).processUpdate(addlnData);
    			}
    		    else if(m_actionType.equals("Remove Batches from Hold"))
    		    {
    				isValid = ((BulkActionSetBatchesOffHold)m_object).processUpdate(addlnData);
    			}
    		    else if(m_actionType.equals("Schedule Batches"))
    		    {
					isValid = ((BulkActionSetBatchesSchedule)m_object).processUpdate(addlnData);
			    }
    		    else if(m_actionType.equals("Reschedule Batches"))
    		    {
					isValid = ((BulkActionSetBatchesReSchedule)m_object).processUpdate(addlnData);
			    }
    		    else if(m_actionType.equals("Execute StartAllOver"))
    		    {
					// isValid = ((BulkActionSetBatchesExecuteStartAllOver)m_object).processUpdate(addlnData);
			    }
    		    else if(m_actionType.equals("Rename Batches"))
    		    {
					isValid = ((BulkActionSetBatchesRename)m_object).processUpdate(addlnData);
			    }
    		    else if(m_actionType.equals("Delete Batches"))
    		    {
					isValid = ((BulkActionSetBatchesDelete)m_object).processUpdate(addlnData);
			    }
		        else if(m_actionType.equals("Adjust Batch Priority"))
		        {
					isValid = ((BulkActionSetBatchesAdjustPriority)m_object).processUpdate(addlnData);
				}
		        else if(m_actionType.equals("Update Custom Queue"))
		        {
					isValid = ((BulkActionSetBatchesUpdateCustomQueue)m_object).processUpdate(addlnData);
				}
		        else if(m_actionType.equals("Reset Batches"))
		        {
					isValid = ((BulkActionSetBatchesReset)m_object).processUpdate(addlnData);
				}
		        else if(m_actionType.equals("Unschedule Batches"))
		        {
					isValid = ((BulkActionSetBatchesUnSchedule)m_object).processUpdate(addlnData);
				}
		        else if(m_actionType.equals("Change Profile"))
		        {
					isValid = ((BulkActionSetBatchesChangeProfile)m_object).processUpdate(addlnData);
				}

        	    if(isValid == false)
         	    {
					m_errorString = "Error detected during Update";
        		}
		    }
	    }
	}

	public boolean validateInputData()
	{
		boolean isValid = true;

		m_errorString = "";

	    String annotateText = m_annotateText.getValue();
	    if(annotateText == null || annotateText.equals(""))
	    {
   	    	// Show Error
            if(m_actionType.equals("Annotate Batches"))
            {
   		    	isValid = false;
   		    	m_errorString = m_errorString + "Error detected in Validation: No Annotation selected";
       	    }
	    }
	    else
	    {
	    	addlnData.put(ANNOTATE_TEXT, annotateText);
	    }
		if(m_actionType.equals("Schedule Batches") || m_actionType.equals("Reschedule Batches"))
		{
			try
			{
		        Date startDate = m_startDateTime.toDate();
    		    IDfTime dftime = new DfTime(startDate);
    		    if((dftime.isValid()) && (!dftime.isNullDate())) // && (startDate.after(new Date())))
    		    {
					// valid date
					addlnData.put(START_DATE, startDate);
			    }
			    else
			    {
					isValid = false;
			        m_errorString = m_errorString + ":" + "Error detected in Validation: Invalid date time";
				}
			}
			catch(Exception e)
			{
                isValid = false;
			    HelperClass.porticoOutput(1, "BulkAction validateInputData() Exception="+e.toString());
				e.printStackTrace();
			}
		}
		else if(m_actionType.equals("Rename Batches"))
		{
    		String prefixText = m_prefixText.getValue();
    		if(prefixText == null || prefixText.equals(""))
    		{
    			// Show Error
    			isValid = false;
    			m_errorString = m_errorString + "Error detected in Validation: No Prefix entered";
    		}
    		else
    		{
    			addlnData.put(PREFIX_STRING, prefixText);
     		}
		}
        else if(m_actionType.equals("Adjust Batch Priority"))
        {
			// drop dowm capture LOW/HIGH
			String priorityValue = m_priorityList.getValue();
			if(priorityValue == null || priorityValue.equals(""))
			{
    			// Show Error
    			isValid = false;
    			m_errorString = m_errorString + "Error detected in Validation: No Priority selected";
			}
			else
			{
    			addlnData.put(PRIORITY_STRING, priorityValue);
		    }
		}
        else if(m_actionType.equals("Update Custom Queue"))
        {
			// drop dowm capture Normal/Custom workflow queue
			String workflowQueueValue = m_workflowQueueList.getValue();
			if(workflowQueueValue == null)
			{
    			// Show Error
    			isValid = false;
    			m_errorString = m_errorString + "Error detected in Validation: No Workflow Queue selected";
			}
			else
			{
    			addlnData.put(WORKFLOW_QUEUE_STRING, workflowQueueValue);
		    }
		}
        else if(m_actionType.equals("Change Profile"))
        {
			// drop dowm capture Profile Id
			String profileIdValue = m_changeProfileList.getValue();
			if(profileIdValue == null)
			{
    			// Show Error
    			isValid = false;
    			m_errorString = m_errorString + "Error detected in Validation: No Profile ID selected";
			}
			else
			{
    			addlnData.put(PROFILE_ID_STRING, profileIdValue);
		    }
		}

		m_hiddenlabel.setLabel(Boolean.toString(isValid));

		return isValid;
	}

	public String getPreconditionErrorString()
	{
		return m_preconditionErrorString;
	}

	public String getErrorString()
	{
		return m_errorString;
	}

	public String getCurrentProcessType()
	{
		return currentProcessType;
	}

	public String getCurrentActionType()
	{
		return m_actionType;
	}

	public TreeMap getSuccessfulPreconditionBatchList()
	{
		TreeMap map = null;
		if(m_object != null)
		{
			if(m_actionType.equals("Annotate Batches"))
			{
			    map = ((BulkActionAnnotateBatches)m_object).getSuccessfulPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Put Batches on Hold"))
   		    {
				map = ((BulkActionSetBatchesOnHold)m_object).getSuccessfulPreconditionBatchList();
			}
   		    else if(m_actionType.equals("Remove Batches from Hold"))
   		    {
				map = ((BulkActionSetBatchesOffHold)m_object).getSuccessfulPreconditionBatchList();
   			}
   		    else if(m_actionType.equals("Schedule Batches"))
   		    {
				map = ((BulkActionSetBatchesSchedule)m_object).getSuccessfulPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Reschedule Batches"))
   		    {
				map = ((BulkActionSetBatchesReSchedule)m_object).getSuccessfulPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Execute StartAllOver"))
   		    {
				// map = ((BulkActionSetBatchesExecuteStartAllOver)m_object).getSuccessfulPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Rename Batches"))
   		    {
				map = ((BulkActionSetBatchesRename)m_object).getSuccessfulPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Delete Batches"))
   		    {
				map = ((BulkActionSetBatchesDelete)m_object).getSuccessfulPreconditionBatchList();
		    }
            else if(m_actionType.equals("Adjust Batch Priority"))
            {
				map = ((BulkActionSetBatchesAdjustPriority)m_object).getSuccessfulPreconditionBatchList();
			}
            else if(m_actionType.equals("Update Custom Queue"))
            {
				map = ((BulkActionSetBatchesUpdateCustomQueue)m_object).getSuccessfulPreconditionBatchList();
			}
            else if(m_actionType.equals("Reset Batches"))
            {
				map = ((BulkActionSetBatchesReset)m_object).getSuccessfulPreconditionBatchList();
			}
		    else if(m_actionType.equals("Unschedule Batches"))
		    {
				map = ((BulkActionSetBatchesUnSchedule)m_object).getSuccessfulPreconditionBatchList();
		    }
		    else if(m_actionType.equals("Change Profile"))
		    {
				map = ((BulkActionSetBatchesChangeProfile)m_object).getSuccessfulPreconditionBatchList();
		    }
	    }
	    return map;
	}

	public TreeMap getFailedPreconditionBatchList()
	{
		TreeMap map = null;
		if(m_object != null)
		{
			if(m_actionType.equals("Annotate Batches"))
			{
			    map = ((BulkActionAnnotateBatches)m_object).getFailedPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Put Batches on Hold"))
   		    {
				map = ((BulkActionSetBatchesOnHold)m_object).getFailedPreconditionBatchList();
			}
   		    else if(m_actionType.equals("Remove Batches from Hold"))
   		    {
				map = ((BulkActionSetBatchesOffHold)m_object).getFailedPreconditionBatchList();
   			}
   		    else if(m_actionType.equals("Schedule Batches"))
   		    {
				map = ((BulkActionSetBatchesSchedule)m_object).getFailedPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Reschedule Batches"))
   		    {
				map = ((BulkActionSetBatchesReSchedule)m_object).getFailedPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Execute StartAllOver"))
   		    {
				// map = ((BulkActionSetBatchesExecuteStartAllOver)m_object).getFailedPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Rename Batches"))
   		    {
				map = ((BulkActionSetBatchesRename)m_object).getFailedPreconditionBatchList();
		    }
   		    else if(m_actionType.equals("Delete Batches"))
   		    {
				map = ((BulkActionSetBatchesDelete)m_object).getFailedPreconditionBatchList();
		    }
            else if(m_actionType.equals("Adjust Batch Priority"))
            {
				map = ((BulkActionSetBatchesAdjustPriority)m_object).getFailedPreconditionBatchList();
		    }
            else if(m_actionType.equals("Update Custom Queue"))
            {
				map = ((BulkActionSetBatchesUpdateCustomQueue)m_object).getFailedPreconditionBatchList();
		    }
            else if(m_actionType.equals("Reset Batches"))
            {
				map = ((BulkActionSetBatchesReset)m_object).getFailedPreconditionBatchList();
		    }
            else if(m_actionType.equals("Unschedule Batches"))
            {
				map = ((BulkActionSetBatchesUnSchedule)m_object).getFailedPreconditionBatchList();
		    }
            else if(m_actionType.equals("Change Profile"))
            {
				map = ((BulkActionSetBatchesChangeProfile)m_object).getFailedPreconditionBatchList();
		    }
	    }
	    return map;
	}

	public TreeMap getSuccessfulUpdateBatchList()
	{
		TreeMap map = null;
		if(m_object != null)
		{
			if(m_actionType.equals("Annotate Batches"))
			{
			    map = ((BulkActionAnnotateBatches)m_object).getSuccessfulUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Put Batches on Hold"))
   		    {
				map = ((BulkActionSetBatchesOnHold)m_object).getSuccessfulUpdateBatchList();
			}
   		    else if(m_actionType.equals("Remove Batches from Hold"))
   		    {
				map = ((BulkActionSetBatchesOffHold)m_object).getSuccessfulUpdateBatchList();
   			}
   		    else if(m_actionType.equals("Schedule Batches"))
   		    {
				map = ((BulkActionSetBatchesSchedule)m_object).getSuccessfulUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Reschedule Batches"))
   		    {
				map = ((BulkActionSetBatchesReSchedule)m_object).getSuccessfulUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Execute StartAllOver"))
   		    {
				// map = ((BulkActionSetBatchesExecuteStartAllOver)m_object).getSuccessfulUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Rename Batches"))
   		    {
				map = ((BulkActionSetBatchesRename)m_object).getSuccessfulUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Delete Batches"))
   		    {
				map = ((BulkActionSetBatchesDelete)m_object).getSuccessfulUpdateBatchList();
		    }
            else if(m_actionType.equals("Adjust Batch Priority"))
            {
				map = ((BulkActionSetBatchesAdjustPriority)m_object).getSuccessfulUpdateBatchList();
			}
            else if(m_actionType.equals("Update Custom Queue"))
            {
				map = ((BulkActionSetBatchesUpdateCustomQueue)m_object).getSuccessfulUpdateBatchList();
			}
            else if(m_actionType.equals("Reset Batches"))
            {
				map = ((BulkActionSetBatchesReset)m_object).getSuccessfulUpdateBatchList();
			}
            else if(m_actionType.equals("Unschedule Batches"))
            {
				map = ((BulkActionSetBatchesUnSchedule)m_object).getSuccessfulUpdateBatchList();
			}
            else if(m_actionType.equals("Change Profile"))
            {
				map = ((BulkActionSetBatchesChangeProfile)m_object).getSuccessfulUpdateBatchList();
			}
	    }
	    return map;
	}

	public TreeMap getFailedUpdateBatchList()
	{
		TreeMap map = null;
		if(m_object != null)
		{
			if(m_actionType.equals("Annotate Batches"))
			{
			    map = ((BulkActionAnnotateBatches)m_object).getFailedUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Put Batches on Hold"))
   		    {
				map = ((BulkActionSetBatchesOnHold)m_object).getFailedUpdateBatchList();
			}
   		    else if(m_actionType.equals("Remove Batches from Hold"))
   		    {
				map = ((BulkActionSetBatchesOffHold)m_object).getFailedUpdateBatchList();
   			}
   		    else if(m_actionType.equals("Schedule Batches"))
   		    {
				map = ((BulkActionSetBatchesSchedule)m_object).getFailedUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Reschedule Batches"))
   		    {
				map = ((BulkActionSetBatchesReSchedule)m_object).getFailedUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Execute StartAllOver"))
   		    {
				// map = ((BulkActionSetBatchesExecuteStartAllOver)m_object).getFailedUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Rename Batches"))
   		    {
				map = ((BulkActionSetBatchesRename)m_object).getFailedUpdateBatchList();
		    }
   		    else if(m_actionType.equals("Delete Batches"))
   		    {
				map = ((BulkActionSetBatchesDelete)m_object).getFailedUpdateBatchList();
		    }
            else if(m_actionType.equals("Adjust Batch Priority"))
            {
				map = ((BulkActionSetBatchesAdjustPriority)m_object).getFailedUpdateBatchList();
		    }
            else if(m_actionType.equals("Update Custom Queue"))
            {
				map = ((BulkActionSetBatchesUpdateCustomQueue)m_object).getFailedUpdateBatchList();
		    }
            else if(m_actionType.equals("Reset Batches"))
            {
				map = ((BulkActionSetBatchesReset)m_object).getFailedUpdateBatchList();
		    }
            else if(m_actionType.equals("Unschedule Batches"))
            {
				map = ((BulkActionSetBatchesUnSchedule)m_object).getFailedUpdateBatchList();
		    }
            else if(m_actionType.equals("Change Profile"))
            {
				map = ((BulkActionSetBatchesChangeProfile)m_object).getFailedUpdateBatchList();
		    }
	    }
	    return map;
	}

	public void onExit()
	{
		HelperClass.porticoOutput(0, "BulkAction onExit() of objects");
		super.onExit();
		if(m_object != null)
		{
            if(m_actionType.equals("Annotate Batches"))
            {
    	        ((BulkActionAnnotateBatches)m_object).clearData();
            }
   		    else if(m_actionType.equals("Put Batches on Hold"))
	        {
		    	((BulkActionSetBatchesOnHold)m_object).clearData();
		    }
	        else if(m_actionType.equals("Remove Batches from Hold"))
	        {
		    	((BulkActionSetBatchesOffHold)m_object).clearData();
		    }
   		    else if(m_actionType.equals("Schedule Batches"))
   		    {
				((BulkActionSetBatchesSchedule)m_object).clearData();
		    }
   		    else if(m_actionType.equals("Reschedule Batches"))
   		    {
				((BulkActionSetBatchesReSchedule)m_object).clearData();
		    }
   		    else if(m_actionType.equals("Execute StartAllOver"))
   		    {
				// ((BulkActionSetBatchesExecuteStartAllOver)m_object).clearData();
		    }
   		    else if(m_actionType.equals("Rename Batches"))
   		    {
				((BulkActionSetBatchesRename)m_object).clearData();
		    }
   		    else if(m_actionType.equals("Delete Batches"))
   		    {
				((BulkActionSetBatchesDelete)m_object).clearData();
		    }
            else if(m_actionType.equals("Adjust Batch Priority"))
            {
				((BulkActionSetBatchesAdjustPriority)m_object).clearData();
			}
            else if(m_actionType.equals("Update Custom Queue"))
            {
				((BulkActionSetBatchesUpdateCustomQueue)m_object).clearData();
			}
            else if(m_actionType.equals("Reset Batches"))
            {
				((BulkActionSetBatchesReset)m_object).clearData();
			}
            else if(m_actionType.equals("Unschedule Batches"))
            {
				((BulkActionSetBatchesUnSchedule)m_object).clearData();
			}
            else if(m_actionType.equals("Change Profile"))
            {
				((BulkActionSetBatchesChangeProfile)m_object).clearData();
			}

		    HelperClass.porticoOutput(0, "BulkAction onExit() of sub objects");
	    }
	}

	private Object m_object;
	private String m_actionType;
	private String currentProcessType;
	private Hashtable addlnData;
	private String m_errorString = "";
	private String m_preconditionErrorString = "";
	private ArrayList m_workflowQueueArrayList = null;

    private DropDownList m_annotateList;
	private TextArea m_annotateText;
	private DateTime m_startDateTime;
	private Text m_prefixText;
	private Label m_hiddenlabel;
    private DropDownList m_priorityList;
    private DropDownList m_changeProfileList;
    private DropDownList m_workflowQueueList;

	public static final String PRECONDITION = "precondition";
	public static final String UPDATE = "update";
	public static final String ANNOTATE_DROPDOWN_CONTROL = "annotate_dropdown_control";
	public static final String ANNOTATE_TEXT_CONTROL = "annotate_text_control";
	public static final String START_DATE_CONTROL = "start_date_control";
	public static final String PREFIX_TEXT_CONTROL = "prefix_text_control";
	public static final String PRIORITY_DROPDOWN_CONTROL = "priority_dropdown_control";
	public static final String CHANGE_PROFILE_DROPDOWN_CONTROL = "change_profile_dropdown_control";
	public static final String WORKFLOW_QUEUE_DROPDOWN_CONTROL = "workflow_queue_dropdown_control";

	// additional data key(s)
	public static final String ANNOTATE_TEXT = "ANNOTATE_TEXT";
	public static final String START_DATE = "START_DATE";
	public static final String PREFIX_STRING = "PREFIX_STRING";
	public static final String PRIORITY_STRING = "PRIORITY_STRING";
	public static final String WORKFLOW_QUEUE_STRING = "WORKFLOW_QUEUE_STRING";
	public static final String PROFILE_ID_STRING = "PROFILE_ID_STRING";

	// Priority values
    public static final String LOW_PRIORITY = "Low Priority";
    public static final String HIGH_PRIORITY = "High Priority";

    // Workflow Queue values
    public static final String REGULAR_WORKFLOW_QUEUE = "";
    // eg: valliworkflowqueue
    public static final String CUSTOM_WORKFLOW_QUEUE_PREFIX = "cn=";
    public static final String CUSTOM_WORKFLOW_QUEUE_SUFFIX = "workflowqueue";
    public static final String CUSTOM_WORKFLOW_QUEUE_QUERY_ATTRIBUTE = "javaClassName";

	// hidden label(to check if the action had validation errors OR not
	public static final String VALIDATION_STATUS_CONTROL = "VALIDATION_STATUS_CONTROL";
}














