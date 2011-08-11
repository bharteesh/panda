
package org.portico.conprep.ui.helper;


public class MetadataUnitUI
{
    private String objectID = "";
    private String objectType = "";
    private String objectName = "";
    private String objectValue = "";

    public MetadataUnitUI()
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
	public void setObjectValue(String value)
	{
		objectValue = value;
	}
	public String getObjectValue()
	{
		return objectValue;
	}
}
