
package org.portico.conprep.ui.qcaction.usermessage.add;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.CallAddUserMessageHandler;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ProcessViewResultItem;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.databound.DataListBox;
import com.documentum.web.formext.component.Component;


public class AddUserMessage extends Component implements IReturnListener
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public AddUserMessage()
    {
		// Control
        m_batchNameLabel = null;
	    m_cuStateLabel = null;
	    // m_severityLabel = null;
	    m_cuStateDataListBox = null;
	    m_userMessageCodeDataListBox = null;
	    m_contextObjectDataListBox = null;

        // Data
        m_strBatchId = "";
        m_strCuStateId = "";
		m_strUserMessageCode = "";
		m_strUserMessageDesc = "";
		m_strUserMessageText = "";
		m_strContextObject = "";

        m_cuStateList = null;
        m_userMessageCodeList = null;
        // m_possibleActionList = null;
        m_contextObjectList = null;

        m_callAddUserMessageHandler = null;
        m_userMessageCount = 0;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strBatchId = argumentlist.get("objectId");
        m_strCuStateId = argumentlist.get("cuStateId");

        HelperClass.porticoOutput(0, "AddUserMessage-onInit()-Argument m_strBatchId,m_strCuStateId=" + m_strBatchId + ","+m_strCuStateId);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
		m_cuStateLabel = (Label)getControl("custate_name", com.documentum.web.form.control.Label.class);
		// m_severityLabel = (Label)getControl("severity", com.documentum.web.form.control.Label.class);
		m_ResultLabel = (Label)getControl("result", com.documentum.web.form.control.Label.class);
        m_cuStateDataListBox = (DataListBox)getControl("datalistbox_custate", com.documentum.web.form.control.databound.DataListBox.class);
        m_userMessageCodeDataListBox = (DataListBox)getControl("datalistbox_user_message_code", com.documentum.web.form.control.databound.DataListBox.class);
        m_contextObjectDataListBox = (DataListBox)getControl("datalistbox_context_object", com.documentum.web.form.control.databound.DataListBox.class);

    	m_cuStateDataListBox.setMutable(true);
    	m_userMessageCodeDataListBox.setMutable(true);
    	m_contextObjectDataListBox.setMutable(true);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void initializeCommonData()
    {
		try
		{
			m_callAddUserMessageHandler = new CallAddUserMessageHandler(getDfSession(), m_strBatchId);
			m_callAddUserMessageHandler.processHandler();
			if(m_strCuStateId == null)
			{
				m_cuStateList = m_callAddUserMessageHandler.getSortedArticles();
			}
			m_userMessageCodeList = m_callAddUserMessageHandler.getUserMessageCodeList();
	    }
	    catch(Exception e)
	    {
            HelperClass.porticoOutput(0, "AddUserMessage-initializeCommonData()-Exception=" + e.getMessage());
		}
		finally
		{
		}
	}

    public void initializeCommonControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_strBatchId, DBHelperClass.BATCH_TYPE));
		populateCuStateInfo();
		populateUserMessageCodeInfo();
   		populateOtherUserMessageInfo();
   		populateContextObject();
	}

	public void	populateCuStateInfo()
	{
		if(m_strCuStateId == null)
		{
			m_cuStateLabel.setVisible(false);
			m_cuStateDataListBox.setMutable(true);
			m_cuStateDataListBox.clearOptions();
    		m_cuStateDataListBox.setValue(""); // ??

			if(m_cuStateList != null && m_cuStateList.size() > 0)
			{
    			Option option = null;
                boolean isFirst = true;

                Collection col = m_cuStateList.values();
                Iterator iterator = col.iterator();

                while(iterator.hasNext())
                {
					ProcessViewResultItem item = (ProcessViewResultItem)iterator.next();
    		    	String cuStateId = item.getThisKey();
    		    	String cuStateDisplayLabel = item.getThisDisplayToken();
                    option = new Option();
                    option.setValue(cuStateId);
                    option.setLabel(cuStateDisplayLabel);
    	    	    m_cuStateDataListBox.addOption(option);
                    HelperClass.porticoOutput(0, "AddUserMessage - initializeCommonControls()-cuStateDisplayLabel="+cuStateDisplayLabel);

    	    	    if(isFirst == true)
    	    	    {
				    	m_strCuStateId = cuStateId;
				    	m_cuStateDataListBox.setValue(cuStateId);
				    	isFirst = false;
				    }
    	        }
			}
		}
		else
		{
			m_cuStateDataListBox.setVisible(false);
            String cuStateDisplayLabel = "";
            TreeMap sortedItem = m_callAddUserMessageHandler.getSortedContextObjects(DBHelperClass.CU_TYPE, m_strCuStateId);
            Collection col = sortedItem.values();
            Iterator iterator = col.iterator();
            while(iterator.hasNext())
            {
                ProcessViewResultItem item = (ProcessViewResultItem)iterator.next();
	        	cuStateDisplayLabel = item.getThisDisplayToken();
	        	break;
			}
			m_cuStateLabel.setLabel(cuStateDisplayLabel);
		}
	}

	public void	populateUserMessageCodeInfo()
	{
		m_userMessageCodeDataListBox.setMutable(true);
		m_userMessageCodeDataListBox.clearOptions();
    	m_userMessageCodeDataListBox.setValue(""); // ??

		if(m_userMessageCodeList != null && m_userMessageCodeList.size() > 0)
		{
    		Option option = null;
            boolean isFirst = true;

            for (int indx=0; indx < m_userMessageCodeList.size(); indx++)
            {
				ValuePair valuePair = (ValuePair)m_userMessageCodeList.get(indx);
    		   	String userMessageCode = valuePair.getKey();
                option = new Option();
                option.setValue(userMessageCode);
                option.setLabel(userMessageCode);
    	        m_userMessageCodeDataListBox.addOption(option);
                HelperClass.porticoOutput(0, "AddUserMessage - populateUserMessageInfo()-userMessageCode="+userMessageCode);

    	        if(isFirst == true)
    	        {
			    	m_strUserMessageCode = userMessageCode;
			    	m_userMessageCodeDataListBox.setValue(userMessageCode);
			    	isFirst = false;
			    }
    	    }
		}
	}

	public void populateOtherUserMessageInfo()
	{
		// m_severityLabel.setLabel("");
		m_strUserMessageDesc = "";
		m_strUserMessageText = "";
		if(m_strUserMessageCode != null && !m_strUserMessageCode.equals(""))
		{
			// m_possibleActionList = m_callAddUserMessageHandler.getPossibleActionList(m_strUserMessageCode);
		    // m_severityLabel.setLabel(m_callAddUserMessageHandler.getSeverity(m_strUserMessageCode));
		    m_strUserMessageText = m_callAddUserMessageHandler.getDescription(m_strUserMessageCode);

    		m_strUserMessageDesc = getString(MSG_SEVERITY) + " " + m_callAddUserMessageHandler.getSeverity(m_strUserMessageCode) + "\n" +"\n";
    		m_strUserMessageDesc += getString(MSG_TEXT) + " " + m_strUserMessageText + "\n" + "\n";
    		// m_strUserMessageDesc += getString(MSG_POSSIBLE_ACTIONS) + "\n";

/*
		    if(m_possibleActionList != null && m_possibleActionList.size() > 0)
		    {
                for (int indx=0; indx < m_possibleActionList.size(); indx++)
                {
					ValuePair valuePair = (ValuePair)m_possibleActionList.get(indx);
					String action = valuePair.getKey();
					m_strUserMessageDesc += action + "\n";
                    HelperClass.porticoOutput(0, "AddUserMessage - populateOtherUserMessageInfo()-action="+action);
				}
			}
*/
	    }
	}

	public String getUserMessageDescription()
	{
		return m_strUserMessageDesc;
	}

