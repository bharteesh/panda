
package org.portico.conprep.ui.helper;

import java.util.Hashtable;

import com.documentum.web.common.ArgumentList;

public class HelperClassConstants
{

	// List of constants for BATCH STATUS
    public final static String NEW = "NEW";
    public final static String LOADED = "LOADED";
    public final static String QUEUED = "QUEUED";
    public final static String AUTO_PROCESSING = "AUTO_PROCESSING";
    public final static String PROBLEM = "PROBLEM";
    public final static String SYSTEM_ERROR = "SYSTEM_ERROR";
    public final static String RESOLVING_PROBLEM = "RESOLVING_PROBLEM";
    public final static String INSPECT = "INSPECT";
    public final static String INSPECTING = "INSPECTING";
    public final static String INSPECTED = "INSPECTED";
    public final static String POST_PROCESSING = "POST_PROCESSING";
    public final static String INGESTED = "INGESTED";
    public final static String WITHDRAWN = "WITHDRAWN";
    public final static String RETAINED = "RETAINED";
    public final static String RELEASED = "RELEASED";

    // Batch Attribute constants
    public final static String BATCH_STATE = "p_state";

	// Config file related argument constants
	// All the actions/components must pass these arguments
	public final static String OBJECTID = "objectId";
	public final static String ACCESSIONID = "accessionId"; // All state objects(oracle) have this
	public final static String OBJECTTYPE = "objectType";
	public final static String BATCHSTATUS = "batchStatus";
	public final static String BATCHOBJECTID = "batchObjectId";
	public final static String ISBATCHONHOLD = "isBatchOnHold";
	public final static String BATCHPERFORMER = "batchPerformer";
	public final static String BATCHUSERACTIONTAKEN = "batchUserActionTaken";
	public final static String BATCHREENTRYACTIVITY = "batchReentryActivity";
	public final static String BATCHWORKFLOWID = "batchWorkflowId";
	public final static String BATCHWORKFLOWRUNTIMESTATUS = "batchWorkflowRuntimeStatus";
	public final static String BATCHLASTACTIVITY = "batchLastActivity";

    // Other constants
	public final static String WF_INITIALIZE_BATCH = "Initialize Batch";

	public static Hashtable getAddlnInfo(ArgumentList argList)
	{
		Hashtable addlnInfo = new Hashtable();

        // Pick 'accessionId first since for 'Replace' action we do pass a objectId(batchFolderId)
        // which is a workaround to the replace/import component, but the true Id for processing is
        // accessionid
	    if(argList.get(HelperClassConstants.ACCESSIONID) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.OBJECTID, argList.get(HelperClassConstants.ACCESSIONID));
		}
        else if(argList.get(HelperClassConstants.OBJECTID) != null)
        {
	    	addlnInfo.put(HelperClassConstants.OBJECTID, argList.get(HelperClassConstants.OBJECTID));
	    }
	    if(argList.get(HelperClassConstants.OBJECTTYPE) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.OBJECTTYPE, argList.get(HelperClassConstants.OBJECTTYPE));
	    }
	    if(argList.get(HelperClassConstants.BATCHSTATUS) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHSTATUS, argList.get(HelperClassConstants.BATCHSTATUS));
	    }
	    if(argList.get(HelperClassConstants.BATCHOBJECTID) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHOBJECTID, argList.get(HelperClassConstants.BATCHOBJECTID));
	    }
	    if(argList.get(HelperClassConstants.ISBATCHONHOLD) != null)
	    {
            String onHold = "false";
            HelperClass.porticoOutput(0, "HelperClassConstants-getAddlnInfo(ISBATCHONHOLD)-argList.get(HelperClassConstants.ISBATCHONHOLD)="+ argList.get(HelperClassConstants.ISBATCHONHOLD));
            if(argList.get(HelperClassConstants.ISBATCHONHOLD).equals("1.0") || argList.get(HelperClassConstants.ISBATCHONHOLD).equals("1"))
            {
				onHold = "true";
			}
	    	addlnInfo.put(HelperClassConstants.ISBATCHONHOLD, onHold);
	    }
	    if(argList.get(HelperClassConstants.BATCHPERFORMER) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHPERFORMER, argList.get(HelperClassConstants.BATCHPERFORMER));
	    }
	    if(argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN) != null)
	    {
            String batchActionTaken = "false";
            HelperClass.porticoOutput(0, "HelperClassConstants-getAddlnInfo(BATCHUSERACTIONTAKEN)-argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN)="+ argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN));
            if(argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN).equals("1.0") || argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN).equals("1"))
            {
				batchActionTaken = "true";
			}
            HelperClass.porticoOutput(0, "HelperClassConstants-getAddlnInfo(BATCHUSERACTIONTAKEN),batchActionTaken="+ argList.get(HelperClassConstants.BATCHUSERACTIONTAKEN)+","+batchActionTaken);
	    	addlnInfo.put(HelperClassConstants.BATCHUSERACTIONTAKEN, batchActionTaken);
	    }
	    if(argList.get(HelperClassConstants.BATCHREENTRYACTIVITY) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHREENTRYACTIVITY, argList.get(HelperClassConstants.BATCHREENTRYACTIVITY));
	    }
	    if(argList.get(HelperClassConstants.BATCHWORKFLOWID) != null)
	    {
        	addlnInfo.put(HelperClassConstants.BATCHWORKFLOWID, argList.get(HelperClassConstants.BATCHWORKFLOWID));
	    }
	    if(argList.get(HelperClassConstants.BATCHWORKFLOWRUNTIMESTATUS) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHWORKFLOWRUNTIMESTATUS, argList.get(HelperClassConstants.BATCHWORKFLOWRUNTIMESTATUS));
	    }
	    if(argList.get(HelperClassConstants.BATCHLASTACTIVITY) != null)
	    {
	    	addlnInfo.put(HelperClassConstants.BATCHLASTACTIVITY, argList.get(HelperClassConstants.BATCHLASTACTIVITY));
	    }

		return addlnInfo;
	}
}