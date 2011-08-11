
package org.portico.conprep.ui.qcaction.note;

import java.util.ArrayList;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.formext.component.Component;

public class Note extends Component
{
    public Note()
    {
        m_batchLabel = null;
        m_noteSelectionDropDownList = null;

        m_strObjectId = null;
        m_strObjectName = null;
        m_noteTextList = null;
        m_noteTextString = "";
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId"); // Batch Folder Id
        m_batchLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
        m_noteSelectionDropDownList = (DropDownList)getControl("dropdownlist_note_selection", com.documentum.web.form.control.DropDownList.class);

    	m_noteSelectionDropDownList.setMutable(true);

        initializeCommonData();
        initializeCommonControls();
    }

    public void initializeCommonData()
    {
		try
		{
            m_strObjectName = HelperClass.getObjectName(getDfSession(), m_strObjectId, DBHelperClass.BATCH_TYPE); // getBatchName();
            m_noteTextList = QcHelperClass.getNoteTextList(getDfSession(), m_strObjectId);
// Testing start
/*
            m_noteTextList = new ArrayList();
            String text = "Note text(1) from workitem";
            m_noteTextList.add(text);
            text = "Note text(2) from workitem";
            m_noteTextList.add(text);
            text = "Note text(3) from workitem";
            m_noteTextList.add(text);
            text = "Note text(4) from workitem";
            m_noteTextList.add(text);
*/
// Testing end
	    }
	    catch(Exception e)
	    {
            HelperClass.porticoOutput(0, "Note-initializeCommonData()-Exception=" + e.getMessage());
		}
		finally
		{
		}
	}

    public void initializeCommonControls()
    {
		m_batchLabel.setLabel(m_strObjectName);
   		m_noteSelectionDropDownList.setMutable(true);
   		m_noteSelectionDropDownList.clearOptions();
		Option option = null;
        option = new Option();
        option.setValue("0");
        option.setLabel("Latest");
    	m_noteSelectionDropDownList.addOption(option);
        option = new Option();
        option.setValue("1");
        option.setLabel("All");
    	m_noteSelectionDropDownList.addOption(option);
		m_noteSelectionDropDownList.setValue("0");

        setNoteTextString("0");
	}

    public ArrayList getAllNoteText()
    {
        return m_noteTextList;
	}

	public String getNoteText()
	{
        return m_noteTextString;
	}

	public void onSelectNoteSelection(Control control,ArgumentList args)
	{
	    HelperClass.porticoOutput(0, "Note - onSelectNoteSelection() entered");
	    String selectedValue = m_noteSelectionDropDownList.getValue();
	    HelperClass.porticoOutput(0, "Note - onSelectNoteSelection() entered - value="+selectedValue);
	    setNoteTextString(selectedValue);
	}

	public void setNoteTextString(String selectedValue)
	{
		m_noteTextString = "";
		if(m_noteTextList != null && m_noteTextList.size() > 0)
		{
            if(selectedValue.equals("0")) // Latest
            {
				m_noteTextString = FROM_HEADER + (String)m_noteTextList.get(m_noteTextList.size()-1) + "\n";
				m_noteTextString = m_noteTextString + "----------------------------------------------------" + "\n";
				HelperClass.porticoOutput(0, "Note(single) - setNoteTextString()="+m_noteTextString);
		    }
		    else // All
		    {
				for(int indx=0; indx < m_noteTextList.size(); indx++)
				{
					m_noteTextString = m_noteTextString + FROM_HEADER + (String)m_noteTextList.get(indx) + "\n";
					m_noteTextString = m_noteTextString + "----------------------------------------------------" + "\n";
    				HelperClass.porticoOutput(0, "Note(multi) - setNoteTextString()="+m_noteTextString);
				}
		    }
	    }
	}

    private Label m_batchLabel;
	private DropDownList m_noteSelectionDropDownList;

    private String m_strObjectId;
    private String m_strObjectName;
    private ArrayList m_noteTextList;
    private String m_noteTextString;
    private static final String FROM_HEADER = "From: ";
}