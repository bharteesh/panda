
package org.portico.conprep.ui.qcaction.delegatetask;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.portico.conprep.ui.app.AppSessionContext;
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
import com.documentum.web.form.control.TextArea;
import com.documentum.web.form.control.databound.DataListBox;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class Delegate extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public Delegate()
    {
		// Control
        m_batchNameLabel = null;
        m_claimerNameLabel = null;
        m_roleDropDownList = null;
        m_userDataListBox = null;
        m_noteTextArea = null;

        // Data
        m_strObjectId = "";
        m_strCurrentClaimer = "";
		m_strDelegatedUser = "";
		m_strNote = "";
		m_strNoteHeader = "";

        m_roleUserHashTable = null;
        m_roleList = null;

        isPostProcessingSuccessful = false;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId");

        HelperClass.porticoOutput(0, "Delegate-onInit()-Argument m_strObjectId=" + m_strObjectId);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
		m_claimerNameLabel = (Label)getControl("claimer_name", com.documentum.web.form.control.Label.class);
        m_roleDropDownList = (DropDownList)getControl("dropdownlist_role", com.documentum.web.form.control.DropDownList.class);
        m_userDataListBox = (DataListBox)getControl("datalistbox_user", com.documentum.web.form.control.databound.DataListBox.class);
        m_noteTextArea = (TextArea)getControl("textarea_note", com.documentum.web.form.control.TextArea.class);

    	m_roleDropDownList.setMutable(true);
    	m_userDataListBox.setMutable(true);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){
        HelperClass.porticoOutput(0, "Delegate - onRender -1 ");
        super.onRender(); //always call the superclass' onRender()
        if(isPostProcessingSuccessful == true)
        {
			callErrorMessageService(isPostProcessingSuccessful, "MSG_DELEGATE_SUCCESS", null);
            HelperClass.porticoOutput(0, "Delegate - onRender -2 ");
		}
		HelperClass.porticoOutput(0, "Delegate - onRender -3 ");
    }

    public void initializeCommonData()
    {
		try
		{
	    	m_strCurrentClaimer = getDfSession().getLoginUserName();
	    	m_strNoteHeader = getString(NLS_NOTE_HEADER);
	    }
	    catch(Exception e)
	    {
            HelperClass.porticoOutput(0, "Delegate-initializeCommonData()-Exception=" + e.getMessage());
		}
		finally
		{
	    	// This could later be got from appSessionContext
	    	m_roleList = AppSessionContext.getUserRolesUI(); // QcHelperClass.getAvailableUserRoles(getDfSession());
	    	m_roleUserHashTable = AppSessionContext.getUsersAndRolesUI(); // QcHelperClass.getRolesAndUsers(getDfSession());
		}
	}

    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE));
		m_claimerNameLabel.setLabel(m_strCurrentClaimer);
		m_roleDropDownList.setValue("");
		m_userDataListBox.setValue("");


    	if(m_roleList != null && m_roleList.size() > 0 && m_roleUserHashTable != null && m_roleUserHashTable.size() > 0)
		{
			m_userDataListBox.setMutable(true);
			m_userDataListBox.clearOptions();

    		m_roleDropDownList.setMutable(true);
    		m_roleDropDownList.clearOptions();

			Option option = null;
            boolean isFirst = true;
	    	String roleName = "";
	    	String roleDisplayName = "";
	    	ValuePair tPorticoValuePair = null;


            for (int indx=0; indx < m_roleList.size(); indx++)
            {
				tPorticoValuePair = (ValuePair)m_roleList.get(indx);
		    	roleName = tPorticoValuePair.getKey();
		    	roleDisplayName = tPorticoValuePair.getValue();
                option = new Option();
                option.setValue(roleName);
                option.setLabel(roleDisplayName);
    	    	m_roleDropDownList.addOption(option);
                HelperClass.porticoOutput(0, "Delegate - initializeCommonControls()-First-roleName,roleDisplayName="+roleName+","+roleDisplayName);

    	    	if(isFirst == true)
    	    	{
					isFirst = false;
					populateUserDataListBox(roleName);
				}
	        }

/*
            for (Enumeration enumKeys = m_roleUserHashTable.keys(); enumKeys.hasMoreElements() ;)
            {
		    	roleName = (String)enumKeys.nextElement();
                option = new Option();
                option.setValue(roleName);
                option.setLabel(roleName);
    	    	m_roleDropDownList.addOption(option);
                HelperClass.porticoOutput(0, "Delegate - initializeCommonControls()-First-roleName="+roleName);

    	    	if(isFirst == true)
    	    	{
					isFirst = false;
					populateUserDataListBox(roleName);
				}
	        }
*/
		}
	}

	private void populateUserDataListBox(String roleName)
	{
    	if(m_roleUserHashTable != null && m_roleUserHashTable.size() > 0)
		{
			m_userDataListBox.setMutable(true);
			m_userDataListBox.clearOptions();

            // HelperClass.porticoOutput(0, "Delegate - populateUserDataListBox()-m_userDataListBox.clearOptions-called");

            Option option = null;
            option = new Option();
            option.setValue(roleName);
            option.setLabel("All");
    	    m_userDataListBox.addOption(option);

		   	TreeSet userList = (TreeSet)m_roleUserHashTable.get(roleName);
			if(userList != null && userList.size() > 0)
			{
                Iterator iterate = userList.iterator();
                while(iterate.hasNext())
                {
			        String userName = (String)iterate.next();
                    option = new Option();
                    option.setValue(userName);
                    option.setLabel(userName);
    	    	    m_userDataListBox.addOption(option);
                    // HelperClass.porticoOutput(0, "Delegate - populateUserDataListBox()-m_userDataListBox.addOption-called");
			    }
			}
		}
	}

    public void onSelectRole(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "Delegate - onSelectRole() entered");
       String selectedRole = m_roleDropDownList.getValue();
       HelperClass.porticoOutput(0, "Delegate - onSelectRole() entered - value="+selectedRole);
       // Clean up the internal value set on the DataListBox, otherwise if it retains the previous value of user selected
       // for a different role, it is errorneous
       m_userDataListBox.setValue("");
       populateUserDataListBox(selectedRole);
    }

    public boolean onCommitChanges()
    {
		boolean isSuccessful = validateUserInput();

        if(isSuccessful == true)
        {
    		isPostProcessingSuccessful = QcHelperClass.postProcessingForDelegate(getDfSession(), m_strObjectId, m_strCurrentClaimer, m_strDelegatedUser, m_strNote, m_strNoteHeader);
            if(isPostProcessingSuccessful == true)
            {
     	      	callErrorMessageService(isPostProcessingSuccessful, "MSG_DELEGATE_SUCCESS", null);
		    }
		    else
		    {
		    	callErrorMessageService(isPostProcessingSuccessful, "MSG_DELEGATE_FAILED", null);
	        }
	        isSuccessful = isPostProcessingSuccessful;
	    }

		return isSuccessful;
	}

	public boolean validateUserInput()
	{
		boolean isValid = true;
		m_strDelegatedUser = m_userDataListBox.getValue();
		m_strNote = m_noteTextArea.getValue();

        // HelperClass.porticoOutput(0, "Delegate - validateUserInput()-m_userDataListBox.getValue()="+m_userDataListBox.getValue()+"|");
        HelperClass.porticoOutput(0, "Delegate - validateUserInput()-m_strDelegatedUser="+m_strDelegatedUser+"|");
        HelperClass.porticoOutput(0, "Delegate - validateUserInput()-m_strNote="+m_strNote+"|");


		if(m_strNote == null || m_strNote.equals(""))
		{
			isValid = false;
			callErrorMessageService(isValid, "MSG_DELEGATE_NOTE_EMPTY", null);
		}
		if(m_strDelegatedUser == null || m_strDelegatedUser.equals(""))
		{
			isValid = false;
			callErrorMessageService(isValid, "MSG_DELEGATE_USER_NOT_SELECTED", null);
		}
		else
		{
			if(m_strDelegatedUser.equals(m_strCurrentClaimer))
			{
    			isValid = false;
    			callErrorMessageService(isValid, "MSG_CLAIMER_DELEGATE_USER_TO_BE_DIFFERENT", null);
			}
		}

		return isValid;
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

	public String getBatchFolder()
	{
		return m_strObjectId;
	}

// Controls
    private Label m_batchNameLabel;
    private Label m_claimerNameLabel;
	private DropDownList m_roleDropDownList;
	private DataListBox m_userDataListBox;
	private TextArea m_noteTextArea;

// Data
    private String m_strCurrentClaimer;
    private String m_strDelegatedUser;
    private String m_strNote;
    private String m_strNoteHeader;
	private String m_strObjectId;
	private Hashtable m_roleUserHashTable;
	private ArrayList m_roleList;
	private boolean isPostProcessingSuccessful = false;

	private static final String NLS_NOTE_HEADER = "MSG_NOTE_HEADER";
}
