
package org.portico.conprep.ui.metadata.view;

import java.util.Hashtable;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;


public class MetadataView extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public MetadataView()
    {
		m_batchId = null;
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_batchId = args.get("objectId");
        HelperClass.porticoOutput(0, "MetadataView-onInit()-Argument m_batchId=" + m_batchId);
		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
		initializeDataControls();
        Control control = getContainer();
        if(control instanceof Form)
            ((Form)control).setModal(false);
        setModal(false);
    }

    /*
     *The onRender method is called when a page corresponding to the component is displayed.
     *The code below demos that it is called for each page corresponding to the component.
     *Thus, it will be called for the start page viz. search.jsp as well as the page
     *displaying the search results viz. searchResults.jsp. If you want any processing
     *done for a specific page, you can put the code here.
     */
    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void onOk(Control control, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

    public void initializeDataControls()
    {
		m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_batchId, DBHelperClass.BATCH_TYPE));
		metaDataContextMap = DBHelperClass.getMetaDataContextMapping(m_batchId);
		sortedArchivalUnitArticleMap = new TreeMap(DBHelperClass.getArchivalUnitArticleMapping(m_batchId, additionalObjectInfo));
	}

    public Hashtable getMetaDataContextMap()
    {
		return metaDataContextMap;
    }

    public TreeMap getSortedArchivalUnitArticleMap()
    {
		return sortedArchivalUnitArticleMap;
	}

    public Hashtable getAdditionalObjectInfo()
    {
		return additionalObjectInfo;
	}

    private Hashtable metaDataContextMap = new Hashtable();
    private TreeMap sortedArchivalUnitArticleMap = null;
    private Hashtable additionalObjectInfo = new Hashtable();
    private Label m_batchNameLabel;
    private String m_batchId;
    public static final String DMD_MD_TYPE = "DMD";
    public static final String PMD_MD_TYPE = "PMD";
}
