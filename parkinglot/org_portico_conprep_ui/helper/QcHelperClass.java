
package org.portico.conprep.ui.helper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.workflow.impl.documentum.ActionTool;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPackage;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfQueueItem;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVirtualDocument;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.operations.IDfDeleteNode;
import com.documentum.operations.IDfDeleteOperation;

public class QcHelperClass
{
	// Default Provider id Entry in the config file
	public static final String profileDefault = "default";

	public static final String LDAP_ADDNEWFILE_IN_NONQCBATCHSTATUS = "addnewfile_in_nonqcbatchstatus";
	public static final String LDAP_INSPECTIONCHECK = "inspectioncheck";

    public QcHelperClass()
    {

    }

// ---------------------PRECONDITIONS for QC Actions - Start----------------------------------

    // objectId - SUStateId being repaired
	public static boolean isValidReplaceableFileAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;
		HelperClass.porticoOutput(0, "QcHelperClass-isValidReplaceableFileAction(Call-Started)-objectId="+objectId);
		HelperClass.porticoOutput(0, "QcHelperClass-isValidReplaceableFileAction(Call-Started)-msgObjectId="+msgObjectId);
		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

    		HelperClass.porticoOutput(0, "QcHelperClass-isValidReplaceableFileAction(Call-Ended)-objectId="+objectId+","+isValid);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidReplaceableFileAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidReplaceableFileAction(Call-finally)-objectId="+objectId + " isValid="+isValid);
		}

		return isValid;
	}

    // objectId(selected) - BatchId
	public static boolean isValidAddNewFileAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

        HelperClass.porticoOutput(0, "QcHelperClass-isValidAddNewFileAction(Call-Start)-objectId="+objectId);

		try
		{
			String batchStatus = getStatusForBatchObject(currentSession, objectId, addlnInfo);
            isValid = isValidBatchStatusForTrueQCAction(batchStatus);
   			if(batchStatus != null && false == isValid)
   			{
				isValid = batchStatus.equalsIgnoreCase("LOADED") && isValidAddNewFileActionAdditionalCheck();
			}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidAddNewFileAction="+e.getMessage());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidAddNewFileAction(Call-finally)-objectId="+objectId + " isValid="+isValid);
		}

		return isValid;
	}

// Prob Res Claim(step 1)
	public static boolean isValidProbResClaimAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
	    boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidProbResClaimAction(Call-Started)-batchObjectId="+batchObjectId);

	    try
	    {
		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
			if(batchStatus != null)
			{
				if(batchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
				    batchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
				{
					// Added SYSTEM_ERROR as per Vinay 05JAN2005 for Archive
		    		isValid = canUserClaimBatch(currentSession, batchObjectId, addlnInfo);
				}
			}
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidProbResClaimAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidProbResClaimAction-batchObjectId="+batchObjectId + " isValid="+isValid);
		}

		return isValid;
	}

// QC claim(step 2)
	public static boolean isValidClaimAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
	    boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidClaimAction(Call-Started)-batchObjectId="+batchObjectId);

	    try
	    {
		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
			if(batchStatus != null)
			{
				if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT))
				{
		    		isValid = canUserClaimBatch(currentSession, batchObjectId, addlnInfo);
				}
			}
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidClaimAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidClaimAction-batchObjectId="+batchObjectId + " isValid="+isValid);
		}

		return isValid;
	}

/*

	public static int getWorkItemRunState(IDfSession currentSession, String batchObjectId)
	{
		int workitemRunState = -1;
		ActionTool actionTool = null;

		HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState(Call-Started)-batchObjectId="+batchObjectId);

        try
		{
            // Call the backend method
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState-Start Call-getWorkItem()");
            String workItemId = actionTool.getWorkItem();
            HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
                HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState-Before-Call-workItem-getRuntimeState");
				workitemRunState = iDfWorkitem.getRuntimeState();
		    }
		    HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState-(workItemId,runState)=" + workItemId + "," + workitemRunState);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-getWorkItemRunState="+e.toString());
	        // e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState-Call-finally-(batchObjectId,workitemRunState)="+ batchObjectId + "," + workitemRunState);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
			}
		}

		HelperClass.porticoOutput(0, "QcHelperClass-getWorkItemRunState(Call-Ended)-batchObjectId,workitemRunState="+batchObjectId+","+workitemRunState);

		return workitemRunState;
	}

*/
	public static boolean isValidContinueProcessingAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		return isValidContinueProcessingAction(currentSession, batchObjectId, false, addlnInfo); // Do not ignore Fatal Messages
	}

	public static boolean isValidStartAllOverAction(IDfSession currentSession, String batchObjectId, boolean performerCheck, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidStartAllOverAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
    				String lastActivity = HelperClass.getLastActivity(currentSession, batchObjectId, addlnInfo);
					if(lastActivity.equalsIgnoreCase("Ingest To Archive") ||
			               lastActivity.equalsIgnoreCase("Clean Up"))
			        {
						// DO NOT allow a 'StartAllOver' - JIRA - CONPREP-1211, UI batch delete change
					}
					else
					{
    					if(batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM))
					    {
					    	isValid = performerCheck;
					    }
					    else if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
					    {
					    	isValid = true;
					    }
				    }
				}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidStartAllOverAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "QcHelperClass-isValidStartAllOverAction(Call-Ended)-batchObjectId,isValid="+batchObjectId+","+isValid);

		return isValid;
	}

	public static boolean isValidReleaseToArchiveAction(IDfSession currentSession, String batchObjectId, boolean performerCheck, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidReleaseToArchiveAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
					if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
					    batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
					{
    			    	// No QC action has been taken
			    	    if(getIsActionTakenForBatch(currentSession, batchObjectId, addlnInfo) == false)
			    	    {
			    	    	// Can be released to Archive
    		    	    	isValid = isInspectionCheckDone(currentSession, batchObjectId);
                            // On Dec,30,2005, Due to AddUserMessages, we check if any fatal, active, User/Data Messages are present,
                            // Note: Adding 'Data' too because we do the same in 'Continue' too.
                            if(isValid == true)
                            {
                                boolean isActive = true; // Active
        			        	String severity = "2"; // fatal
        			        	String[] category = {"0","2"}; // 0=Data, 2=User
			    	            // has Active Fatal Data/User Errors
    			    	        if(hasErrors(currentSession, batchObjectId, isActive, severity, category))
    			    	        {
    								isValid = false;
    								HelperClass.porticoOutput(0, "QcHelperClass-isValidReleaseToArchiveAction-hasErrors-batchObjectId="+batchObjectId + " isValid="+isValid);
    							}
    						}
    			        }
    			        if(isValid == true)
    			        {
    			            if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
    			            {
    							isValid = performerCheck;
    						}
    				    }
    			    }
    		    }
		    }
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidReleaseToArchiveAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidReleaseToArchiveAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidExcludeFileAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = true;
		HelperClass.porticoOutput(0, "QcHelperClass-isValidExcludeFileAction(Call-Started)-objectId="+objectId);
		HelperClass.porticoOutput(0, "QcHelperClass-isValidExcludeFileAction(Call-Started)-msgObjectId="+msgObjectId);
		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

    		HelperClass.porticoOutput(0, "QcHelperClass-isValidExcludeFileAction(Call-Ended)-objectId="+objectId+","+isValid);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidExcludeFileAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidExcludeFileAction(Call-finally)-objectId="+objectId + " isValid="+isValid);
		}

		return isValid;
	}

	public static boolean isValidChangeProfileAction(IDfSession currentSession, String batchObjectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidChangeProfileAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
		    boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
			    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
				isValid = isValidBatchStatusForTrueQCAction(batchStatus);
			}
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidChangeProfileAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidChangeProfileAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidRenameFileAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidRenameFileAction(Call-Started)-objectId="+objectId);

		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidRenameFileAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidRenameFileAction-objectId="+objectId + " isValid="+isValid);

        return isValid;
	}

    // Add a new FU to the Batch,
/*
	public static boolean isValidAddNewFuAction(IDfSession currentSession, String batchObjectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidAddNewFuAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
			isValid = isValidBatchStatusForTrueQCAction(batchStatus);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidAddNewFuAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidAddNewFuAction-batchObjectId="+batchObjectId + " isValid="+isValid);

        return isValid;
	}
*/
// This has to be treated as a true QC action, if a problem is found during Automated Curation
// must be enabled in 'Problem Resolution'(QC1_IN_PROGRESS), if the curated DMD is to be changed at time of
// inspection(QC2_IN_PROGRESS), user's will put the Batch in 'Problem Resolution'(executing Go_to_Problem_Resolution)
// So, here checking for 'QC1_IN_PROGRESS' is appropriate as a true QC action.

	public static boolean isValidEditCuratedDmdAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidEditCuratedDmdAction(Call-Started)-objectId="+objectId);

		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidEditCuratedDmdAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidEditCuratedDmdAction-objectId="+objectId + " isValid="+isValid);

        return isValid;
	}

// Resolve Id Conflict for FU State
	public static boolean isValidResolveIdentityConflictAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidResolveIdentityConflictAction(Call-Started)-objectId="+objectId);

		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidEditCuratedDmdAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidResolveIdentityConflictAction-objectId="+objectId + " isValid="+isValid);

        return isValid;
	}

// Go from QC(step 2)-> ProbRes(step 1)
	public static boolean isValidQcToProbResAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidQcToProbResAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
			isValid = batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidQcToProbResAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidQcToProbResAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidBatchStatusForTrueQCAction(String batchStatus)
	{
		boolean isValid = false;

		if(batchStatus != null)
		{
			isValid = batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM);
		}

		return isValid;
    }

    public static String getReentryActivityForBatch(IDfSession currentSession, String batchObjectId)
    {
		HelperClass.porticoOutput(0, "HelperClass-getReentryActivityForBatch(Started-From Storage) for Batch_ID="+ batchObjectId);

   	    String attrValue = "";
        ValuePair tValuePair = null;
        ArrayList attrList = new ArrayList();
        attrList.add("p_reentry_activity");
        ArrayList outList = HelperClass.getObjectAttrValues(currentSession, DBHelperClass.BATCH_TYPE, batchObjectId, attrList);
        if(outList != null && outList.size() > 0)
        {
            for(int indx=0; indx < outList.size(); indx++)
            {
            	tValuePair = (ValuePair)outList.get(indx);
               	attrValue = (String)tValuePair.getValue(); // value of p_reentry_activity
               	break;
	       	}
		}

		HelperClass.porticoOutput(0, "HelperClass-getReentryActivityForBatch(Ended-From Storage) for Batch_ID,attrValue="+ batchObjectId+","+attrValue);

		return attrValue;
	}

	public static boolean getIsActionTakenForBatch(IDfSession currentSession, String batchObjectId)
	{
		boolean isActionTaken = false;
		HelperClass.porticoOutput(0, "HelperClass-getIsActionTakenForBatch(Started-From Storage) for Batch_ID="+ batchObjectId);

   	    String attrValue = "";
        ValuePair tValuePair = null;
        ArrayList attrList = new ArrayList();
        attrList.add("p_user_action_taken");
        ArrayList outList = HelperClass.getObjectAttrValues(currentSession, DBHelperClass.BATCH_TYPE, batchObjectId, attrList);
        if(outList != null && outList.size() > 0)
        {
            for(int indx=0; indx < outList.size(); indx++)
            {
            	tValuePair = (ValuePair)outList.get(indx);
               	attrValue = (String)tValuePair.getValue(); // value of p_user_action_taken
               	break;
	       	}
		}

        if(attrValue != null && attrValue.equals("1"))
        {
			isActionTaken = true;
		}

		HelperClass.porticoOutput(0, "HelperClass-getIsActionTakenForBatch(Ended-From Storage) for Batch_ID,isActionTaken="+ batchObjectId+","+isActionTaken);

		return isActionTaken;
	}

/* Used for link/relink of su/fu etc.
	public static boolean isUnitIdentityResolved(IDfSession currentSession, String objectId, String objectType)
	{
		boolean isValid = false;

		String parentFolderId = getParentFolderId(currentSession, objectId);
		String parentObjectType = HelperClass.getObjectType(currentSession, parentFolderId);

		if((objectType.equals(HelperClass.getInternalObjectType("fu_state")) &&
		      parentObjectType.equals(HelperClass.getInternalObjectType("cu_state"))) ||
		   (objectType.equals(HelperClass.getInternalObjectType("su_state")) &&
		      parentObjectType.equals(HelperClass.getInternalObjectType("fu_state"))))
		{
			isValid = true;
		}

		return isValid;
	}
*/

/*
	public static boolean hasWarningThresholdExceeded(IDfSession currentSession, String batchObjectId)
	{
		boolean isValid = false;

		try
		{
        	String workpadId = getWorkPadId(currentSession, batchObjectId);
        	if(workpadId != null && !workpadId.equals(""))
        	{
    		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(workpadId));
    		    int currentWarningCount = iDfSysObject.getInt("p_active_warning_count");
    		    int currentWarningThreshold = iDfSysObject.getInt("p_warning_threshold");
                // Added 'currentWarningThreshold > 0' since a change was made on DocApp
                // on 22,JULY,2005 by Vinay, where in 'Warning Threshold Count' is set to '0'
    		    if(currentWarningThreshold > 0 && currentWarningCount >= currentWarningThreshold)
    		    {
					isValid = true;
				}
    		}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-hasWarningThresholdExceeded="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-hasWarningThresholdExceeded-Call-finally");
		}

		return isValid;
	}
*/

/*
	public static boolean hasWarningThresholdExceeded(IDfSession currentSession, String batchObjectId)
	{
		returnolean isValid = false;


		try
		{
			ArrayList alistIn = new ArrayList();
			alistIn.add("p_active_warning_count");
			alistIn.add("p_warning_threshold");
			Hashtable alistOut = DBHelperClass.getBatchAttributes(batchObjectId, alistIn);

            int currentWarningCount = -1;
            int currentWarningThreshold = -1;
			if(alistOut != null)
			{
				Object obj = null;
				if(alistOut.containsKey("p_active_warning_count"))
				{
					obj = alistOut.get("p_active_warning_count");
					if(obj != null)
					{
				    	currentWarningCount = Integer.parseInt((String)obj);
				    }
				}
				if(alistOut.containsKey("p_warning_threshold"))
				{
					obj = alistOut.get("p_warning_threshold");
					if(obj != null)
					{
				    	currentWarningThreshold = Integer.parseInt((String)obj);
				    }
				}
			}
			HelperClass.porticoOutput(0, "QcHelperClass-hasWarningThresholdExceeded-currentWarningCount="+currentWarningCount);
			HelperClass.porticoOutput(0, "QcHelperClass-hasWarningThresholdExceeded-currentWarningThreshold="+currentWarningThreshold);

			// This check is to ensure that we received the data
            if(currentWarningCount != -1 && currentWarningThreshold != -1)
            {
                // Added 'currentWarningThreshold > 0' since a change was made on DocApp
                // on 22,JULY,2005 by Vinay, where in 'Warning Threshold Count' is set to '0'
   	    	    if(currentWarningThreshold > 0 && currentWarningCount >= currentWarningThreshold)
   	    	    {
	    			isValid = true;
	    		}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-hasWarningThresholdExceeded="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-hasWarningThresholdExceeded-Call-finally");
		}

		return isValid;
	}
*/

/*
	public static boolean hasErrors(IDfSession currentSession, String batchObjectId, boolean isActive, String severity, String[] category)
	{
		boolean isValid = false;
		IDfCollection tIDfCollection = null;
		try
		{
			String categoryIn = "";
			if(category != null && category.length > 0)
			{
				for(int indx=0; indx < category.length; indx++)
				{
					if(indx == 0)
					{
						categoryIn = "'" + category[indx] + "'";
					}
					else
					{
						categoryIn = categoryIn + "," + "'" + category[indx] + "'";
					}
				}
			}


			String action_taken = "FALSE";
			if(isActive == false)
			{
				action_taken = "TRUE";
			}
			// p_severity = 'fatal'("2"), p_category IN 'data,user'("0","2"), p_action_taken = 'active'(FALSE)
            String attrNames = "r_object_id";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("usermessage_object") +
                               " where " +
                               " p_category IN (" + categoryIn + ")" + " AND " +
                               " p_severity=" + "'" + severity + "'" + " AND " +
                               " p_action_taken=" + action_taken + " AND " +
                               " FOLDER(ID(" + "'"+batchObjectId+"'" + "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-hasErrors()-dqlString="+dqlString);

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					isValid = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-hasErrors():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-hasErrors() CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-hasErrors()-close" + e.toString());
				e.printStackTrace();
			}
		}

		return isValid;
	}
*/

	public static boolean hasErrors(IDfSession currentSession, String batchObjectId, boolean isActive, String severity, String[] category)
	{
		return DBHelperClass.hasErrors(batchObjectId, isActive, severity, category);
	}

