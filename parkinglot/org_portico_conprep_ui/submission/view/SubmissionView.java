
package org.portico.conprep.ui.submission.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.SubmissionPatternResultItem;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Tree;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.component.Component;


/*
 * @author  ranga
 */
public class SubmissionView extends Component
{

    /**
     *The onInit method intialises the component. When the component is referenced, the XML
     *config file is called first. This file contains the location of the class corresponding
     *to the component in the <class>...</class> tag. The onInit method is called upon
     *instantiation of the class by the WDK framework.
     */
    public SubmissionView()
    {
		m_submissionviewtree = null;
		m_arg_batch_id = null;
		m_resultList = null;
	}

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        m_arg_batch_id = args.get("objectId");
        HelperClass.porticoOutput("SubmissionView-onInit()-Argument batch_id=" + m_arg_batch_id);
        m_submissionviewtree = (Tree)getControl("submissionviewtree", com.documentum.web.form.control.Tree.class);
        // populate_batchListTree(); For testing purposes only
        populate_SubmissionView_Tree();
        Control control = getContainer();
        if(control instanceof Form)
            ((Form)control).setModal(false);
        setModal(false);
    }

    /*
     *The onRender method is called when a page corresponding to the component is displayed.
     */
/*
    public void onRender()
    {
        super.onRender(); //always call the superclass' onRender()
    }
*/
    // Not required for now, this is a Read Only Component

