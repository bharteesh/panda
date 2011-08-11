/*
 * Confidential Property of Portico
 *
 * Project          ConPrep WebTop
 * Module
 * File             BulkActionSetBatchesReset.java
 * Created on       Mar 27, 2008
 *
 */
package org.portico.conprep.ui.bulkaction;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.RenameUtil;
import org.portico.conprep.ui.objectlist.SubmissionBatchObjectListWithMyBatches;
import org.portico.conprep.workflow.impl.documentum.WorkflowConfig;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfId;


/**
 * Description  Fetches and resets batches to Loaded state
 * Author       Ranga
 * Type         BulkActionSetBatchesReset
 */
public class BulkActionSetBatchesReset extends BulkActionResultSet implements BulkActionSetBatchesInterface
{
    public BulkActionSetBatchesReset(String actionType, IDfSession dfSession)
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

		HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processPrecondition()");

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
            		HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processPrecondition()-currentObjectId="+currentObjectId);
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
            		        HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processPrecondition()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(SubmissionBatchObjectListWithMyBatches.P_ON_HOLD));
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
                			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-processPrecondition()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-processPrecondition()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processPrecondition()-finally");
		}

		return isSuccessful;
	}

	public boolean processUpdate(Hashtable addlnData)
	{
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processUpdate()");

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
						// Do this irrespective of the batch workflow attribute on the batch, any other hanging
						// workflows will also be cleaned up by the 'abort workflow' server method.
                        IDfSysObject iDfSysObject = (IDfSysObject)m_dfSession.getObject(new DfId(currentObjectId));
                        iDfSysObject.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.LOADED);
                        iDfSysObject.setInt("p_rawunit_count", 0);
                        iDfSysObject.setInt("p_problem_state_count", 0);
                        iDfSysObject.setString("p_workflow_id", "");
                        iDfSysObject.setString("p_last_activity", "");
                        iDfSysObject.setString("p_sched_timestamp", "");
                        iDfSysObject.setString("p_performer_for_display", "");
                        iDfSysObject.setBoolean("p_auto_processing", false);
       			        iDfSysObject.save();

       			        // Clean up rename records in p_su_rename_action assoc with this batch (conprep-1962)

       		            String batchId = DBHelperClass.getBatchAccessionIdFromBatchId(currentObjectId);
       		            int delCnt = RenameUtil.deleteSuRename(batchId);
       		            HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-count of deleted suRename rcds="+delCnt);

       			        // Abort the workflow also.
      				    // Note: Only a Super user OR the workflow installation owner can abort a workflow,
       					//       conprep inspectors may NOT be able to do this,
       					//       hence this has to be done by a Server method
       					// Passing a batchId would be better, since multiple workflow(s) for this

   						isValid = abortBatchWorkflowThroServerMethod(m_dfSession, currentObjectId);

						if(isValid == true)
						{
     			            m_successfulUpdateBatchList.put(currentObjectId, objectName);
						}
/*---------------------------------End changes for action 2 of 3 -------------------------------------------------*/
					}
   				    catch(Exception ein)
   				    {
						isSuccessful = false;
               			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-processUpdate()-objectName="+objectName+","+"exception="+ein.toString());
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-processUpdate()-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-processUpdate()-finally");
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
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-abortBatchWorkflow()-"+"exception="+e.toString());
			e.printStackTrace();
        }
        finally
        {
        }

        return isSuccessful;
    }

    public static boolean abortBatchWorkflowThroServerMethod(IDfSession iDfSession, String batchId)
    {
        boolean isSuccessful = false;

        IDfCollection iDfCollection = null;

        try
        {
            String docbase = LdapUtil.getAttribute(LdapUtil.SUBCONTEXT_WORKFLOW,
                                                    WorkflowConfig.ENTRY_WORKFLOW_LISTENER,
                                                    WorkflowConfig.ATTRIBUTE_WORKFLOW_DOCBASE);
			String serverUser = LdapUtil.getAttribute(LdapUtil.SUBCONTEXT_WORKFLOW,
			                                    WorkflowConfig.ENTRY_WORKFLOW_LISTENER,
			                            		ATTRIBUTE_SERVER_USER);
			String serverPassword = LdapUtil.getAttribute(LdapUtil.SUBCONTEXT_WORKFLOW,
					                            WorkflowConfig.ENTRY_WORKFLOW_LISTENER,
					                            ATTRIBUTE_SERVER_PASSWORD);
			// RUN THE SERVER METHOD 'p_abort_workflow'
/*
			String applyDQL = " EXECUTE do_method WITH METHOD='p_abort_workflow'," + " ARGUMENTS='" + batchId
						+ " " + docbase + " " + serverUser + " " + serverPassword + "'";
*/
            String applyDQL = "EXECUTE do_method WITH METHOD='p_abort_workflow', ARGUMENTS='-batchId "+
                              batchId +
                              " -docbase " +
                              docbase +
                              " -serverUser " +
                              serverUser +
                              " -serverPassword " +
                              serverPassword + "'";

            HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-abortBatchWorkflowThroServerMethod()-"+"applyDQL="+applyDQL);

			IDfQuery serverMethodQuery = new DfQuery();
			serverMethodQuery.setDQL(applyDQL);
			// The 'iDfSession' is a one with limited privilege ?
			iDfCollection = serverMethodQuery.execute(iDfSession, IDfQuery.DF_EXEC_QUERY);
			// Can we read the 'iDfCollection' object to check for the result ?
            if(iDfCollection != null)
            {
                while(iDfCollection.next())
                {
					isSuccessful = !iDfCollection.getBoolean("launch_failed"); // Here we have to negate the result
                    HelperClass.porticoOutput(0, "BulkActionSetBatchesReset-abortBatchWorkflowThroServerMethod()-"+"launch_failed="+!isSuccessful);
					break;
                }
            }
        }
        catch(Exception e)
        {
			isSuccessful = false;
   			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-abortBatchWorkflowThroServerMethod()-"+"exception="+e.toString());
			e.printStackTrace();
        }
        finally
        {
			try
			{
				if(iDfCollection != null)
				{
					iDfCollection.close();
				}
    		}
    		catch(Exception e)
    		{
    			HelperClass.porticoOutput(1, "Exception in BulkActionSetBatchesReset-abortBatchWorkflowThroServerMethod()-iDfCollection.close()"+"exception="+e.toString());
    			e.printStackTrace();
			}
        }

        return isSuccessful;
    }

    private TreeMap m_successfulPreconditionBatchList;
    private TreeMap m_failedPreconditionBatchList;
    private TreeMap m_successfulUpdateBatchList;
    private TreeMap m_failedUpdateBatchList;
    private IDfSession m_dfSession;

    public final static String ATTRIBUTE_SERVER_USER = "server_user";
    public final static String ATTRIBUTE_SERVER_PASSWORD = "server_password";
}