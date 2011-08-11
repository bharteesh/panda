
package org.portico.conprep.ui.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.portico.common.events.KeyMetadataElementsConstants;
import org.portico.conprep.db.ConnectionManager;
import org.portico.conprep.db.DBBatchObject;
import org.portico.conprep.db.DBRawUnitObject;

public class DBHelperClass
{
	// List of OBJECT_TYPE(S)
	// Documentum mappings
	public static final String BATCH_TYPE = "p_batch";
	public static final String RAW_UNIT_TYPE = "p_raw_unit";

    // Oracle table mappings
	public static final String CU_TYPE = "p_cu";
	public static final String FU_TYPE = "p_fu";
	public static final String SU_TYPE = "p_su";
	public static final String FILE_REF_TYPE = "p_file_ref";
	public static final String DESC_MD_TYPE = "p_desc_md";
	public static final String USER_MESSAGE_TYPE = "p_user_message";
	public static final String SU_RENAME_ACTION = "p_su_rename_action";

	// P_USER_MESSAGE Constant Values
	public static final int SEVERITY_INFO = 0;
	public static final int SEVERITY_WARNING = 1;
	public static final int SEVERITY_FATAL = 2;


	// Oracle view mappings
	public static final String V_PARENTBATCH_OBJECTTYPE_UI = "V_PARENTBATCH_OBJECTTYPE_UI";
	public static final String V_PARENTARTICLE_UI = "V_PARENTARTICLE_UI";
	public static final String V_CONTENTTREE_UI = "V_CONTENTTREE_UI";
	// Rel-1_1_9 merged with V_CONTENTTREE_UI
    // public static final String V_CONTENTTREE_PER_ARTICLE_UI = "V_CONTENTTREE_PER_ARTICLE_UI";
	public static final String V_PROBLEMREPORT_UI = "V_PROBLEMREPORT_UI";
	public static final String V_ARTICLEMETADATA_UI = "V_ARTICLEMETADATA_UI";
	
	// PMD 2.0
	public static final String V_AUARTICLEMAPPING_UI = "V_AUARTICLEMAPPING_UI";

    // List of p_md_type values that the 'p_desc_md' table can have
	public static final String CURATED_DMD_TYPE = "CDMD";
	public static final String EXTRACTED_DMD_TYPE = "EDMD";

	// Note: Our Oracle boolean equivalent "Y" = true, "N" = false
	public static final String TRUE = "Y";
	public static final String FALSE = "N";


    // Lookup files for the OBJECTS
    public static final String CU_LOOKUP_TYPE = "p_cu_state_lookup";
	public static final String FU_LOOKUP_TYPE = "p_fu_state_lookup";
	public static final String SU_LOOKUP_TYPE = "p_su_state_lookup";
	public static final String P_SU_CONTENT_REASON_LKUP = "p_su_content_reason_lkup";
	public static final String P_SU_CONTENT_SOURCE_LKUP = "p_su_content_source_lkup";
	public static final String P_SU_OUTCOME_LKUP = "p_su_outcome_lkup";

	// Attribute Constants
	public static final String UNKNOWN = "unknown";

	// Common Non-Table Attributes
    public static final String P_OBJECT_TYPE = "p_object_type";
    public static final String P_PARENT_ID = "p_parent_id";
    public static final String P_BATCH_OBJECT_ID = "p_batch_object_id";
    public static final String P_MSG_ID = "p_msg_id";
    public static final String PMETS = "PMETS";
    public static final String ARTICLE = "ARTICLE";
    public static final String P_MD_TYPE = "P_MD_TYPE";


	// Common Table Attributes
	public static final String P_ACCESSION_ID = "p_accession_id";
	public static final String P_NAME = "p_name";
	public static final String P_CONTENT_ID = "p_content_id";
    public static final String CONTENT = "content";
    public static final String P_BATCH_ACCESSION_ID = "p_batch_accession_id";

    // p_batch Table attributes
    public static final String P_ACTIVE_WARNING_COUNT = "p_active_warning_count";

	// p_cu Table Attributes
	public static final String P_SORT_KEY = "p_sort_key";
	public static final String P_DISPLAY_LABEL = "p_display_label";
	public static final String P_INSPECTION_REQUIRED = "p_inspection_required";
	public static final String P_INSPECTED = "p_inspected";


	// p_fu Table Attributes
	public static final String P_FU_TYPE = "p_fu_type";

	// p_su Table Attributes
	public static final String P_WORK_FILENAME = "p_work_filename";

	// PMD2.0
	public static final String P_STATUS = "p_status";
	public static final String P_STATUS_RATIONALE = "p_status_rationale";
	public static final String P_ORIGIN = "p_origin";
	public static final String P_ORIGIN_RATIONALE = "p_origin_rationale";
	// public static final String P_ACTIVE = "p_active";
	// public static final String P_CONTENT_REASON = "p_content_reason";
	public static final String P_USER_ADDED = "p_user_added";

	public static final String P_LEAD_SOURCE_ID = "p_lead_source_id";
	public static final String P_IS_CREATED_BY_WORKFLOW = "p_is_created_by_workflow";
	public static final String P_VIRUS_SCANNED = "p_virus_scanned";
	public static final String P_PREDECESSOR_ID = "p_predecessor_id";

	// public static final String P_CONTENT_SOURCE = "p_content_source";
	public static final String P_FORMAT_ID = "p_format_id";
	public static final String P_FORMAT_NAME = "p_format_name";
	public static final String P_MIME_TYPE = "p_mime_type";

	public static final String P_IS_EXCLUDED = "p_is_excluded";
	public static final String P_EXCLUDE_REASON = "p_exclude_reason";
	public static final String P_HAS_SUCCESSOR = "p_has_successor";
	public static final String P_SUCCESSOR_REASON = "p_successor_reason";
	// REL-1_1_9
	public static final String P_IP_RULES_APPLIED = "p_ip_rules_applied";

    public static final String P_AU_ACCESSION_ID = "p_au_accession_id";
    public static final String P_CU_ACCESSION_ID = "p_cu_accession_id";
    public static final String P_AU_NAME = "p_au_name";
    public static final String P_CU_NAME = "p_cu_name";
 
    // p_user_message Table Attributes
    public static final String P_CONTEXT_ID = "p_context_id";
    public static final String P_CODE = "p_code";
    public static final String P_TEXT = "p_text";
    public static final String P_CATEGORY = "p_category";
    public static final String P_SEVERITY = "p_severity";
    public static final String P_IS_ACTION_TAKEN = "p_is_action_taken";
    public static final String P_ACTION_DESC = "p_action_desc";

    // Possible values on p_fu_type
    public static final String LEAD_MD_FU_TYPE_MKUP_HDR = "Text: Marked Up Header";
    public static final String LEAD_MD_FU_TYPE_MKUP_FULLTEXT = "Text: Marked Up Full Text";

    // p_su_rename_action Table Attributes
    public static final String P_ORIG_FILENAME = "p_orig_filename";
    public static final String P_NEW_WORK_FILENAME = "p_new_work_filename";
    public static final String P_CREATED_BY = "p_created_by";
    public static final String P_MODIFIED_BY = "p_modified_by";
    public static final String P_CREATE_TIMESTAMP = "p_create_timestamp";
    public static final String P_MODIFY_TIMESTAMP = "p_modify_timestamp";

    // Other values
    public static final String PREDECESSOR_SEPARATOR = " ";

    public DBHelperClass()
    {
    }

    public static String getBatchAccessionIdFromBatchId(String batchObjectId)
    {
		return DBBatchObject.getBatchAccessionIdFromBatchId(batchObjectId);
	}

    public static String getRawUnitAccessionIdFromRawUnitId(String rawUnitObjectId)
    {
		return DBRawUnitObject.getRawUnitAccessionIdFromRawUnitId(rawUnitObjectId);
	}

