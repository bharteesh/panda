package org.portico.conprep.ui.control;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.ConfigService;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.config.IConfigLookup;
import com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode;

// Referenced classes of package com.documentum.webtop.control:
//            ConprepBrowserTree

public class BrowserNavigationNode extends DocbaseFolderTreeNode
{

    public BrowserNavigationNode(ConprepBrowserTree webtopbrowsertree, String s, String s1, String s2, String s3, TreeNode treenode)
    {
        super(webtopbrowsertree, s, s1, s2, null, s3, treenode);
        m_oContext = null;
        m_strIconFolder = null;
        m_oContext = ((Component)webtopbrowsertree.getForm()).getContext();
    }

    public void getData()
    {
    }

    public boolean mayHaveChildren()
    {
        return false;
    }

    protected void selectNodeFromObjectId(String s, String s1)
    {
        String s2 = super.m_tree.getSelectedId();
        StringBuffer stringbuffer = new StringBuffer(s2.length() + 17);
        stringbuffer.append(s2).append('.').append(s);
        super.m_tree.setSelectedId(stringbuffer.toString());
    }

    public static String createNodeId(String s, String s1, String s2)
    {
        StringBuffer stringbuffer = new StringBuffer(64);
        stringbuffer.append(s1);
        if(s2 != null)
        {
            stringbuffer.append(':');
            stringbuffer.append(s2);
        }
        return stringbuffer.toString();
    }

    protected void getNodesFromComponent()
    {
        IConfigLookup iconfiglookup = ConfigService.getConfigLookup();
        IConfigElement iconfigelement = iconfiglookup.lookupElement("component[id=" + getId() + "]." + "nodes", m_oContext);
        for(Iterator iterator = iconfigelement.getChildElements("node"); iterator.hasNext();)
        {
            IConfigElement iconfigelement1 = (IConfigElement)iterator.next();
            String s = iconfigelement1.getAttributeValue("componentid");
            String s1 = iconfigelement1.getChildValue("label");
            String s2 = getIconFolder() + iconfigelement1.getChildValue("icon");
            String s3 = iconfigelement1.getChildValue("handlerclass");
            if(s3 == null)
                s3 = getClass().getName();
            try
            {
                Class class1 = Class.forName(s3);
                Class aclass[] = {
                    org.portico.conprep.ui.control.ConprepBrowserTree.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, com.documentum.web.form.control.TreeNode.class
                };
                Constructor constructor = class1.getConstructor(aclass);
                Object aobj[] = {
                    (ConprepBrowserTree)super.m_tree, createNodeId(getDocbaseName(), s, null), s2, s1, getDocbaseName(), this
                };
                Object obj = constructor.newInstance(aobj);
                if(!(obj instanceof BrowserNavigationNode))
                    throw new IllegalArgumentException("Custom tree node handler class not of correct type");
            }
            catch(Exception exception)
            {
                throw new WrapperRuntimeException("Unable to create custom browser node handler class: " + exception.toString(), exception);
            }
        }

    }

    protected void setIconFolder(String s)
    {
        m_strIconFolder = s;
    }

    protected String getIconFolder()
    {
        String s = m_strIconFolder;
        if(s != null);
        s = "icons/browsertree/";
        return s;
    }

    public static final String TAG_NODES = "nodes";
    public static final String TAG_NODE = "node";
    public static final String TAG_HANDLER_CLASS = "handlerclass";
    public static final String TAG_ICON = "icon";
    public static final String TAG_COMPONENT_ID = "componentid";
    public static final String TAG_LABEL = "label";
    protected Context m_oContext;
    private String m_strIconFolder;
}