
package org.portico.conprep.ui.process.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.TreeMap;

import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ProcessViewFilter;
import org.portico.conprep.ui.helper.ProcessViewResultItem;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Tree;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.component.Component;


public class ProcessView extends Component{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public ProcessView()
    {
		m_processviewtree = null;
		m_arg_batch_id = null;
		m_resultList = null;
		m_objectContentIdMapping = new Hashtable();
		m_current_node_id = "";
		m_current_node_label = "";
	}

    public void onInit(ArgumentList args){
        super.onInit(args);
        m_arg_batch_id = args.get("objectId");
        m_arg_custate_id = args.get("cuStateId");
        HelperClass.porticoOutput(0, "ProcessView-onInit()-Argument batch_id=" + m_arg_batch_id);
        HelperClass.porticoOutput(0, "ProcessView-onInit()-Argument cu_state_id=" + m_arg_custate_id);
        m_processviewtree = (Tree)getControl("processviewtree", com.documentum.web.form.control.Tree.class);
        // populate_batchListTree(); For testing purposes only
        populate_ProcessView_Tree();
        Control control = getContainer();
        if(control instanceof Form)
            ((Form)control).setModal(false);
        setModal(false);
    }

    /*
     *The onRender method is called when a page corresponding to the component is displayed.
     *The code below demos that it is called for each page corresponding to the component.
     *Thus, it will be called for the start page viz. search.jsp as well as the page
     *displaying the search results viz. searchResults.jsp. If you want any processing
     *done for a specific page, you can put the code here.
     */
    public void onRender(){

        super.onRender(); //always call the superclass' onRender()
    }

    public void callRefresh(Control control, ArgumentList argumentlist)
    {
		m_current_node_label = "";
		m_current_node_id = "";
		m_resultList = null;
		populate_ProcessView_Tree();
	}

    public void onClickProcessViewTree(Control control, ArgumentList argumentlist)
    {
		String treeValue = m_processviewtree.getValue();
        HelperClass.porticoOutput(0, "ProcessView-onClickProcessViewTree");
        HelperClass.porticoOutput(0, "ProcessView-onClickProcessViewTree selected tree value is " + treeValue);
        TreeNode treenode = m_processviewtree.getNodeWithId(m_processviewtree.getValue());
        HelperClass.porticoOutput(0, "ProcessView-onClickProcessViewTree select node unique Id is " + treenode.getUniqueId());
        m_current_node_label = treenode.getLabel();
        HelperClass.porticoOutput(0, "ProcessView-onClickProcessViewTree selected node label is " + m_current_node_label);
        m_current_node_id = treenode.getId().trim();
        HelperClass.porticoOutput(0, "ProcessView-onClickProcessViewTree select node Id is " + m_current_node_id);
	}

	public String getBatchFolderId()
	{
		return m_arg_batch_id;
	}

	public String getSelectedNodeLabel()
	{
		return m_current_node_label;
	}

	public String getSelectedContentObjectId()
	{
		String contentObjectId = "";
		try
		{
            if(m_objectContentIdMapping.containsKey(m_current_node_id))
            {
		    	contentObjectId = (String)m_objectContentIdMapping.get(m_current_node_id);
	        }
            HelperClass.porticoOutput(0, "ProcessView-getSelectedContentObjectId-m_current_node_id="+m_current_node_id+","+
                                                                             "contentObjectId=" + contentObjectId);
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "Exception in ProcessView-onClickProcessViewTree select node Id is=" + m_current_node_id+","+
			                                                                                                  "Exception="+e.toString());
		}
		finally
		{
		}