/*
    public static boolean isInspectionCheckDone(IDfSession currentSession, String batchObjectId)
    {
		boolean isCheckDone = true;

		// Check for any CU State where p_inspection_required=TRUE and p_inspected=FALSE
        IDfCollection tIDfCollection = null;
        try
		{
            String attrNames = "r_object_id";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("cu_state") +
                               " where p_inspection_required=TRUE AND p_inspected=FALSE AND " +
                               " FOLDER(ID(" + "'"+batchObjectId+"'" + "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-isInspectionCheckDone()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    isCheckDone = false;
   					HelperClass.porticoOutput(0, "QcHelperClass-isInspectionCheckDone-Not inspected Cu State Id="+tIDfCollection.getString("r_object_id"));
   					break;
				}
   	    	}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isInspectionCheckDone="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-isInspectionCheckDone CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-isInspectionCheckDone-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-isInspectionCheckDone-Call-finally");
		}

		return isCheckDone;
	}
*/

    public static boolean isInspectionCheckDone(IDfSession currentSession, String batchObjectId)
    {
		boolean isValid = false;
		isValid = isEnvOverridableAction(LDAP_INSPECTIONCHECK);
		if(isValid == false)
		{
		    isValid = DBHelperClass.isInspectionCheckDone(batchObjectId);
	    }
	    return isValid;
	}

    public static boolean isValidReentryPointForContinue(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
		boolean isValid = true;

		String currentRentryPoint = getReentryActivityForBatch(currentSession, batchObjectId, addlnInfo);

		if(currentRentryPoint.equals(HelperClass.getReentryPointName("generatemets")))
		{
			isValid = false;
		}

		return isValid;
	}

	public static boolean isValidAddUserMessageAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidAddUserMessageAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
			isValid = batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidAddUserMessageAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidAddUserMessageAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidDelegateProbResAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidDelegateProbResAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
            String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
   		    if(batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM))
            {
				// using getBatchPerformer here is efficient
                // IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                // String performer = iDfSysObject.getString("p_performer");
                String performer = getBatchPerformer(currentSession, batchObjectId, addlnInfo);
                if(performer != null && !performer.equals(""))
                {
                    if(currentSession.getLoginUserName().equals(performer))
                    {
	        	        isValid = true;
		    	    }
			    }
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidDelegateProbResAction()-" + e.toString());
			e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidDelegateProbResAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidDelegateQcAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidDelegateQcAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
            String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
   		    if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
            {
				// using getBatchPerformer here is efficient
                // IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                // String performer = iDfSysObject.getString("p_performer");
                String performer = getBatchPerformer(currentSession, batchObjectId, addlnInfo);
                if(performer != null && !performer.equals(""))
                {
                    if(currentSession.getLoginUserName().equals(performer))
                    {
    		        	isValid = true;
    			    }
			    }
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidDelegateQcAction()-" + e.toString());
			e.printStackTrace();
	    }
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidDelegateQcAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidLowerPresLvlAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidLowerPresLvlAction(Call-Started)-objectId="+objectId);

		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidLowerPresLvlAction()-" + e.toString());
			e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidLowerPresLvlAction-objectId="+objectId + " isValid="+isValid);

        return isValid;
	}

