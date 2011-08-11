
package org.portico.conprep.ui.docbasequery;

import java.util.StringTokenizer;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.web.formext.Trace;


public class BaseDocbaseQueryServiceWithMyBatches extends com.documentum.webcomponent.navigation.doclist.DocbaseQueryService
{

    public BaseDocbaseQueryServiceWithMyBatches()
    {
		super();
    }

    // For a filtered batch list view
    public static String baseBuildObjectListQuery(String submissionAreaName,
		                                              String loginName,
                                                      String visibleAttrList[],
		                                              String providerId,
		                                              String profileId,
		                                              String wherePerformer,
                                                      String whereObjectName, // LIKE
                                                      String whereLastActivity, // LIKE
                                                      String whereStatus, // IN
                                                      String whereHold,
                                                      String whereFromCreationDate, // "MM/DD/YYYY"
                                                      String whereFromScheduleDate,
                                                      String whereToScheduleDate,
                                                      String whereToCreationDate) // "MM/DD/YYYY"
    {
        HelperClass.porticoOutput(0, "BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-loginName="+ loginName +","+
                                                                                                    "providerId="+providerId + ","+
                                                                                                    "profileId="+profileId + ","+
                                                                                                    "whereObjectName="+whereObjectName + ","+
                                                                                                    "whereLastActivity="+whereLastActivity + ","+
                                                                                                    "whereStatus="+whereStatus + ","+
                                                                                                    "whereHold="+whereHold);
        // select 1,upper(object_name),r_object_id as sortbyobjid, r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,p_state,r_creation_date,p_on_hold,p_rawunit_count,p_article_count,p_performer_for_display,p_sched_timestamp,p_last_activity,p_problem_state_count,p_performer,p_user_action_taken,p_reentry_activity,p_workflow_id,p_provider_id,'1' as isfolder from p_batch where a_is_hidden=false and any i_folder_id='0b0152d480009337' order by r_creation_date desc

        String regularAttributes = buildAttributeList(visibleAttrList);// buildRegularAttributes();
        HelperClass.porticoOutput(0, "BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-regularAttributes="+ regularAttributes);
        String addlnAttributes = buildAddlnAttributes();
        HelperClass.porticoOutput(0, "BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-addlnAttributes="+ addlnAttributes);

		StringBuffer sb = new StringBuffer(1000);
        sb.append("SELECT 1,upper(object_name),r_object_id as sortbyobjid ");
        if(regularAttributes != null && !regularAttributes.equals(""))
        {
            sb.append(",");
            sb.append(regularAttributes);
		}
        if(addlnAttributes != null && !addlnAttributes.equals(""))
        {
            sb.append(",");
            sb.append(addlnAttributes);
	    }
        sb.append(",'1' as isfolder");
        sb.append(" FROM ");
        sb.append(DBHelperClass.BATCH_TYPE);
		sb.append(" WHERE ");

        // For "All" providers, we receive a ""
        if(providerId == null || providerId.equals(""))
        {
		}
		else if(providerId.equals(BaseDocbaseQueryServiceWithMyBatches.MYBATCHES))
	    {
			// Currently 'MYBATCHES' scenario is not available


			// pick all the batches based on 'p_performer'(current logged in user)
    		// sb.append(" p_performer=");
    		// sb.append("'");
    		// sb.append(loginName);
    		// sb.append("'");
    		// sb.append(" OR ");
            sb.append(" p_performer_for_display LIKE ");
            sb.append("'%");
            sb.append(loginName);
            sb.append("%'");
            sb.append(" AND ");
		}
		else
		{
    		// Build the clause if it is a Provider Path eg: '/Batches/AMS'
    		// select object_name, p_provider_id, p_state from p_batch where folder('/Batches/AMS') and p_state = 'PROBLEM'

    		// JIRA - CONPREP-1647 - Go with the providerId, do NOT go with the providerName
/*
    		sb.append(" folder(");
    		sb.append("'");
    		sb.append(providerId);
    		sb.append("'");
    		sb.append(") ");
            sb.append(" AND ");
*/
    		sb.append(" p_provider_id=");
    		sb.append("'");
    		sb.append(providerId);
    		sb.append("'");
            sb.append(" AND ");
		}

		sb.append(" a_is_hidden=false ");

// Start Profile
        // For "All" profiles, we receive a ""
        if(profileId == null || profileId.equals(""))
        {
		}
		else
		{
    		sb.append(" AND p_profile_id=");
    		sb.append("'");
    		sb.append(profileId);
    		sb.append("'");
		}


// End Profile

		if(wherePerformer != null && !wherePerformer.equals(""))
		{
            sb.append(" AND p_performer_for_display LIKE ");
            sb.append("'%");
            sb.append(wherePerformer);
            sb.append("%'");
		}

/*
		if(whereObjectName != null && !whereObjectName.equals(""))
		{
            sb.append(" AND object_name LIKE ");
            sb.append("'%");
            sb.append(whereObjectName);
            sb.append("%'");
		}
*/
        // Do a case insensitive search
		if(whereObjectName != null && !whereObjectName.equals(""))
		{
            sb.append(" AND UPPER(object_name) LIKE ");
            // Note: A wild card % would be keyed in by the user, if that was the user's intention
            // sb.append("'%");
            sb.append("'");
            sb.append(whereObjectName.toUpperCase());
            sb.append("'");
            // sb.append("%'");
		}

        // Do a case insensitive search
		if(whereLastActivity != null && !whereLastActivity.equals(""))
		{
            sb.append(" AND UPPER(p_last_activity) LIKE ");
            // Note: A wild card % would be keyed in by the user, if that was the user's intention
            // sb.append("'%");
            sb.append("'");
            sb.append(whereLastActivity.toUpperCase());
            sb.append("'");
            // sb.append("%'");
		}

		if(whereStatus != null && !whereStatus.equals(""))
		{
			String statusClause = buildInClause(whereStatus);
            sb.append(" AND "+HelperClassConstants.BATCH_STATE+" IN (");
            sb.append(statusClause);
            sb.append(")");
		}

	    if(whereHold != null && !whereHold.equals(""))
	    {
			try
			{
				boolean boolValue = Boolean.valueOf(whereHold).booleanValue();
                sb.append(" AND p_on_hold=");
                sb.append(boolValue + " ");
			}
			catch(Exception e)
			{
				HelperClass.porticoOutput(1, "Exception in BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-whereHold="+whereHold+", Exception="+e.toString());
			}
		}

/*
		// whereFromCreationDate
		if(whereFromCreationDate != null && !whereFromCreationDate.equals(""))
		{
            sb.append(" AND DATEFLOOR(day,\"r_creation_date\") = ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(whereFromCreationDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}
*/

        int createDateType = 0;
        String commonCreateDate = "";
		if(whereFromCreationDate != null && !whereFromCreationDate.equals("") &&
		    whereToCreationDate != null && !whereToCreationDate.equals(""))
		{
			createDateType = 2;
		}
		else
		{
			if(whereFromCreationDate != null && !whereFromCreationDate.equals(""))
			{
				createDateType = 1;
				commonCreateDate = whereFromCreationDate;
			}
			else if(whereToCreationDate != null && !whereToCreationDate.equals(""))
			{
				createDateType = 1;
				commonCreateDate = whereToCreationDate;
			}
		}

		// whereFromCreationDate/whereToCreationDate

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
            sb.append(whereFromCreationDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");

            sb.append(" AND DATEFLOOR(day,\"r_creation_date\") <= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(whereToCreationDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}

// ScheduleDate
/*
		// whereFromScheduleDate
		if(whereFromScheduleDate != null && !whereFromScheduleDate.equals(""))
		{
            sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") = ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(whereFromScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}

*/

        // 0 => NO creation date at all.
        // 1 => Either From/To Creation Date only
        // 2 => Both From & To Creation Date
        int scheduleDateType = 0;
        String commonScheduleDate = "";
		if(whereFromScheduleDate != null && !whereFromScheduleDate.equals("") &&
		    whereToScheduleDate != null && !whereToScheduleDate.equals(""))
		{
			scheduleDateType = 2;
		}
		else
		{
			if(whereFromScheduleDate != null && !whereFromScheduleDate.equals(""))
			{
				scheduleDateType = 1;
				commonScheduleDate = whereFromScheduleDate;
			}
			else if(whereToScheduleDate != null && !whereToScheduleDate.equals(""))
			{
				scheduleDateType = 1;
				commonScheduleDate = whereToScheduleDate;
			}
		}

		// whereFromScheduleDate/whereToScheduleDate

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
            sb.append(whereFromScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");

            sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") <= ");
            sb.append("DATE(");
            sb.append("'");
            sb.append(whereToScheduleDate);
            sb.append("'");
            sb.append(",");
            sb.append("'");
            sb.append("MM/DD/YYYY");
            sb.append("'");
            sb.append(")");
		}


		sb.append(" order by r_creation_date desc");

        if(Trace.COMPONENT)
        {
            com.documentum.web.common.Trace.println("BaseDocbaseQueryServiceWithMyBatches batchBuildObjectListQuery query: " + sb.toString());
		}

        HelperClass.porticoOutput(0, "BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-query String="+ sb.toString());

        return sb.toString();
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

        HelperClass.porticoOutput(0, "BaseDocbaseQueryServiceWithMyBatches-buildInClause-commaSeparatedStringIn,outStr="+commaSeparatedStringIn+","+outStr);

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


    public static String buildRegularAttributes()
    {
		String regularAttributeStr = "";

        for(int indx = 0; indx < REGULAR_ATTRIBUTES.length; indx++)
        {
			if(indx == 0)
			{
				regularAttributeStr = REGULAR_ATTRIBUTES[indx];
			}
			else
			{
			    regularAttributeStr = regularAttributeStr + "," + REGULAR_ATTRIBUTES[indx];
			}
        }

        return regularAttributeStr;
	}

    // In future we could have a common ADDLN_ATTRIBUTES for Regular and MyBatches ObjectList
    //private static final String ADDLN_ATTRIBUTES[] = {
    //    "p_on_hold", "p_rawunit_count", "p_article_count", "p_performer_for_display", "p_sched_timestamp", "p_last_activity", "p_problem_state_count", "p_performer", "p_user_action_taken", "p_reentry_activity", "p_workflow_id", "p_provider_id", "p_profile_id", "p_state"
    //};
    private static final String ADDLN_ATTRIBUTES[] = {
        "p_on_hold", "p_rawunit_count", "p_performer_for_display", "p_sched_timestamp", "p_last_activity", "p_problem_state_count", "p_performer", "p_user_action_taken", "p_reentry_activity", "p_workflow_id", "p_provider_id", "p_profile_id", "p_state"
    };

    private static final String REGULAR_ATTRIBUTES[] = {
        "object_name", "r_object_id", "r_object_type", "r_lock_owner", "owner_name", "r_link_cnt", "r_is_virtual_doc", "r_content_size", "a_content_type", "i_is_reference", "r_creation_date"
    };

    // Keep this query as the reference, DO NOT remove this
    // select 1,upper(object_name),r_object_id as sortbyobjid, r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,p_state,r_creation_date,p_on_hold,p_rawunit_count,p_article_count,p_performer_for_display,p_sched_timestamp,p_last_activity,p_problem_state_count,p_performer,p_user_action_taken,p_reentry_activity,p_workflow_id,p_provider_id,'1' as isfolder from p_batch where a_is_hidden=false and any i_folder_id='0b0152d480009337' order by r_creation_date desc

    public static final String MYBATCHES = "MyBatches";
}