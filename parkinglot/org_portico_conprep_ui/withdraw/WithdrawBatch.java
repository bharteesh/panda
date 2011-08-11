
package org.portico.conprep.ui.withdraw;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;

public class WithdrawBatch extends Component
{
    public WithdrawBatch()
    {
        m_strObjectId = null;
        m_strObjectName = null;
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Folder Id
        m_batchLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
        m_strObjectName = HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE); // getBatchName();
        m_batchLabel.setLabel(m_strObjectName);
    }

    public boolean onCommitChanges()
    {
		boolean isSuccessful = false;

    	try
    	{
    		isSuccessful = HelperClass.postProcessingForWithdraw(getDfSession(), m_strObjectId);
/*
            String rawUnitCount = ""+HelperClass.getAssetCountForBatchObject(getDfSession(), m_strObjectId);
            HelperClass.porticoOutput(0, "WithdrawBatch - onCommitChanges - Start setting p_rawunit_count="+rawUnitCount);
    		IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
    		iDfSysObject.setString("p_rawunit_count", rawUnitCount);
            HelperClass.porticoOutput(0, "WithdrawBatch - onCommitChanges - End setting p_rawunit_count="+rawUnitCount);
    		iDfSysObject.save();
*/
        }
        catch(Exception e)
        {
    		HelperClass.porticoOutput("Exception in WithdrawBatch-"+e.getMessage());
    	}
    	finally
    	{
			// Not required since this function will return false and the form will continue to be displayed,
			// in case of error
    		callErrorMessageService(isSuccessful, null);
    	}

		return isSuccessful;
    }

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_WITHDRAW_BATCH_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_WITHDRAW_BATCH_SUCCESS");
		}
		else
		{
			setReturnError("MSG_WITHDRAW_BATCH_ERROR", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_WITHDRAW_BATCH_ERROR", null);
		}
	}

    private String m_strObjectId;
    private String m_strObjectName;
    private Label m_batchLabel;
}