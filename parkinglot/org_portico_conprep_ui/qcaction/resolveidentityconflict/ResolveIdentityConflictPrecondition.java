
package org.portico.conprep.ui.qcaction.resolveidentityconflict;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class ResolveIdentityConflictPrecondition
    implements IActionPrecondition
{

    public ResolveIdentityConflictPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		boolean isValid = false;
        Hashtable addlnInfo = HelperClassConstants.getAddlnInfo(argumentlist);
	    if(HelperClass.performerCheck(component.getDfSession(), argumentlist.get(HelperClassConstants.ACCESSIONID), addlnInfo) &&
	                 HelperClass.roleCheck(component,argumentlist,context))
	    {
    	    isValid = QcHelperClass.isValidResolveIdentityConflictAction(component.getDfSession(),
    	                                                                  argumentlist.get(HelperClassConstants.ACCESSIONID),
    	                                                                  argumentlist.get("msgObjectId"),
		                                                                  addlnInfo);
	    }

	    return isValid;
    }

    private static final String s_requiredArgs[] = new String[0];

}