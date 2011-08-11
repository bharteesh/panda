
package org.portico.conprep.ui.qcaction.formatassignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class FormatAssignment extends Component
{
    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public FormatAssignment()
    {
		// Controls
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_profileNameLabel = null;
        m_fileNameLabel = null;
        m_reasonText = null;

        m_existingFormatIdLabel = null;
        m_existingFormatNameLabel = null;
        m_formatDropDownList = null;
        m_selectedFormatNameLabel = null;

        m_typeOfAssignmentDropDownList = null;

        m_existingMimeTypeLabel = null;
        m_mimeTypeDropDownList = null;
        m_selectedMimeTypeLabel = null;

        // Data
        m_strObjectId = null;
        m_strUserMessageObjectId = null;
        m_strReEntryPoint = null;

        m_strBatchFolderId = "";
        m_strProviderName = "";
        m_strProfileName = "";

        m_strExistingFormatId = "";
        m_strExistingFormatName = "";
        m_formatList = new TreeMap();
        m_selectedFormatName = "";
        m_fileName = "";

        m_strExistingMimeType = "";
        m_strSelectedMimeType = "";
        m_mimeTypeList = new TreeMap();
        m_typeOfAssignment = "";
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("accessionId");
        m_strUserMessageObjectId = argumentlist.get("msgObjectId");
        m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

        HelperClass.porticoOutput("FormatAssignment-onInit()-Argument SuState Id=" + m_strObjectId);
        HelperClass.porticoOutput("FormatAssignment-onInit()-Argument m_strUserMessageObjectId=" + m_strUserMessageObjectId);
        HelperClass.porticoOutput("FormatAssignment-onInit()-Argument m_strReEntryPoint=" + m_strReEntryPoint);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_profileNameLabel = (Label)getControl("profile_name", com.documentum.web.form.control.Label.class);
		m_fileNameLabel = (Label)getControl("file_name", com.documentum.web.form.control.Label.class);
	    m_reasonText = (Text)getControl("reason", com.documentum.web.form.control.Text.class);

	    m_existingFormatIdLabel = (Label)getControl("existing_format_id", com.documentum.web.form.control.Label.class);
	    m_existingFormatNameLabel = (Label)getControl("existing_format_name", com.documentum.web.form.control.Label.class);
	    m_formatDropDownList = (DropDownList)getControl("dropdownlist_format", com.documentum.web.form.control.DropDownList.class);
	    m_selectedFormatNameLabel = (Label)getControl("selected_format_name", com.documentum.web.form.control.Label.class);

        m_typeOfAssignmentDropDownList = (DropDownList)getControl("dropdownlist_assignment_type", com.documentum.web.form.control.DropDownList.class);

	    m_existingMimeTypeLabel = (Label)getControl("existing_mime_type", com.documentum.web.form.control.Label.class);
	    m_mimeTypeDropDownList = (DropDownList)getControl("dropdownlist_mime_type", com.documentum.web.form.control.DropDownList.class);
	    m_selectedMimeTypeLabel = (Label)getControl("selected_mime_type", com.documentum.web.form.control.Label.class);

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

	    populateFormatList();
	    populateMimeTypeList();

	    ArrayList alist = new ArrayList();
	    alist.add(DBHelperClass.P_FORMAT_ID);
	    alist.add(DBHelperClass.P_FORMAT_NAME);
	    alist.add(DBHelperClass.P_WORK_FILENAME);
	    alist.add(DBHelperClass.P_NAME);
	    alist.add(DBHelperClass.P_MIME_TYPE);

        HelperClass.porticoOutput("FormatAssignment-DBHelperClass.SU_TYPE=" + DBHelperClass.SU_TYPE);
        Hashtable attrHash = DBHelperClass.getObjectAttributes(DBHelperClass.SU_TYPE, m_strObjectId, alist);
        if(null != attrHash)
        {
			m_fileName = (String)attrHash.get(DBHelperClass.P_WORK_FILENAME) + "("+(String)attrHash.get(DBHelperClass.P_NAME)+")";
			m_strExistingFormatId = (String)attrHash.get(DBHelperClass.P_FORMAT_ID);
			if(null == m_strExistingFormatId)
			{
				m_strExistingFormatId = "";
                HelperClass.porticoOutput("FormatAssignment-m_strExistingFormatId is null being set to ="+m_strExistingFormatId);
			}
	        m_strExistingFormatName = (String)attrHash.get(DBHelperClass.P_FORMAT_NAME);
	        if(null == m_strExistingFormatName)
	        {
				m_strExistingFormatName = "";
                HelperClass.porticoOutput("FormatAssignment-m_strExistingFormatName is null being set to ="+m_strExistingFormatName);
			}
	        m_strExistingMimeType = (String)attrHash.get(DBHelperClass.P_MIME_TYPE);
	        if(null == m_strExistingMimeType)
	        {
				m_strExistingMimeType = "";
                HelperClass.porticoOutput("FormatAssignment-m_strExistingMimeType is null being set to ="+m_strExistingMimeType);
			}
	    }
	}

    public void populateFormatList()
    {
		m_formatList = HelperClass.getFormatInfo();
	}

	public void populateMimeTypeList()
	{
		m_mimeTypeList = HelperClass.getMimeTypeList();
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
		m_fileNameLabel.setLabel(m_fileName);

		m_typeOfAssignmentDropDownList.setMutable(true);
		m_typeOfAssignmentDropDownList.clearOptions();

		for(int indx = 0; indx < TYPES_OF_ASSIGNMENT.length; indx++)
		{
    		Option option = new Option();
    		option.setValue(TYPES_OF_ASSIGNMENT[indx]);
    		option.setLabel(TYPES_OF_ASSIGNMENT[indx]);
    		m_typeOfAssignmentDropDownList.addOption(option);
		}
		// Default the selection to FORMAT Assignment
		m_typeOfAssignmentDropDownList.setValue(FORMAT_ASSIGNMENT);

		m_typeOfAssignment = m_typeOfAssignmentDropDownList.getValue();
		displayControlsBasedOnTypeOfAssignment();
	}

	public void displayControlsBasedOnTypeOfAssignment()
	{
		if(m_typeOfAssignment.equals(FORMAT_ASSIGNMENT))
		{
    		m_existingFormatIdLabel.setLabel(m_strExistingFormatId);
		    m_existingFormatNameLabel.setLabel(m_strExistingFormatName);

		    m_formatDropDownList.setMutable(true);
		    m_formatDropDownList.clearOptions();

		    if(m_formatList != null && m_formatList.size() > 0)
            {
		    	Option option = null;
                Iterator iterator = m_formatList.keySet().iterator();
                boolean first = true;
                while(iterator.hasNext())
                {
                    String formatId = (String)iterator.next();
                    option = new Option();
                    option.setValue(formatId);
                    option.setLabel(formatId);
                    m_formatDropDownList.addOption(option);
                    if(true == first)
                    {
                        m_selectedFormatName = (String)((HashMap)m_formatList.get(formatId)).get(HelperClass.FORMAT_NAME);
		    	    }
		    	    first = false;
		    	}
		    }
	    }
	    else if(m_typeOfAssignment.equals(MIME_TYPE_ASSIGNMENT))
	    {
			m_existingMimeTypeLabel.setLabel(m_strExistingMimeType);

			m_mimeTypeDropDownList.setMutable(true);
			m_mimeTypeDropDownList.clearOptions();

			if(m_mimeTypeList != null && m_mimeTypeList.size() > 0)
			{
		    	Option option = null;
                Iterator iterator = m_mimeTypeList.keySet().iterator();
                boolean first = true;
                while(iterator.hasNext())
                {
                    String mimeType = (String)iterator.next();
                    option = new Option();
                    option.setValue(mimeType);
                    option.setLabel(mimeType);
                    m_mimeTypeDropDownList.addOption(option);
                    if(true == first)
                    {
                        m_strSelectedMimeType = mimeType;
		    	    }
		    	    first = false;
		    	}
			}
		}
		else
		{
    		HelperClass.porticoOutput(1, "Error in FormatAssignment-displayControlsBasedOnTypeOfAssignment()-unsupported Assignment type m_typeOfAssignment="+m_typeOfAssignment);
		}
	}

	public void onSelectTypeOfAssignment(Control control,ArgumentList args)
	{
       HelperClass.porticoOutput(0, "FormatAssignment - onSelectTypeOfAssignment() entered");
       m_typeOfAssignment = m_typeOfAssignmentDropDownList.getValue();
       HelperClass.porticoOutput(0, "FormatAssignment - onSelectTypeOfAssignment() m_typeOfAssignment="+m_typeOfAssignment);
       displayControlsBasedOnTypeOfAssignment();
	}

    public void onSelectFormat(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "FormatAssignment - onSelectFormat() entered");
       String selectedFormatId = m_formatDropDownList.getValue();
       m_selectedFormatName = (String)((HashMap)m_formatList.get(selectedFormatId)).get(HelperClass.FORMAT_NAME);// (String)m_formatList.get(selectedFormatId);
    }

    public void onSelectMimeType(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "FormatAssignment - onSelectMimeType() entered");
       m_strSelectedMimeType = m_mimeTypeDropDownList.getValue();
       HelperClass.porticoOutput(0, "FormatAssignment - onSelectMimeType() m_strSelectedMimeType="+m_strSelectedMimeType);
    }

    public String getSelectedFormatName()
    {
		return m_selectedFormatName;
	}

    public String getSelectedMimeType()
    {
		return m_strSelectedMimeType;
	}

    public String getSelectedAssignmentType()
    {
		return m_typeOfAssignment;
	}

    public boolean onCommitChanges()
    {
		boolean isSuccessful = validateUserInput();

        if(isSuccessful == true)
        {
	    	try
	    	{
				if(m_typeOfAssignment.equals(FORMAT_ASSIGNMENT))
				{
				    String newFormatId = m_formatDropDownList.getValue();
                    HelperClass.porticoOutput(0, "FormatAssignment - onCommitChanges() m_typeOfAssignment="+m_typeOfAssignment+",selectedFormatId="+m_formatDropDownList.getValue());
                    HelperClass.porticoOutput(0, "FormatAssignment - onCommitChanges() m_typeOfAssignment="+m_typeOfAssignment+",selectedFormatName="+m_selectedFormatName);
	    		    isSuccessful = QcHelperClass.postProcessingForFormatAssignment(getDfSession(), // IDfSession
	    		                                                                   getFolderObjectId(), // batchObjectId
	    		                                                                   m_strObjectId, // suStateId
	    		                                                                   m_strExistingFormatId, // existingFormatId
	    		                                                                   m_strExistingFormatName, // existingFormatName
	    		                                                                   m_strExistingMimeType, // (String)((HashMap)m_formatList.get(m_strExistingFormatId)).get(HelperClass.MIME_TYPE), // existingMimeType
	    		                                                                   newFormatId, // newFormatId
	    		                                                                   m_selectedFormatName, // newFormatName
	    		                                                                   (String)((HashMap)m_formatList.get(newFormatId)).get(HelperClass.MIME_TYPE), // newMimeType
	    		                                                                   m_reasonText.getValue().trim(),
	    		                                                                   m_strUserMessageObjectId);
			    }
			    else if(m_typeOfAssignment.equals(MIME_TYPE_ASSIGNMENT))
			    {
                    HelperClass.porticoOutput(0, "FormatAssignment - onCommitChanges() m_typeOfAssignment="+m_typeOfAssignment+",m_strSelectedMimeType="+m_strSelectedMimeType);
	    		    isSuccessful = QcHelperClass.postProcessingForMimeTypeAssignment(getDfSession(), // IDfSession
	    		                                                                   getFolderObjectId(), // batchObjectId
	    		                                                                   m_strObjectId, // suStateId
	    		                                                                   m_strExistingMimeType,
	    		                                                                   m_strSelectedMimeType,
	    		                                                                   m_reasonText.getValue().trim(),
	    		                                                                   m_strUserMessageObjectId);
				}
				else
				{
            		HelperClass.porticoOutput(1, "Error in FormatAssignment-onCommitChanges()-unsupported Assignment type m_typeOfAssignment="+m_typeOfAssignment);
				}
	        }
	        catch(Exception e)
	        {
				isSuccessful = false;
	    		HelperClass.porticoOutput("Exception in FormatAssignment-"+e.toString());
	    		e.printStackTrace();
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

        if(m_typeOfAssignment == null || m_typeOfAssignment.equals(""))
        {
			isValid = false;
            setReturnError("MSG_ASSIGNMENT_TYPE_NOT_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ASSIGNMENT_TYPE_NOT_SELECTED", null);
		}
		else if(m_typeOfAssignment.equals(FORMAT_ASSIGNMENT))
		{
    		String selectedFormat = m_formatDropDownList.getValue(); // .trim() removed, to faithfully get/put whatever is passed
		    if(selectedFormat == null || selectedFormat.equals(""))
		    {
		    	isValid = false;
                setReturnError("MSG_FORMAT_NOT_SELECTED", null, null);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_FORMAT_NOT_SELECTED", null);
		    }
	    }
	    else if(m_typeOfAssignment.equals(MIME_TYPE_ASSIGNMENT))
	    {
    		String selectedMimeType = m_mimeTypeDropDownList.getValue(); // .trim() removed, to faithfully get/put whatever is passed
		    if(selectedMimeType == null || selectedMimeType.equals(""))
		    {
		    	isValid = false;
                setReturnError("MSG_MIME_TYPE_NOT_SELECTED", null, null);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MIME_TYPE_NOT_SELECTED", null);
		    }
		}
		else
		{
			HelperClass.porticoOutput(1, "Error in FormatAssignment-validateUserInput()-unsupported Assignment type m_typeOfAssignment="+m_typeOfAssignment);
	    	isValid = false;
            setReturnError("MSG_UNSUPPORTED_ASSIGNMENT_TYPE_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_UNSUPPORTED_ASSIGNMENT_TYPE_SELECTED", null);
		}
		if(true == isValid)
		{
    		String reason = m_reasonText.getValue().trim();
		    if(null == reason || reason.equals(""))
		    {
		    	isValid = false;
                setReturnError("MSG_ASSIGNMENT_REASON_NOT_ENTERED", null, null);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ASSIGNMENT_REASON_NOT_ENTERED", null);
		    }
		}
/*
		else if(selectedFormat.equals(m_strExistingFormatId))
		{
			// CONPREP-2245, in case a same format is assigned we would allow
			//               Any time a 'FormatAssignment' is called it is the final Call
			//               we take it as authentic and move forward.

			isValid = false;
            setReturnError("MSG_FORMAT_SELECT_DIFFERENT_FORMAT", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FORMAT_SELECT_DIFFERENT_FORMAT", null);
		}
*/

		return isValid;
	}

    // msgText is not used currently
	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError("MSG_FORMAT_MIME_TYPE_ASSIGNMENT_SUCCESS", null, null);
			MessageService.addMessage(this, "MSG_FORMAT_MIME_TYPE_ASSIGNMENT_SUCCESS");
		}
		else
		{
			setReturnError("MSG_FORMAT_MIME_TYPE_ASSIGNMENT_FAILED", null, null);
			ErrorMessageService.getService().setNonFatalError(this, "MSG_FORMAT_MIME_TYPE_ASSIGNMENT_FAILED", null);
		}
	}

	public String getFolderObjectId()
	{
		return m_strBatchFolderId;
	}

    // Common controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
	private Label m_profileNameLabel;
	private Label m_fileNameLabel;
	private Text m_reasonText;
	private DropDownList m_typeOfAssignmentDropDownList; // Either Format OR MimeType

    // Format Assignment controls
	private Label m_existingFormatIdLabel;
	private Label m_existingFormatNameLabel;
	private DropDownList m_formatDropDownList;
	private Label m_selectedFormatNameLabel;

    // MimeType Assignment controls
	private Label m_existingMimeTypeLabel;
	private DropDownList m_mimeTypeDropDownList;
	private Label m_selectedMimeTypeLabel;

    // Common data
    private String m_strObjectId;
    private String m_strUserMessageObjectId;
    private String m_strReEntryPoint;
    private String m_strBatchFolderId;
    private String m_strProviderName;
    private String m_strProfileName;
    private String m_fileName;
    private String m_typeOfAssignment;

    // Format Assignment data
    private TreeMap m_formatList;
    private String m_strExistingFormatId;
    private String m_strExistingFormatName;
    private String m_selectedFormatName;

    // MimeType Assignment data
    private TreeMap m_mimeTypeList;
    private String m_strExistingMimeType;
    private String m_strSelectedMimeType;

    private static String[] TYPES_OF_ASSIGNMENT = {"Format","MimeType"};
    public static String FORMAT_ASSIGNMENT = "Format";
    public static String MIME_TYPE_ASSIGNMENT = "MimeType";
}
