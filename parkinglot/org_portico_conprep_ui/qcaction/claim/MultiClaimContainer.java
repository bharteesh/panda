
package org.portico.conprep.ui.qcaction.claim;

import java.util.ArrayList;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.webcomponent.library.messages.MessageService;

public class MultiClaimContainer extends com.documentum.web.formext.component.ComboContainer// com.documentum.web.formext.component.DialogContainer
{

    public MultiClaimContainer()
    {
		super();
		isSuccessful = true;
		isInitialized = false;
		m_erroredObjectList = new ArrayList();
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);

        ArgumentList[] allContainedComponentsArgs = getContainedComponentsArgs();
        HelperClass.porticoOutput(0, "MultiClaimContainer-onInit-allContainedComponentsArgs="+allContainedComponentsArgs.toString());

        if(allContainedComponentsArgs.length > 0)
        {
			for(int cindx=0; cindx < allContainedComponentsArgs.length; cindx++)
			{
		    	ArgumentList singleArgList = allContainedComponentsArgs[cindx];
		    	if(singleArgList != null)
		    	{
					String singleObjectId = singleArgList.get("objectId");
    				HelperClass.porticoOutput(0, "MultiClaimContainer-onInit-singleObjectId="+singleObjectId);
    				if(handleSingleClaim(singleObjectId) == false)
    				{
    					isSuccessful = false;
    					m_erroredObjectList.add(HelperClass.getObjectName(getDfSession(), singleObjectId, DBHelperClass.BATCH_TYPE));
    				}
				}
		    }
		}

		isInitialized = true;
    }

    public void onRender()
    {
        super.onRender(); //always call the superclass' onRender()
        // If the onInit() has been completed and if all the claims went thro' successfully then return else stay
        //    on to display message
        if(isInitialized == true)
        {
            if(isSuccessful == true)
            {
	    		MessageService.addMessage(this, "MSG_CLAIM_SUCCESSFUL");
                setComponentReturn();
	        }
	        else
	        {
				ErrorMessageService.getService().setNonFatalError(this, "MSG_CLAIM_FAILED", null);
			}
	    }
    }

    private boolean handleSingleClaim(String objectId)
    {
		boolean tIsSuccessful = true;

		try
		{
			tIsSuccessful = QcHelperClass.postProcessingForClaim(getDfSession(), objectId);
	    }
	    catch(Exception e)
	    {
			tIsSuccessful = false;
			HelperClass.porticoOutput(1, "Exception in MultiClaimContainer-handleSingleClaim-objectId="+objectId+","+e.getMessage());
		}
		finally
		{
		}

		return tIsSuccessful;
	}

    public ArrayList getErrorString()
    {
		return m_erroredObjectList;
	}

    public void onOk(Control control, ArgumentList argumentlist)
    {
		HelperClass.porticoOutput("MultiClaimContainer-called-onOk()");
        setComponentReturn();
    }


    private boolean isSuccessful = true;
    private boolean isInitialized = false;
    private ArrayList m_erroredObjectList;
}