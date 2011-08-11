
package org.portico.conprep.ui.qcaction.addnewfile;

import java.util.Calendar;
import java.util.Date;


public class AddNewFileItem
{
    public AddNewFileItem()
    {
        m_strFolderId = "";
        m_property = "";
        m_category = "";
        m_strFilenameWithPath = "";
    }

    public String getThisDateTime()
    {
        Calendar cal = Calendar.getInstance();
        Date dt = cal.getTime();
        return dt.toString();
	}

	public String getFilePath()
	{
		return m_strFilenameWithPath ;
	}

	public void setFilePath(String filePath)
	{
		m_strFilenameWithPath = filePath;
	}

	public String getXmlCategory()
	{
		return m_category;
	}

	public void setXmlCategory()
	{
	}

	public String getPropertySettings()
	{
		return m_property;
	}

	public void setPropertySettings()
	{
	}

	public String getFolderId()
	{
		return m_strFolderId;
	}

	public void setFolderId(String id)
	{
		m_strFolderId = id;
	}

	public String getType()
	{
		return m_type;
	}
	public void setType(String type)
	{
		m_type = type;
	}

    private String m_strFolderId;
    private String m_strFilenameWithPath;
    private String m_property;
    private String m_category;
    private String m_type;
}