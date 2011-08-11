
package org.portico.conprep.ui.customqualifiers;

import com.documentum.web.formext.config.IQualifier;
import com.documentum.web.formext.config.QualifierContext;

// Referenced classes of package com.documentum.web.formext.config:
//            IQualifier, QualifierContext

public class PorticoCustomTypeQualifier
    implements IQualifier
{

    public PorticoCustomTypeQualifier()
    {
    }

    public String[] getContextNames()
    {
        return (new String[] {
            "accessionId", "pctype"
        });
    }

    public String getScopeName()
    {
        return "pctype";
    }

    public String getScopeValue(QualifierContext qualifiercontext)
    {
        String s = qualifiercontext.get("pctype");
        if(s == null || s.length() == 0)
        {
            String s1 = qualifiercontext.get("accessionId");
            if(s1 != null && s1.length() != 0)
            {
				// Fire request to Oracle DB to check its type, not required now.
			}
        }
        return s;
    }

    public String getParentScopeValue(String s)
    {
        String s1 = null;
        return s1;
    }

    public String[] getAliasScopeValues(String s)
    {
        return null;
    }
}