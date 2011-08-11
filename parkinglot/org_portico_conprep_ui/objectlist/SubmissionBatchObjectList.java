/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	SubmissionBatchObjectList.java
 * Created on 		Jan 14, 2005
 *
 */
package org.portico.conprep.ui.objectlist;
import java.util.ArrayList;
import java.util.Hashtable;

import org.portico.conprep.ui.docbasequery.BaseDocbaseQueryService;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webtop.webcomponent.objectlist.ObjectList;

public class SubmissionBatchObjectList extends ObjectList
{
	/**
	 *
	 */
	public SubmissionBatchObjectList()
	{
		super();
		m_strStatus = null;
		m_StatusList = new ArrayList();
		m_statusFilter = null;
		m_statusLabel = null;
		m_strDefaultStatusKey = "";
		m_boolOnHold = false;
		m_hashData = new Hashtable();
		m_BatchWorkFlowId = new Hashtable();
		m_queryString = "";
		m_hashBatchStatusLookup = new Hashtable();
	}

	public void onInit(ArgumentList argumentlist)
	{
		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-Start");
		initializeCommonData();
    	m_strStatus = getCurrentBatchStatus();
    	m_boolOnHold = false;
		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-Call(Start) super.onInit");
		super.onInit(argumentlist);
		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-Call(End) super.onInit");
        m_statusFilter = (DropDownList)getControl(SubmissionBatchObjectList.STATUS_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_statusFilter.setMutable(true);
    	m_statusLabel = (Label)getControl(SubmissionBatchObjectList.STATUS_LABEL, com.documentum.web.form.control.Label.class);

        m_checkBoxOnHold = (Checkbox)getControl(SubmissionBatchObjectList.ONHOLD_CONTROL_FILTER, com.documentum.web.form.control.Checkbox.class);

		initializeCommonControls();
		// This is not required because the 'super.onInit' calls the 'updateControlsFromPath'
		// So, it we set the status/hold valiables prior, then this call is unnecessary

		//HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-Call-updateFromStatusFilter-Start");
		// updateFromStatusFilter(getCurrentBatchStatus(), false);
		//HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-Call-updateFromStatusFilter-End");

		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()- Before argumentlist");
		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onInit()-argumentlist=" + argumentlist.toString());
	}

	public void onRender()
	{
		super.onRender();
		// update status filter if required
	}

	public String getCurrentBatchStatus()
	{
		// String tBatchStatus = AppSessionContext.getCurrentBatchStatus();

        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String tBatchStatus = ipreferencestore.readString(SubmissionBatchObjectList.PREFERENCE_BATCH_STATUS);

		if(tBatchStatus == null)
		{
			tBatchStatus = m_strDefaultStatusKey;
		}

		return tBatchStatus;
	}

	public void setCurrentBatchStatus(String tBatchStatus)
	{
		// AppSessionContext.setCurrentBatchStatus(tBatchStatus);
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString(SubmissionBatchObjectList.PREFERENCE_BATCH_STATUS, tBatchStatus);
	}

    public void onClickObject(Control control, ArgumentList argumentlist)
    {
        // HelperClass.porticoOutput(0, "SubmissionBatchObjectList-onClickObject()-argumentlist="+argumentlist);

        String objectType = argumentlist.get("type");
        String objectId = argumentlist.get("objectId");
        String isFolderObject = argumentlist.get("isFolder");
        boolean isCustomComponent = false;

        HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-onClickObject()-argumentlist=" + "type=" + objectType +
                                                      ":objectId=" + objectId +
                                                      ":isFolderObject="+isFolderObject);

        String folderPath = null;
        if(isFolderObject != null && isFolderObject.equals("1") && FolderUtil.isFolderType(objectId))
        {
			folderPath = FolderUtil.getFolderPath(objectId, 0);
			if((folderPath != null) &&
    			(objectType.equals(DBHelperClass.BATCH_TYPE)))
    	   {
		      isCustomComponent = true;
			  HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-onClickObject()-setComponentJump-submission_raw_unit_objectlist-folderpath=" + folderPath);
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
	    }
	    if(isCustomComponent == false)
	    {
			HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-onClickObject()-setComponentJump-Regular-folderpath=" + folderPath);
            super.onClickObject(control, argumentlist);
	    }
	}

	protected void updateControlsFromPath(String s)
	{
		try
		{
    	    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-Start-updateControlsFromPath()");
    		super.updateControlsFromPath(s);
    	    // FolderUtil.getFolderId(s);
    	    IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(FolderUtil.getFolderId(s)));
    	    String currentObjectType = iDfSysObject.getTypeName();
    	    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-updateControlsFromPath()-currentObjectType=" + currentObjectType);
            if(iDfSysObject.getTypeName().equals(HelperClass.getInternalObjectType("provider_folder")))
            {
        	    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-updateControlsFromPath()-Start query");
        	    // Override the existing query(for p_batch include our where clauses if any,
                Datagrid datagrid = (Datagrid)getControl("doclist_grid", com.documentum.web.form.control.databound.Datagrid.class);
                //String queryString = BaseDocbaseQueryService.baseBuildObjectListQuery(super.m_strFolderId, buildVisibleAttributeList(), super.m_bShowFolders, super.m_bShowFiles, super.m_strObjectType, null, currentObjectType, m_strStatus, m_boolOnHold);
                m_queryString = BaseDocbaseQueryService.baseBuildObjectListQuery(super.m_strFolderId, buildVisibleAttributeList(), super.m_bShowFolders, false, super.m_strObjectType, "p_batch", currentObjectType, m_strStatus, m_boolOnHold);
        	    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-updateControlsFromPath()-query="+m_queryString);
                datagrid.getDataProvider().setQuery(m_queryString);
                // fireQueryForHash(queryString);
        	    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-updateControlsFromPath()-End query");
    	    }
        }
        catch(Exception e)
        {
			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectList(New)-updateControlsFromPath()=" + e.getMessage());
		}
		finally
		{
		}
	}

