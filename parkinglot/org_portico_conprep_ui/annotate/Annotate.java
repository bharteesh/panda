
package org.portico.conprep.ui.annotate;

import java.util.ArrayList;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.TextArea;
import com.documentum.web.formext.component.Component;

public class Annotate extends Component
{
    public Annotate()
    {
		// Control
        m_batchLabel = null;
        m_annotateText = null;
        m_annotateDropDownList = null;

        // Data
        m_strObjectId = null;
        m_batchAccessionId = null;
        m_strObjectName = null;
        m_annotationList = new ArrayList();
        m_errorString = "";
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Folder Id

        initializeCommonData();
        initializeCommonControls();
    }

	public void onRender()
	{
		m_annotationList = DBHelperClass.getAnnotationList(m_batchAccessionId);
		super.onRender();
	}

    public void initializeCommonData()
    {
		try
		{
            m_strObjectName = HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE); // getBatchName();
            // This will return as ValuePair - key(annotation text), value(create date string)
            m_batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(m_strObjectId);
			m_annotationList = DBHelperClass.getAnnotationList(m_batchAccessionId);
	    }
	    catch(Exception e)
	    {
            HelperClass.porticoOutput(1, "Annotate-initializeCommonData()-Exception=" + e.toString());
		}
		finally
		{
		}
	}

    public void initializeCommonControls()
    {
        m_batchLabel = (Label)getControl(BATCH_NAME, com.documentum.web.form.control.Label.class);
		m_batchLabel.setLabel(m_strObjectName);

		m_annotateText = (TextArea)getControl(ANNOTATE_TEXT_CONTROL, com.documentum.web.form.control.TextArea.class);
        m_annotateDropDownList = (DropDownList)getControl(ANNOTATE_DROPDOWN_CONTROL, com.documentum.web.form.control.DropDownList.class);
     	m_annotateDropDownList.setMutable(true);
		m_annotateDropDownList.clearOptions();

   		Option option = new Option();
	    option.setValue(DEFAULT_ANNOTATION);
        option.setLabel(DEFAULT_ANNOTATION);
        m_annotateDropDownList.addOption(option);

   		option = new Option();
	    option.setValue("Batch deleted");
        option.setLabel("Batch deleted");
        m_annotateDropDownList.addOption(option);

   		option = new Option();
	    option.setValue("Batch reset");
        option.setLabel("Batch reset");
        m_annotateDropDownList.addOption(option);

   		option = new Option();
	    option.setValue("Batch renamed");
        option.setLabel("Batch renamed");
        m_annotateDropDownList.addOption(option);
	}

    public ArrayList getAnnotationList()
    {
        return m_annotationList;
	}

	public String getErrorString()
	{
		return m_errorString ;
	}

	public void onClickAddAnnotation(Control control, ArgumentList argumentlist)
	{
        HelperClass.porticoOutput(0, "Annotate-onClickAddAnnotation");
    	m_errorString = "";
		boolean isValid = validateInputData();
		if(isValid == true)
		{
			isValid = DBHelperClass.addAnnotation(m_batchAccessionId, m_annotateText.getValue());
			if(isValid == false)
			{
		    	m_errorString = "Error detected during Update";
		    }
		    else
		    {
				// Addition was successful, reset the entered field
				// m_annotateText.setValue("");
				// m_annotateDropDownList.setValue(DEFAULT_ANNOTATION);

				// Allow the previuos selected fields intact.
			}
		}
	}

	public boolean validateInputData()
	{
		boolean isValid = true;
		m_errorString = "";
	    String annotateText = m_annotateText.getValue();
	    if(annotateText == null || annotateText.equals(""))
	    {
        	isValid = false;
	    	m_errorString = m_errorString + "Error detected in Validation: No Annotation selected";
	    }

		return isValid;
	}

    // Control
    private Label m_batchLabel;
    private TextArea m_annotateText;
    private DropDownList m_annotateDropDownList;

    // Data
    private String m_strObjectId;
    private String m_batchAccessionId;
    private String m_strObjectName;
    private ArrayList m_annotationList;
    private String m_errorString;

    // Control constants
	public static final String BATCH_NAME = "batch_name";
	public static final String ANNOTATE_DROPDOWN_CONTROL = "annotate_dropdown_control";
	public static final String ANNOTATE_TEXT_CONTROL = "annotate_text_control";

	// Data constants
	public static final String DEFAULT_ANNOTATION = "";

}