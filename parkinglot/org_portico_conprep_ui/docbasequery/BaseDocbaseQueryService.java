
package org.portico.conprep.ui.docbasequery;

import java.util.StringTokenizer;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.web.formext.Trace;
import com.documentum.web.formext.docbase.TypeUtil;


public class BaseDocbaseQueryService extends com.documentum.webcomponent.navigation.doclist.DocbaseQueryService
{

    public BaseDocbaseQueryService()
    {
		super();
    }

    // For a filtered batch list view
    public static String baseBuildObjectListQuery(String s, String as[], boolean flag, boolean flag1, String s1, String s2, String currentObjectType, String statusFilter, boolean holdFilter)
    {
		if(currentObjectType != null && currentObjectType.equals(HelperClass.getInternalObjectType("provider_folder")))
		{
			// Customized
			return batchBuildObjectListQuery(s, as, flag, flag1, s1, s2, statusFilter, holdFilter);
		}
		else
		{
			// Default
			return buildObjectListQuery(s, as, flag, flag1, s1, s2);
		}
	}

	// statusFilter can be 1 value OR comma separated values
    public static String batchBuildObjectListQuery(String s, String as[], boolean flag, boolean flag1, String s1, String s2, String statusFilter, boolean holdFilter)
    {
		String newStatusFilter = "";
		if(statusFilter != null && !statusFilter.equals(""))
		{
			boolean isFirst = true;
            StringTokenizer strTokenizer = new StringTokenizer(statusFilter, ",");
            while (strTokenizer.hasMoreTokens())
            {
				if(isFirst == true)
				{
			    	newStatusFilter =  newStatusFilter + "'" + strTokenizer.nextToken().trim() + "'";
			    }
			    else
			    {
					newStatusFilter =  newStatusFilter + "," + "'" + strTokenizer.nextToken().trim() + "'";
				}
				isFirst = false;
            }
		}

        HelperClass.porticoOutput(0, "BaseDocbaseQueryService-batchBuildObjectListQuery-newStatusFilter,holdFilter="+newStatusFilter+","+holdFilter);

        StringBuffer stringbuffer = new StringBuffer(1000);// 768
        if(s2 == null)
            s2 = "dm_folder";
        String s3 = buildAttributeList(as);
        String addlnAttributes = buildAddlnAttributes();
        if(s != null && s.length() != 0)
        {
            if(flag)
            {
                stringbuffer.append("select 1,upper(object_name),r_object_id as sortbyobjid, ");
                stringbuffer.append(s3);
                if(addlnAttributes != null && !addlnAttributes.equals(""))
                {
                    stringbuffer.append(",");
                    stringbuffer.append(addlnAttributes);
			    }
                stringbuffer.append(",'1' as isfolder");
                stringbuffer.append(" from ");
                stringbuffer.append(s2);
                stringbuffer.append(" where a_is_hidden=false and any i_folder_id='");
                stringbuffer.append(s);
                stringbuffer.append('\'');
                // Filter status attribute
                if(newStatusFilter != null && !newStatusFilter.equals(""))
                {
                    stringbuffer.append(" and "+HelperClassConstants.BATCH_STATE+" IN (");
                    stringbuffer.append(newStatusFilter);
                    stringbuffer.append(")");
			    }
			    if(holdFilter == true)
			    {
                    stringbuffer.append(" and p_on_hold=");
                    stringbuffer.append(holdFilter + " ");
				}
                if(flag1)
                    stringbuffer.append(" union ");
            }
            if(flag1)
            {
                stringbuffer.append("select 2,upper(object_name),r_object_id as sortbyobjid, ");
                stringbuffer.append(s3);
                stringbuffer.append(",'0' as isfolder");
                stringbuffer.append(" from ");
                stringbuffer.append(s1);
                stringbuffer.append(" where a_is_hidden=false and any i_folder_id='");
                stringbuffer.append(s);
                if(s1.equals(s2) || TypeUtil.isSubtypeOf(s2, s1))
                {
                    stringbuffer.append("' and not type(");
                    stringbuffer.append(s2);
                    stringbuffer.append(')');
                } else
                {
                    stringbuffer.append("'");
                }
                // Filter status attribute
                if(newStatusFilter != null && !newStatusFilter.equals(""))
                {
                    stringbuffer.append(" and "+HelperClassConstants.BATCH_STATE+" IN (");
                    stringbuffer.append(newStatusFilter);
                    stringbuffer.append(")");
			    }
			    if(holdFilter == true)
			    {
                    stringbuffer.append(" and p_on_hold=");
                    stringbuffer.append(holdFilter + " ");
				}
            }
            // stringbuffer.append(" order by 1,2,3");
            stringbuffer.append(" order by r_creation_date desc");
        } else
        {
            stringbuffer.append("select upper(object_name),");
            stringbuffer.append(s3);
            stringbuffer.append(",'1' as isfolder");
            stringbuffer.append(" from dm_cabinet where (is_private=0 or owner_name=USER) and a_is_hidden=false order by 1");
        }
        if(Trace.COMPONENT)
            com.documentum.web.common.Trace.println("batchBuildObjectListQuery query: " + stringbuffer.toString());
        return stringbuffer.toString();
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

    //private static final String ADDLN_ATTRIBUTES[] = {
    //    "p_on_hold", "p_rawunit_count", "p_article_count", "p_performer_for_display", "p_sched_timestamp", "p_last_activity", "p_problem_state_count", "p_performer", "p_user_action_taken", "p_reentry_activity", "p_workflow_id", "p_state"
    //};
    private static final String ADDLN_ATTRIBUTES[] = {
        "p_on_hold", "p_rawunit_count", "p_performer_for_display", "p_sched_timestamp", "p_last_activity", "p_problem_state_count", "p_performer", "p_user_action_taken", "p_reentry_activity", "p_workflow_id", "p_state"
    };
}