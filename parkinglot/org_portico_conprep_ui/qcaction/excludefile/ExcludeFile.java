
package org.portico.conprep.ui.qcaction.excludefile;

import java.util.ArrayList;
import java.util.Iterator;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class ExcludeFile extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ExcludeFile()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_strDescText = null;

        m_strObjectId = null;
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;
        m_strReason = "";
        m_strBatchFolderId = "";
        m_strProviderName = "";
        m_fileList = new ArrayList();
        m_selectedFileList = new ArrayList();
        m_strContextId = null;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("accessionId");
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("ExcludeFile-onInit()-Argument SuState Id=" + m_strObjectId);
        HelperClass.porticoOutput("ExcludeFile-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("ExcludeFile-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
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
		m_strContextId = m_strObjectId;
        // Pick provider name
   		ValuePair tValuePair = null;
   		ArrayList attrList = new ArrayList();
    	attrList.add("p_provider_id");
        ArrayList outList = HelperClass.getObjectAttrValues(getDfSession(), DBHelperClass.BATCH_TYPE, getFolderObjectId(), attrList);

        if(outList != null && outList.size() > 0)
        {
			attrList.clear();
			String attrValue = "";
            for(int indx=0; indx < outList.size(); indx++)
            {
   	        	tValuePair = (ValuePair)outList.get(indx);
   	        	attrValue = (String)tValuePair.getValue(); // value of p_provider_id
   	        	break;
			}

        	attrList.add("name");
        	outList.clear();
            outList = HelperClass.lookupServiceInfo("provider", attrValue, attrList); // format,id,attrlist("name",...)

            if(outList != null && outList.size() > 0)
            {
				for(int indx=0; indx < outList.size(); indx++)
				{
					tValuePair = (ValuePair)outList.get(indx);
     	        	m_strProviderName = (String)tValuePair.getValue(); // name value of the provider id.
					break;
				}
			}
	    }

        // populate exclude file list
	    populateExcludeFileList();

	}

    public void populateExcludeFileList()
    {
		m_fileList.clear();
		ArrayList m_ObjectIdList = new ArrayList();
		if(HelperClass.getObjectType(getDfSession(), m_strObjectId).equalsIgnoreCase(DBHelperClass.CU_TYPE))
		{
			// m_ObjectIdList = (ArrayList)QcHelperClass.getLeadMetadataInfo(getDfSession(), getFolderObjectId(), m_strObjectId);
			m_ObjectIdList = DBHelperClass.getActiveLeadMetadataPerArticle(m_strObjectId);
		}
		else // SU state
		{
			m_ObjectIdList.add(m_strObjectId);
		}
   		if(m_ObjectIdList != null && m_ObjectIdList.size() > 0)
	    {
     		ValuePair valuePair = null;
     		String currentKey = "";
	    	for(int indx=0; indx < m_ObjectIdList.size(); indx++)
	    	{
				currentKey = (String)m_ObjectIdList.get(indx);
				valuePair = new ValuePair();
				valuePair.setKey(currentKey);
				// Later on put the SU display name (workfilename+(su state object name))
				valuePair.setValue(getDisplayName(currentKey)) ; // HelperClass.getObjectName(getDfSession(), HelperClass.getRawUnitIdFromSuState(getDfSession(), currentKey))); // HelperClass.getObjectName(getDfSession(), currentKey)
	        	m_fileList.add(valuePair);
	        }
		}
	}

	public ArrayList getFileList()
	{
		return m_fileList;
	}