    // Get other Batch attributes(eg: BatchOperationsManager related attributes on the Batch)
	public static Hashtable getBatchAttributes(String batchObjectId, ArrayList alistIn)
	{
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getBatchAttributes(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
    		String attrString = DBHelperClass.getAttrListAsString(alistIn);
            sql = "SELECT " + attrString + " FROM " + BATCH_TYPE + " "
                + "WHERE P_OBJECT_ID = ? ";

            printMe(0, "DBHelperClass-getBatchAttributes-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, batchObjectId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
				// Pick all the result data(irrespective of the data type) as a String
				for(int indx=0; indx < alistIn.size(); indx++)
				{
                    String attrKey = (String)alistIn.get(indx);
			    	String attrValue = rs.getString(attrKey);
			    	if(attrValue != null)
			    	{
			        	alistOut.put(attrKey, attrValue);
				    }
			    }
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getBatchAttributes="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getBatchAttributes-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getBatchAttributes-End for batchObjectId="+batchObjectId);

		return alistOut;
	}

	/*---------------------------------------------------------------------------------------------------*/

/*
CREATE OR REPLACE FORCE VIEW DOMIG.V_PARENTBATCH_OBJECTTYPE_UI
(P_ACCESSION_ID, P_BATCH_ACCESSION_ID, P_OBJECT_TYPE, P_BATCH_OBJECT_ID)
AS
select nbobj.p_accession_id p_accession_id,
       nbobj.p_batch_accession_id p_batch_accession_id,
	   nbobj.p_object_type p_object_type,
	   p_batch.p_object_id p_batch_object_id
from
(select p_accession_id p_accession_id, p_batch_accession_id p_batch_accession_id, 'p_cu' p_object_type from p_cu
union
select p_accession_id p_accession_id, p_batch_accession_id p_batch_accession_id, 'p_fu' p_object_type from p_fu
union
select p_accession_id p_accession_id, p_batch_accession_id p_batch_accession_id, 'p_su' p_object_type from p_su
)nbobj, p_batch where nbobj.p_batch_accession_id=p_batch.p_accession_id;

select * from V_PARENTBATCH_OBJECTTYPE_UI where p_accession_id=1
*/

    // Given an Id return the parent Batch Object Id(Note: It is the Documentum BatchObjectId, NOT the Batch Accession Id)
    public static String getParentBatchObjectId(String objectAccessionId)
    {
		String batchObjectId = "";

        printMe(0, "DBHelperClass-getParentBatchObjectId(Started) objectAccessionId="+objectAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT P_BATCH_OBJECT_ID FROM " + V_PARENTBATCH_OBJECTTYPE_UI + " "
                + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getParentBatchObjectId-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, objectAccessionId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
				// Note: 'P_BATCH_OBJECT_ID' in this context refers to the parent batchObjectId(Documentum)
				batchObjectId = rs.getString("P_BATCH_OBJECT_ID");
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getParentBatchObjectId="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getParentBatchObjectId-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getParentBatchObjectId-End for objectAccessionId,batchObjectId="+objectAccessionId+","+batchObjectId);

		return batchObjectId;
	}

	// Note: batchObjectId(Documentum) is handled in the HelperClass, others(accession id) are handled here
    public static String getObjectType(String objectAccessionId)
    {
		String objectType = "";

        printMe(0, "DBHelperClass-getObjectType(Started) objectAccessionId="+objectAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT P_OBJECT_TYPE FROM " + V_PARENTBATCH_OBJECTTYPE_UI + " "
                + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getObjectType-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, objectAccessionId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
				objectType = rs.getString("P_OBJECT_TYPE");
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getObjectType="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getObjectType-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getObjectType-End for objectAccessionId,objectType="+objectAccessionId+","+objectType);

		return objectType;
	}


	/*---------------------------------------------------------------------------------------------------*/

	public static Hashtable getObjectAttributes(String objectType, String objectAccessionId, ArrayList alistIn)
	{
		// Depending on the 'objectType' fire request to the relevant table
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getObjectAttributes(Started) objectType,objectAccessionId="+objectType+","+objectAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
    		String attrString = DBHelperClass.getAttrListAsString(alistIn);
            sql = "SELECT " + attrString + " FROM " + objectType + " "
                + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getObjectAttributes-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, objectAccessionId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
				// Pick all the result data(irrespective of the data type) as a String
				for(int indx=0; indx < alistIn.size(); indx++)
				{
                    String attrKey = (String)alistIn.get(indx);
			    	String attrValue = rs.getString(attrKey);
			    	if(attrValue != null)
			    	{
			        	alistOut.put(attrKey, attrValue);
				    }
			    }
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getObjectAttributes="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getObjectAttributes-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getObjectAttributes-End objectType,objectAccessionId="+objectType+","+objectAccessionId);

		return alistOut;
	}

    // Output hash = (keyObjectId, valueHashAtributes)
	public static Hashtable getAttributesForAllObjects(String batchObjectId, String objectType, ArrayList alistIn)
	{
		// Depending on the 'objectType' fire request to the relevant table for all the object(s) in the table
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getAttributesForAllObjects(Started) batchObjectId,objectType="+batchObjectId+","+objectType);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getAttributesForAllObjects batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
    		    String attrString = DBHelperClass.getAttrListAsString(alistIn);
                sql = "SELECT " + attrString + " FROM " + objectType + " "
                + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-getAttributesForAllObjects-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                while(rs.next()) {
			    	// Pick all the result data(irrespective of the data type) as a String
			    	Hashtable attrListPerObject = new Hashtable();
			    	String currentObjectAccessionId = "";
			    	for(int indx=0; indx < alistIn.size(); indx++)
			    	{
                        String attrKey = (String)alistIn.get(indx);
			        	String attrValue = rs.getString(attrKey);
			        	if(attrValue != null)
			        	{
							if(attrKey.equalsIgnoreCase(DBHelperClass.P_ACCESSION_ID))
							{
								currentObjectAccessionId = attrValue;
							}
			            	attrListPerObject.put(attrKey, attrValue);
			    	    }
			        }
			        if(currentObjectAccessionId != null && !currentObjectAccessionId.equals(""))
			        {
						alistOut.put(currentObjectAccessionId, attrListPerObject);
					}
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getAttributesForAllObjects="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getAttributesForAllObjects-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getAttributesForAllObjects-End batchObjectId,objectType="+batchObjectId+","+objectType);

		return alistOut;
	}

