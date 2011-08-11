

/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	objectversion
 * File           	VersionPrecondition.java
 * Created on 		Dec 7, 2004
 *
 */

package org.portico.conprep.ui.objectversion;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.FolderUtil;

/**
 * Description	Percondition class for versionable objects
 * Author		pramaswamy
 * Type			VersionPrecondition
 */
public class VersionPrecondition implements IActionPrecondition  {

	/**
	 *
	 */
	public VersionPrecondition() {
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.action.IActionPrecondition#queryExecute(java.lang.String, com.documentum.web.formext.config.IConfigElement, com.documentum.web.common.ArgumentList, com.documentum.web.formext.config.Context, com.documentum.web.formext.component.Component)
	 */
	public boolean queryExecute(String action, IConfigElement iConfigElement, ArgumentList argumentList, Context context, Component component)
	{
		boolean retValue = false;
		try
		{
			// HelperClass.porticoOutput(0, "in VersionPrecondition folder objectId="+argumentList.get("objectId"));
			String folderId = null;
			IDfSysObject iDfSysObject = (IDfSysObject)component.getDfSession().getObject(new DfId(argumentList.get("objectId")));
			if(iDfSysObject != null)
			{
				String objectType = iDfSysObject.getTypeName();
				if(objectType.equals("ptc_rmd") ||
				   objectType.equals("ptc_tmd") ||
				   objectType.equals("ptc_trace") ||
				   objectType.equals("ptc_cur_dmd") ||
				   objectType.equals("ptc_ext_dmd"))
				{
					retValue = true;
				}
				else if(iDfSysObject.getFolderIdCount()>0)
			    {
			    	folderId = iDfSysObject.getFolderId(0).getId();
			    	String folderPath = FolderUtil.getFolderPath(folderId,0);
			    	if(folderPath!=null && folderPath.startsWith("/System/Registries"))
			    	{
				    	retValue = true;
				    }
				}
			}
		}
		catch (DfException e)
		{
			ErrorMessageService.getService().setNonFatalError(component.getForm(),null,e);
		}

		return retValue;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.action.IActionPrecondition#getRequiredParams()
	 */
	public String[] getRequiredParams() {
		return m_requiredArgs;
	}
	private String m_requiredArgs[] = new String[0];

}