	public void initializeCommonData()
	{
		m_StatusList = HelperClass.getBatchStatusMappingListFromConfig(getDfSession());
	    m_strDefaultStatusKey = HelperClass.getDefaultBatchStatus(getDfSession());
	    m_hashBatchStatusLookup = HelperClass.getLookupList(getDfSession(), DBHelperClass.BATCH_TYPE, HelperClassConstants.BATCH_STATE);
	}

	public void initializeCommonControls()
	{
		m_statusLabel.setLabel("Status");
		m_statusFilter.setMutable(true);
		m_statusFilter.clearOptions();
		Option option = null;
/*
   		option = new Option();
   		option.setValue("");
        option.setLabel("All");
        m_statusFilter.addOption(option);
*/
		if(m_StatusList != null && m_StatusList.size() > 0)
		{
			for(int indx=0; indx < m_StatusList.size(); indx++)
			{
				ValuePair valuePair = (ValuePair)m_StatusList.get(indx);
				String key = valuePair.getKey();
				String value = valuePair.getValue();
   				option = new Option();
	            option.setValue(key);
                option.setLabel(value);
                m_statusFilter.addOption(option);
			}
		}
		m_statusFilter.setValue(getCurrentBatchStatus());
		// RANGA set checkbox to unchecked state
		m_checkBoxOnHold.setValue(false);
	}

	public void onSelectStatusFilter(Control control,ArgumentList args)
	{
		// RANGA set checkbox to unchecked state
		m_checkBoxOnHold.setValue(false);
        updateFromStatusFilter(m_statusFilter.getValue(), m_checkBoxOnHold.getValue());
    	setCurrentBatchStatus(m_strStatus);
	}

	public void updateFromStatusFilter(String status, boolean onhold)
	{
    	m_strStatus = status;
    	m_boolOnHold = onhold;
    	updateControlsFromPath(getFolderPath());
	}

/*
	public int getAssetCount(String batchObjectId)
	{
		return HelperClass.getAssetCountForBatchObject(getDfSession(), batchObjectId);
	}
*/