// No scope defined for this action
	public static boolean isValidPorticoEditFileAction(IDfSession currentSession, String objectId, String hasBeenCheckedOutByThisUser, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidPorticoEditFileAction(Call-Started)-objectId="+objectId);

		try
		{
            if(objectId != null && !objectId.equals(""))
            {
                IDfSysObject currentIDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
                // if(currentIDfSysObject.isCheckedOutBy(currentSession.getLoginUserName()) == false)
                // Has anyone checked this file out, not only the current user
                if(currentIDfSysObject.isCheckedOut() == false || currentIDfSysObject.isCheckedOutBy(currentSession.getLoginUserName()))
                {
					isValid = true;
				}
		    }
/*
            if(isValid == true)
			{
				// This will be true, if the user has atleast once edited(checked out) the content file
				// even if he/she has checked it in, from the current opened edit curated dmd component point of view
				// we do not want to allow edit button to be enabled again after the check in
     			if(hasBeenCheckedOutByThisUser != null && hasBeenCheckedOutByThisUser.equals("true"))
    			{
    				// If it has been checked out by this user at least once while on the edit curated dmd component,
    				// we do not want to enable edit again(it cannot be edited again)
                    isValid = false;
    			}
		    }
*/
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidPorticoEditFileAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

    	HelperClass.porticoOutput(0, "QcHelperClass-isValidPorticoEditFileAction()-objectId,isValid="+objectId + "," + isValid);

        return isValid;
	}

    public static boolean canUserClaimBatch(IDfSession currentSession, String batchObjectId)
    {
        return canUserClaimBatch(currentSession, batchObjectId, null);
	}

    public static boolean canUserClaimBatch(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;
        IDfCollection workflows = null;

        HelperClass.porticoOutput(0, "QcHelperClass-canUserClaimBatch-Started-batchObjectId="+batchObjectId);

        if(batchObjectId != null)
        {
		    try
		    {
				 String userNameList = "";
				 ArrayList currentUserAndGroupsList = AppSessionContext.getCurrentUserAndGroupsListUI();
				 if(currentUserAndGroupsList != null && currentUserAndGroupsList.size() > 0)
				 {
				     for(int indx=0; indx < currentUserAndGroupsList.size(); indx++)
				     {
						 if(indx == 0)
						 {
							 userNameList = "'" + (String)currentUserAndGroupsList.get(indx) + "'";
						 }
						 else
						 {
						     userNameList = userNameList + "," + "'" + (String)currentUserAndGroupsList.get(indx) + "'";
					     }
					 }

					 HelperClass.porticoOutput(0, "QcHelperClass-canUserClaimBatch-userNameList="+userNameList);
// Performance, try HelperClass.getWorkflowObject() instead
                     String workflowId = null;
/*
                     IDfSysObject batch = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                     workflows= batch.getWorkflows(null,null);
                     if(workflows.next())
                     {
    			    	// There can be only 1 workflow associated with a Batch
                        workflowId = workflows.getString("r_workflow_id");
                     }
*/
                     workflowId = HelperClass.getWorkflowObject(currentSession, batchObjectId, addlnInfo);
                     if(null != workflowId && !workflowId.equals(""))
                     {
                         HelperClass.porticoOutput(0, "QcHelperClass-canUserClaimBatch-workflowId="+workflowId);
						 // delete_flag=false returns the active rows, which have not been marked for delete
                         String qualification = "dmi_queue_item WHERE name IN "+
                                                "(" + userNameList + ")" +
                                                 " and router_id='"+workflowId+"' and task_state='dormant'" +
                                                 " and delete_flag=false";

                         HelperClass.porticoOutput(0, "QcHelperClass-canUserClaimBatch-qualification="+qualification);

                         IDfQueueItem queueItem = (IDfQueueItem) currentSession.getObjectByQualification(qualification);
                         if( null != queueItem )
                         {
                             isValid = true;
                         }
                     }
			     }
			}
            catch(Exception e)
            {
			    HelperClass.porticoOutput(1, "Exception in QcHelperClass-canUserClaimBatch-"+e.toString());
			    e.printStackTrace();
			}
			finally
			{
			    if(workflows != null)
			    {
			   	    try
				    {
                        workflows.close();
					}
					catch(Exception e)
					{
					    HelperClass.porticoOutput(1, "Exception in QcHelperClass-canUserClaimBatch-workflows.close()-"+e.toString());
					    e.printStackTrace();
					}
				}
			}
        }

        HelperClass.porticoOutput(0, "QcHelperClass-canUserClaimBatch-batchObjectId="+batchObjectId + " isValid="+isValid);

        return isValid;
    }

	public static boolean isValidInspectedAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidInspectedAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
   			if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
		    {
		    	// No QC action has been taken
		    	if(getIsActionTakenForBatch(currentSession, batchObjectId, addlnInfo) == false)
		    	{
   		    		isValid = isInspectionCheckDone(currentSession, batchObjectId);
                    // On Dec,30,2005, Due to AddUserMessages, we check if any fatal, active, User/Data Messages are present,
                    // Note: Adding 'Data' too because we do the same in 'Continue' too.
                    if(isValid == true)
                    {
                        boolean isActive = true; // Active
      			    	String severity = "2"; // fatal
       			    	String[] category = {"0","2"}; // 0=Data, 2=User
		    	        // has Active Fatal Data/User Errors
                        // On Dec,30,2005, why not Warning threshold exceeded as in 'Continue'
		    	        if(hasErrors(currentSession, batchObjectId, isActive, severity, category))
		    	        {
							isValid = false;
							HelperClass.porticoOutput(0, "QcHelperClass-isValidInspectedAction-hasErrors-batchObjectId="+batchObjectId + " isValid="+isValid);
						}
					}
		        }
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidInspectedAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidInspectedAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidContinueProcessingAction(IDfSession currentSession, String batchObjectId, boolean ignoreFatalMessages, Hashtable addlnInfo)
	{
		HelperClass.porticoOutput(0, "QcHelperClass-isValidContinueProcessingAction-batchObjectId,ignoreFatalMessages="+batchObjectId + ",ignoreFatalMessages="+ignoreFatalMessages);

		boolean isValid = false;

		try
		{
		    boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
	    	    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
	    	    // 'SYSTEM_ERROR' actual value will be set only while being  Released To Archive and
	    	    // not any time during 'Auto Processing'
	    	    // as per Suku on MAY,06,2005 @12:10PM
    		    if(batchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
	            {
			    	isValid = true;
			    }
			    else if(batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
			             batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
			    {
			    	isValid = true;
			    	boolean isActive = true; // Active
			    	String severity = "2"; // fatal
                    // Added category for "User" also 30DEC2005, to disable 'Continue' if User Fatal Error messages are present
			    	// 0=Data, 2=User
			    	String[] category = {"0","2"};

			    	// has Active Fatal Data/User Errors OR has Warning threshold exceeded
			    	if((hasErrors(currentSession, batchObjectId, isActive, severity, category) && !ignoreFatalMessages))
			    	{
						isValid = false;
					}

					if(isValid == true)
					{
				        isValid = isValidReentryPointForContinue(currentSession, batchObjectId, addlnInfo);
				    }
			    }
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidContinueProcessingAction()-"+e.toString());
			e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidContinueProcessingAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidClearFatalAndContinueProcessingAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		return isValidContinueProcessingAction(currentSession, batchObjectId, true, addlnInfo);// ignore Fatal messages
	}

	public static boolean isValidReScheduleAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidReScheduleAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
					if(batchStatus.equalsIgnoreCase(HelperClassConstants.QUEUED))
					{
						isValid = true;
					}
				}
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidReScheduleAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidReScheduleAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidScheduleAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidScheduleAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
					if(batchStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
					{
						isValid = true;
					}
				}
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidScheduleAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidScheduleAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidSetHoldAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidSetHoldAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
					if(!batchStatus.equals(HelperClassConstants.AUTO_PROCESSING) &&
			             !batchStatus.equals(HelperClassConstants.POST_PROCESSING) &&
			             !batchStatus.equals(HelperClassConstants.RELEASED) &&
			             !batchStatus.equals(HelperClassConstants.INGESTED) &&
			             !batchStatus.equals(HelperClassConstants.RETAINED))
					{
						isValid = true;
					}
				}
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidSetHoldAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidSetHoldAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidUnScheduleAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidUnScheduleAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == false)
			{
    		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
    			if(batchStatus != null)
    			{
					if(batchStatus.equalsIgnoreCase(HelperClassConstants.QUEUED))
					{
						isValid = true;
					}
				}
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidUnScheduleAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidUnScheduleAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidUnSetHoldAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidUnSetHoldAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
			boolean batchOnHold = isBatchOnHold(currentSession, batchObjectId, addlnInfo);
			if(batchOnHold == true)
			{
			    isValid = true;
		    }
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidUnSetHoldAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidUnSetHoldAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}


	public static boolean isValidProbResReportAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidProbResReportAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
   		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
   			if(batchStatus != null)
   			{
				if(batchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
				    batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
				    batchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
				{
					isValid = true;
				}
			}
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidProbResReportAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidProbResReportAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

	public static boolean isValidQcReportAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;

		HelperClass.porticoOutput(0, "QcHelperClass-isValidQcReportAction(Call-Started)-batchObjectId="+batchObjectId);

		try
		{
   		    String batchStatus = getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
   			if(batchStatus != null)
   			{
				if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
				    batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
				    batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
				{
					isValid = true;
				}
			}
		}
		catch(Exception e)
		{
	    	HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidQcReportAction()-"+e.toString());
	    	e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-isValidQcReportAction-batchObjectId="+batchObjectId + " isValid="+isValid);

		return isValid;
	}

    public static boolean isBatchOnHold(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isOnHold = false;
        HelperClass.porticoOutput(0, "QcHelperClass-isBatchOnHold-Started for Batch_ID="+ batchObjectId);
        try
        {
			String batchOnHoldString = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.ISBATCHONHOLD))
			{
		        batchOnHoldString = (String)addlnInfo.get(HelperClassConstants.ISBATCHONHOLD);
			}
            if(batchOnHoldString == null || batchOnHoldString.equals(""))
            {
				HelperClass.porticoOutput(0, "QcHelperClass-isBatchOnHold(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				isOnHold = HelperClass.isBatchOnHold(currentSession, batchObjectId);
			}
			else
			{
				isOnHold = batchOnHoldString.equals("true");
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-isBatchOnHold()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        HelperClass.porticoOutput(0, "QcHelperClass-isBatchOnHold:Batch_ID="+ batchObjectId + " isOnHold="+isOnHold);

        return isOnHold;
    }

    public static String getStatusForBatchObject(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        String batchStatus = "";
        HelperClass.porticoOutput(0, "QcHelperClass-getStatusForBatchObject-Started for Batch_ID="+ batchObjectId);
        try
        {
			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
				HelperClass.porticoOutput(0, "QcHelperClass-getStatusForBatchObject(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				if(batchObjectId != null)
				{
			    	batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchObjectId);
			    }
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getStatusForBatchObject()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        HelperClass.porticoOutput(0, "QcHelperClass-getStatusForBatchObject:Batch_ID="+ batchObjectId + " batchStatus="+batchStatus);

        return batchStatus;
    }

    public static String getBatchPerformer(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        String batchPerformer = "";
        HelperClass.porticoOutput(0, "QcHelperClass-getBatchPerformer-Started for Batch_ID="+ batchObjectId);
        try
        {
			// Batch Performer may not be populated at times, ie during 'PROBLEM', 'QC' etc., so check if
			// if was found in the cache key, if not then go to storage
			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHPERFORMER))
			{
                batchPerformer = (String)addlnInfo.get(HelperClassConstants.BATCHPERFORMER);
			}
			else
			{
				HelperClass.porticoOutput(0, "QcHelperClass-getBatchPerformer(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				if(batchObjectId != null)
				{
			    	batchPerformer = HelperClass.getBatchPerformer(currentSession, batchObjectId);
			    }
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getBatchPerformer()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        HelperClass.porticoOutput(0, "QcHelperClass-getBatchPerformer:Batch_ID="+ batchObjectId + " batchPerformer="+batchPerformer);

        return batchPerformer;
    }

    public static boolean getIsActionTakenForBatch(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isActionTaken = false;
        HelperClass.porticoOutput(0, "QcHelperClass-getIsActionTakenForBatch-Started for Batch_ID="+ batchObjectId);
        try
        {
			String isActionTakenString = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHUSERACTIONTAKEN))
			{
		        isActionTakenString = (String)addlnInfo.get(HelperClassConstants.BATCHUSERACTIONTAKEN);
			}
            if(isActionTakenString == null || isActionTakenString.equals(""))
            {
				HelperClass.porticoOutput(0, "QcHelperClass-getIsActionTakenForBatch(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				isActionTaken = QcHelperClass.getIsActionTakenForBatch(currentSession, batchObjectId);
			}
			else
			{
				isActionTaken = isActionTakenString.equals("true");
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getIsActionTakenForBatch()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        HelperClass.porticoOutput(0, "QcHelperClass-getIsActionTakenForBatch:Batch_ID="+ batchObjectId + " isActionTaken="+isActionTaken);

        return isActionTaken;
    }

    public static String getReentryActivityForBatch(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        String batchReentryActivity = "";
        HelperClass.porticoOutput(0, "QcHelperClass-getReentryActivityForBatch-Started for Batch_ID="+ batchObjectId);
        try
        {
			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHREENTRYACTIVITY))
			{
                batchReentryActivity = (String)addlnInfo.get(HelperClassConstants.BATCHREENTRYACTIVITY);
			}
			else
			{
                // Note: batchReentryActivity = "" is a valid one for initialize so, check for null only
				HelperClass.porticoOutput(0, "QcHelperClass-getReentryActivityForBatch(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				if(batchObjectId != null)
				{
					// Later make this 'From Storage' as pure sysobject-getString() based implementation
			    	batchReentryActivity = QcHelperClass.getReentryActivityForBatch(currentSession, batchObjectId);
			    }
		    }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getReentryActivityForBatch()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        HelperClass.porticoOutput(0, "QcHelperClass-getReentryActivityForBatch:Batch_ID="+ batchObjectId + " batchReentryActivity="+batchReentryActivity);

        return batchReentryActivity;
	}

// ---------------------PRECONDITIONS for QC Actions - End----------------------------------



// ----------------------------------POST PROCESSING for QC Actions - Start----------------------------------
// CONPREP-2351, PMD2.0, events, modified to pass the 'source'
	public static boolean postProcessingForReplace(IDfSession currentSession, String batchObjectId, String suStateId, String newRawUnitId, ArrayList userMsgId, String reasonDesc, String source)
	{
		String contextId = suStateId;
		boolean isSuccessful = false;
   		boolean isSuccessfulBackendAction = false;
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+suStateId + ",newRawUnitId="+newRawUnitId + ",contextId=" + contextId + ",reasonDesc=" + reasonDesc + ",source="+source);

		try
		{
            // Post action-Workflow activity relevant to this action to be called
            // Plug backend code here
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-Start backend call");
            ActionTool actionTool = null;
            try
            {
                actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
                actionTool.flush();
                String newSUStateId = actionTool.addRepairedFile(DBHelperClass.getRawUnitAccessionIdFromRawUnitId(newRawUnitId), suStateId, contextId, reasonDesc, source); // "AddFile");
                isSuccessfulBackendAction = true;
		    }
		    catch(Exception e)
		    {
    	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForReplace="+e.toString());
    	        e.printStackTrace();
			}
			finally
			{
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
				try
				{
                    if(actionTool != null)
                    {
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-call-Before flush");
				    	actionTool.flush();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-call-After flush");
						actionTool.clearSessionContext();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-call-After clearSessionContext");
				    }
			    }
			    catch(Exception eflush)
			    {
					HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
					eflush.printStackTrace();
				}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForReplace="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-Call-finally-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			if(isSuccessfulBackendAction == true)
			{
        		// processUserMessage(currentSession, userMsgId, batchObjectId, "AddFile");
                setCommonBatchInfoForQC(currentSession, batchObjectId, "AddFile");
                isSuccessful = true;
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReplace-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

	public static boolean postProcessingForAddNewFile(IDfSession currentSession, String batchObjectId, String[] newRawUnitId, ArrayList userMsgId, String reasonDesc, String contextId)
	{
		boolean isSuccessful = false;
		boolean isSuccessfulBackendAction = false;
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-Call-Started-batchObjectId="+batchObjectId + ",contextId=" + contextId + ",reasonDesc=" + reasonDesc);
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-Start backend call");
            ActionTool actionTool = null;
            try
            {
                String tContextId = contextId;
                if(HelperClass.getObjectType(currentSession, tContextId).equals(DBHelperClass.BATCH_TYPE))
                {
					tContextId = null;
    				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-(Batch null context)-tContextId="+tContextId);
				}
                actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
                actionTool.flush();
                for(int indx=0; indx < newRawUnitId.length; indx++)
                {
					HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-Send-rawunitId="+newRawUnitId[indx]);
                    String newSUStateId = actionTool.addNewFile(DBHelperClass.getRawUnitAccessionIdFromRawUnitId(newRawUnitId[indx]), tContextId, reasonDesc); // "AddNewFile");
			    }
                isSuccessfulBackendAction = true;
		    }
		    catch(Exception e)
		    {
    	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForAddNewFile="+e.toString());
    	        e.printStackTrace();
			}
			finally
			{
				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
				try
				{
                    if(actionTool != null)
                    {
				    	actionTool.flush();
				    	actionTool.clearSessionContext();
				    }
			    }
			    catch(Exception eflush)
			    {
					HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
					eflush.printStackTrace();
				}
			}

		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForAddNewFile="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-Call-finally-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			if(isSuccessfulBackendAction == true)
			{
    	    	// processUserMessage(currentSession, userMsgId, batchObjectId, "AddNewFile");
    	    	setCommonBatchInfoForQC(currentSession, batchObjectId, "AddNewFile");
                isSuccessful = true;
		    }
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForAddNewFile-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

	public static boolean postProcessingForClaim(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-Call-Started-batchObjectId="+batchObjectId);

        boolean isSuccessful = false;
		ActionTool actionTool = null;

		try
		{
            try
    		{
                // Call the backend method
                actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
                actionTool.flush();
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-Start Call-getWorkItem()");
                String workItemId = getWorkItem(currentSession, batchObjectId); // actionTool.getWorkItem();
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-End Call-getWorkItem()-workItemId="+workItemId);
                if(workItemId != null && !workItemId.equals(""))
                {
                    IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-Before-Call-workItem-acquire");
    				iDfWorkitem.acquire();
        		    isSuccessful = true;
        		    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-workitem Acquired Successfully");
    		    }
    		}
    		catch(Exception eAcquire)
    		{
				// Possibly some one has already acquired the workitem related to that Batch
				isSuccessful = false;
    	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForClaim="+eAcquire.toString());
    	        // eAcquire.printStackTrace(); Ignore
    		}
    		finally
    		{
    			try
    			{
                    if(actionTool != null)
                    {
    			    	actionTool.flush();
    			    	actionTool.clearSessionContext();
    			    }
    		    }
    		    catch(Exception eflush)
    		    {
					HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
					eflush.printStackTrace();
    			}
			}

            if(isSuccessful == true)
            {
		        String batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchObjectId);
			    String newStatus = "";
		        if(batchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
		           batchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
		        {
			    	newStatus = HelperClassConstants.RESOLVING_PROBLEM;
			    }
			    else if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT))
			    {
			    	newStatus = HelperClassConstants.INSPECTING;
			    }
			    if(newStatus != null && !newStatus.equals(""))
			    {
                    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));

                    String currentReentryActivityOnBatch = iDfSysObject.getString("p_reentry_activity");
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-currentReentryActivityOnBatch="+currentReentryActivityOnBatch);

                    String loginUserName = currentSession.getLoginUserName();
                    iDfSysObject.setString("p_performer", loginUserName);
                    iDfSysObject.setString("p_performer_for_display", loginUserName);
                    iDfSysObject.setString(HelperClassConstants.BATCH_STATE, newStatus);
                    iDfSysObject.save();
			    }
			    else
			    {
			        isSuccessful = false;
			        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-newStatus is not Set-isSuccessful="+isSuccessful);
			    }
		    }
		}
		catch(Exception e)
		{
			isSuccessful = false;
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForClaim="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForClaim-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

	// postProcessingForContinueAction
	public static boolean postProcessingForContinueAction(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction-Call-Started-batchObjectId="+batchObjectId);
		// Do some preprocessing
		boolean isSuccessful = false;
		String existingReentryPoint = "";
		String nextReentryPoint = "";
		IDfSysObject iDfSysBatchObject = null;

		try
		{
// RANGA Dec,30,2005, Workflow will continue starting from the existing reentry activity on the Batch
// that was set by the workflow Or any user Actions, it will not set the reentry to next activity and so forth
// RANGA 11 JAN 2006, this piece of code is put back, but will be filtered out for post-processing activities.

			iDfSysBatchObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
			boolean isUserActionTaken = iDfSysBatchObject.getBoolean("p_user_action_taken");
            existingReentryPoint = iDfSysBatchObject.getString("p_reentry_activity");
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction(Existing Rentry Point)="+existingReentryPoint);

			int indexOfCurrentReentryActivity = getIndexOfRentryPoint(currentSession, batchObjectId, existingReentryPoint);
			int indexOfGenerateRandomSampleActivity = getIndexOfRentryPoint(currentSession, batchObjectId, HelperClass.getReentryPointName("generaterandomsample"));

			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction(indexOfCurrentReentryActivity)="+indexOfCurrentReentryActivity);
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction(indexOfGenerateRandomSampleActivity)="+indexOfGenerateRandomSampleActivity);

			if(indexOfCurrentReentryActivity != -1 &&
			    indexOfGenerateRandomSampleActivity != -1 &&
				indexOfCurrentReentryActivity < indexOfGenerateRandomSampleActivity)
			{
/* These are not needed REL-1_1_8, further 'p_active_warning_count' has been removed
				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction check logic for setting nextReentryPoint");
                if(isUserActionTaken == false)
                {
			    	int currentWarningCount = -1;
                    ArrayList alistIn = new ArrayList();
                    alistIn.add("p_active_warning_count");
                    Hashtable alistOut = DBHelperClass.getBatchAttributes(batchObjectId, alistIn);

                    if(alistOut != null && alistOut.containsKey("p_active_warning_count"))
                    {
						Object obj = alistOut.get("p_active_warning_count");
						if(obj != null)
						{
						    currentWarningCount = Integer.parseInt((String)obj);
					    }
					}
			        if(currentWarningCount == 0)
			        {
                        if(hasActiveFatalErrors(currentSession, batchObjectId) == false)
                        {
			    			nextReentryPoint = getNextRentryPointForBatch(currentSession, batchObjectId, existingReentryPoint);
			    			if(nextReentryPoint != null && !nextReentryPoint.equals(""))
			    			{
			    				iDfSysBatchObject.setString("p_reentry_activity", nextReentryPoint);
			    				iDfSysBatchObject.save();
	                            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction(New(Next) Rentry Point)="+nextReentryPoint);
			    			}
			    		}
			        }
			    }
*/
		    }
		    else
		    {
				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction DO NOT check logic for setting nextReentryPoint");
			}
    		isSuccessful = postProcessingForContinueProcessing(currentSession, batchObjectId, false);
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForContinueAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction-Call-finally-isSuccessful="+isSuccessful);
            if(isSuccessful == false)
            {
				// Reset back the reentry point in case of failure
				try
				{
				    if(iDfSysBatchObject != null && nextReentryPoint != null && !nextReentryPoint.equals(""))
				    {
				    	iDfSysBatchObject.setString("p_reentry_activity", existingReentryPoint);
				    	iDfSysBatchObject.save();
                        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction(Reset-Rentry Point)="+existingReentryPoint);
				    }
			    }
			    catch(Exception e)
			    {
        	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForContinueAction(Reset-Rentry Point)="+e.toString());
        	        e.printStackTrace();
				}
				finally
				{
					HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueAction-Call-finally-(Reset-Rentry Point)="+existingReentryPoint);
				}
			}
		}

		return isSuccessful;
	}

/*
	public static boolean hasActiveFatalErrors(IDfSession currentSession, String batchObjectId)
	{
		boolean isValid = false;

        IDfCollection tIDfCollection = null;

        try
		{
            String attrNames = "r_object_id, p_severity";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("usermessage_object") +
                               " where FOLDER(ID('" +
                               batchObjectId +
                               "'), DESCEND) and p_action_taken=false and p_severity=2";
            HelperClass.porticoOutput(0, "QcHelperClass-hasActiveFatalErrors()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					isValid = true;
					break;
				}
   	    	}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-hasActiveFatalErrors="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-hasActiveFatalErrors CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-hasActiveFatalErrors-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-hasActiveFatalErrors-Call-finally-isValid="+isValid);
		}

		return isValid;
	}

*/

	public static boolean hasActiveFatalErrors(IDfSession currentSession, String batchObjectId)
	{
		return DBHelperClass.hasActiveFatalErrors(batchObjectId);
	}

	public static String getNextRentryPointForBatch(IDfSession currentSession, String batchObjectId, String currentReentryPoint)
	{
		String nextReentryPoint = "";

		ArrayList workflowActivityList = getOrderedWorkflowActivityListFromSession(currentSession, batchObjectId);// AppSessionContext.getWorkflowActivityListUI();
		if(workflowActivityList != null && workflowActivityList.size() > 0)
		{
	    	int current_act_index = workflowActivityList.indexOf(currentReentryPoint);
	    	if(current_act_index != -1)
	    	{
    	    	int next_act_indx = current_act_index + 1;
    	    	if(next_act_indx < workflowActivityList.size())
    	    	{
    				nextReentryPoint = (String)workflowActivityList.get(next_act_indx);
    			}
		    }
	    }

		return nextReentryPoint;
	}

	public static int getIndexOfRentryPoint(IDfSession currentSession, String batchObjectId, String reentryPoint)
	{
		int current_act_index = -1;

		ArrayList workflowActivityList = getOrderedWorkflowActivityListFromSession(currentSession, batchObjectId);// AppSessionContext.getWorkflowActivityListUI();
		if(workflowActivityList != null && workflowActivityList.size() > 0)
		{
	    	current_act_index = workflowActivityList.indexOf(reentryPoint);
	    }

		return current_act_index;
	}

	public static boolean postProcessingForContinueProcessing(IDfSession currentSession, String batchObjectId, boolean doAcquire)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Call-Started-batchObjectId,doAcquire="+batchObjectId+","+doAcquire);
   		String contextId = "";
        boolean isSuccessful = false;
        ActionTool actionTool = null;
		try
		{
            // Call the backend method
            String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId);
            contextId = batchAccessionId;
            actionTool = new ActionTool(currentSession, batchAccessionId);
            actionTool.flush();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Start Call-processPseudoActionMsgs()");
            actionTool.processPseudoActionMsgs();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-End Call-processPseudoActionMsgs()");
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Start Call-getWorkItem()");
            String workItemId = getWorkItem(currentSession, batchObjectId); // actionTool.getWorkItem();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
                try
                {
					if(doAcquire == true)
					{
                        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Before-Call-acquire");
						iDfWorkitem.acquire();
                        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-After-Call-acquire");
					}
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Before-Call-complete");
                    iDfWorkitem.complete();
            		isSuccessful = true;
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-workitem-complete-Successful");
			    }
			    catch(Exception e)
			    {
					HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForContinueProcessing-During-Call-complete-Exception="+e.toString());
					// e.printStackTrace(); Ignore
				}
				finally
				{
			    }
                //Reset the batch object attribute(s) after 'complete' was all successful
                if(isSuccessful == true)
                {
        	    	resetCommonBatchInfoPostContinueProcessing(currentSession, batchObjectId);
			    }
		    }
		    else
		    {
                HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForContinueProcessing-workItem is NULL");
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForContinueProcessing="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForContinueProcessing-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}

	public static boolean postProcessingForStartAllOver(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForStartAllOver-Call-Started-batchObjectId="+batchObjectId);

        boolean isSuccessful = false;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForStartAllOver-Start Call-markObjectsForDelete()");
            boolean isSuccessfulCleanup = true;
			if(isSuccessfulCleanup == true)
			{
                // This will Set the reentryPoint on batch to null or "", so that processing will start from begining
                // resetCommonBatchInfoPreStartAllOver(currentSession, batchObjectId);
        		String reentryActivity = "";
        		String newStatus = HelperClassConstants.AUTO_PROCESSING;
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                String batchStatus = iDfSysObject.getString(HelperClassConstants.BATCH_STATE);

                boolean doAcquire = false;
                if(batchStatus.equals(HelperClassConstants.INSPECTED))
                {
					doAcquire = true;
					// On July,27,2006, as per Vinay set outcome to 5,to move from INSPECTED state to 'STARTALLOVER' with an
					// acquire 'true'
					iDfSysObject.setInt("p_activity_outcome", 5);
				}
                iDfSysObject.setString("p_reentry_activity", reentryActivity);
                iDfSysObject.setString(HelperClassConstants.BATCH_STATE, newStatus);// We set this for the latency of the workflow, even if the workflow takes time
	                                              // to set the status in the activity, the ui action preconditions
	                                              // will disable the button.This happens when moving from INSPECTED to
	                                              // 'Initialize'
                iDfSysObject.save();
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForStartAllOver-Before Call(Internal)-postProcessingForContinueProcessing()");
                isSuccessful = postProcessingForContinueProcessing(currentSession, batchObjectId, doAcquire);
		    }
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForStartAllOver-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForStartAllOver="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForStartAllOver-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

	public static boolean postProcessingForReleaseToArchive(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReleaseToArchive-Call-Started-batchObjectId="+batchObjectId);

        boolean isSuccessful = false;
        int outcomeReleaseToArchive = 0; // Checked with VINAY, 11APR2006, On workflow template if(0) goes to 'Generate CheckSum' ie 'Release To Archive'
        String newStatus = HelperClassConstants.POST_PROCESSING;
		try
		{
	        IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
	        String batchStatus = iDfSysObject.getString(HelperClassConstants.BATCH_STATE);
	        iDfSysObject.setInt("p_activity_outcome", outcomeReleaseToArchive);
	        iDfSysObject.setString(HelperClassConstants.BATCH_STATE, newStatus); // We set this for the latency of the workflow, even if the workflow takes time
	                                           // to set the status in the activity, the ui action preconditions
	                                           // will disable the button.This happens when moving from INSPECTED to
	                                           // 'Generate Checksum'
           	iDfSysObject.save();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReleaseToArchive-Before Call(Internal)-postProcessingForContinueProcessing()");
            boolean doAcquire = batchStatus.equals(HelperClassConstants.INSPECTED);
            isSuccessful = postProcessingForContinueProcessing(currentSession, batchObjectId, doAcquire);// Do 'acquire' before 'complete'
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReleaseToArchive-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForReleaseToArchive="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForReleaseToArchive-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}


/* As per DFC doc -
   Nested transactions are not supported. You cannot begin a new transaction using either the beginTransaction or IDfSession.beginTrans methods once either type of transaction is already started.
*/

	public static boolean postProcessingForExcludeFile(IDfSession currentSession, String batchObjectId, ArrayList suStateIdList, ArrayList userMsgId, String reEntryPoint, String reasonMsg, String contextId)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-Call-Started-batchObjectId="+batchObjectId + ",List suStateId=" + suStateIdList.toString() + ",reason="+reasonMsg + ",contextId=" + contextId);
        ActionTool actionTool = null;
		try
		{
    		String[] suStateIdStrArray = new String[suStateIdList.size()];
    		for(int indx=0; indx < suStateIdList.size(); indx++)
    		{
				suStateIdStrArray[indx] = (String)suStateIdList.get(indx);
			}

    		for(int i=0; i < suStateIdStrArray.length; i++)
    		{
				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-Input suStateIdStrArray="+suStateIdStrArray[i]);
			}

            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            //actionTool.flush();
            // actionTool.excludeFile(suStateId, reasonMsg); // Older one
            actionTool.excludeFiles(suStateIdStrArray, reasonMsg, contextId, "ExcludeFile"); // Latest
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-End backend call");
            printBatchInfo(batchObjectId);
    		// processUserMessage(currentSession, userMsgId, batchObjectId, "ExcludeFile");
            setCommonBatchInfoForQC(currentSession, batchObjectId, "ExcludeFile");
            isSuccessful = true;
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForExcludeFile="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForExcludeFile-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}

	public static boolean postProcessingForChangeProfile(IDfSession currentSession, String batchObjectId, String oldProfileId, String newProfileId, ArrayList userMsgId, String reEntryPoint, String reasonMsg)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Call-Started-batchObjectId="+batchObjectId +
                                     ",oldProfileId="+oldProfileId +
                                     ",newProfileId="+newProfileId+
                                     ",reason="+reasonMsg);
        ActionTool actionTool = null;
		try
		{
			boolean isDifferent = checkIfDifferentWfTemplates(oldProfileId, newProfileId);
			setBatchProfileId(currentSession, batchObjectId, newProfileId);
			if(isDifferent == true)
			{
				// Set the reentry point to "", though the Batch status is set to 'AutoProcessing', 'runnow' may set the Batch status to 'queued',
				// depending on the system load
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                iDfSysObject.setString("p_reentry_activity", "");
                iDfSysObject.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.AUTO_PROCESSING);// We set this for the latency of the workflow, even if the workflow takes time
	                                              // to set the status in the activity, the ui action preconditions
	                                              // will disable the button accordingly
                iDfSysObject.save();
			    String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId);
			    // As per Roland pass the batchAccessionId instead of Documentum's batchId
                actionTool = new ActionTool(currentSession, batchAccessionId);
                actionTool.flush();
			    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Start-actionTool-deleteWorkflows");
			    actionTool.deleteWorkflows();
			    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-End-actionTool-deleteWorkflows");
			    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Start-actionTool-runNow");
			    // Modified on 05FEB2007 due to 'WorkflowScheduler' changes(not a static method, since the
			    //          'WorkflowScheduler' process may go down after doing its job, so have to instantiate
			    //          all the time before calling the relevant methods.
			    actionTool.runNow(newProfileId);
			    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-End-actionTool-runNow");
			    isSuccessful = true;
			    // This resets the user action taken,performer etc.
                resetCommonBatchInfoPostContinueProcessing(currentSession, batchObjectId);
		    }
		    else
		    {
				// No change in the the wf template(s) due to the change in profile, continue the normal way
                isSuccessful = postProcessingForStartAllOver(currentSession, batchObjectId);
		    }
/*
            if(isSuccessful == false)
            {
				// Reset back to the old profile, since backend process failed.
				HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Reset back to old profile");
				setBatchProfileId(currentSession, batchObjectId, oldProfileId);
			}
*/
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForChangeProfile="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForChangeProfile-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}

	public static boolean postProcessingForRenameFile(IDfSession currentSession, String batchObjectId, String suStateId, String newName, ArrayList userMsgId, String reEntryPoint, String reasonMsg)
	{
		String contextId = suStateId;
		boolean isSuccessful = false;
		ActionTool actionTool = null;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+suStateId + ",newName=" + newName + ",reason="+reasonMsg + ",contextId=" + contextId);
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-Start frontend call");

            // begin conprep-1962 insert [no more need for ActionTool]
            boolean isFrontEndCallSuccessful = true;

            //String batchId = DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId);
			String batchId = "";
			String performer = "";
			ArrayList alistIn = new ArrayList();
			alistIn.add("p_accession_id");
			alistIn.add("p_performer");
			Hashtable alistOut = DBHelperClass.getBatchAttributes(batchObjectId, alistIn);
			if(alistOut != null && alistOut.size() > 0)
			{
                if(alistOut.containsKey("p_accession_id")) batchId = (String)alistOut.get("p_accession_id");
                if(alistOut.containsKey("p_performer")) performer = (String)alistOut.get("p_performer");
			}
            if (batchId != null) {
            	HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-batchId="+batchId);
            	if (RenameUtil.getSuRename(batchId, suStateId)) {
            		HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-performing updateSuRename()");
            		RenameUtil.updateSuRename(batchId, suStateId, newName, performer);
            	} else {
            		HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-performing createSuRename()");
            		RenameUtil.createSuRename(batchId, suStateId, newName, performer);
            	}
            	actionTool = new ActionTool(currentSession, batchId);
            	actionTool.flush();
            	if (contextId != null) actionTool.processNonPseudoActionMsgs(contextId, "RenameFile");
            	actionTool.flush();
            	if (contextId != null) actionTool.createRenameEvent(contextId, newName, reasonMsg);
            	actionTool.flush();

            	IDfSysObject sysObjBatch = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
            	if(sysObjBatch != null) {
            		sysObjBatch.setString("p_reentry_activity", "Initialize Batch");
            		sysObjBatch.save();
            	}
            } else {
            	HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForRenameFile-  batchId is null");
            	isFrontEndCallSuccessful = false;
            }
            // end conprep-1962 insert

            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-End frontend call-isFrontEndCallSuccessful="+isFrontEndCallSuccessful);
            if(isFrontEndCallSuccessful == true)
            {
                setCommonBatchInfoForQC(currentSession, batchObjectId, "RenameFile");
                isSuccessful = true;
		    }
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForRenameFile="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForRenameFile-Call-finally-isSuccessful="+isSuccessful);
            try
            {
				if(actionTool != null)
				{
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
			}
            catch (Exception e)
            {
				HelperClass.porticoOutput(1, "Exception in flush -"+e.toString());
				e.printStackTrace();
			}
		}

		return isSuccessful;
	}

	public static boolean postProcessingForResolveIdentityConflict(IDfSession currentSession, String batchObjectId, String fuStateId, String fuTypeString, ArrayList userMsgId, String reEntryPoint, String reasonMsg)
	{
		String contextId = fuStateId;
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForResolveIdentityConflict-Call-Started-batchObjectId="+batchObjectId +
                                  ",fuStateId="+fuStateId +
                                  ",fuTypeString="+fuTypeString +
                                  ",reason="+reasonMsg +
                                  ",contextId=" + contextId);
        ActionTool actionTool = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForResolveIdentityConflict-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            actionTool.setFuType(fuStateId, fuTypeString, contextId, "ResolveIdentityConflict");
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForResolveIdentityConflict-End backend call");
  	    	// processUserMessage(currentSession, userMsgId, batchObjectId, "ResolveIdentityConflict");
            setCommonBatchInfoForQC(currentSession, batchObjectId, "ResolveIdentityConflict");
            isSuccessful = true;
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForResolveIdentityConflict-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForResolveIdentityConflict="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForResolveIdentityConflict-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}

// ----------------------------------POST PROCESSING for QC Actions - End----------------------------------

    public static void setBatchProfileId(IDfSession currentSession, String batchObjectId, String newProfileId)
    {
		try
		{
		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
		    iDfSysObject.setString("p_profile_id", newProfileId);
		    iDfSysObject.save();
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-setBatchProfileId="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}
	}

// Accepts only 1 message in the userMsgId, this method is called only from the QC actions, not from outside
/*
    public static void processUserMessage(IDfSession currentSession, ArrayList userMsgId, String batchObjectId, String qcAction)
    {
        HelperClass.porticoOutput(0, "QcHelperClass-processUserMessage-Call-Started");
		try
		{
			if(userMsgId != null && userMsgId.size() > 0)
			{
				// Process only the first message for now
                String msgId = (String)userMsgId.get(0);
                ArrayList singleUserMsgList = new ArrayList();
                singleUserMsgList.add(msgId);
                setUserMessages(currentSession, singleUserMsgList, batchObjectId, qcAction);
                // overhead, but for now okay
   	    	    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(msgId));
       		    String severity = iDfSysObject.getString("p_severity");
   		        if(severity.equals("2")) // "2" implies a 'fatal' msg
   		        {
            		String localDesc = "linkage";
                    ArrayList warningMsgList = new ArrayList(); // only warning
                    ArrayList otherMsgList = new ArrayList(); // fatal, info etc.
                    populateAssociatedActiveUserMessages(currentSession, msgId, batchObjectId, warningMsgList, otherMsgList);
                    if(warningMsgList != null && warningMsgList.size() > 0)
                    {
                        clearWarningMessages(currentSession, warningMsgList, batchObjectId, localDesc);
				    }
                    if(otherMsgList != null && otherMsgList.size() > 0)
                    {
                        setUserMessages(currentSession, otherMsgList, batchObjectId, localDesc);
				    }
			    }
	        }
            HelperClass.porticoOutput(0, "QcHelperClass-processUserMessage-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-processUserMessage="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-processUserMessage-Call-finally");
		}
	}
*/
/*
	public static void populateAssociatedActiveUserMessages(IDfSession currentSession,
	                                                        String msgId,
	                                                        String batchObjectId,
	                                                        ArrayList retWarningMsgList,
	                                                        ArrayList retOtherMsgList)
    {
        IDfCollection tIDfCollection = null;

//select * from p_user_message where p_context_id IN (SELECT p_context_id FROM p_user_message where r_object_id='090152d48001b31e') AND p_action_taken=FALSE

        try
		{
            String attrNames = "r_object_id, p_severity";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("usermessage_object") +
                               " where p_context_id IN " +
                               " (SELECT p_context_id FROM " +
                               HelperClass.getInternalObjectType("usermessage_object") +
                               " where r_object_id=" +
                               "'" + msgId + "'" + ")" +
                               " AND p_action_taken=FALSE";

            HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					// p_severity "1" implies warning
					String severityString = tIDfCollection.getString("p_severity");
					HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages()-outside-severityString="+severityString);
					if(severityString.equals("1"))
					{
    					HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages()-warning-severityString="+severityString);
					    retWarningMsgList.add(tIDfCollection.getString("r_object_id"));
				    }
				    else
				    {
    					HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages()-other-severityString="+severityString);
					    retOtherMsgList.add(tIDfCollection.getString("r_object_id"));
					}
				}
   	    	}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-populateAssociatedActiveUserMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-populateAssociatedActiveUserMessages-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-populateAssociatedActiveUserMessages-Call-finally");
		}
	}
*/

// RANGA Start from here 23AUG2006
    // Object based - switched to this, to avoid opening too many collection objects
// Shared method
// This method is mainly to just update 1 msg
/*
	public static void setUserMessages(IDfSession currentSession, ArrayList userMsgId, String batchObjectId, String qcAction)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-setUserMessages-Call-Started");
		try
		{
			if(userMsgId != null && userMsgId.size() > 0)
			{
				int decrementCount = 0;
				for(int indx=0; indx < userMsgId.size(); indx++)
				{
        		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId((String)userMsgId.get(indx)));
        		    String severity = iDfSysObject.getString("p_severity");
        		    iDfSysObject.setBoolean("p_action_taken", true);
        		    iDfSysObject.setString("p_action_desc", qcAction);
        		    iDfSysObject.save();
        		    if(severity != null)
        		    {
        		        if(severity.equals("1")) // "1" implies a 'warning' msg
        		        {
					    	// For multiple messages this could be efficient by capturing the message count
					    	// and in one shot decrementing the 'WarningCount' on the batchOperationsManager by these many
					    	decrementCount++;
					    }
					}
				}
       	        // decrement the warning counter on the batch object(New datamodel)
       			decrementWarningCount(currentSession, batchObjectId, decrementCount);
	        }
	        else
	        {
				HelperClass.porticoOutput(1, "QcHelperClass-setUserMessages- NO USER MESSAGES passed");
			}
            HelperClass.porticoOutput(0, "QcHelperClass-setUserMessages-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-setUserMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-setUserMessages-Call-finally");
		}
	}
*/

	public static boolean setUserMessage(IDfSession currentSession, String msgId, String batchObjectId, String qcAction, int severity)
	{
		return DBHelperClass.setUserMessage(msgId, batchObjectId, qcAction, severity);
	}


// Shared method used by QC report
    // Note: userMsgId cannot have duplicates, strictly warning messages ONLY

/*  Not used, not efficient
	public static void clearWarningMessages(IDfSession currentSession, ArrayList userMsgId, String batchObjectId, String qcAction)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages-Call-Started");
        IDfCollection tIDfCollection = null;
		try
		{
			if(userMsgId != null && userMsgId.size() > 0)
			{
				int decrementCount = userMsgId.size();
				String inClauseList = "";
				for(int indx=0; indx < userMsgId.size(); indx++)
				{
					if(indx == 0)
					{
				    	inClauseList = "'" + (String)userMsgId.get(indx) + "'";
				    }
				    else
				    {
						inClauseList = inClauseList + ","+ "'" + (String)userMsgId.get(indx) + "'";
					}
				}
                String objectName = HelperClass.getInternalObjectType("usermessage_object");
                DfQuery dfquery = new DfQuery();
                String dqlString = "UPDATE " + objectName + " OBJECTS " +
                                   " SET p_action_taken=TRUE" + "," +
                                   " SET p_action_desc=" + "'" + qcAction + "'" +
                                   " where r_object_id IN " +
                                   "(" +
                                   inClauseList +
                                   ")";

                HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages()-dqlString="+dqlString);
     			dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_EXEC_QUERY);
                if(tIDfCollection != null)
                {
    				//HelperClass.porticoOutput(0, "clearWarningMessages dump");
    				//HelperClass.porticoOutput(0, tIDfCollection.dump());
       		    	tIDfCollection.close();
   	    	    }

        	    String workpadId = getWorkPadId(currentSession, batchObjectId);
        	    if(workpadId != null && !workpadId.equals(""))
        	    {
            	    // decrement the warning counter on the batchOperationsManager
             		decrementWarningCount(currentSession, workpadId, decrementCount);
        		}
        		HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages-workpadId="+workpadId);
                HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages-Call-Ended");
	        }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearWarningMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearWarningMessages-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-clearWarningMessages-Call-finally");
		}
	}
*/
    public static void clearWarningMessages(IDfSession currentSession, String batchObjectId, String qcAction)
    {
		boolean isSuccessful = DBHelperClass.clearAllWarningMessages(batchObjectId, qcAction);
	}

/* Not required any more
    public static String getWorkPadId(IDfSession currentSession, String batchObjectId)
    {
		String workpadId = "";
		IDfCollection tIDfCollection = null;
		try
		{
            String attrNames = "r_object_id";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("workpad_batch") +
                               " where FOLDER(ID(" + "'"+batchObjectId+"'" +
                               "))";

            HelperClass.porticoOutput(0, "QcHelperClass-getWorkPadId()-dqlString="+dqlString);

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					workpadId = tIDfCollection.getString("r_object_id");
					break;
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-getWorkPadId():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-getWorkPadId() CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-getWorkPadId()-close" + e.toString());
				e.printStackTrace();
			}
		}

		return workpadId;
	}
*/
/*
	public static void decrementWarningCount(IDfSession currentSession, String workpadId, int decrementCount)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-decrementWarningCount-Call-Started");

		try
		{
   		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(workpadId));
   		    int currentWarningThreshold = iDfSysObject.getInt("p_active_warning_count");
   		    int newWarningThreshold = currentWarningThreshold - decrementCount;
            HelperClass.porticoOutput(0, "QcHelperClass-decrementWarningCount-decrementCount="+decrementCount);
            HelperClass.porticoOutput(0, "QcHelperClass-decrementWarningCount-currentWarningThreshold="+currentWarningThreshold);
            HelperClass.porticoOutput(0, "QcHelperClass-decrementWarningCount-newWarningThreshold="+newWarningThreshold);
            if(newWarningThreshold < 0)
            {
			    newWarningThreshold = 0;
			    HelperClass.porticoOutput(1, "QcHelperClass-WARNING-decrementWarningCount-newWarningThreshold is NEGATIVE, resetting to 0");
			}
   		    iDfSysObject.setInt("p_active_warning_count", newWarningThreshold);
   		    iDfSysObject.save();
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-decrementWarningCount="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-decrementWarningCount-Call-finally");
		}
	}
*/

/*
	public static boolean decrementWarningCount(IDfSession currentSession, String batchObjectId, int decrementCount)
	{
		return DBHelperClass.decrementWarningCount(batchObjectId, decrementCount);
	}
*/

    // For a post QC Action setting on a BATCH
// Shared method
	public static void setCommonBatchInfoForQC(IDfSession currentSession, String batchObjectId, String qcAction)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-setCommonBatchInfoForQC-Call-Started");
		try
		{
			String reentryActivity = getReentryPointToSet(currentSession, batchObjectId, qcAction);
			HelperClass.porticoOutput(0, "QcHelperClass-setCommonBatchInfoForQC-NewReentryActivity="+reentryActivity);
            IDfSysObject batch = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
            batch.setBoolean("p_user_action_taken", true);
            batch.setString("p_reentry_activity", reentryActivity);
            batch.save();
            HelperClass.porticoOutput(0, "QcHelperClass-setCommonBatchInfoForQC-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-setCommonBatchInfoForQC="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-setCommonBatchInfoForQC-Call-finally");
		}
	}

    // For a post AP setting on a BATCH
	public static void resetCommonBatchInfoPostContinueProcessing(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPostContinueProcessing-Call-Started");
		IDfCollection tIDfCollection = null;
		try
		{
			String noValue = "";
            IDfSysObject batch = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
            batch.setBoolean("p_user_action_taken", false);
            batch.setString("p_performer", noValue);
            batch.setString("p_performer_for_display", "");
            batch.save();

            HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPostContinueProcessing-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-resetCommonBatchInfoPostContinueProcessing="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPostContinueProcessing CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-resetCommonBatchInfoPostContinueProcessing-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPostContinueProcessing-Call-finally");
		}
	}

    // Call this method to reset the 'Reentry activity' on batch to null
	public static void resetCommonBatchInfoPreStartAllOver(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPreStartAllOver-Call-Started");
		IDfCollection tIDfCollection = null;
		try
		{
			String reentryActivity = "";
            IDfSysObject batch = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
            batch.setString("p_reentry_activity", reentryActivity);
            batch.save();

            HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPreStartAllOver-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-resetCommonBatchInfoPreStartAllOver="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPreStartAllOver CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-resetCommonBatchInfoPreStartAllOver-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-resetCommonBatchInfoPreStartAllOver-Call-finally");
		}
	}

	public static String getReentryPointToSet(IDfSession currentSession, String batchObjectId, String qcAction)
	{
		// Pick existing Reentrypoint for this batch
		String reentryActivity = getReentryActivityForBatch(currentSession, batchObjectId);
		HelperClass.porticoOutput(0, "QcHelperClass-getReentryPointToSet-existingReentryActivityOnBatch="+reentryActivity);
        if(qcAction != null && !qcAction.equals(""))
        {
		    Hashtable actionReentryPointMappingList = getActionReentryPointMappingListFromSession(currentSession, batchObjectId); // AppSessionContext.getActionReentryPointMappingListUI();
		    String reentryActivityOfCurrentAction = (String)actionReentryPointMappingList.get(qcAction);
		    HelperClass.porticoOutput(0, "QcHelperClass-getReentryPointToSet-reentryActivityOfCurrentAction="+reentryActivityOfCurrentAction);
    		String newReentryActivity = getEarliestWorkflowActivity(currentSession, batchObjectId, reentryActivityOfCurrentAction, reentryActivity);
    		if(newReentryActivity != null && !newReentryActivity.equals(""))
		    {
		    	reentryActivity = newReentryActivity;
		    }
		    else
		    {
				HelperClass.porticoOutput(1, "QcHelperClass-getReentryPointToSet(Warning)-Leaving the rentry activity unchanged="+reentryActivity);
			}
	    }

		return reentryActivity;
	}

	public static String getEarliestWorkflowActivity(IDfSession currentSession, String batchObjectId, String activity1, String activity2)
	{
		String earliestActivity = "";
		ArrayList workflowActivityList = getOrderedWorkflowActivityListFromSession(currentSession, batchObjectId); // AppSessionContext.getWorkflowActivityListUI();
		if(workflowActivityList != null && workflowActivityList.size() > 0)
		{
	    	int act1_index = workflowActivityList.indexOf(activity1);
	    	if(act1_index == -1)
	    	{
				HelperClass.porticoOutput(1, "QcHelperClass-getEarliestWorkflowActivity(Error)-currentAction activity1(UNKNOWN)="+activity1);
			}
	    	int act2_index = workflowActivityList.indexOf(activity2);
	    	if(act2_index == -1)
	    	{
				HelperClass.porticoOutput(1, "QcHelperClass-getEarliestWorkflowActivity(Error)-existingBatch activity2(UNKNOWN)="+activity2);
			}
    		if(act1_index != -1 && act2_index != -1)
	    	{
	    	    earliestActivity = activity1;
	    	    if(act2_index < act1_index)
	    	    {
	    			earliestActivity = activity2;
	    		}
	    	}
	    }

		return earliestActivity;
	}

    public static TreeSet getPossibleFilePathsForAddition1(IDfSession currentSession, String batchId)
    {
	    TreeSet possibleFilePaths = new TreeSet();
	    int findIndx = -1;
	    String unixStringSeparator = "/";

	    // SEE IF Submission view's 'callPatternHandler' can be used
	    // Fire a dql,
	    // get all the distinct paths(removing the filename)
	    // do recursively
	    //    pick one of the distinct path(c:/temp/vol1/issue1/journal1/)
	    //    slice each path(c:/temp/vol1/issue1/)
	    //    slice each path(c:/temp/vol1/)
	    //    slice each path(c:/temp/)
	    //    slice each path(c:/)
	    // done recursion
	    ArrayList submissionFilePaths = HelperClass.getSubmissionViewObjects(currentSession, batchId);
	    if(submissionFilePaths != null && submissionFilePaths.size() > 0)
	    {
			for(int indx=0; indx < submissionFilePaths.size(); indx++)
			{
    		    SubmissionPatternResultItem tItem = (SubmissionPatternResultItem)submissionFilePaths.get(indx);
		        String currentItem = tItem.getThisToken();
		        if((findIndx=currentItem.lastIndexOf(unixStringSeparator)) != -1)
		        {
    		        // remove filename portion
					currentItem = currentItem.substring(0,findIndx);
    			    // See sample below
    			    // currentItem=C:/ranga/temp 1/good.txt
    			    populatePossibleSubPathCombinations(possibleFilePaths, currentItem);
				}
			}
		}
	    if(possibleFilePaths != null && possibleFilePaths.size() > 0)
	    {
            Iterator tIterate = possibleFilePaths.iterator();
            while(tIterate.hasNext())
            {
      			HelperClass.porticoOutput(0, "QcHelperClass-getPossibleFilePathsForAddition1(Result)="+(String)tIterate.next());
       	    }
		}

	    return possibleFilePaths;
	}

	public static void populatePossibleSubPathCombinations(TreeSet resultList, String currentPath)
	{
		String unixStringSeparator = "/";
		String tempPath = currentPath;
		int findIndx = -1;

		if(tempPath != null && !tempPath.equals(""))
		{
			if(!resultList.contains(tempPath))
			{
			    resultList.add(tempPath);
			}
		}

		while(tempPath != null && !tempPath.equals("") && ((findIndx=tempPath.lastIndexOf(unixStringSeparator)) != -1))
		{
            tempPath = tempPath.substring(0,findIndx); // C:/ranga/temp 1
			if(!resultList.contains(tempPath))
			{
			    resultList.add(tempPath);

			}

			// Just in case, to break out
			if(tempPath.lastIndexOf(unixStringSeparator) == -1)
			{
				// This may not be required, since 'while' condition will take care.
				break;
			}
	    }
	}

    // Used by 'AddNewFile', 'Replace' action components
	public static void cleanUp(IDfSession currentSession, ArrayList contentObjectIds)
	{
		HelperClass.porticoOutput(0, "QcHelperClass-start cleanUp");
		try
		{
			boolean isSuccessfulOperation = true;
            IDfDeleteOperation operation = null;
            IDfSysObject sysObj = null;
            if(contentObjectIds != null && contentObjectIds.size() > 0)
            {
                IDfClientX clientx = new DfClientX();
                //create an IDfDeleteOperation object and set parameters
                operation = clientx.getDeleteOperation();
                //other options for setVersionDeletionPolicy: ALL_VERSIONS, UNUSED_VERSIONS, SELECTED_VERSIONS
                operation.setVersionDeletionPolicy(IDfDeleteOperation.ALL_VERSIONS);

    			for(int indx=0; indx < contentObjectIds.size(); indx++)
    			{
    				String itemId = (String)contentObjectIds.get(indx);
                    sysObj = (IDfSysObject)currentSession.getObject(new DfId(itemId));
                    if(sysObj.isVirtualDocument())
                    {
                        IDfVirtualDocument vDoc = sysObj.asVirtualDocument("CURRENT", false);
                        IDfDeleteNode node = (IDfDeleteNode)operation.add(vDoc);
                    }
                    else
                    {
                        IDfDeleteNode node = (IDfDeleteNode)operation.add(sysObj);
                    }
                    HelperClass.porticoOutput(0, "QcHelperClass-cleanUp()-itemId="+itemId);
    		    }
    		    //executeOperation(operation);
    		    isSuccessfulOperation = operation.execute();
    		    HelperClass.porticoOutput(0, "QcHelperClass-cleanUp()-isSuccessfulOperation=" + isSuccessfulOperation);
    	    }

		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-cleanUp="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
    		HelperClass.porticoOutput(0, "QcHelperClass-finally-cleanUp");
		}
	}

	public static String getExistingNameForRename(IDfSession currentSession, String batchObjectId, String objectId)
	{
		String existingFileName = "";

        HelperClass.porticoOutput(0, "QcHelperClass-getExistingNameForRename-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+objectId);
        ActionTool actionTool = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getExistingNameForRename-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            existingFileName = actionTool.getName(objectId);
            HelperClass.porticoOutput(0, "QcHelperClass-getExistingNameForRename-End backend call");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-getExistingNameForRename="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getExistingNameForRename-Call-finally-existingFileName="+existingFileName);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return existingFileName;
	}

// Sorted order
	public static List getFuTypeList()
	{
        List fuTypeList = new ArrayList();

		fuTypeList.add("Text: Marked Up Header");
		fuTypeList.add("Text: Marked Up Full Text");
		fuTypeList.add("Text: Plain");
        fuTypeList.add("Rendition: Page Images");
        fuTypeList.add("Rendition: Multimedia");        
		fuTypeList.add("Rendition: Web");
		fuTypeList.add("Component: Figure Graphic");
		fuTypeList.add("Component: Formula Graphic");
		fuTypeList.add("Component: Table Graphic");
		fuTypeList.add("Component: Other Graphic");
		fuTypeList.add("Component: Media");
		fuTypeList.add("Component: Other");
		fuTypeList.add("Unknown");

        return fuTypeList;
	}

// For the 'Content Tree' view to sort the FU based on FuTypes
	public static String getFuTypeSortKey(String fuType)
	{
		String sortKey = "99"+fuType;

		int findIndex = -1;
		String findIndexPrefix = "";

        List fuTypeList = getFuTypeList();
        if((findIndex = fuTypeList.indexOf(fuType)) != -1)
        {
			findIndexPrefix = ""+findIndex;
			if(findIndex < 10)
			{
				findIndexPrefix = "0"+findIndex;
			}
            sortKey = findIndexPrefix+fuType;
		}

		HelperClass.porticoOutput(0, "QcHelperClass-getFuTypeSortKey-fuType="+fuType + ",sortKey="+sortKey);

		return sortKey;
	}

/* Used by linkfutocu
	public static List getCuStateList(IDfSession currentSession, String batchObjectId)
	{
		ArrayList cuStateList = new ArrayList();

		// This method used for getting the CU States while processing the Metadata List
		HelperClass.populateContentUnitStateInfo(currentSession, batchObjectId, cuStateList);

        return cuStateList;
	}
*/

// Not used any more, we use edit/checkin curated dmd from Documentum directly, since special
// character(certain utf-8) were not rendered properly on a simple text control
/*
	public static String getCuratedDmd(IDfSession currentSession, String batchObjectId, String cuStateId)
	{
		String existingCuratedDmd = "";
        HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmd-Call-Started-batchObjectId="+batchObjectId +
                                  ",cuStateId="+cuStateId);
        ActionTool actionTool = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmd-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            existingCuratedDmd = actionTool.getDescMetadata(cuStateId);
            HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmd-Call-Ended-existingCuratedDmd=");
            HelperClass.porticoOutput(0, existingCuratedDmd);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-getCuratedDmd="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmd-Call-finally");
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return existingCuratedDmd;
	}
*/

// Try removing from 'callprocessviewhandler' the getDisplayName() and use this in Future

/*
	public static String getDisplayName(IDfSession currentSession, String currentObjectId, String itemObjectType)
	{
		String displayName = "";
   		IDfCollection tIDfCollection = null;
   		IDfSysObject currentIDfSysObject = null;

        try
        {
			currentIDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(currentObjectId));
			if(itemObjectType.equals(HelperClass.getInternalObjectType("fu_state")))
			{
				// go to the "fu" object and pick futype
                String attrNames = "r_object_id,p_fu_type";
                DfQuery dfquery = new DfQuery();
                String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("fu_object") +
                                   " where FOLDER(ID(" + "'"+currentObjectId+"'" +
                                   "), DESCEND)";
                HelperClass.porticoOutput(0, "QcHelperClass-getDisplayName()-dqlString="+dqlString);
      			dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
       					displayName = tIDfCollection.getString("p_fu_type");
       					break;
       				}
       			}
       		}
       		else if(itemObjectType.equals(HelperClass.getInternalObjectType("su_state")))
       		{
				displayName = currentIDfSysObject.getString("p_work_filename");
			}
       		else if(itemObjectType.equals(HelperClass.getInternalObjectType("cu_state")))
       		{
				displayName = currentIDfSysObject.getString("p_display_label");
			}

			displayName += "(" + currentIDfSysObject.getObjectName() + ")";
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-getDisplayName():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-getDisplayName CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-getDisplayName-close" + e.toString());
				e.printStackTrace();
			}
		}

		return displayName;
	}
*/

	public static String getDisplayName(IDfSession currentSession, String currentObjectId, String itemObjectType)
	{
		String displayName = "";
   		String objectName = "";

        HelperClass.porticoOutput(0, "QcHelperClass-getDisplayName(Start) currentObjectId,itemObjectType="+currentObjectId+","+itemObjectType);

        try
        {
			ArrayList alistIn = new ArrayList();
			Hashtable alistOut = null;
			if(itemObjectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
			{
    			IDfSysObject currentIDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(currentObjectId));
    			objectName = currentIDfSysObject.getObjectName();
    			displayName = objectName;
		    }
			else if(itemObjectType.equalsIgnoreCase(DBHelperClass.FU_TYPE))
			{
				alistIn.add(DBHelperClass.P_NAME);
				alistIn.add(DBHelperClass.P_FU_TYPE);
                alistOut = DBHelperClass.getObjectAttributes(DBHelperClass.FU_TYPE, currentObjectId, alistIn);

                if(alistOut != null)
                {
                    if(alistOut.containsKey(DBHelperClass.P_NAME))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_NAME);
				    	if(obj != null)
				    	{
				    	    objectName = (String)obj;
				        }
					}
                    if(alistOut.containsKey(DBHelperClass.P_FU_TYPE))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_FU_TYPE);
				    	if(obj != null)
				    	{
				    	    displayName = (String)obj;
				        }
					}
				}
       		}
       		else if(itemObjectType.equalsIgnoreCase(DBHelperClass.SU_TYPE))
       		{
				alistIn.add(DBHelperClass.P_NAME);
				alistIn.add(DBHelperClass.P_WORK_FILENAME);
                alistOut = DBHelperClass.getObjectAttributes(DBHelperClass.SU_TYPE, currentObjectId, alistIn);

                if(alistOut != null)
                {
                    if(alistOut.containsKey(DBHelperClass.P_NAME))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_NAME);
				    	if(obj != null)
				    	{
				    	    objectName = (String)obj;
				        }
					}
                    if(alistOut.containsKey(DBHelperClass.P_WORK_FILENAME))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_WORK_FILENAME);
				    	if(obj != null)
				    	{
				    	    displayName = (String)obj;
				        }
					}
				}
			}
       		else if(itemObjectType.equalsIgnoreCase(DBHelperClass.CU_TYPE))
       		{
				alistIn.add(DBHelperClass.P_NAME);
				alistIn.add(DBHelperClass.P_DISPLAY_LABEL);
                alistOut = DBHelperClass.getObjectAttributes(DBHelperClass.CU_TYPE, currentObjectId, alistIn);

                if(alistOut != null)
                {
                    if(alistOut.containsKey(DBHelperClass.P_NAME))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_NAME);
				    	if(obj != null)
				    	{
				    	    objectName = (String)obj;
				        }
					}
                    if(alistOut.containsKey(DBHelperClass.P_DISPLAY_LABEL))
                    {
				    	Object obj = alistOut.get(DBHelperClass.P_DISPLAY_LABEL);
				    	if(obj != null)
				    	{
				    	    displayName = (String)obj;
				        }
					}
				}
			}

			displayName += "(" + objectName + ")";
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-getDisplayName():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getDisplayName(End) currentObjectId,itemObjectType,displayName="+currentObjectId+","+itemObjectType+","+displayName);

		return displayName;
	}

