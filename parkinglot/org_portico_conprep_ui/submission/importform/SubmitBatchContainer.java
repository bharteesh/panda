
package org.portico.conprep.ui.submission.importform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.profile.ProfileUI;
import org.portico.conprep.ui.provider.ProviderUI;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.DfXMLUtils;
import com.documentum.operations.IDfImportNode;
import com.documentum.operations.IDfImportOperation;
import com.documentum.web.common.AccessibilityService;
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
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.fileselector.FileSelector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.util.Browser;
import com.documentum.webcomponent.library.contentxfer.UploadContainer;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.documentum.webcomponent.library.messages.MessageService;


public class SubmitBatchContainer extends UploadContainer
    implements IContentXferServiceListener
{

    public SubmitBatchContainer()
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
		m_providerList = null;
		m_profileList = null;
		m_batchName = null;
		m_contentTypeLabel = null;
		listProviderUI = null;
		m_strFolderID = null;

		m_batchDesc = null;
		//m_receiptModeList = null;
		//m_batchTrackingNo = null;
		//listReceiptModeUI = null;
		m_DocbaseObject = null;
        m_docbaseFormatNames = new LinkedList();
        m_extensionMap = new HashMap(311, 1.0F);
        m_ConfiguredDefaultFormats = new HashMap(23, 1.0F);
        m_PorticoSubmitBatchItemList = new ArrayList();
        m_RequiredAttributes = null;
        m_FileExtnTypeList = new Hashtable();

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
        Component component = getContainedComponent();
        if(component != null)
        {
            String s = component.getComponentPage();
            if(s != null && s.equals("xmlappdetect"))
                getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
        }
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
        super.onInit(argumentlist);
        if(!validateInitForminfo())
        {
			// Error condition
			getControl("prev", com.documentum.web.form.control.Button.class).setEnabled(false);
			getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
			getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(false);
            setReturnValue("success", "false");
            // To be changed NLS String
            MessageService.addMessage(this, "MSG_ERROR_SUBMIT_BATCH_INIT_INFO");
 			setComponentPage("batchSubmitErrorPage");
        }
        else
        {
            m_appletImport = (ImportApplet)getControl("importapplet", com.documentum.web.contentxfer.control.ImportApplet.class);
            m_appletLinkDetector = (LinkDetectorApplet)getControl("linkdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
            m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
            m_listboxFiles = (ListBox)getControl("filelist", com.documentum.web.form.control.ListBox.class);
            m_listboxFiles.setMutable(true);
            Panel panel = (Panel)getControl("IEButtons", com.documentum.web.form.control.Panel.class);
            Panel panel1 = (Panel)getControl("NSButtons", com.documentum.web.form.control.Panel.class);
            panel.setVisible(isBrowserIE());
            panel1.setVisible(isBrowserNetscape());
            getControl("prev", com.documentum.web.form.control.Button.class).setEnabled(false);
            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(!AccessibilityService.isAllAccessibilitiesEnabled());
            getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(false);

            // New stuff
            m_batchName = (Text)getControl("batch_name", com.documentum.web.form.control.Text.class);
            m_providerList = (DropDownList)getControl("dropdownlist_provider", com.documentum.web.form.control.DropDownList.class);
	    	m_providerList.setMutable(true);
	    	m_profileList = (DropDownList)getControl("dropdownlist_profile", com.documentum.web.form.control.DropDownList.class);
	    	m_profileList.setMutable(true);
            // New 2
            m_batchDesc = (Text)getControl("batch_desc", com.documentum.web.form.control.Text.class);
            //m_receiptModeList = (DropDownList)getControl("dropdownlist_receipt_mode", com.documentum.web.form.control.DropDownList.class);
	    	//m_receiptModeList.setMutable(true);

            //m_batchTrackingNo = (Text)getControl("batch_tracking_no", com.documentum.web.form.control.Text.class);

            m_contentTypeLabel = (Label)getControl("batch_contenttype", com.documentum.web.form.control.Label.class);

            readRequiredAttributes();
	    	// listProviderUI = AppSessionContext.getProviderUI();
	    	populateProviderProfileOption();

	    	//listReceiptModeUI = HelperClass.getValueAssistanceListFromDocbase(getDfSession(), DBHelperClass.BATCH_TYPE, "p_receipt_mode");
	    	//populateReceiptModeOption();

            // Stuffs from component
            initFormatInformation();
            readConfiguredDefaultFormats();
            setReturnValue("success", "true");
	    }
    }

    public void readRequiredAttributes()
    {
		ArrayList attrNames = new ArrayList();
		attrNames.add("object_name"); // batch_name
		attrNames.add("title");// batch_desc
		attrNames.add("p_provider_id");// dropdownlist_provider
		attrNames.add("p_profile_id");// dropdownlist_profile
//		attrNames.add("p_receipt_mode");// dropdownlist_receipt_mode
//		attrNames.add("p_tracking_id");// batch_tracking_no
		m_RequiredAttributes = HelperClass.getRequiredAttributes(getDfSession(), DBHelperClass.BATCH_TYPE, attrNames);
	}

	public boolean isRequiredAttribute(String attrName)
	{
		boolean tBool = false;
		if(m_RequiredAttributes != null && m_RequiredAttributes.size() > 0)
		{
		   tBool = m_RequiredAttributes.contains(attrName);
		}
		return tBool;
	}

    public boolean validateInitForminfo()
    {
		boolean isInitInfoAvailable = true;

		listProviderUI = AppSessionContext.getProviderUI();
		if(listProviderUI == null || listProviderUI.size() == 0)
		{
			// HelperClass.LoadSessionInformation(getDfSession());
			// listProviderUI = AppSessionContext.getProviderUI();
			// if(listProviderUI == null || listProviderUI.size() == 0)
			// {
			    // Potential webservices issue or no data at all for Provider/Profile
			    isInitInfoAvailable = false;
		    // }
		}

	    return isInitInfoAvailable;
	}

    public void onOk(Control control, ArgumentList argumentlist)
    {
		boolean isValidFiles = checkAddedFiles();

        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before validate()");
        validate();
        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk after validate()");
        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before getIsValid()");
        boolean flag = getIsValid();
        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk after getIsValid()="+flag);
        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before canCommitChanges()");
        boolean flag1 = canCommitChanges();
        HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk after canCommitChanges()="+flag1);
        if(isValidFiles)
        {
            if(flag && flag1 && onCommitChanges())
            {
    			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before importUpload()");
                importUpload("");
     			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk after importUpload()");
    		}
    		else
    		{
    			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before inAutoCommit()");
                if((!flag || !flag1) && inAutoCommit())
                {
        			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk before stopAutoCommit()");
                    stopAutoCommit();
    			}
    			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk after inAutoCommit()");
    		}
	    }
	    else
	    {
			HelperClass.porticoOutput(0, "SubmitBatchContainer - onOk - Do nothing.................");
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
        SubmitBatchComponent importcontent = (SubmitBatchComponent)getContainedComponent();
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
            HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - startImport="+exception.getMessage());
        }
    }

    public void onContentTransferServiceComplete(Control control, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

    public void onAddToList(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("filenameWithPath");
        if(s != null && s.length() > 0 && !m_setFiles.contains(s))
        {
            m_setFiles.add(s);
            setupFileListBox();
            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(true);
        }
    }

    public void onRemoveFromList(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("filenameWithPath");
        if(s != null && s.length() > 0 && m_setFiles.contains(s))
        {
            m_setFiles.remove(s);
            setupFileListBox();
            if(m_setFiles.size() == 0)
                getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
        }
    }

    public boolean onPrevPage()
    {
		String s = getComponentPage();
		if(s.equals("fileselection"))
		{
			setComponentPage("batchSubmitInit");
			getControl("prev", com.documentum.web.form.control.Button.class).setEnabled(false);
            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(true);
            getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(false);
			return true;
		}
		else
		{
			return super.onPrevPage();
		}
	}

    public boolean checkAddedFiles()
    {
		boolean isValidFiles = true;
		String s = getComponentPage();
        if(s.equals("fileselection") || s.equals("accessibleFileselection"))
        {
            if(s.equals("fileselection"))
            {
                FileSelector fileselector = (FileSelector)getControl("__FILE_SELECTOR_APPLET_CONTROL");
                String as[] = fileselector.getFiles();
                if(as == null || as.length == 0)
                {
                    setReturnError("MSG_NO_FILES_SELECTED", null, null);
                    ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_FILES_SELECTED", null);
                    isValidFiles = false;
                    return isValidFiles;
                }
                if(macOS9Nav())
                {
                    as = putXmlFirst(as);
                    m_setFiles_MacNav = as;
                }
                m_setFiles.addAll(Arrays.asList(as));
            }
            int i = m_setFiles.size();
            String s2 = ""; // getObjectIds(); will be used in importUpload();
            m_PorticoSubmitBatchItemList.clear();
            SubmitBatchItem tItem = null;
            if(m_setFiles_MacNav == null)
            {
                Iterator iterator = m_setFiles.iterator();
                for(int k = 0; k < i; k++)
                {
					tItem = new SubmitBatchItem();
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
					tItem = new SubmitBatchItem();
					tItem.setFolderId(s2);
					tItem.setType(DBHelperClass.RAW_UNIT_TYPE);
                    String s3 = m_setFiles_MacNav[j];
                    // String massaged_s3 = massageFilePathName(s3);
                    tItem.setFilePath(s3);//s3
                    m_PorticoSubmitBatchItemList.add(tItem);
                }
            }
        }
        else
        {
			isValidFiles = false;
		}

		return isValidFiles;
	}

    public boolean onNextPage()
    {
        boolean flag = false;
        String s = getComponentPage();
        if(s.equals("batchSubmitInit"))
        {
       		flag = checkInformation();
       		if(flag == true)
       		{
				setComponentPage("fileselection");
                getControl("prev", com.documentum.web.form.control.Button.class).setEnabled(true);
                getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
                getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(true);
       		}
		}

        return flag;
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
        IDfImportOperation idfimportoperation = (IDfImportOperation)contentxferserviceevent.getOperation();
        if(idfimportoperation != null)
        {
            Object obj = null;
            Object obj1 = null;
            Object obj2 = null;
            IDfFolder tIDfFolder = null;
            String folderObjectName = null;
            try
            {
                tIDfFolder = (IDfFolder)getDfSession().getObject(new DfId(m_strFolderID));
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
                        String currentObjectName = iDfSysObject.getObjectName();
                        String massagedObjectName = HelperClass.massageFilePathName(currentObjectName);
                        iDfSysObject.setObjectName(massagedObjectName);
                        // Future
                        //iDfSysObject.setString("p_filename",massagedObjectName);
                        iDfSysObject.setString("p_content_stream_type", m_contentTypeLabel.getLabel());
                        HelperClass.porticoOutput(0, "SubmitBatchContainer - setting p_content_stream_type on Rawunit to="+m_contentTypeLabel.getLabel());
                        iDfSysObject.save();
                    }
                    else
                    {
                        m_strNewObjectIds[i] = null;
                    }
                }

// Change the status of the batch to HelperClassConstants.LOADED, since it was successful
                tIDfFolder.setString(HelperClassConstants.BATCH_STATE, HelperClass.getStatusActualValue(getDfSession(), DBHelperClass.BATCH_TYPE, HelperClassConstants.LOADED));
                tIDfFolder.setString("p_rawunit_count", idflist.getCount()+"");
                HelperClass.porticoOutput(0, "SubmitBatchContainer - finished - setting p_rawunit_count="+idflist.getCount());
                tIDfFolder.setString("p_content_stream_type", m_contentTypeLabel.getLabel());
                HelperClass.porticoOutput(0, "SubmitBatchContainer - setting p_content_stream_type on Batch to="+m_contentTypeLabel.getLabel());
                tIDfFolder.save();

                setReturnValue("newObjectIds", m_strNewObjectIds);
                setReturnValue("success", "true");
                MessageService.addMessage(this, "MSG_IMPORT_SUCCESS");
// Send Notification to user - Successful Batch Import
/*
                String hdr = folderObjectName + " :: Ready for Schedule ";
                String msg = "You may schedule this batch: ";
                sendThisNotification(tIDfFolder, folderObjectName, hdr, msg);
*/
            }
            catch(Exception exception)
            {
// Send Notification to user - Failed Batch Import
/*
                String hdr = folderObjectName + " :: Failed Submission ";
                String msg = "The batch submission failed for: ";
                sendThisNotification(tIDfFolder, folderObjectName, hdr, msg);
*/
                setReturnError("MSG_ERROR_IMPORT", null, exception);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
                HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - finished="+exception.getMessage());
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
        if(AccessibilityService.isAllAccessibilitiesEnabled())
            setComponentPage("accessibleFileselection");
        else
            setComponentPage("batchSubmitInit"); // changed from "fileselection"
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
            HelperClass.porticoOutput(0, "SubmitBatchContainer - importUpload processing within arraylist");
            SubmitBatchItem importcontent = (SubmitBatchItem)m_PorticoSubmitBatchItemList.get(i);
            if(s1 == null)
            {
                // s1 = importcontent.getFolderId();
                s1 = getObjectIds();
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
        HelperClass.porticoOutput(0, "SubmitBatchContainer - importUpload before setComponentPage(importupload)");
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

    public boolean checkIfBatchNameExists()
    {
		boolean tbool = false;
		try
		{
	    	IDfSession tIDfSession = getDfSession();
            String completeBatchPath = m_batchName.getValue().trim();
            completeBatchPath = getCompleteFolderPathForBatch()+"/"+completeBatchPath;

	    	if(tIDfSession.getFolderByPath(completeBatchPath) != null)
	    	{
	    		tbool = true;
	    	}
	    }
        catch(Exception exception)
        {
            setReturnError("MSG_ERROR_SEARCHING_FOLDERPATH", null, exception);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_SEARCHING_FOLDERPATH", exception);
            HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - checkIfBatchNameExists="+exception.getMessage());
        }
        finally
        {
        	// Release session if relevant
		}

		HelperClass.porticoOutput(0, "SubmitBatchContainer - checkIfBatchNameExists="+tbool);
		return tbool;
	}

	public String getCompleteFolderPathForBatch()
	{
            String providerID = m_providerList.getValue().trim();
            String providerName = getProviderNameFromId(providerID);
    		// String parentFolderPath = lookupString("docbase_upload_folderpath");
			String providerFolderID = getParentFolderPath("SUBMISSION") + "/"+ providerName;

			HelperClass.porticoOutput(0, "SubmitBatchContainer - getCompleteFolderPathForBatch - providerFolderID="+providerFolderID);

			return providerFolderID;
	}

    public String getObjectIds()
    {
		// Create a folder of type DBHelperClass.BATCH_TYPE under "/submission_area"
	    if(m_strFolderID == null || m_strFolderID.equals(""))
	    {
            String objectType = DBHelperClass.BATCH_TYPE;
            String objectName = m_batchName.getValue().trim();

            String providerID = m_providerList.getValue().trim();
            String profileID = m_profileList.getValue().trim();
            //String receiptMode = m_receiptModeList.getValue().trim();
            //String trackingNumber = m_batchTrackingNo.getValue().trim();
            String batchDescription = m_batchDesc.getValue().trim();

            m_providerName = getProviderNameFromId(providerID);
            // String profileName = getProfileNameFromId(providerID, profileID);

            String providerFolderID = "";
    		String parentFolderPath = getParentFolderPath("submission"); //lookupString("docbase_upload_folderpath");

    		HelperClass.porticoOutput(0, "getObjectIds - parentFolderPath(lookup)="+parentFolderPath);

			providerFolderID = parentFolderPath + "/"+ m_providerName;
			HelperClass.porticoOutput(0, "SubmitBatchContainer - getObjectIds - providerFolderID="+providerFolderID);

            try
            {
				IDfSession tIDfSession = getDfSession();
				IDfSysObject idfsysobject = null;
				IDfId idfid = null;
				if(tIDfSession.getFolderByPath(providerFolderID) == null)
				{
                    idfsysobject = (IDfSysObject)tIDfSession.newObject(HelperClass.getInternalObjectType("provider_folder"));
                    idfsysobject.setObjectName(m_providerName);
                    // idfsysobject.setWorldPermit(IDfACL.DF_PERMIT_WRITE);
                    if(parentFolderPath != null && parentFolderPath.length() > 0)
                        idfsysobject.link(parentFolderPath);
                    idfsysobject.save();
				}
                idfsysobject = (IDfSysObject)tIDfSession.newObject(objectType);
                idfid = idfsysobject.getObjectId();
                m_strFolderID = idfid.getId();
                idfsysobject.setObjectName(objectName);
                idfsysobject.setString("p_provider_id",providerID);
                idfsysobject.setString("p_profile_id",profileID);
//                idfsysobject.setString("p_receipt_mode",receiptMode);
//                idfsysobject.setString("p_tracking_id",trackingNumber);
                idfsysobject.setString("p_content_stream_type",m_contentTypeLabel.getLabel());
                idfsysobject.setTitle(batchDescription);
                String tStatus = HelperClass.getStatusActualValue(tIDfSession, objectType, HelperClassConstants.NEW);
                idfsysobject.setString(HelperClassConstants.BATCH_STATE, tStatus);
//                idfsysobject.setTime("p_receipt_timestamp", new DfTime());
                // idfsysobject.setWorldPermit(IDfACL.DF_PERMIT_DELETE);
                if(providerFolderID != null && providerFolderID.length() > 0)
                    idfsysobject.link(providerFolderID);
                idfsysobject.save();
            }
            catch(Exception exception)
            {
                setReturnError("MSG_ERROR_CREATING_FOLDER", null, exception);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_CREATING_FOLDER", exception);
                HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - getObjectIds="+exception.getMessage());
            }
            finally
            {
				// Release session if relevant
			}
		}

		return m_strFolderID;
	}

	public void sendThisNotification(IDfFolder idffolder, String objectName, String hdr, String msg)
	{
		try
		{
		 //seshadri ranganathan, priya ramaswamy, Suku -- Documentum username
		 // msg == "You may schedule the submitted batch: "
		 // hdr == "Batch_Submitted:"
		    IDfId iDfId = null;
		    // iDfId = idffolder.queue("seshadri ranganathan",hdr+objectName,1,false,null,msg+objectName);
		    // iDfId = idffolder.queue("priya ramaswamy","Batch_Submitted:"+objectName,1,false,null,msg+objectName);
		    // iDfId = idfsysobject.queue("Suku","Batch_Submitted:"+objectName,1,true,null,msg+objectName);

		    IDfUser iDfUser = getDfSession().getUser(null); // Current logged in User
		    String thisUserName = iDfUser.getUserName(); // Documentum username
		    iDfId = idffolder.queue(thisUserName,hdr,1,false,null,msg+objectName);

		    MessageService.addDetailedMessage(this, "MSG_SUCCESSFUL_NOTIFICATION", " Batch Name:"+ objectName, false);
		}
		catch(Exception e)
		{
			MessageService.addDetailedMessage(this, "MSG_FAILED_NOTIFICATION", " Batch Name:"+ objectName + " " + e.toString(),true);
			HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - sendThisNotification="+e.getMessage());
		}
		finally
		{
		}
	}

    // To be moved to HelperClass
  	public String getProviderNameFromId(String providerID)
	{
		String providerName = "";
		if(listProviderUI != null)
		{
			ProviderUI tProvider = null;
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
				if(currentProviderID.equals(providerID))
				{
					providerName = tProvider.getProviderName();
					break;
				}
			}
		}
		return providerName;
	}

    // To be moved to HelperClass
	public String getProfileNameFromId(String providerID, String profileID)
	{
		String profileName = "";
		if(listProviderUI != null)
		{
			ProviderUI tProvider = null;
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
				if(currentProviderID.equals(providerID))
				{
					ArrayList tProfileList = tProvider.getListProfileUI();
					ProfileUI tProfile = null;
					for(int profIndx=0; profIndx < tProfileList.size(); profIndx++)
					{
						tProfile = (ProfileUI)tProfileList.get(profIndx);
						String currentProfileID = tProfile.getProfileID();
						if(currentProfileID.equals(profileID))
						{
							profileName = tProfile.getProfileName();
							break;
						}
					}
					break;
				}
			}
		}
		return profileName;
	}

    public void onSelectProvider(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "SubmitBatchContainer - onSelectProvider() entered");

       String selectedProvider = m_providerList.getValue();
       HelperClass.porticoOutput(0, "SubmitBatchContainer - onSelectProvider() entered - value="+selectedProvider);
       populateProfileOption(selectedProvider);
    }

    // populateContentTypeLabel(String providerID, String profileID)
    public void onSelectProfile(Control control,ArgumentList args)
    {
       HelperClass.porticoOutput(0, "SubmitBatchContainer - onSelectProfile() entered");
       String selectedProvider = m_providerList.getValue();
       String selectedProfileId = m_profileList.getValue();
       HelperClass.porticoOutput(0, "SubmitBatchContainer - onSelectProfile() entered - selectedProvider,selectedProfileId="+selectedProvider+","+selectedProfileId);
       populateContentTypeLabel(selectedProvider, selectedProfileId);
    }

    public boolean checkInformation()
    {
		boolean tbool = true;
		if(m_batchName == null || m_batchName.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_BATCH_NAME", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_BATCH_NAME", null);
            tbool = false;
		}
		// Other info are already checked OR appear as defaults
/* Not mandatory
		if(m_batchDesc == null || m_batchDesc.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_BATCH_DESC", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_BATCH_DESC", null);
            tbool = false;
		}
*/
		if(m_providerList == null || m_providerList.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_PROVIDER", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_PROVIDER", null);
            tbool = false;
		}
		if(m_profileList == null || m_profileList.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_PROFILE", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_PROFILE", null);
            tbool = false;
		}