/*
    public void populatePossibleActions()
    {
    	m_possibleActionList = null;

		if(m_strUserMessageCode != null && !m_strUserMessageCode.equals(""))
		{
			m_possibleActionList = m_callAddUserMessageHandler.getPossibleActionList(m_strUserMessageCode);

		    if(m_possibleActionList != null && m_possibleActionList.size() > 0)
		    {
                for (int indx=0; indx < m_possibleActionList.size(); indx++)
                {
    				ValuePair valuePair = (ValuePair)m_possibleActionList.get(indx);
        		   	String action = valuePair.getKey();
                    HelperClass.porticoOutput(0, "AddUserMessage - populatePossibleActions()-action="+action);
    	        }
		    }
	    }
	}
*/

	public void populateContextObject()
	{
		m_contextObjectDataListBox.setMutable(true);
		m_contextObjectDataListBox.clearOptions();
    	m_contextObjectDataListBox.setValue(""); // ??

    	m_strUserMessageCodeContext = "";

		if(m_strUserMessageCode != null && !m_strUserMessageCode.equals(""))
		{
			m_strUserMessageCodeContext = m_callAddUserMessageHandler.getUserMessageCodeContext(m_strUserMessageCode);

		    if(m_strUserMessageCodeContext != null && !m_strUserMessageCodeContext.equals(""))
		    {
				m_contextObjectList = m_callAddUserMessageHandler.getSortedContextObjects(m_strUserMessageCodeContext, m_strCuStateId);
				if(m_contextObjectList != null && m_contextObjectList.size() > 0)
				{
    	    	    Option option = null;
                    Collection col = m_contextObjectList.values();

                    if(m_strUserMessageCodeContext.equals(DBHelperClass.SU_TYPE))
                    {
                        Iterator leadSourceMdIterator = col.iterator();
                        while(leadSourceMdIterator.hasNext())
                        {
    				    	ProcessViewResultItem item = (ProcessViewResultItem)leadSourceMdIterator.next();
        		          	String objId = item.getThisKey();
        		        	String displayLabel = item.getThisDisplayToken();
                            HelperClass.porticoOutput(0, "AddUserMessage - populateContextObject()-checking for leadMd-objId,displayLabel="+objId+","+displayLabel);
                            // Lead MD - Source
    				    	if(item.getIsErroredItem() == true)
    				    	{
                                option = new Option();
                                option.setValue(objId);
                                String leadDisplayLabel = LEADMARKER + displayLabel;
                                option.setLabel(leadDisplayLabel);
        	                    m_contextObjectDataListBox.addOption(option);
                                HelperClass.porticoOutput(0, "AddUserMessage - populateContextObject()-leadMdSource(pick it)-objId,displayLabel="+objId+","+displayLabel);
						    }
    	                }
					}

                    Iterator iterator = col.iterator();
                    while(iterator.hasNext())
                    {
    					ProcessViewResultItem item = (ProcessViewResultItem)iterator.next();
    					// Non-lead MD and Other file(s)
    					if(item.getIsErroredItem() == false)
    					{
            		      	String objId = item.getThisKey();
            		    	String displayLabel = item.getThisDisplayToken();
                            option = new Option();
                            option.setValue(objId);
                            option.setLabel(displayLabel);
        	                m_contextObjectDataListBox.addOption(option);
                            HelperClass.porticoOutput(0, "AddUserMessage - populateContextObject()-NonLeadMD-objId,displayLabel="+objId+","+displayLabel);
					    }
    	            }
			    }
		    }
	    }
	}

    public void onSelectCuState(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "AddUserMessage - onSelectCuState() entered");
       m_strCuStateId = m_cuStateDataListBox.getValue().trim();
       HelperClass.porticoOutput(0, "AddUserMessage - onSelectCuState() entered - value="+m_strCuStateId);
       // Clean up the internal value set on the DataListBox, otherwise if it retains the previous value errorneously
       populateContextObject(); // This will initially setValue("") then pull the info and populate it.
    }

    public void onSelectUserMessageCode(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "AddUserMessage - onSelectUserMessageCode() entered");
       m_strUserMessageCode = m_userMessageCodeDataListBox.getValue().trim();
       HelperClass.porticoOutput(0, "AddUserMessage - onSelectUserMessageCode() entered - value="+m_strUserMessageCode);
       // Clean up the internal value set on the DataListBox, otherwise if it retains the previous value errorneously
       populateOtherUserMessageInfo(); // This will initially setValue("") then pull the info and populate it.
       // populateContextObject(); // This will initially setValue("") then pull the info and populate it.
    }

    public boolean validateUserInput()
    {
        boolean isValid = true;
        m_strContextObject = m_contextObjectDataListBox.getValue();

		if(m_strCuStateId == null || m_strCuStateId.equals(""))
		{
			isValid = false;
			callShowResult(isValid, getString("MSG_CU_STATE_EMPTY"), "");
		}
		if(m_strUserMessageCode == null || m_strUserMessageCode.equals(""))
		{
			isValid = false;
			callShowResult(isValid, getString("MSG_USER_MESSAGE_CODE_EMPTY"), "");
		}
		if(m_strContextObject == null || m_strContextObject.equals(""))
		{
			isValid = false;
			callShowResult(isValid, getString("MSG_CONTEXT_OBJECT_EMPTY"), "");
		}

        HelperClass.porticoOutput(0, "AddUserMessage - validateUserInput()-m_strCuStateId="+m_strCuStateId+"|");
        HelperClass.porticoOutput(0, "AddUserMessage - validateUserInput()-m_strUserMessageCode="+m_strUserMessageCode+"|");
        HelperClass.porticoOutput(0, "AddUserMessage - validateUserInput()-m_strContextObject="+m_strContextObject+"|");

		return isValid;
	}


    public void onCreateUserMessage(Control control,ArgumentList args)
    {
		control.setEnabled(false);
		// Set Result status to processing...
		callShowResult(false, "Processing....Please wait....", "");

		boolean isSuccessful = validateUserInput();

        if(isSuccessful == true)
        {
    		isSuccessful = QcHelperClass.postProcessingForUserMessagesCreation(getDfSession(), m_strBatchId, m_strCuStateId, m_strContextObject, m_strUserMessageCode, m_strUserMessageText);
    		String text = "";
            if(isSuccessful == true)
            {
				m_userMessageCount += 1;
				text = ""+m_userMessageCount + " " + getString("MSG_USER_MESSAGE_CREATION_SUCCESS");
     	      	callShowResult(isSuccessful, text, "");
		    }
		    else
		    {
				text = getString("MSG_USER_MESSAGE_CREATION_FAILED");
		    	callShowResult(isSuccessful, text, "");
	        }
	    }
		control.setEnabled(true);
	}

	public void onJumpToNestedQcToProbRes(Control control,ArgumentList args)
	{
		HelperClass.porticoOutput(0, "AddUserMessage - onJumpToNestedQcToProbRes()-Started");
		HelperClass.porticoOutput(0, "AddUserMessage - setComponentReturn(back to caller) for m_strBatchId="+m_strBatchId);
		HelperClass.porticoOutput(0, "AddUserMessage - Manual call to QcToProbRes() is REQUIRED for m_strBatchId="+m_strBatchId);
	    setComponentReturn();
/*
		if(m_userMessageCount > 0)
		{
			// Reset so that in case the page does not return automatically, we do not kick off
			// 'qctoprobres' again.
			m_userMessageCount = 0;
		    ArgumentList newArgs = new ArgumentList();
		    newArgs.add("objectId", m_strBatchId);
		    newArgs.add("type", DBHelperClass.BATCH_TYPE);

		    HelperClass.porticoOutput(0, "AddUserMessage - Message added-setComponentNested(qctoprobres)-Called for m_strBatchId="+m_strBatchId);
            setComponentNested("qctoprobres",
		                    newArgs,
		                    getContext(),
                            this);
		    HelperClass.porticoOutput(0, "AddUserMessage - setComponentNested()-Call Ended for m_strBatchId="+m_strBatchId);
	    }
	    else
	    {
			HelperClass.porticoOutput(0, "AddUserMessage - No Mesage added-setComponentReturn(back to caller) for m_strBatchId="+m_strBatchId);
		    setComponentReturn();
		}
*/
    }

