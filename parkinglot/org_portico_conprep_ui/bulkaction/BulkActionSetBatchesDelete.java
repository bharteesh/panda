/*
 * Confidential Property of Portico
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesDelete.java
 * Created on       Mar 14, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfId;


/**
 * Description  Fetches and reschedules batches
 * Author       Ranga
 * Type         BulkActionSetBatchesDelete
 */
public class BulkActionSetBatchesDelete extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionSetBatchesDelete(String actionType, IDfSession dfSession)
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

		HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processPrecondition()");

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
            		HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processPrecondition()-currentObjectId="+currentObjectId);
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
            		        HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processPrecondition()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD));
                            String currentBatchStatus = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_STATE);
               				if(currentBatchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
               				{
								String lastActivity = "";
								if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.P_LAST_ACTIVITY))
								{
									lastActivity = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_LAST_ACTIVITY);
								}
								if(lastActivity.equalsIgnoreCase("Ingest To Archive") ||
								    lastActivity.equalsIgnoreCase("Clean Up"))
								{
									// DO NOT allow action on these batches JIRA - CONPREP-1211, UI batch delete change
								}
								else
								{
     						        isValid = true;
							    	m_successfulPreconditionBatchList.put(currentObjectId, objectName);
							    }
							}
/*---------------------------------End changes for action 1 of 3 -------------------------------------------------*/
    				    }
    				    catch(Exception ein)
    				    {
							isSuccessful = false;
                			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesDelete-processPrecondition()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesDelete-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processUpdate()");

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

                        // DELETE THE WORKFLOW INSTANCE also

                        isValid = HelperClass.postProcessingForWithdraw(m_dfSession, currentObjectId);
                        if(isValid == true)
                        {
                            Hashtable currentAttrHash = (Hashtable)m_hashData.get(currentObjectId);
                            if(currentAttrHash != null)
                            {
								String workflowId = "";
                                if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.P_BATCH_WORKFLOW_ID))
                                {
        							workflowId = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_BATCH_WORKFLOW_ID);
        						}
        						// Note: Only a Super user OR the workflow installation owner can abort a workflow,
        						//       conprep inspectors may NOT be able to do this,
        						//       hence this has to be done by the backend job
    							// isValid = abortBatchWorkflow(m_dfSession, workflowId);
							}
							if(isValid == true)
							{
     			                m_successfulUpdateBatchList.put(currentObjectId, objectName);
						    }
						}
/*---------------------------------End changes for action 2 of 3 -------------------------------------------------*/
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesDelete-processUpdate()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesDelete-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesDelete-processUpdate()-finally");
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

    public static boolean abortBatchWorkflow(IDfSession iDfSession, String workflowId)
    {
        boolean isSuccessful = true;

        try
        {
            IDfWorkflow workflow = (IDfWorkflow)iDfSession.getObject(new DfId(workflowId));
            if(workflow.getRuntimeState() != IDfWorkflow.DF_WF_STATE_TERMINATED )
            {
                try
                {
                    workflow.haltAll();
                }
                catch(Exception e)
                {
					// This Exception can be ignored, it is okay
				}
                workflow.abort();
            }
            workflow.destroy();
        }
        catch(Exception e)
        {
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesDelete-abortBatchWorkflow()-"+"exception="+e.toString());
			e.printStackTrace();
        }
        finally
        {
        }

        return isSuccessful;
    }

    // SubmissionBatchObjectListWithMyBatches.P_BATCH_WORKFLOW_ID

    private TreeMap m_successfulPreconditionBatchList;
    private TreeMap m_failedPreconditionBatchList;
    private TreeMap m_successfulUpdateBatchList;
    private TreeMap m_failedUpdateBatchList;
    private IDfSession m_dfSession;
}