
package org.portico.conprep.ui.qcaction.claim;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class Claim extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public Claim()
    {
		m_batch_id = null;
		isInitialized = false;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batch_id = args.get("objectId");
        HelperClass.porticoOutput("Claim-onInit()-Argument batchId=" + m_batch_id);
        isInitialized = initializeData();
    }

    public void onRender(){
        super.onRender(); //always call the superclass' onRender()
        if(isInitialized == true)
        {
			MessageService.addMessage(this, "MSG_CLAIM_SUCCESSFUL");
            setComponentReturn();
	    }
    }

    public boolean initializeData()
    {
		boolean isSuccessful = false;

		try
		{
			isSuccessful = QcHelperClass.postProcessingForClaim(getDfSession(), m_batch_id);
	    }
	    catch(Exception e)
	    {
			isSuccessful = false;
			HelperClass.porticoOutput("Exception in Claim-"+e.getMessage());
		}
		finally
		{
		}

		return isSuccessful;
	}

    private String m_batch_id;
    private boolean isInitialized = false;
}
