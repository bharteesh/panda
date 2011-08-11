
package org.portico.conprep.ui.qcaction.replace;

import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class ReplacePrecondition
    implements IActionPrecondition
{

    public ReplacePrecondition()
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
    	if(HelperClass.performerCheck(component.getDfSession(), argumentlist.get("objectId"), addlnInfo) &&
    	          HelperClass.roleCheck(component,argumentlist,context))
    	{
    		HelperClass.porticoOutput(0, "ReplacePrecondition-queryExecute()- Before argumentlist");
    		// objectId - SU State object id
    		// comma separated id(s)== future
    		HelperClass.porticoOutput(0, "ReplacePrecondition-argumentlist=" + argumentlist.toString());

    		isValid = QcHelperClass.isValidReplaceableFileAction(component.getDfSession(),
    		                                      argumentlist.get("objectId"),
    		                                      argumentlist.get("msgObjectId"),
    		                                      addlnInfo);
    	}

	    return isValid;
    }

    private static final String s_requiredArgs[] = new String[0];

}