package org.portico.conprep.ui.control;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.portico.conprep.ui.browsertree.BrowserTree;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.session.SessionManagerHttpBinding;

// Referenced classes of package com.documentum.webtop.control:
//            BrowserNavigationNode, ConprepBrowserTree

public final class DocbaseNavigationNode extends BrowserNavigationNode
{

    public DocbaseNavigationNode(ConprepBrowserTree webtopbrowsertree, String s, String s1, String s2, String s3, TreeNode treenode)
    {
        super(webtopbrowsertree, s, s1, s2, s3, treenode);
        m_tree = webtopbrowsertree;
    }

    public boolean mayHaveChildren()
    {
        return true;
    }

    public void getData()
    {
        String s = getDocbaseName();
        IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
        boolean flag = true;
        com.documentum.fc.client.IDfSession idfsession = null;
        try
        {
            idfsession = idfsessionmanager.getSession(s);
            flag = false;
        }
        catch(Exception exception) { }
        finally
        {
            if(idfsession != null)
                idfsessionmanager.release(idfsession);
        }
        if(!flag)
        {
            addDocbaseNodes(s);
        } else
        {
            BrowserTree browsertree = (BrowserTree)m_tree.getContainer();
            browsertree.requestDocbaseAuthentication(s);
        }
    }

    private void addDocbaseNodes(String s)
    {
        int i = getChildrenCount();
        Hashtable hashtable = null;
        if(i > 0)
        {
            hashtable = new Hashtable(i);
            TreeNode treenode;
            for(Iterator iterator = getChildren(); iterator.hasNext(); hashtable.put(treenode.getId(), treenode))
                treenode = (TreeNode)iterator.next();

        }
        Iterator iterator1 = m_tree.getDocbaseLevelNodes(s);
        if(iterator1 == null)
        {
            BrowserTree browsertree = (BrowserTree)m_tree.getContainer();
            browsertree.readDocbaseLevelConfig(s);
            iterator1 = m_tree.getDocbaseLevelNodes(s);
        }
        while(iterator1.hasNext())
        {
            ConprepBrowserTree.NodeWrapper nodewrapper = (ConprepBrowserTree.NodeWrapper)iterator1.next();
            try
            {
                TreeNode treenode1 = nodewrapper.createBrowserNode(s);
                if(hashtable == null || !hashtable.containsKey(treenode1.getId()))
                    addChild(treenode1);
                else
                    hashtable.remove(treenode1.getId());
            }
            catch(Throwable throwable) { }
        }
        if(hashtable != null)
        {
            String s1;
            for(Enumeration enumeration = hashtable.keys(); enumeration.hasMoreElements(); removeChild((TreeNode)hashtable.get(s1)))
                s1 = (String)enumeration.nextElement();

        }
    }

    private ConprepBrowserTree m_tree;
}