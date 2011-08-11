/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 * 
 * Project        	ConPrep WebTop
 * Module         	Format
 * File           	DdValueFormatterTag.java
 * Created on 		Dec 29, 2004
 * 
 */
package org.portico.conprep.ui.format;

import com.documentum.web.form.Control;
import com.documentum.web.form.control.format.ValueFormatterTag;
/**
 * Description	
 * Author		pramaswamy
 * Type			DdValueFormatterTag
 */
public class DdValueFormatterTag extends ValueFormatterTag {

	/**
	 * 
	 */
	public DdValueFormatterTag() {
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.form.ControlTag#getControlClass()
	 */
	protected Class getControlClass() {
		return org.portico.conprep.ui.format.DdValueFormatter.class;
	}
	public void setAttrfield(String s) {
		m_attrfield = s;
	}
	public String getAttrfield() {
		return m_attrfield;
	}
	public void setType(String s) {
		m_type = s;
	}
	public String getType() {
		return m_type;
	}

	public void release(){
		super.release();
		m_attrfield = null;
		m_type = null;
	}	
	
	protected void setControlProperties(Control control)
	{
		super.setControlProperties(control);
		DdValueFormatter ddValueFormatter = (DdValueFormatter)control;
		if(m_attrfield != null && m_attrfield.length() > 0)
			ddValueFormatter.setAttrfield(m_attrfield);
		if(m_type != null && m_type.length() > 0)
			ddValueFormatter.setType(m_type);
	}

	private String m_attrfield;
	private String m_type;
}
