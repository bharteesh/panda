package org.portico.conprep.ui.control;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Tree;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.docbase.DocbaseFolderTree;
import com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode;
import com.documentum.web.formext.session.SessionManagerHttpBinding;

// Referenced classes of package com.documentum.webtop.control:
//            BrowserNavigationNode, DocbaseNavigationNode

public class ConprepBrowserTree extends DocbaseFolderTree
{
    class NodeWrapper
    {

        public TreeNode createBrowserNode(String s)
        {
            Object obj = null;
            StringBuffer stringbuffer = new StringBuffer(48);
            stringbuffer.append(m_strCompId);
            if(m_strStartPage != null)
            {
                stringbuffer.append('/');
                stringbuffer.append(m_strStartPage);
            }
            if(m_strHandlerClass == null)
            {
                String s1 = getNavigationIconFolder() + m_strIcon;
                obj = new BrowserNavigationNode(ConprepBrowserTree.this, stringbuffer.toString(), s1, m_strLabel, s, null);
            } else
            {
                try
                {
                    Class class1 = Class.forName(m_strHandlerClass);
                    if(m_strCompId.equalsIgnoreCase("cabinets"))
                    {
                        Class aclass[] = {
                            com.documentum.web.formext.control.docbase.DocbaseFolderTree.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, com.documentum.web.form.control.TreeNode.class
                        };
                        String s2 = m_strLabel;
                        if(s2 == null)
                            s2 = m_strFoldersNLS;
                        String s4 = getNavigationIconFolder() + m_strIcon;
                        if(m_strIcon == null)
                            s4 = getIconForDepth(0);
                        Object aobj1[] = {
                            ConprepBrowserTree.this, "/" + s, s4, s2, "docbase", s, null
                        };
                        Constructor constructor1 = class1.getConstructor(aclass);
                        Object obj2 = constructor1.newInstance(aobj1);
                        if(!(obj2 instanceof DocbaseFolderTreeNode))
                            throw new IllegalArgumentException("'cabinets' tree node handler class must be of type DocbaseFolderTreeNode");
                        obj = (DocbaseFolderTreeNode)obj2;
                    } else
                    {
                        Class aclass1[] = {
                            com.documentum.webtop.control.WebTopBrowserTree.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, com.documentum.web.form.control.TreeNode.class
                        };
                        String s3 = getNavigationIconFolder() + m_strIcon;
                        Object aobj[] = {
                            ConprepBrowserTree.this, stringbuffer.toString(), s3, m_strLabel, s, null
                        };
                        Constructor constructor = class1.getConstructor(aclass1);
                        Object obj1 = constructor.newInstance(aobj);
                        if(!(obj1 instanceof BrowserNavigationNode))
                            throw new IllegalArgumentException("Custom tree node handler class not of correct type!");
                        obj = (BrowserNavigationNode)obj1;
                    }
                }
                catch(Exception exception)
                {
                    throw new WrapperRuntimeException("Unable to create custom tree node handler class.", exception);
                }
            }
            return ((TreeNode) (obj));
        }

        private String m_strCompId;
        private String m_strStartPage;
        private String m_strIcon;
        private String m_strLabel;
        private String m_strHandlerClass;

        public NodeWrapper(String s, String s1, String s2, String s3, String s4)
        {
            m_strCompId = s;
            m_strStartPage = s1;
            m_strIcon = s2;
            m_strLabel = s3;
            m_strHandlerClass = s4;
        }
    }


    public ConprepBrowserTree()
    {
        m_docbaseRootNode = null;
        m_topLevelNodes = new ArrayList();
        m_hashDocbaseLevelNodes = new Hashtable();
        m_hashDocbaseTrees = new HashMap(11, 1.0F);
        m_strRootNLS = "Documentum";
        m_strFoldersNLS = "Folders";
        m_strIconDCTM = null;
    }

    public void onInit(ArgumentList argumentlist)
    {
        setExpandRoot(false);
        super.onInit(argumentlist);
        DocbaseFolderTree.m_nOriginalRootDepth = -2;
        setupBrowserIcons();
        BrowserNavigationNode browsernavigationnode = new BrowserNavigationNode(this, "_ROOT", m_strIconDCTM, m_strRootNLS, "_ROOT", null);
        browsernavigationnode.setSelectable(false);
        super.m_rootNode = browsernavigationnode;
        NodeWrapper nodewrapper;
        for(Iterator iterator = m_topLevelNodes.iterator(); iterator.hasNext(); browsernavigationnode.addChild(nodewrapper.createBrowserNode(null)))
            nodewrapper = (NodeWrapper)iterator.next();
		IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
		IDfSession idfsession = null;
        try
        {
            idfsession = idfsessionmanager.getSession(SessionManagerHttpBinding.getCurrentDocbase());
			addDocbaseSubTree( idfsession.getDocbaseName() );

        }catch(Exception exception){
            throw new WrapperRuntimeException("Unable to retrieve list of available docbases!", exception);
        }finally{
			if(idfsession != null)
				idfsessionmanager.release(idfsession);
		}
        browsernavigationnode.expandNode();
        String s = getSelectedId();
        if(s != null)
            setSelectedId(s);
    }

