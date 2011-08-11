
package org.portico.conprep.ui.helper;

import java.util.ArrayList;
import java.util.Hashtable;

public class MetadataMethods
{
	String cuAccessionId = "";
	ArrayList allMetadataList = new ArrayList();
	ArrayList activeLeadMDList = new ArrayList();
	Hashtable objectPredecessorMapping = new Hashtable();

    public MetadataMethods(String tCuAccessionId)
    {
		cuAccessionId = tCuAccessionId;
    }

    public ArrayList getAllMetadataList()
    {

		try
		{
            HelperClass.porticoOutput(0, "MetadataMethods-getAllMetadataList-Start");

    		allMetadataList.clear();
    		activeLeadMDList.clear();
    		objectPredecessorMapping.clear();

    	    activeLeadMDList = DBHelperClass.getActiveLeadMetadataPerArticle(cuAccessionId);
    	    ArrayList outNoPredecessorlist = new ArrayList();
    	    // This contains the complete list of mapping because predecessor(s) could reside on many other FU Type(s)
    	    objectPredecessorMapping = DBHelperClass.getObjectPredecessorMappingPerArticle(cuAccessionId, outNoPredecessorlist);
            for(int activeIndx=0; activeIndx < activeLeadMDList.size(); activeIndx++)
            {
    			String tObjectId = (String)activeLeadMDList.get(activeIndx);
    			if(!allMetadataList.contains(tObjectId))
    			{
    				allMetadataList.add(tObjectId);
    			}
    			populatePredecessorGivenAnObject(tObjectId);
    		}
    		// In case these were not part of the all MD list add them to the list
    		// This would happen in cases, if the active one is a generated object.
    		// If no, generated objects were created, potentially the 'activeLeadMDList' would have picked
    		// these supplied active ones.
    		if(outNoPredecessorlist != null && outNoPredecessorlist.size() > 0)
    		{
    			for(int noIndx=0; noIndx < outNoPredecessorlist.size(); noIndx++)
    			{
    				String tObjectId = (String)outNoPredecessorlist.get(noIndx);
    				if(!allMetadataList.contains(tObjectId))
    				{
    					allMetadataList.add(tObjectId);
    				}
    			}
    		}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in MetadataMethods-getAllMetadataList-"+e.getMessage());
		}
		finally
		{
		    HelperClass.porticoOutput(0, "MetadataMethods-getAllMetadataList-finally");
		}

		return allMetadataList;
	}

	public void populatePredecessorGivenAnObject(String currentObjectId)
	{
		if(objectPredecessorMapping.containsKey(currentObjectId))
		{
			ArrayList alistOut = (ArrayList)objectPredecessorMapping.get(currentObjectId);
			for(int indx=0; indx < alistOut.size(); indx++)
			{
				String tObjectId = (String)alistOut.get(indx);
				if(!allMetadataList.contains(tObjectId))
				{
					allMetadataList.add(tObjectId);
				}
				populatePredecessorGivenAnObject(tObjectId);
			}
		}
	}
}