/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             QCResultSet.java
 * Created on       Feb 15, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.form.control.databound.TableResultSet;

/**
 * Description  Fetches and hold data for QC report as a collection object
 *              this class has three dql calls, in the foll order
 *                  1. fetch all user message object ids for this batch
 *                  2. fetch all cu object ids under this batch
 *                  3. fetch all objects under this batch, to identify unprocessed orphaned ones
 * Author       wjh
 * Type         QCResultSet
 */
public class QCResultSet {

    /**
     * fetches list of user messages associated with this batch
     */
    public QCResultSet(String batchfolderId, String batchStatus, String reportType, IDfSession dfSession) throws DfException, Exception {
        m_batchfolderId =   batchfolderId;
        m_dfSession = dfSession;
        m_batchstatus = batchStatus;
        m_ReportType = reportType;

        HelperClass.porticoOutput(0, "ConPrep UI ....QCResultSet- loadDynamicAttribute-Start");
        // One time call start
        m_isProcessable = loadDynamicAttribute();
        HelperClass.porticoOutput(0, "ConPrep UI ....QCResultSet- loadDynamicAttribute-End-m_isProcessable="+m_isProcessable);
        // One time call end
        HelperClass.porticoOutput(0, "QcResultSet-setReportType-buildTreeAndSetTableResultSet-Start");
        buildTreeAndSetTableResultSet();
        HelperClass.porticoOutput(0, "QcResultSet-setReportType-buildTreeAndSetTableResultSet-End");

/*
        if(m_isProcessable == true)
        {
            HelperClass.porticoOutput(0, "QcResultSet-setReportType-buildTreeAndSetTableResultSet-Start");
            buildTreeAndSetTableResultSet();
            HelperClass.porticoOutput(0, "QcResultSet-setReportType-buildTreeAndSetTableResultSet-End");
        }
        else
        {
            HelperClass.porticoOutput(0, ERROR_IN_DATA);
            displayErrorAndSetTableResultSet(ERROR_IN_DATA);
        }
*/
    }

    /**
     * @return
     */
    public TableResultSet getTableResultSet() {
        return tableResultSet;
    }

    /**
     * @param set
     */
    public void setTableResultSet(TableResultSet set) throws Exception {
        tableResultSet = set;
/*
        if(m_batchstatus.equals("")) getBatchInfo(m_dfSession,m_batchfolderId);
        if(m_batchstatus!=null && !m_batchstatus.equals(HelperClassConstants.INSPECTING) && !m_batchstatus.equals(HelperClassConstants.INSPECT)) {tableResultSet.sort(QC.colNames[4],0,1);
        } else {
            tableResultSet.sort(QC.colNames[7],0,1);
        }
*/
    }

    public void clearData(){
        if(tableResultSet!=null)
            tableResultSet.close();
        if(allRowData!=null)
            allRowData.clear();
    }

    public void displayErrorAndSetTableResultSet(String errorMsg)
    {
        HelperClass.porticoOutput(0, "QCResultSet-displayErrorAndSetTableResultSet-Start");
		try
		{
			allRowData.clear();
            ArrayList currentRow = new ArrayList();
            currentRow.add("");
            currentRow.add(errorMsg);
            currentRow.add("");
            currentRow.add("");
            currentRow.add(""); // errorMsg
            currentRow.add("");
            currentRow.add("");
            currentRow.add(""); // data_set_sort_key
            currentRow.add("");
            currentRow.add("false");
    		printMe(currentRow);
            allRowData.add(currentRow);
            setTableResultSet(new TableResultSet(allRowData,QC.colNames));
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QCResultSet-displayErrorAndSetTableResultSet():"+e.toString());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QCResultSet-displayErrorAndSetTableResultSet-End");
	}

    public String getSuObjId(String suStateObjId) throws Exception
    {
		HelperClass.porticoOutput(0, "QCResultSet-getSuObjId-Start-suStateObjId="+suStateObjId);

    	String suObjId="";
        try
        {
			if(suStateObjId != null && !suStateObjId.equals(""))
			{
				if(listObjectStateObjectMapping.containsKey(suStateObjId))
				{
				    suObjId = (String)listObjectStateObjectMapping.get(suStateObjId);
				}
				else
				{
					HelperClass.porticoOutput(1, "Error QCResultSet-getSuObjId-listObjectStateObjectMapping does not contain suStateObjId="+suStateObjId);
				}
		    }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
        }

		HelperClass.porticoOutput(0, "QCResultSet-getSuObjId-End-suStateObjId,suObjectId="+suStateObjId+","+suObjId);

        return suObjId;
    }

    public boolean isVirusChecked(String objId)
	{
		return true;
	}

/*
    public boolean isVirusChecked(String objId)
	{
		boolean isVirusCheckedObject = false;
		boolean isVirusScanned = false;
		boolean isCreatedByWorkFlow = false;

		HelperClass.porticoOutput(0, "QcResultSet-isVirusChecked - Start for suStateId="+objId);

		try
		{
			if(listObjectStateAttributeValueMapping != null && listObjectStateAttributeValueMapping.size() > 0)
			{
				Object objectItem = null;

				if(listObjectStateAttributeValueMapping.containsKey(objId))
				{
			        objectItem = listObjectStateAttributeValueMapping.get(objId);
			    }

				ArrayList objectItemList = null;
    			if(objectItem != null)
				{
				    objectItemList = (ArrayList)objectItem;
    			    if(objectItemList != null && objectItemList.size() > 0)
    			    {
    			        for(int oindx=0; oindx < objectItemList.size(); oindx++)
    			        {
    			        	ValuePair valuePair = (ValuePair)objectItemList.get(oindx);
                            if(valuePair.getKey().equals(DBHelperClass.P_VIRUS_SCANNED))
                            {
    			        		isVirusScanned = Boolean.parseBoolean(valuePair.getValue());
    			        	}
                            else if(valuePair.getKey().equals(DBHelperClass.P_IS_CREATED_BY_WORKFLOW))
                            {
    			        		isCreatedByWorkFlow = Boolean.parseBoolean(valuePair.getValue());
    			        	}
         		    	}
				    }
				}
			    else
			    {
				    HelperClass.porticoOutput(1, "Error QcResultSet-isVirusChecked-No listObjectStateAttributeValueMapping mapping for objId="+objId);
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QcResultSet-isVirusChecked" + e.toString());
		}
		finally
		{
		}

		// isVirusCheckedObject = isVirusScanned || isCreatedByWorkFlow;
		isVirusCheckedObject = isCreatedByWorkFlow;

		HelperClass.porticoOutput(0, "QcResultSet-isVirusChecked-object(value)=" + objId +"("+isVirusCheckedObject+")");

		return isVirusCheckedObject;
    }
*/

    public String getBatchInfo(String bObjId) throws Exception
    {
        HelperClass.porticoOutput(0, "QcResultSet-getBatchInfo-Started-batchObjectId=" + bObjId);

        String rtnVal="";
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        try
        {
            String gMC="";
            Integer i=new Integer(getActiveUserMessageCount());
            String sCnt=i.toString();
            gMC="<br/>Active message count: "+sCnt;
            sb.append("select object_name,p_provider_id,p_profile_id,p_state,p_workflow_template_name from p_batch where r_object_id='");
            sb.append(bObjId);
            sb.append("'");
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL(sb.toString());
            idfcollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            while(idfcollection.next())
            {
                  String objectName=idfcollection.getString("object_name");
                  String providerId=idfcollection.getString("p_provider_id");
                  String temp1=getLookupServiceName("provider",providerId);
                  HelperClass.porticoOutput(0, "providerId="+providerId+" temp1="+temp1);
                  String profileId=idfcollection.getString("p_profile_id");
                  String temp2=getLookupServiceName("profile",profileId);
                  HelperClass.porticoOutput(0, "profileId="+profileId+" temp2="+temp2);
                  String wfTempNm=idfcollection.getString("p_workflow_template_name");
                  String temp3="<br/>Workflow template name: "+wfTempNm;
                  rtnVal="Batch: "+objectName+"<br/>Provider: "+temp1+"<br/>Profile: "+temp2;
                  rtnVal=rtnVal.concat(temp3);
                  rtnVal=rtnVal.concat(gMC);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                if(idfcollection!=null)
                    idfcollection.close();
                idfcollection = null;
            }
            catch (Exception e2)
            {
                throw e2;
            }
            sb.delete(0,sb.capacity());
            idfcollection=null;
        }

        HelperClass.porticoOutput(0, "QcResultSet-getBatchInfo-Ended-batchObjectId=" + bObjId+","+
                                                                       "rtnVal="+rtnVal);

        return(rtnVal);
    }

