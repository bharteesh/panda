package org.portico.conprep.ui.exportbatchlistreport;

// this block of imports from search component
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.portico.conprep.db.ConnectionManager;
import org.portico.conprep.ui.docbasequery.BaseDocbaseQueryServiceWithMyBatches;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;
import org.portico.conprep.workflow.content.BatchOperationsManager;

import com.documentum.fc.client.IDfSession;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Hidden;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

/**
 * Description	Does the following things
 * 					initiates batch list report
 * Type		CustomBatchListErrorReportTypeA
 */
public class CustomBatchListErrorReportTypeA extends Component
{

    public CustomBatchListErrorReportTypeA()
    {
		m_ReportName = "";
		m_primaryErrorCodesCommaSeparated = "";
		m_secondaryErrorCodesCommaSeparated = "";
		m_primaryErrorCodeList = new ArrayList();
		m_secondaryErrorCodeList = new ArrayList();
		m_BatchRawUnitNameMapping = new HashMap<String, ArrayList>();
		m_BatchSuStatePredecessorMapping = new HashMap<String, HashMap>();
		m_BatchSuStateRawUnitNameMapping= new HashMap<String, HashMap>();
		IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        m_combinedCookie = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

        // Combined cookies
        m_batchStatus = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_BATCH_STATUS);
        m_hold = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_HOLD_STATUS);
        m_providerId = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER);
        m_profileId = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROFILE);
        m_PrefPerformer = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PERFORMER);
		// Input value = "MM/DD/YYYY"
        m_fromCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_CREATION_DATE);
        m_toCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_CREATION_DATE);
        m_fromScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE);
        m_toScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE);

        // Direct cookies
        m_objectName = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_OBJECTNAME);
        m_lastActivity = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_LASTACTIVITY);

    }

	public String readCombinedCookie(String combinedCookieIn, String key)
	{
		final String KEY_VALUE_SEPARATOR = "=";
		final String COMBINED_COOKIE_SEPARATOR = "|";
		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-readCombinedCookie()-combinedCookieIn="+combinedCookieIn);

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

		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-readCombinedCookie()-key="+key+":"+"value="+value);

		return value;
	}

	public void onInit(ArgumentList args)
	{
		super.onInit(args);

		m_ReportName=args.get("reportname").trim();
		HelperClass.porticoOutput("CustomBatchListErrorReportTypeA onInit() for m_ReportName="+m_ReportName);
		if(null != args.get("primaryerrorcodes"))
		{
	    	m_primaryErrorCodesCommaSeparated=args.get("primaryerrorcodes").trim();
	    }
		HelperClass.porticoOutput("CustomBatchListErrorReportTypeA onInit() for primaryerrorcodes="+m_primaryErrorCodesCommaSeparated);
		if(null != args.get("secondaryerrorcodes"))
		{
	    	m_secondaryErrorCodesCommaSeparated=args.get("secondaryerrorcodes").trim();
	    }
		HelperClass.porticoOutput("CustomBatchListErrorReportTypeA onInit() for secondaryerrorcodes="+m_secondaryErrorCodesCommaSeparated);

		initializeCommonInfo(args);
		initializeCommonControls();
	}

	public void	initializeCommonInfo(ArgumentList args)
	{
    }

    public void initializeCommonControls()
    {
	}

	public void onRender()
	{
		super.onRender();
	}

	// methods onRunSearch & setComponentPage from search component
    public void onRunSearch(Control control, ArgumentList argumentlist)
    {
    	System.out.println("onRunSearch()");
    	createXlsData();
        setComponentPage("results");
    }

    public void setComponentPage(String s)
    {
    	System.out.println("setComponentPage()");
        if(s.equals("start"))
        {
            Hidden hidden = (Hidden)getControl("waitRequestId", com.documentum.web.form.control.Hidden.class);
            hidden.setValue(String.valueOf(m_waitRequestId++));
        }
        super.setComponentPage(s);
    }

    public static String getBlobAsString(Blob contentBlob, boolean isLimitable)
    {
        String strContent = "";
   		InputStream inputStream = null;
   		int charCount = 0;
   		boolean isTruncated = false;
   		try
   		{
   	        if(contentBlob != null && contentBlob.length() > 0)
   	        {
   	        	inputStream = contentBlob.getBinaryStream();
   	    		if(inputStream != null)
   	    		{
   	    			BufferedInputStream bis = new BufferedInputStream(inputStream);
   	    			ByteArrayOutputStream baos = new ByteArrayOutputStream();
   	    			int tempChar;
   	    			while ( (tempChar = bis.read()) != -1)
   	    			{
   	    				baos.write(tempChar);
   	    				charCount++;
   	    				if(true == isLimitable && charCount >= EXCEL_CELL_CHAR_LIMIT)
   	    				{
							// Append "...TRUNCATED...."
							isTruncated = true;
							break;
						}
	    			}
   	    			strContent = baos.toString("utf-8");// To preserve utf-8 character set
   	    			if(true == isTruncated)
   	    			{
						strContent += TRUNCATION_STRING;
					}
	    		}
	    	}
   		}
   		catch(Exception e)
   		{
   			e.printStackTrace();
   		}
   		finally
   		{
   			try
   			{
   				if(null != inputStream)
   				{
   					inputStream.close();
				}
   			}
   			catch(Exception eClose)
   			{
   				eClose.printStackTrace();
   			}
   		}

       	HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-getBlobAsString-strContent="+strContent);
       	HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-getBlobAsString-charCount="+charCount+",strContentlength="+strContent.length());

    	return strContent;
	}

	public IDfSession getCurrentDfSession()
	{
		return getDfSession();
	}

	public String getLoginUserName(){
		System.out.println("getLoginUserName()");
		String loginName = "";
		try {
			loginName = getDfSession().getLoginUserName();
		} catch(Exception e) {}
		return loginName;
	}

	public String getReportName()
	{
		return m_ReportName;
	}

	public String getPrimaryErrorCodeInSqlCommaFormatAndPopulatePrimaryErrorCodeList()
	{
		String primaryErrorCodeInSqlCommaFormat = "";

		if(null != m_primaryErrorCodesCommaSeparated && !m_primaryErrorCodesCommaSeparated.equals(""))
		{
            if(-1 != m_primaryErrorCodesCommaSeparated.indexOf(COMMA))
            {
		    	StringTokenizer strTok = new StringTokenizer(m_primaryErrorCodesCommaSeparated, COMMA);
		    	while(strTok.hasMoreTokens())
		    	{
		    		String token = strTok.nextToken().trim();
		    		if(primaryErrorCodeInSqlCommaFormat.equals(""))
		    		{
		    			primaryErrorCodeInSqlCommaFormat = "'" + token + "'";
		    		}
		    		else
		    		{
		    			primaryErrorCodeInSqlCommaFormat = primaryErrorCodeInSqlCommaFormat + "," + "'" + token + "'";
		    		}
		    		m_primaryErrorCodeList.add(token);
		    	}
		    }
		    else
		    {
		    	primaryErrorCodeInSqlCommaFormat = "'" + m_primaryErrorCodesCommaSeparated + "'";
		    	m_primaryErrorCodeList.add(m_primaryErrorCodesCommaSeparated);
		    }
	    }

        return primaryErrorCodeInSqlCommaFormat;
	}

	public String getSecondaryErrorCodeInSqlCommaFormatAndPopulateSecondaryErrorCodeList()
	{
		String secondaryErrorCodeInSqlCommaFormat = "";

		if(null != m_secondaryErrorCodesCommaSeparated && !m_secondaryErrorCodesCommaSeparated.equals(""))
		{
            if(-1 != m_secondaryErrorCodesCommaSeparated.indexOf(COMMA))
            {
		    	StringTokenizer strTok = new StringTokenizer(m_secondaryErrorCodesCommaSeparated, COMMA);
		    	while(strTok.hasMoreTokens())
		    	{
		    		String token = strTok.nextToken().trim();
		    		if(secondaryErrorCodeInSqlCommaFormat.equals(""))
		    		{
		    			secondaryErrorCodeInSqlCommaFormat = "'" + token + "'";
		    		}
		    		else
		    		{
		    			secondaryErrorCodeInSqlCommaFormat = secondaryErrorCodeInSqlCommaFormat + "," + "'" + token + "'";
		    		}
		    		m_secondaryErrorCodeList.add(token);
		    	}
		    }
		    else
		    {
		    	secondaryErrorCodeInSqlCommaFormat = "'" + m_secondaryErrorCodesCommaSeparated + "'";
		    	m_secondaryErrorCodeList.add(m_secondaryErrorCodesCommaSeparated);
		    }
	    }

        return secondaryErrorCodeInSqlCommaFormat;
	}

	public String getCommonBatchListSubSqlString()
	{
		StringBuffer sb = new StringBuffer(1000);

	    // For "All" providers, we receive a ""
	    if(m_providerId == null || m_providerId.equals(""))
	    {
		}
		else if(m_providerId.equals(BaseDocbaseQueryServiceWithMyBatches.MYBATCHES))
	    {
			// Currently 'MYBATCHES' scenario is not available
	        sb.append(" AND batch.p_performer_for_display LIKE ");
	        sb.append("'%");
	        sb.append(getLoginUserName());
	        sb.append("%'");
		}
		else
		{
			// Build the clause if it is a Provider Path eg: '/Batches/AMS'
			// select object_name, p_provider_id, p_state from p_batch where folder('/Batches/AMS') and p_state = 'PROBLEM'
			//String providerId = getProviderId(m_provider);
			sb.append(" AND batch.p_provider_id=");
			sb.append("'");
			sb.append(m_providerId);
			sb.append("'");
		}

	    // Start Profile
	    // For "All" profiles, we receive a ""
	    if(m_profileId == null || m_profileId.equals(""))
	    {
		}
		else
		{
			sb.append(" AND batch.p_profile_id=");
			sb.append("'");
			sb.append(m_profileId);
			sb.append("'");
		}


	    //End Profile

		if(m_PrefPerformer != null && !m_PrefPerformer.equals(""))
		{
	        sb.append(" AND batch.p_performer_for_display LIKE ");
	        sb.append("'%");
	        sb.append(m_PrefPerformer);
	        sb.append("%'");
		}

	    // Do a case insensitive search
		if(m_objectName != null && !m_objectName.equals(""))
		{
	        sb.append(" AND UPPER(batch.p_name) LIKE ");
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
	        sb.append(" AND UPPER(batch.p_last_activity) LIKE ");
	        // Note: A wild card % would be keyed in by the user, if that was the user's intention
	        // sb.append("'%");
	        sb.append("'");
	        sb.append(m_lastActivity.toUpperCase());
	        sb.append("'");
	        // sb.append("%'");
		}

		if(m_batchStatus != null && !m_batchStatus.equals(""))
		{
			String statusClause = BaseDocbaseQueryServiceWithMyBatches.buildInClause(m_batchStatus);
	        sb.append(" AND batch.p_state IN (");
	        sb.append(statusClause);
	        sb.append(")");
		}

	    if(m_hold != null && !m_hold.equals(""))
	    {
			try
			{
				boolean boolValue = Boolean.valueOf(m_hold).booleanValue();
				String strOnHold = "N";
				if(boolValue) {
					strOnHold = "Y";
				}
	            sb.append(" AND batch.p_on_hold='");
	            sb.append(strOnHold + "' ");
			}
			catch(Exception e)
			{
				HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeA-getCommonBatchListSubSqlString-where Hold="+m_hold+", Exception="+e.toString());
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

		// whereFromCreationDate/whereToCreationDate

		if(createDateType == 1)
		{
	        sb.append(" AND TRUNC(batch.p_create_timestamp) = ");
	        sb.append("to_date('"+commonCreateDate+"','mm/dd/yyyy')");

	    }
	    else if(createDateType == 2)
	    {
	        sb.append(" AND TRUNC(batch.p_create_timestamp) >= ");
	        sb.append("to_date('"+m_fromCreationDate+"','mm/dd/yyyy')");
	        sb.append(" AND TRUNC(batch.p_create_timestamp) <= ");
	        sb.append("to_date('"+m_toCreationDate+"','mm/dd/yyyy')");
	    }

	    //ScheduleDate
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

		// whereFromScheduleDate/whereToScheduleDate

		if(scheduleDateType == 1)
		{
	        sb.append(" AND TRUNC(batch.p_sched_timestamp) = ");
	        sb.append("to_date('"+commonScheduleDate+"','mm/dd/yyyy')");
	    }
	    else if(scheduleDateType == 2)
	    {
	        sb.append(" AND TRUNC(batch.p_sched_timestamp) >= ");
	        sb.append("to_date('"+m_fromScheduleDate+"','mm/dd/yyyy')");
	        sb.append(" AND TRUNC(batch.p_sched_timestamp) <= ");
	        sb.append("to_date('"+m_toScheduleDate+"','mm/dd/yyyy')");
	    }

	    return sb.toString();
	}

/*
 select p_raw_unit.p_accession_id p_raw_unit_accession_id, p_raw_unit.p_batch_accession_id p_raw_unit_batch_accession_id, p_raw_unit.p_name p_raw_unit_name, p_raw_unit.p_content_reason p_content_reason,
        p_su.p_batch_accession_id p_su_batch_accession_id, p_su.p_accession_id p_su_accession_id, p_su.p_predecessor_id p_su_predecessor_id
 from p_raw_unit, p_su
 where p_su.p_batch_accession_id='ark:/27927/dd02nw8hn'
       and p_su.p_batch_accession_id=p_raw_unit.p_batch_accession_id(+)
       and p_su.p_raw_unit_id=p_raw_unit.p_accession_id(+)
 order by p_su.p_batch_accession_id asc
*/
	public ArrayList getRawUnitFileNameList(Connection con, String batchAccessionId, String suStateId)
	{
		ArrayList retRawUnitFileNameList = new ArrayList();

		try
		{
			if(!m_BatchRawUnitNameMapping.containsKey(batchAccessionId))
			{
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try
                {
					ArrayList allRawUnitFileNameListPerBatch = new ArrayList();
					HashMap suStateIdPredecessorIdMapPerBatch = new HashMap();
					HashMap suStateIdRawUnitNameMapPerBatch = new HashMap();
    		        String p_batch_accession_id = "";
    		        StringBuffer sb = new StringBuffer(1000);
                    sb.append("select p_raw_unit.p_accession_id p_raw_unit_accession_id, p_raw_unit.p_batch_accession_id p_raw_unit_batch_accession_id, p_raw_unit.p_name p_raw_unit_name, p_raw_unit.p_user_added p_user_added, ");
                    sb.append(" p_su.p_batch_accession_id p_su_batch_accession_id, p_su.p_accession_id p_su_accession_id, p_su.p_predecessor_id p_su_predecessor_id ");
                    sb.append(" from p_raw_unit, p_su ");
                    sb.append(" where p_su.p_batch_accession_id=? ");
                    sb.append(" and p_su.p_batch_accession_id=p_raw_unit.p_batch_accession_id(+) ");
                    sb.append(" and p_su.p_raw_unit_id=p_raw_unit.p_accession_id(+)");
                    String sqlQry = sb.toString();
            	    HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA sqlQry="+sqlQry);
            	    pstmt = con.prepareStatement(sqlQry);
                    pstmt.setString(1, batchAccessionId);
            	    HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA getRawUnitFileNameList-Before executeQuery");
        	        rs = pstmt.executeQuery();
    		        HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA getRawUnitFileNameList-After executeQuery");
        	        while (rs.next())
        	        {
					    p_batch_accession_id = rs.getString("p_su_batch_accession_id");
					    boolean p_user_added = rs.getBoolean("p_user_added");
					    // PMD2.0, p_content_reason->p_user_added
					    // if(null != p_content_reason && p_content_reason.equals("0"))
					    // If it was originally a submitted file
					    if(false == p_user_added)
					    {
							String currentRawUnitName = rs.getString("p_raw_unit_name");
							if(!allRawUnitFileNameListPerBatch.contains(currentRawUnitName))
							{
					            allRawUnitFileNameListPerBatch.add(currentRawUnitName);
						    }
					    }
					    String p_su_predecessor_id = rs.getString("p_su_predecessor_id");
					    String p_su_accession_id = rs.getString("p_su_accession_id");

					    if(null != p_su_predecessor_id && !p_su_predecessor_id.equals("") &&
						        null != p_su_accession_id && !p_su_accession_id.equals(""))
					    {
							suStateIdPredecessorIdMapPerBatch.put(p_su_accession_id, p_su_predecessor_id);
						}

						String p_raw_unit_accession_id = rs.getString("p_raw_unit_accession_id");
					    if(null != p_raw_unit_accession_id && !p_raw_unit_accession_id.equals("") &&
						        null != p_su_accession_id && !p_su_accession_id.equals(""))
					    {
							// If p_raw_unit_accession_id is present then 'p_raw_unit_name' will definitely be present
							suStateIdRawUnitNameMapPerBatch.put(p_su_accession_id, rs.getString("p_raw_unit_name"));
						}
					}

					if(null != p_batch_accession_id && !p_batch_accession_id.equals(""))
					{
						if(null != allRawUnitFileNameListPerBatch && allRawUnitFileNameListPerBatch.size() > 0)
						{
					        m_BatchRawUnitNameMapping.put(p_batch_accession_id, allRawUnitFileNameListPerBatch);
					    }
					    if(null != suStateIdPredecessorIdMapPerBatch && suStateIdPredecessorIdMapPerBatch.size() > 0)
					    {
					        m_BatchSuStatePredecessorMapping.put(p_batch_accession_id, suStateIdPredecessorIdMapPerBatch);
					    }
					    if(null != suStateIdRawUnitNameMapPerBatch && suStateIdRawUnitNameMapPerBatch.size() > 0)
					    {
							m_BatchSuStateRawUnitNameMapping.put(p_batch_accession_id, suStateIdRawUnitNameMapPerBatch);
						}
					}
				}
				catch(Exception e)
				{
					HelperClass.porticoOutput(1,"Exception in CustomBatchListErrorReportTypeA-getRawUnitFileNameList during mapping caputre for batchAccessionId="+batchAccessionId+","+e.getMessage());
					e.printStackTrace();
				}
				finally
				{
                    try
                    {
						if (null != rs)
						{
						    rs.close();
						}
					}
                    catch (Exception e)
                    {
				    }
        	        try
        	        {
						if (null != pstmt)
						{
					    	pstmt.close();
					    }
					}
        	        catch (Exception e)
        	        {
					}
				}
			}

			ArrayList<String> topLevelSuppliedSuStateIdList = getTopLevelSuppliedSuStateIdList(suStateId, batchAccessionId, (HashMap)m_BatchSuStatePredecessorMapping.get(batchAccessionId));

            boolean isPickAllRawUnits = false;
			if(null != topLevelSuppliedSuStateIdList && topLevelSuppliedSuStateIdList.size() > 0)
			{
                HashMap tBatchSuStateRawUnitNameMapping =  (HashMap)m_BatchSuStateRawUnitNameMapping.get(batchAccessionId);
			    for(String topLevelSuppliedSuStateId : topLevelSuppliedSuStateIdList)
			    {
			    	String rawUnitName = "";
			    	if(null != tBatchSuStateRawUnitNameMapping && tBatchSuStateRawUnitNameMapping.size() > 0)
			    	{
                        rawUnitName = (String)tBatchSuStateRawUnitNameMapping.get(topLevelSuppliedSuStateId);
                        if(null != rawUnitName && !rawUnitName.equals("") && !retRawUnitFileNameList.contains(rawUnitName))
                        {
                            retRawUnitFileNameList.add(rawUnitName);
			    	    }
			    	}
			    	if(null == rawUnitName || rawUnitName.equals(""))
			    	{
						isPickAllRawUnits = true;
			    		// Since we are getting all the raw unit name(s), we need not walk thro' further
			    		break;
			    	}
			    }
		    }
		    else
		    {
				// We have not been able to get to the top level supplied file(s), there could have been some
				// predecessor linkages missing OR the CU (Article) did not have a MD file
				// Just pick all the raw unit(s) tied to this Batch
				isPickAllRawUnits = true;
			}

			if(true == isPickAllRawUnits)
			{
	    		if(null != m_BatchRawUnitNameMapping && m_BatchRawUnitNameMapping.size() > 0)
	    		{
	    	    	retRawUnitFileNameList = (ArrayList)m_BatchRawUnitNameMapping.get(batchAccessionId);
	    	    }
	    	    else
	    	    {
	    			HelperClass.porticoOutput(1,"Error in CustomBatchListErrorReportTypeA-getRawUnitFileNameList-No mapping for Batch Raw Units-No Raw Unit found for the Batch batchAccessionId="+batchAccessionId);
	    		}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1,"Exception in CustomBatchListErrorReportTypeA-getRawUnitFileNameList-suStateId()="+suStateId+","+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		return retRawUnitFileNameList;
	}

    // Note: multiplePredecessorIdListOut has to be initialized before passing to this method
	public ArrayList getTopLevelSuppliedSuStateIdList(String suStateId, String batchAccessionId, HashMap batchSuStatePredecessorMappingPerBatch)
	{
		ArrayList topLevelSuppliedSuStateIdList = new ArrayList();
		ArrayList pendingPredecessorIdList = new ArrayList();

        String predecessorId = "";

        if(null != batchSuStatePredecessorMappingPerBatch && batchSuStatePredecessorMappingPerBatch.size() > 0)
        {
            predecessorId = (String)batchSuStatePredecessorMappingPerBatch.get(suStateId);
	    }
		if(null == predecessorId || predecessorId.equals(""))
		{
			topLevelSuppliedSuStateIdList.add(suStateId);
		}
		else if(predecessorId.contains(BatchOperationsManager.MULTIPLE_PREDECESSOR_SEPARATOR_STRING))
		{
			StringTokenizer stringTokenizer = new StringTokenizer(predecessorId, BatchOperationsManager.MULTIPLE_PREDECESSOR_SEPARATOR_STRING);
			while(stringTokenizer.hasMoreTokens())
			{
			    pendingPredecessorIdList.add(stringTokenizer.nextToken().trim());
			}
		}
		else
		{
			pendingPredecessorIdList.add(predecessorId);
		}

		while(null != pendingPredecessorIdList && pendingPredecessorIdList.size() > 0)
		{
            predecessorId = (String)pendingPredecessorIdList.remove(pendingPredecessorIdList.size()-1);
	    	while(null != predecessorId && !predecessorId.equals(""))
	    	{
				// Get the next predecessor
			   	String nextPredecessorId = (String)batchSuStatePredecessorMappingPerBatch.get(predecessorId);
			   	if(null == nextPredecessorId ||  nextPredecessorId.equals(""))
			   	{
			   		// Got the top level supplied file, done
			   		if(!topLevelSuppliedSuStateIdList.contains(predecessorId))
			   		{
			   	    	topLevelSuppliedSuStateIdList.add(predecessorId);
				    }
			   		break;
			   	}
			   	else if(nextPredecessorId.contains(BatchOperationsManager.MULTIPLE_PREDECESSOR_SEPARATOR_STRING))
			   	{
			   		// Contains multiple, capture list and send back
			   		StringTokenizer stringTokenizer = new StringTokenizer(nextPredecessorId, BatchOperationsManager.MULTIPLE_PREDECESSOR_SEPARATOR_STRING);
			   		while(stringTokenizer.hasMoreTokens())
			   		{
						String strToken = stringTokenizer.nextToken().trim();
						if(!pendingPredecessorIdList.contains(strToken))
						{
    		    		    pendingPredecessorIdList.add(strToken);
						}
			   		}
			   		break;
			   	}
			   	predecessorId = nextPredecessorId;
			}
	    }

        return topLevelSuppliedSuStateIdList;
	}

    public void createXlsData() {
    	Hidden rpath = (Hidden)getControl("realpathId", com.documentum.web.form.control.Hidden.class);
    	String path = rpath.getValue();
    	System.out.println("CustomBatchListErrorReportTypeA-createXlsData() path="+path);
    	String loginName="";
    	String tempFileLocation = null;
    	HSSFWorkbook wb = null;
    	String username = "";
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;

    	StringBuffer sb = new StringBuffer(1000);
    	try
    	{
    		Map<String, Set<String>> errorsMap = new TreeMap<String, Set<String>>();
        	String sqlQry = buildSqlQueryString();
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-createXlsData-sqlQry="+sqlQry);
    		username = getLoginUserName();

    		tempFileLocation = path+"/temp/"+username+m_ReportName+"-Instance.xls";
    	    //String template = application.getRealPath("/custom/library/export/templates/MissingElementsReportSheet.xls");
    	    String template = path+"/templates/"+m_ReportName+"-Template.xls";

    	    // Always start our data from rowcount = 4,
    	    // row=0 -> BLANK line
    	    // row=1 -> Description line
    	    // row=2 -> Heading line
    	    // row=3 -> Our data population starts from here

    	    POIFSFileSystem poiFileSystem = new POIFSFileSystem( new FileInputStream(template));
    	    wb = new HSSFWorkbook(poiFileSystem);

    	    HSSFSheet styleSheet = wb.getSheetAt(2);
    	    HSSFCellStyle headerStyle = styleSheet.getRow(0).getCell(0).getCellStyle();
    	    HSSFCellStyle textStyle1 = styleSheet.getRow(1).getCell(0).getCellStyle();
    	    HSSFCellStyle textStyle2 = styleSheet.getRow(2).getCell(0).getCellStyle();

    	    HSSFSheet sheet = wb.getSheetAt(0);
    	    HSSFRow row = null;
    	    HSSFRow preRow = null;;
    	    HSSFCell cell = null;
    	    HSSFCellStyle textStyle = null;
    	    int rowNo = DATA_START_ROW_NUMBER;
    	    int cellNo = 0;
    	    int count = 0;
    	    con = ConnectionManager.getConnection();

            String preContextId = "";
            String currentContextId = "";
            String additionalInformation = ""; // Details from other errors associated with the same contextId.
    	    pstmt = con.prepareStatement(sqlQry);
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-createXlsData Before executeQuery");
        	rs = pstmt.executeQuery();
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-createXlsData After executeQuery");
        	while (rs.next())
        	{
				currentContextId = rs.getString("p_context_id");

                // If the user message context is different it is a different error segment(different row)
                // This is because the query not only returns the errors associated with the passed in error code
                // but also goes one step ahead to gather all the other errors tied to this particular error context.
                // Hence, we need to weed out the other additional results tied to a same context after picking and
                // grouping the same context details

                if(currentContextId.equals(preContextId))
                {
					// working on the same row
			    }
                else
                {
					preContextId = currentContextId;

					// Populate the cumulative info.here to the preRow
                    if(null != additionalInformation && null != preRow)
                    {
                		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-createXlsData-Accumulation state");
        				cell = preRow.createCell(cellNo);
         				cell.setCellStyle(textStyle);
        				cell.setCellValue(additionalInformation);
        				cellNo++;
        				// Clean up for next grouping, if any
         				additionalInformation = "";
        			}
				}

				if(m_primaryErrorCodeList.contains(rs.getString("p_code").trim())) // eg: C494
				{
        		    if(count%2 == 0)
        		    {
        		    	textStyle = textStyle1;
        		    }
        		    else
        		    {
        		    	textStyle = textStyle2;
        		    }
					// Populate all normal info. for this group
        		    cellNo = 0;
        		    row = sheet.createRow(rowNo);
					preRow = row; // This preRow will be used to set the Cumulative(eg: Additional Information)

        		    // Error Code
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_code"));
        		    cellNo++;

        		    // Batch Name
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_batch_name"));
        		    cellNo++;

        		    // Provider ID
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_provider_id"));
        		    cellNo++;

        		    // Profile ID
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_profile_id"));
        		    cellNo++;

        		    // Last Activity
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_last_activity"));
        		    cellNo++;

        		    // Status
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_state"));
        		    cellNo++;

        		    // Context Id
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_context_id"));
        		    cellNo++;

        		    // Context Details
        		    String contextDetails = rs.getString("p_object_name");
        		    // Note: Depending on the p_object_type, one would populated and the other is "",
        		    // eg: For p_cu, p_display_label is populated
        		    // eg: For p_su, p_work_filename is populated
        		    if(rs.getString("p_object_type").equals("p_cu"))
        		    {
						contextDetails += " \n ["+rs.getString("p_display_label")+"]";
					}
					else if(rs.getString("p_object_type").equals("p_su"))
					{
						contextDetails += " \n ["+rs.getString("p_work_filename")+"]";
					}
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(contextDetails);
        		    cellNo++;

        		    // Error Text
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(rs.getString("p_text"));
        		    cellNo++;

        		    // Additional Text
        		    String strContent = getBlobAsString(rs.getBlob("p_content"), true);
        		    cell = row.createCell(cellNo);
        		    cell.setCellStyle(textStyle);
        		    cell.setCellValue(strContent);
        		    cellNo++;

        		    // RawUnit Filename
        		    String formattedRawUnitFileName = "";
        		    if(rs.getString("p_object_type").equals("p_su"))
        		    {
        		        ArrayList<String> rawUnitFileNameList = getRawUnitFileNameList(con, rs.getString("p_batch_accession_id"), rs.getString("p_context_id"));
        		        for(String rawUnitFileName : rawUnitFileNameList)
        		        {
							formattedRawUnitFileName = formattedRawUnitFileName + rawUnitFileName + "\n";
						}
					}
       		        cell = row.createCell(cellNo);
       		        cell.setCellStyle(textStyle);
       		        cell.setCellValue(formattedRawUnitFileName);
        		    cellNo++;

                    // Only if the Primary error code is found, we populate normal stuff and
                    //      increment the rowNo, count etc.
        		    rowNo++;
        		    count++;
			    }
			    else
			    {
					// Keep accumulating, they belong to the same context.
                    additionalInformation = additionalInformation + "\n"+ rs.getString("p_code") +
                                                                "\n"+ getBlobAsString(rs.getBlob("p_content"), true);
				}
           	}

			// Populate the cumulative info.here to the preRow(The last guy)
            if(null != additionalInformation && null != preRow)
            {
           		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeA-createXlsData-Accumulation state-Last Row");
				cell = preRow.createCell(cellNo);
				cell.setCellStyle(textStyle);
				cell.setCellValue(additionalInformation);
				cellNo++;
				// Clean up for next grouping, if any
				additionalInformation = "";
			}
/*
        	// Summary Report
        	row = null;
        	cell = null;
        	HSSFSheet reportSheet = wb.getSheetAt(1);
    	    cellNo = 0;
    	    rowNo = 1;
            Set<String> keySet = errorsMap.keySet();
    		Iterator<String> itr = keySet.iterator();
    		while(itr.hasNext()) {
    			String errCode = itr.next();
    			row = reportSheet.createRow(rowNo);
    			cell = row.createCell(cellNo);
    			cell.setCellStyle(headerStyle);
    			cell.setCellValue("Error Code: "+errCode);
    			rowNo++;

    			Set<String> errorSet = (TreeSet<String>)errorsMap.get(errCode);
    			Iterator<String> iterator = errorSet.iterator();

    			int flip = 0;
    			while(iterator.hasNext()) {
    				row = reportSheet.createRow(rowNo);
    				cell = row.createCell(cellNo);
    				if(flip == 0) {
    					cell.setCellStyle(textStyle1);
    					flip = 1;
    				}
    				else {
    					cell.setCellStyle(textStyle2);
    					flip = 0;
    				}
    				String content = iterator.next();
    				cell.setCellValue(content);
    				rowNo++;
    			}

    		}
*/

        } catch (Exception e) {
        	e.printStackTrace();
        	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeA-CreateXlsData() "+e.getMessage());

        } finally {
        	try {if (null != rs) rs.close();}
        	catch (Exception e) {}
        	try {if (null != pstmt) pstmt.close();}
        	catch (Exception e) {}
        	try {if (null != con) ConnectionManager.closeConnection(con);}
        	catch (Exception e) {}

        }
        try {
        File tempFile = new File(tempFileLocation);
        if( tempFile.exists()){
            tempFile.delete();
        }
        tempFile = null;

        FileOutputStream fileOut = new FileOutputStream(tempFileLocation);

        wb.write(fileOut);
        fileOut.flush();
        fileOut.close();
        wb = null;
        } catch (Exception e) {
        	e.printStackTrace();
        	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeA-CreateXlsData() post-finally "+e.getMessage());
        }

    }

/*
select batch.P_NAME p_batch_name, batch.P_PROVIDER_ID p_provider_id, batch.P_PROFILE_ID p_profile_id, batch.P_LAST_ACTIVITY p_last_activity, batch.P_STATE p_state,
 p_umsg.P_BATCH_ACCESSION_ID p_batch_accession_id, p_umsg.P_CATEGORY p_category, p_umsg.P_CODE p_code, p_umsg.P_CONTENT_ID p_content_id, p_umsg.P_CONTENT_TYPE p_content_type, p_umsg.P_CONTEXT_ID p_context_id, p_umsg.P_CONTEXT_TYPE p_context_type, p_umsg.P_ID p_msg_id, p_umsg.P_IS_ACTION_TAKEN p_is_action_taken, p_umsg.P_MIME_TYPE p_mime_type, p_umsg.P_SEVERITY p_severity, p_umsg.P_TEXT p_text,
 v_customreport.P_NAME p_object_name, v_customreport.P_DISPLAY_LABEL p_display_label, v_customreport.P_FU_TYPE p_fu_type, v_customreport.P_OBJECT_TYPE p_object_type, v_customreport.P_PARENT_ID p_parent_id, v_customreport.P_WORK_FILENAME p_work_filename
 from
 p_batch batch,
 p_user_message p_umsg,
 V_CUSTOMREPORT_UI v_customreport,
 p_content p_content
 where
 batch.P_ACCESSION_ID=p_umsg.P_BATCH_ACCESSION_ID AND batch.P_ACCESSION_ID=v_customreport.P_BATCH_ACCESSION_ID AND p_umsg.P_CONTEXT_ID=v_customreport.P_CONTEXT_ID
 AND v_customreport.p_code in
 ('C494')
 AND p_umsg.p_content_id = p_content.p_id(+)
 and p_umsg.p_code in  ('C494','C486')
 AND v_customreport.P_BATCH_ACCESSION_ID='ark:/27927/dd05p1kct'
 order by v_customreport.P_BATCH_ACCESSION_ID, v_customreport.P_CONTEXT_ID, p_umsg.P_SEVERITY desc;
*/
	public String buildSqlQueryString()
	{
		System.out.println("CustomBatchListErrorReportTypeA-buildSqlQueryString()");
		String primaryErrorCodeInSqlCommaFormat = getPrimaryErrorCodeInSqlCommaFormatAndPopulatePrimaryErrorCodeList();
		String secondaryErrorCodeInSqlCommaFormat = getSecondaryErrorCodeInSqlCommaFormatAndPopulateSecondaryErrorCodeList();

		StringBuffer sb = new StringBuffer(1000);

// LEADING PART of QUERY
        sb.append("select batch.P_NAME p_batch_name, batch.P_PROVIDER_ID p_provider_id, batch.P_PROFILE_ID p_profile_id, batch.P_LAST_ACTIVITY p_last_activity, batch.P_STATE p_state,");
		sb.append("p_umsg.P_BATCH_ACCESSION_ID p_batch_accession_id, p_umsg.P_CATEGORY p_category, p_umsg.P_CODE p_code, p_umsg.P_CONTENT_ID p_content_id, p_umsg.P_CONTENT_TYPE p_content_type, p_umsg.P_CONTEXT_ID p_context_id, p_umsg.P_CONTEXT_TYPE p_context_type, p_umsg.P_ID p_msg_id, p_umsg.P_IS_ACTION_TAKEN p_is_action_taken, p_umsg.P_MIME_TYPE p_mime_type, p_umsg.P_SEVERITY p_severity, p_umsg.P_TEXT p_text,");
		sb.append("v_customreport.P_NAME p_object_name, v_customreport.P_DISPLAY_LABEL p_display_label, v_customreport.P_FU_TYPE p_fu_type, v_customreport.P_OBJECT_TYPE p_object_type, v_customreport.P_PARENT_ID p_parent_id, v_customreport.P_WORK_FILENAME p_work_filename,");
		sb.append("p_content.P_CONTENT p_content ");
		sb.append(" from ");
		sb.append(" p_batch batch, ");
		sb.append(" p_user_message p_umsg, ");
		sb.append(" V_CUSTOMREPORT_UI v_customreport, ");
		sb.append(" p_content p_content ");
		sb.append(" where ");
		sb.append(" batch.P_ACCESSION_ID=p_umsg.P_BATCH_ACCESSION_ID AND batch.P_ACCESSION_ID=v_customreport.P_BATCH_ACCESSION_ID AND p_umsg.P_CONTEXT_ID=v_customreport.P_CONTEXT_ID ");

		if(null != primaryErrorCodeInSqlCommaFormat && !primaryErrorCodeInSqlCommaFormat.equals(""))
		{
		    sb.append(" AND v_customreport.p_code in ");
		    sb.append(" ( ");
		    sb.append(primaryErrorCodeInSqlCommaFormat);
		    sb.append(" ) ");
	    }
// secondaryErrorCodeInSqlCommaFormat
// and p_umsg.p_code in  ('C494','C486')
// It must be a combo, because the p_usmg code must pick all primary and secondary codes
// Note: PrimaryErrorCode(s) are mandatory
		if(null != secondaryErrorCodeInSqlCommaFormat && !secondaryErrorCodeInSqlCommaFormat.equals(""))
		{
		    sb.append(" AND p_umsg.p_code in ");
		    sb.append(" ( ");
		    sb.append(primaryErrorCodeInSqlCommaFormat+","+secondaryErrorCodeInSqlCommaFormat);
		    sb.append(" ) ");
	    }

        // This is common for any query from the Batchl list screen, all the cookies etc.
// COMMON PART of QUERY
	    sb.append(" "+ getCommonBatchListSubSqlString() + " ");

 // FINAL PART of QUERY
		sb.append(" AND p_umsg.p_content_id = p_content.p_id(+) ");
		sb.append(" order by v_customreport.P_BATCH_ACCESSION_ID, v_customreport.P_CONTEXT_ID, p_umsg.P_SEVERITY desc ");

		return sb.toString();
	}

	private String m_combinedCookie;
    // Combined cookies
    private String m_batchStatus;
    private String m_hold;
    private String m_providerId;
    private String m_profileId;
    private String m_PrefPerformer;
	// Input value = "MM/DD/YYYY"
    private String m_fromCreationDate;
    private String m_toCreationDate;
    private String m_fromScheduleDate;
    private String m_toScheduleDate;

    // Direct cookies
    private String m_objectName;
    private String m_lastActivity;

	public String m_ReportName;
	public String m_primaryErrorCodesCommaSeparated;
	public ArrayList m_primaryErrorCodeList;
	public String m_secondaryErrorCodesCommaSeparated;
	public ArrayList m_secondaryErrorCodeList;

	public HashMap m_BatchRawUnitNameMapping;
	public HashMap m_BatchSuStatePredecessorMapping;
	public HashMap m_BatchSuStateRawUnitNameMapping;

	// class variable from search component
    private static int m_waitRequestId = 0;

    public static final String COMMA = ",";
    public static final int DATA_START_ROW_NUMBER = 4; // Rows 0,1,2,3 already filled in the template.
    public static final int EXCEL_CELL_CHAR_LIMIT = 30000;
    public static final String TRUNCATION_STRING = "....Data Truncated....";
}
