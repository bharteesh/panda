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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Hidden;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

/**
 * Description	Does the following things
 * 					initiates batch list report
 * Type		ExportBatchListErrorReport
 */
public class ExportBatchListErrorReport extends Component
{

    public ExportBatchListErrorReport()
    {
		m_actionType = "";
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
		HelperClass.porticoOutput(0, "ExportBatchListErrorReport -readCombinedCookie()-combinedCookieIn="+combinedCookieIn);

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

		HelperClass.porticoOutput(0, "ExportBatchListErrorReport-readCombinedCookie()-key="+key+":"+"value="+value);

		return value;
	}

	public void onInit(ArgumentList args)
	{
		super.onInit(args);

		m_actionType=args.get("actiontype");
		HelperClass.porticoOutput("ExportBatchListErrorReport onInit() for actionType="+m_actionType);

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
    	//String rpath = argumentlist.get("realpath");
    	//createXlsData(rpath);
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

    public void createXlsData() {
    	Hidden rpath = (Hidden)getControl("realpathId", com.documentum.web.form.control.Hidden.class);
    	String path = rpath.getValue();
    	System.out.println("createXlsData() path="+path);
    	String loginName="";
    	String tempFileLocation = null;
    	HSSFWorkbook wb = null;
    	String username = "";
    	Connection con = null;
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	PreparedStatement pstmt1 = null;
    	ResultSet rs1 = null;

    	StringBuffer sb = new StringBuffer(1000);
    	try {



    		Map<String, Set<String>> errorsMap = new TreeMap<String, Set<String>>();

    		//ExportBatchListErrorReport reportComponent = (ExportBatchListErrorReport)pageContext.getAttribute(Form.FORM, PageContext.REQUEST_SCOPE);
        	//String sqlQry = reportComponent.buildSqlQueryString();
        	String sqlQry = buildSqlQueryString();
    		HelperClass.porticoOutput(0, "exportBatchErrorList.jsp-"+sqlQry);
    		//username = reportComponent.getLoginUserName();
    		username = getLoginUserName();

    		/*
request.getSession().getServletContext().getRealPath(...)
    		 */
    		//tempFileLocation = application.getRealPath("/custom/library/export/temp/"+username+"ErrorReport.xls");
    		tempFileLocation = path+"/temp/"+username+"ErrorReport.xls";
    	    //String template = application.getRealPath("/custom/library/export/templates/MissingElementsReportSheet.xls");
    	    String template = path+"/templates/MissingElementsReportSheet.xls";
    	    POIFSFileSystem poiFileSystem = new POIFSFileSystem( new FileInputStream(template));
    	    wb = new HSSFWorkbook(poiFileSystem);

    	    HSSFSheet styleSheet = wb.getSheetAt(2);
    	    HSSFCellStyle headerStyle = styleSheet.getRow(0).getCell(0).getCellStyle();
    	    HSSFCellStyle textStyle1 = styleSheet.getRow(1).getCell(0).getCellStyle();
    	    HSSFCellStyle textStyle2 = styleSheet.getRow(2).getCell(0).getCellStyle();

    	    HSSFSheet sheet = wb.getSheetAt(0);
    	    HSSFRow row = null;
    	    HSSFCell cell = null;
    	    HSSFCellStyle textStyle = null;
    	    int rowNo = 2;
    	    int cellNo = 0;
    	    int count = 0;
    	    con = ConnectionManager.getConnection();
    	    String sqlQry1 = "select p_name contextname, p_work_filename filename from v_problemreport_ui where p_msg_id=?";
    		pstmt1 = con.prepareStatement(sqlQry1);

    	    pstmt = con.prepareStatement(sqlQry);
        	rs = pstmt.executeQuery();
        	while (rs.next()) {

        		if(count%2 == 0) {
        			textStyle = textStyle1;
        		}
        		else {
        			textStyle = textStyle2;
        		}

        		cellNo = 0;
        		row = sheet.createRow(rowNo);

        		// Error Code
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("p_code"));
        		cellNo++;

        		// Batch Name
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("p_name"));
        		cellNo++;

        		// Profile ID
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("profile"));
        		cellNo++;

        		// Last Activity
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("activity"));
        		cellNo++;

