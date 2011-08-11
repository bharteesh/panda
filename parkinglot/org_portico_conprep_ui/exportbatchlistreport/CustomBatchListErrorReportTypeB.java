package org.portico.conprep.ui.exportbatchlistreport;

// this block of imports from search component
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.portico.common.util.StringUtil;
import org.portico.conprep.db.ConnectionManager;
import org.portico.conprep.ui.helper.CallProcessViewHandler;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Hidden;

/**
 * Description	Does the following things
 * 					initiates batch list report
 * Type		CustomBatchListErrorReportTypeB
 */

public class CustomBatchListErrorReportTypeB extends CustomBatchListErrorReportTypeA
{
    public CustomBatchListErrorReportTypeB()
    {
		// ArrayList
		super();
		m_bIsContentTreeRequired = false;
    	m_objectIdSuppliedMetadataMapping = new HashMap<String, ArrayList>();
    	m_objectIdSuppliedMetadataSuStateIdMapping = new HashMap<String, HashMap>();
    	m_objectIdContentTreeMapping = new HashMap<String, String>();
    	// PMD2.0
    	// m_lookupSuStateReason = new Hashtable<String, String>();
	}

	public void onInit(ArgumentList args)
	{
		super.onInit(args);

        if(null != args.get("contenttreerequired"))
        {
			String contenttreerequired = args.get("contenttreerequired");
			m_bIsContentTreeRequired = contenttreerequired.equalsIgnoreCase("true");
		}
	}

/*
 select batch.P_NAME p_batch_name, batch.P_PROVIDER_ID p_provider_id, batch.P_PROFILE_ID p_profile_id, batch.P_LAST_ACTIVITY p_last_activity, batch.P_STATE p_state,
 v_customreport.p_code, v_customreport.P_NAME p_object_name, v_customreport.P_DISPLAY_LABEL p_display_label, v_customreport.P_FU_TYPE p_fu_type, v_customreport.P_OBJECT_TYPE p_object_type, v_customreport.P_PARENT_ID p_parent_id, v_customreport.P_WORK_FILENAME p_work_filename,
  v_customreport.P_CONTENT_ID p_content_id
 from
 p_batch batch,
 V_CUSTOMREPORT_UI v_customreport,
 p_content p_content
 where
 batch.P_ACCESSION_ID=v_customreport.P_BATCH_ACCESSION_ID
 AND v_customreport.p_code in
 ('C800','C801','C802','C805')
 AND v_customreport.p_content_id = p_content.p_id(+)
 order by v_customreport.P_BATCH_ACCESSION_ID, v_customreport.P_CONTEXT_ID desc;
*/
	public String buildSqlQueryString()
	{
		System.out.println("CustomBatchListErrorReportTypeB-buildSqlQueryString()");
		String primaryErrorCodeInSqlCommaFormat = getPrimaryErrorCodeInSqlCommaFormatAndPopulatePrimaryErrorCodeList();
		String secondaryErrorCodeInSqlCommaFormat = getSecondaryErrorCodeInSqlCommaFormatAndPopulateSecondaryErrorCodeList();

		StringBuffer sb = new StringBuffer(1000);

/** LEADING PART of QUERY **/
        sb.append(" select batch.P_NAME p_batch_name, batch.P_PROVIDER_ID p_provider_id, batch.P_PROFILE_ID p_profile_id, batch.P_LAST_ACTIVITY p_last_activity, batch.P_STATE p_state, ");
        sb.append(" v_customreport.p_code p_code, v_customreport.P_NAME p_object_name, v_customreport.P_DISPLAY_LABEL p_display_label, v_customreport.P_FU_TYPE p_fu_type, v_customreport.P_OBJECT_TYPE p_object_type, v_customreport.P_PARENT_ID p_parent_id, v_customreport.P_WORK_FILENAME p_work_filename, ");
        sb.append(" v_customreport.p_cu_token, v_customreport.p_context_id p_context_id, v_customreport.p_batch_accession_id p_batch_accession_id,v_customreport.p_text p_text, ");
        sb.append(" p_umsg.P_CONTENT_ID p_content_id, ");
		sb.append(" p_content.P_CONTENT p_content ");
        sb.append(" from ");
        sb.append(" p_batch batch, ");
        sb.append(" p_user_message p_umsg, ");
        sb.append(" V_CUSTOMREPORT_UI v_customreport, ");
        sb.append(" p_content p_content ");
        sb.append(" where ");
        sb.append(" batch.P_ACCESSION_ID=p_umsg.P_BATCH_ACCESSION_ID AND batch.P_ACCESSION_ID=v_customreport.P_BATCH_ACCESSION_ID AND v_customreport.p_msg_id = p_umsg.p_id");

		if(null != primaryErrorCodeInSqlCommaFormat && !primaryErrorCodeInSqlCommaFormat.equals(""))
		{
		    sb.append(" AND v_customreport.p_code in ");
		    sb.append(" ( ");
		    sb.append(primaryErrorCodeInSqlCommaFormat);
		    sb.append(" ) ");
	    }
/** LEADING PART of QUERY **/

/** COMMON PART of QUERY **/
        // This is common for any query from the Batch list screen, all the cookies etc.
	    sb.append(" "+ getCommonBatchListSubSqlString() + " ");
/** COMMON PART of QUERY **/

/** TRAILING PART of QUERY **/
        sb.append(" AND p_umsg.p_content_id = p_content.p_id(+) ");

        sb.append(" order by v_customreport.P_BATCH_ACCESSION_ID, v_customreport.P_CONTEXT_ID desc ");
/** TRAILING PART of QUERY **/

		return sb.toString();
	}

