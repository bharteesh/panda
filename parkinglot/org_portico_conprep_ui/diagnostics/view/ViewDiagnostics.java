
package org.portico.conprep.ui.diagnostics.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.workflow.render.Renderer;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;


public class ViewDiagnostics extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ViewDiagnostics()
    {
        m_batchNameLabel = null;
        m_articleNameLabel = null;
        m_strCUStateObjectId = null;
        m_strObjectId = null;
        m_strBatchFolderId = "";
        m_strBatchAccessionId = "";
        m_registeredRendererList = null;
        listObjectStateAttributes = new Hashtable();

        m_ObjectPredecessorMapping = new Hashtable();
        m_allMDList = new TreeMap();
        m_allPdfList = new TreeMap();
        m_maxMDColumnCount = 0;
        m_maxPageImageColumnCount = 0;
	}

    public void onInit(ArgumentList argumentlist){
        super.onInit(argumentlist);

        m_strObjectId = argumentlist.get("accessionId");

        HelperClass.porticoOutput("ViewDiagnostics-onInit()-Argument Object Id=" + m_strObjectId);

		m_batchNameLabel = (Label)getControl("batch_name", com.documentum.web.form.control.Label.class);
		m_articleNameLabel = (Label)getControl("article_name", com.documentum.web.form.control.Label.class);

        initializeCommonData();
        initializeCommonControls();
    }

    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void initializeCommonData()
    {
    	m_strBatchFolderId = HelperClass.getParentBatchFolderId(getDfSession(), m_strObjectId);
    	m_strBatchAccessionId = DBHelperClass.getBatchAccessionIdFromBatchId(m_strBatchFolderId);
		m_strCUStateObjectId = HelperClass.getParentArticleId(getDfSession(), m_strObjectId);

		if(m_strCUStateObjectId != null && !m_strCUStateObjectId.equals(""))
		{
			loadDynamicData();
	        populateLeadMetaDataFileList();
	        populateRegisteredRendererList();
	        populatePageImageList();
	    }
	}

    public void initializeCommonControls()
    {
		if(m_strBatchFolderId != null && !m_strBatchFolderId.equals(""))
		{
	    	m_batchNameLabel.setLabel(HelperClass.getObjectName(getDfSession(), m_strBatchFolderId, DBHelperClass.BATCH_TYPE));
	    }

        if(m_strCUStateObjectId != null && !m_strCUStateObjectId.equals(""))
        {
	    	m_articleNameLabel.setLabel(QcHelperClass.getDisplayName(getDfSession(), m_strCUStateObjectId, DBHelperClass.CU_TYPE));
	    }
	    else
	    {
			m_articleNameLabel.setLabel(NO_ARTICLE_FOUND);
		}
	}

	// listObjectStateAttributes
	public void loadDynamicData()
	{
		ArrayList alistIn = new ArrayList();
		alistIn.add(DBHelperClass.P_ACCESSION_ID); // This attribute is required for populating the return hash key
		alistIn.add(DBHelperClass.P_NAME);
		alistIn.add(DBHelperClass.P_WORK_FILENAME);
		alistIn.add(DBHelperClass.P_PREDECESSOR_ID);
		alistIn.add(DBHelperClass.P_IS_CREATED_BY_WORKFLOW);
		alistIn.add(DBHelperClass.P_CONTENT_ID);
		listObjectStateAttributes = DBHelperClass.getAttributesForAllObjects(m_strBatchFolderId, DBHelperClass.SU_TYPE, alistIn);
        ArrayList outNoPredecessorlist = new ArrayList();
        m_ObjectPredecessorMapping = DBHelperClass.getObjectPredecessorMappingPerArticle(m_strCUStateObjectId, outNoPredecessorlist);
        // print m_ObjectPredecessorMapping
   		Iterator iterateMapping = m_ObjectPredecessorMapping.keySet().iterator();
   		while(iterateMapping.hasNext())
   		{
			String currentObjectId = (String)iterateMapping.next();
			HelperClass.porticoOutput(0, "Print ViewDiagnostics-populateLeadMetaDataFileList()-Start ObjectPredecessorMapping for="+currentObjectId);
			ArrayList predecessorValues = (ArrayList)m_ObjectPredecessorMapping.get(currentObjectId);
			if(predecessorValues != null && predecessorValues.size() > 0)
			{
				String predecessorValuesString = "";
				for(int pIndx=0; pIndx < predecessorValues.size(); pIndx++)
				{
					predecessorValuesString = predecessorValuesString + (String)predecessorValues.get(pIndx) + "-";
				}
				HelperClass.porticoOutput(0, "Print ViewDiagnostics-populateLeadMetaDataFileList()-predecessorValues="+predecessorValuesString);
			}
			HelperClass.porticoOutput(0, "Print ViewDiagnostics-populateLeadMetaDataFileList()-End ObjectPredecessorMapping for="+currentObjectId);
		}
	}

    public void populateLeadMetaDataFileList()
    {
        HelperClass.porticoOutput(0, "ViewDiagnostics-populateLeadMetaDataFileList()-Started");
        m_allMDList.clear();
        m_maxMDColumnCount = 0;
		try
		{
            ArrayList activeLeadMDList = DBHelperClass.getActiveLeadMetadataPerArticle(m_strCUStateObjectId);
            if(activeLeadMDList != null && activeLeadMDList.size() > 0)
            {
                int rowKey = 0;
                m_allMDList.put(rowKey, activeLeadMDList); // Topmost - active object(s)
                for(int activeIndx=0; activeIndx < activeLeadMDList.size(); activeIndx++)
                {
        			String tObjectId = (String)activeLeadMDList.get(activeIndx);
        			HelperClass.porticoOutput(0, "ViewDiagnostics-populateLeadMetaDataFileList()-Active-tObjectId="+tObjectId);
        			// Populate next level, note all calls from here will have the same level=rowKey+1
        			populatePredecessorList(tObjectId, rowKey+1);
        		}

                // Print result
        		Iterator iterate = m_allMDList.keySet().iterator();
        		while(iterate.hasNext())
        		{
					Integer rowIndex = (Integer)iterate.next();
					HelperClass.porticoOutput(0, "Print ViewDiagnostics-populateLeadMetaDataFileList()-rowIndex="+rowIndex);
					ArrayList rowValues = (ArrayList)m_allMDList.get(rowIndex);
					if(rowValues != null && rowValues.size() > 0)
					{
						if(m_maxMDColumnCount < rowValues.size())
						{
							m_maxMDColumnCount = rowValues.size();
						}

						String rowValueString = "";
						for(int pIndx=0; pIndx < rowValues.size(); pIndx++)
						{
							rowValueString = rowValueString + (String)rowValues.get(pIndx) + "-";
						}
						HelperClass.porticoOutput(0, "Print ViewDiagnostics-populateLeadMetaDataFileList()-rowValue="+rowValueString);
					}
				}
			}

			/*
						|-----------------------------------
						|                  |
						|        suGen2    |
						|-----------------------------------
						|                  |
						|        suGen1    |
						|-----------------------------------
						|      |     |     |
						| su1* |     | su3*|
						|-----------------------------------
						|      |     |     |
						| su1  | su2 | su3 |
						|      |     |     |
			*/
			/*
						|      |     |     |
						| su1* | su2 | su3*|    row2
						|-----------------------------------
						|      |     |     |
						| su1  | su2 | su3 |    row1
						|      |     |     |

						  col1   col2  col3
			*/

	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populateLeadMetaDataFileList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "ViewDiagnostics-populateLeadMetaDataFileList()-finally");
		}
	}

    // Populate the immediate predecessor under this rowKey
	public void populatePredecessorList(String currentObjectId, int rowKey)
	{
		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePredecessorList()-Started for currentObjectId="+currentObjectId);
		try
		{
		    if(m_ObjectPredecessorMapping.containsKey(currentObjectId))
		    {
		    	ArrayList alist = (ArrayList)m_ObjectPredecessorMapping.get(currentObjectId);
		    	if(alist != null && alist.size() > 0)
		    	{
		    		ArrayList addList = null;
		    		if(m_allMDList.containsKey(rowKey))
		    		{
		    			addList = (ArrayList)m_allMDList.get(rowKey);
		    		}
		    		else
		    		{
		    			addList = new ArrayList();
		    		}
		    		addList.addAll(alist);
		    		m_allMDList.put(rowKey, addList);

                    for(int indx=0; indx < alist.size(); indx++)
                    {
    	    	    	String tObjectId = (String)alist.get(indx);
    	    	    	// Populate next level
    	    	    	populatePredecessorList(tObjectId, rowKey+1);
    	    	    }
		    	}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populatePredecessorList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePredecessorList()-Ended");
	}

	public void populateRegisteredRendererList()
	{
		HelperClass.porticoOutput(0, "ViewDiagnostics-populateRegisteredRendererList()-Started");

		try
		{
		    IDfSysObject idfsysobject = (IDfSysObject)getDfSession().getObject(new DfId(m_strBatchFolderId));
		    String p_last_activity = idfsysobject.getString("p_last_activity");
		    if(p_last_activity != null && !p_last_activity.equals(""))
		    {
				int indexOfLastActivity = QcHelperClass.getIndexOfRentryPoint(getDfSession(), m_strBatchFolderId, p_last_activity);
                if(indexOfLastActivity != -1)
                {
					int indexOfStartActivityForReditionViews = QcHelperClass.getIndexOfRentryPoint(getDfSession(), m_strBatchFolderId, STARTACTIVITY_FOR_RENDITIONVIEWS);
		            if(indexOfStartActivityForReditionViews != -1)
		            {
						if(indexOfLastActivity >= indexOfStartActivityForReditionViews)
						{
                            HelperClass.porticoOutput(0, "ViewDiagnostics-Start calling Renderer.getRegisteredRenderers()");
                            m_registeredRendererList = Renderer.getRegisteredRenderers();
                            HelperClass.porticoOutput(0, "ViewDiagnostics-End calling Renderer.getRegisteredRenderers()");
					    }
					    else
					    {
							HelperClass.porticoOutput(0, "ViewDiagnostics-populateRegisteredRendererList()-Last Activiy not valid for Rendition views-p_last_activity="+p_last_activity);
						}
		            }
		            else
		            {
						HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populateRegisteredRendererList()-STARTACTIVITY_FOR_RENDITIONVIEWS not present in activityList="+STARTACTIVITY_FOR_RENDITIONVIEWS);
					}
		        }
		        else
		        {
					HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populateRegisteredRendererList()-p_last_activity not present in activityList="+p_last_activity);
				}
		    }
		    else
		    {
				HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populateRegisteredRendererList()-p_last_activity not populated");
			}
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populateRegisteredRendererList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
    		HelperClass.porticoOutput(0, "ViewDiagnostics-populateRegisteredRendererList()-finally");
		}
    }

    public void populatePageImageList()
    {
        HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-Started");
        m_allPdfList.clear();
        m_maxPageImageColumnCount = 0;
        try
		{
            ArrayList activePdfList = DBHelperClass.getActivePageImageListPerArticle(m_strCUStateObjectId);
            if(activePdfList != null && activePdfList.size() > 0)
            {
                int rowKey = 0;
                m_allPdfList.put(rowKey, activePdfList); // Topmost - active object(s)
                for(int activeIndx=0; activeIndx < activePdfList.size(); activeIndx++)
                {
        			String tObjectId = (String)activePdfList.get(activeIndx);
        			HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-Active-tObjectId="+tObjectId);
        			// Populate next level, note all calls from here will have the same level=rowKey+1
        			populatePdfPredecessorList(tObjectId, rowKey+1);
        		}

                // Print result
        		Iterator iterate = m_allPdfList.keySet().iterator();
        		while(iterate.hasNext())
        		{
					Integer rowIndex = (Integer)iterate.next();
					HelperClass.porticoOutput(0, "Print ViewDiagnostics-populatePageImageList()-rowIndex="+rowIndex);
					ArrayList rowValues = (ArrayList)m_allPdfList.get(rowIndex);
					if(rowValues != null && rowValues.size() > 0)
					{
						if(m_maxPageImageColumnCount < rowValues.size())
						{
							m_maxPageImageColumnCount = rowValues.size();
						}

						String rowValueString = "";
						for(int pIndx=0; pIndx < rowValues.size(); pIndx++)
						{
							rowValueString = rowValueString + (String)rowValues.get(pIndx) + "-";
						}
						HelperClass.porticoOutput(0, "Print ViewDiagnostics-populatePageImageList()-rowValue="+rowValueString);
					}
				}
			}

			/*
						|-----------------------------------
						|                  |
						|        suGen2    |
						|-----------------------------------
						|                  |
						|        suGen1    |
						|-----------------------------------
						|      |     |     |
						| su1* |     | su3*|
						|-----------------------------------
						|      |     |     |
						| su1  | su2 | su3 |
						|      |     |     |
			*/
			/*
						|      |     |     |
						| su1* | su2 | su3*|    row2
						|-----------------------------------
						|      |     |     |
						| su1  | su2 | su3 |    row1
						|      |     |     |

						  col1   col2  col3
			*/

	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populatePageImageList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-finally");
		}
	}

    // Populate the immediate predecessor under this rowKey
	public void populatePdfPredecessorList(String currentObjectId, int rowKey)
	{
		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePdfPredecessorList()-Started for currentObjectId="+currentObjectId);
		try
		{
		    if(m_ObjectPredecessorMapping.containsKey(currentObjectId))
		    {
		    	ArrayList alist = (ArrayList)m_ObjectPredecessorMapping.get(currentObjectId);
		    	if(alist != null && alist.size() > 0)
		    	{
		    		ArrayList addList = null;
		    		if(m_allPdfList.containsKey(rowKey))
		    		{
		    			addList = (ArrayList)m_allPdfList.get(rowKey);
		    		}
		    		else
		    		{
		    			addList = new ArrayList();
		    		}
		    		addList.addAll(alist);
		    		m_allPdfList.put(rowKey, addList);

                    for(int indx=0; indx < alist.size(); indx++)
                    {
    	    	    	String tObjectId = (String)alist.get(indx);
    	    	    	// Populate next level
    	    	    	populatePdfPredecessorList(tObjectId, rowKey+1);
    	    	    }
		    	}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populatePdfPredecessorList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePdfPredecessorList()-Ended");
	}

	public TreeMap getAllMetadataList()
	{
		return m_allMDList;
	}

	public int getMaxMDColumnCount()
	{
    	return m_maxMDColumnCount;
    }

	public TreeMap getAllPageImageList()
	{
		return m_allPdfList;
	}

	public int getMaxPageImageColumnCount()
	{
    	return m_maxPageImageColumnCount;
    }

	public Hashtable getObjectAttributeMapping()
	{
		return listObjectStateAttributes;
	}

	public List getRegisteredRendererList()
	{
		return m_registeredRendererList;
	}

	public String getBatchFolderId()
	{
		return m_strBatchFolderId;
	}

	public String getBatchAccessionId()
	{
		return m_strBatchAccessionId;
	}

	public String getCUStateObjectId()
	{
    	return m_strCUStateObjectId;
    }

// Controls
    private Label m_batchNameLabel;
    private Label m_articleNameLabel;

// Data
    private String m_strObjectId;
    private String m_strCUStateObjectId;
    private String m_strBatchFolderId;
    private String m_strBatchAccessionId;
    private List m_registeredRendererList;
    private Hashtable listObjectStateAttributes;

    // Per Article - Complete set of key=Object, value=PredecessorArrayList
    private Hashtable m_ObjectPredecessorMapping;
    // Per Article - Complete set of Lead MD (top-key(0)=Generated, bottom-key(max)=Supplied)
    private TreeMap m_allMDList;
    private TreeMap m_allPdfList;
    private int m_maxMDColumnCount;
    private int m_maxPageImageColumnCount;


    private final String NO_ARTICLE_FOUND = "No article found.........";
    private final String STARTACTIVITY_FOR_RENDITIONVIEWS = "Check for Completeness"; // "Check for Completeness 2";
    // private static final String PREDECESSOR_SEPARATOR = " ";
    // private static final String FILLER_OBJECTID = "";
}


/*
	public void populatePageImageList1()
	{
   		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-Started");

		try
		{
    		Hashtable tempSourceHash = new Hashtable();
			Hashtable m_ObjectIdList = HelperClass.getPageImageList(getDfSession(), m_strCUStateObjectId);
   		    if(m_ObjectIdList != null && m_ObjectIdList.size() > 0)
	        {
     	    	ValuePair valuePair = null;
         		String oldestSource = "";

        		// pick ordered pdf Source(s)
                Enumeration enumerate = m_ObjectIdList.keys();
                while(enumerate.hasMoreElements())
                {
             		String currentObjectId = "";
    				String p_predecessor_id = "";
				    String objectname = "";
				    String p_work_filename = "";
				    String suobjectid = "";
				    boolean p_is_created_by_workflow = false;

				    currentObjectId = (String)enumerate.nextElement();
                    // Hashtable attributeHash = (Hashtable)m_ObjectIdList.get(currentObjectId);
                    if(listObjectStateAttributes != null && listObjectStateAttributes.containsKey(currentObjectId))
                    {
						Hashtable attributeHash = (Hashtable)listObjectStateAttributes.get(currentObjectId);

						if(attributeHash != null && attributeHash.size() > 0)
						{
          				    ArrayList alist = new ArrayList();

       		    	        valuePair = new ValuePair();
       	        	        valuePair.setKey("objectid");
       	        	        valuePair.setValue(currentObjectId);
       	        	        alist.add(valuePair);

						    if(attributeHash.containsKey(DBHelperClass.P_NAME))
						    {
			                    objectname = (String)attributeHash.get(DBHelperClass.P_NAME);
			                    if(objectname == null)
			                    {
						    		objectname = "";
						    	}
           		    	        valuePair = new ValuePair();
           	        	        valuePair.setKey("objectname");
           	        	        valuePair.setValue(objectname);
           	        	        alist.add(valuePair);
					        }
						    if(attributeHash.containsKey(DBHelperClass.P_WORK_FILENAME))
						    {
       			                p_work_filename = (String)attributeHash.get(DBHelperClass.P_WORK_FILENAME);
       			                if(p_work_filename == null)
       			                {
						    		p_work_filename = "";
						    	}
           		    	        valuePair = new ValuePair();
                       	        valuePair.setKey("p_work_filename");
           	        	        valuePair.setValue(p_work_filename);
           	        	        alist.add(valuePair);
					        }
						    if(attributeHash.containsKey(DBHelperClass.P_PREDECESSOR_ID))
						    {
       			                p_predecessor_id = (String)attributeHash.get(DBHelperClass.P_PREDECESSOR_ID);
					        }
						    if(attributeHash.containsKey(DBHelperClass.P_IS_CREATED_BY_WORKFLOW))
						    {
       			                String p_is_created_by_workflow_str = (String)attributeHash.get(DBHelperClass.P_IS_CREATED_BY_WORKFLOW);
       			                if(p_is_created_by_workflow_str != null && p_is_created_by_workflow_str.equals(DBHelperClass.TRUE))
       			                {
						    		p_is_created_by_workflow = true;
						    	}
					        }
						    if(attributeHash.containsKey(DBHelperClass.P_OBJECT_ID))
						    {
       			                suobjectid = (String)attributeHash.get(DBHelperClass.P_OBJECT_ID);
       			                if(suobjectid == null)
       			                {
						    		suobjectid = "";
						    	}
           		    	        valuePair = new ValuePair();
           	        	        valuePair.setKey("suobjectid");
           	        	        valuePair.setValue(suobjectid);
           	        	        alist.add(valuePair);
					        }
                            if(p_predecessor_id != null && !p_predecessor_id.equals(""))
                            {
      			    	    	// source
             	                tempSourceHash.put(p_predecessor_id, alist);
    		                }
    		                else
    		                {
    			    	        // Very first source, no predecessor
    			    	        oldestSource = currentObjectId;
    			                m_sourcePdfList.add(alist);
    			            }
					    }
			            else
			            {
					    	HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populatePageImageList()-attributeHash is Empty for currentObjectId="+currentObjectId);
					    }
					}
					else
					{
                        HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populatePageImageList()-listObjectStateAttributes is Empty for currentObjectId="+currentObjectId);
					}
	            }

	            if(m_sourcePdfList != null && m_sourcePdfList.size() > 0)
	            {
                    if(m_sourcePdfList.size() != 1)
                    {
			    		HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populatePageImageList()-Mutiple page images PDF without predecessors");
			    	}
			    	else
			    	{
			    		boolean startLooping = true;
			    		int count = m_ObjectIdList.size();
			    		String currentObjectId = oldestSource;
			    		while(startLooping && count > 0)
			    		{
			    			count--;
			    			if(tempSourceHash.containsKey(currentObjectId))
			    			{
                                Object objectValue = tempSourceHash.get(currentObjectId);
                                if(objectValue != null)
                                {
			    			    	currentObjectId = "";
			    			    	ArrayList aValue = (ArrayList)objectValue;
			    			    	for(int indx=0; indx < aValue.size(); indx++)
			    			    	{
			    			      	    valuePair = (ValuePair)aValue.get(indx);
			    			     	    if(valuePair.getKey().equals("objectid"))
			    			    	    {
			    			    			currentObjectId = valuePair.getValue();
			    			    			break;
			    			    		}
			    			        }
			    			    	m_sourcePdfList.add(aValue);
			    			    }
						    }
			    			else
			    			{
			    				break;
			    			}
			    		}

			    		for(int indx=m_sourcePdfList.size()-1; indx >= 0 ; indx--)
			    		{
			    			ArrayList alist = (ArrayList)m_sourcePdfList.get(indx);
			    			for(int subindx=0; subindx < alist.size(); subindx++)
			    			{
			    			    valuePair = (ValuePair)alist.get(subindx);
			    			    if(valuePair.getKey().equals("objectname"))
			    			    {
			    			        HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-page image PDF="+valuePair.getValue());
			    			        break;
			    			    }
			    		    }
			    		}
			    	}
			    }
			    else
			    {
			    	HelperClass.porticoOutput(1, "Error in ViewDiagnostics-populatePageImageList()-No page image PDF without predecessor");
			    }
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ViewDiagnostics-populatePageImageList()-"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
     		HelperClass.porticoOutput(0, "ViewDiagnostics-populatePageImageList()-finally");
		}
    }
*/
