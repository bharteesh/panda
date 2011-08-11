/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             FastTrackProgressResultSet.java
 * Created on       Jan 11, 2008
 *
 */
package org.portico.conprep.ui.fasttrackprogress;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.form.control.databound.IDataboundParams;
import com.documentum.web.form.control.databound.TableResultSet;

/**
 * Description  Fetches and hold data for FastTrackProgress as a tableresultset object
 * Author       srn
 * Type         FastTrackProgressResultSet
 */

 /*
	public static final String colNames[] = {
		"r_object_id",
		"r_act_seqno",
		"object_name",
		"r_runtime_state",
		"r_creation_date",
		"complete_date",
		"r_performer_name",
		"sortKey",
	};


 */

public class FastTrackProgressResultSet
{
    public FastTrackProgressResultSet(String batchfolderId, String batchStatus, String batchName, IDfSession dfSession) throws DfException, Exception
    {
        m_batchfolderId = batchfolderId;
        m_dfSession = dfSession;
		m_batchstatus = batchStatus;
		m_batchname = batchName;
        allRowData = new ArrayList();

        String displayErrorMsg = "";

        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-loadInfo-Start");
        loadInfo();
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-loadInfo-End");
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-createRowsForTableResultSet-Start");
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-createRowsForTableResultSet-End");
        setTableResultSet(new TableResultSet(allRowData,FastTrackProgress.colNames));

        //    HelperClass.porticoOutput(0, "Not valid for Processing="+displayErrorMsg);
        //    displayError(displayErrorMsg);
        //    setTableResultSet(new TableResultSet(allRowData,ProbResReport.colNames));
    }


// Currently not excercised
    public void displayError(String errorMsg)
    {
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-displayError-Start");
		try
		{
            ArrayList currentRow = new ArrayList();
            currentRow.add("");
            currentRow.add("");
            currentRow.add(errorMsg);
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add(""); // Decides the order of display(2 of 2)
    		printMe(currentRow);
            allRowData.add(currentRow);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-FastTrackProgressResultSet-displayError():"+e.getMessage());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-displayError-End");
	}

    /**
     * @return
     */
    public TableResultSet getTableResultSet()
    {
        return tableResultSet;
    }

    /**
     * @param set
     */
    public void setTableResultSet(TableResultSet set) throws Exception
    {
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet- setTableResultSet Start");
        tableResultSet = set;
    	// Decides the order of display(1 of 2)
        tableResultSet.sort(FastTrackProgress.colNames[7],
                                IDataboundParams.SORTDIR_FORWARD,
                                IDataboundParams.SORTMODE_NUMERIC); // 0.0,1.0,....,10.0,11.0,...


        HelperClass.porticoOutput(0, "FastTrackProgressResultSet- setTableResultSet End");
    }

    public void clearData()
    {
        if(tableResultSet!=null)
        {
            tableResultSet.close();
		}
        if(allRowData!=null)
        {
            allRowData.clear();
		}
    }

    public void loadInfo()
    {
		// UserMessage Hash
		listData = new Hashtable();

		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-listData-Start");

        IDfCollection tIDfCollection = null;
		try
		{
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT w.r_object_id, " +
                                        " w.r_workflow_id, " +
                                        " w.r_act_seqno, " +
								 	    " w.r_performer_name, " +
								 	    " w.r_creation_date, " +
								 	    " w.r_runtime_state, " +
								 	    " w.r_act_def_id, " +
								 	    " a.object_name" +
								 " FROM dmi_workitem w, dm_activity a " +
								 " WHERE w.r_workflow_id IN " +
								 " (SELECT distinct r_workflow_id FROM dmi_package WHERE ANY r_component_id = "+
								 "'" + m_batchfolderId + "')" +
								 " AND w.r_act_def_id = a.r_object_id " +
								 " ORDER BY w.r_act_seqno asc";
            HelperClass.porticoOutput(0, "Timing FastTrackProgressResultSet-loadInfo dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "Timing FastTrackProgressResultSet-loadInfo Completed query execute");

            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					ArrayList currentRow = new ArrayList();
					String r_object_id = tIDfCollection.getString("r_object_id");
	    			currentRow.add(r_object_id);

               		String r_act_seqno = tIDfCollection.getString("r_act_seqno");
	    			currentRow.add(r_act_seqno);

		            String object_name = tIDfCollection.getString("object_name");
	    			currentRow.add(object_name);

		            String r_runtime_state = getRunTimeStateDisplayValue(tIDfCollection.getString("r_runtime_state"));
	    			currentRow.add(r_runtime_state);

		            String r_creation_date = tIDfCollection.getString("r_creation_date");
	    			currentRow.add(r_creation_date);

		            String complete_date = "";
	    			currentRow.add(complete_date);

		            String r_performer_name = tIDfCollection.getString("r_performer_name");
	    			currentRow.add(r_performer_name);

	    			String displaySortKey = r_act_seqno;
	    			currentRow.add(displaySortKey);

	    			allRowData.add(currentRow);
               	}
            }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-FastTrackProgressResultSet-loadInfo():"+e.getMessage());
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
				HelperClass.porticoOutput(1, "Exception-FastTrackProgressResultSet-loadInfo-tIDfCollection.close():"+e.getMessage());
			}
		}
	}

