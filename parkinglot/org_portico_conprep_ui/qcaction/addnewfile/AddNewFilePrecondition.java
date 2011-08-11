
package org.portico.conprep.ui.qcaction.addnewfile;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class AddNewFilePrecondition
    implements IActionPrecondition
{

    public AddNewFilePrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }
    // IDfSession currentSession, String objectId, Hashtable addlnInfo

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		boolean isValid = false;
		Hashtable addlnInfo = HelperClassConstants.getAddlnInfo(argumentlist);

		if(HelperClass.roleCheck(component,argumentlist,context))
		{
    		boolean performerCheck = false;
			String batchStatus = QcHelperClass.getStatusForBatchObject(component.getDfSession(), argumentlist.get("objectId"), addlnInfo);
			if(batchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
			{
				// In case of a Batch in 'Loaded' status, there are no performer(s) yet,
				//    assume as though a performer check was passed.
				performerCheck = true;
			}
			else
			{
				performerCheck = HelperClass.performerCheck(component.getDfSession(),argumentlist.get("objectId"), addlnInfo);
			}

			if(true == performerCheck)
           	{
		            // objectId(selected) - Batch object id
		        isValid = QcHelperClass.isValidAddNewFileAction(component.getDfSession(),
		                                  argumentlist.get("objectId"),
		                                  argumentlist.get("msgObjectId"),
		                                  addlnInfo);
            }
	    }

    	return isValid;
    }

    private static final String s_requiredArgs[] = new String[0];

}