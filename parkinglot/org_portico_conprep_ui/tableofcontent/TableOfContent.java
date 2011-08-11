
package org.portico.conprep.ui.tableofcontent;

import java.util.ArrayList;
import java.util.List;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;

public class TableOfContent extends Component
{
    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public TableOfContent()
    {
        m_batchNameLabel = null;
        m_providerNameLabel = null;
        m_profileNameLabel = null;

        m_strObjectId = "";
        m_strProviderName = "";
        m_strProviderId = "";
        m_strProfileName = "";
        m_strProfileId = "";
        m_TableOfContentList = null;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Object Id

        HelperClass.porticoOutput("TableOfContent-onInit()-Argument Batch Id=" + m_strObjectId);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
	    m_providerNameLabel = (Label)getControl("provider_name", com.documentum.web.form.control.Label.class);
	    m_profileNameLabel = (Label)getControl("profile_name", com.documentum.web.form.control.Label.class);

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
					m_strProviderName = getLookupServiceName("provider", m_strProviderId);
				}
				else if(attrName.equals("p_profile_id"))
				{
                    m_strProfileId = attrValue;
					m_strProfileName = getLookupServiceName("profile", m_strProfileId);
				}
			}
	    }
	    // populate the list of content
	    m_TableOfContentList = HelperClass.getTableOfContent(getDfSession(), m_strObjectId);
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
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE));
		m_providerNameLabel.setLabel(m_strProviderName);
		m_profileNameLabel.setLabel(m_strProfileName);
	}

	public List getTableofContentList()
	{
		return m_TableOfContentList;
	}

	public String getBatchFolderId()
	{
		return m_strObjectId;
	}

// Controls
    private Label m_batchNameLabel;
	private Label m_providerNameLabel;
    private Label m_profileNameLabel;

// Data
    private String m_strObjectId;
    private String m_strProviderName;
    private String m_strProviderId;
    private String m_strProfileName;
    private String m_strProfileId;
    private List m_TableOfContentList;
}