/*
0, meaning dormant
1, meaning acquired
2, meaning finished
3, meaning paused
4, meaning Dpaused (a workitem
in the dormant state is paused)
5, meaning Apaused (a workitem
in the acquired state is paused)
6, meaning Ppaused (a workitem
in the paused state is paused)
*/
	public String getRunTimeStateDisplayValue(String intValue)
	{
		String displayValue = "Unknown";

        if(intValue != null)
        {
// 0 or 0.0 ?
	    	if(intValue.equals("0"))
	    	{
	    		displayValue = "Pending"; // === "Dormant";
	    	}
	    	else if(intValue.equals("1"))
	    	{
	    		displayValue = "Acquired";
	    	}
	    	else if(intValue.equals("2"))
	    	{
	    		displayValue = "Complete"; // ==== "Finished";
	    	}
	    	else if(intValue.equals("3"))
	    	{
	    		displayValue = "Paused";
	    	}
	    	else if(intValue.equals("4"))
	    	{
	    		displayValue = "D Paused";
	    	}
	    	else if(intValue.equals("5"))
	    	{
	    		displayValue = "A Paused";
	    	}
	    	else if(intValue.equals("6"))
	    	{
	    		displayValue = "P Paused";
	    	}
	    }

	    return displayValue;
	}

	public void printMe(ArrayList list)
	{
		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-printMe(Start)-------------");

		if(list != null && list.size() > 0)
		{
			for(int indx=0; indx < list.size(); indx++)
			{
				Object obj = list.get(indx);
                HelperClass.porticoOutput(0, "FastTrackProgressResultSet-printMe="+obj.toString());
			}
		}

		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-printMe(End)-------------");
	}


// All static methods are below, to be self - contained in the parameters, must not use any pRResultSet instance
// Checked self-contained
// Move to probresreport.java/qc.java
    public static String getBatchInfo(IDfSession session, String batchObjId, String batchStat) throws Exception
    {
		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-getLookupServiceName - Start getBatchInfo for batchObjId="+batchObjId);
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        String rtnVal="";
        try
        {
            String gMC="";
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery();
            sb.append("select object_name,p_provider_id,p_profile_id,p_state,p_last_activity,p_workflow_template_name from p_batch where r_object_id='");
            sb.append(batchObjId);
            sb.append("'");
            q.setDQL(sb.toString());
            idfcollection = q.execute(session, IDfQuery.DF_READ_QUERY);
            while(idfcollection.next())
            {
                  String objectName=idfcollection.getString("object_name");
                  String providerId=idfcollection.getString("p_provider_id");
                  String temp1=getLookupServiceName("provider",providerId);
                  HelperClass.porticoOutput(0, "providerId="+providerId+" temp1="+temp1);
                  String profileId=idfcollection.getString("p_profile_id");
                  String temp2=getLookupServiceName("profile",profileId);
                  HelperClass.porticoOutput(0, "profileId="+profileId+" temp2="+temp2);
                  String lastActivity=idfcollection.getString("p_last_activity");
                  String temp3="<br/>Last activity: "+lastActivity;
                  String wfTempNm=idfcollection.getString("p_workflow_template_name");
                  String temp4="<br/>Workflow template name: "+wfTempNm;

                  rtnVal="Batch: "+objectName+"<br/>Provider: "+temp1+"<br/>Profile: "+temp2;
                  rtnVal=rtnVal.concat(gMC);
                  rtnVal=rtnVal.concat(temp3);
                  rtnVal=rtnVal.concat(temp4);
            }
        }
        catch (Exception e)
        {
			HelperClass.porticoOutput(1, "Exception in FastTrackProgressResultSet-getBatchInfo" + e.getMessage());
            throw e;
        }
        finally
        {
            try
            {
                if(idfcollection!=null)
                {
                    idfcollection.close();
				}
                idfcollection = null;
            }
            catch (DfException e2)
            {
                throw e2;
            }
            sb.delete(0,sb.capacity());
            idfcollection=null;
        }

		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-getLookupServiceName - End getBatchInfo");

        return(rtnVal);
    }

// Checked self-contained
    public static String getLookupServiceName(String lookupService, String serviceId)
    {
		HelperClass.porticoOutput(0, "FastTrackProgressResultSet-getLookupServiceName - Start (serviceId)="+serviceId);
        String retServiceName = "";
        ValuePair tValuePair = null;
        ArrayList attrList = new ArrayList();
        attrList.add("name");
        ArrayList outList = HelperClass.lookupServiceInfo(lookupService, serviceId, attrList);
        if(outList != null && outList.size() > 0)
        {
            for(int indx=0; indx < outList.size(); indx++)
            {
                tValuePair = (ValuePair)outList.get(indx);
                retServiceName = (String)tValuePair.getValue();
                break;
            }
        }
        HelperClass.porticoOutput(0, "FastTrackProgressResultSet-getLookupServiceName - End (serviceId)="+serviceId);
        return retServiceName;
    }


    Hashtable listData;
    List allRowData;

    TableResultSet tableResultSet;
    public String m_batchfolderId;
    public String m_batchstatus;
    private IDfSession m_dfSession;
    private String m_batchname;
    private static final String XML_TAG="xml";
}
