
package org.portico.conprep.ui.qcaction.releasetoarchive;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

public class ReleaseToArchivePrecondition
    implements IActionPrecondition
{

    public ReleaseToArchivePrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		boolean isValid = false;

	    if(HelperClass.roleCheck(component,argumentlist,context))
	    {
			Hashtable addlnInfo = HelperClassConstants.getAddlnInfo(argumentlist);
	        isValid = QcHelperClass.isValidReleaseToArchiveAction(component.getDfSession(), argumentlist.get("objectId"),
		                                                           HelperClass.performerCheck(component.getDfSession(),argumentlist.get("objectId"), addlnInfo),
		                                                           addlnInfo);
	    }

	    return isValid;
    }
    private static final String s_requiredArgs[] = new String[0];
}