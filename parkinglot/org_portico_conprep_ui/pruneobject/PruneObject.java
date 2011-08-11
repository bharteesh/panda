
package org.portico.conprep.ui.pruneobject;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

public class PruneObject extends Component
{
    public PruneObject()
    {
        m_strObjectId = null;
        m_strObjectType = null;
        m_pruneList = new ArrayList();
        m_idList = new ArrayList();
        m_batchStatus = "";
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        m_strObjectId = argumentlist.get("objectId");
        m_strObjectType = argumentlist.get("objectType");
        Panel panel = (Panel)getControl("pruneoptions", com.documentum.web.form.control.Panel.class);
        ((Radio)getControl("prune_none", com.documentum.web.form.control.Radio.class)).setEnabled(true);
        ((Radio)getControl("prune_none", com.documentum.web.form.control.Radio.class)).setValue(true);
        populatePruneList();
/*
        Control control = getContainer();
        if(control instanceof Form)
            ((Form)control).setModal(false);
        setModal(false);
*/
    }

    public boolean onCommitChanges()
    {
        Radio radio_none = (Radio)getControl("prune_none", com.documentum.web.form.control.Radio.class);
        HelperClass.porticoOutput("PruneObject - onCommitChanges - radio prune_none="+radio_none.getValue());
        if(radio_none.getValue())
        {
            HelperClass.porticoOutput("PruneObject - onCommitChanges - selected radio_none");
		}
		else
		{
			if(m_pruneList != null && m_pruneList.size() > 0)
			{
				for(int index=0; index < m_pruneList.size(); index++)
				{
					String radioName = index+"";
					Radio radio_level = (Radio)getControl(radioName, com.documentum.web.form.control.Radio.class);
					if(radio_level != null && radio_level.getValue())
					{
						String textName = "text"+index;
						Text pruneText = (Text)getControl(textName, com.documentum.web.form.control.Text.class);
						String pruneString = pruneText.getValue();
						HelperClass.porticoOutput("PruneObject - onCommitChanges - selected text="+pruneString);
						pruneObjectNames(pruneString);
						break;
					}
				}
			}
		}
        return true;
    }