        		// Status
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("status"));
        		cellNo++;

        		// Context Id
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("contextid"));
        		cellNo++;

        		// Context Details
        		String contextDetails = "";
        		pstmt1.setString(1, rs.getString("p_msg_id"));
        		rs1 = pstmt1.executeQuery();
        		if(rs1.next()) {
	        		contextDetails = rs1.getString("contextname");
	    			if(null != rs1.getString("filename")) {
	    				contextDetails += " \n ["+rs1.getString("filename")+"]";
	    			}
        		}
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(contextDetails);
        		cellNo++;
        		if(null != rs1){
        			rs1.close();
        		}

        		// Error Text
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(rs.getString("p_text"));
        		cellNo++;

        		// Additional Text
        		String strContent = "";
        		Blob contentBlob = rs.getBlob("content");
    			if(contentBlob != null && contentBlob.length() > 0) {
    				InputStream inputStream = contentBlob.getBinaryStream();
    				try {
    					if(inputStream != null) {
    						BufferedInputStream bis = new BufferedInputStream(inputStream);
    						ByteArrayOutputStream baos = new ByteArrayOutputStream();
    						int tempChar;
    						while ( (tempChar = bis.read()) != -1)
    							baos.write(tempChar);
    						String content = baos.toString("utf-8");// To preserve utf-8 character set
    						Set<String> errorSet = (TreeSet<String>)errorsMap.get(rs.getString("p_code"));
    						if(null == errorSet)
    							errorSet = new TreeSet<String>();
    						errorSet.add(content);

    						errorsMap.put(rs.getString("p_code"), errorSet);
    						strContent = content;

    					}
    				} catch(Exception e) {
    					e.printStackTrace();
    				} finally {
    					try {
    						if(inputStream != null)
    							inputStream.close();
    					} catch(Exception eClose) {
    						eClose.printStackTrace();
    					}
    				}
    			}

        		// Creation Date
        		cell = row.createCell(cellNo);
        		cell.setCellStyle(textStyle);
        		cell.setCellValue(strContent);
        		cellNo++;


        		rowNo++;
        		count++;
           	}

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

        } catch (Exception e) {
        	e.printStackTrace();
        	HelperClass.porticoOutput(1, "Exception in CreateXlsData() "+e.getMessage());

        } finally {
        	try {if (null != rs) rs.close();}
        	catch (Exception e) {}
        	try {if (null != pstmt) pstmt.close();}
        	catch (Exception e) {}
        	try {if (null != rs1) rs1.close();}
        	catch (Exception e) {}
        	try {if (null != pstmt1) pstmt1.close();}
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
        	HelperClass.porticoOutput(1, "Exception in CreateXlsData() post-finally "+e.getMessage());
        }

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
	public String buildSqlQueryString(){
		System.out.println("buildSqlQueryString()");

		// Severity 0 = Info, 1=Warnings 2=Fatals
		int msgSeverity = 2;
		if(null != m_actionType && m_actionType.equalsIgnoreCase(batchWarningsReportName)) {
			msgSeverity = 1;
		}

		StringBuffer sb = new StringBuffer(1000);
		
		/*sb.append("select msg.p_id p_msg_id,p_code,p_text,content,batch.p_name p_name,batch.p_profile_id profile,batch.p_last_activity activity, ");
		sb.append(" batch.p_state status, msg.p_context_id contextid ");
		sb.append("from p_user_message msg, p_batch batch where  ");
		sb.append(" msg.p_batch_accession_id = batch.p_accession_id and msg.p_severity = "+msgSeverity+" and p_is_action_taken='N' ");*/
		
		sb.append("select msg.p_id p_msg_id,p_code,p_text,content.p_content content,batch.p_name p_name,batch.p_profile_id profile,batch.p_last_activity activity, ");
		sb.append(" batch.p_state status, msg.p_context_id contextid ");
		sb.append("from p_user_message msg, p_batch batch, p_content content where  ");
		sb.append(" msg.p_batch_accession_id = batch.p_accession_id and msg.p_severity = "+msgSeverity+" and p_is_action_taken='N' and msg.p_content_id = content.p_id(+) ");
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
				HelperClass.porticoOutput(1, "Exception in exportBatchListErrorReport.jsp-whereHold="+m_hold+", Exception="+e.toString());
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


		sb.append("  order by msg.p_code,msg.p_text");
		return sb.toString();
	}

	public String getProviderId(String folderPath) {
		String providerId = null;
		IDfCollection col = null;
		try {
			IDfSession dfSession = getCurrentDfSession();
			String dqlQry = "select distinct(p_provider_id) as providerid from p_batch where folder('"+folderPath+"')";
			IDfQuery query = new DfQuery();
	    	query.setDQL(dqlQry);
	    	col = query.execute(dfSession,IDfQuery.DF_EXEC_QUERY);
	    	while (col.next()) {
	    		providerId = col.getString("providerid");
	    	}
		} catch (Exception e) {
			HelperClass.porticoOutput(1,"ExportBatchListErrorReport getProviderId()"+e.getMessage());
		} finally {
			try {
				if(null != col) {
					col.close();
				}
			} catch (DfException dfExec) {
				HelperClass.porticoOutput(1,"ExportBatchListErrorReport getProviderId()while closing collection "+dfExec.getMessage());
			}
		}
		return providerId;
	}

	private String batchErrorReportName = "Batch Error Report";
	private String batchWarningsReportName = "Batch Warnings Report";

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

	public String m_actionType;

	// class variable from search component
    private static int m_waitRequestId = 0;

}
