
package org.portico.conprep.ui.qcaction.renamefile;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class RenameFile extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public RenameFile()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_profileNameLabel = null;
        m_fileNewNameText = null;
        m_reasonText = null;

        m_strObjectId = null;
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;
        m_strNewName = "";
        m_strBatchFolderId = "";
        m_strProviderName = "";
        m_strProfileName = "";
        m_fileList = new ArrayList();
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("accessionId");
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("RenameFile-onInit()-Argument SuState Id=" + m_strObjectId);
        HelperClass.porticoOutput("RenameFile-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("RenameFile-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_profileNameLabel = (Label)getControl("profile_name", com.documentum.web.form.control.Label.class);
	    m_fileNewNameText = (Text)getControl("file_new_name", com.documentum.web.form.control.Text.class);
	    m_reasonText = (Text)getControl("reason", com.documentum.web.form.control.Text.class);

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

	    // populate rename file list
	    populateRenameFileList();
	}

    public void populateRenameFileList()
    {
        ArrayList fileObjectList = getFileObjectIds();
		if(fileObjectList != null && fileObjectList.size() > 0)
		{
			for(int indx=0; indx < fileObjectList.size(); indx++)
			{
		    	m_fileList.add(QcHelperClass.getExistingNameForRename(getDfSession(), getFolderObjectId(), (String)fileObjectList.get(indx)));
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
   	        	retServiceName = (String)tValuePair.getValue();
				break;
			}
		}

		return retServiceName;
	}

	public ArrayList getFileList()
	{
		return m_fileList;
	}

	public ArrayList getFileObjectIds()
	{
		ArrayList fileObjectIds = new ArrayList();

        // SU State Id
		if(m_strObjectId != null)
		{
			if(m_strObjectId.indexOf(COMMA_SEPARATOR) != -1)
			{
				StringTokenizer tStringTokenizer = new StringTokenizer(m_strObjectId, COMMA_SEPARATOR);
				while (tStringTokenizer.hasMoreTokens())
				{
					fileObjectIds.add((String)tStringTokenizer.nextToken().trim());
				}
			}
			else
			{
			    fileObjectIds.add(m_strObjectId);
			}
		}
		return fileObjectIds;
	}

    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), getFolderObjectId(), DBHelperClass.BATCH_TYPE));
		m_providerNameLabel.setLabel(m_strProviderName);
		m_profileNameLabel.setLabel(m_strProfileName);
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
	    		isSuccessful = QcHelperClass.postProcessingForRenameFile(getDfSession(), getFolderObjectId(), m_strObjectId, m_strNewName, userMsgIdList, m_strReEntryPoint, m_reasonText.getValue().trim());

	        }
	        catch(Exception e)
	        {
	    		HelperClass.porticoOutput("Exception in RenameFile-"+e.getMessage());
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

	public boolean validateUserInput()
	{
		boolean isValid = true;
		m_strNewName = m_fileNewNameText.getValue().trim();
		String reason = m_reasonText.getValue().trim();

		if(m_strNewName == null || m_strNewName.equals(""))
		{
			isValid = false;
            setReturnError("MSG_NEW_NAME_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NEW_NAME_NOT_ENTERED", null);
		}
		else if(null == reason || reason.equals(""))
		{
			isValid = false;
            setReturnError("MSG_FORMAT_REASON_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FORMAT_REASON_NOT_ENTERED", null);
		}

		return isValid;
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_RENAMEFILE_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_RENAMEFILE_SUCCESS");
		}
		else
		{
			setReturnError("MSG_RENAMEFILE_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_RENAMEFILE_FAILED", null);
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
	private Text m_fileNewNameText;
	private Text m_reasonText;

// Data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strNewName;
    private String m_strBatchFolderId;
    private String m_strProviderName;
    private String m_strProfileName;
    private ArrayList m_fileList;

    public static String COMMA_SEPARATOR = ",";
}