    public void createXlsData() {
    	Hidden rpath = (Hidden)getControl("realpathId", com.documentum.web.form.control.Hidden.class);
    	String path = rpath.getValue();
    	System.out.println("CustomBatchListErrorReportTypeB-createXlsData path="+path);
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
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-createXlsData-sqlQry="+sqlQry);
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
    	    // These text styles are for the Content Tree.
    	    HSSFCellStyle textStyle31 = styleSheet.getRow(3).getCell(0).getCellStyle();
    	    HSSFCellStyle textStyle32 = styleSheet.getRow(4).getCell(0).getCellStyle();

    	    HSSFSheet sheet = wb.getSheetAt(0);
    	    HSSFRow row = null;
    	    HSSFCell cell = null;
    	    HSSFCellStyle textStyle = null;
    	    int rowNo = DATA_START_ROW_NUMBER;
    	    int cellNo = 0;
    	    int count = 0;
    	    con = ConnectionManager.getConnection();

    	    pstmt = con.prepareStatement(sqlQry);
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-createXlsData Before executeQuery");
        	rs = pstmt.executeQuery();
    		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-createXlsData After executeQuery");
        	while (rs.next())
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
					String p_display_label = rs.getString("p_display_label");
					if(null == p_display_label)
					{
						p_display_label = "";
					}
					String p_cu_token = rs.getString("p_cu_token");
					if(null == p_cu_token)
					{
						p_cu_token = "";
					}
					contextDetails += " \n ["+p_display_label+","+p_cu_token+"]";
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
       		    ArrayList<String> rawUnitFileNameList = new ArrayList<String>();
       		    boolean isPickAllRawUnits = false;
       		    if(rs.getString("p_object_type").equals("p_su"))
       		    {
       		        rawUnitFileNameList = getRawUnitFileNameList(con, rs.getString("p_batch_accession_id"), rs.getString("p_context_id"));
				}
				else if(rs.getString("p_object_type").equals("p_cu"))
				{
					ArrayList<String> suppliedMetadataSuStateIdList = getSuppliedMetadataEntryList(con, rs.getString("p_batch_accession_id"), rs.getString("p_context_id"), rs.getString("p_object_type"), SUSTATE_ID_ENTRY);
					if(null != suppliedMetadataSuStateIdList && suppliedMetadataSuStateIdList.size() > 0)
					{
					    for(String suppliedMetadataSuStateId : suppliedMetadataSuStateIdList)
					    {
          		            ArrayList<String> tRawUnitFileNameList = getRawUnitFileNameList(con, rs.getString("p_batch_accession_id"), suppliedMetadataSuStateId);
        		            for(String rawUnitFileName : tRawUnitFileNameList)
        		            {
					    		if(!rawUnitFileNameList.contains(rawUnitFileName))
					    		{
					    			rawUnitFileNameList.add(rawUnitFileName);
					    		}
    				    	}
					    }
				    }
				    else
				    {
						isPickAllRawUnits = true;
					}
				}
				else
				{
					isPickAllRawUnits = true;
				}
				if(isPickAllRawUnits == true)
				{
					// Pass "", telling no SuState available, pick all raw units tied to this Batch
       		        ArrayList<String> tRawUnitFileNameList = getRawUnitFileNameList(con, rs.getString("p_batch_accession_id"), "");
       		        for(String rawUnitFileName : tRawUnitFileNameList)
       		        {
						if(!rawUnitFileNameList.contains(rawUnitFileName))
						{
							rawUnitFileNameList.add(rawUnitFileName);
						}
   					}
				}
				if(rawUnitFileNameList.size() > 0)
				{
       		        for(String rawUnitFileName : rawUnitFileNameList)
       		        {
   						formattedRawUnitFileName = formattedRawUnitFileName + rawUnitFileName + "\n";
		    			if(formattedRawUnitFileName.length() >= EXCEL_CELL_CHAR_LIMIT)
		    			{
							formattedRawUnitFileName = formattedRawUnitFileName + TRUNCATION_STRING + "\n";
							break;
						}
   					}
				}
   		        cell = row.createCell(cellNo);
   		        cell.setCellStyle(textStyle);
   		        cell.setCellValue(formattedRawUnitFileName);
      		    cellNo++;

                // Supplied Metadata Filename
       		    String formattedSuppliedFileName = "";
       		    ArrayList<String> suppliedMetadataFileNameList = getSuppliedMetadataEntryList(con, rs.getString("p_batch_accession_id"), rs.getString("p_context_id"), rs.getString("p_object_type"), FILE_NAME_ENTRY);
       		    for(String suppliedMetadataFileName : suppliedMetadataFileNameList)
       		    {
					formattedSuppliedFileName = formattedSuppliedFileName + suppliedMetadataFileName + "\n";
				}
   		        cell = row.createCell(cellNo);
   		        cell.setCellStyle(textStyle);
   		        cell.setCellValue(formattedSuppliedFileName);
      		    cellNo++;

      		    if(true == m_bIsContentTreeRequired) // Only then a heading cell would be in the template
      		    {
                    String contentTreeStructure = "";
					contentTreeStructure = getContentTreeAsString(con, rs.getString("p_batch_accession_id"), rs.getString("p_context_id"), rs.getString("p_object_type"));
      		        cell = row.createCell(cellNo);
      		        if(textStyle == textStyle1)
      		        {
         		        cell.setCellStyle(textStyle31);
					}
					else
					{
         		        cell.setCellStyle(textStyle32);
					}
     		        cell.setCellValue(contentTreeStructure);
         		    cellNo++;
				}

       		    rowNo++;
       		    count++;
           	}

        } catch (Exception e) {
        	e.printStackTrace();
        	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-CreateXlsData() "+e.getMessage());

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
        	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-CreateXlsData() post-finally "+e.getMessage());
        }

    }

