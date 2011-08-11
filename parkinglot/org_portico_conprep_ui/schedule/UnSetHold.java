
package org.portico.conprep.ui.schedule;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class UnSetHold extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public UnSetHold()
    {
		m_batch_id = null;
		isInitialized = false;
		isComponentInitialized = false;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batch_id = args.get("objectId");
        HelperClass.porticoOutput("UnSetHold-onInit()-Argument batchId=" + m_batch_id);
        initializeData();
        isComponentInitialized = true;
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
        if(isComponentInitialized == true)
        {
			callErrorMessageService(isInitialized, null);
			setComponentReturn();
		}
    }

    public void initializeData()
    {
		isInitialized = true;
        HelperClass.porticoOutput(0, "UnSetHold-initializeData-Start");
		try
		{
            IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(m_batch_id));
            iDfSysObject.setBoolean("p_on_hold", false);
            iDfSysObject.save();
		}
		catch(Exception e)
		{
			isInitialized = false;
	        HelperClass.porticoOutput(1, "Exception in UnSetHold-initializeData="+e.getMessage());
	        e.printStackTrace();
		}
		finally
		{
		}
        HelperClass.porticoOutput(0, "UnSetHold-initializeData-End");
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_UNSET_HOLD_ACTION_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_UNSET_HOLD_ACTION_SUCCESS");
		}
		else
		{
			setReturnError("MSG_UNSET_HOLD_ACTION_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_UNSET_HOLD_ACTION_FAILED", null);
		}
	}

    private String m_batch_id;
    private boolean isInitialized = false;
    private boolean isComponentInitialized = false;
}