    // RANGA - For better performance try using 'rownum < 2' in the query
	public static boolean hasErrors(String batchObjectId, boolean isActive, String severity, String[] categoryList)
	{
		boolean isTrue = false;

		printMe(0, "DBHelperClass-hasErrors(Started) batchObjectId,isActive,severity="+batchObjectId+","+isActive+","+severity);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-hasErrors batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                String categoryPrepare = "";
                for(int cindx=0; cindx < categoryList.length; cindx++)
                {
    				if(cindx == 0)
    				{
    					categoryPrepare = "?";
    				}
    				else
    				{
    					categoryPrepare = categoryPrepare + "," + "?";
    				}
    			}
                sql = "SELECT P_ID FROM " + USER_MESSAGE_TYPE + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? "
                      + "AND P_IS_ACTION_TAKEN = ? "
                      + "AND P_SEVERITY = ? "
                      + "AND P_CATEGORY IN (" + categoryPrepare + ") "
                      + "AND ROWNUM < 2";

                printMe(0, "DBHelperClass-hasErrors-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);
    			String action_taken = "N"; // Active messages, action is still pending(No action taken yet)
    			if(isActive == false)
    			{
    				action_taken = "Y"; // Inactive messages, action has already been taken
    			}
                pstmt.setString(2, action_taken);
                pstmt.setInt(3, Integer.parseInt(severity));
                int nextStatementSetIndx = 4;
                for(int catIndx=0; catIndx < categoryList.length; catIndx++)
                {
                    pstmt.setInt(nextStatementSetIndx+catIndx, Integer.parseInt(categoryList[catIndx]));
			    }

                rs = pstmt.executeQuery();
                if(rs.next()) {
					isTrue = true;
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-hasErrors="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-hasErrors-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-hasErrors-End batchObjectId,isActive,severity,isTrue="+batchObjectId+","+isActive+","+severity+","+isTrue);

		return isTrue;
	}

    // RANGA - For better performance try using 'rownum < 2' in the query
	public static boolean hasActiveFatalErrors(String batchObjectId)
	{
		boolean isTrue = false;

		printMe(0, "DBHelperClass-hasActiveFatalErrors(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-hasActiveFatalErrors batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT P_ID FROM " + USER_MESSAGE_TYPE + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? "
                      + "AND P_IS_ACTION_TAKEN = ? "
                      + "AND P_SEVERITY = ? "
                      + "AND ROWNUM < 2";

                printMe(0, "DBHelperClass-hasActiveFatalErrors-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);
    			String action_taken = "N"; // Active messages, action is still pending(No action taken yet)
                pstmt.setString(2, action_taken);
                int severity = 2; // 2==Fatal
                pstmt.setInt(3, severity);

                rs = pstmt.executeQuery();
                if(rs.next()) {
					isTrue = true;
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-hasActiveFatalErrors="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-hasActiveFatalErrors-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-hasActiveFatalErrors-End batchObjectId,isTrue="+batchObjectId+","+isTrue);

		return isTrue;
	}

    // RANGA - For better performance try using 'rownum < 2' in the query
    public static boolean isInspectionCheckDone(String batchObjectId)
    {
		boolean isTrue = true;

		printMe(0, "DBHelperClass-isInspectionCheckDone(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-isInspectionCheckDone batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT P_ACCESSION_ID FROM " + CU_TYPE + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? "
                      + "AND P_INSPECTION_REQUIRED = ? "
                      + "AND P_INSPECTED = ? "
                      + "AND ROWNUM < 2";

                printMe(0, "DBHelperClass-isInspectionCheckDone-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);
    			String inspection_required = "Y"; // Inspection is required for this CU(Article)
                pstmt.setString(2, inspection_required);
                String inspected = "N"; // This CU(Article) has not been Inspected yet
                pstmt.setString(3, inspected);

                rs = pstmt.executeQuery();
                if(rs.next()) {
					isTrue = false; // This CU(Article) is still pending, so total inspection check not completed yet
                }
		    }
        }
        catch(Exception e)
        {
			isTrue = false; // Set this
            printMe(1, "Exception in DBHelperClass-isInspectionCheckDone="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-isInspectionCheckDone-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-isInspectionCheckDone-End batchObjectId,isTrue="+batchObjectId+","+isTrue);

		return isTrue;
	}

	public static Hashtable getCuratedDmdObjectId(String batchObjectId, String cuAccessionId)
	{
		Hashtable curatedDmdObjectIdAccessionIdPair = new Hashtable();
		printMe(0, "DBHelperClass-getCuratedDmdObjectId(Started) batchObjectId="+batchObjectId+","+
		                                                         "cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        String curatedDmdObjectId = "";
        String curatedDmdAccessionId = "";

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getCuratedDmdObjectId batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
				// Is 'ORDER BY P_VERSION DESC' it required ????, it is a little costly
                sql = "SELECT "+DBHelperClass.P_CONTENT_ID+",P_ACCESSION_ID FROM " + DESC_MD_TYPE + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? "
                      + "AND P_CONTEXT_ID = ? "
                      + "AND P_MD_TYPE = ? "
                      + "ORDER BY P_VSTAMP DESC";

                printMe(0, "DBHelperClass-getCuratedDmdObjectId-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);
                pstmt.setString(2, cuAccessionId);
                pstmt.setString(3, CURATED_DMD_TYPE);

                rs = pstmt.executeQuery();
                if(rs.next()) {
					curatedDmdObjectId = rs.getString(DBHelperClass.P_CONTENT_ID);
					curatedDmdAccessionId = rs.getString("P_ACCESSION_ID");
					curatedDmdObjectIdAccessionIdPair.put(curatedDmdObjectId, curatedDmdAccessionId);
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getCuratedDmdObjectId="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getCuratedDmdObjectId-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getCuratedDmdObjectId-End batchObjectId="+batchObjectId+","+
		                                                         "cuAccessionId="+cuAccessionId+","+
		                                                         "curatedDmdObjectId="+curatedDmdObjectId);
		return curatedDmdObjectIdAccessionIdPair;
	}

    /*---------------------------------------------------------------------------------------------------*/
/*
CREATE OR REPLACE FORCE VIEW DOMIG.V_PARENTARTICLE_UI
(P_ACCESSION_ID, P_CU_ACCESSION_ID)
AS
select nbobj.p_accession_id p_accession_id,
       nbobj.p_cu_accession_id p_cu_accession_id
from
(select p_accession_id p_accession_id, p_accession_id p_cu_accession_id from p_cu
union
select p_accession_id p_accession_id, p_cu_accession_id p_cu_accession_id from p_fu
union
select p_accession_id p_accession_id, p_cu_accession_id p_cu_accession_id from p_su
)nbobj;

select * from V_PARENTARTICLE_UI where p_accession_id=1
*/

	// Note: The 'select' query must include the p_cu table, because this 'objectAccessionId' could
	//       be the cu_accession_id itself
	public static String getParentArticleId(String objectAccessionId)
	{
		String parentArticleId = "";

        printMe(0, "DBHelperClass-getParentArticleId(Started) objectAccessionId="+objectAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT P_CU_ACCESSION_ID FROM " + V_PARENTARTICLE_UI + " "
                + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getParentArticleId-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, objectAccessionId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
				// Note: 'P_CU_ACCESSION_ID' in this context refers to the parent Article accession Id
				parentArticleId = rs.getString("P_CU_ACCESSION_ID");
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getParentArticleId="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getParentArticleId-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getParentArticleId-End for objectAccessionId,parentArticleId="+objectAccessionId+","+parentArticleId);

		return parentArticleId;
	}

    // Currently a p_cu_accession_id is passed
    // Returns multiple rows
    // Output - hashtable(keyObjectId, valueHashAttributes)
		// Note: Hashtable can store any 'Object' so ObjectValuePair is not required

		// select   p_su.p_su_accession_id,
		//          p_su.p_object_id,
		//          p_su.p_name,
		//          p_su.p_work_filename,
		//          p_su.p_predecessor_id
		// from p_su, p_fu
		//                 where p_fu.p_cu_accession_id=objectAccessionId and
		//                       p_su.p_cu_accession_id=objectAccessionId and
		//                       p_fu.p_accession_id=p_su.p_fu_accession_id and
		//                       p_fu.p_fu_type=pdfFuType
        // Put hash key='p_su.p_su_accession_id', but check if it already exists
        //     also put the 'p_su_accession_id' in the hash value list too
    // May need index(tuning) since it has table joins too
	public static Hashtable getPageImageList(String objectAccessionId)
	{
		Hashtable pageImageList = new Hashtable();
		String pdfFuType = "Rendition: Page Images";

        printMe(0, "DBHelperClass-getPageImageList(Started) objectAccessionId="+objectAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT /*+ index(FU_ALIAS,SYS_C0040342) index(SU_ALIAS,P_SU_IDX2) */ SU_ALIAS.P_ACCESSION_ID P_ACCESSION_ID FROM "
                  + SU_TYPE + " SU_ALIAS"+","
                  + FU_TYPE + " FU_ALIAS" +" "
                  + "WHERE FU_ALIAS.P_CU_ACCESSION_ID = ? "
                  + "AND SU_ALIAS.P_CU_ACCESSION_ID = ? "
                  + "AND FU_ALIAS.P_ACCESSION_ID = SU_ALIAS.P_FU_ACCESSION_ID "
                  + "AND FU_ALIAS.P_FU_TYPE = ? ";

            printMe(0, "DBHelperClass-getPageImageList-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, objectAccessionId);
            pstmt.setString(2, objectAccessionId);
            pstmt.setString(3, pdfFuType);

            rs = pstmt.executeQuery();
            while(rs.next()) {
				String p_su_accession_id = rs.getString("P_ACCESSION_ID");
				if(pageImageList != null && !pageImageList.containsKey(p_su_accession_id))
				{
				    pageImageList.put(p_su_accession_id, p_su_accession_id);
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getPageImageList="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getPageImageList-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getPageImageList-End for objectAccessionId="+objectAccessionId);

		return pageImageList;
	}

    public static Hashtable getArticles(String batchObjectId, ArrayList alistIn)
    {
		return getAttributesForAllObjects(batchObjectId, CU_TYPE, alistIn);
	}

    public static Hashtable getSuStatesPerArticle(String cuAccessionId, ArrayList alistIn)
    {
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getSuStatesPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
   		    String attrString = DBHelperClass.getAttrListAsString(alistIn);
            sql = "SELECT " + attrString + " FROM " + SU_TYPE + " "
                + "WHERE P_CU_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getSuStatesPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);

            rs = pstmt.executeQuery();
            while(rs.next()) {
		    	// Pick all the result data(irrespective of the data type) as a String
		    	Hashtable attrListPerObject = new Hashtable();
		    	String currentObjectAccessionId = "";
		    	for(int indx=0; indx < alistIn.size(); indx++)
		    	{
                    String attrKey = (String)alistIn.get(indx);
		        	String attrValue = rs.getString(attrKey);
		        	if(attrValue != null)
		        	{
						if(attrKey.equalsIgnoreCase(DBHelperClass.P_ACCESSION_ID))
						{
							currentObjectAccessionId = attrValue;
						}
		            	attrListPerObject.put(attrKey, attrValue);
		    	    }
		        }
		        if(currentObjectAccessionId != null && !currentObjectAccessionId.equals(""))
		        {
					alistOut.put(currentObjectAccessionId, attrListPerObject);
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getSuStatesPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getSuStatesPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getSuStatesPerArticle-End cuAccessionId="+cuAccessionId);

		return alistOut;
	}

    public static Hashtable getFuStatesPerArticle(String cuAccessionId, ArrayList alistIn)
    {
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getFuStatesPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
   		    String attrString = DBHelperClass.getAttrListAsString(alistIn);
            sql = "SELECT " + attrString + " FROM " + FU_TYPE + " "
                + "WHERE P_CU_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getFuStatesPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);

            rs = pstmt.executeQuery();
            while(rs.next()) {
		    	// Pick all the result data(irrespective of the data type) as a String
		    	Hashtable attrListPerObject = new Hashtable();
		    	String currentObjectAccessionId = "";
		    	for(int indx=0; indx < alistIn.size(); indx++)
		    	{
                    String attrKey = (String)alistIn.get(indx);
		        	String attrValue = rs.getString(attrKey);
		        	if(attrValue != null)
		        	{
						if(attrKey.equalsIgnoreCase(DBHelperClass.P_ACCESSION_ID))
						{
							currentObjectAccessionId = attrValue;
						}
		            	attrListPerObject.put(attrKey, attrValue);
		    	    }
		        }
		        if(currentObjectAccessionId != null && !currentObjectAccessionId.equals(""))
		        {
					alistOut.put(currentObjectAccessionId, attrListPerObject);
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getFuStatesPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getFuStatesPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getFuStatesPerArticle-End cuAccessionId="+cuAccessionId);

		return alistOut;
	}

    /*---------------------------------------------------------------------------------------------------*/
/*
CREATE OR REPLACE FORCE VIEW DOMIG.V_CONTENTTREE_UI
(P_ACCESSION_ID, P_NAME, P_OBJECT_TYPE, P_PARENT_ID, P_BATCH_ACCESSION_ID,
 P_SORT_KEY, P_DISPLAY_LABEL, P_INSPECTION_REQUIRED, P_INSPECTED, P_FU_TYPE,
 P_WORK_FILENAME, ...., P_STATUS, P_CONTENT_REASON, P_OBJECT_ID,
 P_VIRUS_SCANNED, P_IS_CREATED_BY_WORKFLOW)
AS
SELECT nbobj.p_accession_id p_accession_id,
       nbobj.p_name p_name,
       nbobj.p_object_type p_object_type,
       nbobj.p_parent_id p_parent_id,
	   nbobj.p_batch_accession_id p_batch_accession_id,
       nbobj.p_sort_key p_sort_key,
       nbobj.p_display_label p_display_label,
       nbobj.p_inspection_required p_inspection_required,
       nbobj.p_inspected p_inspected,
       nbobj.p_fu_type p_fu_type,
       nbobj.p_work_filename p_work_filename,
       nbobj.p_status p_status,
       nbobj.p_content_reason p_content_reason,
       nbobj.p_object_id p_object_id,
	   nbobj.p_virus_scanned p_virus_scanned,
	   nbobj.p_is_created_by_workflow p_is_created_by_workflow
FROM
(
SELECT p_accession_id p_accession_id, p_name p_name, 'p_cu' p_object_type, p_batch_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, p_sort_key p_sort_key, p_display_label p_display_label, p_inspection_required p_inspection_required, p_inspected p_inspected, '' p_fu_type, '' p_work_filename, ...., '' p_status, 0 p_content_reason, '' p_object_id, '' p_virus_scanned, '' p_is_created_by_workflow FROM p_cu
union
SELECT p_accession_id p_accession_id, p_name p_name, 'p_fu' p_object_type, p_cu_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, '' p_sort_key, '' p_display_label, '' p_inspection_required, '' p_inspected, p_fu_type p_fu_type, '' p_work_filename, ...., '' p_status, 0 p_content_reason, '' p_object_id, '' p_virus_scanned, '' p_is_created_by_workflow FROM p_fu
union
SELECT p_accession_id p_accession_id, p_name p_name, 'p_su' p_object_type, p_fu_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, '' p_sort_key, '' p_display_label, '' p_inspection_required, '' p_inspected, '' p_fu_type, p_work_filename p_work_filename, ...., p_status p_status, p_content_reason p_content_reason, p_object_id p_object_id, p_virus_scanned p_virus_scanned, p_is_created_by_workflow p_is_created_by_workflow FROM p_su
)nbobj;

select * from V_CONTENTTREE_UI where p_batch_accession_id=1
*/
// 'ORDER BY nbobj.p_object_type asc;' is removed from the Oracle 'View' for performance reasons
// Since the calling application has its own sorting mechanism

    public static Hashtable getContentTreeData(String batchObjectId,
                                                 String cuStateIn,
	                                             ArrayList cuStateList,
	                                             Hashtable listParentObject)
    {
		// Note: cuStateList(ArrayList) is already initialized
		//       listParentObject(Hashtable(parent,object)) is already initialized
		Hashtable listObjectAttribute = new Hashtable();
		// Pull P_ACCESSION_ID,p_inspection_required,p_inspected,p_sort_key also for 'QcReport'
		// For a cuState, parent is BatchAccessionId, the external appln(s) will look at the
		//                 object type and set it to convert this parent to a BatchObjectId
		// If a parent is "" or null, at this point it will be replaced with 'UNKNOWN', possibly
		//                 this would also be converted to a BatchObjectId

		printMe(0, "DBHelperClass-getContentTreeData(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        boolean isBatchLevelContentTree = true;

        try
        {
			if(null != cuStateIn && !cuStateIn.equals(""))
			{
				// It is article level not the whole Batch level content tree
				isBatchLevelContentTree = false;
        		printMe(0, "DBHelperClass-getContentTreeData(Article Level) batchObjectId="+batchObjectId+",cuStateIn="+cuStateIn);
			}
			else
			{
				printMe(0, "DBHelperClass-getContentTreeData(Normal Batch Level) batchObjectId="+batchObjectId);
			}

  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getContentTreeData batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
				if(true == isBatchLevelContentTree)
				{
					// Get normal Batch level Content Tree
                    sql = "SELECT * FROM " + V_CONTENTTREE_UI + " "
                          + " WHERE P_BATCH_ACCESSION_ID = ? ";
				}
				else
				{
					// Get Article level Content Tree
                    sql = "SELECT * FROM " + V_CONTENTTREE_UI + " "
                          + " WHERE P_BATCH_ACCESSION_ID = ?  AND "
                          + " P_CU_ACCESSION_ID = ? ";
				}

                printMe(0, "DBHelperClass-getContentTreeData-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                if(false == isBatchLevelContentTree)
                {
					pstmt.setString(2, cuStateIn);
				}

                rs = pstmt.executeQuery();
                while(rs.next()) {
		        	// Pick all the result data(irrespective of the data type) as a String
		        	Hashtable attrListPerObject = new Hashtable();

	            	String currentObjectAccessionId = rs.getString(P_ACCESSION_ID);
	            	if(currentObjectAccessionId != null)
	            	{
	                	attrListPerObject.put(P_ACCESSION_ID, currentObjectAccessionId);
					}
                    String currentObjectType = rs.getString(P_OBJECT_TYPE);
	            	if(currentObjectType != null)
	            	{
	                	attrListPerObject.put(P_OBJECT_TYPE, currentObjectType);
					}
                    String parentId = rs.getString(P_PARENT_ID);
	            	if(parentId == null || parentId.equals(""))
	            	{
						parentId = UNKNOWN; // This will be handled appropriately by the calling applications
					}
                	attrListPerObject.put(P_PARENT_ID, parentId);
                    String attrValue = rs.getString(P_NAME);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_NAME, attrValue);
					}
                    attrValue = rs.getString(P_BATCH_ACCESSION_ID);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_BATCH_ACCESSION_ID, attrValue);
					}
                    attrValue = rs.getString(P_SORT_KEY);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_SORT_KEY, attrValue);
					}
                    attrValue = rs.getString(P_DISPLAY_LABEL);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_DISPLAY_LABEL, attrValue);
					}
                    attrValue = rs.getString(P_INSPECTION_REQUIRED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_INSPECTION_REQUIRED, attrValue);
					}
                    attrValue = rs.getString(P_INSPECTED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_INSPECTED, attrValue);
					}
                    attrValue = rs.getString(P_FU_TYPE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_FU_TYPE, attrValue);
					}
                    attrValue = rs.getString(P_WORK_FILENAME);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_WORK_FILENAME, attrValue);
					}
                    attrValue = rs.getString(P_IS_EXCLUDED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_IS_EXCLUDED, attrValue);
					}
                    attrValue = rs.getString(P_EXCLUDE_REASON);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_EXCLUDE_REASON, attrValue);
					}
                    attrValue = rs.getString(P_HAS_SUCCESSOR);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_HAS_SUCCESSOR, attrValue);
					}
                    attrValue = rs.getString(P_SUCCESSOR_REASON);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_SUCCESSOR_REASON, attrValue);
					}
