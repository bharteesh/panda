
package org.portico.conprep.ui.qcaction.changeprofile;

import java.util.ArrayList;

import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;
import org.portico.conprep.ui.profile.ProfileUI;
import org.portico.conprep.ui.provider.ProviderUI;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class ChangeProfile extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ChangeProfile()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_strDescText = null;
        m_profileDropDownList = null;
        m_existingProfileNameLabel = null;

        m_strObjectId = "";
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;
        m_strReason = "";
        m_strProviderName = "";
        m_strProviderId = "";
        listProviderUI = new ArrayList();
        m_strExistingProfileName = "";
        m_strExistingProfileId = "";
        m_strNewProfileId = "";
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Object Id
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("ChangeProfile-onInit()-Argument SuState Id=" + m_strObjectId);
        HelperClass.porticoOutput("ChangeProfile-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("ChangeProfile-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_existingProfileNameLabel = (Label)getControl("existing_profile_name", com.documentum.web.form.control.Label.class);
    	m_profileDropDownList = (DropDownList)getControl("dropdownlist_profile", com.documentum.web.form.control.DropDownList.class);
    	m_profileDropDownList.setMutable(true);
	    m_strDescText = (Text)getControl("desc", com.documentum.web.form.control.Text.class);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void initializeCommonData()
    {
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
					m_strProviderId = attrValue;
					m_strProviderName = getLookupServiceName("provider", m_strProviderId);
				}
				else if(attrName.equals("p_profile_id"))
				{
                    m_strExistingProfileId = attrValue;
					m_strExistingProfileName = getLookupServiceName("profile", m_strExistingProfileId);
				}
			}
	    }
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
   	        	retServiceName = (String)tValuePair.getValue(); // name value of the provider id.
				break;
			}
		}

		return retServiceName;
	}

    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), getFolderObjectId(), DBHelperClass.BATCH_TYPE));
		m_providerNameLabel.setLabel(m_strProviderName);
		m_existingProfileNameLabel.setLabel(m_strExistingProfileName);

	    // populate profiles list options for this provider
	    populateProfilesDropDownList();
	}

    public void populateProfilesDropDownList()
    {
		listProviderUI = AppSessionContext.getProviderUI();
		// Get the provideId from the 'p_batch' attribute
		populateProfileOption(m_strProviderId);
	}

	public void populateProfileOption(String selectedProviderID)
	{
		m_profileDropDownList.setMutable(true);
		m_profileDropDownList.clearOptions();

		if(listProviderUI != null)
        {
			Option option = null;
			ProviderUI tProvider = null;
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
                if(currentProviderID.equals(selectedProviderID))
                {
					String tProviderDefaultProfileID = tProvider.getDefaultProfileID();
					ArrayList tProfileList = tProvider.getListProfileUI();
					ProfileUI tProfile = null;
					for(int defprofIndx=0; defprofIndx < tProfileList.size(); defprofIndx++)
					{
						tProfile = (ProfileUI)tProfileList.get(defprofIndx);
						String defProfileID = tProfile.getProfileID();
						if(defProfileID.equals(tProviderDefaultProfileID))
						{
                            option = new Option();
                            option.setValue(tProfile.getProfileID());
                            option.setLabel(tProfile.getProfileName());
             			    m_profileDropDownList.addOption(option);
             			    break;
						}
					}

					for(int profIndx=0; profIndx < tProfileList.size(); profIndx++)
					{
						tProfile = (ProfileUI)tProfileList.get(profIndx);
						String defProfileID = tProfile.getProfileID();
						if(defProfileID.equals(tProviderDefaultProfileID))
						{
							continue;
						}
                        option = new Option();
                        option.setValue(tProfile.getProfileID());
                        option.setLabel(tProfile.getProfileName());
             			m_profileDropDownList.addOption(option);
					}

					break;
				}
			}
		}
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
	    		isSuccessful = QcHelperClass.postProcessingForChangeProfile(getDfSession(), m_strObjectId, m_strExistingProfileId, m_strNewProfileId, userMsgIdList, m_strReEntryPoint, m_strReason);
	        }
	        catch(Exception e)
	        {
	    		HelperClass.porticoOutput("Exception in ChangeProfile-onCommitChanges()-"+e.getMessage());
	    	}
	    	finally
	    	{
				// Not required since this function will return false and the form will continue to be displayed,
				// in case of error
	    		callErrorMessageService(isSuccessful, null);
	    	}
	    }

		return isSuccessful;
	}

// This code not used currently
/*
	public boolean checkUserResponse()
	{
		boolean isUserAccepted = false;
		YesNoDialog confirmDialog = null;
		try
		{
	    	confirmDialog = new YesNoDialog(this, "Change Profile", "Change Profile", "Do you want?", "Yes", "No");
	    	confirmDialog.setModal(true);
	    	confirmDialog.show();
	    	isUserAccepted = confirmDialog.getUserResponse();
	    	HelperClass.porticoOutput("ChangeProfile-checkUserResponse-isUserAccepted="+isUserAccepted);
	    }
	    catch(Exception e)
	    {
		}
		finally
		{
	    	confirmDialog.dispose();
	    }

	// java.awt.Frame frame, java.lang.String strTitle, java.lang.String strMessage, java.lang.String strQuestion, java.lang.String strYesButtonText, java.lang.String strNoButtonText)

	    return isUserAccepted;
    }
*/

	public boolean validateUserInput()
	{
		boolean isValid = true;
		m_strReason = m_strDescText.getValue().trim();
		m_strNewProfileId = m_profileDropDownList.getValue(); // .trim();

		if(m_strReason == null || m_strReason.equals(""))
		{
			isValid = false;
            setReturnError("MSG_REASON_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_REASON_NOT_ENTERED", null);
		}

		if(m_strNewProfileId == null || m_strNewProfileId.equals(""))
		{
			isValid = false;
            setReturnError("MSG_PROFILE_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_PROFILE_SELECTED", null);
		}
		else if(m_strNewProfileId.equals(m_strExistingProfileId))
		{
			isValid = false;
            setReturnError("MSG_SAME_AS_EXISTING_PROFILE", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_SAME_AS_EXISTING_PROFILE", null);
		}

		return isValid;
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_CHANGEPROFILE_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_CHANGEPROFILE_SUCCESS");
		}
		else
		{
			setReturnError("MSG_CHANGEPROFILE_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_CHANGEPROFILE_FAILED", null);
		}
	}

	public String getFolderObjectId()
	{
		return m_strObjectId;
	}

// Controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
    private Label m_existingProfileNameLabel;
	private Text m_strDescText;
	private DropDownList m_profileDropDownList;


// Data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strReason;
    private String m_strProviderName;
    private String m_strProviderId;
    private ArrayList listProviderUI;
    private String m_strExistingProfileName;
    private String m_strExistingProfileId;
    private String m_strNewProfileId;

    public static String COMMA_SEPARATOR = ",";
}