/*
	public static String getParentFolderId(IDfSession currentSession, String objectId)
	{
		String parentFolderId = "";
        IDfCollection idfcollection = null;

        // Note: For some reason give both r_object_id and i_folder_id
        String dqlString = "SELECT r_object_id, i_folder_id from dm_sysobject where r_object_id=" + "'" + objectId + "'";

        try
        {
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL(dqlString);
            HelperClass.porticoOutput(0, "getParentFolderId:dqlString="+ dqlString);
     		HelperClass.porticoOutput(0, "getParentFolderId OPEN IDfCollection");
            for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
            {
				parentFolderId = idfcollection.getString("i_folder_id");
				break;
            }
		}
		catch(Exception e)
		{
		    HelperClass.porticoOutput(1, "Exception in getParentFolderId()="+e.toString());
		    e.printStackTrace();
		}
		finally
		{
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
				}
           		HelperClass.porticoOutput(0, "getParentFolderId- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
		        HelperClass.porticoOutput(1, "Exception in getParentFolderId()-close="+e.toString());
		        e.printStackTrace();
			}
		}

		return parentFolderId;
	}
*/

	public static List getResolveIdentityConflictFuTypeChoices(IDfSession currentSession, String batchObjectId, String fuStateId)
	{
		List fuTypeChoices = null;

        HelperClass.porticoOutput(0, "QcHelperClass-getResolveIdentityConflictFuTypeChoices-Call-Started-batchObjectId="+batchObjectId + ",fuStateId=" + fuStateId);
        ActionTool actionTool = null;
		try
		{
	        HelperClass.porticoOutput(0, "QcHelperClass-getResolveIdentityConflictFuTypeChoices-Start backend call");
	        actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
	        actionTool.flush();
            fuTypeChoices = actionTool.getFuTypeChoices(fuStateId);
	        HelperClass.porticoOutput(0, "QcHelperClass-getResolveIdentityConflictFuTypeChoices-End backend call");
		}
		catch(Exception e)
		{
		    HelperClass.porticoOutput(1, "Exception in QcHelperClass-getResolveIdentityConflictFuTypeChoices="+e.toString());
		    e.printStackTrace();
		}
		finally
		{
	        HelperClass.porticoOutput(0, "QcHelperClass-getResolveIdentityConflictFuTypeChoices-Call-finally");
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return fuTypeChoices;
	}

	/*public static List getLeadMetadataInfo1(IDfSession currentSession, String batchObjectId, String cuStateId)
	{
		List leadMetaData = null;

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-Call-Started-batchObjectId="+batchObjectId + ",cuStateId=" + cuStateId);
        ActionTool actionTool = null;
		try
		{
	        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-Start backend call");
	        actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
	        actionTool.flush();
            leadMetaData = actionTool.getLeadMetadata(cuStateId);
	        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-End backend call");
		}
		catch(Exception e)
		{
		    HelperClass.porticoOutput(1, "Exception in QcHelperClass-getLeadMetadataInfo="+e.toString());
		    e.printStackTrace();
		}
		finally
		{
	        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-Call-finally");
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return leadMetaData;
	}*/

/* Used in linksutofu
	public static List getFuStateList(IDfSession currentSession, String batchObjectId)
	{
		ArrayList fuStateList = new ArrayList();

		IDfCollection tIDfCollection = null;
		try
		{
            String attrNames = "DISTINCT(r_object_id)";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("fu_state") +
                               " where FOLDER(ID(" + "'"+batchObjectId+"'" +
                               "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-getFuStateList()-dqlString="+dqlString);

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					fuStateList.add(tIDfCollection.getString("r_object_id"));
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-getFuStateList():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-getFuStateList() CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-getFuStateList()-close" + e.toString());
				e.printStackTrace();
			}
		}

		return fuStateList;
	}
*/

/*
    Used in linksutofu,linkfutocu
    public static String getContentModelParentId(IDfSession currentSession, String objectId)
    {
		String contentModelParentId = "";
		HelperClass.porticoOutput(0, "QcHelperClass-getContentModelParentId-Call-Started-objectId="+objectId);

		try
		{
		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
		    String currentType = iDfSysObject.getTypeName();
		    if(currentType.equals(HelperClass.getInternalObjectType("fu_state")))
		    {
   		        contentModelParentId = iDfSysObject.getString("p_cu_state_id");
		    }
		    else if(currentType.equals(HelperClass.getInternalObjectType("su_state")))
		    {
   		        contentModelParentId = iDfSysObject.getString("p_fu_state_id");
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-getContentModelParentId="+e.toString());
			e.printStackTrace();
		}
		finally
		{
		    HelperClass.porticoOutput(0, "QcHelperClass-getContentModelParentId-Call-finally-contentModelParentId="+contentModelParentId);
		}

		return contentModelParentId;
	}
*/
// CONPREP-2351, PMD2.0, events, Change preservation level functionality is being removed
/*
	public static boolean postProcessingForLowerPresLvl(IDfSession currentSession, String batchObjectId, String suStateId, String reasonMsg, ArrayList userMsgId, String reEntryPoint)
	{
		String contextId = suStateId;
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForLowerPresLvl-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+suStateId + ",reason="+reasonMsg + ",contextId=" + contextId);
        ActionTool actionTool = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForLowerPresLvl-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            String user = currentSession.getLoginUserName();
            actionTool.changePreservationLevel(suStateId, user, IPreservationLevel.PRESLVL_BYTE_PRESERVED, reasonMsg, contextId, "LowerPresLvl");
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForLowerPresLvl-End backend call");
            setCommonBatchInfoForQC(currentSession, batchObjectId, "LowerPresLvl");
            isSuccessful = true;
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForLowerPresLvl-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForLowerPresLvl="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForLowerPresLvl-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}
*/

//  Changes made(Below) for manual curation UI
/*
	public static String getCuratedDmdObjectId(IDfSession currentSession, String batchObjectId, String cuStateId)
	{
		String curatedDmdObjectId = "";
        HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmdObjectId-Call-Started-batchObjectId="+batchObjectId +
                                  ",cuStateId="+cuStateId);
		IDfCollection tIDfCollection = null;
		try
		{
			String isCurated  = "TRUE";
            String attrNames = "r_object_id";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + HelperClass.getInternalObjectType("desc_dmd_object") +
                               " where " +
                               " p_is_curated =" + isCurated + " AND " +
                               " FOLDER(ID(" + "'"+cuStateId+"'" + "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmdObjectId()-dqlString="+dqlString);

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					curatedDmdObjectId = tIDfCollection.getString("r_object_id");
					break;
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QcHelperClass-getCuratedDmdObjectId():"+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-getCuratedDmdObjectId() CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-getCuratedDmdObjectId()-close" + e.toString());
				e.printStackTrace();
			}
		}

		return curatedDmdObjectId;
	}
*/

	public static Hashtable getCuratedDmdObjectId(IDfSession currentSession, String batchObjectId, String cuStateId)
	{
		return DBHelperClass.getCuratedDmdObjectId(batchObjectId, cuStateId);
	}

    public static boolean postProcessingForEditCuratedDmdMessages(IDfSession currentSession, String batchObjectId, String cuStateId, ArrayList userMsgId, String reEntryPoint, String reasonMsg, String curatedDMDAccessionId, String newCheckedInDmdObjectId)
    {
		String contextId = cuStateId;
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForEditCuratedDmdMessages-Call-Started-batchObjectId="+batchObjectId +
                                  ",cuStateId="+cuStateId +
                                  ",reason="+reasonMsg +
                                  ",contextId=" + contextId +
                                  ",curatedDMDAccessionId="+ curatedDMDAccessionId +
                                  ",newCheckedInDmdObjectId="+newCheckedInDmdObjectId);
        ActionTool actionTool = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForEditCuratedDmdMessages-Start backend call");
            String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId);
            actionTool = new ActionTool(currentSession, batchAccessionId);
            actionTool.flush();
            actionTool.processNonPseudoActionMsgs(contextId, "EditCuDmd");
            // CONPREP-2351, PMD2.0 events, Added a flush because we may call other methods below
            actionTool.flush();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForEditCuratedDmdMessages-End backend call");
  	    	// processUserMessage(currentSession, userMsgId, batchObjectId, "EditCuDmd");