/* PMD2.0
                    attrValue = rs.getString(P_ACTIVE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_ACTIVE, attrValue);
					}
*/
                    attrValue = rs.getString(P_STATUS);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_STATUS, attrValue);
					}
                    attrValue = rs.getString(P_STATUS_RATIONALE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_STATUS_RATIONALE, attrValue);
					}
/* PMD2.0
                    attrValue = rs.getString(P_CONTENT_REASON);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CONTENT_REASON, attrValue);
					}
*/
                    attrValue = rs.getString(P_ORIGIN);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_ORIGIN, attrValue);
					}
                    attrValue = rs.getString(P_ORIGIN_RATIONALE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_ORIGIN_RATIONALE, attrValue);
					}
                    attrValue = rs.getString(P_CONTENT_ID);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CONTENT_ID, attrValue);
					}
/*
                    attrValue = rs.getString(P_VIRUS_SCANNED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_VIRUS_SCANNED, attrValue);
					}
*/
                    attrValue = rs.getString(P_IS_CREATED_BY_WORKFLOW);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_IS_CREATED_BY_WORKFLOW, attrValue);
					}
                    attrValue = rs.getString(P_IP_RULES_APPLIED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_IP_RULES_APPLIED, attrValue);
					}
		            if(currentObjectAccessionId != null && !currentObjectAccessionId.equals(""))
		            {
			    		listObjectAttribute.put(currentObjectAccessionId, attrListPerObject);
					    if(listParentObject != null)
					    {
					    	ArrayList childList = new ArrayList();
					    	if(listParentObject.containsKey(parentId))
					    	{
					    		childList = (ArrayList)listParentObject.get(parentId);
					    		listParentObject.remove(parentId);
					    	}
					    	childList.add(currentObjectAccessionId);
					    	listParentObject.put(parentId, childList);
					    }
					    if(cuStateList != null)
					    {
					    	if(currentObjectType != null && currentObjectType.equalsIgnoreCase(CU_TYPE))
					    	{
					    		cuStateList.add(currentObjectAccessionId);
					    	}
     					}
			    	}
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getContentTreeData="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getContentTreeData-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getContentTreeData-End batchObjectId="+batchObjectId);


		return listObjectAttribute;
	}

/*
CREATE OR REPLACE FORCE VIEW DOMIG.V_PROBLEMREPORT_UI
(P_MSG_ID, P_CONTEXT_ID, P_CODE, P_TEXT, P_CATEGORY,
 P_SEVERITY, CONTENT, P_BATCH_ACCESSION_ID, P_NAME, P_OBJECT_TYPE,
 P_PARENT_ID, P_DISPLAY_LABEL, P_FU_TYPE, P_WORK_FILENAME, P_VIRUS_SCANNED,
 P_IS_CREATED_BY_WORKFLOW, P_LEAD_SOURCE_ID, P_OBJECT_ID)
AS
SELECT pumsg.p_id p_msg_id,
       pumsg.p_context_id p_context_id,
	   pumsg.p_code p_code,
	   pumsg.p_text p_text,
	   pumsg.p_category p_category,
	   pumsg.p_severity p_severity,
   	   pumsg.content content,
	   nbobj.p_batch_accession_id p_batch_accession_id,
       nbobj.p_name p_name,
       nbobj.p_object_type p_object_type,
       nbobj.p_parent_id p_parent_id,
       nbobj.p_display_label p_display_label,
       nbobj.p_fu_type p_fu_type,
       nbobj.p_work_filename p_work_filename,
       nbobj.p_virus_scanned p_virus_scanned,
       nbobj.p_is_created_by_workflow p_is_created_by_workflow,
       nbobj.p_lead_source_id p_lead_source_id,
       nbobj.p_object_id p_object_id
FROM
(
SELECT p_accession_id p_accession_id, p_name p_name, 'p_cu' p_object_type, p_batch_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, p_display_label p_display_label, '' p_fu_type, '' p_work_filename, '' p_virus_scanned, '' p_is_created_by_workflow, '' p_lead_source_id, '' p_object_id FROM p_cu
UNION ALL
SELECT p_accession_id p_accession_id, p_name p_name, 'p_fu' p_object_type, p_cu_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, '' p_display_label, p_fu_type p_fu_type, '' p_work_filename, '' p_virus_scanned, '' p_is_created_by_workflow, '' p_lead_source_id, '' p_object_id FROM p_fu
UNION ALL
SELECT p_accession_id p_accession_id, p_name p_name, 'p_su' p_object_type, p_fu_accession_id p_parent_id, p_batch_accession_id p_batch_accession_id, '' p_display_label, '' p_fu_type, p_work_filename p_work_filename, p_virus_scanned p_virus_scanned, p_is_created_by_workflow p_is_created_by_workflow, p_lead_source_id p_lead_source_id, p_object_id p_object_id FROM p_su
UNION ALL
SELECT p_accession_id p_accession_id, p_name p_name, 'p_batch' p_object_type, '' p_parent_id, p_accession_id p_batch_accession_id, '' p_display_label, '' p_fu_type, '' p_work_filename, '' p_virus_scanned, '' p_is_created_by_workflow, '' p_lead_source_id, p_object_id p_object_id FROM p_batch
)nbobj, p_user_message pumsg
WHERE pumsg.p_is_action_taken='N' AND
      pumsg.p_batch_accession_id=nbobj.p_batch_accession_id AND
	  pumsg.p_context_id=nbobj.p_accession_id;
// ORDER BY pumsg.p_severity desc;

select p_msg_id from V_PROBLEMREPORT_UI where p_batch_accession_id='1'

*/
    public static Hashtable getProblemReportData(String batchObjectId)
    {
		// Note: Use bytes[] = getBytes() for the blob column use 'new String(bytes)' and
		// populate this String as the result
		Hashtable listMessageAndContextAttribute = new Hashtable();
		// Pass the 'batchAccessionId' to the Oracle View rather than the 'batchObjectId'
		printMe(0, "DBHelperClass-getProblemReportData(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getProblemReportData batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT * FROM " + V_PROBLEMREPORT_UI + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-getProblemReportData-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                while(rs.next()) {
		        	// Pick all the result data(irrespective of the data type) as a String
		        	Hashtable attrListPerObject = new Hashtable();
	            	String currentMsgId = rs.getString(P_MSG_ID);
	            	if(currentMsgId != null)
	            	{
	                	attrListPerObject.put(P_MSG_ID, currentMsgId);
					}
                    String attrValue = rs.getString(P_CONTEXT_ID);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CONTEXT_ID, attrValue);
					}

                    attrValue = rs.getString(P_CODE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CODE, attrValue);
					}
                    attrValue = rs.getString(P_TEXT);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_TEXT, attrValue);
					}
                    attrValue = rs.getString(P_CATEGORY);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CATEGORY, attrValue);
					}
                    attrValue = rs.getString(P_SEVERITY);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_SEVERITY, attrValue);
					}
    			    Blob contentBlob = rs.getBlob("CONTENT");
				    if(contentBlob != null && contentBlob.length() > 0)
				    {
/*
// RANGA we pick only the 'int' number of bytes, though it has 'long' number of bytes
                        byte[] byteContent = contentBlob.getBytes(1, (int)contentBlob.length());
                        if(byteContent.length > 0)
                        {
				        	String content = new String(byteContent);
				        	if(content != null)
				        	{
				        		attrListPerObject.put(CONTENT, content);
				        	}
				        }
*/
                        InputStream inputStream = contentBlob.getBinaryStream();
                        try
                        {
                            if(inputStream != null)
                            {
					    		BufferedInputStream bis = new BufferedInputStream(inputStream);
					    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                int tempChar;
                                while ( (tempChar = bis.read()) != -1)
                                {
                                    baos.write(tempChar);
                                }
				        	    String content = baos.toString("utf-8");// To preserve utf-8 character set
				        	    if(content != null && !content.equals(""))
				        	    {
				        	    	attrListPerObject.put(CONTENT, content);
				        	    }
							}
					    }
					    catch(Exception e)
					    {
                            printMe(1, "Exception in DBHelperClass-getProblemReportData(Read Content Blob)="+e.toString());
                            e.printStackTrace();
						}
						finally
						{
							try
							{
								if(inputStream != null)
								{
									inputStream.close();
								}
							}
							catch(Exception eClose)
							{
                                printMe(1, "Exception in DBHelperClass-getProblemReportData(Read Content Blob-eClose)="+eClose.toString());
                                eClose.printStackTrace();
							}
						}
					}

                    attrValue = rs.getString(P_NAME);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_NAME, attrValue);
					}
                    attrValue = rs.getString(P_OBJECT_TYPE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_OBJECT_TYPE, attrValue);
					}
                    String parentId = rs.getString(P_PARENT_ID);
	            	if(parentId == null || parentId.equals(""))
	            	{
						parentId = UNKNOWN; // This will be handled appropriately by the calling applications
					}
                	attrListPerObject.put(P_PARENT_ID, parentId);
                    attrValue = rs.getString(P_DISPLAY_LABEL);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_DISPLAY_LABEL, attrValue);
					}
                    attrValue = rs.getString(P_FU_TYPE);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_FU_TYPE, attrValue);
					}
                    attrValue = rs.getString(P_WORK_FILENAME);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_WORK_FILENAME, attrValue);
					}
