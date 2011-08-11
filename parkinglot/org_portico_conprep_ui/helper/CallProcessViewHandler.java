
package org.portico.conprep.ui.helper;

import java.util.ArrayList;
import java.util.Hashtable;

import org.portico.common.util.StringUtil;

import com.documentum.fc.client.IDfSession;

public class CallProcessViewHandler
{
	public static final String EXCLUDED_TAG = "excluded=";
	public static final String HAS_SUCCESSOR_TAG = "has-successor=";
	public static final String STATUS_TAG = "status=";
	public static final String ORIGIN_TAG = "origin=";
	public static final String COMMA_SEPARATOR = ",";

    // Internal
    private IDfSession currentSession = null;
    private String batchId = null;
    private String batchName = "";
    private String m_cuStateId = null;

    private ProcessViewFilter filter = null;

    // External
    private ArrayList resultList = new ArrayList(); // Contains 'ProcessViewResultItem' item objects

    // Internal
    private Hashtable lookupSuStateReason = null; // Should it be static ?
    private Hashtable lookupSuStateActive = null;
	private	ArrayList cuStateList = new ArrayList(); // List of CU States
	private	Hashtable listParentObject = new Hashtable(); // key=parentId, value=ArrayList-children objectId
	private Hashtable listObjectAttribute = new Hashtable(); // Initialize just in case, cotains (keyObjectId, hashAttributes)

    public CallProcessViewHandler()
    {
    }

    public CallProcessViewHandler(IDfSession iDfSession, String batchObjectId, String cuStateIdIn, ProcessViewFilter tFilter)
    {
		currentSession = iDfSession;
		batchId = batchObjectId;
		filter = tFilter;
		m_cuStateId = cuStateIdIn;
	}

	public String getBatchId()
	{
		return batchId;
	}

	public void setBatchName(String tBatchName)
	{
		batchName = tBatchName;
	}

	public String getBatchName()
	{
		return batchName;
	}

	public void setCuStateId(String cuStateIdIn)
	{
		m_cuStateId = cuStateIdIn;
	}

	public String getCustateId()
	{
		return m_cuStateId;
	}

	public ArrayList getProcessedItems()
	{
		return resultList;
	}

	public void clearProcessedItems()
	{
		resultList.clear();
	}

	public IDfSession getCurrentSession()
	{
		return currentSession;
	}

