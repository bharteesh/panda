/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 * 
 * Project        	ConPrep WebTop
 * Module         	
 * File           	ActionItem.java
 * Created on 		Mar 8, 2005
 * 
 */
package org.portico.conprep.ui.report;

import java.util.Hashtable;

/**
 * Description	Bean Object for each Action Item in ActionMapping.xml 
 * Author		pramaswamy
 * Type			ActionItem
 */
public class OutcomeAction {
	
	private String Context;
	private String Code;
	private String Name;
	private Hashtable actions; 

	/**
	 * @return
	 */
	public Hashtable getActions() {
		return actions;
	}

	/**
	 * @return
	 */
	public String getContext() {
		return Context;
	}

	/**
	 * @return
	 */
	public String getCode() {
		return Code;
	}

	public String getName() {
		return Name;
	}

	/**
	 * @param list
	 */
	public void setActions(Hashtable list) {
		actions = list;
	}

	/**
	 * @param string
	 */
	public void setContext(String string) {
		Context = string;
	}

	/**
	 * @param string
	 */
	public void setCode(String string) {
		Code = string;
	}

	public void setName(String string) {
		Name = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Context=");
		sb.append(Context);
		sb.append(", Code=");
		sb.append(Code);
		sb.append(", actions=");
		if(actions!=null) {
			sb.append(actions.toString());
		}		
		String str = sb.toString();
		sb.delete(0,sb.capacity());
		return str;
	}
}