/*
                    attrValue = rs.getString(P_VIRUS_SCANNED);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_VIRUS_SCANNED, attrValue);
					}
*/
                    attrValue = rs.getString(P_IS_CREATED_BY_WORKFLOW);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_IS_CREATED_BY_WORKFLOW, attrValue);
					}
                    attrValue = rs.getString(P_LEAD_SOURCE_ID);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_LEAD_SOURCE_ID, attrValue);
					}
                    attrValue = rs.getString(P_CONTENT_ID);
	            	if(attrValue != null)
	            	{
	                	attrListPerObject.put(P_CONTENT_ID, attrValue);
					}
		            if(currentMsgId != null && !currentMsgId.equals(""))
		            {
			    		listMessageAndContextAttribute.put(currentMsgId, attrListPerObject);
			    	}
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getProblemReportData="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getProblemReportData-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getProblemReportData-End batchObjectId="+batchObjectId);

		return listMessageAndContextAttribute;
	}

/* PMD2.0, no lookups, all state objects have direct strings
    public static Hashtable getLookupData(String objectType, String attribute)
    {
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getLookupData(Started) objectType="+objectType+","+
		                                                 "attribute="+attribute);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            if(objectType.equalsIgnoreCase(SU_TYPE))
            {
				String lookupTableName = "";
				if(attribute.equals(P_CONTENT_REASON))
				{
					lookupTableName = P_SU_CONTENT_REASON_LKUP;
				}

				else if(attribute.equals(P_CONTENT_SOURCE))
				{
					lookupTableName = P_SU_CONTENT_SOURCE_LKUP;
				}


				if(lookupTableName != null && !lookupTableName.equals(""))
				{
                    sql = "SELECT P_KEY, P_VALUE" + " FROM " + lookupTableName + " ";
			    }
			}

			if(sql != null && !sql.equals(""))
			{
                printMe(0, "DBHelperClass-getLookupData-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                rs = pstmt.executeQuery();
                while(rs.next()) {
    		    	// Pick all the result data(irrespective of the data type) as a String
    		    	String attrKey = rs.getString("P_KEY");
    		    	String attrValue = rs.getString("P_VALUE");
    		    	if(attrKey != null && !attrKey.equals("") &&
    		    	    attrValue != null && !attrValue.equals(""))
    		    	{
						alistOut.put(attrKey, attrValue);
					}
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getLookupData="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getLookupData-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getLookupData-End objectType="+objectType+","+
		                                                 "attribute="+attribute);

		return alistOut;
	}

*/
    // output structure
    // Hashtable1 (key, value) == (contextId1, dataHashPerContext1)
    //                              (contextId2, dataHashPerContext2)
    // dataHashPerContext1 (key, value) == {('PMD', dataHashPMD1),
    //                                      ('DMD', dataHashDMD1)}
    // dataHashPMD1 (key, value) == {('P_ACCESSION_ID', p_accession_id)
    //                                ('P_CONTENT_ID', p_content_id)}
    // dataHashDMD1 (key, value) == {('P_ACCESSION_ID', p_accession_id)
    //                                ('P_CONTENT_ID', p_content_id)}

    public static Hashtable getMetaDataContextMapping(String batchObjectId)
    {
		printMe(0, "DBHelperClass-getMetaDataContextMapping(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        Hashtable metadata = new Hashtable();

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getMetaDataContextMapping batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT * FROM " + V_ARTICLEMETADATA_UI + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-getMetaDataContextMapping-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                while(rs.next())
                {
					Hashtable dataHashPerContext = new Hashtable(); // with hash key as p_md_type(eg: DMD,PMD)

                    String p_accession_id = rs.getString(P_ACCESSION_ID); // Metadata Accession Id
                    String p_name = rs.getString(P_NAME); // eg: 'Descriptive Metadata'
                    String p_md_type = rs.getString(P_MD_TYPE); // eg: 'Descriptive Metadata'
                    String p_status = rs.getString(P_STATUS); // eg: 'Active'
                    String p_context_id = rs.getString(P_CONTEXT_ID); // Context(P_CU_ACCESSION_ID) Id
                    String p_content_id = rs.getString(P_CONTENT_ID);

                    printMe(0, "DBHelperClass-getMetaDataContextMapping-p_context_id="+p_context_id);
    				if(metadata.containsKey(p_context_id))
					{
					    dataHashPerContext = (Hashtable)metadata.get(p_context_id);
					}

                    // Create Hash Data per row received
                    Hashtable dataPerRowReceived = new Hashtable();
                    dataPerRowReceived.put(P_ACCESSION_ID, p_accession_id);
                    dataPerRowReceived.put(P_NAME, p_name);
                    dataPerRowReceived.put(P_MD_TYPE, p_md_type);
                    dataPerRowReceived.put(P_STATUS, p_status);
                    dataPerRowReceived.put(P_CONTEXT_ID, p_context_id);
                    dataPerRowReceived.put(P_CONTENT_ID, p_content_id);

                    // Add this row into the context list, potentially we could have multiple objects
                    //     per context for different p_md_type(s),
                    //     eg: au context object could have a MU(PMD) record and a DMD record
                    dataHashPerContext.put(p_md_type, dataPerRowReceived);

                    metadata.put(p_context_id, dataHashPerContext);
				}
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getMetaDataContextMapping="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getMetaDataContextMapping-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getMetaDataContextMapping-End batchObjectId="+batchObjectId);

		return metadata;
	}

// UPDATE DB CALLS - Start RANGA CONTINUE HERE....

