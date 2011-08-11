
package org.portico.conprep.ui.submission.importform;

import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class SubmitBatchPrecondition
    implements IActionPrecondition
{

    public SubmitBatchPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		String tobjectId = getSubmissionAreaObjectId();
		argumentlist.add("objectId", tobjectId);
        boolean flag = true;
/*
        boolean flag1 = AccessibilityService.isAllAccessibilitiesEnabled();
        String s1 = component.getComponentId(flag1);
        if(s1.equals("vdmlist") || s1.equals("vdmliststreamline"))
            flag = false;
*/
        return flag;
    }

    private String getSubmissionAreaObjectId()
    {
		String batchFolderObjectId = AppSessionContext.getSubmissionAreaObjectIdUI();
		HelperClass.porticoOutput("SubmitBatchPrecondition-getSubmissionAreaObjectId()-batchFolderObjectId="+batchFolderObjectId);
		return batchFolderObjectId;
	}

    private static final String s_requiredArgs[] = new String[0];
}