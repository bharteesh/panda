
package org.portico.conprep.ui.helper;


public class ProcessViewFilter
{
	String objectType = "";
	String filterType = "";

    public ProcessViewFilter()
    {
    }

    public void setObjectType(String tObjectType)
    {
		objectType = tObjectType;
	}

    public String getObjectType()
    {
		return objectType;
	}

    public void setFilterType(String tFilterType)
    {
		filterType = tFilterType;
	}

    public String getFilterType()
    {
		return filterType;
	}
}