/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionAnnotateBatches.java
 * Created on       Mar 06, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.db.ConnectionManager;
import org.portico.conprep.db.DBBatchObject;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.IDfSession;


/**
 * Description  Fetches and holds data for Bulk Actions
 * Author       Ranga
 * Type         BulkActionAnnotateBatches
 */
public class BulkActionAnnotateBatches extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionAnnotateBatches(String actionType, IDfSession dfSession)
    {
		super(actionType, dfSession);
        m_dfSession = dfSession;
        m_successfulPreconditionBatchList = new TreeMap();
        m_failedPreconditionBatchList = new TreeMap();
        m_successfulUpdateBatchList = new TreeMap();
        m_failedUpdateBatchList = new TreeMap();
        processPrecondition();
    }

	public boolean processPrecondition()
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processPrecondition()");

		try
		{
			m_successfulPreconditionBatchList.clear();
			m_failedPreconditionBatchList.clear();

			if(m_hashData != null && m_hashData.size() > 0)
			{
				Iterator iterate = m_hashData.keySet().iterator();
                while(iterate.hasNext())
                {
                    String currentObjectId = (String)iterate.next();
                    String objectName = "";
                    Hashtable currentAttrHash = (Hashtable)m_hashData.get(currentObjectId);
            		HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processPrecondition()-currentObjectId="+currentObjectId);
                    if(currentAttrHash != null)
                    {
                        if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME))
                        {
							objectName = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME);
							m_successfulPreconditionBatchList.put(currentObjectId, objectName);
						}
				    }
				}
			}
		}
		catch(Exception e)
		{
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processUpdate()");

		try
		{
			isSuccessful = processAnnotation(addlnData);
		}
		catch(Exception e)
		{
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processUpdate()-finally");
		}

		return isSuccessful;
	}

	public boolean processAnnotation(Hashtable addlnData)
	{
		boolean isSuccessful = false;

		m_failedUpdateBatchList.clear();
		m_successfulUpdateBatchList.clear();
        isSuccessful = processAnnotateDB(m_hashData, addlnData, m_successfulPreconditionBatchList, m_failedUpdateBatchList, m_successfulUpdateBatchList);
        if(m_failedUpdateBatchList.size() > 0)
        {
			isSuccessful = false;
		}

	    return isSuccessful;
	}

    // Note: outFailedMap, outSuccessfulMap have to be initialized by the calling method
	public static boolean processAnnotateDB(Hashtable allData, Hashtable addlnData, TreeMap inputObjectIdMap, TreeMap outFailedMap, TreeMap outSuccessfulMap)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processAnnotateDB()");

        Connection con = null;
        PreparedStatement pstmt = null;
        int resultRowCount = 0;
        String sql = null;
        String annotate_text = "";

		try
		{
            con = ConnectionManager.getConnection();
            outSuccessfulMap.clear();
            outFailedMap.clear();

            if(addlnData != null && addlnData.size() > 0 && addlnData.containsKey(BulkAction.ANNOTATE_TEXT))
            {
                annotate_text = (String)addlnData.get(BulkAction.ANNOTATE_TEXT);
		    }
			if(annotate_text != null && !annotate_text.equals("") && inputObjectIdMap != null && inputObjectIdMap.size() > 0)
			{
				Iterator iterate = inputObjectIdMap.keySet().iterator();
                while(iterate.hasNext())
		        {
					boolean isValid = false;
                    String currentObjectId = (String)iterate.next();
            		HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processAnnotateDB()-currentObjectId="+currentObjectId);
                    String objectName = (String)inputObjectIdMap.get(currentObjectId);
                    String currentObjectAccessionId = "";
                    Hashtable currentAttrHash = (Hashtable)allData.get(currentObjectId);
                    if(currentAttrHash != null)
                    {
                        if(currentAttrHash.containsKey(BulkActionResultSet.P_ACCESSION_ID))
                        {
							currentObjectAccessionId = (String)currentAttrHash.get(BulkActionResultSet.P_ACCESSION_ID);
						}
				    }
                    // This scenario will not happen, just in case the p_accession_id is not populated, try getting
                    // another way
                    if(currentObjectAccessionId == null || currentObjectAccessionId.equals(""))
                    {
                        currentObjectAccessionId = DBBatchObject.getBatchAccessionIdFromBatchId(currentObjectId);
				    }
                    try
                    {
/*---------------------------------Start changes for action 2 of 2 -------------------------------------------------*/
                        // Do DB Insert into the Oracle table p_annotate
                        sql = "INSERT INTO "+ P_ANNOTATE + "("
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

                        HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processAnnotateDB()-sql="+sql);
                        pstmt = con.prepareStatement(sql);
                        pstmt.setString(1, currentObjectAccessionId);
                        pstmt.setString(2, annotate_text);
                        java.sql.Timestamp jsqlCurrentTimeStamp = new java.sql.Timestamp(DBBatchObject.getCurrentTimeInMilliSeconds());
                        if(jsqlCurrentTimeStamp != null)
                        {
    			            pstmt.setTimestamp(3, jsqlCurrentTimeStamp);
    			            pstmt.setTimestamp(4, jsqlCurrentTimeStamp);
		                }
                        resultRowCount = pstmt.executeUpdate();
/*---------------------------------End changes for action 2 of 2 -------------------------------------------------*/
                        if(resultRowCount > 0)
                        {
        			        isValid = true;
        			        outSuccessfulMap.put(currentObjectId, objectName);
					    }
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-processAnnotateDB()-objectName="+objectName+","+"exception="+ein.toString());
   						ein.printStackTrace();
   					}
   					finally
   					{
						if(isValid == false)
						{
							outFailedMap.put(currentObjectId, objectName);
						}
						try
						{
                            if(pstmt != null) pstmt.close();
					    }
					    catch(Exception e)
					    {
               			    HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-processAnnotateDB()-pstmt.close()-objectName="+objectName+","+"exception="+e.toString());
   						    e.printStackTrace();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-processAnnotateDB()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-processAnnotateDB()-finally");
            try
            {
                if(con != null) ConnectionManager.closeConnection(con);
            }
            catch (Exception e1)
            {
                HelperClass.porticoOutput(1, "Exception in BulkActionAnnotateBatches-ConnectionManager.closeConnection()="+e1.toString());
                e1.printStackTrace();
			}

		}

		return isSuccessful;
	}

	public TreeMap getSuccessfulPreconditionBatchList()
	{
/*
		Iterator iterate = getSortedData(m_successfulPreconditionBatchList).keySet().iterator();
        while(iterate.hasNext())
        {
            String currentKey = (String)iterate.next();
            HelperClass.porticoOutput(0, "BulkActionAnnotateBatches-sortedObject-asc(sortkey,value)="+currentKey+","+(String)m_successfulPreconditionBatchList.get(currentKey));
		}
*/
		return getSortedData(m_successfulPreconditionBatchList);
	}

	public TreeMap getFailedPreconditionBatchList()
	{
		return getSortedData(m_failedPreconditionBatchList);
	}

	public TreeMap getSuccessfulUpdateBatchList()
	{
		return getSortedData(m_successfulUpdateBatchList);
	}

	public TreeMap getFailedUpdateBatchList()
	{
		return getSortedData(m_failedUpdateBatchList);
	}

	public void clearData()
	{
		if(m_successfulPreconditionBatchList != null)
		{
			m_successfulPreconditionBatchList.clear();
		}
		if(m_failedPreconditionBatchList != null)
		{
			m_failedPreconditionBatchList.clear();
		}
		if(m_successfulUpdateBatchList != null)
		{
			m_successfulUpdateBatchList.clear();
		}
		if(m_failedUpdateBatchList != null)
		{
			m_failedUpdateBatchList.clear();
		}
		super.clearData();
	}

    private TreeMap m_successfulPreconditionBatchList;
    private TreeMap m_failedPreconditionBatchList;
    private TreeMap m_successfulUpdateBatchList;
    private TreeMap m_failedUpdateBatchList;
    private IDfSession m_dfSession;

    private static final String P_ANNOTATE = "p_annotate";
}