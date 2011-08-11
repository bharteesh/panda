/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	SubmissionRawUnitObjectList.java
 * Created on 		Jan 14, 2005
 *
 */
package org.portico.conprep.ui.objectlist;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webtop.webcomponent.objectlist.ObjectList;

public class SubmissionRawUnitObjectList extends ObjectList
{

	/**
	 *
	 */
	public SubmissionRawUnitObjectList()
	{
		super();
	}

	public void onInit(ArgumentList argumentlist)
	{
		super.onInit(argumentlist);
		HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onInit()- Before argumentlist");
		HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onInit()-argumentlist=" + argumentlist.toString());
	}

	public void onClickObject(Control control, ArgumentList argumentlist)
	{
	    // HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onClickObject()-argumentlist="+argumentlist);

	    String objectType = argumentlist.get("type");
	    String objectId = argumentlist.get("objectId");
	    String isFolderObject = argumentlist.get("isFolder");
	    boolean isComponentHandled = false;

        HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onClickObject()-argumentlist=" + "type=" + objectType +
                                                      ":objectId=" + objectId +
                                                      ":isFolderObject="+isFolderObject);

        String folderPath = null;
        if(isFolderObject != null && isFolderObject.equals("1") && FolderUtil.isFolderType(objectId))
        {
			folderPath = FolderUtil.getFolderPath(objectId, 0);
			if(folderPath != null)
    	    {
		       isComponentHandled = true;
			   HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onClickObject()-setComponentJump-objectlist(Regular)-folderpath=" + folderPath);
               updateContextFromPath(folderPath);
               // Switch to Default behaviour driven from 'objectlist'
               setComponentJump("objectlist", argumentlist, getContext());
	        }
	    }
	    if(isComponentHandled == false)
	    {
			HelperClass.porticoOutput(0, "SubmissionRawUnitObjectList-onClickObject()-super.onClickObject called");
            super.onClickObject(control, argumentlist);
	    }
    }
}