    public void onCollapse(Tree tree, ArgumentList argumentlist)
    {
        String s = getValue();
        super.onCollapse(tree, argumentlist);
        String s1 = getValue();
        if(!s1.equals(s))
        {
            int i = s1.indexOf('.');
            s1 = s1.substring(i + 1);
            i = s1.indexOf('.');
            if(i != -1)
            {
                s1 = s1.substring(i + 1);
                ArgumentList argumentlist1 = new ArgumentList();
                argumentlist1.add("id", s1);
                ((Component)getContainer()).setClientEvent("onNodeSelected", argumentlist1);
            }
        }
    }

    public void refresh(String s, String s1)
    {
        StringBuffer stringbuffer = new StringBuffer(64);
        if(s1 == null || s1.length() == 0)
        {
            buildDocbaseRootId(s, stringbuffer);
            DocbaseFolderTreeNode docbasefoldertreenode = (DocbaseFolderTreeNode)getNodeWithId(stringbuffer.toString());
            if(docbasefoldertreenode != null)
                docbasefoldertreenode.clearChildren();
        } else
        {
            buildComponentRootId(s, s1, stringbuffer);
            BrowserNavigationNode browsernavigationnode = (BrowserNavigationNode)getNodeWithId(stringbuffer.toString());
            if(browsernavigationnode != null)
                browsernavigationnode.clearChildren();
        }
        setSelectedId(super.m_strSelectedId);
    }

    public void registerTopLevelNode(String s, String s1, String s2, String s3, String s4)
    {
        if(s == null)
        {
            throw new IllegalArgumentException("Must specify component id for custom node!");
        } else
        {
            m_topLevelNodes.add(new NodeWrapper(s, s1, s2, s3, s4));
            return;
        }
    }

    public void registerDocbaseLevelNode(String s, String s1, String s2, String s3, String s4)
    {
        if(s == null)
            throw new IllegalArgumentException("Must specify component id for custom node!");
        String s5 = SessionManagerHttpBinding.getCurrentDocbase();
        ArrayList arraylist = (ArrayList)m_hashDocbaseLevelNodes.get(s5);
        if(arraylist == null)
        {
            arraylist = new ArrayList(16);
            m_hashDocbaseLevelNodes.put(s5, arraylist);
        }
        arraylist.add(new NodeWrapper(s, s1, s2, s3, s4));
    }

    public DocbaseFolderTreeNode createRootDocbaseNode(String s, String s1, String s2, String s3, String s4)
    {
        DocbaseFolderTreeNode docbasefoldertreenode = createDocbaseNode(s, s1, m_strFoldersNLS, s3, s4, null);
        m_docbaseRootNode = docbasefoldertreenode;
        return docbasefoldertreenode;
    }

    /**
     * @deprecated Method createRootFolderNode is deprecated
     */

    public DocbaseFolderTreeNode createRootFolderNode(String s, DocbaseNavigationNode docbasenavigationnode)
    {
        return new DocbaseFolderTreeNode(this, '/' + s, getIconForDepth(0), m_strFoldersNLS, "docbase", s, docbasenavigationnode);
    }

    public void selectNodeFromObjectId(String s, String s1, String s2)
    {
        if(s == null || s.charAt(0) == '/')
        {
            setSelectedId(getSelectedId() + '.' + s1);
        } else
        {
            String s3 = SessionManagerHttpBinding.getCurrentDocbase();
            StringBuffer stringbuffer = new StringBuffer(64);
            buildComponentRootId(s3, s, stringbuffer);
            BrowserNavigationNode browsernavigationnode = (BrowserNavigationNode)getNodeWithId(stringbuffer.toString());
            if(browsernavigationnode == null)
                throw new IllegalStateException("Unable to find tree node id: " + stringbuffer.toString());
           // browsernavigationnode.selectNodeFromObjectId(s1, s2);
        }
    }

    public void selectNodeFromAbsolutePath(String s, String s1, String s2)
    {
        StringBuffer stringbuffer = new StringBuffer(128);
        if(s1 == null || s1.charAt(0) == '/')
            buildDocbaseRootId(s, stringbuffer);
        else
            buildComponentNodeId(s, s1, stringbuffer);
        if(s2 != null && s2.length() != 0)
            stringbuffer.append('.').append(s2);
        setSelectedId(stringbuffer.toString());
    }

    public void setRootNLS(String s)
    {
        m_strRootNLS = s;
    }

    public void setFoldersNLS(String s)
    {
        m_strFoldersNLS = s;
    }

