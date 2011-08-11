
package org.portico.conprep.ui.qcaction.replace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.DfXMLUtils;
import com.documentum.operations.IDfImportNode;
import com.documentum.operations.IDfImportOperation;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.control.ImportApplet;
import com.documentum.web.contentxfer.control.LinkDetectorApplet;
import com.documentum.web.contentxfer.control.ServiceProgressFeedback;
import com.documentum.web.contentxfer.server.ContentTransferService;
import com.documentum.web.contentxfer.server.ContentXferServiceEvent;
import com.documentum.web.contentxfer.server.IContentXferServiceListener;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.FileBrowse;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.util.Browser;
import com.documentum.webcomponent.library.contentxfer.UploadContainer;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.documentum.webcomponent.library.messages.MessageService;


public class ReplaceContainer extends UploadContainer
    implements IContentXferServiceListener
{
    public ReplaceContainer()
    {
        m_appletImport = null;
        m_appletLinkDetector = null;
        m_progress = null;
        m_strContentTicket = null;
        m_setFiles = new TreeSet();
        m_setFiles_MacNav = null;
        m_listboxFiles = null;
        m_strNewObjectIds = null;
        m_xmlParsable = false;
        m_intXmlEndIndex = 0;

        // New stuff
		m_batchName = null;

        m_docbaseFormatNames = new LinkedList();
        m_extensionMap = new HashMap(311, 1.0F);
        m_ConfiguredDefaultFormats = new HashMap(23, 1.0F);
        m_PorticoSubmitBatchItemList = new ArrayList();
        m_FileExtnTypeList = new Hashtable();

        m_strObjectId = "";
        m_strBatchFolderId = "";
        m_strDesc = null;
        m_strSelectedSource = "";
        m_strDescValue = "";
        m_fileList = new ArrayList();
        m_sourceList = new ArrayList();
        m_msgObjectList = new ArrayList();
        m_strUserMessageObjectId = "";
        m_strReEntryPoint = "";
        m_strAdditionalErrorMessageNLS = "";
    }

    private boolean macOS9Nav()
    {
        HttpServletRequest httpservletrequest = (HttpServletRequest)getPageContext().getRequest();
        boolean flag = Browser.isMac(httpservletrequest) && Browser.isNetscape(httpservletrequest) && !Browser.isMacOSXNav(httpservletrequest);
        return flag;
    }

    public void updateControls()
    {
        super.updateControls();
    }

    private String[] putXmlFirst(String as[])
    {
        if(!macOS9Nav())
            return as;
        Vector vector = new Vector();
        Vector vector1 = new Vector();
        for(int i = 0; i < as.length; i++)
        {
            String s = as[i];
            if(s.toLowerCase().endsWith(".xml"))
                vector.addElement(s);
            else
                vector1.addElement(s);
        }

        if(vector.size() == 0)
            return as;
        String as1[] = new String[as.length];
        m_intXmlEndIndex = vector.size();
        for(int j = 0; j < m_intXmlEndIndex; j++)
            as1[j] = (String)vector.get(j);

        for(int k = 0; k < vector1.size(); k++)
            as1[m_intXmlEndIndex + k] = (String)vector1.get(k);

        return as1;
    }

    public boolean canCommitChanges()
    {
        boolean flag = super.canCommitChanges();
        if(!flag || !m_xmlParsable || !macOS9Nav())
            return flag;
        int i = getCurrentComponent();
        if(hasNextPage() && m_intXmlEndIndex > i)
            flag = false;
        return flag;
    }

    public void onInit(ArgumentList argumentlist)
    {
            HelperClass.porticoOutput(0, "ReplaceContainer - START onInit() Before argumentlist");
		    HelperClass.porticoOutput(0, "Before ReplaceContainer-onInit()-argumentlist=" + argumentlist.toString());
            super.onInit(argumentlist);
            HelperClass.porticoOutput(0, "After ReplaceContainer-onInit()-argumentlist=" + argumentlist.toString());

            ArgumentList allArgumentlist = getContainedComponentArgs();
            HelperClass.porticoOutput(0, "After ReplaceContainer-allArgumentlist()=" + allArgumentlist.toString());


            // From Menu, report
            m_strObjectId = argumentlist.get("accessionId"); // This can be only single valued == SUSTATE Id(Repair/Replace)

            // From report only
            m_strUserMessageObjectId = argumentlist.get("msgObjectId"); // List of Message object Id
// TESTING ONLY START
            // m_strUserMessageObjectId = "090152d48001b332";
// TESTING ONLY END
            m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

            HelperClass.porticoOutput(0, "ReplaceContainer - onInit ArgumentList-m_strObjectId="+ m_strObjectId);
            HelperClass.porticoOutput(0, "ReplaceContainer - onInit ArgumentList-m_strUserMessageObjectId="+ m_strUserMessageObjectId);
            HelperClass.porticoOutput(0, "ReplaceContainer - onInit ArgumentList-m_strReEntryPoint="+ m_strReEntryPoint);

            m_appletImport = (ImportApplet)getControl("importapplet", com.documentum.web.contentxfer.control.ImportApplet.class);
            m_appletLinkDetector = (LinkDetectorApplet)getControl("linkdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
            m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
            m_listboxFiles = (ListBox)getControl("filelist", com.documentum.web.form.control.ListBox.class);
            m_listboxFiles.setMutable(true);
            Panel panel = (Panel)getControl("IEButtons", com.documentum.web.form.control.Panel.class);
            Panel panel1 = (Panel)getControl("NSButtons", com.documentum.web.form.control.Panel.class);
            panel.setVisible(isBrowserIE());
            panel1.setVisible(isBrowserNetscape());

            // New stuff
            m_batchName = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
            m_providerName = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
            m_strDesc = (Text)getControl("desc", com.documentum.web.form.control.Text.class);

            // Pick relevant data info
            initializeData();
    		initializeControls();

            // Stuffs from component
            initFormatInformation();
            readConfiguredDefaultFormats();
            setReturnValue("success", "true");
    }

    public void initializeData()
    {
		// Pick batch name
        m_strBatchFolderId = HelperClass.getParentBatchFolderId(getDfSession(), m_strObjectId);

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

        // m_sourceList - // getValueAssistanceListFromDocbase
	    // m_sourceList = HelperClass.getValueAssistanceListFromDocbase(getDfSession(), DBHelperClass.RAW_UNIT_TYPE, "p_source");
        m_sourceList = HelperClass.getReplaceActionKeyMetadataOriginList();

	    // populate repair/replace file list
	    populateReplaceFileList();
	    populateMsgObjectList();
	}

    public void initializeControls()
    {
		m_batchName.setLabel(HelperClass.getObjectName(getDfSession(), getFolderObjectId(), DBHelperClass.BATCH_TYPE));
		m_providerName.setLabel(m_strProviderName);
        // radio - dynamic
        // filebrowse - dynamic
        // filebrowselabel - dynamic
	}

    public void populateReplaceFileList()
    {
        ArrayList fileObjectList = getFileObjectIds();
		if(fileObjectList != null && fileObjectList.size() > 0)
		{
			for(int indx=0; indx < fileObjectList.size(); indx++)
			{
		    	// m_fileList.add(HelperClass.getObjectName(getDfSession(), (String)fileObjectList.get(indx)));
		    	m_fileList.add(getDisplayName((String)fileObjectList.get(indx)));
		    }
		}
	}

	public ArrayList getFileList()
	{
		return m_fileList;
	}

	public ArrayList getSourceList()
	{
		return m_sourceList;
	}

    public void onOk(Control control, ArgumentList argumentlist)
    {
        validate(); // documentum validate

        // Clean up additional messages
        m_strAdditionalErrorMessageNLS = "";

        HelperClass.porticoOutput(0, "ReplaceContainer - onOk before validate()");
        HelperClass.porticoOutput(0, "ReplaceContainer - onOk after validate()");
        HelperClass.porticoOutput(0, "ReplaceContainer - onOk before getIsValid()");
        boolean flag = getIsValid();
        HelperClass.porticoOutput(0, "ReplaceContainer - onOk after getIsValid()="+flag);
        HelperClass.porticoOutput(0, "ReplaceContainer - onOk before canCommitChanges()");
        boolean flag1 = canCommitChanges();
        HelperClass.porticoOutput(0, "ReplaceContainer - onOk after canCommitChanges()="+flag1);

	    boolean isValidFiles = checkAddedFiles();
        if(isValidFiles)
        {
			boolean isValidUserInfo = checkUserInformation();

			if(isValidUserInfo == true)
			{
				if(checkAdditionalValidation() == true)
				{
                    if(flag && flag1 && onCommitChanges())
                    {
   	    	        	HelperClass.porticoOutput(0, "ReplaceContainer - onOk before importUpload()");
                        importUpload("");
  	    	        	HelperClass.porticoOutput(0, "ReplaceContainer - onOk after importUpload()");
   	    	        }
   	    	        else
   	    	        {
   	    	        	HelperClass.porticoOutput(0, "ReplaceContainer - onOk before inAutoCommit()");
                        if((!flag || !flag1) && inAutoCommit())
                        {
           	        		HelperClass.porticoOutput(0, "ReplaceContainer - onOk before stopAutoCommit()");
                            stopAutoCommit();
   	    	        	}
   	    	        	HelperClass.porticoOutput(0, "ReplaceContainer - onOk after inAutoCommit()");
   	    	        }
			    }
	        }
		}
	    else
	    {
		   	HelperClass.porticoOutput(0, "ReplaceContainer - onOk - Do nothing(No added files).................");
		}
	}

    public void onImportUploadComplete(Control control, ArgumentList argumentlist)
    {
        m_strContentTicket = argumentlist.get("contentTicket");
        m_progress.setServiceMgr(this);
        setComponentPage("serviceprogress");
    }

    public void onXMLAppListUpdated(Control control, ArgumentList argumentlist)
    {
        ReplaceComponent importcontent = (ReplaceComponent)getContainedComponent();
        importcontent.onXMLAppListUpdated(control, argumentlist);
    }

    public void onAfterLinkDetect(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("linkInstructions");
        importUpload(s);
    }

    public void startService(IContentXferServiceListener icontentxferservicelistener)
    {
        ContentTransferService contenttransferservice = new ContentTransferService();
        contenttransferservice.addContentXferServiceListener(icontentxferservicelistener);
        contenttransferservice.addContentXferServiceListener(this);
        startImport(contenttransferservice);
    }

    protected void startImport(ContentTransferService contenttransferservice)
    {
        try
        {
            contenttransferservice.importContent(m_strContentTicket, getPageContext());
        }
        catch(Exception exception)
        {
            setReturnError("MSG_ERROR_IMPORT", null, exception);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
            HelperClass.porticoOutput(1, "Exception in ReplaceContainer - startImport="+exception.getMessage());
        }
    }

    public void onContentTransferServiceComplete(Control control, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

    public void onRender()
    {
        super.onRender();
	}

    public boolean checkAddedFiles()
    {
		boolean isValidFiles = true;
		ArrayList fileList = new ArrayList();

		if(m_fileList != null && m_fileList.size() > 0)
		{
			FileBrowse fileBrowseControl = null;
			Label fileBrowseLabelControl = null;
			String fileBrowserValue = "";

			for(int indx=0; indx < m_fileList.size(); indx++)
			{
		    	fileBrowseControl = (FileBrowse)getControl(ReplaceContainer.FILEBROWSER_PREFIX+indx);
		    	fileBrowseLabelControl = (Label)getControl(ReplaceContainer.FILEBROWSERLABEL_PREFIX+indx);
		    	if(fileBrowseControl != null)
		    	{
					fileBrowserValue = fileBrowseControl.getValue();
					if(fileBrowseLabelControl != null)
					{
    					fileBrowseLabelControl.setLabel(fileBrowserValue);
					}

                    HelperClass.porticoOutput(0, "ReplaceContainer - checkAddedFiles-New filebrowser="+fileBrowserValue);

                    if(fileBrowserValue == null || fileBrowserValue.equals(""))
                    {
                        setReturnError("MSG_FILES_NOT_SELECTED", null, null);
                        ErrorMessageService.getService().setNonFatalError(this, "MSG_FILES_NOT_SELECTED", null);
                        isValidFiles = false;
                    }
					if(isValidFiles == false)
					{
						break;
					}
                    else
                    {
                   		fileList.add(fileBrowserValue);
            		}
	            }
		    }
		}

		if(isValidFiles == true && fileList != null && fileList.size() > 0)
		{
            int numberOfFiles = fileList.size();
            String as[] = new String[numberOfFiles];
            for(int ii=0; ii < numberOfFiles; ii++)
            {
  	    		as[ii] = (String)fileList.get(ii);
   	    	}
            if(macOS9Nav())
            {
                as = putXmlFirst(as);
                m_setFiles_MacNav = as;
            }
            m_setFiles.clear();
            m_setFiles.addAll(Arrays.asList(as));
            int i = m_setFiles.size();
            String s2 = ""; // getFolderObjectId(); will be populated in importUpload();
            m_PorticoSubmitBatchItemList.clear();
            ReplaceItem tItem = null;
            if(m_setFiles_MacNav == null)
            {
                Iterator iterator = m_setFiles.iterator();
                for(int k = 0; k < i; k++)
                {
   					tItem = new ReplaceItem();
   					tItem.setFolderId(s2);
   					tItem.setType(DBHelperClass.RAW_UNIT_TYPE);
                    String s4 = (String)iterator.next();
                    // String massaged_s4 = massageFilePathName(s4);
                    tItem.setFilePath(s4);//s4
                    m_PorticoSubmitBatchItemList.add(tItem);
                }
            }
            else
            {
                for(int j = 0; j < m_setFiles_MacNav.length; j++)
                {
   					tItem = new ReplaceItem();
   					tItem.setFolderId(s2);
   					tItem.setType(DBHelperClass.RAW_UNIT_TYPE);
                    String s3 = m_setFiles_MacNav[j];
                    // String massaged_s3 = massageFilePathName(s3);
                    tItem.setFilePath(s3);//s3
                    m_PorticoSubmitBatchItemList.add(tItem);
                }
    		}
    		captureOtherUserInfo();
	    }

		return isValidFiles;
	}

	private void captureOtherUserInfo()
	{
		// Walk thro' source radio controls to pick the selected source actual value
		if(m_sourceList != null && m_sourceList.size() > 0)
		{
			for(int index=0; index < m_sourceList.size(); index++)
			{
				String radioName = ReplaceContainer.SOURCERADIOLABEL_PREFIX+index;
				Radio radio_level = (Radio)getControl(radioName, com.documentum.web.form.control.Radio.class);
				if(radio_level != null && radio_level.getValue())
				{
                    m_strSelectedSource = ((ValuePair)m_sourceList.get(index)).getKey();
					break;
				}
			}
		}
	}

    public void setObjectType(String s)
    {
        ArgumentList argumentlist = getContainedComponentArgs();
        argumentlist.replace("type", s);
        Component component = (Component)getContainedComponents().get(getCurrentComponent());
        remove(component);
        getContainedComponent();
    }

    public void onControlInitialized(Form form, Control control)
    {
        String s = control.getName();
        if((s == null || !s.equalsIgnoreCase("formatList")) && (s == null || !s.equalsIgnoreCase("attribute_object_name")))
            super.onControlInitialized(form, control);
    }

    public void errorOccurred(ContentXferServiceEvent contentxferserviceevent)
    {
    }

    public void finished(ContentXferServiceEvent contentxferserviceevent)
    {
		HelperClass.porticoOutput(0, "ReplaceContainer - finished...........");
        IDfImportOperation idfimportoperation = (IDfImportOperation)contentxferserviceevent.getOperation();
        if(idfimportoperation != null)
        {
            Object obj = null;
            Object obj1 = null;
            Object obj2 = null;
            IDfFolder tIDfFolder = null;
            String folderObjectName = null;
            
            // CONPREP-2351, PMD2.0, 'p_reason' is replaced by 'p_user_added'
            // String repairReasonValue = "1"; // "1" for repaired
            
            String oldRawUnitId = "";
            try
            {
                oldRawUnitId = HelperClass.getRawUnitIdFromSuState(getDfSession(), m_strObjectId);
                tIDfFolder = (IDfFolder)getDfSession().getObject(new DfId(m_strBatchFolderId));
                folderObjectName = tIDfFolder.getObjectName();
                IDfList idflist = idfimportoperation.getNodes();
                m_strNewObjectIds = new String[idflist.getCount()];
                for(int i = 0; i < idflist.getCount(); i++)
                {
                    IDfImportNode idfimportnode = (IDfImportNode)idflist.get(i);
                    IDfId idfid = idfimportnode.getNewObjectId();
                    if(!idfid.isNull())
                    {
                        String s = idfid.toString();
                        m_strNewObjectIds[i] = new String(s);

                        IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(idfid);
						// iDfSysObject.setWorldPermit(IDfACL.DF_PERMIT_DELETE);
                        String massagedObjectName = HelperClass.massageFilePathName(iDfSysObject.getObjectName());
						iDfSysObject.setObjectName(massagedObjectName);
                        // Future
                        //iDfSysObject.setString("p_filename",massagedObjectName);
						
						// CONPREP-2351, PMD2.0, p_source is dropped
                        // iDfSysObject.setString("p_source",m_strSelectedSource);
                        
                        // CONPREP-2351, PMD2.0, 'p_reason' is replaced by 'p_user_added'
                        // iDfSysObject.setString("p_reason",repairReasonValue);
                        iDfSysObject.setBoolean("p_user_added", true);

                        if(oldRawUnitId != null && !oldRawUnitId.equals(""))
                        {
                            iDfSysObject.setString("p_predecessor_id", DBHelperClass.getRawUnitAccessionIdFromRawUnitId(oldRawUnitId));
					    }
                        // iDfSysObject.setString("", m_strDescValue);
                        iDfSysObject.save();
                    }
                    else
                    {
                        m_strNewObjectIds[i] = null;
                    }
                }




                // Post processing - Session, batchId, SUStateId, newRawUnitId
                boolean isSuccessful = false;
                try
                {
					// CONPREP-2351, PMD2.0, events, we need the source picked by the user has to be 'Provider' or 'Archive'
                    isSuccessful = QcHelperClass.postProcessingForReplace(getDfSession(), getFolderObjectId(), m_strObjectId, m_strNewObjectIds[0], getMsgObjectList(), m_strDescValue, m_strSelectedSource);
                    if(isSuccessful == true)
                    {
                        // Set the p_active flag on old raw unit to 'FALSE'
                        // Check again if this object still exists, because the Replace
                        //       could have potentially blown away the object
                        //       If it still exists make it inactive
    			    	HelperClass.porticoOutput(0, "ReplaceContainer - Before getting oldRawUnitId value");
                        oldRawUnitId = HelperClass.getRawUnitIdFromSuState(getDfSession(), m_strObjectId);
    			    	HelperClass.porticoOutput(0, "ReplaceContainer - oldRawUnitId value="+oldRawUnitId);
                        if(oldRawUnitId != null && !oldRawUnitId.equals(""))
                        {
                            IDfSysObject iDfSysObjectOldRawUnit = (IDfSysObject)getDfSession().getObject(new DfId(oldRawUnitId));
                            iDfSysObjectOldRawUnit.setBoolean("p_active", false);
                            iDfSysObjectOldRawUnit.save();
    			        }
				        else
				        {
				    		HelperClass.porticoOutput(0, "ReplaceContainer - oldSU/Rawunit not available, it is legitimate, keep moving forward");
				    	}
				    }
			    }
			    catch(Exception ex)
			    {
					isSuccessful = false;
					HelperClass.porticoOutput(1, "Exception in ReplaceContainer - finished(at setting oldrawunit info)="+ex.getMessage());
				}
				finally
				{
                    if(isSuccessful == true)
                    {
                        String rawUnitCount = ""+HelperClass.getAssetCountForBatchObject(getDfSession(), m_strBatchFolderId);
                        HelperClass.porticoOutput(0, "ReplaceContainer - finished - Start setting p_rawunit_count="+rawUnitCount);
                        tIDfFolder.setString("p_rawunit_count", rawUnitCount);
                        HelperClass.porticoOutput(0, "ReplaceContainer - finished - End setting p_rawunit_count="+rawUnitCount);
                        tIDfFolder.save();

                        setReturnValue("newObjectIds", m_strNewObjectIds);
                        setReturnValue("success", "true");
                        MessageService.addMessage(this, "MSG_REPAIRFILE_SUCCESS");
    				}
    				else
    				{
    					ArrayList rawunitIdList = new ArrayList();
    					rawunitIdList.add(m_strNewObjectIds[0]);
    					QcHelperClass.cleanUp(getDfSession(), rawunitIdList);
                        setReturnValue("newObjectIds", null);
                        setReturnValue("success", "false");
                        ErrorMessageService.getService().setNonFatalError(this, "MSG_REPAIRFILE_FAILED", null);
    				}
				}
            }
            catch(Exception exception)
            {
                setReturnError("MSG_ERROR_IMPORT", null, exception);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
                HelperClass.porticoOutput(1, "Exception in ReplaceContainer - finished="+exception.getMessage());
            }
        }
    }

    public void percentComplete(ContentXferServiceEvent contentxferserviceevent)
    {
    }

    public void started(ContentXferServiceEvent contentxferserviceevent)
    {
    }

    public void stepFinished(ContentXferServiceEvent contentxferserviceevent)
    {
    }

    public String[] getNewObjectIds()
    {
        return m_strNewObjectIds;
    }

    protected void continueAfterAppletCheck()
    {
        m_appletImport.setServiceUrl(getContentReceiverUrl());
        m_appletImport.setLocale(LocaleService.getLocale().toString());
        m_appletLinkDetector.setLocale(LocaleService.getLocale().toString());
        m_appletLinkDetector.setXmlAppServletURL(getXmlAppServletUrl());
        DfXMLUtils dfxmlutils = new DfXMLUtils();
        dfxmlutils.setSession(getDfSession());
        String s = DfXMLUtils.getParseableFileExt();
        m_appletLinkDetector.setParseableFileExt(s);
        setComponentPage("replaceSubmitInit"); // changed from "fileselection"
    }

    protected void continueAfterFullAppletCheck()
    {
        setComponentPage("containerstart");
    }

    private void importUpload(String s)
    {
        String s1 = null;
        StringBuffer stringbuffer = new StringBuffer(64);
        StringBuffer stringbuffer1 = new StringBuffer(64);
        StringBuffer stringbuffer2 = new StringBuffer(64);
        StringBuffer stringbuffer3 = new StringBuffer(64);
        StringBuffer stringbuffer4 = new StringBuffer(64);
        StringBuffer stringbuffer5 = new StringBuffer(64);

        for(int i = 0; i < m_PorticoSubmitBatchItemList.size(); i++)
        {
            HelperClass.porticoOutput(0, "ReplaceContainer - importUpload processing within arraylist");
            ReplaceItem importcontent = (ReplaceItem)m_PorticoSubmitBatchItemList.get(i);
            if(s1 == null)
            {
                // s1 = importcontent.getFolderId();
                s1 = getFolderObjectId();
                stringbuffer.append(importcontent.getFilePath());


                // getObjectName() has just the file name, we want the complete filename with Path,
                // so append the getFilePath(), which is the full file name
                // stringbuffer3.append(importcontent.getObjectName());

                stringbuffer3.append(importcontent.getFilePath());
                stringbuffer1.append(importcontent.getType());
                stringbuffer2.append(getFormat(importcontent.getFilePath()));
                if(macOS9Nav())
                {
                    String s2 = getFormat(importcontent.getFilePath());
                    if(!s2.equals("xml"))
                        stringbuffer4.append("");
                    else
                        stringbuffer4.append(importcontent.getXmlCategory());
                }
                else
                {
                    stringbuffer4.append(importcontent.getXmlCategory());
                }
                stringbuffer5.append(importcontent.getPropertySettings());
            }
            else
            {
                stringbuffer.append("|").append(importcontent.getFilePath());


                // getObjectName() has just the file name, we want the complete filename with Path,
                // so append the getFilePath(), which is the full file name
                // stringbuffer3.append("|").append(importcontent.getObjectName());

                stringbuffer3.append("|").append(importcontent.getFilePath());
                stringbuffer1.append("|").append(importcontent.getType());
                stringbuffer2.append("|").append(getFormat(importcontent.getFilePath()));
                if(macOS9Nav())
                {
                    String s3 = getFormat(importcontent.getFilePath());
                    if(!s3.equals("xml"))
                        stringbuffer4.append("|").append("");
                    else
                        stringbuffer4.append("|").append(importcontent.getXmlCategory());
                }
                else
                {
                    stringbuffer4.append("|").append(importcontent.getXmlCategory());
                }
                stringbuffer5.append("|").append(importcontent.getPropertySettings());
            }
        }

        m_appletImport.setLinkInstructions(s);
        m_appletImport.setFile(stringbuffer.toString());
        m_appletImport.setType(stringbuffer1.toString());
        m_appletImport.setFormat(stringbuffer2.toString());
        m_appletImport.setFilename(stringbuffer3.toString());
        m_appletImport.setCategory(stringbuffer4.toString());
        m_appletImport.setPropertySettings(stringbuffer5.toString());
        m_appletImport.setFolderId(s1);
        HelperClass.porticoOutput(0, "ReplaceContainer - importUpload before setComponentPage(importupload)");
        setComponentPage("importupload");
    }

    private void setupFileListBox()
    {
        m_listboxFiles.setMutable(true);
        m_listboxFiles.clearOptions();
        Option option;
        for(Iterator iterator = m_setFiles.iterator(); iterator.hasNext(); m_listboxFiles.addOption(option))
        {
            String s = (String)iterator.next();
            option = new Option();
            option.setValue(s);
            if(s.length() > 64)
            {
                StringBuffer stringbuffer = new StringBuffer(67);
                String s1 = s.substring(0, 3);
                String s2 = s.substring((6 + s.length()) - 64);
                stringbuffer.append(s1);
                stringbuffer.append("...");
                stringbuffer.append(s2);
                s = stringbuffer.toString();
            }
            option.setLabel(s);
        }
    }

    public String getFolderObjectId()
    {
		return m_strBatchFolderId;
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
					// fileObjectIds.add(HelperClass.getRawUnitIdFromSuState(getDfSession(), (String)tStringTokenizer.nextToken().trim()));
					fileObjectIds.add((String)tStringTokenizer.nextToken().trim());
				}
			}
			else
			{
			    // fileObjectIds.add(HelperClass.getRawUnitIdFromSuState(getDfSession(), m_strObjectId));
			    fileObjectIds.add(m_strObjectId);
			}
		}
		return fileObjectIds;
	}

	public void populateMsgObjectList()
	{
		m_msgObjectList.clear();
        // User Message Id

		if(m_strUserMessageObjectId != null)
		{
			if(m_strUserMessageObjectId.indexOf(COMMA_SEPARATOR) != -1)
			{
				StringTokenizer tStringTokenizer = new StringTokenizer(m_strUserMessageObjectId, COMMA_SEPARATOR);
				while (tStringTokenizer.hasMoreTokens())
				{
					m_msgObjectList.add((String)tStringTokenizer.nextToken().trim());
				}
			}
			else
			{
			    m_msgObjectList.add(m_strUserMessageObjectId);
			}
		}
	}

	public ArrayList getMsgObjectList()
	{
		return m_msgObjectList;
	}

	public String getAdditionalErrorMessages()
	{
		return m_strAdditionalErrorMessageNLS;
	}
