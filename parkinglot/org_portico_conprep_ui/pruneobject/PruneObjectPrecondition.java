
package org.portico.conprep.ui.pruneobject;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

public class PruneObjectPrecondition
    implements IActionPrecondition
{

    public PruneObjectPrecondition()
    {
    }

    public String[] getRequiredParams()
    {
       return s_requiredArgs;
    }

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component)
    {
       return HelperClass.isValidPruneBatchAction(component.getDfSession(),
                                                   argumentlist.get("objectId"),
                                                   HelperClassConstants.getAddlnInfo(argumentlist));
    }
    private static final String s_requiredArgs[] = new String[0];
}