/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * File           	ExportBatchListReport.java
 * Created on 		Apr 01, 2008
 *
 */
package org.portico.conprep.ui.exportbatchlistreport;

//this block of imports from search component
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.portico.conprep.ui.docbasequery.BaseDocbaseQueryServiceWithMyBatches;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Hidden;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

/**
 * Description	Does the following things
 * 					initiates batch list report
 * Type		ExportBatchListReport
 */
public class ExportBatchListReport extends Component
{
	/**
	 *
	 */
	public ExportBatchListReport()
	{
		m_actionType = "";
	}

	public void onInit(ArgumentList args)
	{
		super.onInit(args);

		m_actionType=args.get("actiontype");
		HelperClass.porticoOutput("ExportBatchListReport onInit() for actionType="+m_actionType);

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
        setComponentPage("results");
	  createXlsData();
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

	public void createXlsData()
	{
    	Hidden rpath = (Hidden)getControl("realpathId", com.documentum.web.form.control.Hidden.class);
    	String path = rpath.getValue();
    	System.out.println("createXlsData() path="+path);
	IDfSessionManager sessionMgr = null;
	IDfSession dfSession = null;
	IDfCollection col = null;

	IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();

	String batchName = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_OBJECTNAME);
	String lastActivity = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_LASTACTIVITY);

    String m_combinedCookie = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);


    // Combined cookies

    String status = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_BATCH_STATUS);
    String onHold = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_HOLD_STATUS);
    String providerId = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER);
    String performer = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PERFORMER);
	// Input value = "MM/DD/YYYY"
    String fromCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_CREATION_DATE);
    String toCreationDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_CREATION_DATE);
    String fromScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE);
    String toScheduleDate = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE);

    String profileId = readCombinedCookie(m_combinedCookie, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROFILE);

	String loginName="";

	StringBuffer sb = new StringBuffer(1000);
    sb.append("SELECT 1,object_name as batchname,r_object_id as sortbyobjid,p_profile_id,p_provider_id,p_state,p_on_hold,");
    //sb.append("p_rawunit_count,p_article_count,p_performer_for_display,r_creation_date,p_sched_timestamp,p_last_activity,");
    sb.append("p_rawunit_count,p_performer_for_display,r_creation_date,p_sched_timestamp,p_last_activity,");
    sb.append("p_problem_state_count,p_workflow_queue,p_queue_priority,p_accession_id,p_batch_logfile ");
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