// Note: If 'severity' is passed it would avoid firing a query to get the severity.
//       If warning message do a decrement warning count on the Oracle Batch
	public static boolean setUserMessage(String msgId, String batchObjectId, String qcActionDesc, int severity)
	{
		boolean isSuccessful = true;

        HelperClass.porticoOutput(0, "DBHelperClass-setUserMessages-Call-Started-msgId="+msgId+","+
                                                                                 "batchObjectId="+batchObjectId+","+
                                                                                 "qcActionDesc="+qcActionDesc+","+
                                                                                 "severity="+severity);
        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;

        try
        {
			int intMsgId = -1;
			try
			{
				intMsgId = Integer.parseInt(msgId);
			}
			catch(Exception eParseInt)
			{
				isSuccessful = false;
                printMe(1, "Exception(eParseInt) in DBHelperClass-setUserMessages(msgId)="+msgId+",eParseInt="+eParseInt.toString());
                eParseInt.printStackTrace();
			}

            if(isSuccessful == true && intMsgId != -1)
            {
                sql = "UPDATE "+ USER_MESSAGE_TYPE + " "
                      + "SET P_IS_ACTION_TAKEN = ?, "
                      + "P_ACTION_DESC = ?, "
                      + "P_MODIFY_TIMESTAMP = ? "
                      + "WHERE P_ID = ? ";

                printMe(0, "DBHelperClass-setUserMessages-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

  			    String action_taken = "Y"; // Action has been taken
                pstmt.setString(1, action_taken);
                pstmt.setString(2, qcActionDesc);

                java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
                if(jsqlCurrentTimeStamp != null)
                {
			    	pstmt.setTimestamp(3, jsqlCurrentTimeStamp);
		        }

                pstmt.setInt(4, intMsgId);

                resultRowCount = pstmt.executeUpdate();

                // If the message is a 'warning' and was updated successfully,
                //    decrement the P_WARNING_COUNT on the P_BATCH Oracle object
                HelperClass.porticoOutput(0, "DBHelperClass-setUserMessages-After-executeUpdate-resultRowCount="+resultRowCount);
/* REL-1_1_8, p_active_warning_count removed
                if(severity == 1 && resultRowCount > 0)
                {
                    HelperClass.porticoOutput(0, "DBHelperClass-setUserMessages-Start-Call-decrementWarningCount-batchObjectId="+batchObjectId+","+
                                                                                                             "resultRowCount="+resultRowCount+","+
                                                                                                             "severity="+severity);
			    	isSuccessful = decrementWarningCount(batchObjectId, resultRowCount);
                    HelperClass.porticoOutput(0, "DBHelperClass-setUserMessages-End-Call-decrementWarningCount-batchObjectId="+batchObjectId+","+
                                                                                                             "resultRowCount="+resultRowCount+","+
                                                                                                             "severity="+severity+","+
                                                                                                             "isSuccessful="+isSuccessful);
                }
*/
		    }
        }
        catch(Exception e)
        {
			isSuccessful = false;
            printMe(1, "Exception in DBHelperClass-setUserMessages="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-setUserMessages-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

        HelperClass.porticoOutput(0, "DBHelperClass-setUserMessages-Call-Ended-msgId="+msgId+","+
                                                                                 "batchObjectId="+batchObjectId+","+
                                                                                 "qcActionDesc="+qcActionDesc+","+
                                                                                 "severity="+severity+","+
                                                                                 "resultRowCount="+resultRowCount+","+
                                                                                 "isSuccessful="+isSuccessful);
        return isSuccessful;
	}

    // severity (0=Info), (1=Warning), (2=Fatal)
    public static boolean clearAllMessages(String batchObjectId, String qcActionDesc, int severity)
    {
// RANGA see the impact of always setting isSuccessful=true, because 'clearAllWarningMessages' will be affected for
//       Problem Report/QC Report rendering(onRender())
// Ans: No Impact, we can return a boolean
		boolean isSuccessful = true;

        HelperClass.porticoOutput(0, "DBHelperClass-clearAllMessages-Call-Started-batchObjectId="+batchObjectId+","+
                                                                                 "qcActionDesc="+qcActionDesc+","+
                                                                                 "severity="+severity);
        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;

        try
        {
   		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
   		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				isSuccessful = false;
				printMe(1, "Error in DBHelperClass-clearAllMessages batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "UPDATE "+ USER_MESSAGE_TYPE + " "
                      + "SET P_IS_ACTION_TAKEN = ?, "
                      + "P_ACTION_DESC = ?, "
                      + "P_MODIFY_TIMESTAMP = ? "
                      + "WHERE P_BATCH_ACCESSION_ID = ? "
                      + "AND P_SEVERITY = ? "
                      + "AND P_IS_ACTION_TAKEN = ? ";

                printMe(0, "DBHelperClass-setUserMessages-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

  			    String action_taken = "Y"; // Action has been taken
                pstmt.setString(1, action_taken);
                pstmt.setString(2, qcActionDesc);

                java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
                if(jsqlCurrentTimeStamp != null)
                {
			    	pstmt.setTimestamp(3, jsqlCurrentTimeStamp);
				}

                pstmt.setString(4, batchAccessionId);
                pstmt.setInt(5, severity);
                pstmt.setString(6, "N"); // Set only those user messages for which Action has NOT been taken, so that other
                                         // message for which Action had already been taken is not affected

                resultRowCount = pstmt.executeUpdate();

                // If the message is a 'warning' and was updated successfully,
                //    decrement the P_WARNING_COUNT on the P_BATCH Oracle object
                HelperClass.porticoOutput(0, "DBHelperClass-clearAllMessages-After-executeUpdate-resultRowCount="+resultRowCount);

/*  REL-1_1_8, p_active_warning_count removed
                if(severity == SEVERITY_WARNING && resultRowCount > 0)
                {
                    HelperClass.porticoOutput(0, "DBHelperClass-clearAllMessages-Start-Call-decrementWarningCount-batchObjectId="+batchObjectId+","+
                                                                                                             "resultRowCount="+resultRowCount+","+
                                                                                                             "severity="+severity);
			    	isSuccessful = decrementWarningCount(batchObjectId, resultRowCount);
                    HelperClass.porticoOutput(0, "DBHelperClass-clearAllMessages-End-Call-decrementWarningCount-batchObjectId="+batchObjectId+","+
                                                                                                             "resultRowCount="+resultRowCount+","+
                                                                                                             "severity="+severity+","+
                                                                                                             "isSuccessful="+isSuccessful);
			    }
*/
		    }
        }
        catch(Exception e)
        {
			isSuccessful = false;
            printMe(1, "Exception in DBHelperClass-clearAllMessages="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-clearAllMessages-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

        HelperClass.porticoOutput(0, "DBHelperClass-clearAllMessages-Call-Ended-batchObjectId="+batchObjectId+","+
                                                                                 "qcActionDesc="+qcActionDesc+","+
                                                                                 "severity="+severity+","+
                                                                                 "resultRowCount="+resultRowCount+","+
                                                                                 "isSuccessful="+isSuccessful);
		return isSuccessful;
    }

    // Called from Multiple places(QcHelperClass, PRRResultSet, QCResultSet)
    // Return true, if there were any updates, else return false from Problem Report Perspective
    public static boolean clearAllWarningMessages(String batchObjectId, String qcActionDesc)
    {
		return clearAllMessages(batchObjectId, qcActionDesc, SEVERITY_WARNING);
	}

	public static boolean clearAllFatalMessages(String batchObjectId)
	{
		String qcActionDesc = "UNKNOWN";
		return clearAllMessages(batchObjectId, qcActionDesc, SEVERITY_FATAL);
	}

// Below method to be tested
	public static boolean clearAllInspectedInfo(String batchObjectId)
	{
		boolean isSuccessful = true;

        HelperClass.porticoOutput(0, "DBHelperClass-clearAllInspectedInfo-Call-Started-batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;

        try
        {
   		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
   		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				isSuccessful = false;
				printMe(1, "Error in DBHelperClass-clearAllInspectedInfo batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "UPDATE "+ CU_TYPE + " "
                      + "SET P_INSPECTION_REQUIRED = ?, "
                      + "P_INSPECTED = ?, "
                      + "P_MODIFY_TIMESTAMP = ? "
                      + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-clearAllInspectedInfo-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, "N");
                pstmt.setString(2, "N");

                java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
                if(jsqlCurrentTimeStamp != null)
                {
			    	pstmt.setTimestamp(3, jsqlCurrentTimeStamp);
				}

                pstmt.setString(4, batchAccessionId);

                resultRowCount = pstmt.executeUpdate();
		    }
        }
        catch(Exception e)
        {
			isSuccessful = false;
            printMe(1, "Exception in DBHelperClass-clearAllInspectedInfo="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-clearAllInspectedInfo-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

        HelperClass.porticoOutput(0, "DBHelperClass-clearAllInspectedInfo-Call-Ended-batchObjectId="+batchObjectId+","+
                                                                                  "resultRowCount="+resultRowCount+","+
                                                                                  "isSuccessful="+isSuccessful);

        return isSuccessful;
    }

// Tested fine
    // cuState Accession Id is passed
	public static boolean setArticleInspectionStatus(String objectAccessionId, boolean checkedValue)
	{
		boolean isSuccessful = true;

        HelperClass.porticoOutput(0, "DBHelperClass-setArticleInspectionStatus-Call-Started-objectAccessionId="+objectAccessionId+",checkedValue="+checkedValue);

        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;

        try
        {
            sql = "UPDATE "+ CU_TYPE + " "
                      + "SET P_INSPECTED = ?, "
                      + "P_MODIFY_TIMESTAMP = ? "
                      + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-clearInspectedInfo-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            String boolString = "N";
	        if(checkedValue == true)
	        {
	        	boolString = "Y";
	        }
            pstmt.setString(1, boolString);

            java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
            if(jsqlCurrentTimeStamp != null)
            {
		    	pstmt.setTimestamp(2, jsqlCurrentTimeStamp);
			}

            pstmt.setString(3, objectAccessionId);

            resultRowCount = pstmt.executeUpdate();
        }
        catch(Exception e)
        {
			isSuccessful = false;
            printMe(1, "Exception in DBHelperClass-clearInspectedInfo="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-clearInspectedInfo-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

        HelperClass.porticoOutput(0, "DBHelperClass-clearInspectedInfo-Call-Ended-objectAccessionId="+objectAccessionId+","+
                                                                                  "checkedValue="+checkedValue+","+
                                                                                  "resultRowCount="+resultRowCount+","+
                                                                                  "isSuccessful="+isSuccessful);

		return isSuccessful;
	}

	public static boolean setCheckedInDmdObjectId(String batchAccessionId, String cuAccessionId, String curatedDMDAccessionId, String curatedDMDObjectId)
	{
		boolean isSuccessful = true;

        HelperClass.porticoOutput(0, "DBHelperClass-setCheckedInDmdObjectId-Call-Started-batchAccessionId="+batchAccessionId+",cuAccessionId="+cuAccessionId+",curatedDMDAccessionId="+curatedDMDAccessionId+",curatedDMDObjectId="+curatedDMDObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;

        try
        {
        	sql = "UPDATE "+ DESC_MD_TYPE + " "
                      + "SET "+DBHelperClass.P_CONTENT_ID+" = ?, "
                      + "P_MODIFY_TIMESTAMP = ? "
                      + "WHERE P_ACCESSION_ID = ? ";

        	printMe(0, "DBHelperClass-setCheckedInDmdObjectId-sql="+sql);

        	con = ConnectionManager.getConnection();
        	pstmt = con.prepareStatement(sql);

        	pstmt.setString(1, curatedDMDObjectId);

        	java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
        	if(jsqlCurrentTimeStamp != null)
        	{
        		pstmt.setTimestamp(2, jsqlCurrentTimeStamp);
        	}

        	pstmt.setString(3, curatedDMDAccessionId);

        	resultRowCount = pstmt.executeUpdate();

        	pstmt.close();

            sql = "UPDATE "+ CU_TYPE + " "
                    + "SET P_IS_CURATED = ?, "
                    + "P_MODIFY_TIMESTAMP = ? "
                    + "WHERE P_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-clearIsCurated-sql="+sql);

            //use same connection
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, "N");

            jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
            if(jsqlCurrentTimeStamp != null)
            {
		    	pstmt.setTimestamp(2, jsqlCurrentTimeStamp);
			}

            pstmt.setString(3, cuAccessionId);

            resultRowCount = pstmt.executeUpdate();

        }
        catch(Exception e)
        {
			isSuccessful = false;
            printMe(1, "Exception in DBHelperClass-setCheckedInDmdObjectId="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-setCheckedInDmdObjectId-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

        HelperClass.porticoOutput(0, "DBHelperClass-setCheckedInDmdObjectId-Call-Ended-batchAccessionId="+batchAccessionId+",cuAccessionId="+cuAccessionId+",curatedDMDAccessionId="+curatedDMDAccessionId+",curatedDMDObjectId="+curatedDMDObjectId+
                                                                                  ",isSuccessful="+isSuccessful);

		return isSuccessful;
	}

// UPDATE DB CALLS - End

    // Internal use
	private static String getAttrListAsString(ArrayList alistIn)
	{
		String attrList = "";

		try
		{
			if(alistIn != null && alistIn.size() > 0)
			{
				for(int indx=0; indx < alistIn.size(); indx++)
				{
					if(indx == 0)
					{
						attrList = (String)alistIn.get(indx);
					}
					else
					{
						attrList = attrList + "," + (String)alistIn.get(indx);
					}
				}
			}
		}
		catch(Exception e)
		{
			printMe(1, "Exception in DBHelperClass-getAttrListAsString="+e.toString());
		}
		finally
		{
		}

		return attrList;
	}

    // Any change to logging can be just changed here
	public static void printMe(int level, String msg)
	{
		try
		{
		    HelperClass.porticoOutput(level, msg);
	    }
	    catch(Exception e)
	    {
			System.out.println("Error in DBHelperClass.printMe="+e.toString());
		}
		finally
		{
		}
	}

    public static ArrayList getDistinctWorkFileNames(String batchObjectId)
    {
		ArrayList listWorkFileNames = new ArrayList();

		printMe(0, "DBHelperClass-getDistinctWorkFileNames(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getDistinctWorkFileNames batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT DISTINCT " + P_WORK_FILENAME + " " + P_WORK_FILENAME + " FROM " + SU_TYPE + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-getDistinctWorkFileNames-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                while(rs.next())
                {
                    String currentWorkFileName = rs.getString(P_WORK_FILENAME);
					if(currentWorkFileName != null && !listWorkFileNames.contains(currentWorkFileName))
					{
						// printMe(0, "DBHelperClass-getDistinctWorkFileNames-value="+currentWorkFileName);
						listWorkFileNames.add(currentWorkFileName);
					}
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getDistinctWorkFileNames="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getDistinctWorkFileNames-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getDistinctWorkFileNames-End batchObjectId,numberofDistinctWorkFileNames(s)="+batchObjectId+","+listWorkFileNames.size());

		return listWorkFileNames;
	}

    // Returns the lead metadata's p_su_accession id(s) which are active
/*
select p_su.P_ACCESSION_ID P_ACCESSION_ID from p_su, p_fu
where p_fu.p_cu_accession_id='ark:/27927/dc0tnrbk5' and p_su.P_FU_ACCESSION_ID=p_fu.P_ACCESSION_ID and p_su.P_STATUS='Active' and (p_fu.p_fu_type='Text: Marked Up Header' or
p_fu.p_fu_type='Text: Marked Up Full Text')
*/
    public static ArrayList getActiveLeadMetadataPerArticle(String cuAccessionId)
    {
		ArrayList alistOut = new ArrayList();

		printMe(0, "DBHelperClass-getActiveLeadMetadataPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT SU_ALIAS.P_ACCESSION_ID P_ACCESSION_ID FROM "
                  + SU_TYPE + " SU_ALIAS"+","
                  + FU_TYPE + " FU_ALIAS" +" "
                  + "WHERE FU_ALIAS.P_CU_ACCESSION_ID = ? "
                  + "AND SU_ALIAS.P_FU_ACCESSION_ID = FU_ALIAS.P_ACCESSION_ID "
                  + "AND SU_ALIAS.P_STATUS = ? "
                  + "AND (FU_ALIAS.P_FU_TYPE = ? OR FU_ALIAS.P_FU_TYPE = ?)";

            printMe(0, "DBHelperClass-getActiveLeadMetadataPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);
            pstmt.setString(2, KeyMetadataElementsConstants.SU_STATUS_ACTIVE);
            pstmt.setString(3, LEAD_MD_FU_TYPE_MKUP_HDR);
            pstmt.setString(4, LEAD_MD_FU_TYPE_MKUP_FULLTEXT);

            rs = pstmt.executeQuery();
            while(rs.next())
            {
				alistOut.add(rs.getString(P_ACCESSION_ID));
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getActiveLeadMetadataPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getActiveLeadMetadataPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getActiveLeadMetadataPerArticle-End cuAccessionId="+cuAccessionId);

		return alistOut;
	}

    // Returns (predecessorId, objectId) and also returns thro' parameter the noPredecessor List
    // 2 child enties must not have the same predecessor key
    // Use predecessorId as key, child value1
    // If no predecessors found put them under
/*
    public static Hashtable getObjectPredecessorMappingPerArticle1(String cuAccessionId, ArrayList outNoPredecessorlist)
    {
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
   		    String attrString = "P_ACCESSION_ID,P_PREDECESSOR_ID";
            sql = "SELECT " + attrString + " FROM " + SU_TYPE + " "
                + "WHERE P_CU_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);

            rs = pstmt.executeQuery();
            while(rs.next())
            {
		    	// Pick all the result data(irrespective of the data type) as a String
		    	String attrValue = rs.getString(P_ACCESSION_ID);
	        	String attrKey = rs.getString(P_PREDECESSOR_ID);
		        if(attrKey != null && !attrKey.equals(""))
		        {
					StringTokenizer strTok = new StringTokenizer(attrKey, PREDECESSOR_SEPARATOR);
                    while (strTok.hasMoreTokens())
                    {
						String cToken = strTok.nextToken();
						if(!alistOut.containsKey(cToken))
						{
           	            	alistOut.put(cToken, attrValue);
					    }
					}
				}
				else
				{
					if(outNoPredecessorlist != null)
					{
				    	outNoPredecessorlist.add(attrValue);
				    }
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getObjectPredecessorMappingPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getObjectPredecessorMappingPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-End cuAccessionId="+cuAccessionId);

		return alistOut;
	}
*/

    // Returns (objectId, predecessorId) and also returns thro' parameter the noPredecessor List
    // 2 child enties must not have the same predecessor key
    // Use child as key, predecessorId value1
    // If no predecessors found, put them under the outNoPredecessorlist
    public static Hashtable getObjectPredecessorMappingPerArticle(String cuAccessionId, ArrayList outNoPredecessorlist)
    {
		Hashtable alistOut = new Hashtable();

		printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
   		    String attrString = "P_ACCESSION_ID,P_PREDECESSOR_ID";
            sql = "SELECT " + attrString + " FROM " + SU_TYPE + " "
                + "WHERE P_CU_ACCESSION_ID = ? ";

            printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);

            rs = pstmt.executeQuery();
            while(rs.next())
            {
		    	// Pick all the result data(irrespective of the data type) as a String
		    	String attrKey = rs.getString(P_ACCESSION_ID);
	        	String attrValue = rs.getString(P_PREDECESSOR_ID);
		        if(attrValue != null && !attrValue.equals(""))
		        {
					printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-attrKey="+attrKey+"-attrValue="+attrValue);
					ArrayList tList = new ArrayList();
					StringTokenizer strTok = new StringTokenizer(attrValue, PREDECESSOR_SEPARATOR);
                    while (strTok.hasMoreTokens())
                    {
						String cToken = strTok.nextToken();
						tList.add(cToken);
						printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-cToken="+cToken);
					}
					if(tList != null && tList.size() > 0)
					{
				    	alistOut.put(attrKey, tList);
				    }
				}
				else
				{
					ArrayList tList = new ArrayList();
					alistOut.put(attrKey, tList);

					if(outNoPredecessorlist != null)
					{
				    	outNoPredecessorlist.add(attrKey);
				    }
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getObjectPredecessorMappingPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getObjectPredecessorMappingPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getObjectPredecessorMappingPerArticle-End cuAccessionId="+cuAccessionId);

		return alistOut;
	}

	// public static Hashtable getPageImageList(String objectAccessionId)
    public static ArrayList getActivePageImageListPerArticle(String cuAccessionId)
	{
		ArrayList pageImageList = new ArrayList();
		String pdfFuType = "Rendition: Page Images";

        printMe(0, "DBHelperClass-getActivePageImageListPerArticle(Started) cuAccessionId="+cuAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
            sql = "SELECT /*+ index(FU_ALIAS,SYS_C0040342) index(SU_ALIAS,P_SU_IDX2) */ SU_ALIAS.P_ACCESSION_ID P_ACCESSION_ID FROM "
                  + SU_TYPE + " SU_ALIAS"+","
                  + FU_TYPE + " FU_ALIAS" +" "
                  + "WHERE FU_ALIAS.P_CU_ACCESSION_ID = ? "
                  + "AND SU_ALIAS.P_CU_ACCESSION_ID = ? "
                  + "AND FU_ALIAS.P_ACCESSION_ID = SU_ALIAS.P_FU_ACCESSION_ID "
                  + "AND FU_ALIAS.P_FU_TYPE = ? "
                  + "AND SU_ALIAS.P_STATUS = ?";

            printMe(0, "DBHelperClass-getActivePageImageListPerArticle-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, cuAccessionId);
            pstmt.setString(2, cuAccessionId);
            pstmt.setString(3, pdfFuType);
            pstmt.setString(4, KeyMetadataElementsConstants.SU_STATUS_ACTIVE);

            rs = pstmt.executeQuery();
            while(rs.next())
            {
				String p_su_accession_id = rs.getString("P_ACCESSION_ID");
				if(pageImageList != null && !pageImageList.contains(p_su_accession_id))
				{
				    pageImageList.add(p_su_accession_id);
				}
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getActivePageImageListPerArticle="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getActivePageImageListPerArticle-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getActivePageImageListPerArticle-End for cuAccessionId="+cuAccessionId);

		return pageImageList;
	}

	// Return ArrayList - each item in the list is a ValuePair - key(annotation text), value(create date string)
	public static ArrayList getAnnotationList(String batchAccessionId)
	{
		ArrayList annotationList = new ArrayList();

        printMe(0, "DBHelperClass-getAnnotationList(Started) batchAccessionId="+batchAccessionId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        ValuePair valuePair = null;

        try
        {
            sql = "SELECT P_TEXT, P_CREATE_TIMESTAMP FROM P_ANNOTATE "
                       + "WHERE P_BATCH_ACCESSION_ID = ? "
                       + "ORDER BY P_CREATE_TIMESTAMP DESC";

            printMe(0, "DBHelperClass-getAnnotationList-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, batchAccessionId);
            rs = pstmt.executeQuery();
            while(rs.next())
            {
				String p_create_timestamp = ""; // rs.getString("P_CREATE_TIMESTAMP");
				java.sql.Timestamp timeStamp = rs.getTimestamp("P_CREATE_TIMESTAMP");
				if(timeStamp != null)
				{
					p_create_timestamp = timeStamp.toString();
				}
    			String p_text = rs.getString("P_TEXT");
    			if(p_text == null)
    			{
					p_text = "";
				}
				valuePair = new ValuePair();
				valuePair.setKey(p_text);
				valuePair.setValue(p_create_timestamp);
				annotationList.add(valuePair);
            }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getAnnotationList="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getAnnotationList-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getAnnotationList-End for batchAccessionId="+batchAccessionId);

		return annotationList;
	}

	public static boolean addAnnotation(String batchAccessionId, String annotationText)
	{
		boolean isSuccessful = false;

        printMe(0, "DBHelperClass-addAnnotation(Started) batchAccessionId,annotationText="+batchAccessionId+":"+annotationText);

        Connection con = null;
        PreparedStatement pstmt = null;
        String sql = null;

        try
        {
            // Do DB Insert into the Oracle table p_annotate
            sql = "INSERT INTO P_ANNOTATE ("
        						+ "P_ID" + ","
                               + "P_BATCH_ACCESSION_ID" + ","
                               + "P_TEXT" + ","
                               + "P_CREATE_TIMESTAMP" + ","
                               + "P_MODIFY_TIMESTAMP"
                               + ") "
                               + "VALUES" + "("
                      			+ "P_ANNOTATION_SEQ.nextval" + ","
                               + "?" + ","
                               + "?" + ","
                               + "?" + ","
                               + "?" + ")";

            printMe(0, "DBHelperClass-addAnnotation()-sql="+sql);

            con = ConnectionManager.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, batchAccessionId);
            pstmt.setString(2, annotationText);
            java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
            if(jsqlCurrentTimeStamp != null)
            {
				pstmt.setTimestamp(3, jsqlCurrentTimeStamp);
				pstmt.setTimestamp(4, jsqlCurrentTimeStamp);
		    }
            int resultRowCount = pstmt.executeUpdate();
            if(resultRowCount > 0)
            {
                isSuccessful = true;
			}
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-addAnnotation="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-addAnnotation-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-addAnnotation-End for batchAccessionId,isSuccessful="+batchAccessionId+","+isSuccessful);

		return isSuccessful;
	}

    // Output hash = (keyObjectId, valueHashAtributes)
	public static TreeMap getRenamedFileList(String batchObjectId, String objectType, ArrayList alistIn)
	{
		// Depending on the 'objectType' fire request to the relevant table for all the object(s) in the table
		TreeMap alistOut = new TreeMap();

		printMe(0, "DBHelperClass-getRenamedFileList(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getRenamedFileList batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
    		    String attrString = DBHelperClass.getAttrListAsString(alistIn);

                sql = "SELECT " + attrString
                      + " FROM " + objectType
                      + " WHERE P_BATCH_ACCESSION_ID = ? "
                      + " ORDER BY P_CREATE_TIMESTAMP DESC" ;

                printMe(0, "DBHelperClass-getRenamedFileList-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                int mapIndex = 0;
                while(rs.next())
                {
			    	// Pick all the result data(irrespective of the data type) as a String
			    	Hashtable attrListPerObject = new Hashtable();
			    	for(int indx=0; indx < alistIn.size(); indx++)
			    	{
                        String attrKey = (String)alistIn.get(indx);
			        	String attrValue = rs.getString(attrKey);
			        	if(attrValue != null)
			        	{
			            	attrListPerObject.put(attrKey, attrValue);
			    	    }
			    	    else
			    	    {
			            	attrListPerObject.put(attrKey, "");
						}
			        }
			        if(attrListPerObject != null && attrListPerObject.size() > 0)
			        {
						alistOut.put(mapIndex, attrListPerObject);
					}

					mapIndex++;
                }
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getRenamedFileList="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getRenamedFileList-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getRenamedFileList-End batchObjectId,objectType="+batchObjectId+","+objectType);

		return alistOut;
	}


/*
CREATE OR REPLACE VIEW V_AUARTICLEMAPPING_UI
(P_AU_ACCESSION_ID, P_CU_ACCESSION_ID, P_AU_NAME, P_CU_NAME,
P_BATCH_ACCESSION_ID)
AS
SELECT p_au_obj.p_accession_id p_au_accession_id,
       p_cu_obj.p_accession_id p_cu_accession_id,
           p_au_obj.p_name p_au_name,
           p_cu_obj.p_name p_cu_name,
           p_au_obj.p_batch_accession_id p_batch_accession_id
FROM P_AU p_au_obj, P_CU p_cu_obj
where p_cu_obj.p_au_accession_id=p_au_obj.p_accession_id order by p_au_obj.p_batch_accession_id, p_au_obj.p_accession_id, p_cu_obj.p_accession_id;
*/
    // 'additionalObjectInfo' has to be initialized by the caller, it would be populated here
    // with details about the AU and CU objects
    public static Hashtable getArchivalUnitArticleMapping(String batchObjectId, Hashtable additionalObjectInfo)
    {
		printMe(0, "DBHelperClass-getArchivalUnitArticleMapping(Started) batchObjectId="+batchObjectId);

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;
        Hashtable<String, ArrayList> archivalUnitArticleMapping = new Hashtable<String, ArrayList>();

        try
        {
  		    String batchAccessionId = getBatchAccessionIdFromBatchId(batchObjectId);
  		    if(batchAccessionId == null || batchAccessionId.equals(""))
  		    {
				printMe(1, "Error in DBHelperClass-getArchivalUnitArticleMapping batchAccessionId NOT FOUND for batchObjectId="+batchObjectId);
			}
			else
			{
                sql = "SELECT * FROM " + V_AUARTICLEMAPPING_UI + " "
                      + "WHERE P_BATCH_ACCESSION_ID = ? ";

                printMe(0, "DBHelperClass-getArchivalUnitArticleMapping-sql="+sql);

                con = ConnectionManager.getConnection();
                pstmt = con.prepareStatement(sql);

                pstmt.setString(1, batchAccessionId);

                rs = pstmt.executeQuery();
                while(rs.next())
                {
					ArrayList dataListPerArchivalUnit = new ArrayList();
                    String p_au_accession_id = rs.getString(P_AU_ACCESSION_ID);
                    String p_cu_accession_id = rs.getString(P_CU_ACCESSION_ID);
                    String p_au_name = rs.getString(P_AU_NAME);
                    String p_cu_name = rs.getString(P_CU_NAME);

    				if(archivalUnitArticleMapping.containsKey(p_au_accession_id))
					{
					    dataListPerArchivalUnit = (ArrayList)archivalUnitArticleMapping.get(p_au_accession_id);
					}
					// Since the query is ordered, at least take advantage of the internal CU Order.
					//       The parent AU order has to be taken care by the caller.
					dataListPerArchivalUnit.add(p_cu_accession_id);
					archivalUnitArticleMapping.put(p_au_accession_id, dataListPerArchivalUnit);
					// We may populate the 'p_au_accession_id' again, since each row would have the same p_au_accession_id
					//    but different p_cu_accession_id, but it is okay
					additionalObjectInfo.put(p_au_accession_id, p_au_name);
					additionalObjectInfo.put(p_cu_accession_id, p_cu_name);
				}
		    }
        }
        catch(Exception e)
        {
            printMe(1, "Exception in DBHelperClass-getArchivalUnitArticleMapping="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(rs != null) rs.close();
                if(pstmt != null) pstmt.close();
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                printMe(1, "Exception in DBHelperClass-getArchivalUnitArticleMapping-close()="+e1.toString());
                e1.printStackTrace();
			}
        }

		printMe(0, "DBHelperClass-getArchivalUnitArticleMapping-End batchObjectId="+batchObjectId);

		return archivalUnitArticleMapping;
	}

}
