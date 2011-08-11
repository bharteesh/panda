
package org.portico.conprep.ui.fasttrackprogress;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class FastTrackProgressPrecondition
    implements IActionPrecondition
{

    public FastTrackProgressPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredParams;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		boolean isValid = false;
		HelperClass.porticoOutput(0, "FastTrackProgressPrecondition-queryExecute-Started for Batch_ID="+ argumentlist.get("objectId"));
		Hashtable addlnInfo = HelperClassConstants.getAddlnInfo(argumentlist);
		String workflowObjectId = HelperClass.getWorkflowObject(component.getDfSession(), argumentlist.get("objectId"), addlnInfo);
		if(workflowObjectId != null && !workflowObjectId.equals(""))
		{
			// We can use the same call as we used for the regular Track Progress
	    	if(HelperClass.isValidReportDetailsListAction(component.getDfSession(), workflowObjectId, addlnInfo))
	    	{
			   isValid = true;
		    }
	    }
	    HelperClass.porticoOutput("FastTrackProgressPrecondition-workflowObjectId="+workflowObjectId+" isValid="+isValid);
		HelperClass.porticoOutput(0, "FastTrackProgressPrecondition-queryExecute-Ended for Batch_ID,isValid="+ argumentlist.get("objectId")+","+isValid);
		return isValid;
    }
    private static final String s_requiredParams[] = {
        "objectId"
    };
}