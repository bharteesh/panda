
package org.portico.conprep.ui.helper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.portico.common.events.KeyMetadataElementsConstants;
import org.portico.common.messagelookup.MessageLookupService;
import org.portico.common.messagelookup.MessageLookupServiceFactory;
import org.portico.common.messagelookup.MessageLookupUtil;
import org.portico.common.messagelookup.facade.MessageSetFacade;
import org.portico.common.messagelookup.xml.MessageType;
import org.portico.common.messagelookup.xml.VariableTextType;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;

public class CallAddUserMessageHandler // extends CallProcessViewHandler
{
    // Internal
    private static final String USERMESSAGE_PREFIX = "U";
    private static final String USER_CATEGORY = "User";
    private MessageLookupService messageLookupService;
    private IDfSession currentSession = null;
    private String batchId = null;

    public CallAddUserMessageHandler()
    {
    }

    public CallAddUserMessageHandler(IDfSession iDfSession, String batchObjectId)
    {
		currentSession = iDfSession;
		batchId = batchObjectId;
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-constructor call");
	}

	public String getBatchId()
	{
		return batchId;
	}

	public IDfSession getCurrentSession()
	{
		return currentSession;
	}

    public void processHandler()
    {
		try
		{
			HelperClass.porticoOutput(0, "CallAddUserMessageHandler-processHandler initialize MessageLookupServiceFactory Start");
			MessageLookupServiceFactory messageLookupServiceFactory = MessageLookupUtil.getFactory();
			messageLookupService = messageLookupServiceFactory.createService();

            if(messageLookupService == null)
            {
				HelperClass.porticoOutput(1, "ERROR - CallAddUserMessageHandler-processHandler Unable to create messageLookupService");
			}

			HelperClass.porticoOutput(0, "CallAddUserMessageHandler-processHandler initialize MessageLookupServiceFactory End");
		}
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in CallAddUserMessageHandler-processHandler()" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public TreeMap getSortedContextObjects(String objectType, String cuStateId)
	{
		TreeMap sortedItemMap = null;
		if(objectType.equalsIgnoreCase(DBHelperClass.SU_TYPE))
		{
			sortedItemMap = getSortedSuStatesPerArticle(cuStateId);
		}
		else if(objectType.equalsIgnoreCase(DBHelperClass.FU_TYPE))
		{
			sortedItemMap = getSortedFuStatesPerArticle(cuStateId);
		}
		else if(objectType.equalsIgnoreCase(DBHelperClass.CU_TYPE))
		{
			sortedItemMap = getArticleDetails(cuStateId);
		}
		else if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
		{
			sortedItemMap = getBatchDetails();
		}
		else
		{
			HelperClass.porticoOutput(1, "CallAddUserMessageHandler-getSortedContextObjects()-Unknown objectType="+objectType);
		}

		return sortedItemMap;
	}

    public TreeMap getSortedArticles()
    {
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedArticles()-Start");
		TreeMap sortedItemMap = new TreeMap();
		try
		{
			ArrayList attrList = new ArrayList();
			attrList.add(DBHelperClass.P_ACCESSION_ID);
			attrList.add(DBHelperClass.P_NAME);
			attrList.add(DBHelperClass.P_SORT_KEY);
			attrList.add(DBHelperClass.P_DISPLAY_LABEL);
			Hashtable articles = DBHelperClass.getArticles(batchId, attrList);
			if(articles != null && articles.size() > 0)
			{
                Enumeration enumerate = articles.keys();
                while(enumerate.hasMoreElements())
                {
                    String cuStateId = (String)enumerate.nextElement();
				    Hashtable alistOut = (Hashtable)articles.get(cuStateId);
				    if(alistOut != null && alistOut.size() > 0)
				    {
               		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
               		    tPorticoProcessViewResultItem.setThisKey(cuStateId);
               		    tPorticoProcessViewResultItem.setParentKey("");
   		                tPorticoProcessViewResultItem.setParentToken("");
   		                tPorticoProcessViewResultItem.setParentObjectType("");
   		                tPorticoProcessViewResultItem.setThisObjectType(DBHelperClass.CU_TYPE);
						ValuePair oValuePair = null;
						String objectName = "";
						String displayLabel = "";
						String sortKey = "";

					    if(alistOut.containsKey(DBHelperClass.P_NAME))
					    {
					    	objectName = (String)alistOut.get(DBHelperClass.P_NAME);
					    }
					    if(alistOut.containsKey(DBHelperClass.P_SORT_KEY))
					    {
					    	sortKey = (String)alistOut.get(DBHelperClass.P_SORT_KEY);
					    }
					    if(alistOut.containsKey(DBHelperClass.P_DISPLAY_LABEL))
					    {
					    	displayLabel = (String)alistOut.get(DBHelperClass.P_DISPLAY_LABEL);
					    }
               		    tPorticoProcessViewResultItem.setThisToken(objectName);
						tPorticoProcessViewResultItem.setThisSortKey(sortKey);
						displayLabel += "("+objectName+")";
						tPorticoProcessViewResultItem.setThisDisplayToken(displayLabel);
   	                    tPorticoProcessViewResultItem.setIsErroredItem(false);
						if(sortKey != null && !sortKey.equals(""))
						{
						    sortedItemMap.put(sortKey, tPorticoProcessViewResultItem);
						}
						else
						{
							HelperClass.porticoOutput(1, "Error in CallAddUserMessageHandler-getSortedArticleObjects()-Sort key NOT found for article-cuStateId=" + cuStateId);
						}
					}
				}
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in CallAddUserMessageHandler-getSortedArticleObjects()" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedArticles()-End");

		return sortedItemMap;
	}

	public TreeMap getSortedSuStatesPerArticle(String cuStateId)
	{
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedSuStatesPerArticle()-Start-cuStateId="+cuStateId);

		TreeMap sortedItemMap = new TreeMap();
		ArrayList attrListIn = new ArrayList();
		attrListIn.add(DBHelperClass.P_ACCESSION_ID);
		attrListIn.add(DBHelperClass.P_WORK_FILENAME);
		attrListIn.add(DBHelperClass.P_NAME);
		attrListIn.add(DBHelperClass.P_STATUS);
		attrListIn.add(DBHelperClass.P_CONTENT_ID);
		attrListIn.add(DBHelperClass.P_LEAD_SOURCE_ID);
		List leadMDList = QcHelperClass.getLeadMetadataInfo(currentSession, batchId, cuStateId);
		Hashtable suStates = DBHelperClass.getSuStatesPerArticle(cuStateId, attrListIn);
		if(suStates != null && suStates.size() > 0)
		{
    		String leadSourceSuStateId = getLeadSourceGivenSuStates(suStates);
            Enumeration enumerate = suStates.keys();
            while(enumerate.hasMoreElements())
            {
                String suStateId = (String)enumerate.nextElement();
    		    Hashtable alistOut = (Hashtable)suStates.get(suStateId);
			    if(alistOut != null && alistOut.size() > 0)
			    {
           		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
           		    tPorticoProcessViewResultItem.setThisKey(suStateId);
           		    tPorticoProcessViewResultItem.setParentKey("");
	                tPorticoProcessViewResultItem.setParentToken("");
	                tPorticoProcessViewResultItem.setParentObjectType("");
	                tPorticoProcessViewResultItem.setThisObjectType(DBHelperClass.SU_TYPE);
					String objectName = "";
					String workFileName = "";
					String status = "";
					String leadSourceIds = "";

					String displayLabel = "";
					String sortKey = "";

					if(alistOut.containsKey(DBHelperClass.P_NAME))
					{
						objectName = (String)alistOut.get(DBHelperClass.P_NAME);
					}
					if(alistOut.containsKey(DBHelperClass.P_WORK_FILENAME))
					{
						workFileName = (String)alistOut.get(DBHelperClass.P_WORK_FILENAME);
					}
					if(alistOut.containsKey(DBHelperClass.P_STATUS))
					{
						status = (String)alistOut.get(DBHelperClass.P_STATUS);
					}
					if(alistOut.containsKey(DBHelperClass.P_LEAD_SOURCE_ID))
					{
						leadSourceIds = (String)alistOut.get(DBHelperClass.P_LEAD_SOURCE_ID);
					}
                    // Populate only active SU States(s)
					if(status.equalsIgnoreCase(KeyMetadataElementsConstants.SU_STATUS_ACTIVE))
					{
               		    tPorticoProcessViewResultItem.setThisToken(objectName);
                        sortKey = workFileName;
                        tPorticoProcessViewResultItem.setThisSortKey(sortKey);
                        displayLabel = workFileName + "("+objectName+")";
					    tPorticoProcessViewResultItem.setThisDisplayToken(displayLabel);
                        tPorticoProcessViewResultItem.setIsErroredItem(leadMDList.contains(suStateId));
					    if(sortKey != null && !sortKey.equals(""))
					    {
					    	// RANGA, check this if we could have 2 SU(s) with same workfilename
					        sortedItemMap.put(sortKey, tPorticoProcessViewResultItem);
					    }
						else
						{
							HelperClass.porticoOutput(1, "Error in CallAddUserMessageHandler-getSortedSuStatesPerArticle()-Sort key(workFileName) NOT found for suStateId=" + suStateId);
						}
				    }
				}
			}
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedSuStatesPerArticle()-End-cuStateId="+cuStateId);


		return sortedItemMap;
	}

	public TreeMap getSortedFuStatesPerArticle(String cuStateId)
	{
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedFuStatesPerArticle()-Start-cuStateId="+cuStateId);

		TreeMap sortedItemMap = new TreeMap();
		ArrayList attrListIn = new ArrayList();
		attrListIn.add(DBHelperClass.P_ACCESSION_ID);
		attrListIn.add(DBHelperClass.P_FU_TYPE);
		attrListIn.add(DBHelperClass.P_NAME);
		Hashtable fuStates = DBHelperClass.getFuStatesPerArticle(cuStateId, attrListIn);

		if(fuStates != null && fuStates.size() > 0)
		{
            Enumeration enumerate = fuStates.keys();
            while(enumerate.hasMoreElements())
            {
                String fuStateId = (String)enumerate.nextElement();
    		    Hashtable alistOut = (Hashtable)fuStates.get(fuStateId);
			    if(alistOut != null && alistOut.size() > 0)
			    {
           		    ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
           		    tPorticoProcessViewResultItem.setThisKey(fuStateId);
           		    tPorticoProcessViewResultItem.setParentKey("");
	                tPorticoProcessViewResultItem.setParentToken("");
	                tPorticoProcessViewResultItem.setParentObjectType("");
	                tPorticoProcessViewResultItem.setThisObjectType(DBHelperClass.FU_TYPE);
					String objectName = "";
					String fuType = "";

					String displayLabel = "";
					String sortKey = "";

					if(alistOut.containsKey(DBHelperClass.P_NAME))
					{
						objectName = (String)alistOut.get(DBHelperClass.P_NAME);
					}
					if(alistOut.containsKey(DBHelperClass.P_FU_TYPE))
					{
						fuType = (String)alistOut.get(DBHelperClass.P_FU_TYPE);
					}

           		    tPorticoProcessViewResultItem.setThisToken(objectName);
                    sortKey = fuType + objectName;
    				tPorticoProcessViewResultItem.setThisSortKey(sortKey);
                    displayLabel = fuType + "("+objectName+")";
				    tPorticoProcessViewResultItem.setThisDisplayToken(displayLabel);
                    tPorticoProcessViewResultItem.setIsErroredItem(false);
    			    if(sortKey != null && !sortKey.equals(""))
				    {
				        sortedItemMap.put(sortKey, tPorticoProcessViewResultItem);
				    }
					else
					{
						HelperClass.porticoOutput(1, "Error in CallAddUserMessageHandler-getSortedFuStatesPerArticle()-Sort key(fuType and objectName) NOT found for fuStateId=" + fuStateId);
					}
				}
			}
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getSortedFuStatesPerArticle()-End-cuStateId="+cuStateId);

		return sortedItemMap;
	}

    public TreeMap getArticleDetails(String cuStateId)
    {
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getArticleDetails()-Start-cuStateId="+cuStateId);

		TreeMap sortedItemMap = new TreeMap();
		try
		{
			ArrayList attrList = new ArrayList();
			attrList.add(DBHelperClass.P_ACCESSION_ID);
			attrList.add(DBHelperClass.P_NAME);
			attrList.add(DBHelperClass.P_SORT_KEY);
			attrList.add(DBHelperClass.P_DISPLAY_LABEL);
			Hashtable article = DBHelperClass.getObjectAttributes(DBHelperClass.CU_TYPE, cuStateId, attrList);
			if(article != null && article.size() > 0)
			{
				ValuePair oValuePair = null;
				String objectName = "";
				String displayLabel = "";
				String sortKey = "";

               	ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
               	tPorticoProcessViewResultItem.setThisKey(cuStateId);
               	tPorticoProcessViewResultItem.setParentKey("");
   		        tPorticoProcessViewResultItem.setParentToken("");
   		        tPorticoProcessViewResultItem.setParentObjectType("");
   		        tPorticoProcessViewResultItem.setThisObjectType(DBHelperClass.CU_TYPE);
   		        if(article.containsKey(DBHelperClass.P_NAME))
   		        {
					objectName = (String)article.get(DBHelperClass.P_NAME);
			    }
   		        if(article.containsKey(DBHelperClass.P_SORT_KEY))
   		        {
					sortKey = (String)article.get(DBHelperClass.P_SORT_KEY);

			    }
   		        if(article.containsKey(DBHelperClass.P_DISPLAY_LABEL))
   		        {
					displayLabel = (String)article.get(DBHelperClass.P_DISPLAY_LABEL);
			    }
                tPorticoProcessViewResultItem.setThisToken(objectName);
                tPorticoProcessViewResultItem.setThisSortKey(sortKey);
			    displayLabel += "("+objectName+")";
				tPorticoProcessViewResultItem.setThisDisplayToken(displayLabel);
   	            tPorticoProcessViewResultItem.setIsErroredItem(false);
				if(sortKey != null && !sortKey.equals(""))
				{
					sortedItemMap.put(sortKey, tPorticoProcessViewResultItem);
				}
				else
				{
					HelperClass.porticoOutput(1, "Error in CallAddUserMessageHandler-getArticleDetails()-Sort key NOT found for article-cuStateId=" + cuStateId);
				}
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in CallAddUserMessageHandler-getArticleDetails()" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getArticleDetails()-End-cuStateId="+cuStateId);

		return sortedItemMap;
	}

    public TreeMap getBatchDetails()
    {
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getBatchDetails()-Start");

		TreeMap sortedItemMap = new TreeMap();
		try
		{
			String qualification = DBHelperClass.BATCH_TYPE + " WHERE r_object_id="+
			                       "'"+batchId+"'";
            HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getBatchDetails-qualification="+qualification);
            IDfSysObject iDfSysObject = (IDfSysObject) currentSession.getObjectByQualification(qualification);
			if(null != iDfSysObject)
			{
				String objectName = "";
				String displayLabel = "";
				String sortKey = "";

               	ProcessViewResultItem tPorticoProcessViewResultItem = new ProcessViewResultItem();
               	tPorticoProcessViewResultItem.setThisKey(batchId);
               	tPorticoProcessViewResultItem.setParentKey("");
   		        tPorticoProcessViewResultItem.setParentToken("");
   		        tPorticoProcessViewResultItem.setParentObjectType("");
   		        tPorticoProcessViewResultItem.setThisObjectType(DBHelperClass.BATCH_TYPE);

				objectName = iDfSysObject.getObjectName();
				displayLabel = objectName;
				sortKey = objectName;
                tPorticoProcessViewResultItem.setThisToken(objectName);
                tPorticoProcessViewResultItem.setThisSortKey(sortKey);
				tPorticoProcessViewResultItem.setThisDisplayToken(displayLabel);
   	            tPorticoProcessViewResultItem.setIsErroredItem(false);
			    if(sortKey != null && !sortKey.equals(""))
			    {
			    	sortedItemMap.put(sortKey, tPorticoProcessViewResultItem);
			    }
				else
				{
					HelperClass.porticoOutput(1, "Error in CallAddUserMessageHandler-getBatchDetails()-Sort key(objectName) NOT found for batch-batchObjectId=" + batchId);
				}
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in CallAddUserMessageHandler-getBatchDetails()" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getBatchDetails()-End");

		return sortedItemMap;
	}

    public String getLeadSourceGivenSuStates(Hashtable suStates)
    {
		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getLeadSourceGivenSuStates()-Start");

		String leadSourceSuStateId = "";
		String leadSourceSuObjectId = "";
		Hashtable suStateAssociatedObjectMapping = new Hashtable();

		if(suStates != null && suStates.size() > 0)
		{
            Enumeration enumerate = suStates.keys();
            while(enumerate.hasMoreElements())
            {
                String suStateId = (String)enumerate.nextElement();
    		    Hashtable alistOut = (Hashtable)suStates.get(suStateId);
    		    String leadSourceIds = "";
    		    String associatedObjectId = "";
			    if(alistOut != null && alistOut.size() > 0)
			    {
					if(alistOut.containsKey(DBHelperClass.P_LEAD_SOURCE_ID))
					{
						leadSourceIds = (String)alistOut.get(DBHelperClass.P_LEAD_SOURCE_ID);
					}
					if(alistOut.containsKey(DBHelperClass.P_CONTENT_ID))
					{
						associatedObjectId = (String)alistOut.get(DBHelperClass.P_CONTENT_ID);
						if(suStateAssociatedObjectMapping != null)
						{
						    if(suStateAssociatedObjectMapping.size() > 0 && suStateAssociatedObjectMapping.containsKey(associatedObjectId))
						    {
								HelperClass.porticoOutput(0, "Error-CallAddUserMessageHandler-getLeadSourceGivenSuStates-2 SU States associated with same file object-fileObjectId="+
									                              associatedObjectId+
									                              ",SuStates="+
									                              (String)suStateAssociatedObjectMapping.get(associatedObjectId)+
									                              ","+suStateId);
							}
							else
							{
					        	suStateAssociatedObjectMapping.put(associatedObjectId,suStateId);
					        }
						}
					}
				}
				if(leadSourceIds != null && !leadSourceIds.equals(""))
				{
					// Note: leadSourceIds contains leadSourceSUObject,leadTargetSUObject
                    StringTokenizer strTokenizer = new StringTokenizer(leadSourceIds, ",");
                    while (strTokenizer.hasMoreTokens())
                    {
    			        leadSourceSuObjectId =  strTokenizer.nextToken().trim();
    			        HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getLeadSourceGivenSuStates-leadSourceSuObjectId="+leadSourceSuObjectId);
        			    break;
                    }
				}
			}

			if(leadSourceSuObjectId != null && !leadSourceSuObjectId.equals(""))
			{
				if(suStateAssociatedObjectMapping != null && suStateAssociatedObjectMapping.size() > 0)
				{
					if(suStateAssociatedObjectMapping.containsKey(leadSourceSuObjectId))
					{
						leadSourceSuStateId = (String)suStateAssociatedObjectMapping.get(leadSourceSuObjectId);
					}
				}
			}
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getLeadSourceGivenSuStates()-End-leadSourceSuStateId="+leadSourceSuStateId);

		return leadSourceSuStateId;
	}

    // From MessageMapping xml
	public ArrayList getUserMessageCodeList()
	{

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getUserMessageCodeList Start");

		ArrayList userMessageCodeList = new ArrayList();

		ValuePair valuePair = null;

		try
		{
		    MessageSetFacade messageSetFacade = messageLookupService.getMessageSet();
		    if(messageSetFacade != null)
		    {
				List messageCodeList = messageSetFacade.getMessageCodes();
				if(messageCodeList != null && messageCodeList.size() > 0)
				{
		            for(int indx=0; indx < messageCodeList.size(); indx++)
		            {
                        String messageCode = (String)messageCodeList.get(indx);
                        MessageType messageType = messageLookupService.getMessage(messageCode);
                        if(messageType != null && messageType.getCategory().equals(USER_CATEGORY))
                        {
		         		    valuePair = new ValuePair();
		        		    valuePair.setKey(messageCode);
		        			valuePair.setValue(messageCode);
                            userMessageCodeList.add(valuePair);
						}
/*
                        if(messageCode.startsWith(USERMESSAGE_PREFIX))
                        {
		         		    valuePair = new ValuePair();
		        		    valuePair.setKey(messageCode);
		        			valuePair.setValue(messageCode);
                            userMessageCodeList.add(valuePair);
		        		}
*/
		        	}
		        }
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception-CallAddUserMessageHandler-getUserMessageCodeList():"+e.getMessage());
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "CallAddUserMessageHandler-getUserMessageCodeList End");

		return userMessageCodeList;
	}

    // From MessageMapping xml
	public String getSeverity(String userMessageCode)
	{
		String severity = "";
		try
		{
		    MessageType messageType = messageLookupService.getMessage(userMessageCode);
		    severity = messageType.getSeverity().value();
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception-CallAddUserMessageHandler-getSeverity():"+e.getMessage());
		}
		finally
		{
		}

		return severity;
	}

    // From MessageMapping xml
	public String getDescription(String userMessageCode)
	{
		String description = "";
		try
		{
		    MessageType messageType = messageLookupService.getMessage(userMessageCode);
		    VariableTextType variableTextType = messageType.getText();
		    description = variableTextType.getValue();
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception-CallAddUserMessageHandler-getDescription():"+e.getMessage());
		}
		finally
		{
		}

		return description;
	}


    // From ActionConfig.xml
	public String getUserMessageCodeContext(String userMessageCode)
	{
		String userMessageCodeContext = "";
        userMessageCodeContext = DBHelperClass.SU_TYPE;
		return userMessageCodeContext; // Tested fine for p_su_state, p_fu_state, p_cu_state, p_batch
	}
}