/* removing attrib from conprep MD
		if(m_receiptModeList == null || m_receiptModeList.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_RECEIPT_MODE", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_RECEIPT_MODE", null);
            tbool = false;
		}
*/
/* Not mandatory
		if(m_batchTrackingNo == null || m_batchTrackingNo.getValue().trim().equals(""))
		{
            setReturnError("MSG_NO_BATCH_TRACKING_NO", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_BATCH_TRACKING_NO", null);
            tbool = false;
		}
*/
		if(checkIfBatchNameExists() == true)
		{
            setReturnError("MSG_BATCH_NAME_ALREADY_EXISTS", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_BATCH_NAME_ALREADY_EXISTS", null);
            tbool = false;
		}
		return tbool;
	}

    public void populateProviderProfileOption()
    {
		m_providerList.setMutable(true);
		m_providerList.clearOptions();

		if(listProviderUI != null)
		{
			Option option = null;
			ProviderUI tProvider = null;
/*
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
                option = new Option();
            	option.setValue(currentProviderID);
                option.setLabel(tProvider.getProviderName());
				m_providerList.addOption(option);

				if(0==provIndx)
				{
					populateProfileOption(currentProviderID);
				}
			}
*/
            TreeMap sortedProviderList = new TreeMap();
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);

				String currentProviderID = tProvider.getProviderID();
				sortedProviderList.put(tProvider.getProviderName(), tProvider.getProviderID());
			}

            if(sortedProviderList != null && sortedProviderList.size() > 0)
            {
                Iterator iterator = sortedProviderList.keySet().iterator();
                int indx = 0;
                while(iterator.hasNext())
                {
			    	String key = (String)iterator.next();// name
					String value = (String)sortedProviderList.get(key); // id

                    option = new Option();
                	option.setValue(value);
                    option.setLabel(key);
    				m_providerList.addOption(option);

    				if(0==indx)
				    {
				    	populateProfileOption(value);
				    }
					indx++;
				}
		    }
		}
	}

	public void populateProfileOption(String selectedProviderID)
	{
		m_profileList.setMutable(true);
		m_profileList.clearOptions();

		String displayedProfileId = "unknown";

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
             			    m_profileList.addOption(option);
             			    displayedProfileId = tProfile.getProfileID();
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
             			m_profileList.addOption(option);
             			if(displayedProfileId.equals("unknown"))
             			{
							displayedProfileId = tProfile.getProfileID();
						}
					}

					break;
				}
			}
            // Populate the ContentType Display label ??
            populateContentTypeLabel(selectedProviderID, displayedProfileId);
		}
	}

	public void populateContentTypeLabel(String providerID, String profileID)
	{
		m_contentTypeLabel.setLabel("Unknown ContentType");

		if(listProviderUI != null)
		{
			ProviderUI tProvider = null;
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
				if(currentProviderID.equals(providerID))
				{
					ArrayList tProfileList = tProvider.getListProfileUI();
					ProfileUI tProfile = null;
					for(int profIndx=0; profIndx < tProfileList.size(); profIndx++)
					{
						tProfile = (ProfileUI)tProfileList.get(profIndx);
						String currentProfileID = tProfile.getProfileID();
						if(currentProfileID.equals(profileID))
						{
							m_contentTypeLabel.setLabel(tProfile.getContentType());
							HelperClass.porticoOutput(0, "SubmitBatchContainer-setting contentType for providerID,profileID,ContentType="+providerID+","+profileID+","+tProfile.getContentType());
							break;
						}
					}
					break;
				}
			}
		}
	}