/*
Final if -'su' passed
----------------------
select p_su.p_name p_su_name, p_su.P_WORK_FILENAME p_workfilename, p_su.p_accession_id, p_su.P_PREDECESSOR_ID, p_fu.p_name, p_fu.P_FU_TYPE, p_su.p_cu_accession_id from p_su,p_fu where p_su.p_batch_accession_id='ark:/27927/dd0gmzr84' and p_su.p_batch_accession_id=p_fu.p_batch_accession_id and
 p_fu.P_CU_ACCESSION_ID=p_su.P_CU_ACCESSION_ID and p_su.P_FU_ACCESSION_ID=p_fu.P_ACCESSION_ID and p_fu.P_FU_TYPE in ('Text: Marked Up Header', 'Text: Marked Up Full Text')
 and p_su.p_cu_accession_id in (select p_cu_accession_id from p_su where p_accession_id='ark:/27927/dd0rtbgfj')
 and (p_su.P_PREDECESSOR_ID IS NULL OR p_su.P_PREDECESSOR_ID='')


cu-	  'ark:/27927/dd0rtb634'
else if 'cu' passed
--------------------
select p_su.p_name p_su_name, p_su.P_WORK_FILENAME p_workfilename, p_su.p_accession_id, p_su.P_PREDECESSOR_ID, p_fu.p_name, p_fu.P_FU_TYPE, p_su.p_cu_accession_id from p_su,p_fu where p_su.p_batch_accession_id='ark:/27927/dd0gmzr84' and p_su.p_batch_accession_id=p_fu.p_batch_accession_id and
 p_fu.P_CU_ACCESSION_ID=p_su.P_CU_ACCESSION_ID and p_su.P_FU_ACCESSION_ID=p_fu.P_ACCESSION_ID and p_fu.P_FU_TYPE in ('Text: Marked Up Header', 'Text: Marked Up Full Text')
 and p_su.p_cu_accession_id='ark:/27927/dd0rtb634'
 and (p_su.P_PREDECESSOR_ID IS NULL OR p_su.P_PREDECESSOR_ID='')
*/
    public ArrayList getSuppliedMetadataEntryList(Connection con, String batchAccessionId, String accessionId, String objectType, String entryType)
    {
		ArrayList suppliedMetadataEntryList = new ArrayList();

		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-batchAccessionId="+batchAccessionId+",accessionId="+accessionId+",objectType="+objectType+",entryType="+entryType);

        if(!m_objectIdSuppliedMetadataMapping.containsKey(accessionId))
		{
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String sql = null;
            ArrayList suppliedMetadataFileNameList = new ArrayList();
            ArrayList suppliedMetadataSuStateIdList = new ArrayList();

            try
            {
		        StringBuffer sb = new StringBuffer(1000);
		        if(null != objectType && objectType.equals("p_su"))
		        {
                    sb.append(" select p_su.p_name p_su_name, p_su.P_WORK_FILENAME p_workfilename, p_su.p_accession_id p_su_accession_id, p_su.P_PREDECESSOR_ID, p_fu.p_name, p_fu.P_FU_TYPE, p_su.p_cu_accession_id ");
                    sb.append(" from ");
                    sb.append(" p_su,p_fu ");
                    sb.append(" where ");
                    sb.append(" p_su.p_batch_accession_id=? ");
                    sb.append(" and p_su.p_batch_accession_id=p_fu.p_batch_accession_id ");
                    sb.append(" and p_fu.P_CU_ACCESSION_ID=p_su.P_CU_ACCESSION_ID and p_su.P_FU_ACCESSION_ID=p_fu.P_ACCESSION_ID ");
                    sb.append(" and p_fu.P_FU_TYPE in ");
                    sb.append(" ( ");
                    sb.append(" '" + LEAD_MD_FU_TYPE_MKUP_HDR +"'"+","+"'"+ LEAD_MD_FU_TYPE_MKUP_FULLTEXT+"' ");
                    sb.append(" ) ");
                    sb.append(" and p_su.p_cu_accession_id in (select p_cu_accession_id from p_su where p_accession_id=?) ");
                    /*CONPREP-2434 - Changing predecessor id column from varchar to clob - START */
                    //sb.append(" and (p_su.P_PREDECESSOR_ID IS NULL OR p_su.P_PREDECESSOR_ID='') ");
                    sb.append(" and (dbms_lob.compare(p_su.P_PREDECESSOR_ID, empty_clob()) = 0) ");                    
                    /*CONPREP-2434 - Changing predecessor id column from varchar to clob - END */                    
                    sql = sb.toString();
		    	}
		    	else if(null != objectType && objectType.equals("p_cu"))
		    	{
                    sb.append(" select p_su.p_name p_su_name, p_su.P_WORK_FILENAME p_workfilename, p_su.p_accession_id p_su_accession_id, p_su.P_PREDECESSOR_ID, p_fu.p_name, p_fu.P_FU_TYPE, p_su.p_cu_accession_id ");
                    sb.append(" from ");
                    sb.append(" p_su,p_fu ");
                    sb.append(" where ");
                    sb.append(" p_su.p_batch_accession_id=? ");
                    sb.append(" and p_su.p_batch_accession_id=p_fu.p_batch_accession_id ");
                    sb.append(" and p_fu.P_CU_ACCESSION_ID=p_su.P_CU_ACCESSION_ID and p_su.P_FU_ACCESSION_ID=p_fu.P_ACCESSION_ID ");
                    sb.append(" and p_fu.P_FU_TYPE in ");
                    sb.append(" ( ");
                    sb.append(" '" + LEAD_MD_FU_TYPE_MKUP_HDR +"'"+","+"'"+ LEAD_MD_FU_TYPE_MKUP_FULLTEXT+"' ");
                    sb.append(" ) ");
                    sb.append(" and p_su.p_cu_accession_id=? ");
                    /*CONPREP-2434 - Changing predecessor id column from varchar to clob - START */
                    //sb.append(" and (p_su.P_PREDECESSOR_ID IS NULL OR p_su.P_PREDECESSOR_ID='') ");                    
                    sb.append(" and (dbms_lob.compare(p_su.P_PREDECESSOR_ID, empty_clob()) = 0) ");
                    /*CONPREP-2434 - Changing predecessor id column from varchar to clob - END */                    
                    sql = sb.toString();
		    	}
		    	else
		    	{
            		HelperClass.porticoOutput(1, "Error in CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-unsupported object type="+objectType);
		    	}

    	    	if(null != sql && !sql.equals(""))
    	    	{
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-sql="+sql);
                    pstmt = con.prepareStatement(sql);
                    pstmt.setString(1, batchAccessionId);
                    pstmt.setString(2, accessionId);
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-Before executeQuery");
                    rs = pstmt.executeQuery();
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-After executeQuery");
                    while(rs.next())
                    {
    	    			suppliedMetadataFileNameList.add(rs.getString("p_su_name")+" "+"["+rs.getString("p_workfilename")+"]");
    	    			suppliedMetadataSuStateIdList.add(rs.getString("p_su_accession_id"));
                    }

                    HashMap entryList = new HashMap();
                    entryList.put(FILE_NAME_ENTRY, suppliedMetadataFileNameList);
                    entryList.put(SUSTATE_ID_ENTRY, suppliedMetadataSuStateIdList);

                    if(!m_objectIdSuppliedMetadataMapping.containsKey(accessionId))
                    {
		    			m_objectIdSuppliedMetadataMapping.put(accessionId, entryList);
		    		}
    	        }
            }
            catch(Exception e)
            {
       	    	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList="+e.toString());
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if(rs != null) rs.close();
                    if(pstmt != null) pstmt.close();
                }
                catch (Exception e)
                {
           		    HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-close()="+e.toString());
                    e.printStackTrace();
		    	}
            }
	    }

        if(m_objectIdSuppliedMetadataMapping.containsKey(accessionId))
        {
		    HashMap entryList = (HashMap)m_objectIdSuppliedMetadataMapping.get(accessionId);
		    if(null != entryList && entryList.size() > 0 && entryList.containsKey(entryType))
		    {
    	        suppliedMetadataEntryList = (ArrayList)entryList.get(entryType);
	        }
	        else
	        {
		    	HelperClass.porticoOutput(1, "Error CustomBatchListErrorReportTypeB-getSuppliedMetadataEntryList-Missing data for accessionId="+accessionId+",objectType="+objectType+",entryType="+entryType);
		    }
	    }

		return suppliedMetadataEntryList;
	}