    public String getLookupServiceName(String lookupService, String serviceId)
    {
        HelperClass.porticoOutput(0, "QcResultSet-getLookupServiceName-Started-lookupService="+lookupService+","+
                                                                               "serviceId="+serviceId);
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
        HelperClass.porticoOutput(0, "QcResultSet-getLookupServiceName-Started-lookupService="+lookupService+","+
                                                                               "serviceId="+serviceId+","+
                                                                               "retServiceName="+retServiceName);
        return retServiceName;
    }

    public int getTotalArticleCount()
    {
		int count = 0;
		if(listExpandCuMapping != null)
		{
			count = listExpandCuMapping.size();
	    }

		return count;
	}

    public int getInspectArticleCount()
    {
		int count = 0;
		if(listInspectCuMapping != null)
		{
			count = listInspectCuMapping.size();
	    }

		return count;
	}

	public int getActiveUserMessageCount()
	{
		int count = 0;
		if(listUserMessages != null)
		{
			count = listUserMessages.size();
	    }

		return count;
    }

    public void buildTreeAndSetTableResultSet()
    {
		try
		{
            if(m_isProcessable == true)
            {
    			// Clear the existing row(s)
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-clear allRows() Start");
			    allRowData.clear();
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-clear allRows() End");
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-insertSingleRow for Batch Start");
			    // insert a row for the Batch, this is to retain the functionality of the old QC Report
			    insertSingleRow(m_batchfolderId);
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-insertSingleRow for Batch End");
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-buildTree-Start");
                buildTree(); // This populates the allRowData
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-buildTree-End");
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-appendMessages-Start");
    // File ref to be taken care of
                appendMessages();
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-appendMessages-End");
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-setTableResultSet-Start");
                setTableResultSet(new TableResultSet(allRowData,QC.colNames));
                HelperClass.porticoOutput(0, "QcResultSet-buildTreeAndSetTableResultSet-setTableResultSet-End");
		    }
		    else
		    {
                HelperClass.porticoOutput(0, ERROR_IN_DATA);
                displayErrorAndSetTableResultSet(ERROR_IN_DATA);
			}
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcResultSet-buildTreeAndSetTableResultSet="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}
	}

    public void setReportType(String reportType)
    {
		try
		{
		    m_ReportType = reportType;
	    }
	    catch(Exception e)
	    {
	        HelperClass.porticoOutput(1, "Exception in QcResultSet-setReportType="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}
	}

    public String getReportType()
    {
		return m_ReportType;
	}

    public boolean setObjectInspectionStatus(String objectId, boolean checkedValue)
    {
		boolean isSuccessful = true;

		HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus(Start)-articleId,checkedValue="+objectId+","+checkedValue);

		try
		{
			isSuccessful = DBHelperClass.setArticleInspectionStatus(objectId, checkedValue);
/*
            DfQuery dfquery = new DfQuery();
            String dqlString = "UPDATE " + "p_cu_state" + " OBJECTS " +
                                   " SET p_inspected=" + checkedValue +
                                   " where r_object_id=" +
                                   "'" + objectId + "'";

            HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus()-dqlString="+dqlString);
     		dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_EXEC_QUERY);
            if(tIDfCollection != null)
            {
   		    	tIDfCollection.close();
    	    }
*/
            if(isSuccessful == true)
            {
				HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus()-DB Update Successful for objectId,checkedValue="+objectId+","+checkedValue);
    	        if(listInspectCuMapping.containsKey(objectId))
    	        {
			    	listInspectCuMapping.remove(objectId);
			    	listInspectCuMapping.put(objectId, Boolean.valueOf(checkedValue));
			    	HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus()-listInspectCuMapping updated(Hash) objectId,checkedValue="+objectId+","+checkedValue);
			    }
			    else
			    {
			    	HelperClass.porticoOutput(1, "Error in QcResultSet-setObjectInspectionStatus-objectId not in listInspectCuMapping="+objectId);
			    }
		    }
		    else
		    {
				HelperClass.porticoOutput(1, "Error in QcResultSet-setObjectInspectionStatus-DB Update Failed for articleId="+objectId);
			}
		}
		catch(Exception e)
		{
			isSuccessful = false;
	        HelperClass.porticoOutput(1, "Exception in QcResultSet-setObjectInspectionStatus="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus(End)-articleId,checkedValue,isSuccessful="+objectId+","+checkedValue+","+isSuccessful);

		return isSuccessful;
	}

	public boolean getObjectInspectionState(String objectId)
	{
		boolean objectInspectionState = false;

   	    if(listInspectCuMapping.containsKey(objectId))
   	    {
			Boolean bool = (Boolean)listInspectCuMapping.get(objectId);
			objectInspectionState = bool.booleanValue();
		}
		else
		{
			HelperClass.porticoOutput(1, "QcResultSet-getObjectInspectionState-objectId not in listInspectCuMapping="+objectId);
		}

		HelperClass.porticoOutput(0, "QcResultSet-setObjectInspectionStatus-objectId,objectInspectionState="+objectId+","+objectInspectionState);

		return objectInspectionState;
	}

    public boolean setObjectExpandStatus(String objectId)
    {
		boolean retVal = true;
		try
		{
    	    if(listExpandCuMapping.containsKey(objectId))
    	    {
				Boolean bool = (Boolean)listExpandCuMapping.remove(objectId);
				boolean existingValueInMap = bool.booleanValue();
				listExpandCuMapping.put(objectId, Boolean.valueOf(!existingValueInMap));
				HelperClass.porticoOutput(0, "QcResultSet-setObjectExpandStatus()-listExpandCuMapping updated objectId,existingValueInMap="+objectId+","+existingValueInMap);
			}
			else
			{
				HelperClass.porticoOutput(1, "Error in QcResultSet-setObjectExpandStatus-objectId not in listExpandCuMapping="+objectId);
			}
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in QcResultSet-setObjectExpandStatus="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
            HelperClass.porticoOutput(0, "QcResultSet-setObjectExpandStatus-Call-finally");
		}

		return retVal;
	}


    public boolean loadDynamicAttribute()
    {
        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute - Start");

        boolean isProcessable = true;

		listObjectStateParentMapping = new Hashtable();
		listInspectCuMapping = new Hashtable();
		listExpandCuMapping = new Hashtable();
		listObjectStateAttributeValueMapping = new Hashtable();
		listObjectStateObjectMapping = new Hashtable();
		ValuePair valuePair = null;

		try
		{

            HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute-getBatchAccessionIdFromBatchId-Start-batchObjectId="+m_batchfolderId);
    		String batchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(m_batchfolderId);
            HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute-getBatchAccessionIdFromBatchId-End-batchObjectId,batchAccessionId="+m_batchfolderId+","+batchAccessionId);

	        ArrayList cuStateList = new ArrayList(); // List of CU States
			// listObjectAttribute has (keyObjectId, hashAttributes)
			// listObjectStateParentMapping has (key=parentId, value=ArrayList-children objectId)
			Hashtable listObjectAttribute = DBHelperClass.getContentTreeData(m_batchfolderId,
			                                                                 null, // Pull for all articles
	                                                                  cuStateList,
	                                                                  listObjectStateParentMapping);

            HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute Completed query execute");
/*
            String attrNames = "i_folder_id,r_object_id,object_name,r_object_type";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM dm_sysobject where r_object_type IN ("+
                               "'"+HelperClass.getInternalObjectType("cu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("fu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("su_state")+"'"+
                                ")" + " AND " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
*/
            if(cuStateList == null || cuStateList.size() == 0)
            {
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error QcResultSet-loadDynamicAttribute-cuStateList is Empty");
			}
            if(listObjectStateParentMapping == null || listObjectStateParentMapping.size() == 0)
            {
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error QcResultSet-loadDynamicAttribute-listObjectStateParentMapping is Empty");
			}
            if(listObjectAttribute == null || listObjectAttribute.size() == 0)
            {
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error QcResultSet-loadDynamicAttribute-listObjectAttribute is Empty");
			}
			if(batchAccessionId == null || batchAccessionId.equals(""))
			{
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error QcResultSet-loadDynamicAttribute-batchAccessionId is NULL");
			}

			if(isProcessable == true)
			{
				ArrayList tBatchChildren = new ArrayList();
				ArrayList tOrphanedChildren = new ArrayList();

				// This would normally be the cuStateId(s)
				if(listObjectStateParentMapping.containsKey(batchAccessionId))
				{
					tBatchChildren = (ArrayList)listObjectStateParentMapping.get(batchAccessionId);
					listObjectStateParentMapping.remove(batchAccessionId);
				}
				// Orphaned objects, append them at the end to other Batch children
				if(listObjectStateParentMapping.containsKey(DBHelperClass.UNKNOWN))
				{
					tOrphanedChildren = (ArrayList)listObjectStateParentMapping.get(DBHelperClass.UNKNOWN);
					listObjectStateParentMapping.remove(DBHelperClass.UNKNOWN);
					if(tOrphanedChildren != null && tOrphanedChildren.size() > 0)
					{
						tBatchChildren.addAll(tOrphanedChildren);
					}
				}
				if(tBatchChildren != null && tBatchChildren.size() > 0)
				{
					listObjectStateParentMapping.put(m_batchfolderId, tBatchChildren);
				}

                ArrayList alistBatch = new ArrayList();
    			valuePair = new ValuePair();

        		valuePair = new ValuePair();
                valuePair.setKey(DBHelperClass.P_NAME);
                valuePair.setValue(HelperClass.getObjectName(m_dfSession, m_batchfolderId, DBHelperClass.BATCH_TYPE));
                alistBatch.add(valuePair);

                valuePair.setKey(DBHelperClass.P_DISPLAY_LABEL);
                valuePair.setValue("thisBatch");
                alistBatch.add(valuePair);

        		valuePair = new ValuePair();
                valuePair.setKey(DBHelperClass.P_OBJECT_TYPE);
                valuePair.setValue(DBHelperClass.BATCH_TYPE);
                alistBatch.add(valuePair);
                listObjectStateAttributeValueMapping.put(m_batchfolderId, alistBatch);

                Enumeration listObjectAttributeEnumerate = listObjectAttribute.keys();
                while(listObjectAttributeEnumerate.hasMoreElements())
                {
					String currentObjectId = (String)listObjectAttributeEnumerate.nextElement();
					Hashtable attrPerObjectHash = (Hashtable)listObjectAttribute.get(currentObjectId);

					if(attrPerObjectHash == null || attrPerObjectHash.size() == 0)
					{
						HelperClass.porticoOutput(1, "Error-QcResultSet-loadDynamicAttribute has empty attrPerObjectHash for objectAccessionId="+currentObjectId);
					}
					else
					{
    					ArrayList attrLevellist = new ArrayList();
    					String r_object_type = "";
    					String parentObjectId = "";
    					String object_name = "";
    					if(attrPerObjectHash.containsKey(DBHelperClass.P_OBJECT_TYPE))
    					{
							r_object_type = (String)attrPerObjectHash.get(DBHelperClass.P_OBJECT_TYPE);
                            if(r_object_type == null)
                            {
       				    		r_object_type = "";
       				    	}
       				    	else if(r_object_type.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
       				    	{
								// Usually it will not come here, since no children of Batch type will be passed
								// Just in case then,
								// Override the BatchAccessionId with Documentum batchObjectId
								currentObjectId = m_batchfolderId;
							}
      				    	valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_OBJECT_TYPE);
                            valuePair.setValue(r_object_type);
                            attrLevellist.add(valuePair);
						}
    				    if(attrPerObjectHash.containsKey(DBHelperClass.P_PARENT_ID))
    				    {
						   	parentObjectId = (String)attrPerObjectHash.get(DBHelperClass.P_PARENT_ID);
        				   	valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_PARENT_ID);
                            if(parentObjectId == null)
                            {
        				  		parentObjectId = "";
         				   	}
         				   	else
         				   	{
								// Override the BatchAccessionId with Documentum batchObjectId
								if(parentObjectId.equals(DBHelperClass.UNKNOWN) ||
								        parentObjectId.equals(batchAccessionId))
								{
									parentObjectId = m_batchfolderId;
								}
							}
                            valuePair.setValue(parentObjectId);
                            attrLevellist.add(valuePair);
				        }
   				        if(attrPerObjectHash.containsKey(DBHelperClass.P_NAME))
   				        {
					    	object_name = (String)attrPerObjectHash.get(DBHelperClass.P_NAME);
       				    	valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_NAME);
                            if(object_name == null)
                            {
       				    		object_name = "";
       				    	}
                            valuePair.setValue(object_name);
                            attrLevellist.add(valuePair);
			            }
                        if(r_object_type.equalsIgnoreCase(DBHelperClass.CU_TYPE))
                        {
       				        if(attrPerObjectHash.containsKey(DBHelperClass.P_DISPLAY_LABEL))
       				        {
   						    	String p_display_label = (String)attrPerObjectHash.get(DBHelperClass.P_DISPLAY_LABEL);
           				    	valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_DISPLAY_LABEL);
                                if(p_display_label == null)
                                {
           				    		p_display_label = "";
           				    	}
                                valuePair.setValue(p_display_label);
                                attrLevellist.add(valuePair);
   				            }
       				        if(attrPerObjectHash.containsKey(DBHelperClass.P_SORT_KEY))
       				        {
   						    	String p_sort_key = (String)attrPerObjectHash.get(DBHelperClass.P_SORT_KEY);
           				    	valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_SORT_KEY);
                                if(p_sort_key == null)
                                {
           				    		p_sort_key = "";
           				    	}
                                valuePair.setValue(p_sort_key);
                                attrLevellist.add(valuePair);
   				            }
        				    if(attrPerObjectHash.containsKey(DBHelperClass.P_INSPECTION_REQUIRED))
        				    {
    						    String p_inspection_required = (String)attrPerObjectHash.get(DBHelperClass.P_INSPECTION_REQUIRED);
                                if(p_inspection_required != null && p_inspection_required.equalsIgnoreCase(DBHelperClass.TRUE))
                                {
									String p_inspected = "false";
            				    	if(attrPerObjectHash.containsKey(DBHelperClass.P_INSPECTED))
            				    	{
            				        	p_inspected = (String)attrPerObjectHash.get(DBHelperClass.P_INSPECTED);
// RANGA - To be careful, we assume Oracle DB will have 'p_inspected'="N" by default, this will avoid potential
//         problems of getting away without inspecting
//         Also 'p_inspection_required' has to be set appropriately as well
                                        if(p_inspected != null && p_inspected.equalsIgnoreCase(DBHelperClass.TRUE))
                                        {
								        	p_inspected = "true";
								        }
								    }
							        listInspectCuMapping.put(currentObjectId, Boolean.valueOf(p_inspected));
             				    }
    				        }
    				        listExpandCuMapping.put(currentObjectId, Boolean.valueOf(false));
						}
						else if(r_object_type.equalsIgnoreCase(DBHelperClass.FU_TYPE))
						{
        				    if(attrPerObjectHash.containsKey(DBHelperClass.P_FU_TYPE))
        				    {
    					     	String p_fu_type = (String)attrPerObjectHash.get(DBHelperClass.P_FU_TYPE);
            			    	valuePair = new ValuePair();
            			    	// Note: Value of 'p_fu_type' is used as the display_label
                                valuePair.setKey(DBHelperClass.P_DISPLAY_LABEL);
                                if(p_fu_type == null)
                                {
            			    		p_fu_type = "";
             			    	}
                                valuePair.setValue(p_fu_type);
                                attrLevellist.add(valuePair);
    				        }
						}
						else if(r_object_type.equalsIgnoreCase(DBHelperClass.SU_TYPE))
						{
        				    if(attrPerObjectHash.containsKey(DBHelperClass.P_WORK_FILENAME))
        				    {
    						    String workfilename = (String)attrPerObjectHash.get(DBHelperClass.P_WORK_FILENAME);
            				    valuePair = new ValuePair();
            			    	// Note: Value of 'workfilename' is used as the display_label
                                valuePair.setKey(DBHelperClass.P_DISPLAY_LABEL);
                                if(workfilename == null)
                                {
            				    	workfilename = "";
             				    }
                                valuePair.setValue(workfilename);
                                attrLevellist.add(valuePair);
    				        }
        				    if(attrPerObjectHash.containsKey(DBHelperClass.P_VIRUS_SCANNED))
        				    {
    						    String p_virus_scanned = (String)attrPerObjectHash.get(DBHelperClass.P_VIRUS_SCANNED);
            				    valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_VIRUS_SCANNED);
                                if(p_virus_scanned == null || p_virus_scanned.equalsIgnoreCase(DBHelperClass.FALSE))
                                {
            				    	p_virus_scanned = "false";
             				    }
             				    else
             				    {
									p_virus_scanned = "true";
								}
                                valuePair.setValue(p_virus_scanned);
                                attrLevellist.add(valuePair);
    				        }
        				    if(attrPerObjectHash.containsKey(DBHelperClass.P_IS_CREATED_BY_WORKFLOW))
        				    {
    						    String p_is_created_by_workflow = (String)attrPerObjectHash.get(DBHelperClass.P_IS_CREATED_BY_WORKFLOW);
            				    valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_IS_CREATED_BY_WORKFLOW);
                                if(p_is_created_by_workflow == null || p_is_created_by_workflow.equalsIgnoreCase(DBHelperClass.FALSE))
                                {
            				    	p_is_created_by_workflow = "false";
             				    }
             				    else
             				    {
									p_is_created_by_workflow = "true";
								}
                                valuePair.setValue(p_is_created_by_workflow);
                                attrLevellist.add(valuePair);
    				        }
       				        if(attrPerObjectHash.containsKey(DBHelperClass.P_CONTENT_ID))
       				        {
   						    	String p_object_id = (String)attrPerObjectHash.get(DBHelperClass.P_CONTENT_ID);
           				    	valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_CONTENT_ID);
                                if(p_object_id == null)
                                {
           				    		p_object_id = "";
           				    	}
           				    	else
           				    	{
									listObjectStateObjectMapping.put(currentObjectId, p_object_id);
								}
                                valuePair.setValue(p_object_id);
                                attrLevellist.add(valuePair);
   				            }
					    }
                        listObjectStateAttributeValueMapping.put(currentObjectId, attrLevellist);
                        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute(currentObjectId,parentObjectId,object_name,r_object_type)="+currentObjectId+","+parentObjectId+","+object_name+","+r_object_type);
					}

                    // Parent = Batch, Children = CU State(s)
                    // Parent = CU State, Children = FU State(s)
                    // Parent = FU State, Children = SU State(s)
				}
			}
