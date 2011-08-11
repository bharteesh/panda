
package org.portico.conprep.ui.reportdetailsummarylist;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class ReportDetailsSummaryListPrecondition
    implements IActionPrecondition
{

    public ReportDetailsSummaryListPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredParams;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		boolean isValid = false;
		HelperClass.porticoOutput(0, "ReportDetailsSummaryListPrecondition-queryExecute-Started for Batch_ID="+ argumentlist.get("objectId"));
		Hashtable addlnInfo = HelperClassConstants.getAddlnInfo(argumentlist);
		String workflowObjectId = HelperClass.getWorkflowObject(component.getDfSession(), argumentlist.get("objectId"), addlnInfo);
		if(workflowObjectId != null && !workflowObjectId.equals(""))
		{
	    	if(HelperClass.isValidReportDetailsListAction(component.getDfSession(), workflowObjectId, addlnInfo))
	    	{
			   // This batch has a workflow associated with it, or it could be directly a workflow object capable of being
			   // listed - Note this imitates an out of box 'Tracking Progress' component
			   argumentlist.replace("objectId", workflowObjectId);
			   isValid = true;
		    }
	    }
	    HelperClass.porticoOutput("ReportDetailsSummaryListPrecondition-workflowObjectId="+workflowObjectId+" isValid="+isValid);
		HelperClass.porticoOutput(0, "ReportDetailsSummaryListPrecondition-queryExecute-Ended for Batch_ID,isValid="+ argumentlist.get("objectId")+","+isValid);
		return isValid;
    }
    private static final String s_requiredParams[] = {
        "objectId"
    };
}