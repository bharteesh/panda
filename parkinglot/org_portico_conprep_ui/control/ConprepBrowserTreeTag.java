package org.portico.conprep.ui.control;

import com.documentum.web.formext.control.docbase.DocbaseFolderTreeTag;

public class ConprepBrowserTreeTag extends DocbaseFolderTreeTag
{

    public ConprepBrowserTreeTag()
    {
    }

    protected Class getControlClass()
    {
        return org.portico.conprep.ui.control.ConprepBrowserTree.class;
    }
}