/*
select * from V_CUSTOMREPORT_CONTENTTREE_UI where p_batch_accession_id='ark:/27927/dd0gmzr84' and p_cu_accession_id='ark:/27927/dd0rtb634' order by p_display_sort_order asc
*/
    public String getContentTreeAsString(Connection con, String batchAccessionId, String accessionId, String objectType)
    {
		String contentTreeStructure = "";

		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getContentTreeAsString-accessionId="+accessionId+",objectType="+objectType);

/*
        if(null != m_lookupSuStateReason && m_lookupSuStateReason.size() > 0)
        {
			// We already have queried and cached the Lookup for the Su State Reason info.
		}
		else
		{
	    	m_lookupSuStateReason = DBHelperClass.getLookupData(DBHelperClass.SU_TYPE, DBHelperClass.P_CONTENT_REASON);
	    }
*/
        if(m_objectIdContentTreeMapping.containsKey(accessionId))
        {
			contentTreeStructure = (String)m_objectIdContentTreeMapping.get(accessionId);
		}
		else
		{
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String sql = null;

            try
            {
		        StringBuffer sb = new StringBuffer(1000);
		        if(null != objectType && objectType.equals("p_su"))
		        {
                    sb.append(" select * from V_CUSTOMREPORT_CONTENTTREE_UI ");
                    sb.append(" where p_batch_accession_id=? ");
                    sb.append(" and p_cu_accession_id in (select p_cu_accession_id from p_su where p_accession_id=?) ");
                    sb.append(" order by p_display_sort_order asc ");
                    sql = sb.toString();
		    	}
		    	else if(null != objectType && objectType.equals("p_cu"))
		    	{
                    sb.append(" select * from V_CUSTOMREPORT_CONTENTTREE_UI ");
                    sb.append(" where p_batch_accession_id=? ");
                    sb.append(" and p_cu_accession_id=? ");
                    sb.append(" order by p_display_sort_order asc ");
                    sql = sb.toString();
		    	}
		    	else
		    	{
            		HelperClass.porticoOutput(1, "Error in CustomBatchListErrorReportTypeB-getContentTreeAsString-unsupported object type="+objectType);
		    	}

    	    	if(null != sql && !sql.equals(""))
    	    	{
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getContentTreeAsString-sql="+sql);
                    pstmt = con.prepareStatement(sql);
                    pstmt.setString(1, batchAccessionId);
                    pstmt.setString(2, accessionId);
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getContentTreeAsString-Before executeQuery");
                    rs = pstmt.executeQuery();
            		HelperClass.porticoOutput(0, "CustomBatchListErrorReportTypeB-getContentTreeAsString-After executeQuery");
                    while(rs.next())
                    {
		    			String p_context_line_details = "";
		    			String p_object_type = rs.getString(DBHelperClass.P_OBJECT_TYPE);
		    			if(null != rs.getString(DBHelperClass.P_NAME))
		    			{
	    			    	p_context_line_details = "(" + rs.getString(DBHelperClass.P_NAME)+")";
					    }
		    			if(p_object_type.equals("p_cu"))
		    			{
		    				String additionalDisplayInfo = rs.getString(DBHelperClass.P_DISPLAY_LABEL);
		    				if(null != additionalDisplayInfo)
		    				{
		    					p_context_line_details = additionalDisplayInfo + p_context_line_details;
		    				}
		    				p_context_line_details = P_CU_DISPLAY_TAB + p_context_line_details;
		    			}
		    			else if(p_object_type.equals("p_fu"))
		    			{
		    				String additionalDisplayInfo = rs.getString(DBHelperClass.P_FU_TYPE);
		    				if(null != additionalDisplayInfo)
		    				{
		    					p_context_line_details = additionalDisplayInfo + p_context_line_details;
		    				}
		    				p_context_line_details = P_FU_DISPLAY_TAB + p_context_line_details;
		    			}
		    			else if(p_object_type.equals("p_su"))
		    			{
// 1.xml(SU 3)(IP1)(has-successor=Y,Normalized)(active=N)(reason=Layer Removed)
		    				String additionalDisplayInfo = rs.getString(DBHelperClass.P_WORK_FILENAME);
		    				if(null != additionalDisplayInfo)
		    				{
		    					p_context_line_details = additionalDisplayInfo + p_context_line_details;
		    				}

		    				additionalDisplayInfo = rs.getString(DBHelperClass.P_IP_RULES_APPLIED);
		    				if(null != additionalDisplayInfo)
		    				{
		    					p_context_line_details = p_context_line_details + "("+additionalDisplayInfo+")";
		    				}

		    				additionalDisplayInfo = rs.getString(DBHelperClass.P_IS_EXCLUDED);
		    				if(null != additionalDisplayInfo && additionalDisplayInfo.equalsIgnoreCase(DBHelperClass.TRUE))
		    				{
		    					p_context_line_details = p_context_line_details + "(" + CallProcessViewHandler.EXCLUDED_TAG + additionalDisplayInfo;
		    					additionalDisplayInfo = rs.getString(DBHelperClass.P_EXCLUDE_REASON);
		    					if(null != additionalDisplayInfo)
		    					{
									p_context_line_details = p_context_line_details + CallProcessViewHandler.COMMA_SEPARATOR + additionalDisplayInfo;
								}
								p_context_line_details += ")";
		    				}

		    				additionalDisplayInfo = rs.getString(DBHelperClass.P_HAS_SUCCESSOR);
		    				if(null != additionalDisplayInfo && additionalDisplayInfo.equalsIgnoreCase(DBHelperClass.TRUE))
		    				{
		    					p_context_line_details = p_context_line_details + "(" + CallProcessViewHandler.HAS_SUCCESSOR_TAG + additionalDisplayInfo;
		    					additionalDisplayInfo = rs.getString(DBHelperClass.P_SUCCESSOR_REASON);
		    					if(null != additionalDisplayInfo)
		    					{
									p_context_line_details = p_context_line_details + CallProcessViewHandler.COMMA_SEPARATOR + additionalDisplayInfo;
								}
								p_context_line_details += ")";
		    				}

		    				additionalDisplayInfo = rs.getString(DBHelperClass.P_STATUS);
		    				if(!StringUtil.isEmpty(additionalDisplayInfo))
		    				{
		    					p_context_line_details = p_context_line_details + "(";
		    					p_context_line_details = p_context_line_details + CallProcessViewHandler.STATUS_TAG + additionalDisplayInfo;
		    					String statusRationale = rs.getString(DBHelperClass.P_STATUS_RATIONALE);
		    					if(!StringUtil.isEmpty(statusRationale))
		    					{
		    						p_context_line_details = p_context_line_details + "," + statusRationale;
		    					}
		    					p_context_line_details = p_context_line_details + ")";
							}

		    				additionalDisplayInfo = rs.getString(DBHelperClass.P_ORIGIN);
		    				if(!StringUtil.isEmpty(additionalDisplayInfo))
		    				{
		    					p_context_line_details = p_context_line_details + "(";
		    					p_context_line_details = p_context_line_details + CallProcessViewHandler.ORIGIN_TAG + additionalDisplayInfo;
		    					String originRationale = rs.getString(DBHelperClass.P_ORIGIN_RATIONALE);
		    					if(!StringUtil.isEmpty(originRationale))
		    					{
		    						p_context_line_details = p_context_line_details + "," + originRationale;
		    					}
		    					p_context_line_details = p_context_line_details + ")";
							}

		    				/* PMD2.0
		    				additionalDisplayInfo = rs.getString("P_CONTENT_REASON");
		    				if(null != additionalDisplayInfo)
		    				{
    					    	if(m_lookupSuStateReason.containsKey(additionalDisplayInfo))
						    	{
						    		String reasonLookup = (String)m_lookupSuStateReason.get(additionalDisplayInfo);
						    		if(null != reasonLookup)
						    		{
									    additionalDisplayInfo = reasonLookup;
									}
						    	}
		    					p_context_line_details = p_context_line_details + "(" + CallProcessViewHandler.REASON_TAG + additionalDisplayInfo + ")";
							}
							*/

		    				p_context_line_details = P_SU_DISPLAY_TAB + p_context_line_details;
		    			}
		    			contentTreeStructure = contentTreeStructure + p_context_line_details + "\n";
		    			if(contentTreeStructure.length() >= EXCEL_CELL_CHAR_LIMIT)
		    			{
							contentTreeStructure = contentTreeStructure + TRUNCATION_STRING + "\n";
							break;
						}
                    }

                    if(!m_objectIdContentTreeMapping.containsKey(accessionId))
                    {
		    			m_objectIdContentTreeMapping.put(accessionId, contentTreeStructure);
		    		}
    	        }
            }
            catch(Exception e)
            {
       	    	HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-getContentTreeAsString="+e.toString());
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if(rs != null) rs.close();
                    if(pstmt != null) pstmt.close();
                }
                catch (Exception e)
                {
       	    	    HelperClass.porticoOutput(1, "Exception in CustomBatchListErrorReportTypeB-getContentTreeAsString-close()="+e.toString());
                    e.printStackTrace();
		    	}
            }
	    }

		return contentTreeStructure;
	}


	public HashMap m_objectIdSuppliedMetadataMapping;
	public HashMap m_objectIdSuppliedMetadataSuStateIdMapping;
	public HashMap m_objectIdContentTreeMapping;

//	public Hashtable m_lookupSuStateReason;

    private boolean m_bIsContentTreeRequired = false;
    public static final String LEAD_MD_FU_TYPE_MKUP_HDR = "Text: Marked Up Header";
    public static final String LEAD_MD_FU_TYPE_MKUP_FULLTEXT = "Text: Marked Up Full Text";
    public static final String P_CU_DISPLAY_TAB = "  ";
    public static final String P_FU_DISPLAY_TAB = "  ----";
    public static final String P_SU_DISPLAY_TAB = "    ----";

    private static final String FILE_NAME_ENTRY = "FILE_NAME_ENTRY";
    private static final String SUSTATE_ID_ENTRY = "SUSTATE_ID_ENTRY";
}
