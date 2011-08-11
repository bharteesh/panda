/*
 * Confidential Property of Portico
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesChangeProfile.java
 * Created on       Mar 18, 2008
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
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;


/**
 * Description  Fetches and reschedules batches
 * Author       Ranga
 * Type         BulkActionSetBatchesChangeProfile
 * Note         Only a single Provider can be selected on the Batch List screen.
 */
public class BulkActionSetBatchesChangeProfile extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionSetBatchesChangeProfile(String actionType, IDfSession dfSession)
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

		HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processPrecondition()");

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
                    String queuePriority = "";
                    Hashtable currentAttrHash = (Hashtable)m_hashData.get(currentObjectId);
            		HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processPrecondition()-currentObjectId="+currentObjectId);
                    if(currentAttrHash != null)
                    {
						boolean isValid = false;
                        if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME))
                        {
							objectName = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.OBJECT_NAME);
						}
/*---------------------------------Start changes for action 1 of 3 -------------------------------------------------*/
                        try
                        {
            		        HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processPrecondition()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD));
// 27JUN2008 - The preconditions(as per Vinay) must be the same as in the case of 'Reset Batches' + HelperClassConstants.LOADED(Loaded) state
                            String currentBatchStatus = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_STATE);
               				if(currentBatchStatus.equalsIgnoreCase(HelperClassConstants.LOADED) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
               				     currentBatchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
               				{
						        isValid = true;
								m_successfulPreconditionBatchList.put(currentObjectId, objectName);
							}

/*

                            if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD) == true &&
                                 ((String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD)).equals("0"))
                            {
            		            HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processPrecondition()-currentObjectId(p_onhold is 0)="+currentObjectId);
                                Hashtable addlnInfo = new Hashtable();
                                addlnInfo.put(HelperClassConstants.ISBATCHONHOLD, "false");
                                addlnInfo.put(HelperClassConstants.BATCHSTATUS, (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_STATE));
                                String performer = "";
                                if(currentAttrHash.containsKey(SubmissionBatchObjectListWithMyBatches.P_PERFORMER))
                                {
						    		performer = (String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_PERFORMER);
						    	}
                                addlnInfo.put(HelperClassConstants.BATCHPERFORMER, performer);
                                addlnInfo.put(HelperClassConstants.BATCHOBJECTID, currentObjectId);
                                if(HelperClass.performerCheck(m_dfSession, currentObjectId, addlnInfo) == true)
                                {
						    		// No MsgId needs to be passed here make it ""
                                    if(QcHelperClass.isValidChangeProfileAction(m_dfSession, currentObjectId, "", addlnInfo) == true)
                                    {
    					    		    isValid = true;
    					    			m_successfulPreconditionBatchList.put(currentObjectId, objectName);
            			    		}
						        }
        				    }
*/
/*---------------------------------End changes for action 1 of 3 -------------------------------------------------*/
    				    }
    				    catch(Exception ein)
    				    {
							isSuccessful = false;
                			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesChangeProfile-processPrecondition()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesChangeProfile-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processUpdate()");

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
						if(addlnData.containsKey(BulkAction.PROFILE_ID_STRING))
						{
                            String profileIdString = (String)addlnData.get(BulkAction.PROFILE_ID_STRING);
		                    if(profileIdString != null && !profileIdString.equals(""))
		                    {
                                IDfSysObject iDfSysObject = (IDfSysObject)m_dfSession.getObject(new DfId(currentObjectId));
								iDfSysObject.setString("p_profile_id", profileIdString);
								// Mimic a Reset to Loaded state
                                iDfSysObject.setInt("p_rawunit_count", 0);
                                iDfSysObject.setInt("p_problem_state_count", 0);
                                iDfSysObject.setString("p_workflow_id", "");
                                iDfSysObject.setString("p_last_activity", "");
                                iDfSysObject.setString("p_performer_for_display", "");
                                // This has to be applied even for the 'Reset' bulk action, this will make sure that
                                //      the Scheduler does not count this as part of a 'processing' Batch anymore.
                                iDfSysObject.setBoolean("p_auto_processing", false);
                                if(iDfSysObject.getString(HelperClassConstants.BATCH_STATE).equals(HelperClassConstants.LOADED))
                                {
                                    iDfSysObject.setString("p_sched_timestamp", "");
            			            iDfSysObject.save();
            			            isValid = true;
								}
								else
								{
            			            isValid = BulkActionSetBatchesReset.abortBatchWorkflowThroServerMethod(m_dfSession, currentObjectId);
            			            if(isValid == true)
            			            {
                                        iDfSysObject.setTime("p_sched_timestamp", new DfTime());
                                        iDfSysObject.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.QUEUED);
            			                iDfSysObject.save();
								    }
							    }
       ;						if(isValid == true)
        						{
             			            m_successfulUpdateBatchList.put(currentObjectId, objectName);
        						}
					        }
				        }
/*---------------------------------End changes for action 2 of 3 -------------------------------------------------*/
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesChangeProfile-processUpdate()-objectName="+objectName+","+"exception="+ein.toString());
   						ein.printStackTrace();
   					}
   					finally
   					{
						HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processUpdate()-finally-per batch");
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesChangeProfile-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesChangeProfile-processUpdate()-finally");
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