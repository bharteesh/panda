
package org.portico.conprep.ui.rename;

import java.util.ArrayList;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;

public class ViewRenamedFiles extends Component
{
    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ViewRenamedFiles()
    {
        m_strObjectId = "";
        m_strProviderName = "";
        m_strProviderId = "";
        m_strProfileName = "";
        m_strProfileId = "";
        m_ViewRenamedFilesList = null;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Object Id

        HelperClass.porticoOutput("ViewRenamedFiles-onInit()-Argument Batch Id=" + m_strObjectId);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void initializeCommonData()
    {
        // Pick current provider id, provider name
   		ValuePair tValuePair = null;
   		ArrayList attrList = new ArrayList();
    	attrList.add("p_provider_id");
    	attrList.add("p_profile_id");
        ArrayList outList = HelperClass.getObjectAttrValues(getDfSession(), DBHelperClass.BATCH_TYPE, m_strObjectId, attrList);
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
					m_strProviderId = attrValue;
					m_strProviderName = attrValue;
				}
				else if(attrName.equals("p_profile_id"))
				{
                    m_strProfileId = attrValue;
					m_strProfileName = attrValue;
				}
			}
	    }
        ArrayList attrListIn = new ArrayList();
        attrListIn.add(DBHelperClass.P_ORIG_FILENAME);
        attrListIn.add(DBHelperClass.P_NEW_WORK_FILENAME);
        attrListIn.add(DBHelperClass.P_CREATED_BY);
        attrListIn.add(DBHelperClass.P_MODIFIED_BY);
        attrListIn.add(DBHelperClass.P_CREATE_TIMESTAMP);
        attrListIn.add(DBHelperClass.P_MODIFY_TIMESTAMP);
	    // populate the list of renamed file(s)
	    m_ViewRenamedFilesList = DBHelperClass.getRenamedFileList(m_strObjectId, DBHelperClass.SU_RENAME_ACTION, attrListIn);
	}

    public void initializeCommonControls()
    {
		m_batchName = HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE);
	}

	public TreeMap getViewRenamedFilesList()
	{
		return m_ViewRenamedFilesList;
	}

	public String getBatchFolderId()
	{
		return m_strObjectId;
	}

	public String getBatchName()
	{
		return m_batchName;
	}

	public String getProviderName()
	{
		return m_strProviderName;
	}

	public String getProfileName()
	{
		return m_strProfileName;
	}

// Controls
    private String m_batchName;
	private String m_providerName;
    private String m_profileName;

// Data
    private String m_strObjectId;
    private String m_strProviderName;
    private String m_strProviderId;
    private String m_strProfileName;
    private String m_strProfileId;
    private TreeMap m_ViewRenamedFilesList;
}
