
package org.portico.conprep.ui.qcaction.resolveidentityconflict;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class ResolveIdentityConflict extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ResolveIdentityConflict()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_profileNameLabel = null;
        m_fuStateNameLabel = null;
        m_strDescText = null;

        m_strObjectId = null;
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;
        m_strSelectedFuType = "";
        m_strProviderName = "";
        m_strProfileName = "";
        m_fuTypeList = null;
        m_strBatchFolderId = "";
        m_strFuStateName = "";
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("accessionId"); // FU State Id
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("ResolveIdentityConflict-onInit()-Argument Fu State Id=" + m_strObjectId);
        HelperClass.porticoOutput("ResolveIdentityConflict-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("ResolveIdentityConflict-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_profileNameLabel = (Label)getControl("profile_name", com.documentum.web.form.control.Label.class);
	    m_fuStateNameLabel = (Label)getControl("fu_state_name", com.documentum.web.form.control.Label.class);
    	m_strDescText = (Text)getControl("desc", com.documentum.web.form.control.Text.class);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void initializeCommonData()
    {
		m_strBatchFolderId = HelperClass.getParentBatchFolderId(getDfSession(), m_strObjectId);
        // Pick current provider id, provider name
   		ValuePair tValuePair = null;
   		ArrayList attrList = new ArrayList();
    	attrList.add("p_provider_id");
    	attrList.add("p_profile_id");
        ArrayList outList = HelperClass.getObjectAttrValues(getDfSession(), DBHelperClass.BATCH_TYPE, getFolderObjectId(), attrList);
        if(outList != null && outList.size() > 0)
        {
			String attrValue = "";
			String attrName = "";
            for(int indx=0; indx < outList.size(); indx++)
            {
   	        	tValuePair = (ValuePair)outList.get(indx);
   	        	attrName = (String)tValuePair.getKey();
   	        	attrValue = (String)tValuePair.getValue();
   	        	if(attrName.equals("p_provider_id"))
   	        	{
					m_strProviderName = getLookupServiceName("provider", attrValue);
				}
				else if(attrName.equals("p_profile_id"))
				{
					m_strProfileName = getLookupServiceName("profile", attrValue);
				}
			}
	    }

        // Modify later to get the 'Label+(objectname)'
	    m_strFuStateName = QcHelperClass.getDisplayName(getDfSession(), m_strObjectId, DBHelperClass.FU_TYPE);

	    // populate rename file list
	    populateFuTypeList();
	}

    public void populateFuTypeList()
    {
		m_fuTypeList = QcHelperClass.getResolveIdentityConflictFuTypeChoices(getDfSession(), getFolderObjectId(), m_strObjectId); // pass the parent batch Id, fu state id
	}

    public String getLookupServiceName(String lookupService, String serviceId)
    {
		String retServiceName = "";
		ValuePair tValuePair = null;
		ArrayList attrList = new ArrayList();
       	attrList.add("name");
        ArrayList outList = HelperClass.lookupServiceInfo(lookupService, serviceId, attrList);
        if(outList != null && outList.size() > 0)
        {
			for(int indx=0; indx < outList.size(); indx++)
			{
				tValuePair = (ValuePair)outList.get(indx);
   	        	retServiceName = (String)tValuePair.getValue();
				break;
			}
		}

		return retServiceName;
	}

    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), getFolderObjectId(), DBHelperClass.BATCH_TYPE));
		m_providerNameLabel.setLabel(m_strProviderName);
		m_profileNameLabel.setLabel(m_strProfileName);
		m_fuStateNameLabel.setLabel(m_strFuStateName);
	}

	public List getPossibleFuTypeList()
	{
		return m_fuTypeList;
	}

    public boolean onCommitChanges()
    {
		boolean isSuccessful = validateUserInput();

        if(isSuccessful == true)
        {
	    	try
	    	{
	    		ArrayList userMsgIdList = new ArrayList();
	    		if(m_strUserMessageObjectId != null)
	    		{
	    	    	userMsgIdList.add(m_strUserMessageObjectId);
			    }
	    		isSuccessful = QcHelperClass.postProcessingForResolveIdentityConflict(getDfSession(), getFolderObjectId(), m_strObjectId, m_strSelectedFuType, userMsgIdList, m_strReEntryPoint, m_strReason);
	        }
	        catch(Exception e)
	        {
	    		HelperClass.porticoOutput("Exception in ResolveIdentityConflict-onCommitChanges()-"+e.getMessage());
	    	}
	    	finally
	    	{
	    		callErrorMessageService(isSuccessful, null);
	    	}
	    }

		return isSuccessful;
	}

	public boolean validateUserInput()
	{
		boolean isValid = true;

		String itemName = "";
        if(m_fuTypeList != null && m_fuTypeList.size() > 0) // ResolveIdentityConflict
        {
            Iterator tIterate = m_fuTypeList.iterator();
            int indx=0;
            while(tIterate.hasNext())
            {
				itemName = (String)tIterate.next();
				String radioName = ResolveIdentityConflict.POSSIBLE_FUTYPE_RADIOLABEL_PREFIX+indx;
				HelperClass.porticoOutput("ResolveIdentityConflict - validateUserInput - radioName="+radioName);
				Radio radio_level = (Radio)getControl(radioName, com.documentum.web.form.control.Radio.class);
				if(radio_level != null && radio_level.getValue())
				{
                    m_strSelectedFuType = itemName;
                    HelperClass.porticoOutput("ResolveIdentityConflict - validateUserInput - m_strSelectedFuType="+m_strSelectedFuType);
					break;
				}
                indx++;
            }
		}


		m_strReason = m_strDescText.getValue().trim();

		if(m_strReason == null || m_strReason.equals(""))
		{
			isValid = false;
            setReturnError("MSG_REASON_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_REASON_NOT_ENTERED", null);
		}
		if(m_strSelectedFuType == null || m_strSelectedFuType.equals(""))
		{
			isValid = false;
            setReturnError("MSG_FU_TYPE_NOT_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FU_TYPE_NOT_SELECTED", null);
		}


		return isValid;
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_RESOLVE_IDENTITY_CONFLICT_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_RESOLVE_IDENTITY_CONFLICT_SUCCESS");
		}
		else
		{
			setReturnError("MSG_RESOLVE_IDENTITY_CONFLICT_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_RESOLVE_IDENTITY_CONFLICT_FAILED", null);
		}
	}

	public String getFolderObjectId()
	{
		return m_strBatchFolderId;
	}

// Controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
	private Label m_profileNameLabel;
	private Label m_fuStateNameLabel;
    private Text m_strDescText;

// Data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strProviderName;
    private String m_strProfileName;
    private String m_strBatchFolderId;
    private String m_strFuStateName;
    private String m_strSelectedFuType;
    private String m_strReason;
    private List m_fuTypeList;

    public static String COMMA_SEPARATOR = ",";
    public static String POSSIBLE_FUTYPE_RADIOLABEL_PREFIX = "possiblefutypelabel";
}