// Try using existing sort == CU 1/*
//                            CU 1/FU 2/*
//                            CU 1/FU 2/SU 7
//  orphan                    Z/FU 6
//  orphan                    Z/SU 10

		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QCResultSet-loadDynamicAttribute():"+e.toString());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttribute-End-isProcessable="+isProcessable);

		return isProcessable;
	}

    public void buildTree()
	{
		boolean isValid = true;

		HelperClass.porticoOutput(0, "QCResultSet-buildTree()-Start for m_batchfolderId="+m_batchfolderId);

		try
		{
			isValid = getChildren(m_batchfolderId);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QCResultSet-buildTree():"+e.toString());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "QCResultSet-buildTree()-End-for m_batchfolderId,isValid="+m_batchfolderId+","+isValid);
	}

// Pass the batchObjectId, the 'loadDynamicAttribute()' would have the parent 'BatchAccessionId' converted to 'BatchObjectId'
	public boolean getChildren(String objectId)
	{
		boolean isValid = true;
		try
		{
            if(listObjectStateParentMapping != null &&
                     listObjectStateParentMapping.size() > 0 &&
                     listObjectStateParentMapping.containsKey(objectId))
            {

				ArrayList childList = getSortedChildList(objectId);
				if(childList != null && childList.size() > 0)
				{
					for(int childindx=0; childindx < childList.size(); childindx++)
					{
						String childId = (String)childList.get(childindx);
						HelperClass.porticoOutput(0, "QCResultSet-Before buildNode()");
						printNode(objectId);
						printNode(childId);
						insertSingleRow(childId);
						HelperClass.porticoOutput(0, "QCResultSet-After buildNode()");
		            	getChildren(childId);
			        }
			    }
		    }
		}
		catch(Exception e)
		{
			isValid = false;
			HelperClass.porticoOutput(1, "Exception-QCResultSet-getChildren():"+e.toString());
		}
		finally
		{
		}

		return isValid;
	}

	public ArrayList getSortedChildList(String objectId)
	{
		ArrayList sortedChildList = new ArrayList();
        TreeMap sortedItemMap = new TreeMap();

		ArrayList unSortedChildList = (ArrayList)listObjectStateParentMapping.get(objectId);

		if(objectId.equals(m_batchfolderId))
		{
			for(int childindx=0; childindx < unSortedChildList.size(); childindx++)
			{
				String sortKeyForDisplay = "";
				String objectName = "";
				String sortKey = "";
			    String childId = (String)unSortedChildList.get(childindx);

				boolean isValidChildForTheReport = true;
				if(m_ReportType.equals("inspect") && !listInspectCuMapping.containsKey(childId))
				{
					isValidChildForTheReport = false;
				}

                if(isValidChildForTheReport == true)
                {
			        if(listObjectStateAttributeValueMapping != null &&
			            listObjectStateAttributeValueMapping.size() > 0 &&
			            listObjectStateAttributeValueMapping.containsKey(childId))
                    {
				    	// listInspectCuMapping
				        ArrayList childAttr = (ArrayList)listObjectStateAttributeValueMapping.get(childId);
				        for(int childattrindx=0; childattrindx < childAttr.size(); childattrindx++)
				        {
				        	ValuePair valuePair = (ValuePair)childAttr.get(childattrindx);
				        	String value = valuePair.getValue();
				        	String key = valuePair.getKey();
				        	if(key.equals(DBHelperClass.P_NAME))
				        	{
				        		objectName = value;
				        	}
				        	else if(key.equals(DBHelperClass.P_SORT_KEY))
				        	{
				        		sortKey = value;
				        	}
				        }
			        }
			        sortKeyForDisplay = objectName + sortKey;
			        sortedItemMap.put(sortKeyForDisplay, childId);
			        HelperClass.porticoOutput(0, "QCResultSet - getSortedChildList()-sortKeyForDisplay="+sortKeyForDisplay);
			    }
			}

			if(sortedItemMap != null && sortedItemMap.size() > 0)
			{
			    HelperClass.porticoOutput(0, "QCResultSet - getSortedChildList()-sortedItemMap has values");
			    Collection col = sortedItemMap.values();
                Iterator childIterator = col.iterator();
                while(childIterator.hasNext())
                {
					sortedChildList.add((String)childIterator.next());
				}
			}
		}
		else
		{
			// Blanket for all objects CU State, FU State, SU State
			sortedChildList.addAll(unSortedChildList);
			// This list contains CU State(s)
			// Currently we pass any object to it, to check if it's children have to be displayed or not,
			//           but checking goes for only the CU State(s)
			// Filter for CU State(s) only
			if(listExpandCuMapping.containsKey(objectId))
			{
				Boolean bool = (Boolean)listExpandCuMapping.get(objectId);
				if(bool.booleanValue() == false)
				{
					sortedChildList.clear();
                }
			}
		}

		return sortedChildList;
	}

    public void insertSingleRow(String currentObjectId) throws Exception
    {
		try
		{
    		ValuePair valuePair = null;
			String currentObjectType = "";
			String currentObjectName = "";
			String displayLabel = "";
			String currentDisplayObjectName = "";
			String parentObjectId = "";
			String isOrphaned = "false";

			if(listObjectStateAttributeValueMapping != null &&
			        listObjectStateAttributeValueMapping.size() > 0 &&
			        listObjectStateAttributeValueMapping.containsKey(currentObjectId))
            {
				ArrayList objectAttr = (ArrayList)listObjectStateAttributeValueMapping.get(currentObjectId);
				for(int objectattrindx=0; objectattrindx < objectAttr.size(); objectattrindx++)
				{
					valuePair = (ValuePair)objectAttr.get(objectattrindx);
					String value = valuePair.getValue();;
					String key = valuePair.getKey();
					if(key.equals(DBHelperClass.P_NAME))
					{
						currentObjectName = value;
					}
					else if(key.equals(DBHelperClass.P_DISPLAY_LABEL))
					{
						displayLabel = value;
					}
					else if(key.equals(DBHelperClass.P_OBJECT_TYPE))
					{
						currentObjectType = value;
					}
					else if(key.equals(DBHelperClass.P_PARENT_ID))
					{
						parentObjectId = value;
					}
				}
			}
			else
			{
				HelperClass.porticoOutput(0, "QCResultSet-insertSingleRow():child node NO mapping for objectId ="+currentObjectId);
			}

            if(currentObjectType.equalsIgnoreCase(DBHelperClass.FU_TYPE) || currentObjectType.equalsIgnoreCase(DBHelperClass.SU_TYPE))
            {
				if(currentObjectType.equalsIgnoreCase(DBHelperClass.SU_TYPE))
				{
					int findIndex = -1;
					if((findIndex = displayLabel.lastIndexOf("/")) != -1)
					{
						// Future just check if 'findIndex+1' is < p_work_filename.length
						if((findIndex+1) < displayLabel.length())
						{
					        displayLabel = displayLabel.substring(findIndex+1);
						}
				    }
				}
				if(!parentObjectId.equals(m_batchfolderId))
				{
					// Ranga check page after commenting this. Tested works fine
                    // currentDisplayObjectName = "&nbsp;&nbsp;";
			    }
			    else
			    {
					isOrphaned = "true";
    				// All orphans will be at the same level as the CU State displays
                    HelperClass.porticoOutput(0, "QCResultSet-insertSingleRow():orphaned objectId,currentObjectType ="+currentObjectId+","+currentObjectType);
				}
			}

            currentDisplayObjectName = currentDisplayObjectName + currentObjectName + "["+displayLabel+"]";

            ArrayList currentRow = new ArrayList();
            currentRow.add(currentObjectId);
            currentRow.add(currentDisplayObjectName);
            currentRow.add(currentObjectType);
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add(""); // data_set_sort_key
            currentRow.add("");
            currentRow.add(isOrphaned);
    		printMe(currentRow);
            if((m_batchstatus.equals(HelperClassConstants.INSPECTING) || m_batchstatus.equals(HelperClassConstants.INSPECT) || m_batchstatus.equals(HelperClassConstants.INSPECTED)) &&
                     currentObjectType.equalsIgnoreCase(DBHelperClass.FILE_REF_TYPE))
            {
                HelperClass.porticoOutput(0, "QCResultSet-DO NOT insertSingleRow");
		    }
		    else
		    {
                allRowData.add(currentRow);
                HelperClass.porticoOutput(0, "QCResultSet-insertSingleRow");
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in QCResultSet-insertSingleRow" + e.toString());
		}
		finally
		{
		}
	}

	public void appendMessages()
	{
		try
		{
			loadUserMessages();
	        if(listUserMessages != null && listUserMessages.size() > 0)
		    {
                Enumeration msgListEnumerate = listUserMessages.keys();
                while(msgListEnumerate.hasMoreElements())
                {
					Object msgHashKey = msgListEnumerate.nextElement();
                    String msgObjId = (String)msgHashKey;

		    		ArrayList msgItemList = (ArrayList)listUserMessages.get(msgHashKey);
		    		String p_context_id = "";
		    		String p_code = "";
		    		String p_text = "";
		    		String p_category = "";
		    		String p_severity = "";
		    		String content = "";
		    		for(int tindx=0; tindx < msgItemList.size(); tindx++)
		    		{
		    			ValuePair valuePair = (ValuePair)msgItemList.get(tindx);
                        if(valuePair.getKey().equals(DBHelperClass.P_CONTEXT_ID))
                        {
		    				p_context_id = valuePair.getValue();
		    			}
                        else if(valuePair.getKey().equals(DBHelperClass.P_CODE))
                        {
		    				p_code = valuePair.getValue();
		    			}
                        else if(valuePair.getKey().equals(DBHelperClass.P_TEXT))
                        {
		    				p_text = valuePair.getValue();
		    			}
                        else if(valuePair.getKey().equals(DBHelperClass.P_CATEGORY))
                        {
		    				p_category = valuePair.getValue();
		    			}
                        else if(valuePair.getKey().equals(DBHelperClass.P_SEVERITY))
                        {
		    				p_severity = getSeverityText(valuePair.getValue());
		    			}
                        else if(valuePair.getKey().equals(DBHelperClass.CONTENT))
                        {
		    				content = valuePair.getValue();
		    			}
		    		}

				    String object_name = "";
				    String r_object_type = "";
				    String p_sort_key = "";
				    Object objectItem = null;

				    if(listObjectStateAttributeValueMapping.containsKey(p_context_id))
				    {
				        objectItem = listObjectStateAttributeValueMapping.get(p_context_id);
				    }

				    ArrayList objectItemList = null;
    				if(objectItem != null)
				    {
				    	objectItemList = (ArrayList)objectItem;
    			    	if(objectItemList != null && objectItemList.size() > 0)
    			    	{
    			    	    for(int oindx=0; oindx < objectItemList.size(); oindx++)
    			    	    {
    			    	    	ValuePair valuePair = (ValuePair)objectItemList.get(oindx);
                                if(valuePair.getKey().equals(DBHelperClass.P_NAME))
                                {
    			    	    		object_name = valuePair.getValue();
    			    	    	}
                                else if(valuePair.getKey().equals(DBHelperClass.P_OBJECT_TYPE))
                                {
    			    	    		r_object_type = valuePair.getValue();
    			    	    	}
         		    		}
				    	}
					}
			        else
			        {
				    	HelperClass.porticoOutput(1, "Error QCResultSet-appendMessages-No object mapping for p_context_id="+p_context_id);
				    }
                    String currentDisplayObjectName = p_code + " " + p_text;
                    String contextDisplayLabel = getFormattedObjectName(p_context_id, object_name, r_object_type);
                    ArrayList currentRow = new ArrayList();
                    // Reuse of Id(s) since on the jsp if 2 Id(s) are identical, chance for potential grouping
                    // along with the content tree row ??
                    currentRow.add(p_context_id); // r_object_id
                    currentRow.add(currentDisplayObjectName);
                    currentRow.add(""); // r_object_type, DO NOT populate this, this will cause collision issues with Other Tree nodes on jsp
                    currentRow.add(p_code);
                    currentRow.add(contextDisplayLabel); // Text use for context Display Label
                    currentRow.add(p_severity);
                    currentRow.add(r_object_type); // Category used for r_object_type
                    currentRow.add(""); // data_set_sort_key
                    currentRow.add(msgObjId); // msgObjId
                    currentRow.add("false");
    	         	printMe(currentRow);
                    allRowData.add(currentRow);
                    HelperClass.porticoOutput(0, "QCResultSet-appendMessages-added row");
				}
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in QCResultSet-appendMessages" + e.toString());
		}
		finally
		{
		}
	}

	public String getSeverityText(String severityCode)
	{
		String severityText = severityCode;

	    if(severityCode.equals("0"))
	    {
	        severityText = "Info";
		}
	    else if(severityCode.equals("1"))
	    {
			severityText = "Warning";
		}
	    else if(severityCode.equals("2"))
	    {
			severityText = "Fatal";
		}
	    else
	    {
	        severityText = "Unknown";
	    }

	    return severityText;
	}

    public void loadUserMessages()
    {
		// RANGA, Future for 'listUserMessages' if sort to be maintained, use 'ArrayList' in place of Hashtable
		// So, that we can get away with tableResultSet.sort()
		// Performance Note: In the query 'orderByType' can be removed, if tableResultSet.sort() is used
		listUserMessages = new Hashtable();
		listMsgIdContextIdMapping = new Hashtable();
		ValuePair valuePair = null;

		HelperClass.porticoOutput(0, "QCResultSet-loadUserMessages-Start for m_batchfolderId="+m_batchfolderId);

		try
		{
			// Pick all the Message(s) order by severity 2(Fatal),1(Warning),0(Info) decending
/*
            String attrNames = "p_context_id,p_code,p_text,p_category,p_severity,r_page_cnt,r_object_id"; // r_page_cnt
            String orderByName = "p_severity";
            String orderByType = "desc";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM p_user_message where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND) and p_action_taken = false" +
                                 " ORDER BY " + orderByName + " " + orderByType +" ";
            HelperClass.porticoOutput(0, "Timing QCResultSet-loadUserMessages dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "Timing QCResultSet-loadUserMessages Completed query execute");
*/

            HelperClass.porticoOutput(0, "Timing QCResultSet-loadUserMessages Start for m_batchfolderId="+m_batchfolderId);
			Hashtable listMessageAndContextAttribute = DBHelperClass.getProblemReportData(m_batchfolderId);
            HelperClass.porticoOutput(0, "Timing QCResultSet-loadUserMessages End for m_batchfolderId="+m_batchfolderId);

            if(listMessageAndContextAttribute != null && listMessageAndContextAttribute.size() > 0)
            {
                Enumeration messageAndContextAttributeEnumerate = listMessageAndContextAttribute.keys();
                while(messageAndContextAttributeEnumerate.hasMoreElements())
                {
					String msgObjectId = (String)messageAndContextAttributeEnumerate.nextElement();
					Hashtable messageAndContextAttributeHash = (Hashtable)listMessageAndContextAttribute.get(msgObjectId);
					if(messageAndContextAttributeHash == null || messageAndContextAttributeHash.size() == 0)
					{
						HelperClass.porticoOutput(1, "Error-QCResultSet-loadUserMessagesAndData has empty messageAndContextAttributeHash for msgObjectId="+msgObjectId);
					}
					else
					{
						// Message Level Info
					    ArrayList msgLevellist = new ArrayList();
			            String r_object_type = "";
   				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_OBJECT_TYPE))
   				        {
					    	r_object_type = (String)messageAndContextAttributeHash.get(DBHelperClass.P_OBJECT_TYPE);
                            if(r_object_type == null)
                            {
       				    		r_object_type = "";
       				    	}
			            }
    					String p_context_id = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_CONTEXT_ID))
    				    {
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_CONTEXT_ID);
							if(r_object_type.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
							{
								// Note: Override the 'Oracle Batch Accession Id' and set to Documentum's BatchObjectId
							    p_context_id = m_batchfolderId;
							}
							else
							{
							    p_context_id = (String)messageAndContextAttributeHash.get(DBHelperClass.P_CONTEXT_ID);
                                if(p_context_id == null)
                                {
        					    	p_context_id = "";
         					    }
						    }
                            valuePair.setValue(p_context_id);
                            msgLevellist.add(valuePair);
				        }
                        String value = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_CODE))
    				    {
							value = (String)messageAndContextAttributeHash.get(DBHelperClass.P_CODE);
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_CODE);
                            if(value == null)
                            {
        						value = "";
         					}
                            valuePair.setValue(value);
                            msgLevellist.add(valuePair);
				        }
				        value = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_TEXT))
    				    {
							value = (String)messageAndContextAttributeHash.get(DBHelperClass.P_TEXT);
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_TEXT);
                            if(value == null)
                            {
        						value = "";
         					}
                            valuePair.setValue(value);
                            msgLevellist.add(valuePair);
				        }
				        value = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_CATEGORY))
    				    {
							value = (String)messageAndContextAttributeHash.get(DBHelperClass.P_CATEGORY);
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_CATEGORY);
                            if(value == null)
                            {
        						value = "";
         					}
                            valuePair.setValue(value);
                            msgLevellist.add(valuePair);
				        }
				        String p_severity = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_SEVERITY))
    				    {
							p_severity = (String)messageAndContextAttributeHash.get(DBHelperClass.P_SEVERITY);
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_SEVERITY);
                            if(p_severity == null)
                            {
        						p_severity = "";
         					}
                            valuePair.setValue(p_severity);
                            msgLevellist.add(valuePair);
				        }
				        // CONTENT(AddlnText on the blob)
				        String content = "";
    				    if(messageAndContextAttributeHash.containsKey(DBHelperClass.CONTENT))
    				    {
							content = (String)messageAndContextAttributeHash.get(DBHelperClass.CONTENT);
        					valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.CONTENT);
                            if(content == null)
                            {
        						content = "";
         					}
                            valuePair.setValue(content);
                            msgLevellist.add(valuePair);
				        }
                        listUserMessages.put(msgObjectId, msgLevellist);
                        listMsgIdContextIdMapping.put(msgObjectId, p_context_id);
                        HelperClass.porticoOutput(0, "QCResultSet-loadUserMessages(msgObjectId,p_severity)="+msgObjectId+","+p_severity);
				    }
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QCResultSet-loadUserMessages():"+e.toString());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "QCResultSet-loadUserMessages-End for m_batchfolderId="+m_batchfolderId);
	}

	public String getFormattedObjectName(String objectid, String objectname, String objecttype)
	{
		String newObjectName = objectname;

		try
		{
			Object addlObjectItem = null;

			if(listObjectStateAttributeValueMapping.containsKey(objectid))
			{
    		    addlObjectItem = listObjectStateAttributeValueMapping.get(objectid);
		    }
    		if(addlObjectItem != null)
    		{
   				ArrayList alist = (ArrayList)addlObjectItem;
   				if(alist != null && alist.size() > 0)
        		{
            		if(objecttype.equalsIgnoreCase(DBHelperClass.CU_TYPE))
    				{
    					for(int indx=0; indx < alist.size(); indx++)
    					{
    		    			ValuePair valuePair = (ValuePair)alist.get(indx);
                            if(valuePair.getKey().equals(DBHelperClass.P_DISPLAY_LABEL))
                            {
    		    				newObjectName = newObjectName + "</br>" + "[" + (String)valuePair.getValue() + "]";
    		    				// Remove break for multiple values
    		    				break;
    		    			}
    					}
    				}
            		else if(objecttype.equals(DBHelperClass.SU_TYPE))
    				{
    					for(int indx=0; indx < alist.size(); indx++)
    					{
    		    			ValuePair valuePair = (ValuePair)alist.get(indx);
                            if(valuePair.getKey().equals(DBHelperClass.P_DISPLAY_LABEL))
                            {
								String p_work_filename = (String)valuePair.getValue();
								if(p_work_filename != null && !p_work_filename.equals(""))
								{
									int findIndex = -1;
									if((findIndex = p_work_filename.lastIndexOf("/")) != -1)
									{
										// Future just check if 'findIndex+1' is < p_work_filename.length
										if((findIndex+1) < p_work_filename.length())
										{
									        p_work_filename = p_work_filename.substring(findIndex+1);
									    }
								    }
								    // No "</br>" because it is a link in jsp
    		    				    newObjectName = newObjectName + "[" + p_work_filename + "]";
							    }
    		    				// Remove break for multiple values
    		    				break;
    		    			}
    					}
    				}
            		else if(objecttype.equals(DBHelperClass.FU_TYPE))
    				{
    					for(int indx=0; indx < alist.size(); indx++)
    					{
    		    			ValuePair valuePair = (ValuePair)alist.get(indx);
                            if(valuePair.getKey().equals(DBHelperClass.P_DISPLAY_LABEL))
                            {
								String p_fu_type = (String)valuePair.getValue();
								if(p_fu_type != null && !p_fu_type.equals(""))
								{
    		    				    newObjectName = newObjectName + "</br>" + "[" + p_fu_type + "]";
							    }
    		    				// Remove break for multiple values
    		    				break;
    		    			}
    					}
    				}
    			}
    		}
    		else
    		{
				HelperClass.porticoOutput(1, "QCResultSet-getFormattedObjectName()-listObjectStateAttributeValueMapping not found(Check if Fileref) for objectid="+objectid);
			}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in QCResultSet-getFormattedObjectName" + e.toString());
		}
		finally
		{
		}

		return newObjectName;
	}

    public void printMe(ArrayList list)
	{
		HelperClass.porticoOutput(0, "QCResultSet-printMe(Start)-------------");

		if(list != null && list.size() > 0)
		{
			for(int indx=0; indx < list.size(); indx++)
			{
				Object obj = list.get(indx);
                HelperClass.porticoOutput(0, "QCResultSet-printMe="+obj.toString());
			}
		}

		HelperClass.porticoOutput(0, "QCResultSet-printMe(End)-------------");
	}

	public boolean printNode(String childId)
	{
		boolean isValid = true;
		ValuePair valuePair = null;
	    if(listObjectStateAttributeValueMapping != null &&
			listObjectStateAttributeValueMapping.size() > 0 &&
			listObjectStateAttributeValueMapping.containsKey(childId))
        {
		    ArrayList childAttr = (ArrayList)listObjectStateAttributeValueMapping.get(childId);
			for(int childattrindx=0; childattrindx < childAttr.size(); childattrindx++)
			{
				valuePair = (ValuePair)childAttr.get(childattrindx);
				if(valuePair.getKey().equals(DBHelperClass.P_NAME))
				{
					HelperClass.porticoOutput(0, "QCResultSet-buildNode():child node="+childId + "," + valuePair.getValue());
					break;
				}
			}
		}
		else
		{
			HelperClass.porticoOutput(0, "QCResultSet-buildNode():child node(Note:Ignore if batchOperationsManager) NO mapping objectId ="+childId);
		}

		return isValid;
	}

    public static boolean clearAllWarnings(String bObjId, IDfSession session) throws Exception
    {
		HelperClass.porticoOutput(0, "QCResultSet-clearAllWarnings - Start for batchId="+bObjId);

		boolean isSuccessful = true;

        try
        {
			String desc = ""; // No known description
			DBHelperClass.clearAllWarningMessages(bObjId, desc);
            // If warning messages were present and was cleared	return true
            // else return false.But, here there is no way of knowing that so
            // render all the time
        }
        catch (Exception e)
        {
			isSuccessful = false;
            HelperClass.porticoOutput(1, "Exception in QCResultSet-clearAllWarnings="+e.toString());
        }
        finally
        {
        }

		HelperClass.porticoOutput(0, "QCResultSet-clearAllWarnings - End for batchObjectId,isSuccessful="+bObjId+","+isSuccessful);

        return isSuccessful;
    }

    // Default it returns as Info
    public static int getSeverityFromSeverityText(String severityText)
    {
        int intSeverity = 0; // "Info"

        if(severityText.equalsIgnoreCase("Warning"))
        {
		    intSeverity = 1;
		}
		else if(severityText.equalsIgnoreCase("Fatal"))
		{
			intSeverity = 2;
		}

		return intSeverity;
	}

	public String getControlSafeObjectIdFromObjectId(String objectId)
	{
		String controlSafeObjectId = objectId;
		if(objectId != null && !objectId.equals(""))
		{
		    controlSafeObjectId = QcHelperClass.getControlSafeString(objectId);
    		if(!listControlSafeObjectList.containsKey(controlSafeObjectId))
    		{
				listControlSafeObjectList.put(controlSafeObjectId, objectId);
			}
	    }
	    else
	    {
			HelperClass.porticoOutput(1, "Error in QCResultSet-getControlSafeObjectIdFromObjectId-objectId not populated-objectId=" + objectId);
		}

		HelperClass.porticoOutput(0, "QCResultSet-getControlSafeObjectIdFromObjectId-input objectId="+ objectId + "," + "output controlSafeObjectId="+controlSafeObjectId);

		return controlSafeObjectId;
	}

	public String getObjectIdFromControlSafeObjectId(String controlSafeObjectId)
	{
		String objectId = controlSafeObjectId;

		if(listControlSafeObjectList.containsKey(controlSafeObjectId))
		{
			objectId = (String)listControlSafeObjectList.get(controlSafeObjectId);
		}
		else
		{
			HelperClass.porticoOutput(1, "Error in QCResultSet-getObjectIdFromControlSafeObjectId-controlSafeObjectId="+controlSafeObjectId+ " not in the listControlSafeObjectList");
		}

		HelperClass.porticoOutput(0, "QCResultSet-getObjectIdFromControlSafeObjectId-input controlSafeObjectId="+ controlSafeObjectId + "," + "output objectId="+objectId);

		return objectId;
	}

        // Holds the CU State, FU State, SU State parent/children mappings
	Hashtable listObjectStateParentMapping;

	// Holds the attributes of the objects required for processing and display
	Hashtable listObjectStateAttributeValueMapping;

	// Holds if the CU's for inspection have been inspected Or not
	// First time refreshed from Database, subsequent updates cause database updates as well as
	//       this Hashtable update(for this session), to avoid querying database for every click
	Hashtable listInspectCuMapping;

	// Holds if the CU's children are expanded or collapsed.
	//Expand true, Collapse false
	Hashtable listExpandCuMapping;

    // Holds the Object State - Object mapping, currently SU State/SU Object mappings
	Hashtable listObjectStateObjectMapping;

	Hashtable listUserMessages;
	Hashtable listMsgIdContextIdMapping;

	Hashtable listControlSafeObjectList = new Hashtable();

    /*
     * maintains list of rows
     */
    List allRowData = new ArrayList();
    TableResultSet tableResultSet;
    private String m_batchfolderId;
    private String m_batchstatus;
    private String m_checkListId;
    private String scratchPadId;
    private int numberArt;
    private int inspectNumberArt;
	private String m_ReportType; // "inspect"; // "entire";
    private IDfSession m_dfSession;
    private boolean m_isProcessable = true;
    private static final String ERROR_IN_DATA = "Error in data";
}


