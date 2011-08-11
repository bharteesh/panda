
package org.portico.conprep.ui.helper;

import java.util.ArrayList;

public class MetadataStructureUI
{
    private String objectID = "";
    private String objectType = "";
    private String objectName = "";
    private String objectSortKey = "";
    private ArrayList objectValue = new ArrayList();

    public MetadataStructureUI()
    {
	}
	public void setObjectID(String objID)
	{
		this.objectID = objID;
	}
	public String getObjectID()
	{
		return objectID;
	}

	public void setObjectType(String objType)
	{
		this.objectType = objType;
	}
	public String getObjectType()
	{
		return objectType;
	}

	public void setObjectName(String objName)
	{
		this.objectName = objName;
	}
	public String getObjectName()
	{
		return objectName;
	}

	public void setObjectSorkKey(String objSortKey)
	{
		this.objectSortKey = objSortKey;
	}
	public String getObjectSortKey()
	{
		return objectSortKey;
	}

	public void setObjectValue(Object value)
	{
		objectValue.add(value);
	}
	public void setObjectValue(ArrayList list)
	{
		for(int i=0; i < list.size(); i++)
		{
	    	objectValue.add(list.get(i));
	    }
	}
	public ArrayList getObjectValue()
	{
		return objectValue;
	}
}
