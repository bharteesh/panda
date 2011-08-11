
package org.portico.conprep.ui.qcaction.qctoprobres;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;


public class QcToProbRes extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public QcToProbRes()
    {
        m_batchNameLabel = null;
        m_processStatusLabel = null;
        isComponentInitialized = false;

		m_batch_id = "";
		refreshCounter = 0;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batch_id = args.get("objectId");
        HelperClass.porticoOutput("QcToProbRes-onInit()-Argument batchId=" + m_batch_id);
		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
		m_processStatusLabel = (Label)getControl("process_status", com.documentum.web.form.control.Label.class);

        initializeData();
        initializeControl();
        startProcessing();
        isComponentInitialized = true;
        // setComponentPage("waitpage");
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
        HelperClass.porticoOutput("QcToProbRes-onRender()-Start for batchId(refreshCounter)=" + m_batch_id+"("+refreshCounter+")");
		if(isComponentInitialized == true && isItemInitialized == false)
		{
			HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(isComponentInitialized,isItemInitialized)=" + m_batch_id+"("+isComponentInitialized+","+isItemInitialized +")");
		    exitThisComponent(false, NLS_PROCESS_FAILED);
		}
        if(isItemInitialized == true && isItemAvailable == false)
        {
			HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(isItemInitialized,isItemAvailable)=" + m_batch_id+"("+isItemInitialized+","+isItemAvailable +")");
			if(checkForInboxMessage())
			{
				HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(checkForInboxMessage)=" + m_batch_id+"("+isItemInitialized+","+"true" +")");
				HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(start claimInboxMessage)=" + m_batch_id);
                claimInboxMessage();
				HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(end claimInboxMessage)=" + m_batch_id);
			}
		}
		if(isItemFinished == true)
		{
			HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(isItemFinished)=" + m_batch_id+"("+isItemFinished+")");
			setControlFlag(getString(NLS_PROCESSED));
			exitThisComponent(true, NLS_PROCESSED);
		}
		else if(refreshCounter >= MAX_REFRESH_COUNT)
		{
			HelperClass.porticoOutput("QcToProbRes-onRender()-batchId(refreshCounter,MAX_REFRESH_COUNT)=" + m_batch_id+"("+refreshCounter+","+MAX_REFRESH_COUNT +")");
			setControlFlag(getString(NLS_PROCESS_NOT_COMPLETE));
		    exitThisComponent(true, NLS_PROCESS_NOT_COMPLETE);
		}
		refreshCounter += 1;
        HelperClass.porticoOutput("QcToProbRes-onRender()-End for batchId=" + m_batch_id);
    }

    public void initializeData()
    {
	}

    public void initializeControl()
    {
        m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_batch_id, DBHelperClass.BATCH_TYPE));
        setControlFlag(getString(NLS_PROCESSING));
	}

	public void setControlFlag(String text)
	{
		m_processStatusLabel.setLabel(text);
	}

    public void startProcessing()
    {
		try
		{
			isItemInitialized = QcHelperClass.postProcessingForQcToProbRes(getDfSession(), m_batch_id);
			HelperClass.porticoOutput("QcToProbRes component-postProcessingForQcToProbRes-isItemInitialized="+isItemInitialized);
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput("Exception in QcToProbRes component="+e.getMessage());
		}
		finally
		{
		}
	}

	public boolean checkForInboxMessage()
	{
		isItemAvailable = QcHelperClass.canUserClaimBatch(getDfSession(), m_batch_id);
		HelperClass.porticoOutput("QcToProbRes component-checkForInboxMessage-isItemAvailable="+isItemAvailable);
		return isItemAvailable;
	}

	public boolean claimInboxMessage()
	{
		isItemFinished = QcHelperClass.postProcessingForWaitQcToProbRes(getDfSession(), m_batch_id);
		HelperClass.porticoOutput("QcToProbRes component-claimInboxMessage-isItemFinished="+isItemFinished);
		if(isItemInitialized == false)
		{
		    exitThisComponent(false, NLS_PROCESS_FAILED);
		}

		return isItemFinished;
	}

	public void exitThisComponent(boolean status, String nlsText)
	{
		HelperClass.porticoOutput("QcToProbRes component-exitThisComponent");
		callErrorMessageService(status, nlsText);
		setComponentReturn();
	}

	public void callErrorMessageService(boolean status, String msgText)
	{
		if(status)
		{
			setReturnError(msgText, null, null);
			MessageService.addMessage(this, msgText);
		}
		else
		{
			setReturnError(msgText, null, null);
			ErrorMessageService.getService().setNonFatalError(this, msgText, null);
		}
	}


    private Label m_batchNameLabel;
    private Label m_processStatusLabel;


    private String m_batch_id;
    private boolean isItemInitialized = false;
    private boolean isItemAvailable = false;
    private boolean isItemFinished = false;
    private boolean isComponentInitialized = false;
    private int refreshCounter;

    private static final int MAX_REFRESH_COUNT = 5;
    private static final String NLS_PROCESSING = "MSG_QCTOPROBRES_PROCESSING";
    private static final String NLS_PROCESSED = "MSG_QCTOPROBRES_PROCESSED";
    private static final String NLS_PROCESS_NOT_COMPLETE = "MSG_QCTOPROBRES_PROCESS_NOT_COMPLETE";
    private static final String NLS_PROCESS_FAILED = "MSG_QCTOPROBRES_FAILED";
}