// As per Suku/Nita - Edit Curated DMD must NOT be treated as a QC Action,
//                    so need not set any batch attributes-p_user_action_taken, p_reentry_activity
// On 31,OCT,2005 Edit Curated DMD was revisited and now has to be treated as a true QC action
// potentially if there is a problem in Automated Curation, the workflow state goes to Problem Resolution
// ie. QC1_IN_PROGRESS, after editing the curated DMD the reentry activity will be set accordingly
// and the action_taken will have to be set to true.
// As per Roland, need not set the reentry activity, but since it is a Trur QC action now, we do set the
// reentry activity as well

            if(DBHelperClass.setCheckedInDmdObjectId(batchAccessionId, cuStateId, curatedDMDAccessionId, newCheckedInDmdObjectId) == true)
            {
                setCommonBatchInfoForQC(currentSession, batchObjectId, "EditCuDmd"); //
		    }

		    actionTool.createEditDescriptiveMetadataEvent(cuStateId, curatedDMDAccessionId, reasonMsg);
		    actionTool.flush();

            isSuccessful = true;
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForEditCuratedDmdMessages-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForEditCuratedDmdMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForEditCuratedDmdMessages-Call-finally-isSuccessful="+isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

		return isSuccessful;
	}

    public static boolean postProcessingForDelegate(IDfSession currentSession,
                                                      String batchObjectId,
                                                      String currentClaimer,
                                                      String delegatedUser,
                                                      String note,
                                                      String msgHeader)
    {
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Call-Started-batchObjectId="+batchObjectId +
                                  ",currentClaimer="+currentClaimer +
                                  ",delegatedUser="+delegatedUser +
                                  ",note=" + note);
        IDfCollection tIDfCollection = null;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Start Call-getWorkItem()");
            String workItemId = getWorkItem(currentSession, batchObjectId); // actionTool.getWorkItem();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));

                tIDfCollection = iDfWorkitem.getPackages("");
                String packageId = null;
                if (tIDfCollection != null && tIDfCollection.next())
                {
                    packageId = tIDfCollection.getString("r_object_id");
				}

				if(packageId != null && !packageId.equals(""))
				{
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Before-Call-getObject-packageId="+packageId);
                    IDfPackage iDfPackage = (IDfPackage)currentSession.getObject(new DfId(packageId));
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-After-Call-getObject-packageId");

                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Before-Call-appendNote");
/*
                    DfTime dfTime = new DfTime();
					String noteString = dfTime.asString(DfTime.DF_TIME_PATTERN44) + " ---- " + note;
*/

                    iDfPackage.appendNote(note, true); // persistent=true, to carry it thro' out the workflow
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-After-Call-appendNote");

                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Before-Call-workItem-delegateTask");
                    iDfWorkitem.delegateTask(delegatedUser);
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-After-Call-workItem-delegateTask");

                    // Cannot call complete because the workitem is out of the current user's control
                    /*
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Before-Call-workItem-complete");
                    iDfWorkitem.complete();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-After-Call-workItem-complete");
                    */

    	            String batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchObjectId);
    		        String newStatus = "";

    		        // Reset back the Batch Status to pre QC Claim or ProbRes Claim state
    		        // String objectType = HelperClass.getInternalObjectType("submission_batch");
    	            if(batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM))
    	            {
    		        	newStatus = HelperClassConstants.PROBLEM;
    		        }
    		        else if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
    		        {
    		        	newStatus = HelperClassConstants.INSPECT;
    		        }
                    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                    iDfSysObject.setString("p_performer", "");
                    iDfSysObject.setString("p_performer_for_display", "( "+delegatedUser+" )");
                    iDfSysObject.setString(HelperClassConstants.BATCH_STATE, newStatus);
                    iDfSysObject.save();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Saved Batch Object");
            		isSuccessful = true;
				}
                else
                {
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-getPackages()-No Packages found");
                }
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForDelegate()-" + e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate() CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForDelegate()-close" + e.toString());
				e.printStackTrace();
			}
		}

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForDelegate-Call-finally-isSuccessful="+isSuccessful);

		return isSuccessful;
	}