    private void pruneObjectNames(String pruneString)
    {
		try
		{
		    for(int indx=0; indx < m_idList.size(); indx++)
		    {
                IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId((String)m_idList.get(indx)));
                String currentObjectName = iDfSysObject.getObjectName();
                String massagedObjectName = getMassagedObjectName(pruneString, currentObjectName);
                iDfSysObject.setObjectName(massagedObjectName);
                iDfSysObject.save();
		    }

		    if(m_idList.size() > 0)
		    {
				IDfSysObject iDfSysObjectBatch = (IDfSysObject)getDfSession().getObject(new DfId(m_strObjectId));
				if(m_batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
				       m_batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
				{
					iDfSysObjectBatch.setString("p_reentry_activity", HelperClassConstants.WF_INITIALIZE_BATCH);
					iDfSysObjectBatch.save();
					HelperClass.porticoOutput(0, "PruneObject - pruneObjectNames setting Batch p_reentry_activity to="+HelperClassConstants.WF_INITIALIZE_BATCH);
				}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput("PruneObject - pruneObjectNames Exception="+e.getMessage());
		}
		finally
		{
		}
	}

	private String getMassagedObjectName(String pruneString, String currentObjectName)
	{
		String massagedObjectName = currentObjectName;
		try
		{
            if(pruneString != null && pruneString.length() > 0 &&
                   currentObjectName != null && currentObjectName.length() > 0)
            {
		        boolean startsWithPruneString = currentObjectName.startsWith(pruneString);
		    	if(startsWithPruneString)
		    	{
		             massagedObjectName = 	currentObjectName.substring(pruneString.length());
		             HelperClass.porticoOutput("PruneObject - getMassagedObjectName massagedObjectName="+massagedObjectName);
		        }
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput("PruneObject - getMassagedObjectName prunestring,currentObjectName="+pruneString+","+currentObjectName);
			HelperClass.porticoOutput("PruneObject - getMassagedObjectName Exception="+e.getMessage());
		}
		finally
		{
		}
		return massagedObjectName;
	}

    private void populatePruneList()
    {
		HelperClass.porticoOutput("PruneObject-populatePruneList-called");
		m_idList.clear();
		m_pruneList.clear();
        String attrNames = "r_object_type,r_object_id,object_name";
        IDfCollection tIDfCollection = null;
        try
        {
			IDfFolder idffolder = (IDfFolder)getDfSession().getObject(new DfId(m_strObjectId));
            tIDfCollection = idffolder.getContents(attrNames);
            if(tIDfCollection != null)
            {
		    	int leastTokenCount = -1;
		    	String leastTokenString = "";
                while(tIDfCollection.next())
                {
		        	IDfTypedObject tIDfTypedObject = tIDfCollection.getTypedObject();
		     		String objectType = tIDfTypedObject.getString("r_object_type");
		     		HelperClass.porticoOutput("PruneObject-populatePruneList-objectType="+objectType);
		            if(objectType.equals(DBHelperClass.RAW_UNIT_TYPE))
		            {
						HelperClass.porticoOutput("PruneObject-populatePruneList-(match)objectType="+objectType);
    	    	    	String objectId = tIDfTypedObject.getString("r_object_id");
		        		String objectName = tIDfTypedObject.getString("object_name");
		        		int tCount = new StringTokenizer(objectName, "/").countTokens();
		        		HelperClass.porticoOutput("PruneObject-populatePruneList-tCount="+tCount);
		        		HelperClass.porticoOutput("PruneObject-populatePruneList-objectName="+objectName);
		        		// tCount > 1 could potentially lead to scenario of object file names without a '/',
		        		//            that will affect the submission view pattern matching

						if((leastTokenCount == -1) || (tCount < leastTokenCount))
						{
							// leastTokenCount is the lowest no. of tokens
							leastTokenCount = tCount;
   		    			    leastTokenString = objectName; // ==== least 'dir/a.pdf' has 2 times token
   		    			                                   // ==== least 'a.pdf' has 1 time token, we do not want this
						}

/*
		        		if(tCount > 2) // or tCount > 2 because object file names without '/' could be a problem in submission view
		        		{
						}
*/
		    			m_idList.add(objectId);
		    		}
		        }
                // Ranga test modify to 'leastTokenCount >= 2' instead of 'leastTokenCount > 2'
    		    if(!leastTokenString.equals("") && leastTokenCount >= 2) // leastTokenCount > 2 because object file names without '/' could be a problem in submission view
    		    {
					HelperClass.porticoOutput("PruneObject-populatePruneList-leastTokenString="+leastTokenString);
					HelperClass.porticoOutput("PruneObject-populatePruneList-leastTokenCount="+leastTokenCount);
    			    createPruneList(leastTokenString);
    			}

    		}

    		m_batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), m_strObjectId);
	    }
	    catch(Exception e)
	    {
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput("PruneObject-populatePruneList-CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput("Exception in PruneObject-populatePruneList-close" + e.getMessage());
			}

		}
	}

	public String getCurrentBatchStatus()
	{
		return m_batchStatus;
	}

	private void createPruneList(String leastTokenString)
	{
        HelperClass.porticoOutput("PruneObject-createPruneList-leastTokenString=" + leastTokenString);
	    String massagedLeastTokenString = leastTokenString.substring(0, leastTokenString.lastIndexOf("/"));
	    // start for submission view issue overcoming
	    // Ranga test commented start
	    // massagedLeastTokenString = massagedLeastTokenString.substring(0, massagedLeastTokenString.lastIndexOf("/"));
	    // Ranga test commented end
	    // end for submission view issue overcoming
	    HelperClass.porticoOutput("PruneObject-createPruneList-massagedLeastTokenString=" + massagedLeastTokenString);
		StringTokenizer tStringTokenizer = new StringTokenizer(massagedLeastTokenString, "/");
		String prune = "";
		while (tStringTokenizer.hasMoreTokens())
		{
			String token = tStringTokenizer.nextToken();
			prune = prune + token + "/";
			m_pruneList.add(prune);
		    HelperClass.porticoOutput("PruneObject-createPruneList-prune=" + prune);
		}
	}

	public ArrayList getPruneList()
	{
		return m_pruneList;
	}

    private String m_strObjectId;
    private String m_strObjectType;
    private ArrayList m_pruneList;
    private ArrayList m_idList;
    private String m_batchStatus;
}