/*
    public void onClickSubmissionViewTree(Control control, ArgumentList argumentlist)
    {
		String treeValue = m_submissionviewtree.getValue();
	    //(Tree)
        HelperClass.porticoOutput("...called onClicksubmissionViewTree in page");
        HelperClass.porticoOutput("...selected tree value is " + treeValue);
        TreeNode treenode = m_submissionviewtree.getNodeWithId(m_submissionviewtree.getValue());
        HelperClass.porticoOutput("...selected node label is " + treenode.getLabel());
        HelperClass.porticoOutput("...select node unique Id is " + treenode.getUniqueId());
	}
*/

    public void onOk(Control control, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

    public void populate_SubmissionView_Tree()
    {
		m_resultList = HelperClass.getSubmissionViewObjects(getDfSession(), m_arg_batch_id);
		HelperClass.porticoOutput("Start Submission View Tree print Results");
    	for(int resultIndx=0; resultIndx < m_resultList.size(); resultIndx++)
    	{
			HelperClass.porticoOutput("----------------------------------------------");
		    SubmissionPatternResultItem tItem = (SubmissionPatternResultItem)m_resultList.get(resultIndx);
		    String currentItem = tItem.getThisToken();
		    String parentItem = tItem.getParentToken();
		    String currentOriginalItem = tItem.getOriginalToken();
			HelperClass.porticoOutput("currentItem="+currentItem);
			HelperClass.porticoOutput("currentOriginalItem(Only for Root File Name)="+currentOriginalItem);
			HelperClass.porticoOutput("parentItem="+parentItem);
			HelperClass.porticoOutput("----------------------------------------------");
		}
		HelperClass.porticoOutput("End Submission View Tree print Results");
		SubmissionPatternResultItem tItem = null;
		String rootName = "";
		for(int idx=0; idx < m_resultList.size(); idx++)
		{
			 tItem = (SubmissionPatternResultItem)m_resultList.get(idx);
			 if(tItem.getParentToken().equals(""))
			 {
				 rootName = tItem.getThisToken();
				 break;
			 }
		}

		// Create root node
		HelperClass.porticoOutput("RootNode="+rootName);

		HelperClass.porticoOutput("Start Tree Results");

	    boolean tStatus = getChildren(rootName, null, true, "");
		HelperClass.porticoOutput("End Tree Results");
    }

	public boolean getChildren(String nodeName, TreeNode parentTreeNode, boolean topLevel, String nodeDisplayOriginalName)
	{
		boolean tStatus = true;
		boolean isTopLevel = topLevel;

        TreeNode thisNode = null;
        if(parentTreeNode == null)
        {
			// batch_name node
			// previously isTopLevel was false, but has no effect in 'createThisRootNode' since it is not used
			thisNode = createThisRootNode(nodeName, parentTreeNode, isTopLevel, nodeDisplayOriginalName);
			isTopLevel = true;
		}
		else
		{
	    	thisNode = createThisNode(nodeName, parentTreeNode, isTopLevel, nodeDisplayOriginalName);
	    	isTopLevel = false;
	    }


/*
		SubmissionPatternResultItem tItem = null;

		for(int indx=0; indx < m_resultList.size(); indx++)
		{
			// In order to get a sorted list of children, pick all these children, sort and then call 'getChildren()'
			tItem = (SubmissionPatternResultItem)m_resultList.get(indx);
			String currentTokenName = tItem.getThisToken();
			if(tItem.getParentToken().equals(nodeName))
			{
				String tCurrentItemOriginalToken = "";
				if(isTopLevel == true && !tItem.getOriginalToken().equals(""))
				{
					// The getChildren() is going to pick the first RootFileName with the original file separators
					tCurrentItemOriginalToken = tItem.getOriginalToken();
				}
				tStatus = getChildren(tItem.getThisToken(), thisNode, isTopLevel, tCurrentItemOriginalToken);
			}
		}
*/
        TreeSet tTreeSet = getAssociatedChildren(nodeName, isTopLevel);
	    if(tTreeSet != null && tTreeSet.size() > 0)
	    {
            Iterator tIterate = tTreeSet.iterator();
            while(tIterate.hasNext())
            {
      			String sortedChildNodeName = (String)tIterate.next();
	    		tStatus = getChildren(sortedChildNodeName, thisNode, isTopLevel, sortedChildNodeName);
       	    }
		}

		return tStatus;
	}

	private TreeSet getAssociatedChildren1(String parentNodeName, boolean isTopLevel)
	{
		TreeSet childrenTreeSet = new TreeSet();
		SubmissionPatternResultItem tChildItem = null;
		for(int indx=0; indx < m_resultList.size(); indx++)
		{
			// In order to get a sorted list of children, pick all these children, sort and then call 'getChildren()'
			tChildItem = (SubmissionPatternResultItem)m_resultList.get(indx);
			if(tChildItem.getParentToken().equals(parentNodeName))
			{
				String tCurrentItemToken = tChildItem.getThisToken();
				if(isTopLevel == true && !tChildItem.getOriginalToken().equals(""))
				{
					// The getChildren() is going to pick the first RootFileName with the original file separators
					tCurrentItemToken = tChildItem.getOriginalToken();
				}
				childrenTreeSet.add(tCurrentItemToken);
				// tStatus = getChildren(tItem.getThisToken(), thisNode, isTopLevel, tCurrentItemOriginalToken);
			}
		}

		return childrenTreeSet;
	}

	public TreeNode createThisRootNode(String nodeName, TreeNode parentTreeNode, boolean topLevel, String nodeDisplayOriginalName)
	{
		String displayName = getThisDisplayName(nodeName);
		String labelName = getThisLabelName(nodeName);
		TreeNode tNode = m_submissionviewtree.createRootNode(labelName, null, displayName);
		tNode.setNodeState(TreeNode.STATE_EXPANDED); // 2
		tNode.setSelectable(false);
		return tNode;
	}

	public TreeNode createThisNode(String nodeName, TreeNode parentTreeNode, boolean topLevel, String nodeDisplayOriginalName)
	{
		String displayName = nodeName;
		if(topLevel == true && !nodeDisplayOriginalName.equals(""))
		{
		    displayName = nodeDisplayOriginalName;
		}
		else
		{
			displayName = getThisDisplayName(nodeName);
		}

		String labelName = getThisLabelName(nodeName);
		TreeNode tNode = m_submissionviewtree.createNode(labelName, null, displayName, parentTreeNode);
		tNode.setNodeState(TreeNode.STATE_EXPANDED); // 2
		tNode.setSelectable(false);
		return tNode;
	}

    public String getThisLabelName(String nodeName)
    {
		// labelName for internal processing, convert a '.' to '|'
		char oldChar = '.';
		char newChar = '|';
		return nodeName.replace(oldChar, newChar);
	}

    public String getThisDisplayName(String nodeName)
    {
		// displayName for picking only the filename or directory name, not the complete absolute name
		char findChar = '/';
		int indx = nodeName.lastIndexOf(findChar);
		String displayName = nodeName.substring(indx+1, nodeName.length());
		HelperClass.porticoOutput("Submission tree-Nodename=" + nodeName);
		HelperClass.porticoOutput("Submission tree-getThisDisplayName=" + displayName);
		return displayName;
	}

/* For testing purposes only
    public void populate_batchListTree()
    {
        TreeNode treenode = m_submissionviewtree.createRootNode("root", null, "Root");
        treenode.setNodeState(2);
        TreeNode treenode1 = m_submissionviewtree.createNode("node|1", null, "Node.1", treenode);
        m_submissionviewtree.createNode("node2", null, "Node 2", treenode);
        TreeNode treenode2 = m_submissionviewtree.createNode("node3", null, "\uFF21\uFF22\uFF23\uFF5A", treenode);
        treenode2.setNodeState(2);
        TreeNode treenode3 = m_submissionviewtree.createNode("node4", null, "Node 4", treenode);
        TreeNode treenode4 = m_submissionviewtree.createNode("subnode1", null, "Sub Node 1", treenode2);
        treenode4.setNodeState(2);
        TreeNode treenode5 = m_submissionviewtree.createNode("subnode2", null, "Sub Node 2", treenode2);
        treenode5.setNodeState(2);
        TreeNode treenode6 = m_submissionviewtree.createNode("subnode3", null, "Sub Node 3 (1,000 children)", treenode2);
        m_submissionviewtree.createNode("subsubnode1", null, "SubSub Node 1", treenode4);
        m_submissionviewtree.createNode("subsubnode2", null, "SubSub Node 2", treenode4);
        TreeNode treenode7 = m_submissionviewtree.createNode("subsubnode3", null, "SubSub Node 3", treenode5);
        treenode7.setEnabled(false);
        m_submissionviewtree.createNode("subsubnode4", null, "SubSub Node 4", treenode5);
        for(int i = 1; i <= 10; i++)
            m_submissionviewtree.createNode("Node" + i + "of1000", null, "Node " + i + " of 10", treenode6);

        m_submissionviewtree.createNode("invis1", null, "Invisible 1", treenode1);
        m_submissionviewtree.createNode("invis2", null, "Invisible 2", treenode3);
	}
*/
    // This method has been changed to allow for an identical Batch Name(topLevel) and Raw Unit Name,
    //      but would stop if 2 Raw Unit Name(s) are identical
	private TreeSet getAssociatedChildren(String parentNodeName, boolean isTopLevel)
	{
		TreeSet childrenTreeSet = new TreeSet();
		SubmissionPatternResultItem tChildItem = null;
		for(int indx=0; indx < m_resultList.size(); indx++)
		{
			// In order to get a sorted list of children, pick all these children, sort and then call 'getChildren()'
			tChildItem = (SubmissionPatternResultItem)m_resultList.get(indx);
			if(tChildItem.getParentToken().equals(parentNodeName))
			{
				String tCurrentItemToken = tChildItem.getThisToken();

                // If the parentNode is not a Batch level(topLevel) node then
                //    do not allow further here, because ideally there should not be 2 active Raw Unit(s)
                //    with the same names
                //    Note: This would otherwise lead to an infinite loop since the
                //    parent Name and Child Name are the same
                // We would relax the above rule between the top Level Batch node name and a Raw Unit node name
				if(tCurrentItemToken.equals(parentNodeName) && isTopLevel != true)
				{
					HelperClass.porticoOutput(1, "Error-getAssociatedChildren identical raw unit names-parentNodeName="+parentNodeName+",tCurrentItemToken="+tCurrentItemToken);
					break;
				}
				else
				{
					if(tCurrentItemToken.equals(parentNodeName) && isTopLevel == true)
					{
    					HelperClass.porticoOutput(0, "Warning-getAssociatedChildren identical Batch Name,Raw unit names-parentNodeName="+parentNodeName+",tCurrentItemToken="+tCurrentItemToken);
 					}

    				if(isTopLevel == true && !tChildItem.getOriginalToken().equals(""))
			    	{
			    		// The getChildren() is going to pick the first RootFileName with the original file separators
			    		tCurrentItemToken = tChildItem.getOriginalToken();
			    	}
			    	childrenTreeSet.add(tCurrentItemToken);
			    	// tStatus = getChildren(tItem.getThisToken(), thisNode, isTopLevel, tCurrentItemOriginalToken);
			    }
			}
		}

		return childrenTreeSet;
	}

    private Tree m_submissionviewtree;
    private String m_arg_batch_id;
    private ArrayList m_resultList;
}