// End of Delegate


/*
// View Note Start

    public static ArrayList getNoteTextList(IDfSession currentSession, String batchObjectId)
    {
		ArrayList noteTextList = new ArrayList();

        HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-Started-batchObjectId="+batchObjectId);

        ActionTool actionTool = null;
		try
		{
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-Start Call-getWorkItem()");
            String workItemId = actionTool.getWorkItem();
            HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
                HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-Before-Call-workItem-get Packages");
                IDfCollection packages = null;
                try
                {
                    packages = iDfWorkitem.getPackages("");
                    if(packages != null && packages.next())
                    {
                        String tempBatchId = packages.getString("r_component_id");
                        if(tempBatchId != null && tempBatchId.equals(batchObjectId))
                        {
							String packageId = packages.getString("r_object_id");
							IDfPackage iDfPackage = (IDfPackage)currentSession.getObject(new DfId(packageId));
							if(iDfPackage.getNoteCount() > 0)
							{
								for(int indx=0; indx < iDfPackage.getNoteCount(); indx++)
								{
									String singleNoteText = iDfPackage.getNoteWriter(indx) + " " + "@" + iDfPackage.getNoteCreationDate(indx).asString(DfTime.DF_TIME_PATTERN44) + "\n";
									singleNoteText += iDfPackage.getNoteText(indx);
									noteTextList.add(singleNoteText);
								}
							}
							else
							{
								HelperClass.porticoOutput(1, "QcHelperClass-getNoteTextList-No Notes attached to workitem-package");
							}
                        }
                    }
                }
                catch(Exception e)
                {
					HelperClass.porticoOutput(1, "QcHelperClass-getNoteTextList-getPackages-Exception="+e.toString());
					e.printStackTrace();
				}
                finally
                {
                    if(packages != null)
                    {
                        packages.close();
				    }
                }

       		    HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-workitem Packages Successfully");
   		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-getNoteTextList-Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-End-batchObjectId="+batchObjectId);

		return noteTextList;
	}

// View Note End
*/

// RANGA 23AUG2006

/* Used here only by 'getLeadMetadataListWithMarkedSource()' which we are getting rid off here
    public static Hashtable getLeadMetadataList(IDfSession currentSession, String batchObjectId, Collection suStateIdList)
    {
		Hashtable leadMdFiles = new Hashtable();

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataList-Start-batchObjectId="+batchObjectId);

        ActionTool actionTool = null;

		try
		{
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            if(suStateIdList != null && suStateIdList.size() > 0)
            {
                Iterator iterator = suStateIdList.iterator();
                while(iterator.hasNext())
                {
   					ProcessViewResultItem item = (ProcessViewResultItem)iterator.next();
       		      	String suStateId = item.getThisKey();
                    if(actionTool.getIsLeadMetadata(suStateId) == true)
                    {
						leadMdFiles.put(suStateId, item);
						HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataList-suStateId="+suStateId);
					}
   	            }
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-getLeadMetadataList-Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataList-Call-finally");
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataList-End-batchObjectId="+batchObjectId);

		return leadMdFiles;
	}
*/
/*
    public static Hashtable getLeadMetadataListWithMarkedSource(IDfSession currentSession, String batchObjectId, Collection suStateIdList)
    {
		Hashtable leadMetadataList = null;

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-Start-batchObjectId="+batchObjectId);

		try
		{
			leadMetadataList = getLeadMetadataList(currentSession, batchObjectId, suStateIdList);
            if(leadMetadataList != null && leadMetadataList.size() > 0)
            {
				String leadSourceSuStateId = "";
    			for (Enumeration e = leadMetadataList.keys() ; e.hasMoreElements() ;)
    			{
					String suStateId = (String)e.nextElement();
// New(oracle) datamodel change to call Oracle DB for p_su, 'p_lead_source_id'
                   // DBHelperClass.getSUStateAttributes(String objectId, ArrayList alistIn)
					IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(suStateId));
					String leadSourceSuObjectIdString = iDfSysObject.getString(DBHelperClass.P_LEAD_SOURCE_ID);
					if(leadSourceSuObjectIdString != null && !leadSourceSuObjectIdString.equals(""))
					{
						HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-leadSourceSuObjectIdString="+leadSourceSuObjectIdString);
					    String leadSourceSuObjectId = "";
                        StringTokenizer strTokenizer = new StringTokenizer(leadSourceSuObjectIdString, ",");
                        while (strTokenizer.hasMoreTokens())
                        {
    			        	leadSourceSuObjectId =  strTokenizer.nextToken().trim();
    			        	HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-leadSourceSuObjectId="+leadSourceSuObjectId);
        			        break;
                        }
                        // 'getParentFolderId' to be changed New datamodel
                        leadSourceSuStateId = getParentFolderId(currentSession, leadSourceSuObjectId);
                        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-parent suState leadSourceSuStateId="+leadSourceSuStateId);
						break;
					}
    			}

    			if(leadSourceSuStateId != null && !leadSourceSuStateId.equals(""))
    			{
					ProcessViewResultItem item = (ProcessViewResultItem)leadMetadataList.get(leadSourceSuStateId);
					if(item != null)
					{
						// This 'setIsErroredItem' is an indicator for 'lead source'
    					item.setIsErroredItem(true);
    					leadMetadataList.remove(leadSourceSuStateId);
    					leadMetadataList.put(leadSourceSuStateId, item);
				    }
				    else
				    {
    					HelperClass.porticoOutput(1, "QcHelperClass-getLeadMetadataListWithMarkedSource-End-batchObjectId,p_lead_source_id:LeadSource not in leadMetadataList="+batchObjectId+","+leadSourceSuStateId);
					}
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-getLeadMetadataListWithMarkedSource-Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-Call-finally");
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-End-batchObjectId="+batchObjectId);

		return leadMetadataList;
	}
*/

