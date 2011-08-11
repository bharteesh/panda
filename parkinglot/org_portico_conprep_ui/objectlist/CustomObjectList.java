/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	CustomObjectList.java
 * Created on 		Jan 14, 2005
 *
 */
package org.portico.conprep.ui.objectlist;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webtop.app.AppSessionContext;
import com.documentum.webtop.webcomponent.objectlist.ObjectList;

public class CustomObjectList extends ObjectList
{

	/**
	 *
	 */
	public CustomObjectList()
	{
		super();
	}
	public void onInit(ArgumentList argumentlist)
	{
		super.onInit(argumentlist);

		HelperClass.porticoOutput(0, "CustomObjectList - START onInit");
		HelperClass.porticoOutput(0, "CustomObjectList-onInit()-argumentlist=" + argumentlist.toString());

		AppSessionContext appsessioncontext = AppSessionContext.get(getPageContext().getSession());
		String folderPath = appsessioncontext.getAppLocation().getFolderPath();
		//parse type from context string
		String strCtx = getContext().toString();
		// Sometimes gives error while writing thro' the Dflogger

		HelperClass.porticoOutput(0, "CustomObjectList(New)-onInit()-getContext=" + strCtx);

		String type = null;
		int start = strCtx.indexOf("type=");
		int end = -1;
		if(start>0)
		{
			end = strCtx.indexOf(",",start);
			type = strCtx.substring(start+5,end);
		}

		HelperClass.porticoOutput(0, "CustomObjectList(New)-onInit()-type=" + type);
		HelperClass.porticoOutput(0, "CustomObjectList(New)-onInit()-folderPath=" + folderPath);

		if(folderPath!=null)
		{
		    //if(folderPath.startsWith(getSubmissionAreaPath()))
		    //{
		        if(type.equals(HelperClass.getInternalObjectType("provider_folder")))
		        {
    	            HelperClass.porticoOutput(0, "CustomObjectList(New)-onInit()-Jump to submission_batch_objectlist");
    	    		setComponentJump("submission_batch_objectlist",argumentlist,getContext());
		    	}
		    	else if(type.equals(DBHelperClass.BATCH_TYPE))
		    	{
    	            HelperClass.porticoOutput(0, "CustomObjectList(New)-onInit()-Jump to submission_raw_unit_objectlist");
    	    		setComponentJump("submission_raw_unit_objectlist",argumentlist,getContext());
		    	}
		    //}
		}
	}

    public void onClickObject(Control control, ArgumentList argumentlist)
    {
        // HelperClass.porticoOutput(0, "CustomObjectList-onClickObject()-argumentlist="+argumentlist.toString());
        // {objectId=0b0152d480008a5c,type=dm_folder,isFolder=1}
        String objectType = argumentlist.get("type");
        String objectId = argumentlist.get("objectId");
        String isFolderObject = argumentlist.get("isFolder");
        boolean isCustomComponent = false;

        HelperClass.porticoOutput(0, "CustomObjectList(New)-onClickObject()-argumentlist=" + "type=" + objectType +
                                                      ":objectId=" + objectId +
                                                      ":isFolderObject="+isFolderObject);
        String folderPath = null;
        if(isFolderObject != null && isFolderObject.equals("1") && FolderUtil.isFolderType(objectId))
        {
			folderPath = FolderUtil.getFolderPath(objectId, 0);
			if(folderPath != null)
			{
    			//if(folderPath.startsWith(getSubmissionAreaPath()))
    	        //{
			       if(objectType.equals(HelperClass.getInternalObjectType("provider_folder")))
			       {
    			       isCustomComponent = true;
    			       HelperClass.porticoOutput(0, "CustomObjectList(New)-onClickObject()-setComponentJump-submission_batch_objectlist-folderpath=" + folderPath);
                       updateContextFromPath(folderPath);
                       if(argumentlist.get("folderPath") != null)
                       {
        				   argumentlist.replace("folderPath", folderPath);
           			   }
        			   else
         			   {
        				   argumentlist.add("folderPath", folderPath);
         			   }
                       setComponentJump("submission_batch_objectlist", argumentlist, getContext());
			       }
			       else if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
			       {
				       isCustomComponent = true;
				       HelperClass.porticoOutput(0, "CustomObjectList(New)-onClickObject()-setComponentJump-submission_raw_unit_objectlist-folderpath=" + folderPath);
                       updateContextFromPath(folderPath);
                       if(argumentlist.get("folderPath") != null)
                       {
        				   argumentlist.replace("folderPath", folderPath);
           			   }
        			   else
         			   {
        				   argumentlist.add("folderPath", folderPath);
         			   }
                       setComponentJump("submission_raw_unit_objectlist", argumentlist, getContext());
			       }
			    //}
	        }
	    }
	    if(isCustomComponent == false)
	    {
			HelperClass.porticoOutput(0, "CustomObjectList(New)-onClickObject()-setComponentJump-Regular-folderpath=" + folderPath);
            super.onClickObject(control, argumentlist);
	    }
	}

	private String getSubmissionAreaPath()
	{
		return "/" + HelperClass.getSubmissionAreaName(getDfSession()) + "/";
	}
}
