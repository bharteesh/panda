
/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Workflow Schedule
 * File           	WfRunNow.java
 * Created on 		Dec 6, 2004
 *
 */
package org.portico.conprep.ui.schedule;


import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.workflow.impl.documentum.ActionTool;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.component.Component;


/**
 * Description	Component for scheduling a batch
 * Author		pramaswamy
 * Type			WfRunNow
 */
public class WfRunNow extends Component
{
	String objectId="";

    public void WfRunNow() {
    }

    public void onInit(ArgumentList argumentlist) {
        super.onInit(argumentlist);
		objectId = argumentlist.get("objectId");
		HelperClass.porticoOutput(0, "WfRunNow.onInit()");
		// setContainerInfo();
    }

	public void onRender() {
		super.onRender();
	}

	public boolean onCommitChanges()
	{
		super.onCommitChanges();
		boolean flag = false;
		ActionTool actionTool = null;
		try
		{
			// String performer=getDfSession().getLoginUserName();
	        IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(objectId));
            String profileId = iDfSysObject.getString("p_profile_id");
			HelperClass.porticoOutput(0, "WfRunNow profileId,documentum batch objectId="+profileId+","+objectId);
            String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(objectId);
			HelperClass.porticoOutput(0, "calling ActionTool.runNow() with profileId,batchaccessionId="+profileId+","+batchAccessionId);
            actionTool = new ActionTool(getDfSession(), batchAccessionId);
            actionTool.flush();
			actionTool.runNow(profileId);
			HelperClass.porticoOutput(0, "returned from ActionTool.runNow()");
			flag = true;
		}
		catch (Exception e)
		{
			HelperClass.porticoOutput(1, "Exception - WfRunNow-onCommitChanges() on batchObjectId="+objectId+","+e.getMessage());
			ErrorMessageService.getService().setNonFatalError(this, "MSG_FAILED", null);
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "WfRunNow-onCommitChanges-Call-finally");
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
			}
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
}
