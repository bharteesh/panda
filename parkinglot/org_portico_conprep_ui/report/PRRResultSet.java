/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             PRRResultSet.java
 * Created on       Feb 15, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.web.form.control.databound.IDataboundParams;
import com.documentum.web.form.control.databound.TableResultSet;

/**
 * Description  Fetches and hold data for ProblemRes report as a collection object
 *              this class has three dql calls, in the foll order
 *                  1. fetch all user message object ids for this batch
 *                  2. fetch all cu object ids under this batch
 *                  3. fetch all objects under this batch, to identify unprocessed orphaned ones
 * Author       pramaswamy
 * Type         PRRResultSet
 */
public class PRRResultSet
{
    public PRRResultSet(String batchfolderId, String batchStatus, String batchName, IDfSession dfSession) throws DfException, Exception
    {
        m_batchfolderId = batchfolderId;
        m_dfSession = dfSession;
		m_batchstatus = batchStatus;
		m_batchname = batchName;
        allRowData = new ArrayList();

        boolean canProcess = true;
        String displayErrorMsg = "";

		if(!m_batchstatus.equals(HelperClassConstants.PROBLEM) &&
		            !m_batchstatus.equals(HelperClassConstants.RESOLVING_PROBLEM) &&
		            !m_batchstatus.equals(HelperClassConstants.SYSTEM_ERROR))
		{
			canProcess = false;
			displayErrorMsg = "Batch Status not in Problem OR Resolving Problem State!";
		}

        if(canProcess == true)
        {
            HelperClass.porticoOutput(0, "PRRResultSet-loadInfo-Start");
            loadUserMessagesAndData();
            HelperClass.porticoOutput(0, "PRRResultSet-loadInfo-End");
            HelperClass.porticoOutput(0, "PRRResultSet-createRowsForTableResultSet-Start");
            createRowsForTableResultSet(); // populates 'allRowData' List
            HelperClass.porticoOutput(0, "PRRResultSet-createRowsForTableResultSet-End");
            setTableResultSet(new TableResultSet(allRowData,ProbResReport.colNames));
        }
        else
        {
            HelperClass.porticoOutput(0, "Not valid for Processing="+displayErrorMsg);
            displayError(displayErrorMsg);
            setTableResultSet(new TableResultSet(allRowData,ProbResReport.colNames));
        }
    }