/* Change 'CallAddUserMessageHandler' to do this while querying and getting the data
    public static Hashtable getLeadMetadataListWithMarkedSource(IDfSession currentSession, String batchObjectId, Collection suStateIdList)
    {
		Hashtable leadMetadataList = null;

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-Start-batchObjectId="+batchObjectId);

		try
		{
			leadMetadataList = getLeadMetadataList(currentSession, batchObjectId, suStateIdList);
            if(leadMetadataList != null && leadMetadataList.size() > 0)
            {
				String leadSourceSuStateId = "";
    			for (Enumeration e = leadMetadataList.keys() ; e.hasMoreElements() ;)
    			{
					String suStateId = (String)e.nextElement();
// New(oracle) datamodel change to call Oracle DB for p_su, 'p_lead_source_id'
                   // DBHelperClass.getSUStateAttributes(String objectId, ArrayList alistIn)
					IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(suStateId));
					String leadSourceSuObjectIdString = iDfSysObject.getString(DBHelperClass.P_LEAD_SOURCE_ID);
					if(leadSourceSuObjectIdString != null && !leadSourceSuObjectIdString.equals(""))
					{
						HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-leadSourceSuObjectIdString="+leadSourceSuObjectIdString);
					    String leadSourceSuObjectId = "";
                        StringTokenizer strTokenizer = new StringTokenizer(leadSourceSuObjectIdString, ",");
                        while (strTokenizer.hasMoreTokens())
                        {
    			        	leadSourceSuObjectId =  strTokenizer.nextToken().trim();
    			        	HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-leadSourceSuObjectId="+leadSourceSuObjectId);
        			        break;
                        }
                        // 'getParentFolderId' to be changed New datamodel
                        leadSourceSuStateId = getParentFolderId(currentSession, leadSourceSuObjectId);
                        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-parent suState leadSourceSuStateId="+leadSourceSuStateId);
						break;
					}
    			}

    			if(leadSourceSuStateId != null && !leadSourceSuStateId.equals(""))
    			{
					ProcessViewResultItem item = (ProcessViewResultItem)leadMetadataList.get(leadSourceSuStateId);
					if(item != null)
					{
						// This 'setIsErroredItem=true' is an indicator for 'lead source'
    					item.setIsErroredItem(true);
    					leadMetadataList.remove(leadSourceSuStateId);
    					leadMetadataList.put(leadSourceSuStateId, item);
				    }
				    else
				    {
    					HelperClass.porticoOutput(1, "QcHelperClass-getLeadMetadataListWithMarkedSource-End-batchObjectId,p_lead_source_id:LeadSource not in leadMetadataList="+batchObjectId+","+leadSourceSuStateId);
					}
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-getLeadMetadataListWithMarkedSource-Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-Call-finally");
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataListWithMarkedSource-End-batchObjectId="+batchObjectId);

		return leadMetadataList;
	}
*/

	public static boolean postProcessingForUserMessagesCreation(IDfSession currentSession,
	                                                            String batchObjectId,
	                                                            String cuStateId,
	                                                            String contextObjectId,
	                                                            String userMessageCode,
	                                                            String userMessageText)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForUserMessagesCreation-Start-" +
                                                                        "batchObjectId="+batchObjectId + "," +
        	                                                            "cuStateId="+cuStateId + "," +
			                                                            "contextObjectId="+contextObjectId + ","+
			                                                            "userMessageCode="+userMessageCode + ","+
			                                                            "userMessageText="+userMessageText);
        ActionTool actionTool = null;

		try
		{
			String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId);
            actionTool = new ActionTool(currentSession, batchAccessionId);
            actionTool.flush();
// RANGA, after Mike deploy's the latest User Messages related to Add UserMessages, change to,
            // actionTool.addUserMessage(contextObjectId, userMessageCode); // Other agrs are String ... args(variable args
                                                                            // for %1, %2 stubs in the message
            if(contextObjectId != null && contextObjectId.equals(batchObjectId))
            {
				contextObjectId = batchAccessionId;
			}
            actionTool.addUserMessage(contextObjectId, userMessageCode); //, userMessageText);
            isSuccessful = true;
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForUserMessagesCreation-Exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForUserMessagesCreation-Call-finally-batchObjectId,isSuccessful="+batchObjectId + "," + isSuccessful);
			try
			{
                if(actionTool != null)
                {
			    	actionTool.flush();
			    	actionTool.clearSessionContext();
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
				eflush.printStackTrace();
			}
		}

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForUserMessagesCreation-Call-End-batchObjectId,isSuccessful="+batchObjectId + "," + isSuccessful);

		return isSuccessful;
	}

