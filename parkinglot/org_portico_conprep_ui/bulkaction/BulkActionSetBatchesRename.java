/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesRename.java
 * Created on       Mar 10, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;


/**
 * Description  Fetches and reschedules batches
 * Author       Ranga
 * Type         BulkActionSetBatchesRename
 */
public class BulkActionSetBatchesRename extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionSetBatchesRename(String actionType, IDfSession dfSession)
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

		HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processPrecondition()");

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
            		HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processPrecondition()-currentObjectId="+currentObjectId);
                    if(currentAttrHash != null)
                    {
						boolean isValid = false;
                        if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME))
                        {
							objectName = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME);
						}
                        try
                        {
/*---------------------------------Start changes for action 1 of 3 -------------------------------------------------*/
            		        HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processPrecondition()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD));
                            String currentBatchStatus = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_STATE);
               				if(currentBatchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
               				{
						        isValid = true;
								m_successfulPreconditionBatchList.put(currentObjectId, objectName);
							}
/*---------------------------------End changes for action 1 of 3 -------------------------------------------------*/
    				    }
    				    catch(Exception ein)
    				    {
							isSuccessful = false;
                			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesRename-processPrecondition()-objectName="+objectName+","+"exception="+ein.toString());
    						ein.printStackTrace();
    					}
    					finally
    					{
    				    	if(isValid == false)
    				    	{
								m_failedPreconditionBatchList.put(currentObjectId, objectName);
					    	}
					    }
				    }
				}

				// Refresh this page after this action
			}
		}
		catch(Exception e)
		{
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesRename-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processUpdate()");

		try
		{
            m_successfulUpdateBatchList.clear();
            m_failedUpdateBatchList.clear();

			if(m_successfulPreconditionBatchList != null && m_successfulPreconditionBatchList.size() > 0)
			{
				Iterator iterate = m_successfulPreconditionBatchList.keySet().iterator();
                while(iterate.hasNext())
		        {
					boolean isValid = false;
                    String currentObjectId = (String)iterate.next();
                    String objectName = (String)m_successfulPreconditionBatchList.get(currentObjectId);
                    try
                    {
						if(addlnData.containsKey(BulkAction.PREFIX_STRING))
						{
                            String prefixString = (String)addlnData.get(BulkAction.PREFIX_STRING);
		                    if(prefixString != null && !prefixString.equals(""))
		                    {
/*---------------------------------Start changes for action 2 of 3 -------------------------------------------------*/
                                IDfFolder tIDfFolder = (IDfFolder)m_dfSession.getObject(new DfId(currentObjectId));
                                String newBatchName = prefixString+tIDfFolder.getObjectName();
                                // Get the complete batch folder path
                                String folderPath = tIDfFolder.getFolderPath(0);
                                String parentFolderPath = folderPath.substring(0, folderPath.lastIndexOf("/"));
                                String newBatchFolderPath = parentFolderPath + "/" + newBatchName;
                                if(checkIfBatchExists(newBatchFolderPath) == false)
                                {
									tIDfFolder.setObjectName(newBatchName);
									tIDfFolder.save();
        			                isValid = true;
        			                m_successfulUpdateBatchList.put(currentObjectId, objectName);
								}
/*---------------------------------End changes for action 2 of 3 -------------------------------------------------*/
					        }
					    }
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesRename-processUpdate()-objectName="+objectName+","+"exception="+ein.toString());
   						ein.printStackTrace();
   					}
   					finally
   					{
						if(isValid == false)
						{
							m_failedUpdateBatchList.put(currentObjectId, objectName);
						}
					}
				}
/*---------------------------------Start changes for action 3 of 3 -------------------------------------------------*/
				isSuccessful = processAnnotation(addlnData);
/*---------------------------------End changes for action 3 of 3 -------------------------------------------------*/
			}
		}
		catch(Exception e)
		{
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesRename-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-processUpdate()-finally");
		}

		return isSuccessful;
	}

	public boolean processAnnotation(Hashtable addlnData)
	{
		boolean isSuccessful = false;

	    TreeMap annotateFailedMap = new TreeMap();
		TreeMap annotateSuccessfulMap = new TreeMap();
	    isSuccessful = BulkActionAnnotateBatches.processAnnotateDB(m_hashData, addlnData, m_successfulUpdateBatchList, annotateFailedMap, annotateSuccessfulMap);

        if(annotateFailedMap.size() > 0)
        {
			isSuccessful = false;
		}

	    return isSuccessful;
	}

	public TreeMap getSuccessfulPreconditionBatchList()
	{
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

    public boolean checkIfBatchExists(String newBatchFolderPath)
    {
        // HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-checkIfBatchExists()-newBatchFolderPath="+newBatchFolderPath);

        boolean batchExists = false;

        try
        {
            if(m_dfSession.getFolderByPath(newBatchFolderPath) != null)
            {
                batchExists = true;
            }
        }
        catch(Exception e)
        {
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesRename-checkIfBatchExists()-"+"exception="+e.toString());
			e.printStackTrace();
        }
        finally
        {
        }

        // HelperClass.porticoOutput(0, "BulkActionSetBatchesRename-checkIfBatchExists()-newBatchFolderPath,batchExists="+newBatchFolderPath+","+batchExists);

        return batchExists;
    }

    private TreeMap m_successfulPreconditionBatchList;
    private TreeMap m_failedPreconditionBatchList;
    private TreeMap m_successfulUpdateBatchList;
    private TreeMap m_failedUpdateBatchList;
    private IDfSession m_dfSession;
}