    public void displayError(String errorMsg)
    {
        HelperClass.porticoOutput(0, "PRRResultSet-displayError-Start");
		try
		{
            ArrayList currentRow = new ArrayList();
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add("");
            currentRow.add(errorMsg);
		    currentRow.add("Fatal");
            currentRow.add("");
            currentRow.add("true");
            currentRow.add("");
            currentRow.add("false");
            currentRow.add(""); // Decides the order of display(2 of 2)
    		printMe(currentRow);
            allRowData.add(currentRow);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-PRRResultSet-displayError():"+e.getMessage());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "PRRResultSet-displayError-End");
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
        HelperClass.porticoOutput(0, "PRRResultSet- setTableResultSet Start");
        tableResultSet = set;

/*
        if(m_batchstatus.equals("")) getBatchStatus();
        if(m_batchstatus!=null && !m_batchstatus.equals(HelperClassConstants.INSPECTING) && !m_batchstatus.equals(HelperClassConstants.INSPECT))
        {
			// Decides the order of display(1 of 2)
            tableResultSet.sort(ProbResReport.colNames[10],
                                IDataboundParams.SORTDIR_REVERSE,
                                IDataboundParams.SORTMODE_TEXT); // 0,1
	    }
*/

    	// Decides the order of display(1 of 2)
        tableResultSet.sort(ProbResReport.colNames[10],
                                IDataboundParams.SORTDIR_REVERSE,
                                IDataboundParams.SORTMODE_TEXT); // 0,1


        HelperClass.porticoOutput(0, "PRRResultSet- setTableResultSet End");
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

// Combine loadUserMessages/loadDynamicAttributeLookUp since all attribute are available in 1 single query
    public void loadUserMessagesAndData()
    {
		// RANGA, Future for 'listUserMessages' if sort to be maintained, use 'ArrayList' in place of Hashtable
		// So, that we can get away with tableResultSet.sort()
		// Performance Note: In the query 'orderByType' can be removed, if tableResultSet.sort() is used

		// UserMessage Hash
		listUserMessages = new Hashtable();
		listMsgIdContextIdMapping = new Hashtable();
		listFatalUserMessage = new Hashtable();
		listWarningUserMessage = new Hashtable();

        // Data Hash
        listObjectStateAttributeValueMapping = new Hashtable();
        listObjectStateObjectMapping = new Hashtable(); // key=AccessionId, value=DocumentumId

/*
        listObjectStateAddlnAttributeValueMapping = new Hashtable();
        listObjectFUStateAttributeValueMapping = new Hashtable();
*/

		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-Start");
		try
		{
            // Initialize Batch Info into 'listObjectStateAttributeValueMapping'
			ValuePair valuePair = null;

            ArrayList alistBatch = new ArrayList();
			valuePair = new ValuePair();
            valuePair.setKey(DBHelperClass.P_PARENT_ID);
            valuePair.setValue("");
            alistBatch.add(valuePair);

    		valuePair = new ValuePair();
            valuePair.setKey(DBHelperClass.P_NAME);
            valuePair.setValue(m_batchname);
            alistBatch.add(valuePair);

    		valuePair = new ValuePair();
            valuePair.setKey(DBHelperClass.P_OBJECT_TYPE);
            valuePair.setValue("p_batch");
            alistBatch.add(valuePair);
            listObjectStateAttributeValueMapping.put(m_batchfolderId, alistBatch);

    		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-Start-listAddlnSUStateAttributeMapping");
			ArrayList aSUlistIn = new ArrayList();
			aSUlistIn.add(DBHelperClass.P_ACCESSION_ID); // This attribute is required for populating the return hash key
			aSUlistIn.add(DBHelperClass.P_CONTENT_ID);
			listAddlnSUStateAttributeMapping = DBHelperClass.getAttributesForAllObjects(m_batchfolderId, DBHelperClass.SU_TYPE, aSUlistIn);
    		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-End-listAddlnSUStateAttributeMapping");

    		// listMessageAndContextAttribute has (key, hash)
			Hashtable listMessageAndContextAttribute = DBHelperClass.getProblemReportData(m_batchfolderId);


/*
			// Pick all the Message(s) order by severity 2(Fatal),1(Warning),0(Info) decending
            String attrNames = "p_context_id,p_code,p_text,p_category,p_severity,r_page_cnt,r_object_id"; // r_page_cnt
            String orderByName = "p_severity";
            String orderByType = "desc";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM p_user_message where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND) and p_action_taken = false" +
                                 " ORDER BY " + orderByName + " " + orderByType +" ";
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadUserMessages dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadUserMessages Completed query execute");
*/
            if(listMessageAndContextAttribute != null && listMessageAndContextAttribute.size() > 0)
            {
                Enumeration messageAndContextAttributeEnumerate = listMessageAndContextAttribute.keys();
                while(messageAndContextAttributeEnumerate.hasMoreElements())
                {
					String msgObjectId = (String)messageAndContextAttributeEnumerate.nextElement();
					Hashtable messageAndContextAttributeHash = (Hashtable)listMessageAndContextAttribute.get(msgObjectId);
					if(messageAndContextAttributeHash == null || messageAndContextAttributeHash.size() == 0)
					{
						HelperClass.porticoOutput(1, "Error-PRRResultSet-loadUserMessagesAndData has empty messageAndContextAttributeHash for msgObjectId="+msgObjectId);
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
                            HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-Msg Content(addlnContent)="+content);
				        }

// RANGA NO 'r_page_cnt' see the impact can we substitute it with the Addln Text in the blob
/*
                        value = tIDfCollection.getString("r_page_cnt");
					    valuePair = new ValuePair();
                        valuePair.setKey("r_page_cnt");
                        if(value == null)
                        {
					    	value = "";
					    }
                        valuePair.setValue(value);
                        alist.add(valuePair);
*/

                        listUserMessages.put(msgObjectId, msgLevellist);
                        listMsgIdContextIdMapping.put(msgObjectId, p_context_id);
                        if(p_severity.equals("2"))
                        {
					        if(!listFatalUserMessage.containsKey(p_context_id))
					        {
                                listFatalUserMessage.put(p_context_id, p_context_id);
					        }
				        }
				        else if(p_severity.equals("1"))
				        {
					        if(!listWarningUserMessage.containsKey(p_context_id))
					        {
                                listWarningUserMessage.put(p_context_id, p_context_id);
					        }
					    }
                        HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData(msgObjectId,p_severity)="+msgObjectId+","+p_severity);

                        // Populate Context Related Info if it has NOT been populated already
                        if(p_context_id != null && !p_context_id.equals("") && !listObjectStateAttributeValueMapping.containsKey(p_context_id))
                        {
                            HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-Start-Data for p_context_id="+p_context_id);

                            ArrayList attrLevellist = new ArrayList();
       				    	valuePair = new ValuePair();
                            valuePair.setKey(DBHelperClass.P_OBJECT_TYPE);
                            if(r_object_type == null)
                            {
       				    		r_object_type = "";
       				    	}
                            valuePair.setValue(r_object_type);
                            attrLevellist.add(valuePair);

    				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_PARENT_ID))
    				        {
						    	String parentObjectId = (String)messageAndContextAttributeHash.get(DBHelperClass.P_PARENT_ID);
        				    	valuePair = new ValuePair();
                                valuePair.setKey(DBHelperClass.P_PARENT_ID);
                                if(parentObjectId == null)
                                {
        				    		parentObjectId = "";
         				    	}
                                valuePair.setValue(parentObjectId);
                                attrLevellist.add(valuePair);
				            }
				            String object_name = "";
    				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_NAME))
    				        {
						    	object_name = (String)messageAndContextAttributeHash.get(DBHelperClass.P_NAME);
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
    				            String p_display_label = "";
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_DISPLAY_LABEL))
        				        {
    						    	p_display_label = (String)messageAndContextAttributeHash.get(DBHelperClass.P_DISPLAY_LABEL);
            				    	valuePair = new ValuePair();
                                    valuePair.setKey(DBHelperClass.P_DISPLAY_LABEL);
                                    if(p_display_label == null)
                                    {
            				    		p_display_label = "";
             				    	}
                                    valuePair.setValue(p_display_label);
                                    attrLevellist.add(valuePair);
    				            }
						    }
						    else if(r_object_type.equalsIgnoreCase(DBHelperClass.FU_TYPE))
						    {
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_FU_TYPE))
        				        {
    						    	String p_fu_type = (String)messageAndContextAttributeHash.get(DBHelperClass.P_FU_TYPE);
            				    	valuePair = new ValuePair();
                                    valuePair.setKey(DBHelperClass.P_FU_TYPE);
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
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_WORK_FILENAME))
        				        {
    						    	String workfilename = (String)messageAndContextAttributeHash.get(DBHelperClass.P_WORK_FILENAME);
            				    	valuePair = new ValuePair();
                                    valuePair.setKey(DBHelperClass.P_WORK_FILENAME);
                                    if(workfilename == null)
                                    {
            				    		workfilename = "";
             				    	}
                                    valuePair.setValue(workfilename);
                                    attrLevellist.add(valuePair);
    				            }
