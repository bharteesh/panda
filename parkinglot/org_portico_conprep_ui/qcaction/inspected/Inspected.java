
package org.portico.conprep.ui.qcaction.inspected;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class Inspected extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public Inspected()
    {
		m_batch_id = null;
		isInitialized = false;
		isComponentInitialized = false;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batch_id = args.get("objectId");
        HelperClass.porticoOutput("Inspected-onInit()-Argument batchId=" + m_batch_id);
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
		try
		{
			isInitialized = QcHelperClass.postProcessingForInspected(getDfSession(), m_batch_id);
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput("Exception in Inspected-"+e.getMessage());
		}
		finally
		{
		}
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_INSPECTED_ACTION_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_INSPECTED_ACTION_SUCCESS");
		}
		else
		{
			setReturnError("MSG_INSPECTED_ACTION_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_INSPECTED_ACTION_FAILED", null);
		}
	}

    private String m_batch_id;
    private boolean isInitialized = false;
	private boolean isComponentInitialized = false;
}