/* receiptMode removal from conprepMd
	public void populateReceiptModeOption()
	{
		m_receiptModeList.setMutable(true);
		m_receiptModeList.clearOptions();

		if(listReceiptModeUI != null)
		{
			String defaultTopDisplayValue = "1"; // FTP
			Option option = null;
            for(int indx=0; indx < listReceiptModeUI.size(); indx++)
            {
				ValuePair tPorticoValuePair = (ValuePair)listReceiptModeUI.get(indx);
                String thisKey = (String)tPorticoValuePair.getKey();
                String thisValue = (String)tPorticoValuePair.getValue();

                HelperClass.porticoOutput(0, "key="+thisKey);
                HelperClass.porticoOutput(0, "value="+thisValue);

                if(thisKey.equals(defaultTopDisplayValue)) // FTP
                {
                    option = new Option();
                    option.setValue(thisKey); // 0, 1, 2(Actual Value)
                    option.setLabel(thisValue); // CD, FTP, E-MAIL(Display value)
		            m_receiptModeList.addOption(option);
		            break;
			    }
		    }
            for(int sindx=0; sindx < listReceiptModeUI.size(); sindx++)
            {
				ValuePair tPorticoValuePair = (ValuePair)listReceiptModeUI.get(sindx);
                String thisKey = (String)tPorticoValuePair.getKey();
                String thisValue = (String)tPorticoValuePair.getValue();

                HelperClass.porticoOutput(0, "key="+thisKey);
                HelperClass.porticoOutput(0, "value="+thisValue);

                if(thisKey.equals(defaultTopDisplayValue)) // FTP
                {
					continue;
				}
				else
				{
                    option = new Option();
                    option.setValue(thisKey); // 0, 1, 2(Actual Value)
                    option.setLabel(thisValue); // CD, FTP, E-MAIL(Display value)
		            m_receiptModeList.addOption(option);
			    }
		    }
		}
	}
*/
/* Moved to HelperClass
	private String massageFilePathName(String filePathName)
	{
		char winChar = '\\';
		char unixChar = '/';
		String winString = "\\";
		String regex = "\\w:\\\\"; // Matching pattern like c:\ or z:\
		String massagedString = filePathName;
		String subStr = "";

		if(massagedString.indexOf(winString) != -1)
		{
	    	subStr = massagedString.substring(0,3);
	    	if(subStr.matches(regex))
	    	{
				// Do not remove drive(C:) here 'Prune' will take care later
				// massagedString = massagedString.substring(3);
			}
			massagedString = massagedString.replace(winChar, unixChar);
	    }

        HelperClass.porticoOutput(0, "SubmitBatchContainer - massageFilePathName="+massagedString);
	    return massagedString;
	}
*/
	private String getThisMimeType(String fileNameWithPath)
	{
		String mimetype = "";
		String extn = "";
		try
		{
		    extn = UploadUtil.extractExtension(fileNameWithPath);
		    HelperClass.porticoOutput(0, "SubmitBatchContainer-getThisMimeType(filename:extn)="+fileNameWithPath + ":" +extn);
		    if(m_FileExtnTypeList.size() > 0 && m_FileExtnTypeList.containsKey(extn))
		    {
				mimetype = (String)m_FileExtnTypeList.get(extn);
			}
			else
			{
				mimetype = HelperClass.getMimeTypeInfo(extn);
				m_FileExtnTypeList.put(extn, mimetype);
			}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in SubmitBatchContainer - getThisMimeType="+fileNameWithPath + ":" +extn+":"+e.getMessage());
		}
		finally
		{
		}
		return mimetype;
	    // m_FileExtnType
	}

    private String getParentFolderPath(String parentType)
    {
	    return "/" + HelperClass.getSubmissionAreaName(getDfSession());
	}

    private void initFormatInformation()
    {
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL("SELECT name,description,dos_extension FROM dm_format WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') ORDER BY description");
   		HelperClass.porticoOutput(0, "SubmitBatchContainer - initFormatInformation OPEN IDfCollection");
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
           		HelperClass.porticoOutput(0, "SubmitBatchContainer - initFormatInformation CLOSE IDfCollection");
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

    private static final int FILEPATH_MAXLENGTH = 64;
    private static final int FILEPATH_PREFIX_LENGTH = 3;
    public static final String FILE_SELECTOR_APPLET_CONTROL = "__FILE_SELECTOR_APPLET_CONTROL";
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

    // New stuff
    private DropDownList m_providerList;
	private DropDownList m_profileList;
	private Text m_batchName;
    private ArrayList listProviderUI;
    private String m_strFolderID;
    private Text m_batchDesc;
    //private DropDownList m_receiptModeList;
    //private Text m_batchTrackingNo;
    //private ArrayList listReceiptModeUI;
    private static String DEFAULT_PARENT_FOLDER_PATH = "/Submission"; // In case
    private DocbaseObject m_DocbaseObject;
    private List m_docbaseFormatNames;
    private Map m_extensionMap;
    private Map m_ConfiguredDefaultFormats;
    private ArrayList m_PorticoSubmitBatchItemList;
    private ArrayList m_RequiredAttributes;
    private Hashtable m_FileExtnTypeList;
    private String m_providerName;

    private Label m_contentTypeLabel;
}