/*
    public void loadDynamicAttributeLookUp()
    {
		listObjectStateParentMapping = new Hashtable();
		listInspectCuMapping = new Hashtable();
		listExpandCuMapping = new Hashtable();
		listObjectStateAttributeValueMapping = new Hashtable();
		listObjectStateObjectMapping = new Hashtable();

		HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp-Start");
		HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp OPEN IDfCollection");
		IDfCollection tIDfCollection = null;
		try
		{
            // listObjectStateAttributeValueMapping
			ValuePair valuePair = null;
            tIDfCollection = null;
            String attrNames = "i_folder_id,r_object_id,object_name,r_object_type";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM dm_sysobject where r_object_type IN ("+
                               "'"+HelperClass.getInternalObjectType("cu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("fu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("su_state")+"'"+
                                ")" + " AND " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

            HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp Completed query execute");
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					ArrayList alist = new ArrayList();
					String currentObjectId = tIDfCollection.getString("r_object_id");
   					String parentObjectId = tIDfCollection.getString("i_folder_id");
				    String object_name = tIDfCollection.getString("object_name");
				    String r_object_type = tIDfCollection.getString("r_object_type");
   					valuePair = new ValuePair();
                    valuePair.setKey("i_folder_id");
                    if(parentObjectId == null)
                    {
				    	parentObjectId = "";
				    }
                    valuePair.setValue(parentObjectId);
                    alist.add(valuePair);
  					valuePair = new ValuePair();
                    valuePair.setKey("object_name");
                    if(object_name == null)
                    {
				    	object_name = "";
				    }
                    valuePair.setValue(object_name);
                    alist.add(valuePair);
  					valuePair = new ValuePair();
                    valuePair.setKey("r_object_type");
                    if(r_object_type == null)
                    {
				    	r_object_type = "";
				    }
                    valuePair.setValue(r_object_type);
                    alist.add(valuePair);
                    listObjectStateAttributeValueMapping.put(currentObjectId, alist);

                    // Parent = batchOperationsManager, Children = CU State(s)
                    // Parent = CU State, Children = FU State(s)
                    // Parent = FU State, Children = SU State(s)
					if(listObjectStateParentMapping != null)
					{
						if(listObjectStateParentMapping.size() > 0 &&
						            listObjectStateParentMapping.containsKey(parentObjectId))
						{
							((ArrayList)listObjectStateParentMapping.get(parentObjectId)).add(currentObjectId);
						}
						else
						{
							ArrayList childList = new ArrayList();
							childList.add(currentObjectId);
							listObjectStateParentMapping.put(parentObjectId, childList);
						}
					}

                    HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp(currentObjectId,parentObjectId,object_name,r_object_type)="+currentObjectId+","+parentObjectId+","+object_name+","+r_object_type);
				}

				tIDfCollection.close();
			}
// Try using existing sort == CU 1/*
//                            CU 1/FU 2/*
//                            CU 1/FU 2/SU 7
//  orphan                    Z/FU 6
//  orphan                    Z/SU 10


// Start

            // listObjectStateAddlnAttributeValueMapping

            ArrayList alistBatch = new ArrayList();
			valuePair = new ValuePair();

    		valuePair = new ValuePair();
            valuePair.setKey("object_name");
            valuePair.setValue(HelperClass.getObjectName(m_dfSession, m_batchfolderId));
            alistBatch.add(valuePair);

            valuePair.setKey("p_display_label");
            valuePair.setValue("thisBatch");
            alistBatch.add(valuePair);

    		valuePair = new ValuePair();
            valuePair.setKey("r_object_type");
            valuePair.setValue("p_batch");
            alistBatch.add(valuePair);
            listObjectStateAttributeValueMapping.put(m_batchfolderId, alistBatch);

            tIDfCollection = null;
			{
				attrNames = "r_object_id,p_display_label,p_sort_key,p_inspection_required,p_inspected";
                dqlString = "SELECT " + attrNames + " FROM p_cu_state where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_cu_state) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_cu_state) Completed query execute");
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("r_object_id");
			    		String sortKey = tIDfCollection.getString("p_sort_key");
       					String displaylabel = tIDfCollection.getString("p_display_label");
       					valuePair = new ValuePair();
                        valuePair.setKey("p_display_label");
                        if(displaylabel == null)
                        {
       						displaylabel = "";
       					}
                        valuePair.setValue(displaylabel);
                        alist.add(valuePair);

       					valuePair = new ValuePair();
                        valuePair.setKey("p_sort_key");
                        if(sortKey == null)
                        {
       						sortKey = "";
       					}
                        valuePair.setValue(sortKey);
                        alist.add(valuePair);

                        // listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);

    					if(listObjectStateAttributeValueMapping != null)
    					{
    						if(listObjectStateAttributeValueMapping.size() > 0 &&
						            listObjectStateAttributeValueMapping.containsKey(currentObjectId))
    						{
    							((ArrayList)listObjectStateAttributeValueMapping.get(currentObjectId)).addAll(alist);
    						}
    						else
    						{
    							listObjectStateAttributeValueMapping.put(currentObjectId, alist);
    						}
    					}

                        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp(CU State Addln)displaylabel,sortKey="+displaylabel+","+sortKey);

                        boolean isInspectionRequired = tIDfCollection.getBoolean("p_inspection_required");
                        if(isInspectionRequired == true)
                        {
                            listInspectCuMapping.put(currentObjectId, Boolean.valueOf(tIDfCollection.getBoolean("p_inspected")));  //Boolean.toString(tIDfCollection.getBoolean("p_inspected")));
					    }
					    listExpandCuMapping.put(currentObjectId, Boolean.valueOf(false));
					}
				}

				tIDfCollection.close();
			}

            // listObjectStateAddlnAttributeValueMapping
            tIDfCollection = null;
			{
				attrNames = "r_object_id,p_work_filename,p_virus_scanned,p_is_created_by_workflow";
                dqlString = "SELECT " + attrNames + " FROM p_su_state where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_su_state) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_su_state) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("r_object_id");

       					String workfilename = tIDfCollection.getString("p_work_filename");
                        boolean p_virus_scanned = tIDfCollection.getBoolean("p_virus_scanned");
                        boolean p_is_created_by_workflow = tIDfCollection.getBoolean("p_is_created_by_workflow");
  					    valuePair = new ValuePair();
                        valuePair.setKey("p_display_label");
                        if(workfilename == null)
                        {
   					    	workfilename = "";
   					    }
                        valuePair.setValue(workfilename);
                        alist.add(valuePair);

   					    valuePair = new ValuePair();
                        valuePair.setKey("p_virus_scanned");
                        valuePair.setValue(Boolean.toString(p_virus_scanned));
                        alist.add(valuePair);

   					    valuePair = new ValuePair();
                        valuePair.setKey("p_is_created_by_workflow");
                        valuePair.setValue(Boolean.toString(p_is_created_by_workflow));
                        alist.add(valuePair);

                        // listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);
    					if(listObjectStateAttributeValueMapping != null)
    					{
    						if(listObjectStateAttributeValueMapping.size() > 0 &&
						            listObjectStateAttributeValueMapping.containsKey(currentObjectId))
    						{
    							((ArrayList)listObjectStateAttributeValueMapping.get(currentObjectId)).addAll(alist);
    						}
    						else
    						{
    							listObjectStateAttributeValueMapping.put(currentObjectId, alist);
    						}
    					}

                        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp(SU State Addln)(workfilename)="+workfilename+","+
                                                        "p_virus_scanned="+p_virus_scanned);
					}
				}

				tIDfCollection.close();
			}

            tIDfCollection = null;
			{
				// Documentum for some reason does not return the i_folder_id for some r_object_id(s), but if
				// we do a query giving specifically where r_object_id=p_fu objectid, it returns the i_folder_id
				// Probably a bug in Documentum
				// Workaround give the 'order by r_object_id' it returns okay !!!!
				attrNames = "i_folder_id,r_object_id,p_fu_type";
                dqlString = "SELECT " + attrNames + " FROM p_fu where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND) order by r_object_id";
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_fu) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_fu) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("i_folder_id");
			    		String rObjectId = tIDfCollection.getString("r_object_id");
   					    String futype = tIDfCollection.getString("p_fu_type");
       					valuePair = new ValuePair();
                        valuePair.setKey("p_display_label");
                        if(futype == null)
                        {
   					    	futype = "";
   					    }
                        valuePair.setValue(futype);
                        alist.add(valuePair);
                        // listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);

                        if(currentObjectId != null)
                        {
        					if(listObjectStateAttributeValueMapping != null)
        					{
        						if(listObjectStateAttributeValueMapping.size() > 0 &&
    						            listObjectStateAttributeValueMapping.containsKey(currentObjectId))
        						{
        							((ArrayList)listObjectStateAttributeValueMapping.get(currentObjectId)).addAll(alist);
        						}
        						else
        						{
        							listObjectStateAttributeValueMapping.put(currentObjectId, alist);
        						}
        					}
					    }
					    else
					    {
							HelperClass.porticoOutput(1, "QCResultSet-loadDynamicAttributeLookUp(FU Addln) i_folder_id IS NULL for r_object_id="+rObjectId);
						}

                        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp(FU Addln)(futype)="+futype);
					}
				}

				tIDfCollection.close();
			}

            tIDfCollection = null;
			{
				// Documentum for some reason does not return the i_folder_id for some r_object_id(s), but if
				// we do a query giving specifically where r_object_id=p_fu objectid, it returns the i_folder_id
				// Probably a bug in Documentum
				// Workaround give the 'order by r_object_id' it returns okay !!!!
				attrNames = "i_folder_id,r_object_id";
                dqlString = "SELECT " + attrNames + " FROM p_su where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND) order by r_object_id";
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_su) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing QCResultSet-loadDynamicAttributeLookUp(p_su) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String parentObjectId = tIDfCollection.getString("i_folder_id");
			    		String currentObjectId = tIDfCollection.getString("r_object_id");

                        if(parentObjectId != null)
                        {
			    		    listObjectStateObjectMapping.put(parentObjectId, currentObjectId);
					    }
					    else
					    {
							HelperClass.porticoOutput(1, "QCResultSet-loadDynamicAttributeLookUp(SU State-SU Object) i_folder_id IS NULL for r_object_id="+currentObjectId);
						}

                        HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp(SU State-SU Object)="+parentObjectId+","+currentObjectId);
					}
				}

				tIDfCollection.close();
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-QCResultSet-loadDynamicAttributeLookUp():"+e.getMessage());
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in QCResultSet-loadDynamicAttributeLookUp-close" + e.getMessage());
			}
		}

		HelperClass.porticoOutput(0, "QCResultSet-loadDynamicAttributeLookUp-End");
	}
*/
/*
    public void clearButton(String temp) throws Exception {
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        try {
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery();
            sb.append("update p_user_message object set p_action_taken=true where r_object_id='");
            sb.append(temp);
            sb.append("'");

            q.setDQL(sb.toString());
            idfcollection = q.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
        } catch (DfException e) {
            throw e;
        } catch (Exception e1) {
            throw e1;
        } finally{
            try {
                if(idfcollection!=null)
                    idfcollection.close();
                idfcollection = null;
            } catch (DfException e2) {
                throw e2;
            }
            sb.delete(0,sb.capacity());
            idfcollection=null;
        }
    }
*/
/*
    public boolean clearAllWarnings(String bObjId) throws Exception{
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        ArrayList userMsgId = new ArrayList();
        try {
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery();
            sb.append("select r_object_id from p_user_message where FOLDER(ID('");
            sb.append(m_batchfolderId);
            sb.append("'),DESCEND) and p_action_taken=false and p_severity=1");
            q.setDQL(sb.toString());
            idfcollection = q.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            while(idfcollection.next()) {
                userMsgId.add(idfcollection.getString("r_object_id"));
            }
            if(!userMsgId.isEmpty()) {
              QcHelperClass.setUserMessages(m_dfSession,userMsgId,bObjId,"");
              return(true);
            } else return(false);
        } catch (DfException e) {
            throw e;
        } catch (Exception e1) {
            throw e1;
        } finally{
            try {
                if(idfcollection!=null)
                    idfcollection.close();
                idfcollection = null;
            } catch (DfException e2) {
                throw e2;
            }
            sb.delete(0,sb.capacity());
            idfcollection=null;
        }
    }
*/