    public boolean refreshDocbaseSubTree(String s)
    {
        boolean flag = false;
        DocbaseNavigationNode docbasenavigationnode = (DocbaseNavigationNode)m_hashDocbaseTrees.get(s);
        if(docbasenavigationnode != null)
        {
            IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
            IDfSession idfsession = null;
            try
            {
                idfsession = idfsessionmanager.getSession(s);
                docbasenavigationnode.getData();
                docbasenavigationnode.expandNode();
                docbasenavigationnode.setLabel(docbasenavigationnode.getLabel() + " : " + idfsession.getLoginUserName());
                String s1 = '/' + s;
                for(Iterator iterator = docbasenavigationnode.getChildren(); iterator.hasNext() && !flag;)
                {
                    TreeNode treenode = (TreeNode)iterator.next();
                    if(treenode.getId().equals(s1))
                    {
                        setSelectedNode(treenode);
                        flag = true;
                    }
                }

                docbasenavigationnode.setSelectable(false);
            }
            catch(Exception exception) { }
            finally
            {
                if(idfsession != null)
                    idfsessionmanager.release(idfsession);
            }
            if(!flag)
                docbasenavigationnode.collapseNode(false);
        }
        return flag;
    }

    public void buildDocbaseRootId(String s, StringBuffer stringbuffer)
    {
        stringbuffer.append("_ROOT").append('.').append(s).append("./").append(s);
    }

    public void buildComponentRootId(String s, String s1, StringBuffer stringbuffer)
    {
        stringbuffer.append("_ROOT").append('.').append(s).append('.').append(s1);
    }

    DocbaseFolderTreeNode getDocbaseRootNode()
    {
        return m_docbaseRootNode;
    }

    public boolean hasDocbaseLevelNodeRegistered(String s)
    {
        return m_hashDocbaseLevelNodes.get(s) != null;
    }

    Iterator getDocbaseLevelNodes(String s)
    {
        Iterator iterator = null;
        if(s != null)
        {
            ArrayList arraylist = (ArrayList)m_hashDocbaseLevelNodes.get(s);
            if(arraylist != null)
                iterator = arraylist.iterator();
        }
        return iterator;
    }

    Iterator getDocbaseLevelNodes()
    {
        String s = SessionManagerHttpBinding.getCurrentDocbase();
        ArrayList arraylist = (ArrayList)m_hashDocbaseLevelNodes.get(s);
        if(arraylist == null && m_hashDocbaseLevelNodes.size() > 0)
            arraylist = (ArrayList)m_hashDocbaseLevelNodes.elements().nextElement();
        if(arraylist == null)
            arraylist = new ArrayList(0);
        return arraylist.iterator();
    }

    Iterator getTopLevelNodes()
    {
        return m_topLevelNodes.iterator();
    }

    private void buildComponentNodeId(String s, String s1, StringBuffer stringbuffer)
    {
        TreeNode treenode = getNodeWithId("_ROOT." + s);
        Iterator iterator = treenode.getChildren();
        TreeNode treenode1 = searchChildNodes(iterator, s1);
        String s2 = treenode1.getUniqueId();
        stringbuffer.append(s2);
    }

    private TreeNode searchChildNodes(Iterator iterator, String s)
    {
        TreeNode treenode;
        for(treenode = null; treenode == null && iterator.hasNext();)
        {
            TreeNode treenode1 = (TreeNode)iterator.next();
            if(treenode1.getId().equals(s))
                treenode = treenode1;
            else
            if(treenode1.getChildrenCount() != 0)
                treenode = searchChildNodes(treenode1.getChildren(), s);
        }

        return treenode;
    }

    private void setupBrowserIcons()
    {
        String s = getNavigationIconFolder();
        m_strIconDCTM = s + "portico_logo_16.gif";
    }

    private String getNavigationIconFolder()
    {
        return "icons/browsertree/";
    }

    private void addDocbaseSubTree(String s)
    {
        if(!m_hashDocbaseTrees.containsKey(s))
        {
            DocbaseNavigationNode docbasenavigationnode = new DocbaseNavigationNode(this, s, getIconForDepth(0), s, s, super.m_rootNode);
            m_hashDocbaseTrees.put(s, docbasenavigationnode);
            IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
            IDfSession idfsession = null;
            try
            {
                idfsession = idfsessionmanager.getSession(s);
                if(docbasenavigationnode.getLabel().indexOf(" : ") == -1)
                    docbasenavigationnode.setLabel(docbasenavigationnode.getLabel() + " : " + idfsession.getLoginUserName());
                docbasenavigationnode.getData();
                docbasenavigationnode.expandNode();
                docbasenavigationnode.setSelectable(false);
            }
            catch(Exception exception) { }
            finally
            {
                if(idfsession != null)
                    idfsessionmanager.release(idfsession);
            }
        }
    }

    public static final String ID_DCTM_ROOT = "_ROOT";
    private DocbaseFolderTreeNode m_docbaseRootNode;
    private ArrayList m_topLevelNodes;
    Hashtable m_hashDocbaseLevelNodes;
    private HashMap m_hashDocbaseTrees;
    private String m_strRootNLS;
    private String m_strFoldersNLS;
    private String m_strIconDCTM;
    private static final String ICON_DCTM = "portico_logo_16.gif";


}