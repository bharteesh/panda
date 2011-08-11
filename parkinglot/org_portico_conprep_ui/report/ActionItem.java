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
public class ActionItem {
	
	private String objType;
	private String procCode;
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
	public String getObjType() {
		return objType;
	}

	/**
	 * @return
	 */
	public String getProcCode() {
		return procCode;
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
	public void setObjType(String string) {
		objType = string;
	}

	/**
	 * @param string
	 */
	public void setProcCode(String string) {
		procCode = string;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("objType=");
		sb.append(objType);
		sb.append(", procCode=");
		sb.append(procCode);
		sb.append(", actions=");
		if(actions!=null) {
			sb.append(actions.toString());
		}		
		String str = sb.toString();
		sb.delete(0,sb.capacity());
		return str;
	}
}
