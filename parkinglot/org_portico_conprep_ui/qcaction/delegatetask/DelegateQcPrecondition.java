
package org.portico.conprep.ui.qcaction.delegatetask;

import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;


public class DelegateQcPrecondition
    implements IActionPrecondition
{

    public DelegateQcPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
		return QcHelperClass.isValidDelegateQcAction(component.getDfSession(),
		                                              argumentlist.get("objectId"),
		                                              HelperClassConstants.getAddlnInfo(argumentlist));
    }

    private static final String s_requiredArgs[] = new String[0];

}