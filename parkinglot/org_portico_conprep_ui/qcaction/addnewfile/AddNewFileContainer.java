
package org.portico.conprep.ui.qcaction.addnewfile;

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
import org.portico.conprep.ui.helper.HelperClassConstants;
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
import com.documentum.web.form.control.Hidden;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataListBox;
import com.documentum.web.form.control.fileselector.FileSelector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.util.Browser;
import com.documentum.webcomponent.library.contentxfer.UploadContainer;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.documentum.webcomponent.library.messages.MessageService;


public class AddNewFileContainer extends UploadContainer
    implements IContentXferServiceListener
{
    public AddNewFileContainer()
    {
        m_appletImport = null;
        m_appletLinkDetector = null;
        m_progress = null;
        m_strContentTicket = null;
        m_setFiles = new TreeSet();
        m_setFiles_MacNav = null;
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
        // m_listboxFiles = null;
        m_listboxPossibleFilePath = null;
        m_listboxMappedCart = null;
        m_hiddenControl = null;

        m_strObjectId = "";
        m_strBatchFolderId = "";
        m_strDesc = null;
        m_strWorkingFilePath = null;
        m_strSelectedSource = "";
        m_strDescValue = "";
        m_sourceList = new ArrayList();
        m_possiblePathList = new TreeSet();
        m_msgObjectList = new ArrayList();
		m_strUserMessageObjectId = "";
		m_strReEntryPoint = "";
		m_fileNamePossibleFilePathMapping = new ArrayList();
		m_strBatchStatus = null;
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
            super.onInit(argumentlist);
            m_strObjectId = argumentlist.get("objectId");

            // Note: This action can be taken from the menu(scope type='p_batch')
            //       OR could also be taken from the problem report(scoped for a p_su_state accession id),
            //       hence have to accomodate for both the cases.
            if(argumentlist.get("accessionId") != null)
            {
				m_strObjectId = argumentlist.get("accessionId");
			}

            // From report only
            m_strUserMessageObjectId = argumentlist.get("msgObjectId"); // List of Message object Id
            m_strReEntryPoint = argumentlist.get("reEntryPoint"); // WorkFlow reEntryPoint

            HelperClass.porticoOutput("AddNewFileContainer - onInit ArgumentList-m_strObjectId="+ m_strObjectId);
            HelperClass.porticoOutput("AddNewFileContainer - onInit ArgumentList-m_strUserMessageObjectId="+ m_strUserMessageObjectId);
            HelperClass.porticoOutput("AddNewFileContainer - onInit ArgumentList-m_strReEntryPoint="+ m_strReEntryPoint);

            m_appletImport = (ImportApplet)getControl("importapplet", com.documentum.web.contentxfer.control.ImportApplet.class);
            m_appletLinkDetector = (LinkDetectorApplet)getControl("linkdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
            m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
            // m_listboxFiles = (DataListBox)getControl("filelist", com.documentum.web.form.control.databound.DataListBox.class);
            // m_listboxFiles.setMutable(true);
            m_hiddenControl = (Hidden)getControl("hiddenselectedfile", com.documentum.web.form.control.Hidden.class);
            m_hiddenControl.setValue("");


            m_listboxPossibleFilePath = (DataListBox)getControl("possiblefilelist", com.documentum.web.form.control.databound.DataListBox.class);
            m_listboxPossibleFilePath.setMutable(true);

            m_listboxMappedCart = (DataListBox)getControl("cartmappedfilelist", com.documentum.web.form.control.databound.DataListBox.class);
            m_listboxMappedCart.setMutable(true);

            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(true);
            getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(false);

            Panel panel = (Panel)getControl("IEButtons", com.documentum.web.form.control.Panel.class);
            Panel panel1 = (Panel)getControl("NSButtons", com.documentum.web.form.control.Panel.class);
            panel.setVisible(isBrowserIE());
            panel1.setVisible(isBrowserNetscape());

            // New stuff
            m_batchName = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
            m_providerName = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
            m_strDesc = (Text)getControl("desc", com.documentum.web.form.control.Text.class);
            m_strWorkingFilePath = (Text)getControl("userdefinedworkfilepath", com.documentum.web.form.control.Text.class);

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
		if(HelperClass.getObjectType(getDfSession(), m_strObjectId).equals(DBHelperClass.BATCH_TYPE))
		{
			m_strBatchFolderId = m_strObjectId;
		}
		else
		{
            m_strBatchFolderId = HelperClass.getParentBatchFolderId(getDfSession(), m_strObjectId);
	    }

		m_strBatchStatus = HelperClass.getStatusForBatchObject(getDfSession(), m_strBatchFolderId);

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
	    // PMD2.0
	    m_sourceList = HelperClass.getAddNewFileActionKeyMetadataOriginList();

		if(m_strBatchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
		{
            m_possiblePathList = QcHelperClass.getPossibleFilePathsForAdditionFromRawunits(getDfSession(), getFolderObjectId());
		}
		else
		{
			// This is from the working file name(s) on the SU(s)
            m_possiblePathList = QcHelperClass.getPossibleFilePathsForAddition(getDfSession(), getFolderObjectId());
	    }
        m_possiblePathList.add(AddNewFileContainer.BLANKPATHDIRECTBATCH);
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

	public ArrayList getSourceList()
	{
		return m_sourceList;
	}

	public TreeSet getPossiblePathList()
	{
		return m_possiblePathList;
	}

	// This returns the list of file(s) added from the previous page
	public ArrayList getFileNameList()
	{
		return m_fileNamePossibleFilePathMapping;
	}

	public boolean onNextPage()
	{
        boolean flag = false;
        String s = getComponentPage();
        if(s.equals("addnewfileSubmitInit"))
        {
            flag = checkInitPageInfo();
         	if(flag == true)
         	{
  		    	setComponentPage("addnewfilemapping");
                getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
                getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(true);
                populateSecondPageInfo();
         	}
  		}

        return flag;
	}

    public boolean checkInitPageInfo()
    {
		boolean isValidFiles = true;
        FileSelector fileselector = (FileSelector)getControl(AddNewFileContainer.ADDNEWFILE_FILE_SELECTOR_APPLET_CONTROL);
        String as[] = fileselector.getFiles();
        if(as == null || as.length == 0)
        {
	        HelperClass.porticoOutput(0, "AddNewFileContainer - checkInitPageInfo - No files selected");
            setReturnError("MSG_FILES_NOT_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_FILES_NOT_SELECTED", null);
            isValidFiles = false;
		}

        if(isValidFiles == true)
        {
			m_fileNamePossibleFilePathMapping.clear();
			for(int asIndx=0; asIndx < as.length; asIndx++)
			{
				ArrayList alist = new ArrayList();
	   	    	ValuePair valuePair = new ValuePair();
	   	    	valuePair.setKey(AddNewFileContainer.ADDEDFILENAME);
	   	    	valuePair.setValue((String)as[asIndx]);
	   	    	alist.add(valuePair);
				m_fileNamePossibleFilePathMapping.add(alist);
			}
            if(macOS9Nav())
            {
                as = putXmlFirst(as);
                m_setFiles_MacNav = as;
            }
            m_setFiles.addAll(Arrays.asList(as));
            int i = m_setFiles.size();
            String s2 = ""; // getFolderObjectId(); will be populated in importUpload();
            m_PorticoSubmitBatchItemList.clear();
            AddNewFileItem tItem = null;
            if(m_setFiles_MacNav == null)
            {
                Iterator iterator = m_setFiles.iterator();
                for(int k = 0; k < i; k++)
                {
   					tItem = new AddNewFileItem();
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
   					tItem = new AddNewFileItem();
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
				String radioName = AddNewFileContainer.SOURCERADIOLABEL_PREFIX+index;
				Radio radio_level = (Radio)getControl(radioName, com.documentum.web.form.control.Radio.class);
				if(radio_level != null && radio_level.getValue())
				{
                    m_strSelectedSource = ((ValuePair)m_sourceList.get(index)).getKey();
					break;
				}
			}
		}
	}

	public void populateSecondPageInfo()
	{
		updateAddedFilesAndCartListBox();
		updatePossibleFilePathListBox("");
	}

	public void updateAddedFilesAndCartListBox()
	{
		HelperClass.porticoOutput("AddNewFileContainer - updateAddedFilesAndCartListBox- Start");
        // m_listboxFiles.setMutable(true);
        // m_listboxFiles.clearOptions();
        // m_listboxFiles.setValue("");
        m_hiddenControl.setValue("");
// Check if jsp select/option to be cleared on onbody init
        m_listboxMappedCart.setMutable(true);
        m_listboxMappedCart.clearOptions();
        Option option;
		if(m_fileNamePossibleFilePathMapping != null && m_fileNamePossibleFilePathMapping.size() > 0)
		{
			for(int dindx=0; dindx < m_fileNamePossibleFilePathMapping.size(); dindx++)
			{
				String addedFileName = "";
				String possibleFilePath = "";
				ArrayList alist = (ArrayList)m_fileNamePossibleFilePathMapping.get(dindx);
				if(alist != null && alist.size() > 0)
				{
					for(int aindx=0; aindx < alist.size(); aindx++)
					{
			        	ValuePair valuePair = (ValuePair)alist.get(aindx);
			        	String key = valuePair.getKey();
			        	String value = valuePair.getValue();
			        	if(key.equals(AddNewFileContainer.ADDEDFILENAME))
			        	{
							addedFileName = value;
						}
						else if(key.equals(AddNewFileContainer.ADDEDPOSSIBLEFILEPATH))
						{
							possibleFilePath = value;
						}
			        }
			    }
			    if(addedFileName != null && !addedFileName.equals(""))
			    {
                     option = new Option();

                     if(possibleFilePath != null && !possibleFilePath.equals(""))
                     {
                         option.setValue(addedFileName);
                         option.setLabel(HelperClass.getFileNameFromAbsoluteName(addedFileName) + "--" + possibleFilePath);
						 m_listboxMappedCart.addOption(option);
					 }
                     else
                     {
						 // files to be added lsit is populated in the jsp (non WDK listbox)
				     }
				}
			}
		}
		HelperClass.porticoOutput("AddNewFileContainer - updateAddedFilesAndCartListBox- End");
	}

	public void updatePossibleFilePathListBox(String possibleFilePathIn)
	{
		HelperClass.porticoOutput("AddNewFileContainer - updatePossibleFilePathListBox- Start");
		m_listboxPossibleFilePath.setMutable(true);
		m_listboxPossibleFilePath.clearOptions();
        Option option;
        if(m_possiblePathList != null && m_possiblePathList.size() > 0)
        {
            Iterator tIterate = m_possiblePathList.iterator();
            while(tIterate.hasNext())
            {
				String possibleFilePath = (String)tIterate.next();
			    if(possibleFilePath != null && !possibleFilePath.equals(""))
			    {
                     option = new Option();
                     option.setValue(possibleFilePath);
                     option.setLabel(possibleFilePath);

                     if(possibleFilePath != null && !possibleFilePath.equals(""))
                     {
						 m_listboxPossibleFilePath.addOption(option);
					 }
				}
            }

            // Note: This possibleFilePathIn would have been added as part of the options in 'm_listboxPossibleFilePath'
    		if(possibleFilePathIn != null && !possibleFilePathIn.equals(""))
			{
				m_listboxPossibleFilePath.setValue(possibleFilePathIn);
			}
		}
		HelperClass.porticoOutput("AddNewFileContainer - updatePossibleFilePathListBox- End");
	}

	// End of First page info

	public void onMapFileWithPossibleFilePath(Control control, ArgumentList argumentlist)
	{

		String selectedFileName = ""; //m_listboxFiles.getValue(); // use trim() if required

		if(m_hiddenControl != null)
		{
		    HelperClass.porticoOutput(0, "AddNewFileContainer- onMapFileWithPossibleFilePath()-hiddenControl NOT NULL");
		    selectedFileName = m_hiddenControl.getValue();
			HelperClass.porticoOutput(0, "AddNewFileContainer- onMapFileWithPossibleFilePath()-hiddenControl.getValue()="+selectedFileName);
        }
        else
        {
		    HelperClass.porticoOutput(1, "AddNewFileContainer- onMapFileWithPossibleFilePath()-hiddenControl IS NULL");
		}

		String selectedPossibleFilePath = m_listboxPossibleFilePath.getValue();

		HelperClass.porticoOutput(0, "AddNewFileContainer - onMapFileWithPossibleFilePath-selectedFileName="+selectedFileName);
		HelperClass.porticoOutput(0, "AddNewFileContainer - onMapFileWithPossibleFilePath-selectedPossibleFilePath="+selectedPossibleFilePath);

		if(selectedFileName == null || selectedFileName.equals(""))
		{
		    HelperClass.porticoOutput(0, "AddNewFileContainer - onMapFileWithPossibleFilePath - FILE not selected");
            setReturnError("MSG_ERROR_NO_FILE_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_NO_FILE_SELECTED", null);
		}
		else if(selectedPossibleFilePath == null || selectedPossibleFilePath.equals(""))
		{
		    HelperClass.porticoOutput(0, "AddNewFileContainer - onMapFileWithPossibleFilePath - POSSIBLE FILE PATH not selected");
            setReturnError("MSG_ERROR_NO_POSSIBLE_FILE_PATH_SELECTED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_NO_POSSIBLE_FILE_PATH_SELECTED", null);
		}
		else
		{
			ArrayList selectedFilesList = getSelectedFilesListFromHiddenValue(selectedFileName);
			ArrayList indexList = new ArrayList();
        	if(m_fileNamePossibleFilePathMapping != null && m_fileNamePossibleFilePathMapping.size() > 0)
    		{
			    for(int dindx=0; dindx < m_fileNamePossibleFilePathMapping.size(); dindx++)
			    {
	            	String key = "";
	            	String value = "";
			    	ArrayList alist = (ArrayList)m_fileNamePossibleFilePathMapping.get(dindx);
			    	if(alist != null && alist.size() > 0)
			    	{
			    		for(int aindx=0; aindx < alist.size(); aindx++)
			    		{
			            	ValuePair valuePair = (ValuePair)alist.get(aindx);
			            	key = valuePair.getKey();
			            	value = valuePair.getValue();
			            	if(key.equals(AddNewFileContainer.ADDEDFILENAME))
			            	{
			    				if(selectedFilesList.contains(value))
			    				{
            						indexList.add(dindx);
			    			    }
								break;
			    			}
			            }
			        }
			    }

			    if(selectedFilesList != null && selectedFilesList.size() > 0 &&
			           indexList != null && indexList.size() > 0 &&
			           selectedFilesList.size() == indexList.size())
		        {
					for(int rindx=0; rindx < indexList.size(); rindx++)
					{
			        	ArrayList alist = new ArrayList();
			        	ValuePair valuePair = new ValuePair();
			        	valuePair.setKey(AddNewFileContainer.ADDEDFILENAME);
			        	valuePair.setValue((String)selectedFilesList.get(rindx));
			        	alist.add(valuePair);

                        valuePair = new ValuePair();
			        	valuePair.setKey(AddNewFileContainer.ADDEDPOSSIBLEFILEPATH);
			        	valuePair.setValue(selectedPossibleFilePath);
			        	alist.add(valuePair);

			        	m_fileNamePossibleFilePathMapping.set(((Integer)indexList.get(rindx)).intValue(), alist); // intValue()
                    }
			    }
			    else
			    {
				    HelperClass.porticoOutput(1, "AddNewFileContainer- onMapFileWithPossibleFilePath()-List MISMATCH selectedFilesList,indexList");
				}
		    }
    		updateAddedFilesAndCartListBox();
		}
	}

	public ArrayList getSelectedFilesListFromHiddenValue(String hiddenSelectedFileName)
	{
		ArrayList filelist = new ArrayList();

		StringTokenizer stringTokenizer = new StringTokenizer(hiddenSelectedFileName, AddNewFileContainer.HIDDENVALUESEPARATOR);

		while(stringTokenizer.hasMoreTokens())
		{
			String filename = (String)stringTokenizer.nextToken();
			filelist.add(filename);
			HelperClass.porticoOutput(0, "AddNewFileContainer- getSelectedFilesListFromHiddenValue()-hiddenControl parsed single filename"+filename);
		}

		return filelist;
	}

    // Second page information
    public boolean checkSecondPageInfo()
    {
		boolean isValid = true;
		isValid = checkIfAllFilesMapped();

		if(isValid == true)
		{
		    m_strDescValue = m_strDesc.getValue().trim();
		    if(m_strDescValue == null || m_strDescValue.equals(""))
		    {
		    	isValid = false;
		        HelperClass.porticoOutput(0, "AddNewFileContainer - checkSecondPageInfo - m_strDescValue not entered");
                setReturnError("MSG_ERROR_NO_DESC_ENTERED", null, null);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_NO_DESC_ENTERED", null);
		    }
	    }

		return isValid;
	}

	public boolean checkIfAllFilesMapped()
	{
		boolean isValid = true;
       	if(m_fileNamePossibleFilePathMapping != null && m_fileNamePossibleFilePathMapping.size() > 0)
   		{
		    for(int dindx=0; dindx < m_fileNamePossibleFilePathMapping.size(); dindx++)
		    {
				String fileName = "";
				String possibleFilePath = "";
		    	ArrayList alist = (ArrayList)m_fileNamePossibleFilePathMapping.get(dindx);
		    	if(alist != null && alist.size() > 0)
		    	{
		    		for(int aindx=0; aindx < alist.size(); aindx++)
		    		{
		            	ValuePair valuePair = (ValuePair)alist.get(aindx);
		            	String key = valuePair.getKey();
		            	String value = valuePair.getValue();
		            	if(key.equals(AddNewFileContainer.ADDEDFILENAME))
		            	{
							fileName = value;
						}
		            	else if(key.equals(AddNewFileContainer.ADDEDPOSSIBLEFILEPATH))
		            	{
							possibleFilePath = value;
						}
		            }
		        }

		        if(fileName == null || fileName.equals("") || possibleFilePath == null || possibleFilePath.equals(""))
		        {
					isValid = false;
        		    HelperClass.porticoOutput(0, "AddNewFileContainer - checkIfAllFilesMapped - Not all files have been mapped");
                    setReturnError("MSG_ERROR_NOT_ALL_FILES_MAPPED", null, null);
                    ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_NOT_ALL_FILES_MAPPED", null);
			    	break;
			    }
		    }
		}

    	return isValid;
	}

    public void onOk(Control control, ArgumentList argumentlist)
    {
        HelperClass.porticoOutput("AddNewFileContainer - onOk before validate()");
        validate(); // documentum validate
        HelperClass.porticoOutput("AddNewFileContainer - onOk after validate()");
        HelperClass.porticoOutput("AddNewFileContainer - onOk before getIsValid()");
        boolean flag = getIsValid();
        HelperClass.porticoOutput("AddNewFileContainer - onOk after getIsValid()="+flag);
        HelperClass.porticoOutput("AddNewFileContainer - onOk before canCommitChanges()");
        boolean flag1 = canCommitChanges();
        HelperClass.porticoOutput("AddNewFileContainer - onOk after canCommitChanges()="+flag1);
		boolean isValidFiles = checkSecondPageInfo();
        if(isValidFiles)
        {
            if(flag && flag1 && onCommitChanges())
            {
   		    	HelperClass.porticoOutput("AddNewFileContainer - onOk before importUpload()");
                importUpload("");
   		    	HelperClass.porticoOutput("AddNewFileContainer - onOk after importUpload()");
   		    }
   		    else
   		    {
   		    	HelperClass.porticoOutput("AddNewFileContainer - onOk before inAutoCommit()");
                if((!flag || !flag1) && inAutoCommit())
                {
       	    		HelperClass.porticoOutput("AddNewFileContainer - onOk before stopAutoCommit()");
                    stopAutoCommit();
   		    	}
   		    	HelperClass.porticoOutput("AddNewFileContainer - onOk after inAutoCommit()");
   		    }
	    }
	    else
	    {
			HelperClass.porticoOutput("AddNewFileContainer - onOk - Do nothing.................");
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
        AddNewFileComponent importcontent = (AddNewFileComponent)getContainedComponent();
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
            HelperClass.porticoOutput(1, "Exception in AddNewFileContainer - startImport="+exception.getMessage());
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
		HelperClass.porticoOutput("AddNewFileContainer - finished...........");
        IDfImportOperation idfimportoperation = (IDfImportOperation)contentxferserviceevent.getOperation();
        if(idfimportoperation != null)
        {
            Object obj = null;
            Object obj1 = null;
            Object obj2 = null;
            IDfFolder tIDfFolder = null;
            String folderObjectName = null;
// CONPREP-2351, PMD2.0, not used any more, refer to 'p_user_added'
/*
            String addNewFileReasonValue =
                String.valueOf(IContentSourceReason.REASON_ADDED);
*/

            try
            {
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
                        String localFileName = iDfSysObject.getObjectName();
						String possibleFilePath = "";

						if(m_fileNamePossibleFilePathMapping != null && m_fileNamePossibleFilePathMapping.size() > 0)
						{
							for(int dindx=0; dindx < m_fileNamePossibleFilePathMapping.size(); dindx++)
							{
								ArrayList alist = (ArrayList)m_fileNamePossibleFilePathMapping.get(dindx);
								if(alist != null && alist.size() > 0)
								{
									String localFileNameMapping = "";
									String possibleFilePathMapping = "";
									for(int aindx=0; aindx < alist.size(); aindx++)
									{
							        	ValuePair valuePair = (ValuePair)alist.get(aindx);
							        	if(valuePair.getKey().equals(AddNewFileContainer.ADDEDFILENAME))
							        	{
											localFileNameMapping = valuePair.getValue();
										}
										else if(valuePair.getKey().equals(AddNewFileContainer.ADDEDPOSSIBLEFILEPATH))
										{
											possibleFilePathMapping = valuePair.getValue();
										}
							        }

							        if(localFileName.equals(localFileNameMapping))
							        {
										possibleFilePath = possibleFilePathMapping;
										break;
									}
							    }
							}
						}

						String newObjectName = HelperClass.getFileNameFromAbsoluteName(iDfSysObject.getObjectName());

						if(possibleFilePath.equals(AddNewFileContainer.BLANKPATHDIRECTBATCH))
						{
							// Leave the newObjectName as it is, it has to be attached directly to the Batch
						}
						else
						{
						    newObjectName = possibleFilePath + "/" + newObjectName;
					    }

						iDfSysObject.setObjectName(newObjectName);
						// May not be required, we prepend the filepath, which is already massaged during batch submission
						/*
                        String massagedObjectName = HelperClass.massageFilePathName(iDfSysObject.getObjectName());
						iDfSysObject.setObjectName(massagedObjectName);
						*/
                        // Future
                        //iDfSysObject.setString("p_filename",massagedObjectName);


                        // CONPREP-2351, PMD2.0, p_source is dropped
                        //iDfSysObject.setString("p_source",m_strSelectedSource);

                        // CONPREP-2096
                        if(m_strBatchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
                        {
							// Do not treat it as a true qcaction, ie. it must not be treated as normal 'AddNewFile' during
							// a qcaction. Otherwise the Initialize Batch activity will delete this file.
						}
						else
						{
                            // CONPREP-2351, PMD2.0, 'p_reason' is replaced by 'p_user_added'
                            // iDfSysObject.setString("p_reason",addNewFileReasonValue);
                            iDfSysObject.setBoolean("p_user_added", true);
					    }
                        iDfSysObject.save();
                    }
                    else
                    {
                        m_strNewObjectIds[i] = null;
                    }
                }


                // Post processing - BatchId, newRawUnitId
                boolean isSuccessful = false;
                try
                {
    				if(m_strBatchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
    				{
    					isSuccessful = true;
    				}
    				else
    				{
                        isSuccessful = QcHelperClass.postProcessingForAddNewFile(getDfSession(), getFolderObjectId(), m_strNewObjectIds, getMsgObjectList(), m_strDescValue, m_strObjectId);
				    }
			    }
			    catch(Exception e)
			    {
                    HelperClass.porticoOutput(1, "Exception in AddNewFileContainer - finished-postProcessingForAddNewFile="+e.getMessage());
				}
				finally
				{
                    if(isSuccessful == true)
                    {
                        String rawUnitCount = ""+HelperClass.getAssetCountForBatchObject(getDfSession(), m_strBatchFolderId);
                        HelperClass.porticoOutput(0, "AddNewFileContainer - finished - Start setting p_rawunit_count="+rawUnitCount);
                        tIDfFolder.setString("p_rawunit_count", rawUnitCount);
                        HelperClass.porticoOutput(0, "AddNewFileContainer - finished - End setting p_rawunit_count="+rawUnitCount);
                        tIDfFolder.save();

                        setReturnValue("newObjectIds", m_strNewObjectIds);
                        setReturnValue("success", "true");
                        MessageService.addMessage(this, "MSG_ADDNEWFILE_SUCCESS");
				    }
				    else
				    {
				    	ArrayList rawunitIdList = new ArrayList();
				    	rawunitIdList.add(m_strNewObjectIds[0]);
				    	QcHelperClass.cleanUp(getDfSession(), rawunitIdList);
                        setReturnValue("newObjectIds", null);
                        setReturnValue("success", "false");
                        ErrorMessageService.getService().setNonFatalError(this, "MSG_ADDNEWFILE_FAILED", null);
				    }
				}
            }
            catch(Exception exception)
            {
                setReturnError("MSG_ERROR_IMPORT", null, exception);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
                HelperClass.porticoOutput(1, "Exception in AddNewFileContainer - finished="+exception.getMessage());
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
        setComponentPage("addnewfileSubmitInit"); // changed from "fileselection"
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
            HelperClass.porticoOutput("AddNewFileContainer - importUpload processing within arraylist");
            AddNewFileItem importcontent = (AddNewFileItem)m_PorticoSubmitBatchItemList.get(i);
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
        HelperClass.porticoOutput("AddNewFileContainer - importUpload before setComponentPage(importupload)");
        setComponentPage("importupload");
    }

/*
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
*/

    public String getFolderObjectId()
    {
		return m_strBatchFolderId;
	}


    // Methods from corresponding component
    private void initFormatInformation()
    {
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL("SELECT name,description,dos_extension FROM dm_format WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') ORDER BY description");
   		HelperClass.porticoOutput("AddNewFileContainer - initFormatInformation OPEN IDfCollection");
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
           		HelperClass.porticoOutput("AddNewFileContainer - initFormatInformation CLOSE IDfCollection");
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

    // JIRA - CONPREP-878 UI - Ability to add a file to user defined path.
    // Pick(Read) the working_file_path and add to the 'm_possiblePathList' list
    // update 'updatePossibleFilePathListBox' and set the selected possible path to this latest added
    //        path if any
    public void onAddNewFU(Control control, ArgumentList argumentlist)
    {
	    String workingFilePath = m_strWorkingFilePath.getValue().trim();
	    if(workingFilePath == null || workingFilePath.equals(""))
	    {
		}
		else
		{
			if(workingFilePath.endsWith("/"))
			{
				workingFilePath = workingFilePath.substring(0, workingFilePath.lastIndexOf("/"));
			}
			if(workingFilePath.startsWith("/"))
			{
				workingFilePath = workingFilePath.substring(1);
			}
		}

		// Check again after above manipulations
	    if(workingFilePath == null || workingFilePath.equals(""))
	    {
            HelperClass.porticoOutput(0, "AddNewFileContainer - onAddNewFU - Invalid workingFilePath="+workingFilePath);
            setReturnError("MSG_ERROR_INVALID_WORKING_FILE_PATH_ENTERED", null, null);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_INVALID_WORKING_FILE_PATH_ENTERED", null);
	    }
	    else
	    {
			// update only if it is a new file path not already existing
			if(m_possiblePathList != null && m_possiblePathList.size() > 0 && !m_possiblePathList.contains(workingFilePath))
			{
				HelperClass.porticoOutput(0, "AddNewFileContainer - onAddNewFU - workingFilePath="+workingFilePath);
				m_possiblePathList.add(workingFilePath);
				updatePossibleFilePathListBox(workingFilePath);
			}
		}
	}

    public static final String FILE_BROWSER_APPLET_CONTROL = "__FILE_BROWSER_APPLET_CONTROL";
    public static final String ADDNEWFILE_FILE_SELECTOR_APPLET_CONTROL = "__ADDNEWFILE_FILE_SELECTOR_APPLET_CONTROL";
    private ImportApplet m_appletImport;
    private LinkDetectorApplet m_appletLinkDetector;
    private ServiceProgressFeedback m_progress;
    private String m_strContentTicket;
    private TreeSet m_setFiles;
    private String m_setFiles_MacNav[];
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
    private Text m_strWorkingFilePath; // This is the path the newly added file for newFU would be added to
    // private DataListBox m_listboxFiles;
    private DataListBox m_listboxPossibleFilePath;
    private DataListBox m_listboxMappedCart;
    private Hidden m_hiddenControl;

    // Data
    private String m_strObjectId;
    private String m_strSelectedSource;
    private String m_strBatchFolderId;
    private String m_strProviderName;
    private String m_strDescValue;
    private ArrayList m_sourceList;
    private TreeSet m_possiblePathList;
    private String m_strUserMessageObjectId = "";
    private ArrayList m_msgObjectList;
	private String m_strReEntryPoint = "";
	private ArrayList m_fileNamePossibleFilePathMapping;
	private String m_strBatchStatus;

    // Check separator in jsp too
    public static String HIDDENVALUESEPARATOR = "|";
    public static String ADDEDFILENAME = "addedfilename";
    public static String ADDEDPOSSIBLEFILEPATH = "addedpossiblefilepath";

    public static String COMMA_SEPARATOR = ",";
    // public static String FILEBROWSER_PREFIX = "filebrowse";
    // public static String FILEBROWSERLABEL_PREFIX = "filebrowselabel";
    public static String SOURCERADIOLABEL_PREFIX = "sourcelabel";
    public static String POSSIBLEFILEPATHRADIOLABEL_PREFIX = "possiblefilepathlabel";
    public static String BLANKPATHDIRECTBATCH = "-- Map directly to Batch --";
}
