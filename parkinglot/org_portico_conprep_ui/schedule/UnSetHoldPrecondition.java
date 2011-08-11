
/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Workflow Schedule
 * File           	HoldPrecondition.java
 * Created on 		Dec 8, 2004
 *
 */
package org.portico.conprep.ui.schedule;

import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

/**
 * Description	Precondition for putting a batch 'on hold'
 * Author		pramaswamy
 * Type			HoldPrecondition
 */
public class UnSetHoldPrecondition  implements IActionPrecondition {

	/**
	 *
	 */
	public UnSetHoldPrecondition() {
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.action.IActionPrecondition#queryExecute(java.lang.String, com.documentum.web.formext.config.IConfigElement, com.documentum.web.common.ArgumentList, com.documentum.web.formext.config.Context, com.documentum.web.formext.component.Component)
	 */
	public boolean queryExecute(String action, IConfigElement iConfigElement, ArgumentList argumentlist, Context context, Component component)
	{
		boolean isValid = false;
        isValid = QcHelperClass.isValidUnSetHoldAction(component.getDfSession(),
		                                                        argumentlist.get("objectId"),
		                                                        HelperClassConstants.getAddlnInfo(argumentlist));
		return isValid;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.action.IActionPrecondition#getRequiredParams()
	 */
	public String[] getRequiredParams() {
		return m_requiredArgs;
	}
	private String m_requiredArgs[] = new String[0];

}
