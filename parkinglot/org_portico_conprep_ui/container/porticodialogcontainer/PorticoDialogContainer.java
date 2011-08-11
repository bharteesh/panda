
package org.portico.conprep.ui.container.porticodialogcontainer;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.component.Component;

public class PorticoDialogContainer extends com.documentum.web.formext.component.DialogContainer
{

    public PorticoDialogContainer()
    {
		returnComponentArgumentList = null;
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
    }

    public void onOk(Control control, ArgumentList argumentlist)
    {
		HelperClass.porticoOutput("PorticoDialogContainer-called-onOk()-step1(start)");
		boolean setJump = false;
        if(canCommitChanges() && onCommitChanges())
        {
			if(returnComponentArgumentList != null)
			{
    			String returnComponentId = returnComponentArgumentList.get("returnComponentId");
    			String returnObjectId = returnComponentArgumentList.get("returnObjectId");
				if(returnComponentId != null && returnObjectId != null)
				{
				    if(returnComponentId.equals("objectlist"))
				    {
						HelperClass.porticoOutput("PorticoDialogContainer-returnComponentId="+returnComponentId);
						setJump = true;
		                ArgumentList viewargumentlist = new ArgumentList();
						viewargumentlist.add("objectId", returnObjectId);
			            ActionService.execute("view", viewargumentlist, getContext(), this, null);
				    }
				}
			}
			if(setJump == false)
			{
				HelperClass.porticoOutput("PorticoDialogContainer-REGULAR RETURN");
                setComponentReturn();
		    }
		}
    }

    public void updateControls()
    {
        super.updateControls();
        Component component = getContainedComponent();
        if(component != null)
        {
            boolean flag = canCommitChanges();
            boolean flag1 = canCancelChanges();
            boolean flag2 = flag || flag1;
            getControl("ok1", com.documentum.web.form.control.Button.class).setVisible(flag2);
            getControl("cancel1", com.documentum.web.form.control.Button.class).setVisible(flag2);
            // getControl("close", com.documentum.web.form.control.Button.class).setVisible(!flag2);
            getControl("ok1", com.documentum.web.form.control.Button.class).setEnabled(flag);
            getControl("cancel1", com.documentum.web.form.control.Button.class).setEnabled(flag1);
        }
    }

    public ArgumentList getReturnComponentArgumentList()
    {
		return returnComponentArgumentList;
	}

    // This will be set by the embedded component
	public void setReturnComponentArgumentList(ArgumentList argumentlist)
	{
		returnComponentArgumentList = new ArgumentList(argumentlist);
	}

    private ArgumentList returnComponentArgumentList;
}