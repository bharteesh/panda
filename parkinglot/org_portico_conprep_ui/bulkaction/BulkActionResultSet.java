/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionResultSet.java
 * Created on       Feb 09, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

/**
 * Description  Fetches and holds data for Bulk Actions
 * Author       Ranga
 * Type         BulkActionResultSet
 */
public class BulkActionResultSet
{
    public BulkActionResultSet(String actionType, IDfSession dfSession)
    {
		m_actionObject = null;
		// This hashData-the complete queried master result will remain thro' out the course of precondtion and update actions
		m_hashData = new Hashtable();
		m_sortKeyHash = new Hashtable();
        m_actionType = actionType;
        m_dfSession = dfSession;
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        m_combinedCookie = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

        // Combined cookies
        m_batchStatus = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_BATCH_STATUS);
        m_hold = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_HOLD_STATUS);
        m_provider = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER);
        m_profile = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROFILE);
        m_PrefPerformer = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PERFORMER);
		// Input value = "MM/DD/YYYY"
        m_fromCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_CREATION_DATE);
        m_toCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_CREATION_DATE);
        m_fromScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE);
        m_toScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE);

        // Direct cookies
        m_objectName = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_OBJECTNAME);
        m_lastActivity = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_LASTACTIVITY);

        m_submissionAreaName = HelperClass.getSubmissionAreaName(m_dfSession);


        boolean isSuccessful = queryData();
		HelperClass.porticoOutput(0, "BulkActionResultSet-constructor()-after-queryDataAndComputePrecondition()-isSuccessful="+isSuccessful);
    }

	public static String readCombinedCookie(String combinedCookieIn, String key)
	{
		HelperClass.porticoOutput(0, "BulkActionResultSet-readCombinedCookie()-combinedCookieIn="+combinedCookieIn);

		String value = "";
		if(combinedCookieIn != null)
		{
			int indx = combinedCookieIn.indexOf(key+KEY_VALUE_SEPARATOR);
			if(indx != -1)
			{
				int endIndx = combinedCookieIn.indexOf(COMBINED_COOKIE_SEPARATOR, indx);
				if(endIndx != -1)
				{
					// eg: mybStatus=RDY_FOR_QC1|
					// indx = 0, key.length()=10, endIndx = 21

					// eg: mybStatus=|
					// indx = 0, key.length()=10, endIndx = 10 => no value, discard this
					int startIndx = indx+key.length()+KEY_VALUE_SEPARATOR.length();

					if(endIndx > startIndx)
					{
						value = combinedCookieIn.substring(startIndx, endIndx);
					}
				}
			}
		}

		HelperClass.porticoOutput(0, "BulkActionResultSet-readCombinedCookie()-key="+key+":"+"value="+value);

		return value;
	}

	public boolean queryData()
	{
		boolean isSuccessful = true;
		// Clear the existing data hash
   		if(m_hashData != null)
   		{
   			m_hashData.clear();
   		}
   		if(m_sortKeyHash != null)
   		{
			m_sortKeyHash.clear();
		}
        String queryString = buildQueryString();
        if(queryString != null && !queryString.equals(""))
        {
			isSuccessful = fireQueryForHash(queryString);
		}

		return isSuccessful;
    }

    // Sort key is createtimestamp+objectId
	public TreeMap getSortedData(TreeMap unSortedTreeMap)
	{
		TreeMap sortedTreeMap = new TreeMap();
		try
		{
            Iterator iterate = unSortedTreeMap.keySet().iterator();
            while(iterate.hasNext())
            {
				String currentObjectId = (String)iterate.next();
                String sortKey = currentObjectId;

                if(m_sortKeyHash.containsKey(sortKey))
                {
					sortKey = (String)m_sortKeyHash.get(sortKey)+sortKey;
				}
				sortedTreeMap.put(sortKey, unSortedTreeMap.get(currentObjectId));
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in BulkActionResultSet-getSortedData-Exception="+e.toString());
		}

		return sortedTreeMap;
	}

	public void clearData()
	{
		if(m_hashData != null)
		{
			m_hashData.clear();
		}
		if(m_sortKeyHash != null)
		{
			m_sortKeyHash.clear();
		}
	}

    public String buildQueryString()
    {
		 // select 1,upper(object_name),r_object_id as sortbyobjid, r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,p_state,r_creation_date,p_on_hold,p_rawunit_count,p_article_count,p_performer_for_display,p_sched_timestamp,p_last_activity,p_problem_state_count,p_performer,p_user_action_taken,p_reentry_activity,p_workflow_id,p_provider_id,'1' as isfolder from p_batch where a_is_hidden=false and any i_folder_id='0b0152d480009337' order by r_creation_date desc

        String addlnAttributes = buildAddlnAttributes();
        HelperClass.porticoOutput(0, "BulkActionResultSet-buildQueryString-addlnAttributes="+ addlnAttributes);

		StringBuffer sb = new StringBuffer(1000);
        // sb.append("SELECT upper(object_name),r_object_id ");
        sb.append("SELECT object_name,r_object_id ");
        if(addlnAttributes != null && !addlnAttributes.equals(""))
        {
            sb.append(",");
            sb.append(addlnAttributes);
	    }
        sb.append(" FROM ");
        sb.append(DBHelperClass.BATCH_TYPE);
		sb.append(" WHERE ");

        // For "All" providers, we receive a ""
        if(m_provider == null || m_provider.equals(""))
        {
		}
		else
		{
            // JIRA - CONPREP-1647 - Go with the providerId, do NOT go with the providerName
    		sb.append(" p_provider_id=");
    		sb.append("'");
    		sb.append(m_provider);
    		sb.append("'");
            sb.append(" AND ");
		}

		sb.append(" a_is_hidden=false ");

// Start Profile
        // JIRA - CONPREP-1647 - Include the profileId as well
        // For "All" profiles, we receive a ""
        if(m_profile == null || m_profile.equals(""))
        {
		}
		else
		{
    		sb.append(" AND p_profile_id=");
    		sb.append("'");
    		sb.append(m_profile);
    		sb.append("'");
		}
// End Profile

		if(m_PrefPerformer != null && !m_PrefPerformer.equals(""))
		{
            sb.append(" AND p_performer_for_display LIKE ");
            sb.append("'%");
            sb.append(m_PrefPerformer);
            sb.append("%'");
		}

        // Do a case insensitive search
		if(m_objectName != null && !m_objectName.equals(""))
		{
            sb.append(" AND UPPER(object_name) LIKE ");
            // Note: A wild card % would be keyed in by the user, if that was the user's intention
            // sb.append("'%");
            sb.append("'");
            sb.append(m_objectName.toUpperCase());
            sb.append("'");
            // sb.append("%'");
		}

        // Do a case insensitive search
		if(m_lastActivity != null && !m_lastActivity.equals(""))
		{
            sb.append(" AND UPPER(p_last_activity) LIKE ");
            // Note: A wild card % would be keyed in by the user, if that was the user's intention
            // sb.append("'%");
            sb.append("'");
            sb.append(m_lastActivity.toUpperCase());
            sb.append("'");
            // sb.append("%'");
		}

		if(m_batchStatus != null && !m_batchStatus.equals(""))
		{
			String statusClause = buildInClause(m_batchStatus);
            sb.append(" AND "+HelperClassConstants.BATCH_STATE+" IN (");
            sb.append(statusClause);
            sb.append(")");
		}

	    if(m_hold != null && !m_hold.equals(""))
	    {
			try
			{
				boolean boolValue = Boolean.valueOf(m_hold).booleanValue();
                sb.append(" AND p_on_hold=");
                sb.append(boolValue + " ");
			}
			catch(Exception e)
			{
				HelperClass.porticoOutput(1, "Exception in BulkActionResultSet-buildQueryString-m_hold="+m_hold+", Exception="+e.toString());
			}
		}

        int createDateType = 0;
        String commonCreateDate = "";
		if(m_fromCreationDate != null && !m_fromCreationDate.equals("") &&
		    m_toCreationDate != null && !m_toCreationDate.equals(""))
		{
			createDateType = 2;
		}
		else
		{
			if(m_fromCreationDate != null && !m_fromCreationDate.equals(""))
			{
				createDateType = 1;
				commonCreateDate = m_fromCreationDate;
			}
			else if(m_toCreationDate != null && !m_toCreationDate.equals(""))
			{
				createDateType = 1;
				commonCreateDate = m_toCreationDate;
			}
		}

		// m_fromCreationDate/m_toCreationDate

		if(createDateType == 1)
		{
            sb.append(" AND DATEFLOOR(day,\"r_creation_date\") = ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(commonCreateDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
	    }
	    else if(createDateType == 2)
	    {
            sb.append(" AND DATEFLOOR(day,\"r_creation_date\") >= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(m_fromCreationDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");

            sb.append(" AND DATEFLOOR(day,\"r_creation_date\") <= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(m_toCreationDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}

        // 0 => NO creation date at all.
        // 1 => Either From/To Creation Date only
        // 2 => Both From & To Creation Date
        int scheduleDateType = 0;
        String commonScheduleDate = "";
		if(m_fromScheduleDate != null && !m_fromScheduleDate.equals("") &&
		    m_toScheduleDate != null && !m_toScheduleDate.equals(""))
		{
			scheduleDateType = 2;
		}
		else
		{
			if(m_fromScheduleDate != null && !m_fromScheduleDate.equals(""))
			{
				scheduleDateType = 1;
				commonScheduleDate = m_fromScheduleDate;
			}
			else if(m_toScheduleDate != null && !m_toScheduleDate.equals(""))
			{
				scheduleDateType = 1;
				commonScheduleDate = m_toScheduleDate;
			}
		}

		// m_fromScheduleDate/m_toScheduleDate

		if(scheduleDateType == 1)
		{
            sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") = ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(commonScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
	    }
	    else if(scheduleDateType == 2)
	    {
            sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") >= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(m_fromScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");

            sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") <= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(m_toScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}


		sb.append(" order by r_creation_date desc");


        HelperClass.porticoOutput(0, "BulkActionResultSet-buildQueryString-query String="+ sb.toString());

        return sb.toString();
	}

    public boolean fireQueryForHash(String queryString)
    {
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "BulkActionResultSet(New)-fireQueryForHash()-Start Query for Hash");
        IDfCollection tIDfCollection = null;

        try
		{
    		if(queryString != null && !queryString.equals(""))
    		{
                DfQuery dfquery = new DfQuery();
                HelperClass.porticoOutput(0, "BulkActionResultSet(New)-fireQueryForHash()-Start Regular Query for Hash");
         		dfquery.setDQL(queryString);
                HelperClass.porticoOutput(0, "BulkActionResultSet(New)-fireQueryForHash()-End Regular Query for Hash");
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    String objectid = "";
                    String value = "";
                    IDfTime iDfTime = null;
                    while(tIDfCollection.next())
                    {
    					objectid = tIDfCollection.getString("r_object_id");
                        Hashtable attrHash = new Hashtable();
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME, value);
    					}

                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.R_OBJECT_TYPE);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.R_OBJECT_TYPE, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_STATE);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_STATE, value);
    					}
                        iDfTime = tIDfCollection.getTime(SubmissionBatchObjectListWithMyBatches.R_CREATION_DATE);
                        if(iDfTime != null)
                        {
							value = iDfTime.asString(IDfTime.DF_TIME_PATTERN26);
                            if(value != null)
                            {
    					    	attrHash.put(SubmissionBatchObjectListWithMyBatches.R_CREATION_DATE, value);
    					    	m_sortKeyHash.put(objectid, value);
    					    	HelperClass.porticoOutput(0, "BulkActionResultSet(New)-fireQueryForHash()-objectid,value="+objectid+","+value+"-sortKey="+value+objectid);
    					    }
					    }
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_PERFORMER_FOR_DISPLAY);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_PERFORMER_FOR_DISPLAY, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_SCHED_TIMESTAMP);
                        if(value != null && !value.equals(DfTime.DF_NULLDATE_STR))
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_SCHED_TIMESTAMP, value);
    					}

                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_LAST_ACTIVITY);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_LAST_ACTIVITY, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_PERFORMER);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_PERFORMER, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_USER_ACTION_TAKEN);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_USER_ACTION_TAKEN, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_REENTRY_ACTIVITY);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_REENTRY_ACTIVITY, value);
    					}
                        value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_BATCH_WORKFLOW_ID);
                        if(value != null)
                        {
    						attrHash.put(SubmissionBatchObjectListWithMyBatches.P_BATCH_WORKFLOW_ID, value);
    					}
                        value = tIDfCollection.getString(P_ACCESSION_ID);
                        if(value != null)
                        {
    						attrHash.put(P_ACCESSION_ID, value);
    					}
    					value = tIDfCollection.getString(P_QUEUE_PRIORITY);
                        if(value != null)
                        {
   					    	attrHash.put(P_QUEUE_PRIORITY, value);
						}
    					value = tIDfCollection.getString(P_WORKFLOW_QUEUE);
                        if(value != null)
                        {
   					    	attrHash.put(P_WORKFLOW_QUEUE, value);
						}
    					value = tIDfCollection.getString(SubmissionBatchObjectListWithMyBatches.P_PROFILE_ID);
                        if(value != null)
                        {
   					    	attrHash.put(SubmissionBatchObjectListWithMyBatches.P_PROFILE_ID, value);
						}
    					HelperClass.porticoOutput(0, "BulkActionResultSet-fireQueryForHash-values="+"objectid="+objectid);
    					m_hashData.put(objectid, attrHash);
    				}

     	        	tIDfCollection.close();
     	    	}
		    }

		    isSuccessful = true;
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in BulkActionResultSet-fireQueryForHash="+e.toString());
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
           		HelperClass.porticoOutput(0, "BulkActionResultSet-fireQueryForHash CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in BulkActionResultSet-fireQueryForHash-close" + e.toString());
			}
            HelperClass.porticoOutput(0, "BulkActionResultSet-fireQueryForHash-Call-finally");
		}

        HelperClass.porticoOutput(0, "BulkActionResultSet(New)-fireQueryForHash()-End Query for Hash");

        return isSuccessful;
	}


    public static String buildInClause(String commaSeparatedStringIn)
    {
		String outStr = "";
		if(commaSeparatedStringIn != null && !commaSeparatedStringIn.equals(""))
		{
			boolean isFirst = true;
            StringTokenizer strTokenizer = new StringTokenizer(commaSeparatedStringIn, ",");
            while (strTokenizer.hasMoreTokens())
            {
				if(isFirst == true)
				{
			    	outStr =  outStr + "'" + strTokenizer.nextToken().trim() + "'";
			    }
			    else
			    {
					outStr =  outStr + "," + "'" + strTokenizer.nextToken().trim() + "'";
				}
				isFirst = false;
            }
		}

        HelperClass.porticoOutput(0, "BulkActionResultSet-buildInClause-commaSeparatedStringIn,outStr="+commaSeparatedStringIn+","+outStr);

        return outStr;
	}

    public static String buildAddlnAttributes()
    {
		String addlnAttributeStr = "";

        for(int indx = 0; indx < ADDLN_ATTRIBUTES.length; indx++)
        {
			if(indx == 0)
			{
				addlnAttributeStr = ADDLN_ATTRIBUTES[indx];
			}
			else
			{
			    addlnAttributeStr = addlnAttributeStr + "," + ADDLN_ATTRIBUTES[indx];
			}
        }

        return addlnAttributeStr;
	}

	public String getCurrentproviderId()
	{
		return m_provider;
	}

    // In future we could have a common ADDLN_ATTRIBUTES
    private static final String ADDLN_ATTRIBUTES[] = {
        "r_object_type","p_state", "r_creation_date","p_on_hold", "p_performer_for_display", "p_performer", "p_user_action_taken", "p_reentry_activity", "p_workflow_id", "p_provider_id", "p_sched_timestamp", "p_last_activity", "p_accession_id", "p_queue_priority", "p_workflow_queue", "p_profile_id"
    };


    private Object m_actionObject;
    private String m_actionType;
    private IDfSession m_dfSession;
    private String m_combinedCookie;
    public Hashtable m_hashData;
    public Hashtable m_sortKeyHash;
    private String m_submissionAreaName;

        // Combined cookies
    private String m_batchStatus;
    private String m_hold;
    private String m_provider;
    private String m_profile;
    private String m_PrefPerformer;
		// Input value = "MM/DD/YYYY"
    private String m_fromCreationDate;
    private String m_toCreationDate;
    private String m_fromScheduleDate;
    private String m_toScheduleDate;

    // Direct cookies
    private String m_objectName;
    private String m_lastActivity;

    public static final String FAILED_PRECONDITION = "Precondition NOT successful";
    public static final String KEY_VALUE_SEPARATOR = "=";
    public static final String COMBINED_COOKIE_SEPARATOR = "|";
    public static final String DATESEPARATOR = "/";
    public static final String P_ACCESSION_ID="p_accession_id";
    public static final String P_QUEUE_PRIORITY="p_queue_priority";
    public static final String P_WORKFLOW_QUEUE="p_workflow_queue";
}