/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	ServiceValueFormatter.java
 * Created on 		Jan 12, 2005
 *
 */
package org.portico.conprep.ui.format;

import java.util.ArrayList;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.form.control.format.ValueFormatter;

/**
 * Description
 * Author		ranga
 * Type			ServiceValueFormatter
 */
public class ServiceValueFormatter extends ValueFormatter
{
	public ServiceValueFormatter()
	{
		m_attrfield = null;
		m_type = null;
	}
	public String getAttrfield()
    {
		return m_attrfield;
	}

	public void setAttrfield(String s)
	{
		m_attrfield = s;
	}

	public void setType(String s)
	{
		m_type = s;
	}

	public String getType()
	{
		return m_type;
	}

	/*
	 * @see com.documentum.web.form.control.format.IValueFormatter#format(java.lang.String)
	 */
	public String format(String attrValue)
	{
		String attrDisp = attrValue;

		if(m_type != null && m_attrfield != null)
		{
		    try
		    {
        		ArrayList outList = null;
        		ValuePair tValuePair = null;
        		ArrayList attrList = new ArrayList();
    	    	attrList.add(m_attrfield);
    	    	HelperClass.porticoOutput("ServiceValueFormatter-Before Call HelperClass.lookupServiceInfo for(servicetype::serviceid)-"+m_type+"::"+attrValue);
    	    	outList = HelperClass.lookupServiceInfo(m_type, attrValue, attrList); // format,id,attrlist("name",...)
    	    	HelperClass.porticoOutput("ServiceValueFormatter-After Call HelperClass.lookupServiceInfo for(servicetype::serviceid)-"+m_type+"::"+attrValue);
    	    	if(outList != null & outList.size() > 0)
    	    	{
                    for(int indx=0; indx < outList.size(); indx++)
                    {
	        		    tValuePair = (ValuePair)outList.get(indx);
	        		    String currentKey = tValuePair.getKey();
	        		    String currentValue = tValuePair.getValue();

	        			HelperClass.porticoOutput("ServiceValueFormatter-(format)list="+currentKey+"::"+currentValue);
	        		    if(currentKey.equals(m_attrfield))
	        		    {
		    				if(currentValue != null && !currentValue.equals(""))
		    				{
  	        		            attrDisp = currentValue;
		    			    }
    	        			HelperClass.porticoOutput("ServiceValueFormatter-(format)list(Matched)="+currentKey+"::"+currentValue);
	        		        break;
		    		    }
	        		}
	        	}
		    }
		    catch(Exception e)
		    {
		    	HelperClass.porticoOutput("ServiceValueFormatter-Exception-"+e.getMessage());
		    }
		    finally
		    {
				m_type = null;
				m_attrfield = null;
	        	if(attrDisp.equals(attrValue))
	        	{
					attrDisp = "["+attrDisp+"]";
				}
		    }
	    }

        HelperClass.porticoOutput("ServiceValueFormatter-Display value="+attrDisp);

		return attrDisp;
	}
	private String m_attrfield;
	private String m_type;
}
