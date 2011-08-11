
package org.portico.conprep.ui.qcaction.clearfatalandcontinue;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


// For Managers, Super Users this component can to be called
public class ClearFatalAndContinue extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ClearFatalAndContinue()
    {
		m_batch_id = null;
		isInitialized = false;
		isComponentInitialized = false;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batch_id = args.get("objectId");
        HelperClass.porticoOutput("ClearFatalAndContinue-onInit()-Argument batchId=" + m_batch_id);
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
			// We set the 'p_user_action_taken' to simulate as though an action has been taken,
			// This will ensure the proper working of 'postProcessingForContinueAction'
			IDfSysObject iDfSysBatchObject = (IDfSysObject)getDfSession().getObject(new DfId(m_batch_id));
			iDfSysBatchObject.setBoolean("p_user_action_taken", true);
            iDfSysBatchObject.save();

            // Clear all fatal messages if any, so that the activity goes on without any previous messages
            // causing the activity to fail
            isInitialized = QcHelperClass.clearAllFatalMessages(getDfSession(), m_batch_id);
            if(isInitialized == true)
            {
                HelperClass.porticoOutput(0, "ClearFatalAndContinue-initializeData-Start Post Processing");
			    isInitialized = QcHelperClass.postProcessingForContinueAction(getDfSession(), m_batch_id);
			    if(isInitialized == false)
			    {
					HelperClass.porticoOutput(1, "Error in ClearFatalAndContinue-initializeData-Failed in postProcessingForContinueAction");
				}
                HelperClass.porticoOutput(0, "ClearFatalAndContinue-initializeData-End Post Processing-isInitialized="+isInitialized);
		    }
		    else
		    {
			    HelperClass.porticoOutput(1, "Error in ClearFatalAndContinue-initializeData-Failed in clearAllFatalMessages");
			}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ClearFatalAndContinue-"+e.getMessage());
		}
		finally
		{
		}
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_CLEARFATALANDCONTINUE_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_CLEARFATALANDCONTINUE_SUCCESS");
		}
		else
		{
			setReturnError("MSG_CLEARFATALANDCONTINUE_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_CLEARFATALANDCONTINUE_FAILED", null);
		}
	}

    private String m_batch_id;
    private boolean isInitialized = false;
    private boolean isComponentInitialized = false;
}