/*
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_VIRUS_SCANNED))
        				        {
    						    	String p_virus_scanned = (String)messageAndContextAttributeHash.get(DBHelperClass.P_VIRUS_SCANNED);
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
*/
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_IS_CREATED_BY_WORKFLOW))
        				        {
    						    	String p_is_created_by_workflow = (String)messageAndContextAttributeHash.get(DBHelperClass.P_IS_CREATED_BY_WORKFLOW);
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
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_LEAD_SOURCE_ID))
        				        {
    						    	String p_lead_source_id = (String)messageAndContextAttributeHash.get(DBHelperClass.P_LEAD_SOURCE_ID);
            				    	valuePair = new ValuePair();
                                    valuePair.setKey(DBHelperClass.P_LEAD_SOURCE_ID);
                                    if(p_lead_source_id == null)
                                    {
            				    		p_lead_source_id = "";
             				    	}
                                    valuePair.setValue(p_lead_source_id);
                                    attrLevellist.add(valuePair);
    				            }
    				            // listObjectStateObjectMapping
        				        if(messageAndContextAttributeHash.containsKey(DBHelperClass.P_CONTENT_ID))
        				        {
    						    	String p_object_id = (String)messageAndContextAttributeHash.get(DBHelperClass.P_CONTENT_ID);
            				    	valuePair = new ValuePair();
                                    valuePair.setKey(DBHelperClass.P_CONTENT_ID);
                                    if(p_object_id == null)
                                    {
            				    		p_object_id = "";
             				    	}
             				    	else
             				    	{
										listObjectStateObjectMapping.put(p_context_id, p_object_id);
									}
                                    valuePair.setValue(p_object_id);
                                    attrLevellist.add(valuePair);
    				            }
						    }
                            listObjectStateAttributeValueMapping.put(p_context_id, attrLevellist);

                            HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-End-Data(p_context_id,object_name,r_object_type)="+p_context_id+","+object_name+","+r_object_type);
					    }
				    }
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-PRRResultSet-loadUserMessagesAndData():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessagesAndData-End");
	}


	public void createRowsForTableResultSet()
	{
		try
		{
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
		    				p_severity = valuePair.getValue();
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
				    	HelperClass.porticoOutput(1, "Error PRRResultSet-createRowsForTableResultSet-No object mapping for p_context_id="+p_context_id);
				    }

                    String isFolder = "true";
                    String isOrphan = "";
                    // Future - could be (p_severity+object_name) for uniqueness
	    			String displaySortKey = getMaxSeverity(p_context_id) + p_context_id + p_severity + p_code; // p_severity+object_name;
    			    insertSingleRow(p_context_id,
					                object_name,
					                r_object_type,
					                p_code,
					                p_text,
					                p_severity,
					                p_category,
					                isFolder,
					                msgObjId,
					                isOrphan,
					                displaySortKey,
					                content);
		        }
		    }
		    else
		    {
		    	HelperClass.porticoOutput(0, "PRRResultSet-createRowsForTableResultSet-No Message mapping list entries");
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-createRowsForTableResultSet" + e.getMessage());
		}
		finally
		{
		}
	}

    public void insertSingleRow(String currentObjectId,
                                String currentObjectName,
                                String currentObjectType,
                                String currentMsgCode,
                                String currentMsgText,
                                String currentMsgSeverity,
                                String currentMsgCategory,
                                String isCurrentObjectFolder,
                                String currentMsgObjectId,
                                String isCurrentObjectOrphaned,
                                String currentDisplaySortKey,
                                String content) throws Exception
    {
		try
		{
		    String tSeverity = currentMsgSeverity;
		    String tCategory = currentMsgCategory;
		    String tObjectName = getFormattedObjectName(currentObjectId, currentObjectName, currentObjectType);
		    String tText = getFormattedMsgText(currentMsgObjectId, currentMsgCode, currentMsgText, content);

            ArrayList currentRow = new ArrayList();
            currentRow.add(currentObjectId);
            currentRow.add(tObjectName);
            currentRow.add(currentObjectType);
            currentRow.add(currentMsgCode);
            currentRow.add(tText);
            if(tSeverity.equals("0"))
            {
                tSeverity = "Info";
	        }
            else if(tSeverity.equals("1"))
            {
		    	tSeverity = "Warning";
	        }
            else if(tSeverity.equals("2"))
            {
		    	tSeverity = "Fatal";
	        }
            else
            {
                tSeverity = "Unknown";
		    }
		    currentRow.add(tSeverity);
            if(tCategory.equals("0"))
            {
		    	tCategory = "Data";
		    }
		    else if(tCategory.equals("1"))
		    {
		    	tCategory = "System";
		    }
		    else
		    {
		    	tCategory = "Unknown";
		    }
            currentRow.add(tCategory);
            currentRow.add(isCurrentObjectFolder);
            currentRow.add(currentMsgObjectId);
            currentRow.add(isCurrentObjectOrphaned);
            currentRow.add(currentDisplaySortKey); // Decides the order of display(2 of 2)
    		printMe(currentRow);
            if((m_batchstatus.equals(HelperClassConstants.INSPECTING) || m_batchstatus.equals(HelperClassConstants.INSPECT)) &&
                     currentObjectType.equalsIgnoreCase(DBHelperClass.FILE_REF_TYPE))
            {
                HelperClass.porticoOutput(0, "PRRResultSet-DO NOT insertSingleRow(For file Ref Object)");
		    }
		    else
		    {
                allRowData.add(currentRow);
                HelperClass.porticoOutput(0, "PRRResultSet-insertSingleRow");
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-insertSingleRow" + e.getMessage());
		}
		finally
		{
		}
	}

	public String getMaxSeverity(String contextId)
	{
		String maxSeverity = "0";

		if(listFatalUserMessage.containsKey(contextId))
		{
			maxSeverity = "2";
		}
		else if(listWarningUserMessage.containsKey(contextId))
		{
			maxSeverity = "1";
		}

		return maxSeverity;
	}

    public String getFormattedMsgText(String msgObjectId, String msgCode, String msgText, String addlnContent)
    {
		String formattedMsgText = "";
		try
		{
			formattedMsgText = getCommonFormattedText(msgText);
    		formattedMsgText = msgCode + "</br>" + formattedMsgText + "</br>";
    		if(addlnContent != null && !addlnContent.equals(""))
    		{
                formattedMsgText = formattedMsgText + getFormattedAdditionalText(addlnContent);
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getFormattedMsgText" + e.getMessage());
		}
		finally
		{
		}

		return formattedMsgText;
	}

// RANGA - New 1_1 Datamodel, change to work on the blob data
/*
    public String getAdditionalText(String msgObjId)
    {
		String additionalText = "";
        byte[] bytes = null;
        HelperClass.porticoOutput(0, "getAdditionalText-Start(msgObjId)="+msgObjId);
        try
        {
            IDfDocument mesgObj = (IDfDocument)m_dfSession.getObject(new DfId(msgObjId));
            if(mesgObj.getContentSize()>0)
            {
                ByteArrayInputStream in = mesgObj.getContent();
                int count = in.available();
                if (count > 0)
                {
                    bytes = new byte[count];
                    in.read(bytes, 0, count);
                    additionalText = new String(bytes);
                }
			}
            HelperClass.porticoOutput(0, "getAdditionalText-Content="+additionalText);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getAdditionalText" + e.getMessage());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "getAdditionalText-End(msgObjId)="+msgObjId);

		return additionalText;
	}
*/

	public String getFormattedAdditionalText(String addlnContent)
	{
		/*
		String formattedAddlnMsgText = getAdditionalText(msgObjId);
		formattedAddlnMsgText = getCommonFormattedText(formattedAddlnMsgText);
		*/
		String formattedAddlnMsgText = getCommonFormattedText(addlnContent);
		if(formattedAddlnMsgText != null && !formattedAddlnMsgText.equals(""))
		{
            String formattedAddlnMsgTextHeader = "Additional Text: " + "</br>"; // "</br>" + "Additional Text: " + "</br>";
            formattedAddlnMsgText = formattedAddlnMsgTextHeader + formattedAddlnMsgText + "</br>";
	    }

	    return formattedAddlnMsgText;
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
            		else if(objecttype.equalsIgnoreCase(DBHelperClass.SU_TYPE))
    				{
						String p_work_filename = "";
						String p_is_created_by_workflow = "";

    					for(int indx=0; indx < alist.size(); indx++)
    					{
    		    			ValuePair valuePair = (ValuePair)alist.get(indx);
    		    			String key = valuePair.getKey();
    		    			String value = (String)valuePair.getValue();
                            if(key.equals(DBHelperClass.P_WORK_FILENAME))
                            {
								p_work_filename = value;
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
							    }
    		    				// Remove break for multiple values
    		    				// break;
    		    			}
    		    			else if(key.equals(DBHelperClass.P_IS_CREATED_BY_WORKFLOW))
    		    			{
								p_is_created_by_workflow = value;
							}

							if(p_work_filename != null && !p_work_filename.equals("") &&
							      p_is_created_by_workflow != null && !p_is_created_by_workflow.equals(""))
							{
								// Received and picked the relevant info, so break
								break;
							}
    					}

    					if(p_is_created_by_workflow != null && !p_is_created_by_workflow.equals(""))
    					{
							if(Boolean.parseBoolean(p_is_created_by_workflow) == true)
							{
								// If the SU is created by the workflow, it is an xml file eventhough the source
								// could be eg: abcd.sgml
						    	p_work_filename = p_work_filename + "(" + PRRResultSet.XML_TAG + ")";
						    }
						}

    				    // No "</br>" because it is a link in jsp
    				    newObjectName = newObjectName + "[" + p_work_filename + "]";
    				}
            		else if(objecttype.equalsIgnoreCase(DBHelperClass.FU_TYPE))
    				{
    					for(int indx=0; indx < alist.size(); indx++)
    					{
    		    			ValuePair valuePair = (ValuePair)alist.get(indx);
                            if(valuePair.getKey().equals(DBHelperClass.P_FU_TYPE))
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
				HelperClass.porticoOutput(0, "PRRResultSet-getFormattedObjectName()-listObjectStateAttributeValueMapping not found for objectid="+objectid);
			}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getFormattedObjectName" + e.getMessage());
		}
		finally
		{
		}

		return newObjectName;
	}

    public String getCommonFormattedText(String s1)
    {
		HelperClass.porticoOutput(0, "PRRResultSet- getCommonFormattedText Start");
        String sysText1 = "";
		try
		{
            String out="";
            while(s1.length()>60 || s1.indexOf("\n") > 0)
            {
                int i2=s1.indexOf("\n");
                int i3=0;
                if(i2>60)
                {
                	i3=s1.lastIndexOf(" ",60);
                	if(i3>30)
                	{
                		out=out.concat(s1.substring(0,i3)+"<br/>");
                		s1=s1.substring(i3+1);
                	}
                	else
                	{
                        out=out.concat(s1.substring(0,59)+"<br/>");
                        s1=s1.substring(59);
                	}
                }
                else if(i2<=60&&i2>=0)
                {
                    out=out.concat(s1.substring(0,i2)+"<br/>");
                    s1=s1.substring(i2+1);
                }
                else if(i2<0)
                {
                	i3=s1.lastIndexOf(" ",60);
                	if(i3>30)
                	{
                		out=out.concat(s1.substring(0,i3)+"<br/>");
                		s1=s1.substring(i3+1);
                	}
                	else
                	{
                        out=out.concat(s1.substring(0,59)+"<br/>");
                        s1=s1.substring(59);
                	}
                }
            }
            out=out.concat(s1);
            sysText1="<pre>"+out+"</pre>";
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getCommonFormattedText" + e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "PRRResultSet- getCommonFormattedText End");

        return sysText1;
    }


	public int getActiveMessageCount()
	{
		int activeMessageCount = 0;

		if(listUserMessages != null)
		{
			activeMessageCount = listUserMessages.size();
		}

		return activeMessageCount;
	}

	public boolean isVirusChecked(String objId)
	{
		return true;
	}

    // p_su_state objectId
/*
	public boolean isVirusChecked(String objId)
	{
		boolean isVirusCheckedObject = false;
		boolean isVirusScanned = false;
		boolean isCreatedByWorkFlow = false;

		HelperClass.porticoOutput(0, "PRRResultSet-isVirusChecked - Start for objId="+objId);

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
				    HelperClass.porticoOutput(1, "Error PRRResultSet-isVirusChecked-No listObjectStateAttributeValueMapping mapping for objId="+objId);
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-isVirusChecked" + e.getMessage());
		}
		finally
		{
		}

		// isVirusCheckedObject = isVirusScanned || isCreatedByWorkFlow;
		isVirusCheckedObject = isCreatedByWorkFlow;

		HelperClass.porticoOutput(0, "PRRResultSet-isVirusChecked-object(value)=" + objId +"("+isVirusCheckedObject+")");

		return isVirusCheckedObject;
	}
*/

    // p_su_state objectId
	public String getLeadSource(String objId)
	{
		String leadSource = "";

		HelperClass.porticoOutput(0, "PRRResultSet-getLeadSource - Start for objId="+objId);

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
                            if(valuePair.getKey().equals(DBHelperClass.P_LEAD_SOURCE_ID))
                            {
    			        		leadSource = valuePair.getValue();
        			        	break;
    			        	}
         		    	}
				    }
				}
			    else
			    {
				    HelperClass.porticoOutput(1, "Error PRRResultSet-getLeadSource-No AddlnAttributeValueMapping mapping for objId="+objId);
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getLeadSource" + e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "PRRResultSet-getLeadSource-object(leadSourceString)=" + objId +"("+leadSource + ")");

		return leadSource;
	}

	public void printMe(ArrayList list)
	{
		HelperClass.porticoOutput(0, "PRRResultSet-printMe(Start)-------------");

		if(list != null && list.size() > 0)
		{
			for(int indx=0; indx < list.size(); indx++)
			{
				Object obj = list.get(indx);
                HelperClass.porticoOutput(0, "PRRResultSet-printMe="+obj.toString());
			}
		}

		HelperClass.porticoOutput(0, "PRRResultSet-printMe(End)-------------");
	}


