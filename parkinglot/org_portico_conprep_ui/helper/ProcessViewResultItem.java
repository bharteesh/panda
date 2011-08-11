package org.portico.conprep.ui.helper;

public class ProcessViewResultItem
{
    private String thisKey = "";
    private String thisToken = "";
    private String thisObjectType = "";
    private String thisDisplayToken = "";
    private String thisSortKey = "";

    private String parentKey = "";
    private String parentToken = "";
    private String parentObjectType = "";

    private String contentObjectId = "";

    private boolean isErroredItem = false;

    public void setThisToken(String tToken)
    {
		thisToken = tToken;
	}
    public String getThisToken()
    {
		return thisToken;
	}

    public void setThisDisplayToken(String tDisplayToken)
    {
		thisDisplayToken = tDisplayToken;
	}
    public String getThisDisplayToken()
    {
		return thisDisplayToken;
	}

    public void setThisSortKey(String tSortKey)
    {
		thisSortKey = tSortKey;
	}
    public String getThisSortKey()
    {
		return thisSortKey;
	}

    public void setParentToken(String tToken)
    {
		parentToken = tToken;
	}
    public String getParentToken()
    {
		return parentToken;
	}

	public void setIsErroredItem(boolean tIsErrored)
	{
		isErroredItem = tIsErrored;
	}
	public boolean getIsErroredItem()
	{
		return isErroredItem;
	}

    public void setThisKey(String tKey)
    {
		thisKey = tKey;
	}
    public String getThisKey()
    {
		return thisKey;
	}

    public void setParentKey(String tParentKey)
    {
		parentKey = tParentKey;
	}
    public String getParentKey()
    {
		return parentKey;
	}

    public void setThisObjectType(String tObjectType)
    {
		thisObjectType = tObjectType;
	}
    public String getThisObjectType()
    {
		return thisObjectType;
	}

    public void setParentObjectType(String tParentObjectType)
    {
		parentObjectType = tParentObjectType;
	}
    public String getParentObjectType()
    {
		return parentObjectType;
	}

    public void setContentObjectId(String tContentObjectId)
    {
		contentObjectId = tContentObjectId;
	}
    public String getContentObjectId()
    {
		return contentObjectId;
	}
}