/* Moved to HelperClass.java
	private String getRawUnitIdFromSuState(String suStateId)
	{
	    String rawUnitId = "";
   		ValuePair tValuePair = null;
   		ArrayList attrList = new ArrayList();
        attrList.add("p_raw_unit_id");
        ArrayList outList = HelperClass.getObjectAttrValues(getDfSession(), HelperClass.SU_TYPE, suStateId, attrList);
        if(outList != null && outList.size() > 0)
        {
            String attrValue = "";
            for(int indx=0; indx < outList.size(); indx++)
            {
               	tValuePair = (ValuePair)outList.get(indx);
               	rawUnitId = (String)tValuePair.getValue();
               	break;
            }
        }

        return rawUnitId;
	}
*/

    public boolean checkUserInformation()
    {
		boolean tbool = true;
		m_strDescValue = m_strDesc.getValue().trim();
		if(m_strDescValue == null || m_strDescValue.equals(""))
		{
			tbool = false;
		    HelperClass.porticoOutput(0, "ReplaceContainer - checkUserInformation - m_strDescValue not entered");
            setReturnError("MSG_ERROR_NO_DESC_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_NO_DESC_ENTERED", null);
		}

		return tbool;
	}

    public boolean checkAdditionalValidation()
    {
		boolean tbool = true;
		try
		{
		    if(QcHelperClass.isSuStatePartOfMultipleSuppliedFiles(getDfSession(), getFolderObjectId(), m_strObjectId) && !QcHelperClass.isTopLevelSuppliedFile(getDfSession(), getFolderObjectId(), m_strObjectId))
		    {
		    	tbool = false;
		        HelperClass.porticoOutput(0, "ReplaceContainer - checkAdditionalValidation");
		        m_strAdditionalErrorMessageNLS = MSG_MULTIPLE_SUPPLIED_FILES;
		    }
	    }
	    catch(Exception e)
	    {
			tbool = false;
			HelperClass.porticoOutput(1, "Exception in ReplaceContainer - checkAdditionalValidation="+e.getMessage());
			e.printStackTrace();
		}

		return tbool;
	}

	private String getPredecessorAbsoluteFileName()
	{
		String predecessorAbsoluteFileName = "";
		if(m_fileList != null && m_fileList.size() > 0)
		{
			predecessorAbsoluteFileName = (String)m_fileList.get(0);
		}
		return predecessorAbsoluteFileName;
	}

    // Methods from corresponding component
    private void initFormatInformation()
    {
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL("SELECT name,description,dos_extension FROM dm_format WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') ORDER BY description");
   		HelperClass.porticoOutput(0, "ReplaceContainer - initFormatInformation OPEN IDfCollection");
        IDfCollection idfcollection = null;
        try
        {
            for(idfcollection = dfquery.execute(getDfSession(), 0); idfcollection.next();)
            {
                String s = idfcollection.getString("name");
                String s1 = idfcollection.getString("description");
                String s2 = idfcollection.getString("dos_extension");
                m_docbaseFormatNames.add(s);
                if(!s2.equals(""))
                    m_extensionMap.put(s2, s);
            }
        }
        catch(DfException dfexception)
        {
            throw new WrapperRuntimeException("Unable to query format types from docbase!", dfexception);
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
				}
           		HelperClass.porticoOutput(0, "ReplaceContainer - initFormatInformation CLOSE IDfCollection");
            }
            catch(DfException dfexception1) { }
        }
    }

    private String getFormat(String fileNameWithPath)
    {
		String currentFormat = "unknown";
        String s = UploadUtil.extractExtension(fileNameWithPath);
        if(s != null)
        {
            String s1 = null;
            boolean flag = false;
            s1 = (String)m_ConfiguredDefaultFormats.get(s);
            if(s1 != null)
            {
                flag = m_docbaseFormatNames.contains(s1);
                if(flag)
                    currentFormat = s1;
            }
            if(!flag)
            {
                String s2 = (String)m_extensionMap.get(s);
                if(s2 != null && s2.length() > 0)
                    currentFormat = s2;
                else
                    currentFormat= "unknown";
            }
        }
        return currentFormat;
	}

    private void readConfiguredDefaultFormats()
    {
        IConfigElement iconfigelement = lookupElement("default_formats");
        if(iconfigelement != null)
        {
            for(Iterator iterator = iconfigelement.getChildElements("format"); iterator.hasNext();)
            {
                IConfigElement iconfigelement1 = (IConfigElement)iterator.next();
                if(iconfigelement1 != null)
                {
                    String s = iconfigelement1.getAttributeValue("dos_extension");
                    String s1 = iconfigelement1.getAttributeValue("name");
                    if(s != null && s1 != null)
                    {
                        if(m_ConfiguredDefaultFormats.containsKey(s))
                            throw new WrapperRuntimeException("Duplicate format entries for a dos_extension in the config file.");
                        m_ConfiguredDefaultFormats.put(s, s1);
                    }
                }
            }

        }
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

    public static final String FILE_BROWSER_APPLET_CONTROL = "__FILE_BROWSER_APPLET_CONTROL";
    private ImportApplet m_appletImport;
    private LinkDetectorApplet m_appletLinkDetector;
    private ServiceProgressFeedback m_progress;
    private String m_strContentTicket;
    private TreeSet m_setFiles;
    private String m_setFiles_MacNav[];
    private ListBox m_listboxFiles;
    private String m_strNewObjectIds[];
    private boolean m_xmlParsable;
    private int m_intXmlEndIndex;

    // Relevant to corresponding component of this container
    private List m_docbaseFormatNames;
    private Map m_extensionMap;
    private Map m_ConfiguredDefaultFormats;
    private ArrayList m_PorticoSubmitBatchItemList;
    private Hashtable m_FileExtnTypeList;

    // New stuff
	private Label m_batchName;
	private Label m_providerName;
    private Text m_strDesc;

    // Data
    private String m_strObjectId;
    private String m_strSelectedSource;
    private String m_strBatchFolderId;
    private String m_strProviderName;
    private String m_strDescValue;
    private ArrayList m_fileList;
    private ArrayList m_sourceList;
    private String m_strUserMessageObjectId = "";
    private ArrayList m_msgObjectList;
	private String m_strReEntryPoint = "";
	private String m_strAdditionalErrorMessageNLS = "";

    public static String COMMA_SEPARATOR = ",";
    public static String FILEBROWSER_PREFIX = "filebrowse";
    public static String FILEBROWSERLABEL_PREFIX = "filebrowselabel";
    public static String SOURCERADIOLABEL_PREFIX = "sourcelabel";
    public static String MSG_MULTIPLE_SUPPLIED_FILES = "MSG_MULTIPLE_SUPPLIED_FILES";
}