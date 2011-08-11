/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesOffHold.java
 * Created on       Feb 10, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;


/**
 * Description  Fetches and holds data for Bulk Actions
 * Author       Ranga
 * Type         BulkActionSetBatchesOffHold
 */
public class BulkActionSetBatchesOffHold extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionSetBatchesOffHold(String actionType, IDfSession dfSession)
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

		HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processPrecondition()");

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
            		HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processPrecondition()-currentObjectId="+currentObjectId);
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
            		        HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processPrecondition()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD));
                            if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD) == true &&
                                     ((String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD)).equals("1"))
                            {
								isValid = true;
								m_successfulPreconditionBatchList.put(currentObjectId, objectName);
        					}
/*---------------------------------End changes for action 1 of 3 -------------------------------------------------*/
    				    }
    				    catch(Exception ein)
    				    {
							isSuccessful = false;
                			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesOffHold-processPrecondition()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesOffHold-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processUpdate()");

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
/*---------------------------------Start changes for action 2 of 3 -------------------------------------------------*/
                        IDfSysObject iDfSysObject = (IDfSysObject)m_dfSession.getObject(new DfId(currentObjectId));
                        iDfSysObject.setBoolean("p_on_hold", false);
        			    iDfSysObject.save();
/*---------------------------------End changes for action 2 of 3 -------------------------------------------------*/
        			    isValid = true;
        			    m_successfulUpdateBatchList.put(currentObjectId, objectName);
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesOffHold-processUpdate()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesOffHold-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesOffHold-processUpdate()-finally");
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

    private TreeMap m_successfulPreconditionBatchList;
    private TreeMap m_failedPreconditionBatchList;
    private TreeMap m_successfulUpdateBatchList;
    private TreeMap m_failedUpdateBatchList;
    private IDfSession m_dfSession;
}