        return contentObjectId;
	}

	public String getSelectedContentAccessionId()
	{
		return m_current_node_id;
	}

    public void onOk(Control control, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

/*
    public String getCurrentNodeObjectType()
    {
		String objectType = "";
		if(m_resultList != null && m_resultList.size() > 0)
		{
    	    for(int resultIndx=0; resultIndx < m_resultList.size(); resultIndx++)
    	    {
				ProcessViewResultItem tItem = (ProcessViewResultItem)m_resultList.get(resultIndx);
				if(tItem.getThisKey().equals(m_current_node_id))
				{
			    	objectType = tItem.getThisObjectType();
			        break;
			    }
		    }
		}

		return objectType;
	}
*/

    public void populate_ProcessView_Tree()
    {
		// Filter will be populated from a control (pulldown)
		ProcessViewFilter tPorticoProcessViewFilter = new ProcessViewFilter();
		m_resultList = HelperClass.getProcessViewObjects(getDfSession(), m_arg_batch_id, m_arg_custate_id, tPorticoProcessViewFilter);
		HelperClass.porticoOutput(0, "ProcessView-Start print Results-Process View Tree");

    	for(int resultIndx=0; resultIndx < m_resultList.size(); resultIndx++)
    	{
			HelperClass.porticoOutput(0, "----------------------------------------------");
		    ProcessViewResultItem tItem = (ProcessViewResultItem)m_resultList.get(resultIndx);
		    String currentItem = tItem.getThisToken();
		    String parentItem = tItem.getParentToken();
		    String displayItem = tItem.getThisDisplayToken();
		    String thisObjectType = tItem.getThisObjectType();
		    String thisSortKey = tItem.getThisSortKey();
			HelperClass.porticoOutput(0, "currentItem="+currentItem);
			HelperClass.porticoOutput(0, "parentItem="+parentItem);
			HelperClass.porticoOutput(0, "displayItem="+displayItem);
			HelperClass.porticoOutput(0, "objectType="+thisObjectType);
			HelperClass.porticoOutput(0, "sortKey="+thisSortKey);
			HelperClass.porticoOutput(0, "----------------------------------------------");
		}
		HelperClass.porticoOutput(0, "ProcessView-End print Results-Process View Tree");
		ProcessViewResultItem tItem = null;
		String rootName = "";
		String rootDisplayName = "";
		for(int idx=0; idx < m_resultList.size(); idx++)
		{
			 tItem = (ProcessViewResultItem)m_resultList.get(idx);
			 if(tItem.getParentKey().equals(""))
			 {
				 rootName = tItem.getThisKey();
				 rootDisplayName = tItem.getThisDisplayToken();
				 if(tItem.getIsErroredItem())
				 {
					 rootDisplayName = addErrorDisplay(rootDisplayName);
				 }
				 break;
			 }
		}

		// Create root node
		HelperClass.porticoOutput(0, "ProcessView-RootNode="+rootName);
		HelperClass.porticoOutput(0, "ProcessView-Start Tree Results");
	    boolean tStatus = getChildren(rootName, rootDisplayName, null);
		HelperClass.porticoOutput(0, "ProcessView-End Tree Results");
    }

    private String addErrorDisplay(String rootDisplayName)
    {
		// String newDisplayName = "(?)" + " " + rootDisplayName;
		String newDisplayName = m_error_symbol + " " + rootDisplayName;
		return newDisplayName;
	}

	public boolean getChildren(String nodeName, String nodeDisplayName, TreeNode parentTreeNode)
	{
		boolean tStatus = true;
        TreeNode thisNode = null;
        if(parentTreeNode == null)
        {
			thisNode = createThisRootNode(nodeName, nodeDisplayName, parentTreeNode);
		}
		else
		{
	    	thisNode = createThisNode(nodeName, nodeDisplayName, parentTreeNode);
	    }

    	// In order to get a sorted list of children, pick all these children, sort and then call 'getChildren()'
        HelperClass.porticoOutput(0, "ProcessView-Parent-Print(logging) displayName=" + nodeDisplayName + "(" + nodeName +")");
        ArrayList childList = getAssociatedChildren(nodeName);
        if(childList != null && childList.size() > 0)
        {
    		for(int indx=0; indx < childList.size(); indx++)
     		{
		    	ProcessViewResultItem tItem = (ProcessViewResultItem)childList.get(indx);
		    	if(tItem.getParentKey().equals(nodeName))
		    	{
    	    		String currentDisplayName = tItem.getThisDisplayToken();
		    		String currentKey = tItem.getThisKey();
    	    		if(tItem.getIsErroredItem())
		    		{
		    		    currentDisplayName = addErrorDisplay(currentDisplayName);
		    		}
		    		if(tItem.getThisObjectType().equalsIgnoreCase(DBHelperClass.SU_TYPE))
		    		{
						String contentItem = tItem.getContentObjectId();
						if(contentItem != null && !contentItem.equals(""))
						{
		    	        	m_objectContentIdMapping.put(currentKey, contentItem);
					    }
				    }
		    		HelperClass.porticoOutput(0, "ProcessView-Children="+tItem.getThisObjectType() + "|" + tItem.getThisSortKey() + "|" + currentDisplayName);
		    		tItem = null;
		    		tStatus = getChildren(currentKey, currentDisplayName, thisNode);
		    	}
		    }
	    }

		return tStatus;
	}

	private ArrayList getAssociatedChildren(String nodeName)
	{
		ArrayList childList = new ArrayList();
		TreeMap sortedItemMap = new TreeMap();
		ArrayList unsortedItemList = new ArrayList();
		String parentObjectType = "";

		for(int indx=0; indx < m_resultList.size(); indx++)
		{
	    	ProcessViewResultItem tItem = (ProcessViewResultItem)m_resultList.get(indx);
	    	if(tItem.getParentKey().equals(nodeName))
	    	{
				String itemSortKey = tItem.getThisSortKey();
				String itemDisplayName = tItem.getThisDisplayToken();
				if(itemSortKey != null && !itemSortKey.equals(""))
				{
					parentObjectType = tItem.getParentObjectType();
					sortedItemMap.put(itemSortKey, tItem);
				}
				else
				{
                    unsortedItemList.add(tItem);
				}
	    		tItem = null;
	    	}
	    }

	    Collection col = sortedItemMap.values();
        if(col != null && col.size() > 0)
        {
	    	// pick sorted items first
	    	childList.addAll(col);
		}
        if(unsortedItemList != null && unsortedItemList.size() > 0)
        {
			// append unsorted items in the end, usually the SU(s) and other unlinked units
	        childList.addAll(unsortedItemList);
	    }

// Logging only start
        if(childList != null && childList.size() > 0)
        {
			for(int logIndx=0; logIndx < childList.size(); logIndx++)
			{
				ProcessViewResultItem tItem = (ProcessViewResultItem)childList.get(logIndx);
				HelperClass.porticoOutput(0, "ProcessView-getAssociatedChildren-Print(logging) displayName="+tItem.getThisDisplayToken() + "--sortkey=" + tItem.getThisSortKey() + "--key=" + tItem.getThisKey());
			}
		}
		else
		{
			HelperClass.porticoOutput(0, "ProcessView-getAssociatedChildren-Print(logging) - No Children");
		}
// Logging only end

	    return childList;
	}

	public TreeNode createThisRootNode(String nodeName, String nodeDisplayName, TreeNode parentTreeNode)
	{
		String valueName = getThisValueName(nodeName); // To fix documentum tree node name limitation, no '.' allowed
		TreeNode tNode = m_processviewtree.createRootNode(valueName, null, nodeDisplayName); // id, icon, displayname
		tNode.setNodeState(TreeNode.STATE_EXPANDED); // 2
		tNode.setSelectable(false);
        return tNode;
	}


	public TreeNode createThisNode(String nodeName, String nodeDisplayName, TreeNode parentTreeNode)
	{
		boolean isSelectable = m_objectContentIdMapping.containsKey(nodeName);
		String valueName = getThisValueName(nodeName); // To fix documentum tree node name limitation, no '.' allowed
		TreeNode tNode = m_processviewtree.createNode(valueName, null, nodeDisplayName, parentTreeNode);// id, icon, displayname, parent TreeNode
		tNode.setNodeState(TreeNode.STATE_EXPANDED); // 2
		tNode.setSelectable(isSelectable); // false
		return tNode;
	}

    public String getThisValueName(String nodeName)
    {
		// labelName for internal processing, convert a '.' to '|'
		String massagedString = nodeName;
		char oldChar = '.';
		char newChar = '|';
		massagedString = massagedString.replace(oldChar, newChar);
		oldChar = '\\';
		massagedString = massagedString.replace(oldChar, newChar);
		return massagedString;
	}

/* For testing purposes only
    public void populate_batchListTree()
    {
        TreeNode treenode = m_processviewtree.createRootNode("root", null, "Root");
        treenode.setNodeState(2);
        TreeNode treenode1 = m_processviewtree.createNode("node|1", null, "Node.1", treenode);
        m_processviewtree.createNode("node2", null, "Node 2", treenode);
        TreeNode treenode2 = m_processviewtree.createNode("node3", null, "\uFF21\uFF22\uFF23\uFF5A", treenode);
        treenode2.setNodeState(2);
        TreeNode treenode3 = m_processviewtree.createNode("node4", null, "Node 4", treenode);
        TreeNode treenode4 = m_processviewtree.createNode("subnode1", null, "Sub Node 1", treenode2);
        treenode4.setNodeState(2);
        TreeNode treenode5 = m_processviewtree.createNode("subnode2", null, "Sub Node 2", treenode2);
        treenode5.setNodeState(2);
        TreeNode treenode6 = m_processviewtree.createNode("subnode3", null, "Sub Node 3 (1,000 children)", treenode2);
        m_processviewtree.createNode("subsubnode1", null, "SubSub Node 1", treenode4);
        m_processviewtree.createNode("subsubnode2", null, "SubSub Node 2", treenode4);
        TreeNode treenode7 = m_processviewtree.createNode("subsubnode3", null, "SubSub Node 3", treenode5);
        treenode7.setEnabled(false);
        m_processviewtree.createNode("subsubnode4", null, "SubSub Node 4", treenode5);
        for(int i = 1; i <= 10; i++)
            m_processviewtree.createNode("Node" + i + "of1000", null, "Node " + i + " of 10", treenode6);

        m_processviewtree.createNode("invis1", null, "Invisible 1", treenode1);
        m_processviewtree.createNode("invis2", null, "Invisible 2", treenode3);
	}
*/
    private Tree m_processviewtree;
    private String m_arg_batch_id;
    private ArrayList m_resultList;
    private String m_error_symbol;
    private String m_current_node_id;
    private String m_current_node_label;
    private Hashtable m_objectContentIdMapping;
    private String m_arg_custate_id;
}
