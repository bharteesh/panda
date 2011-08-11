/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Service
 * File           	ServiceValueFormatterTag.java
 * Created on 		Jan 12, 2005
 *
 */
package org.portico.conprep.ui.format;

import com.documentum.web.form.Control;
import com.documentum.web.form.control.format.ValueFormatterTag;
/**
 * Description
 * Author		ranga
 * Type			ServiceValueFormatterTag
 */
public class ServiceValueFormatterTag extends ValueFormatterTag
{
	/**
	 *
	 */
	public ServiceValueFormatterTag() {
		m_attrfield = null;
		m_type = null;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.form.ControlTag#getControlClass()
	 */
	protected Class getControlClass()
	{
		return org.portico.conprep.ui.format.ServiceValueFormatter.class;
	}
	public void setAttrfield(String s)
	{
		m_attrfield = s;
	}
	public String getAttrfield()
	{
		return m_attrfield;
	}
	public void setType(String s)
	{
		m_type = s;
	}
	public String getType()
	{
		return m_type;
	}

	public void release()
	{
		super.release();
		m_attrfield = null;
		m_type = null;
	}

	protected void setControlProperties(Control control)
	{
		super.setControlProperties(control);
		ServiceValueFormatter serviceValueFormatter = (ServiceValueFormatter)control;
		if(m_attrfield != null && m_attrfield.length() > 0)
			serviceValueFormatter.setAttrfield(m_attrfield);
		if(m_type != null && m_type.length() > 0)
			serviceValueFormatter.setType(m_type);
	}

	private String m_attrfield;
	private String m_type;
}