// Start QcToProbRes
	public static boolean postProcessingForQcToProbRes(IDfSession currentSession, String batchObjectId)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes(Step 1 of 3)-Call-Started-batchObjectId="+batchObjectId);
        // set batch status to QC1_IN_PROGRESS
        String processingStatus = HelperClassConstants.PROBLEM; // HelperClassConstants.AUTO_PROCESSING;
        // As per Roland, set batch outcome to 1 == FAILED status for QC, moves to Prob Res Activity
        int outcome = 1;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-Start Call-getWorkItem()");
            String workItemId = getWorkItem(currentSession, batchObjectId);
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                try
                {
                    IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
     		        IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
     		        iDfSysObject.setInt("p_activity_outcome", outcome);
        		    iDfSysObject.setString(HelperClassConstants.BATCH_STATE, processingStatus);
        		    iDfSysObject.setString("p_reentry_activity", "Generate Random Sample"); // HelperClass.getReentryPointName("generatechecksum"));
                	iDfSysObject.save();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-p_activity_outcome,p_state has been set");
                    boolean isSuccessfulClear = clearAllInspectedInfo(currentSession, batchObjectId);
                    if(isSuccessfulClear == false)
                    {
                        HelperClass.porticoOutput(1, "Error-QcHelperClass-postProcessingForQcToProbRes-Failed in clearAllInspectedInfo()");
				    }
                    iDfWorkitem.complete();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-Done-iDfWorkitem.complete()");
                	isSuccessful = true;
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-workitem-complete-Successful");
			    }
			    catch(Exception e)
			    {
					HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForQcToProbRes-During-Call-complete-Exception="+e.toString());
					// Potentially possible, avoid stack trace
				}
				finally
				{
			    }
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-(Step 1 of 3)Done");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForQcToProbRes="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForQcToProbRes-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

	// (Step 2 of 3)Wait(jsp) call 'canUserClaimBatch' (after postProcessingForQcToProbRes)

	public static boolean postProcessingForWaitQcToProbRes(IDfSession currentSession, String batchObjectId)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes(Step 3 of 3)-Call-Started-batchObjectId="+batchObjectId);
        String problemStatus = HelperClassConstants.RESOLVING_PROBLEM;
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-Start Call-getWorkItem()");
            String workItemId = getWorkItem(currentSession, batchObjectId);
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-End Call-getWorkItem()-workItemId="+workItemId);
            if(workItemId != null && !workItemId.equals(""))
            {
                try
                {
                    IDfWorkitem iDfWorkitem = (IDfWorkitem)currentSession.getObject(new DfId(workItemId));
     		        IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
        		    iDfSysObject.setString(HelperClassConstants.BATCH_STATE, problemStatus);
                	iDfSysObject.save();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-p_state has been set");
                    iDfWorkitem.acquire();
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-Done-iDfWorkitem.acquire()");
                	isSuccessful = true;
                    HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-workitem-acquire-Successful");
			    }
			    catch(Exception e)
			    {
					HelperClass.porticoOutput(1, "QcHelperClass-postProcessingForWaitQcToProbRes-During-Call-acquire-Exception="+e.toString());
					// Potentially possible, avoid stack trace
				}
				finally
				{
			    }
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-(Step 3 of 3) Done");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForWaitQcToProbRes="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForWaitQcToProbRes-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

/*
    // Replaced by 'clearAllInspectedInfo'
	public static boolean clearInspectedInfo(IDfSession currentSession, String batchObjectId)
	{
		boolean isSuccessful = false;

        HelperClass.porticoOutput(0, "QcHelperClass-clearInspectedInfo-Call-Started");
        IDfCollection tIDfCollection = null;
		try
		{
            String objectName = HelperClass.getInternalObjectType("cu_state");
            DfQuery dfquery = new DfQuery();
            String dqlString = "UPDATE " + objectName + " OBJECTS " +
                                   " SET p_inspection_required=FALSE" + "," +
                                   " SET p_inspected=FALSE" +
                                   " where " +
                                   " FOLDER(ID(" + "'"+batchObjectId+"'" + "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-clearInspectedInfo()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_EXEC_QUERY);
            if(tIDfCollection != null)
            {
   		    	tIDfCollection.close();
    	    }
    	    isSuccessful = true;

            HelperClass.porticoOutput(0, "QcHelperClass-clearInspectedInfo-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearInspectedInfo="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-clearInspectedInfo CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearInspectedInfo-close" + e.toString());
				e.printStackTrace();
			}
            HelperClass.porticoOutput(0, "QcHelperClass-clearInspectedInfo-Call-finally");
		}

		return isSuccessful;
	}
*/

	public static boolean clearAllInspectedInfo(IDfSession currentSession, String batchObjectId)
	{
		return DBHelperClass.clearAllInspectedInfo(batchObjectId);
	}

	public static boolean postProcessingForInspected(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForInspected-Call-Started-batchObjectId="+batchObjectId);

        boolean isSuccessful = false;
        int outcomeInspected = 5; // Check with VINAY, 11APR2006, On workflow template from QC if(5) go to 'Inspected' intermediate step before 'Release To Archive'
        String inspectedStatus = HelperClassConstants.INSPECTED;
		try
		{
	        IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
	        iDfSysObject.setInt("p_activity_outcome", outcomeInspected);
           	iDfSysObject.save();
            // Call the backend method
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForInspected-Before Call(Internal)-postProcessingForContinueProcessing()");
            isSuccessful = postProcessingForContinueProcessing(currentSession, batchObjectId, false);
	        iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
   		    iDfSysObject.setString(HelperClassConstants.BATCH_STATE, inspectedStatus);
           	iDfSysObject.save();
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForInspected-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForInspected="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForInspected-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

/*
	public static void clearAllFatalMessages(IDfSession currentSession, String batchObjectId)
	{
        HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages-Call-Started");
        IDfCollection tIDfCollection = null;
		try
		{
			String qcAction = "UNKNOWN";
			String severity = "2"; // fatal
			String action_taken = "FALSE";
            String objectName = HelperClass.getInternalObjectType("usermessage_object");
            DfQuery dfquery = new DfQuery();
            String dqlString = "UPDATE " + objectName + " OBJECTS " +
                                   " SET p_action_taken=TRUE" + "," +
                                   " SET p_action_desc=" + "'" + qcAction + "'" +
                                   " where " +
                                   " p_severity=" + "'" + severity + "'" + " AND " +
                                   " p_action_taken=" + action_taken + " AND " +
                                   " FOLDER(ID(" + "'"+batchObjectId+"'" + "), DESCEND)";

            HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages()-dqlString="+dqlString);
   			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_EXEC_QUERY);
            if(tIDfCollection != null)
            {
   		    	tIDfCollection.close();
    	    }
            HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages-Call-Ended");
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearAllFatalMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearAllFatalMessages-close" + e.toString());
			}
            HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages-Call-finally");
		}
	}
*/
	public static boolean clearAllFatalMessages(IDfSession currentSession, String batchObjectId)
	{
		boolean isSuccessful = true;
        HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages-Call-Started-batchObjectId="+batchObjectId);
		try
		{
			isSuccessful = DBHelperClass.clearAllFatalMessages(batchObjectId);
		}
		catch(Exception e)
		{
			isSuccessful = false;
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-clearAllFatalMessages="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcHelperClass-clearAllFatalMessages-Call-Ended-batchObjectId="+batchObjectId+","+
                                                                                       "isSuccessful="+isSuccessful);
        return isSuccessful;
	}

	public static Hashtable getActionReentryPointMappingListFromSession(IDfSession currentSession, String batchObjectId)
	{
		Hashtable retMapping = new Hashtable();
		try
		{
		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
		    String profileId = iDfSysObject.getString("p_profile_id");
		    HelperClass.porticoOutput(0, "QcHelperClass-getActionReentryPointMappingListFromSession-profileId="+profileId);
		    Hashtable mapping = AppSessionContext.getActionReentryPointMappingListUI();

		    if(mapping != null && mapping.size() > 0)
		    {
		    	if(mapping.containsKey(profileId))
		    	{
                    retMapping = (Hashtable)mapping.get(profileId);
		        }
		        else
		        {
					HelperClass.porticoOutput(0, "QcHelperClass-getActionReentryPointMappingListFromSession-Using default for profileId="+profileId);
		    		// Return default
		    		retMapping = (Hashtable)mapping.get(QcHelperClass.profileDefault);
		    	}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-getActionReentryPointMappingListFromSession-" + e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

// Test print start
		HelperClass.porticoOutput(0, "QcHelperClass-getActionReentryPointMappingListFromSession-print-Start");
        Enumeration retEnumerate = retMapping.keys();
        while(retEnumerate.hasMoreElements())
        {
			String key = (String)retEnumerate.nextElement();
			String value = (String)retMapping.get(key);
			HelperClass.porticoOutput(0, "QcHelperClass-getActionReentryPointMappingListFromSession-key,value="+key+","+value);

		}
		HelperClass.porticoOutput(0, "QcHelperClass-getActionReentryPointMappingListFromSession-print-End");
// Test print end

		return retMapping;
	}

	public static ArrayList getOrderedWorkflowActivityListFromSession(IDfSession currentSession, String batchObjectId)
	{
		ArrayList retList = new ArrayList();
		try
		{
		    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
		    String profileId = iDfSysObject.getString("p_profile_id");
		    HelperClass.porticoOutput(0, "QcHelperClass-getOrderedWorkflowActivityListFromSession-profileId="+profileId);
		    Hashtable mapping = AppSessionContext.getWorkflowActivityListUI();
		    if(mapping != null && mapping.size() > 0)
		    {
		    	if(mapping.containsKey(profileId))
		    	{
                    retList = (ArrayList)mapping.get(profileId);
		        }
		        else
		        {
					HelperClass.porticoOutput(0, "QcHelperClass-getOrderedWorkflowActivityListFromSession-Using default for profileId="+profileId);
		    		// Return default
		    		retList = (ArrayList)mapping.get(QcHelperClass.profileDefault);
		    	}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-getOrderedWorkflowActivityListFromSession-" + e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}
// Test print start
		HelperClass.porticoOutput(0, "QcHelperClass-getOrderedWorkflowActivityListFromSession-print-Start");
		for(int tindx=0; tindx < retList.size(); tindx++)
		{
			HelperClass.porticoOutput(0, "QcHelperClass-getOrderedWorkflowActivityListFromSession-"+retList.get(tindx));
		}
		HelperClass.porticoOutput(0, "QcHelperClass-getOrderedWorkflowActivityListFromSession-print-End");
// Test print end

        return retList;
	}

	public static boolean checkIfDifferentWfTemplates(String oldProfileId, String newProfileId)
	{
		HelperClass.porticoOutput(0, "QcHelperClass-checkIfDifferentWfTemplates-oldProfileId,newProfileId="+oldProfileId+","+newProfileId);

		boolean isDifferent = false;
		String oldTemplate = "";
		String newTemplate = "";
        ValuePair tValuePair = null;
        ArrayList attrList = new ArrayList();
        attrList.add("alternateworkflow");
        ArrayList outList = HelperClass.lookupServiceInfo("profile", oldProfileId, attrList);
        if(outList != null && outList.size() > 0)
        {
            for(int indx=0; indx < outList.size(); indx++)
            {
                tValuePair = (ValuePair)outList.get(indx);
                if(tValuePair.getKey().equals("alternateworkflow"))
                {
                    oldTemplate = (String)tValuePair.getValue();
                    break;
			    }
            }
        }
        outList.clear();
        outList = HelperClass.lookupServiceInfo("profile", newProfileId, attrList);
        if(outList != null && outList.size() > 0)
        {
            for(int indx=0; indx < outList.size(); indx++)
            {
                tValuePair = (ValuePair)outList.get(indx);
                if(tValuePair.getKey().equals("alternateworkflow"))
                {
                    newTemplate = (String)tValuePair.getValue();
                    break;
			    }
            }
        }

		HelperClass.porticoOutput(0, "QcHelperClass-checkIfDifferentWfTemplates-oldProfileId,oldTemplate="+oldProfileId+","+oldTemplate);
		HelperClass.porticoOutput(0, "QcHelperClass-checkIfDifferentWfTemplates-newProfileId,newTemplate="+newProfileId+","+newTemplate);

        if(oldTemplate == null && newTemplate == null)
        {
			// templates are same, no change
		}
		else if(oldTemplate != null && newTemplate != null && oldTemplate.equals(newTemplate))
		{
			// templates are same, no change
		}
		else
		{
			isDifferent = true;
		}

		HelperClass.porticoOutput(0, "QcHelperClass-checkIfDifferentWfTemplates-End-isDifferent="+isDifferent);

		return isDifferent;
	}

    public static ArrayList getNoteTextList(IDfSession currentSession, String batchObjectId)
    {
		ArrayList noteTextList = new ArrayList();

        HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-Started-batchObjectId="+batchObjectId);
		IDfCollection tIDfCollection = null;
		try
		{
            String attrNames = "r_object_id,r_workflow_id,r_component_id ";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + "dmi_package" +
                               " WHERE any r_component_id=" + "'"+batchObjectId+"'" +
                               " AND r_workflow_id=" +
                               "'"+HelperClass.getWorkflowObject(currentSession, batchObjectId)+"'";

            HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList()-dqlString="+dqlString);

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					String packageId = tIDfCollection.getString("r_object_id");
					IDfPackage iDfPackage = (IDfPackage)currentSession.getObject(new DfId(packageId));
					if(iDfPackage.getNoteCount() > 0)
					{
						for(int indx=0; indx < iDfPackage.getNoteCount(); indx++)
						{
							String singleNoteText = iDfPackage.getNoteWriter(indx) + " " + "@" + iDfPackage.getNoteCreationDate(indx).asString(DfTime.DF_TIME_PATTERN44) + "\n";
							singleNoteText += iDfPackage.getNoteText(indx);
							noteTextList.add(singleNoteText);
						}
					}
					else
					{
						HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-No Notes attached to package");
					}
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "QcHelperClass-getNoteTextList-Exception="+e.toString());
			// e.printStackTrace(); Ignore
		}
		finally
		{
			try
			{
			    if(tIDfCollection != null)
			    {
			    	tIDfCollection.close();
			    }
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "QcHelperClass-getNoteTextList-tIDfCollection.close()-Exception="+e.toString());
				e.printStackTrace();
		    }
		}

        HelperClass.porticoOutput(0, "QcHelperClass-getNoteTextList-End-batchObjectId="+batchObjectId);

		return noteTextList;
	}

	public static String getControlSafeString(String input)
	{
		String controlSafeString = input;
	    if(controlSafeString != null && !controlSafeString.equals(""))
		{
			controlSafeString = controlSafeString.replace(':', 'A');
			controlSafeString = controlSafeString.replace('/', 'Z');
		}

		return controlSafeString;
	}

    // New implementation to pick the possibleFilePaths from the SU(S) for 'AddNewFile' action
    public static TreeSet getPossibleFilePathsForAddition(IDfSession currentSession, String batchId)
    {
	    TreeSet possibleFilePaths = new TreeSet();
	    int findIndx = -1;
	    String unixStringSeparator = "/";

	    // SEE IF Submission view's 'callPatternHandler' can be used
	    // Fire a dql,
	    // get all the distinct paths(removing the filename)
	    // do recursively
	    //    pick one of the distinct path(c:/temp/vol1/issue1/journal1/)
	    //    slice each path(c:/temp/vol1/issue1/)
	    //    slice each path(c:/temp/vol1/)
	    //    slice each path(c:/temp/)
	    //    slice each path(c:/)
	    // done recursion
	    ArrayList submissionFilePaths = DBHelperClass.getDistinctWorkFileNames(batchId);
	    if(submissionFilePaths != null && submissionFilePaths.size() > 0)
	    {
			for(int indx=0; indx < submissionFilePaths.size(); indx++)
			{
    		    String currentItem = (String)submissionFilePaths.get(indx);
		        if((findIndx=currentItem.lastIndexOf(unixStringSeparator)) != -1)
		        {
    			    // currentItem=C:/ranga/temp 1/good.txt
    		        // remove filename portion
					currentItem = currentItem.substring(0,findIndx);
    			    // converted to currentItem=C:/ranga/temp 1
    			    populatePossibleSubPathCombinations(possibleFilePaths, currentItem);
				}
			}
		}
	    if(possibleFilePaths != null && possibleFilePaths.size() > 0)
	    {
            Iterator tIterate = possibleFilePaths.iterator();
            while(tIterate.hasNext())
            {
      			HelperClass.porticoOutput(0, "QcHelperClass-getPossibleFilePathsForAddition(Result)="+(String)tIterate.next());
       	    }
		}

	    return possibleFilePaths;
	}

	public static String getWorkItem(IDfSession currentSession, String batchObjectId)
	{
		String workItem = "";

        HelperClass.porticoOutput(0, "QcHelperClass-Start-getWorkItem-for-batchObjectId="+batchObjectId);

        if(batchObjectId != null)
        {
		    try
		    {
				 String userNameList = "";
				 ArrayList currentUserAndGroupsList = AppSessionContext.getCurrentUserAndGroupsListUI();
				 if(currentUserAndGroupsList != null && currentUserAndGroupsList.size() > 0)
				 {
				     for(int indx=0; indx < currentUserAndGroupsList.size(); indx++)
				     {
						 if(indx == 0)
						 {
							 userNameList = "'" + (String)currentUserAndGroupsList.get(indx) + "'";
						 }
						 else
						 {
						     userNameList = userNameList + "," + "'" + (String)currentUserAndGroupsList.get(indx) + "'";
					     }
					 }

					 HelperClass.porticoOutput(0, "QcHelperClass-getWorkItem-userNameList="+userNameList);

                     IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
					 String workflowId = iDfSysObject.getString("p_workflow_id");
                     if(null != workflowId && !workflowId.equals(""))
                     {
                         HelperClass.porticoOutput(0, "QcHelperClass-getWorkItem-workflowId="+workflowId);
						 // delete_flag=false returns the active rows, which have not been marked for delete
                         String qualification = "dmi_queue_item WHERE name IN "+
                                                "(" + userNameList + ")" +
                                                 " and router_id='"+workflowId+"' " +
                                                 " and delete_flag=false";

                         HelperClass.porticoOutput(0, "QcHelperClass-getWorkItem-qualification="+qualification);

                         IDfQueueItem queueItem = (IDfQueueItem) currentSession.getObjectByQualification(qualification);
                         if( null != queueItem )
                         {
                             HelperClass.porticoOutput(0, "QcHelperClass-getWorkItem-Received-queueItem");
                             IDfWorkitem iDfWorkitem = queueItem.getWorkitem();
                             workItem = iDfWorkitem.getObjectId().getId();
                         }
                         else
                         {
    						 HelperClass.porticoOutput(1, "Error-QcHelperClass-getWorkItem-queueItem not found for batchObjectId="+batchObjectId);
						 }
                     }
                     else
                     {
						 HelperClass.porticoOutput(1, "Error-QcHelperClass-getWorkItem-WorkflowId not populated for batchObjectId="+batchObjectId);
					 }
			     }
			}
            catch(Exception e)
            {
			    HelperClass.porticoOutput(1, "Exception in QcHelperClass-getWorkItem-"+e.toString());
			    // e.printStackTrace(); Ignore
			}
			finally
			{
			}
		}

		HelperClass.porticoOutput(0, "QcHelperClass-End-getWorkItem-for-batchObjectId="+batchObjectId+",workItem="+workItem);

		return workItem;
	}

	public static void printBatchInfo(String batchObjectId)
	{
		try
		{
			String batchAccessionId = "";
			ArrayList alistIn = new ArrayList();
			alistIn.add(DBHelperClass.P_ACCESSION_ID);
			Hashtable alistOut = DBHelperClass.getBatchAttributes(batchObjectId, alistIn);
			if(alistOut != null && alistOut.size() > 0)
			{
				String warningCount = "";
                if(alistOut.containsKey(DBHelperClass.P_ACCESSION_ID))
                {
					batchAccessionId = (String)alistOut.get(DBHelperClass.P_ACCESSION_ID);
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcHelperClass-printBatchInfo-"+e.toString());
			// e.printStackTrace(); Ignore
		}
		finally
		{
		}
	}

	public static List getLeadMetadataInfo(IDfSession currentSession, String batchObjectId, String cuStateId)
	{
		List leadMetaData = null;

        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-Call-Started-batchObjectId="+batchObjectId + ",cuStateId=" + cuStateId);
		try
		{
	        MetadataMethods metadataMethods = new MetadataMethods(cuStateId);
	        leadMetaData = metadataMethods.getAllMetadataList();
		}
		catch(Exception e)
		{
		    HelperClass.porticoOutput(1, "Exception in QcHelperClass-getLeadMetadataInfo="+e.toString());
		    e.printStackTrace();
		}
		finally
		{
	        HelperClass.porticoOutput(0, "QcHelperClass-getLeadMetadataInfo-Call-finally");
		}

		return leadMetaData;
	}

	public static boolean isEnvOverridableAction(String actionlike)
	{
		boolean isOverridableAction = false;

        HelperClass.porticoOutput(0, "QcHelperClass-isEnvOverridableAction-Call-Started-actionlike="+actionlike);

		try
		{
			String isOverridableActionStr = LdapUtil.getAttribute("dc=ui", "cn=overrideaction", actionlike);
			if(isOverridableActionStr != null && isOverridableActionStr.equalsIgnoreCase("true"))
            {
				isOverridableAction = true;
			}
	    }
	    catch(Exception e)
	    {
		    HelperClass.porticoOutput(1, "Exception in QcHelperClass-isEnvOverridableAction="+e.toString());
		    e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "QcHelperClass-isEnvOverridableAction-Call-Ended-actionlike="+actionlike+",isOverridableAction="+isOverridableAction);

		return isOverridableAction;
	}

	public static boolean isValidFormatAssignmentAction(IDfSession currentSession, String objectId, String msgObjectId, Hashtable addlnInfo)
	{
		boolean isValid = false;
		HelperClass.porticoOutput(0, "QcHelperClass-isValidFormatAssignmentAction(Call-Started)-objectId="+objectId);
		HelperClass.porticoOutput(0, "QcHelperClass-isValidFormatAssignmentAction(Call-Started)-msgObjectId="+msgObjectId);
		try
		{
			String batchStatus = "";

			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHSTATUS))
			{
                batchStatus = (String)addlnInfo.get(HelperClassConstants.BATCHSTATUS);
			}

            if(batchStatus == null || batchStatus.equals(""))
			{
    			String batchId = "";

    			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
			    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
			    }

                if(batchId == null || batchId.equals(""))
                {
    		    	batchId = HelperClass.getParentBatchFolderId(currentSession, objectId);
			    }

    			if(batchId != null)
    			{
    			    batchStatus = HelperClass.getStatusForBatchObject(currentSession, batchId);
			    }
			}

	    	isValid = isValidBatchStatusForTrueQCAction(batchStatus);

    		HelperClass.porticoOutput(0, "QcHelperClass-isValidFormatAssignmentAction(Call-Ended)-objectId="+objectId+","+isValid);
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isValidFormatAssignmentAction="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isValidFormatAssignmentAction(Call-finally)-objectId="+objectId + " isValid="+isValid);
		}

		return isValid;
	}

	public static boolean postProcessingForFormatAssignment(IDfSession currentSession, String batchObjectId, String suStateId,
	                                                            String existingFormatId, String existingFormatName, String existingMimeType,
	                                                            String newFormatId, String newFormatName, String newMimeType,
	                                                            String reason, String userMsgId)
	{
		String contextId = suStateId;
		boolean isSuccessful = false;
   		boolean isSuccessfulBackendAction = false;
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+suStateId
                                                + ",existingFormatId="+existingFormatId + ",existingFormatName=" + existingFormatName + ",existingMimeType=" + existingMimeType
                                                + ",newFormatId="+newFormatId + ",newFormatName=" + newFormatName + ",newMimeType=" + newMimeType
                                                + ",reason="+reason + ",userMsgId=" + userMsgId);
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-Start backend call");
            ActionTool actionTool = null;
            try
            {
                actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
                actionTool.flush();
                actionTool.assignFormat(suStateId,
                                            currentSession.getLoginUserName(),
                                            existingFormatId,
                                            newFormatId,
                                            existingFormatName,
                                            newFormatName,
                                            existingMimeType,
                                            newMimeType,
                                            reason,
                                            suStateId,
                                            reason);
                isSuccessfulBackendAction = true;
		    }
		    catch(Exception e)
		    {
    	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForFormatAssignment="+e.toString());
    	        e.printStackTrace();
			}
			finally
			{
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
				try
				{
                    if(actionTool != null)
                    {
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-call-Before flush");
				    	actionTool.flush();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-call-After flush");
						actionTool.clearSessionContext();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-call-After clearSessionContext");
				    }
			    }
			    catch(Exception eflush)
			    {
					HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
					eflush.printStackTrace();
				}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForFormatAssignment-for suStateId="+suStateId+":"+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-Call-finally-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			if(isSuccessfulBackendAction == true)
			{
        		// processUserMessage(currentSession, userMsgId, batchObjectId, "FormatAssignment");
                setCommonBatchInfoForQC(currentSession, batchObjectId, "FormatAssignment");
                isSuccessful = true;
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForFormatAssignment-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}

    public static TreeSet getPossibleFilePathsForAdditionFromRawunits(IDfSession currentSession, String batchId)
    {
	    TreeSet possibleFilePaths = new TreeSet();
	    int findIndx = -1;
	    String unixStringSeparator = "/";

	    // SEE IF Submission view's 'callPatternHandler' can be used
	    // Fire a dql,
	    // get all the distinct paths(removing the filename)
	    // do recursively
	    //    pick one of the distinct path(c:/temp/vol1/issue1/journal1/)
	    //    slice each path(c:/temp/vol1/issue1/)
	    //    slice each path(c:/temp/vol1/)
	    //    slice each path(c:/temp/)
	    //    slice each path(c:/)
	    // done recursion
	    ArrayList submissionFilePaths = HelperClass.getSubmissionViewObjects(currentSession, batchId);
	    if(submissionFilePaths != null && submissionFilePaths.size() > 0)
	    {
			for(int indx=0; indx < submissionFilePaths.size(); indx++)
			{
    		    SubmissionPatternResultItem tItem = (SubmissionPatternResultItem)submissionFilePaths.get(indx);
		        String currentItem = tItem.getThisToken();
		        if((findIndx=currentItem.lastIndexOf(unixStringSeparator)) != -1)
		        {
    		        // remove filename portion
					currentItem = currentItem.substring(0,findIndx);
    			    // See sample below
    			    // currentItem=C:/ranga/temp 1/good.txt
    			    populatePossibleSubPathCombinations(possibleFilePaths, currentItem);
				}
			}
		}
	    if(possibleFilePaths != null && possibleFilePaths.size() > 0)
	    {
            Iterator tIterate = possibleFilePaths.iterator();
            while(tIterate.hasNext())
            {
      			HelperClass.porticoOutput(0, "QcHelperClass-getPossibleFilePathsForAdditionFromRawunits(Result)="+(String)tIterate.next());
       	    }
		}

	    return possibleFilePaths;
	}

    public static boolean isValidAddNewFileActionAdditionalCheck()
    {
		return isEnvOverridableAction(LDAP_ADDNEWFILE_IN_NONQCBATCHSTATUS);
	}

    public static boolean isTopLevelSuppliedFile(IDfSession currentSession, String batchObjectId, String suStateId)
    {
		boolean flag = false;
        boolean isSuccessfulBackendAction = false;

        HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-Start backend call-suStateId="+suStateId);
        ActionTool actionTool = null;
        try
        {
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            flag = actionTool.isTopLevelSuppliedFile(suStateId);
            isSuccessfulBackendAction = true;
	    }
	    catch(Exception e)
	    {
   	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isTopLevelSuppliedFile="+e.toString());
   	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			try
			{
                if(actionTool != null)
                {
					HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-call-Before flush");
			    	actionTool.flush();
					HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-call-After flush");
					actionTool.clearSessionContext();
					HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-call-After clearSessionContext");
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-isTopLevelSuppliedFile-flush -"+eflush.toString());
				eflush.printStackTrace();
			}

			HelperClass.porticoOutput(0, "QcHelperClass-isTopLevelSuppliedFile-suStateId="+suStateId+",flag="+flag);
	    }

	    return flag;
	}

    public static boolean isSuStatePartOfMultipleSuppliedFiles(IDfSession currentSession, String batchObjectId, String suStateId)
    {
		boolean flag = false;
        boolean isSuccessfulBackendAction = false;

        HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-Start backend call-suStateId="+suStateId);
        ActionTool actionTool = null;
        try
        {
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            flag = actionTool.isSuStatePartOfMultipleSuppliedFiles(suStateId);
            isSuccessfulBackendAction = true;
	    }
	    catch(Exception e)
	    {
   	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-isSuStatePartOfMultipleSuppliedFiles="+e.toString());
   	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			try
			{
                if(actionTool != null)
                {
					HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-call-Before flush");
			    	actionTool.flush();
					HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-call-After flush");
					actionTool.clearSessionContext();
					HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-call-After clearSessionContext");
			    }
		    }
		    catch(Exception eflush)
		    {
				HelperClass.porticoOutput(1, "Exception in QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-flush -"+eflush.toString());
				eflush.printStackTrace();
			}

			HelperClass.porticoOutput(0, "QcHelperClass-isSuStatePartOfMultipleSuppliedFiles-suStateId="+suStateId+",flag="+flag);
	    }

	    return flag;
	}

	public static boolean postProcessingForMimeTypeAssignment(IDfSession currentSession, String batchObjectId, String suStateId,
	                                                            String existingMimeType,
	                                                            String newMimeType,
	                                                            String reason,
	                                                            String userMsgId)
	{
		String contextId = suStateId;
		boolean isSuccessful = false;
   		boolean isSuccessfulBackendAction = false;
        HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-Call-Started-batchObjectId="+batchObjectId + ",suStateId="+suStateId
                                                + ",existingMimeType=" + existingMimeType
                                                + ",newMimeType=" + newMimeType
                                                + ",reason="+reason + ",userMsgId=" + userMsgId);
		try
		{
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-Start backend call");
            ActionTool actionTool = null;
            try
            {
                actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
                actionTool.flush();
                actionTool.assignMimeType(suStateId,
                                            currentSession.getLoginUserName(),
                                            existingMimeType,
                                            newMimeType,
                                            reason,
                                            suStateId,
                                            reason);
                isSuccessfulBackendAction = true;
		    }
		    catch(Exception e)
		    {
    	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForMimeTypeAssignment="+e.toString());
    	        e.printStackTrace();
			}
			finally
			{
                HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-End backend call-isSuccessfulBackendAction="+isSuccessfulBackendAction);
				try
				{
                    if(actionTool != null)
                    {
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-call-Before flush");
				    	actionTool.flush();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-call-After flush");
						actionTool.clearSessionContext();
						HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-call-After clearSessionContext");
				    }
			    }
			    catch(Exception eflush)
			    {
					HelperClass.porticoOutput(1, "Exception in flush -"+eflush.toString());
					eflush.printStackTrace();
				}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcHelperClass-postProcessingForMimeTypeAssignment-for suStateId="+suStateId+":"+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-Call-finally-isSuccessfulBackendAction="+isSuccessfulBackendAction);
			if(isSuccessfulBackendAction == true)
			{
                setCommonBatchInfoForQC(currentSession, batchObjectId, "MimeTypeAssignment");
                isSuccessful = true;
			}
            HelperClass.porticoOutput(0, "QcHelperClass-postProcessingForMimeTypeAssignment-Call-finally-isSuccessful="+isSuccessful);
		}

		return isSuccessful;
	}
}