// All static methods are below, to be self - contained in the parameters, must not use any pRResultSet instance
// Checked self-contained
// Move to probresreport.java/qc.java
    public static String getBatchInfo(IDfSession session, String batchObjId, String batchStat, String msgCount) throws Exception
    {
		HelperClass.porticoOutput(0, "PRRResultSet-getLookupServiceName - Start getBatchInfo for batchObjId="+batchObjId);
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        String rtnVal="";
        try
        {
            String gMC="";
            gMC="<br/>Active message count: "+msgCount;
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
                  // JIRA - CONPREP-1647 - ProviderId is the providerName, need not go further
                  String temp1=providerId; // getLookupServiceName("provider",providerId);
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
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getBatchInfo" + e.getMessage());
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

		HelperClass.porticoOutput(0, "PRRResultSet-getLookupServiceName - End getBatchInfo");

        return(rtnVal);
    }

// Checked self-contained
    public static String getLookupServiceName(String lookupService, String serviceId)
    {
		HelperClass.porticoOutput(0, "PRRResultSet-getLookupServiceName - Start (serviceId)="+serviceId);
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
        HelperClass.porticoOutput(0, "PRRResultSet-getLookupServiceName - End (serviceId)="+serviceId);
        return retServiceName;
    }

// Checked self-contained, not on Initial page, called only when clicked - OKAY performance wise
// In the new datamodel 1_1, given the accessionId(suStateId), we get the p_object_id(Documentum content objectId)
    public String getSuObjectId(String suStateId)
    {
		String suObjectId = "";

		if(listObjectStateObjectMapping.containsKey(suStateId))
		{
			suObjectId = (String)listObjectStateObjectMapping.get(suStateId);
		}
		else
		{
			// Try from listAddlnSUStateAttributeMapping

			if(listAddlnSUStateAttributeMapping != null &&
			       listAddlnSUStateAttributeMapping.size() > 0 &&
			       listAddlnSUStateAttributeMapping.containsKey(suStateId))
			{
				HelperClass.porticoOutput(0, "PRRResultSet-getSuObjectId - from listAddlnSUStateAttributeMapping");
				Hashtable attributeHash = (Hashtable)listAddlnSUStateAttributeMapping.get(suStateId);
				if(attributeHash != null &&
				      attributeHash.size() > 0 &&
				      attributeHash.containsKey(DBHelperClass.P_CONTENT_ID))
				{
		            suObjectId = (String)attributeHash.get(DBHelperClass.P_CONTENT_ID);
				}
			}
		}

		if(suObjectId == null || suObjectId.equals(""))
		{
			HelperClass.porticoOutput(1, "Unable to find a SU object for suStateId="+suStateId);
		}

        return suObjectId;
	}

// Checked self-contained
// RANGA New datamodel, use DBHelperClass.clearAllWarningMessages()'
    public static boolean clearAllWarnings(String bObjId, IDfSession session) throws Exception
    {
		HelperClass.porticoOutput(0, "PRRResultSet-clearAllWarnings - Start for batchId="+bObjId);

		boolean isSuccessful = true;

        try
        {
			String desc = ""; // No known description
			DBHelperClass.clearAllWarningMessages(bObjId, desc);
            // If warning messages were present and was cleared	return true
            // else return false.Anyway, it is better to refresh the page
            // whether the action was successful or not, so always return true
        }
        catch (Exception e)
        {
			isSuccessful = false;
            HelperClass.porticoOutput(1, "Exception in PRRResultSet-clearAllWarnings="+e.getMessage());
        }
        finally
        {
        }

		HelperClass.porticoOutput(0, "PRRResultSet-clearAllWarnings - End for batchObjectId,isSuccessful="+bObjId+","+isSuccessful);

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

    Hashtable listUserMessages;
    Hashtable listObjectStateAttributeValueMapping;
/*
    Hashtable listObjectStateAddlnAttributeValueMapping;
    Hashtable listObjectFUStateAttributeValueMapping;
*/
    Hashtable listMsgIdContextIdMapping;
	Hashtable listFatalUserMessage;
	Hashtable listWarningUserMessage;
	Hashtable listObjectStateObjectMapping;
	Hashtable listAddlnSUStateAttributeMapping;
    List allRowData;

    TableResultSet tableResultSet;
    public String m_batchfolderId;
    public String m_batchstatus;
    private IDfSession m_dfSession;
    private String m_batchname;
    private static final String XML_TAG="xml";
}

/*
    public static boolean clearAllWarnings(String bObjId, IDfSession session) throws Exception
    {
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        ArrayList userMsgId = new ArrayList();
        try
        {
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery();
            sb.append("select r_object_id from p_user_message where FOLDER(ID('");
            sb.append(bObjId);
            sb.append("'),DESCEND) and p_action_taken=false and p_severity=1");
            q.setDQL(sb.toString());
            idfcollection = q.execute(session, IDfQuery.DF_READ_QUERY);
            while(idfcollection.next())
            {
                userMsgId.add(idfcollection.getString("r_object_id"));
            }
            if(!userMsgId.isEmpty())
            {
              QcHelperClass.setUserMessages(session,userMsgId,bObjId,"");
              return(true);
              //onRender();
            }
            else
            {
                return(false);
		    }
        }
        catch (Exception e1)
        {
            throw e1;
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
    }
*/

// HelperClass.getStatusForBatchObject(IDfSession currentSession, String batchObjectId)
/*
    private void getBatchStatus() throws Exception
    {
        IDfCollection idfcollection = null;
        StringBuffer sb = new StringBuffer();
        try {
            String gMC="";
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery();
            sb.append("select object_name,p_provider_id,p_profile_id,p_state from p_batch where r_object_id='");
            sb.append(m_batchfolderId);
            sb.append("'");
            q.setDQL(sb.toString());
            idfcollection = q.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            String rtnVal="";
            while(idfcollection.next())
            {
                  m_batchstatus=idfcollection.getString(HelperClassConstants.BATCH_STATE);
                  break;
            }
        }
        catch (Exception e)
        {
			HelperClass.porticoOutput(1, "Exception in PRRResultSet-getBatchStatus" + e.getMessage());
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
    }
*/


/*
    public void loadUserMessages()
    {
		// RANGA, Future for 'listUserMessages' if sort to be maintained, use 'ArrayList' in place of Hashtable
		// So, that we can get away with tableResultSet.sort()
		// Performance Note: In the query 'orderByType' can be removed, if tableResultSet.sort() is used
		listUserMessages = new Hashtable();
		listMsgIdContextIdMapping = new Hashtable();
		listFatalUserMessage = new Hashtable();
		listWarningUserMessage = new Hashtable();

		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessages-Start");
		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessages OPEN IDfCollection");
		IDfCollection tIDfCollection = null;
		try
		{
			// Pick all the Message(s) order by severity 2(Fatal),1(Warning),0(Info) decending
            String attrNames = "p_context_id,p_code,p_text,p_category,p_severity,r_page_cnt,r_object_id"; // r_page_cnt
            String orderByName = "p_severity";
            String orderByType = "desc";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM p_user_message where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND) and p_action_taken = false" +
                                 " ORDER BY " + orderByName + " " + orderByType +" ";
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadUserMessages dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadUserMessages Completed query execute");
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					ArrayList alist = new ArrayList();
    				ValuePair valuePair = null;
					String msgObjectId = tIDfCollection.getString("r_object_id");

					String p_context_id = tIDfCollection.getString("p_context_id");
					valuePair = new ValuePair();
                    valuePair.setKey("p_context_id");
                    if(p_context_id == null)
                    {
						p_context_id = "";
					}
                    valuePair.setValue(p_context_id);
                    alist.add(valuePair);

                    String value = tIDfCollection.getString("p_code");
					valuePair = new ValuePair();
                    valuePair.setKey("p_code");
                    if(value == null)
                    {
						value = "";
					}
                    valuePair.setValue(value);
                    alist.add(valuePair);

                    value = tIDfCollection.getString("p_text");
					valuePair = new ValuePair();
                    valuePair.setKey("p_text");
                    if(value == null)
                    {
						value = "";
					}
                    valuePair.setValue(value);
                    alist.add(valuePair);

                    value = tIDfCollection.getString("p_category");
					valuePair = new ValuePair();
                    valuePair.setKey("p_category");
                    if(value == null)
                    {
						value = "";
					}
                    valuePair.setValue(value);
                    alist.add(valuePair);

                    value = tIDfCollection.getString("r_page_cnt");
					valuePair = new ValuePair();
                    valuePair.setKey("r_page_cnt");
                    if(value == null)
                    {
						value = "";
					}
                    valuePair.setValue(value);
                    alist.add(valuePair);

                    String p_severity = tIDfCollection.getString("p_severity");
					valuePair = new ValuePair();
                    valuePair.setKey("p_severity");
                    if(p_severity == null)
                    {
						p_severity = "";
					}
                    valuePair.setValue(p_severity);
                    alist.add(valuePair);

                    listUserMessages.put(msgObjectId, alist);
                    listMsgIdContextIdMapping.put(msgObjectId, p_context_id);
                    if(p_severity.equals("2"))
                    {
					    if(!listFatalUserMessage.containsKey(p_context_id))
					    {
                            listFatalUserMessage.put(p_context_id, p_context_id);
					    }
				    }
				    else if(p_severity.equals("1"))
				    {
					    if(!listWarningUserMessage.containsKey(p_context_id))
					    {
                            listWarningUserMessage.put(p_context_id, p_context_id);
					    }
					}
                    HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessages(msgObjectId,p_severity)="+msgObjectId+","+p_severity);
				}
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-PRRResultSet-loadUserMessages():"+e.getMessage());
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessages CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in PRRResultSet-loadUserMessages-close" + e.getMessage());
			}
		}

		HelperClass.porticoOutput(0, "PRRResultSet-loadUserMessages-End");
	}

    public void loadDynamicAttributeLookUp()
    {
        listObjectStateAttributeValueMapping = new Hashtable();
        listObjectStateAddlnAttributeValueMapping = new Hashtable();
        listObjectFUStateAttributeValueMapping = new Hashtable();
        listObjectStateObjectMapping = new Hashtable();

        boolean hasCuState = false;
        boolean hasSuState = false;

		HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp-Start");
		HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp OPEN IDfCollection");
		IDfCollection tIDfCollection = null;
		try
		{
            // listObjectStateAttributeValueMapping
			ValuePair valuePair = null;

            ArrayList alistBatch = new ArrayList();
			valuePair = new ValuePair();
            valuePair.setKey("i_folder_id");
            valuePair.setValue("");
            alistBatch.add(valuePair);

    		valuePair = new ValuePair();
            valuePair.setKey("object_name");
            valuePair.setValue(m_batchname);
            alistBatch.add(valuePair);

    		valuePair = new ValuePair();
            valuePair.setKey("r_object_type");
            valuePair.setValue("p_batch");
            alistBatch.add(valuePair);

            listObjectStateAttributeValueMapping.put(m_batchfolderId, alistBatch);

            tIDfCollection = null;
            String attrNames = "i_folder_id,r_object_id,object_name,r_object_type";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM dm_sysobject where r_object_type IN ("+
                               "'"+HelperClass.getInternalObjectType("cu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("fu_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("su_state")+"'" + "," +
                               "'"+HelperClass.getInternalObjectType("file_ref_object")+"'" +
                                ")" + " AND " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp dqlString="+dqlString);
			dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
            HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp Completed query execute");
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
					ArrayList alist = new ArrayList();
					String currentObjectId = tIDfCollection.getString("r_object_id");

					// Store only those Objects in our message context
					if(listMsgIdContextIdMapping.containsValue(currentObjectId))
					{
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

                        if(r_object_type.equals(HelperClass.getInternalObjectType("cu_state")))
                        {
							hasCuState = true;
						}
						else if(r_object_type.equals(HelperClass.getInternalObjectType("su_state")))
						{
							hasSuState = true;
						}
						else if(r_object_type.equals(HelperClass.getInternalObjectType("fu_state")))
						{
							listObjectFUStateAttributeValueMapping.put(currentObjectId, currentObjectId);
						}

                        HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(currentObjectId,parentObjectId,object_name,r_object_type)="+currentObjectId+","+parentObjectId+","+object_name+","+r_object_type);
				    }
				    else
				    {
						HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp-Ignored not in Msg Context(currentObjectId)="+currentObjectId);
					}
				}

				tIDfCollection.close();
			}

            // listObjectStateAddlnAttributeValueMapping
            tIDfCollection = null;
			if(hasCuState == true)
			{
				attrNames = "r_object_id,p_display_label";
                dqlString = "SELECT " + attrNames + " FROM p_cu_state where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_cu_state) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_cu_state) Completed query execute");
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("r_object_id");

    					if(listMsgIdContextIdMapping.containsValue(currentObjectId))
    					{
        					String displaylabel = tIDfCollection.getString("p_display_label");
        					valuePair = new ValuePair();
                            valuePair.setKey("p_display_label");
                            if(displaylabel == null)
                            {
        						displaylabel = "";
        					}
                            valuePair.setValue(displaylabel);
                            alist.add(valuePair);
                            listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);
                            HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(CU State Addln)(displaylabel)="+displaylabel);
					    }
					    else
					    {
							HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(p_cu_state)-Ignored not in Msg Context(currentObjectId)="+currentObjectId);
						}
					}
				}

				tIDfCollection.close();
			}

            // listObjectStateAddlnAttributeValueMapping
            tIDfCollection = null;
			if(hasSuState == true)
			{
				attrNames = "r_object_id,p_work_filename,p_virus_scanned,p_is_created_by_workflow,p_lead_source_id"; // p_lead_source_id
                dqlString = "SELECT " + attrNames + " FROM p_su_state where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";

                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_su_state) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_su_state) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("r_object_id");

    					if(listMsgIdContextIdMapping.containsValue(currentObjectId))
    					{
        					String workfilename = tIDfCollection.getString("p_work_filename");
                            boolean p_virus_scanned = tIDfCollection.getBoolean("p_virus_scanned");
                            boolean p_is_created_by_workflow = tIDfCollection.getBoolean("p_is_created_by_workflow");
                            String p_lead_source_id = tIDfCollection.getString("p_lead_source_id"); // p_lead_source_id, p_lead_source_id

    					    valuePair = new ValuePair();
                            valuePair.setKey("p_work_filename");
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

    					    valuePair = new ValuePair();
                            valuePair.setKey("p_lead_source_id"); // p_lead_source_id
                            valuePair.setValue(p_lead_source_id); // p_lead_source_id
                            alist.add(valuePair);

                            listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);
                            HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(SU State Addln)(workfilename)="+workfilename+","+
                                                        "p_virus_scanned="+p_virus_scanned+","+"p_is_created_by_workflow="+p_is_created_by_workflow+","+
                                                        "p_lead_source_id="+p_lead_source_id);// p_lead_source_id,p_lead_source_id
						}
					    else
					    {
							HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(p_su_state)-Ignored not in Msg Context(currentObjectId)="+currentObjectId);
						}
					}
				}

				tIDfCollection.close();

                tIDfCollection = null;
				attrNames = "i_folder_id,r_object_id";
                dqlString = "SELECT " + attrNames + " FROM p_su where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";
                HelperClass.porticoOutput(0, "Timing PRResultSet-loadDynamicAttributeLookUp(p_su) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing PRResultSet-loadDynamicAttributeLookUp(p_su) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String parentObjectId = tIDfCollection.getString("i_folder_id");
			    		String currentObjectId = tIDfCollection.getString("r_object_id");
			    		listObjectStateObjectMapping.put(parentObjectId, currentObjectId);

                        HelperClass.porticoOutput(0, "PRResultSet-loadDynamicAttributeLookUp(SU State-SU Object)="+parentObjectId+","+currentObjectId);
					}
				}

				tIDfCollection.close();
			}

            tIDfCollection = null;
			if(listObjectFUStateAttributeValueMapping != null && listObjectFUStateAttributeValueMapping.size() > 0)
			{
				attrNames = "i_folder_id,r_object_id,p_fu_type";
                dqlString = "SELECT " + attrNames + " FROM p_fu where " +
                                " FOLDER(ID(" + "'"+m_workPadId+"'" +
                                 "), DESCEND)";
                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_fu) dqlString="+dqlString);
			    dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(m_dfSession, IDfQuery.DF_READ_QUERY);
                HelperClass.porticoOutput(0, "Timing PRRResultSet-loadDynamicAttributeLookUp(p_fu) Completed query execute");
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {
			    		ArrayList alist = new ArrayList();
			    		String currentObjectId = tIDfCollection.getString("i_folder_id");
			    		if(listObjectFUStateAttributeValueMapping.containsKey(currentObjectId))
			    		{
    					    String futype = tIDfCollection.getString("p_fu_type");
        					valuePair = new ValuePair();
                            valuePair.setKey("p_fu_type");
                            if(futype == null)
                            {
    					    	futype = "";
    					    }
                            valuePair.setValue(futype);
                            alist.add(valuePair);
                            listObjectStateAddlnAttributeValueMapping.put(currentObjectId, alist);
                            HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp(FU Addln)(futype)="+futype);
					    }
					    else
					    {
							HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp-(FU State)Ignored not in Msg Context(currentObjectId)="+currentObjectId);
						}
					}
				}

				tIDfCollection.close();
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-PRRResultSet-loadDynamicAttributeLookUp():"+e.getMessage());
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in PRRResultSet-loadDynamicAttributeLookUp-close" + e.getMessage());
			}
		}

		HelperClass.porticoOutput(0, "PRRResultSet-loadDynamicAttributeLookUp-End");
	}
*/