/*
	public void onCancel(Control control,ArgumentList args)
	{
		HelperClass.porticoOutput(0, "AddUserMessage - setComponentReturn()-onCancel for m_strBatchId="+m_strBatchId);
		setComponentReturn();
	}
*/

    // IReturnListener handler
	public void onReturn(Form form, java.util.Map map)
	{
		HelperClass.porticoOutput(0, "AddUserMessage - IReturnListener-onReturn()-setComponentReturn(back to caller) for m_strBatchId="+m_strBatchId);
		setComponentReturn();
	}

	public void callShowResult(boolean status, String text, String addlnText)
	{
        m_ResultLabel.setLabel(text + " " + addlnText);
	}

// End of new stuff

// Controls
    private Label m_batchNameLabel;
	private Label m_cuStateLabel; // Article
	// private Label m_severityLabel; // Severity of message code
	private Label m_ResultLabel;
	private DataListBox m_cuStateDataListBox;
	private DataListBox m_userMessageCodeDataListBox;
	private DataListBox m_contextObjectDataListBox;// Lead source if any, appears on top marked with *

// Data
    private String m_strBatchId;
    private String m_strCuStateId;
    private String m_strUserMessageCode;
    private String m_strUserMessageDesc;
    private String m_strUserMessageText;// Text from messagemapping for the relevant message code
    private String m_strContextObject;
    private String m_strUserMessageCodeContext;

    private TreeMap m_cuStateList; // contains sorted ProcessViewResultItem
    private ArrayList m_userMessageCodeList; // ValuePair - code,desc
    // private ArrayList m_possibleActionList; // Simple
    private TreeMap m_contextObjectList; // contains sorted ProcessViewResultItem, with mark on lead source if any

    private CallAddUserMessageHandler m_callAddUserMessageHandler;
    private int m_userMessageCount;

    private static final String LEADMARKER = " (*) ";
    private static final String MSG_SEVERITY = "MSG_SEVERITY";
    private static final String MSG_TEXT = "MSG_TEXT";
    private static final String MSG_POSSIBLE_ACTIONS = "MSG_POSSIBLE_ACTIONS";
}