/*
	public ArrayList getFileObjectIds()
	{
		ArrayList fileObjectIds = new ArrayList();

        // SU State Id or CU State Id
		if(m_strObjectId != null)
		{
			if(m_strObjectId.indexOf(COMMA_SEPARATOR) != -1)
			{
				StringTokenizer tStringTokenizer = new StringTokenizer(m_strObjectId, COMMA_SEPARATOR);
				while (tStringTokenizer.hasMoreTokens())
				{
					fileObjectIds.add(HelperClass.getRawUnitIdFromSuState(getDfSession(), (String)tStringTokenizer.nextToken().trim()));
				}
			}
			else
			{
			    fileObjectIds.add(HelperClass.getRawUnitIdFromSuState(getDfSession(), m_strObjectId));
			}
		}
		return fileObjectIds;
	}

*/
    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), getFolderObjectId(), DBHelperClass.BATCH_TYPE));
		m_providerNameLabel.setLabel(m_strProviderName);
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
	    		// pass m_selectedFileList
	    		// String suState[] = (String[])m_selectedFileList.toArray();
	    		isSuccessful = QcHelperClass.postProcessingForExcludeFile(getDfSession(), HelperClass.getParentBatchFolderId(getDfSession(), m_strObjectId), m_selectedFileList, userMsgIdList, m_strReEntryPoint, m_strReason, m_strContextId);
	        }
	        catch(Exception e)
	        {
	    		HelperClass.porticoOutput("Exception in ExcludeFile-"+e.getMessage());
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

        if(m_fileList != null && m_fileList.size() > 0)
        {
            Iterator tIterate = m_fileList.iterator();
            int indx=0;
            ValuePair valuePair = null;
            while(tIterate.hasNext())
            {
				valuePair = (ValuePair)tIterate.next();
				String checkBoxName = ExcludeFile.EXCLUDEFILE_CHECKBOXNAME_PREFIX+indx;
				HelperClass.porticoOutput("ExcludeFile - validateUserInput - checkBoxName="+checkBoxName);
				Checkbox checkbox_level = (Checkbox)getControl(checkBoxName, com.documentum.web.form.control.Checkbox.class);
				HelperClass.porticoOutput("ExcludeFile - validateUserInput - checkbox_level="+checkbox_level);
				HelperClass.porticoOutput("ExcludeFile - validateUserInput - checkbox_level-getValue="+checkbox_level.getValue());
				if(checkbox_level != null && checkbox_level.getValue())
				{
					String itemName = valuePair.getKey();
                    m_selectedFileList.add(itemName);
                    HelperClass.porticoOutput("ExcludeFile - validateUserInput - itemName="+itemName);
				}
                indx++;
            }
		}

		if(m_selectedFileList == null || m_selectedFileList.size() <= 0)
		{
			isValid = false;
            setReturnError("MSG_FILES_NOT_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FILES_NOT_SELECTED", null);
		}

		m_strReason = m_strDescText.getValue().trim();

		if(m_strReason == null || m_strReason.equals(""))
		{
			isValid = false;
            setReturnError("MSG_REASON_NOT_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_REASON_NOT_ENTERED", null);
		}

		return isValid;
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_EXCLUDEFILE_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_EXCLUDEFILE_SUCCESS");
		}
		else
		{
			setReturnError("MSG_EXCLUDEFILE_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_EXCLUDEFILE_FAILED", null);
		}
	}

	public String getFolderObjectId()
	{
		return m_strBatchFolderId;
	}

	public String getDisplayName(String suStateId)
	{
		String displayName = "";
		ArrayList attrList = new ArrayList();
		attrList.add("p_work_filename");
		ArrayList outList = HelperClass.getObjectAttrValues(getDfSession(), DBHelperClass.SU_TYPE, suStateId, attrList);
		if(outList != null && outList.size() > 0)
		{
			for(int aindx=0; aindx < outList.size(); aindx++)
			{
				ValuePair tValuePair = (ValuePair)outList.get(aindx);
				if(tValuePair.getKey().equals("p_work_filename"))
				{
					displayName = tValuePair.getValue();
					break;
				}
			}
		}

		displayName = displayName + "(" + HelperClass.getObjectName(getDfSession(), suStateId, DBHelperClass.SU_TYPE) + ")";

		return displayName;
	}

// Controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
	private Text m_strDescText;

// Data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strReason;
    private String m_strBatchFolderId;
    private String m_strProviderName;
    private ArrayList m_fileList;
    private ArrayList m_selectedFileList;

    private String m_strContextId;

    public static String COMMA_SEPARATOR = ",";
    public static String EXCLUDEFILE_CHECKBOXNAME_PREFIX="excludefilename";

}
