
package org.portico.conprep.ui.porticoactiondispatcher;


import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Label;

// Referenced classes of package com.documentum.web.formext.action:
//            ActionService

public class PorticoActionDispatcherComponent extends com.documentum.web.formext.action.ActionDispatcherComponent
{

    public PorticoActionDispatcherComponent()
    {
		super();
		closeMe = "false";
    }

    public void onInit(ArgumentList argumentlist)
    {
		closeMe = argumentlist.get("closeme");
		if(closeMe == null)
		{
			closeMe = "false";
		}
        Label label = (Label)getControl(PorticoActionDispatcherComponent.CLOSE_WINDOW_MSG, com.documentum.web.form.control.Label.class);
        label.setLabel(closeMe);

        super.onInit(argumentlist);
    }

    private String closeMe = "false";
    public static final String CLOSE_WINDOW_MSG = "closewindowmessage";
}