//End Profile

	if(performer != null && !performer.equals(""))
	{
        sb.append(" AND p_performer_for_display LIKE ");
        sb.append("'%");
        sb.append(performer);
        sb.append("%'");
	}

    // Do a case insensitive search
	if(batchName != null && !batchName.equals(""))
	{
        sb.append(" AND UPPER(object_name) LIKE ");
        // Note: A wild card % would be keyed in by the user, if that was the user's intention
        // sb.append("'%");
        sb.append("'");
        sb.append(batchName.toUpperCase());
        sb.append("'");
        // sb.append("%'");
	}

    // Do a case insensitive search
	if(lastActivity != null && !lastActivity.equals(""))
	{
        sb.append(" AND UPPER(p_last_activity) LIKE ");
        // Note: A wild card % would be keyed in by the user, if that was the user's intention
        // sb.append("'%");
        sb.append("'");
        sb.append(lastActivity.toUpperCase());
        sb.append("'");
        // sb.append("%'");
	}

	if(status != null && !status.equals(""))
	{
		String statusClause = BaseDocbaseQueryServiceWithMyBatches.buildInClause(status);
        sb.append(" AND "+HelperClassConstants.BATCH_STATE+" IN (");
        sb.append(statusClause);
        sb.append(")");
	}

    if(onHold != null && !onHold.equals(""))
    {
		try
		{
			boolean boolValue = Boolean.valueOf(onHold).booleanValue();
            sb.append(" AND p_on_hold=");
            sb.append(boolValue + " ");
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in BaseDocbaseQueryServiceWithMyBatches-baseBuildObjectListQuery-whereHold="+onHold+", Exception="+e.toString());
		}
	}

    int createDateType = 0;
    String commonCreateDate = "";
	if(fromCreationDate != null && !fromCreationDate.equals("") &&
	    toCreationDate != null && !toCreationDate.equals(""))
	{
		createDateType = 2;
	}
	else
	{
		if(fromCreationDate != null && !fromCreationDate.equals(""))
		{
			createDateType = 1;
			commonCreateDate = fromCreationDate;
		}
		else if(toCreationDate != null && !toCreationDate.equals(""))
		{
			createDateType = 1;
			commonCreateDate = toCreationDate;
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
        sb.append(fromCreationDate);
        sb.append("'");
        sb.append(",");
        sb.append("'");
        sb.append("MM/DD/YYYY");
        sb.append("'");
        sb.append(")");

        sb.append(" AND DATEFLOOR(day,\"r_creation_date\") <= ");
        sb.append("DATE(");
        sb.append("'");
        sb.append(toCreationDate);
        sb.append("'");
        sb.append(",");
        sb.append("'");
        sb.append("MM/DD/YYYY");
        sb.append("'");
        sb.append(")");
	}

//ScheduleDate
    // 0 => NO creation date at all.
    // 1 => Either From/To Creation Date only
    // 2 => Both From & To Creation Date
    int scheduleDateType = 0;
    String commonScheduleDate = "";
	if(fromScheduleDate != null && !fromScheduleDate.equals("") &&
	    toScheduleDate != null && !toScheduleDate.equals(""))
	{
		scheduleDateType = 2;
	}
	else
	{
		if(fromScheduleDate != null && !fromScheduleDate.equals(""))
		{
			scheduleDateType = 1;
			commonScheduleDate = fromScheduleDate;
		}
		else if(toScheduleDate != null && !toScheduleDate.equals(""))
		{
			scheduleDateType = 1;
			commonScheduleDate = toScheduleDate;
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
        sb.append(fromScheduleDate);
        sb.append("'");
        sb.append(",");
        sb.append("'");
        sb.append("MM/DD/YYYY");
        sb.append("'");
        sb.append(")");

        sb.append(" AND DATEFLOOR(day,\"p_sched_timestamp\") <= ");
        sb.append("DATE(");
        sb.append("'");
        sb.append(toScheduleDate);
        sb.append("'");
        sb.append(",");
        sb.append("'");
        sb.append("MM/DD/YYYY");
        sb.append("'");
        sb.append(")");
	}


	sb.append(" order by r_creation_date desc");
	String dqlQry = sb.toString();
	HelperClass.porticoOutput(0, "exportBatchList.jsp-"+dqlQry);
	String tempFileLocation = null;
	HSSFWorkbook wb = null;
	String username = "";
	try {
		Date date1 = new Date();
		long longTime = date1.getTime();
		/*sessionMgr = SessionManagerHttpBinding.getSessionManager();
    	String docbase = SessionManagerHttpBinding.getCurrentDocbase();
    	dfSession = sessionMgr.getSession(docbase);*/
    	//ExportBatchListReport reportComponent = (ExportBatchListReport)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
    	dfSession = getCurrentDfSession();
		username = dfSession.getLoginUserName();

		tempFileLocation = path+"/temp/"+username+"workbook.xls";
	    String template = path+"/templates/BatchListScreenTemplate.xls";
	    POIFSFileSystem poiFileSystem = new POIFSFileSystem( new FileInputStream(template));
	    wb = new HSSFWorkbook(poiFileSystem);
	    HSSFSheet sheet = wb.getSheetAt(0);
	    int headStartRow = 1;
	    int rowNo = 5;
	    int cellNo = 0;
	    HSSFSheet styleSheet = wb.getSheetAt(1);
	    HSSFCellStyle normalStyle = styleSheet.getRow(1).getCell(0).getCellStyle();
	    HSSFCellStyle dateStyle = styleSheet.getRow(2).getCell(0).getCellStyle();

	    HSSFRow row = null;
	    HSSFCell cell = null;

	    row = sheet.getRow(headStartRow);
	    cell = row.getCell(1);
	    if(null != providerId) {
	    	cell.setCellValue(providerId);
	    }
	    else {
	    	cell.setCellValue("All");
	    }

	    cell = row.getCell(5);
	    if(null != batchName) {
	    	cell.setCellValue(batchName);
	    }

	    cell = row.getCell(8);
	    if(null != status) {
	    	cell.setCellValue(status);
	    }

	    cell = row.getCell(10);
	    if(null != fromCreationDate) {
	    	cell.setCellValue(fromCreationDate);
	    }

	    cell = row.getCell(13);
	    if(null != fromScheduleDate) {
	    	cell.setCellValue(fromScheduleDate);
	    }

	    headStartRow++;
	    row = sheet.getRow(headStartRow);
	    cell = row.getCell(1);
	    if(null != performer) {
	    	cell.setCellValue(performer);
	    }
	    else {
	    	cell.setCellValue("All");
	    }

	    cell = row.getCell(5);
	    if(null != lastActivity) {
	    	cell.setCellValue(lastActivity);
	    }

	    cell = row.getCell(8);
	    if(null != onHold) {
	    	cell.setCellValue(onHold);
	    }
	    else {
	    	cell.setCellValue("Ignore");
	    }

	    cell = row.getCell(10);
	    if(null != toCreationDate) {
	    	cell.setCellValue(toCreationDate);
	    }

	    cell = row.getCell(13);
	    if(null != toScheduleDate) {
	    	cell.setCellValue(toScheduleDate);
	    }

	    row = null;
	    cell = null;

	    IDfQuery query = new DfQuery();
    	query.setDQL(dqlQry);
    	col = query.execute(dfSession,IDfQuery.DF_EXEC_QUERY);
    	while (col.next()) {
    		cellNo = 0;
    		row = sheet.createRow(rowNo);

    		// Batch Name
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("batchname"));
    		cellNo++;

    		// Profile Name
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_profile_id"));
    		cellNo++;

    		// Provider Id
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_provider_id"));
    		cellNo++;

    		// Status
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_state"));
    		cellNo++;

    		// On Hold
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getBoolean("p_on_hold"));
    		cellNo++;

    		// # of Files
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getInt("p_rawunit_count"));
    		cellNo++;

    		// # of Articles
    		//cell = row.createCell(cellNo);
    		//cell.setCellStyle(normalStyle);
    		//cell.setCellValue(col.getInt("p_article_count"));
    		//cellNo++;

    		// Performer
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_performer_for_display"));
    		cellNo++;

    		// Creation Date
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(dateStyle);
    		if(!col.getTime("r_creation_date").isNullDate()) {
    			cell.setCellValue(col.getTime("r_creation_date").getDate());
    		}
    		cellNo++;

    		// Scheduled Date
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(dateStyle);
    		if(!col.getTime("p_sched_timestamp").isNullDate()) {
    			cell.setCellValue(col.getTime("p_sched_timestamp").getDate());
    		}
    		cellNo++;

    		// Last Activity
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_last_activity"));
    		cellNo++;

    		// Problem state count
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getInt("p_problem_state_count"));
    		cellNo++;

    		// Workflow Queue
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_workflow_queue"));
    		cellNo++;

    		// Queue Priority
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		int priority = col.getInt("p_queue_priority");
    		if(priority == 1) {
    			cell.setCellValue("High");
    		} else {
    			cell.setCellValue("Normal");
    		}
    		cellNo++;

    		// Documentum Object Id
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("sortbyobjid"));
    		cellNo++;

    		// Accession Id
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_accession_id"));
    		cellNo++;

    		// Log File
    		cell = row.createCell(cellNo);
    		cell.setCellStyle(normalStyle);
    		cell.setCellValue(col.getString("p_batch_logfile"));
    		cellNo++;

    		rowNo++;

       	}

    } catch (Exception e) {
    	e.printStackTrace();
    	HelperClass.porticoOutput(1, "Exception in ExportBatchListReport.createXlsData() "+e.getMessage());

    } finally {
    	try {if (null != col) col.close();}
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
        	HelperClass.porticoOutput(1, "Exception in CreateXlsData() post-finally "+e.getMessage());
        }
	}

public String readCombinedCookie(String combinedCookieIn, String key)
{
	final String KEY_VALUE_SEPARATOR = "=";
	final String COMBINED_COOKIE_SEPARATOR = "|";
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

	HelperClass.porticoOutput(0, "ExportBatchListReport.readCombinedCookie()-key="+key+":"+"value="+value);

	return value;
}

	public IDfSession getCurrentDfSession()
	{
		return getDfSession();
	}

	public String m_actionType;

	// class variable from search component
    private static int m_waitRequestId = 0;

}














