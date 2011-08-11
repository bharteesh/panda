/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	DdValueFormatter.java
 * Created on 		Dec 29, 2004
 *
 */
package org.portico.conprep.ui.format;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.format.ValueFormatter;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
/**
 * Description
 * Author		pramaswamy
 * Type			DdValueFormatter
 */
public class DdValueFormatter extends ValueFormatter {

	public DdValueFormatter() {
	}
//	public String getAttrfield() {
//		return m_attrfield;
//	}

	public void setAttrfield(String s) {
		m_attrfield = s;
	}

	public void setType(String s) {
		m_type = s;
	}

	/*
	 * @see com.documentum.web.form.control.format.IValueFormatter#format(java.lang.String)
	 */
	public String format(String attrValue) {
		String attrDisp = attrValue;
		IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
		IDfSession idfsession = null;
		int findIndex = -1;
		try
		{
			idfsession = idfsessionmanager.getSession(SessionManagerHttpBinding.getCurrentDocbase());
			IDfTypedObject iDfTypedObject = idfsession.getTypeDescription(m_type,m_attrfield,null,null);
			HelperClass.porticoOutput(0, "attrValue="+ attrValue);

            if(m_type!=null && m_type.trim().length()>0 && attrValue != null && attrValue.trim().length()>0)
            {
        		if(iDfTypedObject.hasAttr("map_data_string") && iDfTypedObject.hasAttr("map_display_string"))
        		{
    				if(m_attrfield.equals("p_receipt_mode"))
    				{
            			findIndex = iDfTypedObject.findInt(
          							"map_data_string",
        							(int)Double.parseDouble(attrValue));
    				}
    				else
    				{
            			findIndex = iDfTypedObject.findString(
        							"map_data_string",
        							attrValue);
    				}
        			HelperClass.porticoOutput(0, "findIndex="+ findIndex);
    				attrDisp = iDfTypedObject.getRepeatingString("map_display_string", findIndex);
    			}
		    }
		    else
		    {
				attrDisp = 	attrValue;
			}
		}
		catch(DfException dfexception)	{
			throw new WrapperRuntimeException("Failed to display values from docbase.", dfexception);
		}
		finally	{
			if(idfsession != null)
				idfsessionmanager.release(idfsession);
		}

		HelperClass.porticoOutput(0, "attrValue,attrDisp="+ attrValue +","+attrDisp);
		return attrDisp;
	}
	private String m_attrfield;
	private String m_type;
}