	public void onClickOnHold(Control control,ArgumentList args)
	{
		m_statusFilter.setMutable(true);
		if(m_checkBoxOnHold.getValue() == true)
		{
			m_statusFilter.setValue("");
		}
		else
		{
			m_statusFilter.setValue(getCurrentBatchStatus());
		}

		updateFromStatusFilter(m_statusFilter.getValue(), m_checkBoxOnHold.getValue());
	}

	public String getBatchScheduleTime(String batchObjectId)
	{
		String batchScheduleTime = null;
		try
		{
            String qualification = "p_batch WHERE r_object_id ="+  "'" + batchObjectId + "'";
	        HelperClass.porticoOutput(0, "SubmissionBatchObjectList-getBatchScheduleTime-qualification="+qualification);
            IDfPersistentObject batchObject = (IDfPersistentObject) getDfSession().getObjectByQualification(qualification);
            if(batchObject != null)
            {
		    	batchScheduleTime = batchObject.getString("p_sched_timestamp");
		    	if(batchScheduleTime.equals(DfTime.DF_NULLDATE_STR))
		    	{
					batchScheduleTime = null;
				}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "SubmissionBatchObjectList-getBatchScheduleTime-Exception="+e.getMessage());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectList-getBatchScheduleTime-batchScheduleTime="+batchScheduleTime);

		return batchScheduleTime;
    }

    public Hashtable getHashData()
    {
		return m_hashData;
	}

    public Hashtable getBatchStatusLookUp()
    {
		return m_hashBatchStatusLookup;
	}


    // Delayed firing from the Page, not from the Component because on the Component the call to
    //         updateControlsFromPath happens about 3 times before the page is displayed
    public void fireQueryForHash()
    {
        HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-Start Query for Hash");
        IDfCollection tIDfCollection = null;

        try
		{
    		if(m_hashData != null)
    		{
    			m_hashData.clear();
    		}

    		if(m_BatchWorkFlowId != null)
    		{
    			m_BatchWorkFlowId.clear();
    		}

    		if(m_queryString != null && !m_queryString.equals(""))
    		{
                DfQuery dfquery = new DfQuery();
/*
                HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-Start Addln(WorkflowId) Query for Hash");

     	    	// Additional query to get workflowid(s)
     	    	String addlnQuery = "SELECT distinct r_component_id, r_workflow_id from dmi_package";
         		dfquery.setDQL(addlnQuery);
                tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-End Addln(WorkflowId) Query for Hash");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
    			    	String currentobjectid = tIDfCollection.getString("r_component_id");
    			    	String currentworkflowid = tIDfCollection.getString("r_workflow_id");
    			    	// The below check is at times due to a bug in Documentum, have seen
    			    	// cases where a null objectId comes out of the query from dql editor
    			    	if(currentobjectid != null && !currentobjectid.equals("") &&
    			    	       currentworkflowid != null && !currentworkflowid.equals(""))
    			    	{
							if(!m_BatchWorkFlowId.containsKey(currentobjectid))
							{
								m_BatchWorkFlowId.put(currentobjectid, currentworkflowid);
							}
						}
				    }
				    tIDfCollection.close();
			    }
*/

                HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-Start Regular Query for Hash");
         		dfquery.setDQL(m_queryString);
                HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-End Regular Query for Hash");
                tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    String objectid = "";
                    String objectname = "";
                    String objecttype = "";
                    String objectstatus = "";
                    String objectcreationdate = "";
                    String objectonhold = "";
                    String objectrawunitcount = "";
                    //String objectarticlecount = "";
                    String performerfordisplay = "";
                    String schedtimestamp = "";
                    String lastactivity = "";
                    String problemstatecount = "";
                    String performer = "";
                    String useractiontaken = "";
                    String reentryactivity = "";
                    String batchWorkFlowId = "";
                    while(tIDfCollection.next())
                    {
    					objectid = tIDfCollection.getString("r_object_id");
                        Hashtable attrHash = new Hashtable();
                        objectname = tIDfCollection.getString(OBJECT_NAME);
                        if(objectname != null)
                        {
    						attrHash.put(OBJECT_NAME, objectname);
    					}

                        objecttype = tIDfCollection.getString(R_OBJECT_TYPE);
                        if(objecttype != null)
                        {
    						attrHash.put(R_OBJECT_TYPE, objecttype);
    					}
                        objectstatus = tIDfCollection.getString(P_STATE);
                        if(objectstatus != null)
                        {
    						attrHash.put(P_STATE, objectstatus);
    					}
                        objectcreationdate = tIDfCollection.getString(R_CREATION_DATE);
                        if(objectcreationdate != null)
                        {
    						attrHash.put(R_CREATION_DATE, objectcreationdate);
    					}

                        objectonhold = tIDfCollection.getString(P_ON_HOLD);
                        if(objectonhold != null)
                        {
    						attrHash.put(P_ON_HOLD, objectonhold);
    					}
                        objectrawunitcount = tIDfCollection.getString(P_RAWUNIT_COUNT);
                        if(objectrawunitcount != null)
                        {
    						attrHash.put(P_RAWUNIT_COUNT, objectrawunitcount);
    					}
                        //objectarticlecount = tIDfCollection.getString(P_ARTICLE_COUNT);
                        //if(objectarticlecount != null)
                        //{
    				//		attrHash.put(P_ARTICLE_COUNT, objectarticlecount);
    				//	}
                        performerfordisplay = tIDfCollection.getString(P_PERFORMER_FOR_DISPLAY);
                        if(performerfordisplay != null)
                        {
    						attrHash.put(P_PERFORMER_FOR_DISPLAY, performerfordisplay);
    					}
                        schedtimestamp = tIDfCollection.getString(P_SCHED_TIMESTAMP);
                        if(schedtimestamp != null && !schedtimestamp.equals(DfTime.DF_NULLDATE_STR))
                        {
    						attrHash.put(P_SCHED_TIMESTAMP, schedtimestamp);
    					}

                        lastactivity = tIDfCollection.getString(P_LAST_ACTIVITY);
                        if(lastactivity != null)
                        {
    						attrHash.put(P_LAST_ACTIVITY, lastactivity);
    					}
                        problemstatecount = tIDfCollection.getString(P_PROBLEM_STATE_COUNT);
                        if(problemstatecount != null)
                        {
    						attrHash.put(P_PROBLEM_STATE_COUNT, problemstatecount);
    					}
                        performer = tIDfCollection.getString(P_PERFORMER);
                        if(performer != null)
                        {
    						attrHash.put(P_PERFORMER, performer);
    					}
                        useractiontaken = tIDfCollection.getString(P_USER_ACTION_TAKEN);
                        if(useractiontaken != null)
                        {
    						attrHash.put(P_USER_ACTION_TAKEN, useractiontaken);
    					}
                        reentryactivity = tIDfCollection.getString(P_REENTRY_ACTIVITY);
                        if(reentryactivity != null)
                        {
    						attrHash.put(P_REENTRY_ACTIVITY, reentryactivity);
    					}
                        batchWorkFlowId = tIDfCollection.getString(P_BATCH_WORKFLOW_ID);
                        if(batchWorkFlowId != null)
                        {
    						attrHash.put(P_BATCH_WORKFLOW_ID, batchWorkFlowId);
    					}
    					m_hashData.put(objectid, attrHash);
    				}

     	        	tIDfCollection.close();
     	    	}

/*
                if(m_hashData != null && m_hashData.size() > 0)
                {
                    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-Start Addln(WorkflowId) Query for Hash");

     	    	    // Additional query to get workflowid(s)
     	    	    String addlnQuery = "SELECT r_component_id, r_workflow_id from dmi_package";
         		    dfquery.setDQL(addlnQuery);
                    tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                    HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-End Addln(WorkflowId) Query for Hash");
                    if(tIDfCollection != null)
                    {
                        while(tIDfCollection.next())
                        {
    			    		String currentobjectid = tIDfCollection.getString("r_component_id");
    			    		String currentworkflowid = tIDfCollection.getString("r_workflow_id");
    			    		// The below check is at times due to a bug in Documentum, have seen
    			    		// cases where a null objectId comes out of the query from dql editor
    			    		if(currentobjectid != null && !currentobjectid.equals("") &&
    			    		       currentworkflowid != null && !currentworkflowid.equals(""))
    			    		{
    							Hashtable attrHash = null;
    			    		    if(m_hashData.containsKey(currentobjectid))
    			    		    {
				    		    	attrHash = (Hashtable)m_hashData.get(currentobjectid);
    							    if(attrHash != null && !attrHash.containsKey(P_BATCH_WORKFLOW_ID))
    							    {
    								    attrHash.put(P_BATCH_WORKFLOW_ID, currentworkflowid);
				    		    	    m_hashData.remove(currentobjectid);
								        m_hashData.put(currentobjectid, attrHash);
    								}
				    		    }
							}
				    	}
			        }
			    }
*/
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectList-fireQueryForHash="+e.getMessage());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "SubmissionBatchObjectList-fireQueryForHash CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectList-fireQueryForHash-close" + e.getMessage());
			}
            HelperClass.porticoOutput(0, "SubmissionBatchObjectList-fireQueryForHash-Call-finally");
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectList(New)-fireQueryForHash()-End Query for Hash");
	}

/*
Extra query to get the workflow and workflow runtime status
SELECT p.r_object_id, p.r_workflow_id, w.r_runtime_state FROM dmi_package p, dm_workflow w where p.r_workflow_id=w.r_object_id
*/
    protected String m_strStatus;
    protected boolean m_boolOnHold;
    private ArrayList m_StatusList;
    private String m_strDefaultStatusKey;
    private Hashtable m_hashData;
    private String m_queryString;
    private Hashtable m_BatchWorkFlowId;
    private Hashtable m_hashBatchStatusLookup;

    private DropDownList m_statusFilter;
    private Label m_statusLabel;
    private Checkbox m_checkBoxOnHold;

    public static final String STATUS_CONTROL_FILTER = "status_filter";
    public static final String STATUS_LABEL = "Status";
    public static final String ONHOLD_CONTROL_FILTER = "onhold_filter";
    public static final String PREFERENCE_BATCH_STATUS = "currentBatchStatus";
    // Attributes to be in Hash for better performance
/*
    query=select 1,upper(object_name),r_object_id as sortbyobjid, r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,p_state,r_creation_date,p_on_hold,p_rawunit_count,p_article_count,p_performer_for_display,p_sched_timestamp,p_last_activity,p_problem_state_count,p_performer,p_user_action_taken,p_reentry_activity,'1' as isfolder from p_batch where a_is_hidden=false and any i_folder_id='0b0152d480009337' order by r_creation_date desc
*/
    public static final String R_OBJECT_ID = "r_object_id";
    public static final String OBJECT_NAME = "object_name";
    public static final String R_OBJECT_TYPE = "r_object_type";
    public static final String P_STATE = "p_state";
    public static final String R_CREATION_DATE = "r_creation_date";
    public static final String P_ON_HOLD = "p_on_hold";
    public static final String P_RAWUNIT_COUNT = "p_rawunit_count";
    public static final String P_ARTICLE_COUNT = "p_article_count";
    public static final String P_PERFORMER_FOR_DISPLAY = "p_performer_for_display";
    public static final String P_SCHED_TIMESTAMP = "p_sched_timestamp";
    public static final String P_LAST_ACTIVITY = "p_last_activity";
    public static final String P_PROBLEM_STATE_COUNT = "p_problem_state_count";
    public static final String P_PERFORMER = "p_performer";
    public static final String P_USER_ACTION_TAKEN = "p_user_action_taken";
    public static final String P_REENTRY_ACTIVITY = "p_reentry_activity";
    public static final String P_BATCH_WORKFLOW_ID = "p_workflow_id";
}