    public void processHandler()
    {
		try
		{
			initializeInfo();
			HelperClass.porticoOutput(0, "CallProcessViewHandler-processHandler Start traverseChildObject");
			processItems();
			HelperClass.porticoOutput(0, "CallProcessViewHandler-processHandler End traverseChildObject");
		}
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in CallProcessViewHandler-processHandler()" + e.getMessage());
		}
		finally
		{
		}
	}

	public void initializeInfo()
	{
		HelperClass.porticoOutput(0, "CallProcessViewHandler-initializeInfo() Start");
		loadStaticLookUp();
		setBatchName(HelperClass.getObjectName(currentSession, batchId, DBHelperClass.BATCH_TYPE));
		HelperClass.porticoOutput(0, "CallProcessViewHandler-initializeInfo() End");
	}

    // Pick all the DocApp related Static values - value assistance mapping etc. - Global based on objectType
    // RANGA NEW DATAMODEL use LOOKUP Tables
    public void loadStaticLookUp()
    {
        // lookupSuStateReason = new Hashtable();
        // lookupSuStateActive = new Hashtable(); // Currently not populated, the value by itself is meaningful

        // lookupSuStateReason = DBHelperClass.getLookupData(DBHelperClass.SU_TYPE, DBHelperClass.P_CONTENT_REASON);
    }

    public void processItems()
    {
		try
		{
			// All these attributes will be on the 'Oracle View' a union of different kinds of
			// objectType p_cu_state,p_fu_state,p_su_state
			/*
			ArrayList cuAttrListIn = new ArrayList();
			cuAttrListIn.add("p_accession_id");       // p_accession_id
			                                          // p_object_type*
			cuAttrListIn.add("p_name");               // p_name
			cuAttrListIn.add("p_batch_accession_id"); // p_parent_id*
			cuAttrListIn.add("p_sort_key");           // p_sort_key
			cuAttrListIn.add("p_display_label");      // p_display_label


			ArrayList fuAttrListIn = new ArrayList();
			fuAttrListIn.add("p_accession_id");       // p_accession_id
			                                          // p_object_type*
			fuAttrListIn.add("p_name");               // p_name
			fuAttrListIn.add("p_cu_accession_id");    // p_parent_id*
			fuAttrListIn.add("p_fu_type");            // p_fu_type

			ArrayList suAttrListIn = new ArrayList();
			suAttrListIn.add("p_accession_id");       // p_accession_id
			                                          // p_object_type*
			suAttrListIn.add("p_name");               // p_name
			suAttrListIn.add("p_fu_accession_id");    // p_parent_id*
			suAttrListIn.add("p_work_filename");      // p_work_filename
			suAttrListIn.add("p_status");             // p_status
			suAttrListIn.add("p_status_rationale");             // p_status_rationale
			suAttrListIn.add("p_origin");             // p_origin
			suAttrListIn.add("p_origin_rationale");             // p_origin_rationale
			suAttrListIn.add("p_is_excluded");            // p_is_excluded
			suAttrListIn.add("p_exclude_reason");            // p_exclude_reason
			suAttrListIn.add("p_has_successor");            // p_has_successor
			suAttrListIn.add("p_successor_reason");            // p_successor_reason
			suAttrListIn.add("p_content_reason");             // p_content_reason
			suAttrListIn.add("p_object_id");             // p_object_id(To view the Documentum Object)
			*/

            // Cleanup all the retrieved data
	    	resultList.clear();
	    	cuStateList.clear();
	    	listParentObject.clear();
	    	listObjectAttribute.clear();

			// listObjectAttribute has (key, hash)
			buildBatch("", "", "");
			listObjectAttribute = DBHelperClass.getContentTreeData(batchId,
			                                                          m_cuStateId,
	                                                                  cuStateList,
	                                                                  listParentObject);
            boolean isProcessable = true;
            if(cuStateList == null || cuStateList.size() == 0)
            {
				// isProcessable = false;
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-processItems-cuStateList is Empty-Still show orphans");
			}
            if(listParentObject == null || listParentObject.size() == 0)
            {
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-processItems-listParentObject is Empty");
			}
            if(listObjectAttribute == null || listObjectAttribute.size() == 0)
            {
				isProcessable = false;
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-processItems-listObjectAttribute is Empty");
			}

			if(isProcessable == true)
			{
    			// listObjectAttribute contains (keyObjectId, hashAttribute)
    			for(int cuIndx=0; cuIndx < cuStateList.size(); cuIndx++)
    			{
					String cuStateId = (String)cuStateList.get(cuIndx);
					buildCuState(batchId, cuStateId, "", ""); // parent, object, sortKeyPrefix, sortKeySuffix
					if(listParentObject.containsKey(cuStateId))
					{
						ArrayList fuStateList = (ArrayList)listParentObject.get(cuStateId);
						if(fuStateList != null && fuStateList.size() > 0)
						{
						    for(int fuIndx=0; fuIndx < fuStateList.size(); fuIndx++)
						    {
						    	String fuStateId = (String)fuStateList.get(fuIndx);
						    	buildFuState(cuStateId, fuStateId, "", ""); // parent, object, sortKeyPrefix, sortKeySuffix
						    	if(listParentObject.containsKey(fuStateId))
						    	{
						    		ArrayList suStateList = (ArrayList)listParentObject.get(fuStateId);
						    		if(suStateList != null && suStateList.size() > 0)
						    		{
										for(int suIndx=0; suIndx < suStateList.size(); suIndx++)
										{
											String suStateId = (String)suStateList.get(suIndx);
											buildSuState(fuStateId, suStateId, "", ""); // parent, object, sortKeyPrefix, sortKeySuffix
										}
									}
    							}
    							else
    							{
									HelperClass.porticoOutput(1, "Error CallProcessViewHandler-processItems-fuStateId NOT in listParentObject(Possibly No children for this fuState)-fuStateId="+fuStateId);
								}
	    					}
					    }
					}
					else
					{
						HelperClass.porticoOutput(1, "Error CallProcessViewHandler-processItems-cuStateId NOT in listParentObject(Possibly No children for this cuState)-cuStateId="+cuStateId);
					}
				}

				handleUnProcessedItems();// whose 'listParentObject' parent id = DBHelperClass.UNKNOWN
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-processItems():"+e.getMessage());
		}
		finally
		{
		}
	}

/*
traverseChildObject("", "", "", HelperClass.getInternalObjectType("workpad_batch"), getWorkPadId(), "");

	private void traverseChildObject(String parentKey,
	                                   String parentShortName,
	                                   String parentObjectType,
	                                   String currentObjectType,
	                                   String currentObjectId,
	                                   String incomingCurrentObjectName)
	currentObjectName = createOverrideItemFromFolderAddToList(parentKey, parentShortName, parentObjectType, currentObjectId, currentObjectType, currentObjectName);


*/

	public void buildBatch(String parentId, String sortKeyPrefix, String sortKeySuffix)
	{
		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildBatch-Start");

		String itemKey = "";
		String itemShortName = "";
		String itemObjectType = "";
		String itemDisplayName = "";
		String itemSortKey = "";
		String parentShortName = "";
		String parentObjectType = "";

		try
		{
			itemKey = batchId;
		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
		    tPorticoProcessViewResultItem.setThisKey(itemKey);
		    tPorticoProcessViewResultItem.setParentKey(parentId);

			itemShortName = batchName;
			itemObjectType = DBHelperClass.BATCH_TYPE;
			itemSortKey = "";
			itemDisplayName = "";

		    tPorticoProcessViewResultItem.setThisToken(itemShortName);
		    tPorticoProcessViewResultItem.setParentToken(parentShortName);
		    tPorticoProcessViewResultItem.setThisObjectType(itemObjectType);
		    tPorticoProcessViewResultItem.setParentObjectType(parentObjectType);

		    itemSortKey = sortKeyPrefix + itemObjectType + itemSortKey + itemKey + sortKeySuffix;
		    tPorticoProcessViewResultItem.setThisSortKey(itemSortKey);

			itemDisplayName = itemShortName;
		    tPorticoProcessViewResultItem.setThisDisplayToken(itemDisplayName);
    	    tPorticoProcessViewResultItem.setIsErroredItem(false);
		    resultList.add(tPorticoProcessViewResultItem);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-buildBatch():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildBatch-End");
	}


	// ParentObjectId, objectId
	public void buildCuState(String parentId, String cuStateId, String sortKeyPrefix, String sortKeySuffix)
	{
		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildCuState-Start");

		String itemKey = "";
		String itemShortName = "";
		String itemObjectType = "";
		String itemDisplayName = "";
		String itemSortKey = "";
		String parentShortName = "";
		String parentObjectType = "";

		try
		{
			itemKey = cuStateId;
			parentObjectType = DBHelperClass.BATCH_TYPE;
		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
		    tPorticoProcessViewResultItem.setThisKey(itemKey);
		    tPorticoProcessViewResultItem.setParentKey(parentId);
			if(listObjectAttribute.containsKey(itemKey))
			{
				Hashtable attrList = (Hashtable)listObjectAttribute.get(itemKey);
				if(attrList != null && attrList.size() > 0)
				{
					if(attrList.containsKey(DBHelperClass.P_NAME))
					{
						itemShortName = (String)attrList.get(DBHelperClass.P_NAME);
						if(itemShortName == null)
						{
							itemShortName = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_OBJECT_TYPE))
					{
						itemObjectType = (String)attrList.get(DBHelperClass.P_OBJECT_TYPE);
						if(itemObjectType == null)
						{
							itemObjectType = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_SORT_KEY))
					{
						itemSortKey = (String)attrList.get(DBHelperClass.P_SORT_KEY);
						if(itemSortKey == null)
						{
							itemSortKey = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_DISPLAY_LABEL))
					{
						itemDisplayName = (String)attrList.get(DBHelperClass.P_DISPLAY_LABEL);
						if(itemDisplayName == null)
						{
							itemDisplayName = "";
						}
					}
				}
			}
			else
			{
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-buildCuState-cuStateId NOT in listParentObject-cuStateId="+cuStateId);
			}
		    tPorticoProcessViewResultItem.setThisToken(itemShortName);
		    tPorticoProcessViewResultItem.setParentToken(parentShortName);
		    tPorticoProcessViewResultItem.setThisObjectType(itemObjectType);
		    tPorticoProcessViewResultItem.setParentObjectType(parentObjectType);

		    itemSortKey = sortKeyPrefix + itemObjectType + itemSortKey + itemKey + sortKeySuffix;
		    tPorticoProcessViewResultItem.setThisSortKey(itemSortKey);

			itemDisplayName = itemDisplayName + "(" + itemShortName + ")";
		    tPorticoProcessViewResultItem.setThisDisplayToken(itemDisplayName);
    	    tPorticoProcessViewResultItem.setIsErroredItem(false);
		    resultList.add(tPorticoProcessViewResultItem);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-buildCuState():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildCuState-End");
	}

	// ParentObjectId, objectId
	public void buildFuState(String parentId, String fuStateId, String sortKeyPrefix, String sortKeySuffix)
	{
		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildFuState-Start");

		String itemKey = "";
		String itemShortName = "";
		String itemObjectType = "";
		String itemDisplayName = "";
		String itemSortKey = "";
		String parentShortName = "";
		String parentObjectType = "";

		try
		{
			itemKey = fuStateId;
			parentObjectType = DBHelperClass.CU_TYPE;
		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
		    tPorticoProcessViewResultItem.setThisKey(itemKey);
		    tPorticoProcessViewResultItem.setParentKey(parentId);
			if(listObjectAttribute.containsKey(itemKey))
			{
				Hashtable attrList = (Hashtable)listObjectAttribute.get(itemKey);
				if(attrList != null && attrList.size() > 0)
				{
					if(attrList.containsKey(DBHelperClass.P_NAME))
					{
						itemShortName = (String)attrList.get(DBHelperClass.P_NAME);
						if(itemShortName == null)
						{
							itemShortName = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_OBJECT_TYPE))
					{
						itemObjectType = (String)attrList.get(DBHelperClass.P_OBJECT_TYPE);
						if(itemObjectType == null)
						{
							itemObjectType = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_FU_TYPE))
					{
						itemDisplayName = (String)attrList.get(DBHelperClass.P_FU_TYPE);
						if(itemDisplayName == null)
						{
							itemDisplayName = "";
						}
					}
				}
			}
			else
			{
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-buildFuState-fuStateId NOT in listParentObject-fuStateId="+fuStateId);
			}

		    tPorticoProcessViewResultItem.setThisToken(itemShortName);
		    tPorticoProcessViewResultItem.setParentToken(parentShortName);
		    tPorticoProcessViewResultItem.setThisObjectType(itemObjectType);
		    tPorticoProcessViewResultItem.setParentObjectType(parentObjectType);

   		    itemSortKey = QcHelperClass.getFuTypeSortKey(itemDisplayName);
   		    itemSortKey = sortKeyPrefix + itemObjectType + itemSortKey + itemKey + sortKeySuffix;
			tPorticoProcessViewResultItem.setThisSortKey(itemSortKey);

			itemDisplayName = itemDisplayName + "(" + itemShortName + ")";
		    tPorticoProcessViewResultItem.setThisDisplayToken(itemDisplayName);
    	    tPorticoProcessViewResultItem.setIsErroredItem(false);
		    resultList.add(tPorticoProcessViewResultItem);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-buildFuState():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildFuState-End");
	}

	// ParentObjectId, objectId
	public void buildSuState(String parentId, String suStateId, String sortKeyPrefix, String sortKeySuffix)
	{
		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildSuState-Start");

		String itemKey = "";
		String itemShortName = "";
		String itemObjectType = "";
		String itemDisplayName = "";
		String itemSortKey = "";
		String parentShortName = "";
		String parentObjectType = "";
        String addlnItemDisplayName = "";
        String contentObjectId = ""; // Refers to the Documentum ObjectId

		try
		{
			itemKey = suStateId;
			parentObjectType = DBHelperClass.FU_TYPE;
		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
		    tPorticoProcessViewResultItem.setThisKey(itemKey);
		    tPorticoProcessViewResultItem.setParentKey(parentId);
			if(listObjectAttribute.containsKey(itemKey))
			{
				Hashtable attrList = (Hashtable)listObjectAttribute.get(itemKey);
				if(attrList != null && attrList.size() > 0)
				{
					if(attrList.containsKey(DBHelperClass.P_NAME))
					{
						itemShortName = (String)attrList.get(DBHelperClass.P_NAME);
						if(itemShortName == null)
						{
							itemShortName = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_OBJECT_TYPE))
					{
						itemObjectType = (String)attrList.get(DBHelperClass.P_OBJECT_TYPE);
						if(itemObjectType == null)
						{
							itemObjectType = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_WORK_FILENAME))
					{
						itemDisplayName = (String)attrList.get(DBHelperClass.P_WORK_FILENAME);
						if(itemDisplayName == null)
						{
							itemDisplayName = "";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_IP_RULES_APPLIED))
					{
						String ruleId = (String)attrList.get(DBHelperClass.P_IP_RULES_APPLIED);
						if(null != ruleId)
						{
    					    addlnItemDisplayName = addlnItemDisplayName + "(" + ruleId + ")";
						}
					}
					if(attrList.containsKey(DBHelperClass.P_IS_EXCLUDED))
					{
						String excluded = (String)attrList.get(DBHelperClass.P_IS_EXCLUDED);
						if(excluded != null && excluded.equalsIgnoreCase(DBHelperClass.TRUE))
						{
							String excludedTagValue = excluded;
						    if(attrList.containsKey(DBHelperClass.P_EXCLUDE_REASON))
						    {
								excludedTagValue = excludedTagValue + COMMA_SEPARATOR + (String)attrList.get(DBHelperClass.P_EXCLUDE_REASON);
						    }
						    addlnItemDisplayName = addlnItemDisplayName + "(" + EXCLUDED_TAG + excludedTagValue + ")";
					    }
					}
					if(attrList.containsKey(DBHelperClass.P_HAS_SUCCESSOR))
					{
						String hasSuccessor = (String)attrList.get(DBHelperClass.P_HAS_SUCCESSOR);
						if(hasSuccessor != null && hasSuccessor.equalsIgnoreCase(DBHelperClass.TRUE))
						{
							String hasSuccessorTagValue = hasSuccessor;
						    if(attrList.containsKey(DBHelperClass.P_SUCCESSOR_REASON))
						    {
								hasSuccessorTagValue = hasSuccessorTagValue + COMMA_SEPARATOR + (String)attrList.get(DBHelperClass.P_SUCCESSOR_REASON);
						    }
						    addlnItemDisplayName = addlnItemDisplayName + "(" + HAS_SUCCESSOR_TAG + hasSuccessorTagValue + ")";
					    }
					}
					if(attrList.containsKey(DBHelperClass.P_STATUS))
					{
                        // PMD2.0
						String status = (String)attrList.get(DBHelperClass.P_STATUS);
						if(!StringUtil.isEmpty(status))
						{
							addlnItemDisplayName = addlnItemDisplayName + "(";
						    addlnItemDisplayName = addlnItemDisplayName + STATUS_TAG + status;
        					if(attrList.containsKey(DBHelperClass.P_STATUS_RATIONALE))
        					{
                                // PMD2.0
        						String statusRationale = (String)attrList.get(DBHelperClass.P_STATUS_RATIONALE);
        						if(!StringUtil.isEmpty(statusRationale))
        						{
        						    addlnItemDisplayName = addlnItemDisplayName + "," + statusRationale;
        					    }
        					}
						    addlnItemDisplayName = addlnItemDisplayName + ")";
     				    }
					}
					if(attrList.containsKey(DBHelperClass.P_ORIGIN))
					{
                        // PMD2.0
						String origin = (String)attrList.get(DBHelperClass.P_ORIGIN);
						if(!StringUtil.isEmpty(origin))
						{
							addlnItemDisplayName = addlnItemDisplayName + "(";
						    addlnItemDisplayName = addlnItemDisplayName + ORIGIN_TAG + origin;
        					if(attrList.containsKey(DBHelperClass.P_ORIGIN_RATIONALE))
        					{
                                // PMD2.0
        						String originRationale = (String)attrList.get(DBHelperClass.P_ORIGIN_RATIONALE);
        						if(!StringUtil.isEmpty(originRationale))
        						{
        						    addlnItemDisplayName = addlnItemDisplayName + "," + originRationale;
        					    }
        					}
						    addlnItemDisplayName = addlnItemDisplayName + ")";
     				    }
					}
					if(attrList.containsKey(DBHelperClass.P_CONTENT_ID))
					{
						contentObjectId = (String)attrList.get(DBHelperClass.P_CONTENT_ID);
						if(contentObjectId == null)
						{
							contentObjectId = "";
					    }
					}
				}
			}
			else
			{
				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-buildSuState-suStateId NOT in listParentObject-suStateId="+suStateId);
			}

		    tPorticoProcessViewResultItem.setThisToken(itemShortName);
		    tPorticoProcessViewResultItem.setParentToken(parentShortName);
		    tPorticoProcessViewResultItem.setThisObjectType(itemObjectType);
		    tPorticoProcessViewResultItem.setParentObjectType(parentObjectType);

            itemSortKey = sortKeyPrefix + itemObjectType + itemKey + sortKeySuffix;
			tPorticoProcessViewResultItem.setThisSortKey(itemSortKey);

			itemDisplayName = itemDisplayName + "(" + itemShortName + ")" + addlnItemDisplayName;
		    tPorticoProcessViewResultItem.setThisDisplayToken(itemDisplayName);
    	    tPorticoProcessViewResultItem.setIsErroredItem(false);
    	    tPorticoProcessViewResultItem.setContentObjectId(contentObjectId);
		    resultList.add(tPorticoProcessViewResultItem);
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-buildSuState():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallProcessViewHandler-buildSuState-End");
	}

	public void handleUnProcessedItems()
	{
		try
		{
			// The DBHelperClass.UNKNOWN will be populated by the DBHelperClass query result,
			// if the object(s) parent is null OR not populated
			if(listParentObject.containsKey(DBHelperClass.UNKNOWN))
			{
				ArrayList unprocessedItems = (ArrayList)listParentObject.get(DBHelperClass.UNKNOWN);
				if(unprocessedItems != null && unprocessedItems.size() > 0)
				{
					for(int uIndx=0; uIndx < unprocessedItems.size(); uIndx++)
					{
						String objectId = (String)unprocessedItems.get(uIndx);
						String itemObjectType = "";
						if(listObjectAttribute.containsKey(objectId))
						{
							Hashtable attrList = (Hashtable)listObjectAttribute.get(objectId);
            				if(attrList != null && attrList.size() > 0)
            				{
            					if(attrList.containsKey(DBHelperClass.P_OBJECT_TYPE))
            					{
            						itemObjectType = (String)attrList.get(DBHelperClass.P_OBJECT_TYPE);
            						if(itemObjectType == null)
             						{
            							itemObjectType = "";
             						}
            					}
							}
						}
						else
						{
							HelperClass.porticoOutput(1, "Error CallProcessViewHandler-handleUnProcessedItems-objectId NOT in listObjectAttribute-objectId="+objectId);
						}

						if(itemObjectType != null && !itemObjectType.equals(""))
						{
							if(itemObjectType.equals(DBHelperClass.CU_TYPE))
							{
								buildCuState(batchId, objectId, "z", ""); // "z" to list at the end
							}
							else if(itemObjectType.equals(DBHelperClass.FU_TYPE))
							{
								buildFuState(batchId, objectId, "z", ""); // "z" to list at the end
							}
							else if(itemObjectType.equals(DBHelperClass.SU_TYPE))
							{
								buildSuState(batchId, objectId, "z", ""); // "z" to list at the end
							}
							else
							{
								HelperClass.porticoOutput(1, "Error CallProcessViewHandler-handleUnProcessedItems-Unknown itemObjectType="+itemObjectType + " for objectId="+objectId);
							}
						}
						else
						{
            				HelperClass.porticoOutput(1, "Error CallProcessViewHandler-handleUnProcessedItems-itemObjectType is NULL or Empty for objectId="+objectId);
						}
				    }
				}
			}
			else
			{
				HelperClass.porticoOutput(0, "CallProcessViewHandler-handleUnProcessedItems-NO Unprocessed Items");
			}
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception-CallProcessViewHandler-handleUnProcessedItems():"+e.getMessage());
		}
		finally
		{
		}
	}
}