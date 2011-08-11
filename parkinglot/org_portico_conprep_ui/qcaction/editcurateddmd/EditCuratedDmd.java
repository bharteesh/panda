
package org.portico.conprep.ui.qcaction.editcurateddmd;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class EditCuratedDmd extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public EditCuratedDmd()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_profileNameLabel = null;
        m_cuStateNameLabel = null;
        // m_curatedDmdTextArea = null;
        m_reasonText = null;

        m_strObjectId = null;
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;
        m_strProviderName = "";
        m_strProfileName = "";
        m_strBatchFolderId = "";
        m_strExistingCuratedDmd = "";
        m_strNewCuratedDmd = "";
        m_strCuStateName = "";
        m_strCuratedDmdObjectId = "";
        m_strHasBeenCheckedOutByThisUser = "false";
        m_strHasBeenCheckedInByThisUser = "false";
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("accessionId"); // CU State Id
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("EditCuratedDmd-onInit()-Argument Batch Id=" + m_strObjectId);
        HelperClass.porticoOutput("EditCuratedDmd-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("EditCuratedDmd-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_profileNameLabel = (Label)getControl("profile_name", com.documentum.web.form.control.Label.class);
	    m_cuStateNameLabel = (Label)getControl("cu_state_name", com.documentum.web.form.control.Label.class);
    	// m_curatedDmdTextArea = (TextArea)getControl("curated_dmd_textarea", com.documentum.web.form.control.TextArea.class);
	    m_reasonText = (Text)getControl("reason", com.documentum.web.form.control.Text.class);

        initializeCommonData();
        initializeCommonControls();
/*
        Control control = getContainer();
        if(control instanceof Form)
            ((Form)control).setModal(false);
        setModal(false);
*/
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
        try
        {
            if(m_strCuratedDmdObjectId != null && !m_strCuratedDmdObjectId.equals(""))
            {
                IDfSysObject currentIDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(m_strCuratedDmdObjectId));
                if(currentIDfSysObject.isCheckedOutBy(getDfSession().getLoginUserName()))
                {
					// Set this to see if the user had edited the file while on this component call
		    		m_strHasBeenCheckedOutByThisUser = "true";
		    	}
		    	else
		    	{
					// Currently the content file is not in a checked out state, so check
					// if the user had edited the file while on this component call
					// if true, that means the user had edited and checked in the file
					if(m_strHasBeenCheckedOutByThisUser != null && m_strHasBeenCheckedOutByThisUser.equals("true"))
					{
						// Set the checked in flag
						m_strHasBeenCheckedInByThisUser = "true";
					}
				}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput("Exception in EditCuratedDmd-onRender()-"+e.getMessage());
		}
		finally
		{
		}
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
	    m_strCuStateName = QcHelperClass.getDisplayName(getDfSession(), m_strObjectId, DBHelperClass.CU_TYPE);

	    // populate the existing curated dmd
	    populateExistingCuratedDmd();
	}

    public void populateExistingCuratedDmd()
    {
		try
		{
    		Hashtable curatedDmdObjectIdAccessionIdPair = QcHelperClass.getCuratedDmdObjectId(getDfSession(), getFolderObjectId(), m_strObjectId);
    		if(curatedDmdObjectIdAccessionIdPair != null && curatedDmdObjectIdAccessionIdPair.size() > 0)
    		{
                Iterator iterator = curatedDmdObjectIdAccessionIdPair.keySet().iterator();
                while(iterator.hasNext())
                {
					m_strCuratedDmdObjectId = (String)iterator.next();
					m_strCuratedDmdAccessionId = (String)curatedDmdObjectIdAccessionIdPair.get(m_strCuratedDmdObjectId);
    		    }

        		// m_strCuratedDmdObjectName
			}
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput("Exception in EditCuratedDmd-populateExistingCuratedDmd()-"+e.getMessage());
		}
		finally
		{
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
		m_cuStateNameLabel.setLabel(m_strCuStateName);
		// m_curatedDmdTextArea.setValue(m_strExistingCuratedDmd);
	}

	public String getFolderObjectId()
	{
		return m_strBatchFolderId;
	}

	public String getCuratedDmdObjectId()
	{
		return m_strCuratedDmdObjectId;
	}

	public String getHasBeenCheckedOutByThisUser()
	{
    	return m_strHasBeenCheckedOutByThisUser;
    }

    public boolean onCommitChanges()
    {
		boolean isSuccessful = true;
		IDfCollection iDfVersionCollection = null;

	    try
	    {
			isSuccessful = validateUserInput();
            if(m_strCuratedDmdObjectId != null && !m_strCuratedDmdObjectId.equals(""))
            {
                IDfSysObject currentIDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(m_strCuratedDmdObjectId));
                if(currentIDfSysObject.isCheckedOutBy(getDfSession().getLoginUserName()))
                {
					isSuccessful = false;
					callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_SAVE_CHANGES", null);
				}

    			// Content File has already been checked in by this User while on this component call
                if(isSuccessful == true && m_strHasBeenCheckedInByThisUser != null && m_strHasBeenCheckedInByThisUser.equals("true"))
                {
					String newCheckedInDmdObjectId = "";
					String newCheckedInDmdObjectModifiedDate = "";
					iDfVersionCollection = currentIDfSysObject.getVersions("r_modify_date,r_object_id");
					if(iDfVersionCollection != null)
					{
						// The latest version(CURRENT) is returned first
					    while(iDfVersionCollection.next())
					    {
							newCheckedInDmdObjectId = iDfVersionCollection.getString("r_object_id");
							newCheckedInDmdObjectModifiedDate = iDfVersionCollection.getString("r_modify_date");
							break;
						}
				    }

				    if(newCheckedInDmdObjectId != null && !newCheckedInDmdObjectId.equals(""))
				    {
            	        HelperClass.porticoOutput("EditCuratedDmd-onCommitChanges()-call-callPostProcessing()-newCheckedInDmdObjectId,newCheckedInDmdObjectModifiedDate="+newCheckedInDmdObjectId+","+newCheckedInDmdObjectModifiedDate);
    			    	callPostProcessing(newCheckedInDmdObjectId);
				    }
				    else
				    {
            			isSuccessful = false;
						HelperClass.porticoOutput(1, "EditCuratedDmd-onCommitChanges()-newCheckedInDmdObjectId is null="+newCheckedInDmdObjectId);
            	    	callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_FAILED", null);
					}
    			}
		    }
	    }
	    catch(Exception e)
	    {
			isSuccessful = false;
	    	HelperClass.porticoOutput("Exception in EditCuratedDmd-onCommitChanges()-"+e.getMessage());
	    	callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_FAILED", null);
	    }
	    finally
	    {
			try
			{
			    if(iDfVersionCollection != null)
			    {
			    	iDfVersionCollection.close();
			    }
		    }
		    catch(Exception e)
		    {
    	    	HelperClass.porticoOutput("Exception in EditCuratedDmd-onCommitChanges()-iDfVersionCollection.close()="+e.getMessage());
			}
	    }

		return isSuccessful;
	}

	public boolean validateUserInput()
	{
		boolean isValid = true;

		String reason = m_reasonText.getValue().trim();

        if(null == reason || reason.equals(""))
		{
			isValid = false;
            setReturnError("MSG_FORMAT_REASON_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FORMAT_REASON_NOT_ENTERED", null);
		}

		return isValid;
	}

	public boolean onCancelChanges()
	{
		boolean isSuccessful = true;
		IDfCollection iDfVersionCollection = null;
		try
		{
            if(m_strCuratedDmdObjectId != null && !m_strCuratedDmdObjectId.equals(""))
            {
                IDfSysObject currentIDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(m_strCuratedDmdObjectId));
                if(currentIDfSysObject.isCheckedOutBy(getDfSession().getLoginUserName()))
                {
					// cancels checkout without saving the changes if any
					currentIDfSysObject.cancelCheckout();
					// Is this save required ??
					currentIDfSysObject.save();
				}
				else
				{
					// Content File has already been checked in by this User while on this component call
					if(m_strHasBeenCheckedInByThisUser != null && m_strHasBeenCheckedInByThisUser.equals("true"))
					{
        	        	HelperClass.porticoOutput("EditCuratedDmd-onCancelChanges()-call-callPostProcessing()");

					    String newCheckedInDmdObjectId = "";
					    String newCheckedInDmdObjectModifiedDate = "";
					    iDfVersionCollection = currentIDfSysObject.getVersions("r_modify_date,r_object_id");
					    if(iDfVersionCollection != null)
					    {
					    	// The latest version(CURRENT) is returned first
					        while(iDfVersionCollection.next())
					        {
					    		newCheckedInDmdObjectId = iDfVersionCollection.getString("r_object_id");
					    		newCheckedInDmdObjectModifiedDate = iDfVersionCollection.getString("r_modify_date");
					    		break;
					    	}
				        }

    				    if(newCheckedInDmdObjectId != null && !newCheckedInDmdObjectId.equals(""))
				        {
            	            HelperClass.porticoOutput("EditCuratedDmd-onCancelChanges()-call-callPostProcessing()-newCheckedInDmdObjectId,newCheckedInDmdObjectModifiedDate="+newCheckedInDmdObjectId+","+newCheckedInDmdObjectModifiedDate);
    			        	callPostProcessing(newCheckedInDmdObjectId);
				        }
				        else
				        {
            		    	isSuccessful = false;
					    	HelperClass.porticoOutput(1, "EditCuratedDmd-onCancelChanges()-newCheckedInDmdObjectId is null="+newCheckedInDmdObjectId);
            	        	callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_FAILED", null);
					    }
				    }
				}
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput("Exception in EditCuratedDmd-onCancelChanges()-"+e.getMessage());
		}
		finally
		{
			try
			{
			    if(iDfVersionCollection != null)
			    {
			    	iDfVersionCollection.close();
			    }
		    }
		    catch(Exception e)
		    {
    	    	HelperClass.porticoOutput("Exception in EditCuratedDmd-onCancelChanges()-iDfVersionCollection.close()="+e.getMessage());
			}
		}
		return isSuccessful;
	}

	public void callPostProcessing(String newCheckedInDmdObjectId)
	{
		boolean isSuccessful = true;

	   	ArrayList userMsgIdList = new ArrayList();
	   	if(m_strUserMessageObjectId != null)
	   	{
	       	userMsgIdList.add(m_strUserMessageObjectId);
	    }

		// Clear the 'a_category' attribute on the CDMD documentum object, reason being if the
		// value reads 'Default XML Application' the view/edit applets throws the following error
		// 'ERROR: Failed to download document for viewing.:The processing instruction target matching "[xX][mM]... '
		// and does not open the xml document.This happens after the file goes thro' the JAXB Curation tool
		// Probably the standalone="yes" on the header xml tag along with the 'Default XML Application' property(added by the checkin applet)
		// are not a valid combination within the view/edit applets.

		try
		{
	    	IDfSysObject latestCDMDIDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(newCheckedInDmdObjectId));
	    	latestCDMDIDfSysObject.setString("a_category", "");
	    	latestCDMDIDfSysObject.save();
    	    // isSuccessful = QcHelperClass.postProcessingForEditCuratedDmd(getDfSession(), getFolderObjectId(), m_strObjectId, utf8String, userMsgIdList, m_strReEntryPoint, reason);
    	    isSuccessful = QcHelperClass.postProcessingForEditCuratedDmdMessages(getDfSession(), getFolderObjectId(), m_strObjectId, userMsgIdList, m_strReEntryPoint, m_reasonText.getValue().trim(), m_strCuratedDmdAccessionId, newCheckedInDmdObjectId);
	    }
	    catch(Exception e)
	    {
			isSuccessful = false;
			HelperClass.porticoOutput(1, "Exception in EditCuratedDmd-callPostProcessing()-new checkedin r_object_id="+newCheckedInDmdObjectId+":"+e.getMessage());
		}
		finally
		{
		}

        if(isSuccessful == true)
        {
     	   	callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_SUCCESS", null);
		}
		else
		{
			callErrorMessageService(isSuccessful, "MSG_EDIT_CURATED_DMD_FAILED", null);
		}
	}


	public void callErrorMessageService(boolean status, String nlsString, String msgText)
	{
		if(status)
		{
			setReturnError(nlsString, null, null);
			MessageService.addMessage(this, nlsString);
		}
		else
		{
			setReturnError(nlsString, null, null);
			ErrorMessageService.getService().setNonFatalError(this, nlsString, null);
		}
	}

// Controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
	private Label m_profileNameLabel;
	private Label m_cuStateNameLabel;
	// private TextArea m_curatedDmdTextArea;
	private Text m_reasonText;

// Data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strProviderName;
    private String m_strProfileName;
    private String m_strBatchFolderId;
    private String m_strCuStateName;

    private String m_strExistingCuratedDmd;
    private String m_strNewCuratedDmd;

    private String m_strCuratedDmdObjectId;
    private String m_strCuratedDmdAccessionId;
    private String m_strHasBeenCheckedOutByThisUser;
    private String m_strHasBeenCheckedInByThisUser;

    public static String COMMA_SEPARATOR = ",";
}
