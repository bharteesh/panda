
package org.portico.conprep.ui.helper;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfId;

public class DebugClass
{
    public DebugClass()
    {
    }

    public void dumpObject(IDfSession currentsession, String objectType, String objectId)
    {
   		try
		{
			if(objectType.equals("workflow"))
			{
		    	IDfWorkflow iDfWorkflow = (IDfWorkflow)currentsession.getObject(new DfId(objectId));
		    	HelperClass.porticoOutput(0, "DebugClass-Dump(Start) of Workflow object for workflow id = "+ objectId);
		    	// HelperClass.porticoOutput(iDfWorkflow.dump());
		    	HelperClass.porticoOutput(0, "DebugClass-Dump(End) of Workflow object for workflow id = "+ objectId);
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in DebugClass-dumpObject ObjectId = "+ objectId + "::" + e.getMessage());
		}
		finally
		{
		}
	}
}