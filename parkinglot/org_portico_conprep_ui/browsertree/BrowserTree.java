
package org.portico.conprep.ui.browsertree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.portico.conprep.ui.control.ConprepBrowserTree;

import com.documentum.web.common.AccessibilityService;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.TreeNode;
import com.documentum.web.formext.config.ConfigService;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.navigation.foldertree.FolderTree;
import com.documentum.webcomponent.navigation.homecabinet.HomeCabinetService;
import com.documentum.webtop.app.AppSessionContext;
import com.documentum.webtop.app.ApplicationLocation;

public class BrowserTree extends FolderTree
{

    public BrowserTree()
    {
        m_strPreferenceEntryNode = null;
        m_strLoginDocbase = null;
        m_nodeSet = new HashSet(7, 1.0F);
        m_bSetFireNodeSelectedClientEvent = false;
    }

    public void onInit(ArgumentList argumentlist)
    {
        ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser", org.portico.conprep.ui.control.ConprepBrowserTree.class);
        readConfig();
        webtopbrowsertree.setRootNLS(getString("MSG_ROOT_NODE"));
        webtopbrowsertree.setFoldersNLS(getString("MSG_DOCBASE_FOLDERS"));
        String s = getEntryNodeId(webtopbrowsertree, argumentlist);
        String s1 = getSelectedNodeId(s, webtopbrowsertree, argumentlist);
        m_strPreferenceEntryNode = s;
        super.onInit(argumentlist);
        webtopbrowsertree.setSelectedId(s1);
        s1 = webtopbrowsertree.getSelectedId();
        fireNodeSelectedClientEvent(s1);
        Panel panel = (Panel)getControl("workarealinkpanel", com.documentum.web.form.control.Panel.class);
        panel.setVisible(AccessibilityService.isAllAccessibilitiesEnabled());
    }

    public void onRender()
    {
        super.onRender();
        ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
        String s = webtopbrowsertree.getSelectedId();
        if(!isValidTreeNodeID(webtopbrowsertree, s))
        {
            ErrorMessageService.getService().setNonFatalError(this, "MSG_CANNOT_FETCH_OBJECT", null);
        } else
        {
            if(!m_bSetFireNodeSelectedClientEvent && webtopbrowsertree != null)
            {
                String s1 = getPageContext().getRequest().getParameter("entryNode");
                if(s1 != null && s1.length() > 0)
                {
                    ArgumentList argumentlist = new ArgumentList();
                    argumentlist.add("entryNode", s1);
                    s1 = getEntryNodeId(webtopbrowsertree, argumentlist);
                    String s3 = getSelectedNodeId(s1, webtopbrowsertree, argumentlist);
                    webtopbrowsertree.setSelectedId(s3);
                    s3 = webtopbrowsertree.getSelectedId();
                    fireNodeSelectedClientEvent(s3);
                }
            }
            if(s != null)
            {
                StringTokenizer stringtokenizer = new StringTokenizer(s, ".");
                if(stringtokenizer.countTokens() > 2)
                {
                    String s2 = stringtokenizer.nextToken();
                    if(s2.equals("_ROOT"))
                    {
                        stringtokenizer.nextToken();
                        String s4 = stringtokenizer.nextToken();
                        StringBuffer stringbuffer = new StringBuffer(64);
                        while(stringtokenizer.hasMoreTokens())
                        {
                            stringbuffer.append(stringtokenizer.nextToken());
                            if(stringtokenizer.hasMoreTokens())
                                stringbuffer.append('.');
                        }
                        String s5 = "";
                        if(stringbuffer.length() != 0)
                            s5 = FolderUtil.getFolderPathFromIds(stringbuffer.toString());
                        AppSessionContext appsessioncontext = AppSessionContext.get(getPageContext().getSession());
                        if(s4.charAt(0) == '/')
                            appsessioncontext.getAppLocation().setAppLocation(getCurrentDocbase(), s5);
                        else
                            appsessioncontext.getAppLocation().setAppLocation(getCurrentDocbase(), s4, s5);
                    }
                }
            }
        }
    }

    private boolean isValidTreeNodeID(ConprepBrowserTree webtopbrowsertree, String s)
    {
        Object obj = null;
        TreeNode treenode = null;
        StringTokenizer stringtokenizer = new StringTokenizer(s, ".");
        int i = 0;
        while(stringtokenizer.hasMoreTokens())
        {
            String s1 = stringtokenizer.nextToken();
            treenode = webtopbrowsertree.getChildNodeWithId(s1, treenode);
            i++;
            if(s1.length() == 16 && i > 3 && treenode != null && (treenode.getClass().getName().equalsIgnoreCase((com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode.class).getName()) || treenode.getClass().getName().equalsIgnoreCase((com.documentum.webtop.webcomponent.homecabinet.HomeCabinetNode.class).getName())) && !DocbaseUtils.doesDocbaseObjectExist(s1))
                return false;
        }
        return true;
    }

    public void onRenderEnd()
    {
        super.onRenderEnd();
        m_bSetFireNodeSelectedClientEvent = false;
    }

    public void onTreeInvalidated(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("docbase");
        if(s == null || s.length() == 0)
        {
            throw new IllegalArgumentException("Docbase name must be specified for tree refresh!");
        } else
        {
            ((ConprepBrowserTree)getControl("docbrowser")).refresh(s, argumentlist.get("componentId"));
            return;
        }
    }

    public void refreshTreeFromId(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("componentId");
        String s1 = argumentlist.get("objectId");
        String s2 = argumentlist.get("data");
        if(s1 == null)
        {
            throw new IllegalArgumentException("Must specify objectId argument!");
        } else
        {
            ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
            webtopbrowsertree.selectNodeFromObjectId(s, s1, s2);
            return;
        }
    }

    public void refreshTreeFromAbsolutePath(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("objectIds");
        String s1 = argumentlist.get("componentId");
        ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
        webtopbrowsertree.selectNodeFromAbsolutePath(getCurrentDocbase(), s1, s);
    }

    public void onLogin(Control control, ArgumentList argumentlist)
    {
        if(m_strLoginDocbase != null)
        {
            readDocbaseLevelConfig(m_strLoginDocbase);
            ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
            if(webtopbrowsertree.refreshDocbaseSubTree(m_strLoginDocbase))
            {
                String s = lookupString("nodes.docbasenodes.entrynode");
                if(s == null || s.length() == 0)
                    s = "cabinets";
                ArgumentList argumentlist1 = new ArgumentList();
                argumentlist1.add("entryNode", s);
                onclickTree(webtopbrowsertree, argumentlist1);
            }
            m_strLoginDocbase = null;
        }
    }

    public void onclickTree(ConprepBrowserTree webtopbrowsertree, ArgumentList argumentlist)
    {
        DocbaseFolderTreeNode docbasefoldertreenode = (DocbaseFolderTreeNode)webtopbrowsertree.getSelectedNode();
        String s = null;
        String s1 = argumentlist.get("entryNode");
        if(s1 != null && !m_strPreferenceEntryNode.equals("cabinets"))
        {
            StringBuffer stringbuffer = new StringBuffer();
            stringbuffer.append("_ROOT");
            stringbuffer.append('.');
            stringbuffer.append(docbasefoldertreenode.getDocbaseName());
            stringbuffer.append('.');
            stringbuffer.append(s1);
            s = stringbuffer.toString();
            webtopbrowsertree.setSelectedId(s);
            s = webtopbrowsertree.getSelectedId();
        } else
        {
            s = docbasefoldertreenode.getUniqueId();
        }
        int i = s.indexOf('.');
        s = s.substring(i + 1);
        i = s.indexOf('.');
        if(i != -1)
            s = s.substring(i + 1);
        String s2 = docbasefoldertreenode.getDocbaseName();
        AppSessionContext.setEntryPointInView(s);
        AppSessionContext.setRefreshDocbaseName(s2);
        MessageService.clear(webtopbrowsertree.getForm());
        if(s2 != null)
            if(docbasefoldertreenode.getParent().getParent() == null && docbasefoldertreenode.getId().equals(s2))
            {
                requestDocbaseAuthentication(s2);
            } else
            {
                SessionManagerHttpBinding.setCurrentDocbase(s2);
                if(!webtopbrowsertree.hasDocbaseLevelNodeRegistered(s2))
                    readDocbaseLevelConfig();
                if(s1 == null)
                    fireNodeSelectedClientEvent(docbasefoldertreenode.getUniqueId());
                else
                    fireNodeSelectedClientEvent(s);
            }
    }

    public void requestDocbaseAuthentication(String s)
    {
        ArgumentList argumentlist = new ArgumentList();
        argumentlist.add("docbase", s);
        setClientEvent("authenticate", argumentlist);
        m_strLoginDocbase = s;
    }

    public void onInitEntryNode(Control control, ArgumentList argumentlist)
    {
        String s = argumentlist.get("entryNode");
        if(s != null && s.length() > 0)
        {
            ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser", org.portico.conprep.ui.control.ConprepBrowserTree.class);
            String s1 = getEntryNodeId(webtopbrowsertree, argumentlist);
            String s2 = getSelectedNodeId(s1, webtopbrowsertree, argumentlist);
            m_strPreferenceEntryNode = s1;
            webtopbrowsertree.setSelectedId(s2);
            s2 = webtopbrowsertree.getSelectedId();
            fireNodeSelectedClientEvent(s2);
        }
    }

    private void readConfig()
    {
        ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
        IConfigElement iconfigelement = lookupElement("nodes");
        if(iconfigelement != null)
        {
            String s;
            String s1;
            String s2;
            String s3;
            String s4;
            for(Iterator iterator = iconfigelement.getChildElements("node"); iterator.hasNext(); webtopbrowsertree.registerTopLevelNode(s, s1, s2, s3, s4))
            {
                IConfigElement iconfigelement1 = (IConfigElement)iterator.next();
                s = iconfigelement1.getAttributeValue("componentid");
                if(s == null || s.length() == 0)
                    throw new IllegalArgumentException("Custom browser node must specify component id!");
                s1 = iconfigelement1.getChildValue("startpage");
                if(s1 == null || s1.length() == 0 || s1.equals("start"))
                    s1 = null;
                s2 = iconfigelement1.getChildValue("icon");
                if(s2 == null || s2.length() == 0)
                    throw new IllegalArgumentException("Custom browser node must specify icon!");
                s3 = iconfigelement1.getChildValue("label");
                if(s3 == null || s3.length() == 0)
                    throw new IllegalArgumentException("Custom browser node must specify label!");
                s4 = iconfigelement1.getChildValue("handlerclass");
            }

            readDocbaseLevelConfig();
        }
    }

    public void readDocbaseLevelConfig(String s)
    {
        String s1 = SessionManagerHttpBinding.getCurrentDocbase();
        if(s == null || s1 == null || s1.equals(s))
            readDocbaseLevelConfig();
        else
            synchronized(getPageContext().getSession())
            {
                synchronized(SessionManagerHttpBinding.getSessionManager())
                {
                    try
                    {
                        SessionManagerHttpBinding.setCurrentDocbase(s);
                        readDocbaseLevelConfig();
                    }
                    finally
                    {
                        SessionManagerHttpBinding.setCurrentDocbase(s1);
                    }
                }
            }
    }

    private void readDocbaseLevelConfig()
    {
        ConprepBrowserTree webtopbrowsertree = (ConprepBrowserTree)getControl("docbrowser");
        IConfigElement iconfigelement = ConfigService.getConfigLookup().lookupElement("component[id=" + getComponentId() + "].nodes", Context.getSessionContext());
        if(iconfigelement != null)
        {
            IConfigElement iconfigelement1 = iconfigelement.getChildElement("docbasenodes");
            if(iconfigelement1 != null)
            {
                String s;
                String s1;
                String s2;
                String s3;
                String s4;
                for(Iterator iterator = iconfigelement1.getChildElements("node"); iterator.hasNext(); webtopbrowsertree.registerDocbaseLevelNode(s, s1, s2, s3, s4))
                {
                    IConfigElement iconfigelement2 = (IConfigElement)iterator.next();
                    s = iconfigelement2.getAttributeValue("componentid");
                    if(s == null || s.length() == 0)
                        throw new IllegalArgumentException("Custom browser node must specify component id!");
                    m_nodeSet.add(s);
                    s1 = iconfigelement2.getChildValue("startpage");
                    if(s1 == null || s1.length() == 0 || s1.equals("start"))
                        s1 = null;
                    s2 = iconfigelement2.getChildValue("icon");
                    if((s2 == null || s2.length() == 0) && !s.equalsIgnoreCase("cabinets"))
                        throw new IllegalArgumentException("Custom browser node must specify icon!");
                    s3 = iconfigelement2.getChildValue("label");
                    if(s3 == null || s3.length() == 0)
                        throw new IllegalArgumentException("Custom browser node must specify label!");
                    s4 = iconfigelement2.getChildValue("handlerclass");
                    if(s.equalsIgnoreCase("cabinets") && (s4 == null || s4.length() == 0))
                        s4 = "com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode";
                }

            }
        }
    }

    protected String getEntryNodeId(ConprepBrowserTree webtopbrowsertree, ArgumentList argumentlist)
    {
        String s = argumentlist.get("entryNode");
        if(s == null || s.length() == 0)
            s = lookupString("nodes.docbasenodes.entrynode");
        if(s == null || s.length() == 0)
            s = "cabinets";
        else
        if(s.charAt(0) != '/')
        {
            String s1 = s;
            int i = s1.indexOf('.');
            if(i != -1)
                s1 = s1.substring(0, i);
            if(!m_nodeSet.contains(s1))
                s = "cabinets";
        }
        AppSessionContext.setEntryPointInView(s);
        return s;
    }

    protected String getSelectedNodeId(String s, ConprepBrowserTree webtopbrowsertree, ArgumentList argumentlist)
    {
        String s1 = null;
        ApplicationLocation applicationlocation = AppSessionContext.get(getPageContext().getSession()).getAppLocation();
        if(s.equals("cabinets") || s.charAt(0) == '/')
        {
            argumentlist.remove("folderPath");
            StringBuffer stringbuffer = new StringBuffer(128);
            webtopbrowsertree.buildDocbaseRootId(getCurrentDocbase(), stringbuffer);
            String s3 = applicationlocation.getFolderPath();
            if(s3 != null && s3.length() != 0)
            {
                stringbuffer.append('.');
                stringbuffer.append(FolderUtil.getFolderIdsFromPath(s3));
            }
            s1 = stringbuffer.toString();
        } else
        if(s.startsWith("homecabinet") && applicationlocation.getComponentId() != null && applicationlocation.getComponentId().startsWith("homecabinet"))
        {
            argumentlist.remove("folderPath");
            String s2 = null;
            int i = s.indexOf('.');
            s2 = i != -1 ? s.substring(0, i) : s;
            StringBuffer stringbuffer2 = new StringBuffer(128);
            webtopbrowsertree.buildComponentRootId(getCurrentDocbase(), s2, stringbuffer2);
            String s4 = applicationlocation.getComponentPath();
            if(s4 != null && s4.length() != 0)
            {
                stringbuffer2.append('.');
                String s5 = HomeCabinetService.getHomeCabinetPath();
                String s6 = FolderUtil.getFolderIdsFromPath(s5 + s4);
                for(int j = 0; j < FolderUtil.getPathDepth(s5); j++)
                    if(s6.length() > 16)
                        s6 = s6.substring(17);

                stringbuffer2.append(s6);
            }
            s1 = stringbuffer2.toString();
        } else
        {
            StringBuffer stringbuffer1 = new StringBuffer(128);
            webtopbrowsertree.buildComponentRootId(getCurrentDocbase(), s, stringbuffer1);
            s1 = stringbuffer1.toString();
        }
        return s1;
    }

    protected void fireNodeSelectedClientEvent(String s)
    {
        int i = s.indexOf('.');
        s = s.substring(i + 1);
        i = s.indexOf('.');
        if(i != -1)
            s = s.substring(i + 1);
        ArgumentList argumentlist = new ArgumentList();
        argumentlist.add("id", s);
        setClientEvent("onNodeSelected", argumentlist);
        m_bSetFireNodeSelectedClientEvent = true;
    }

    private String m_strPreferenceEntryNode;
    private String m_strLoginDocbase;
    private Set m_nodeSet;
    private boolean m_bSetFireNodeSelectedClientEvent;
    private static final String CABINETS_NODE_CLASS = "com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode";
    public static final String ID_DCTM_ROOT = "_ROOT";
    public static final String TREE_CONTROL = "docbrowser";
    public static final String PREF_ENTRY_NODE = "nodes.docbasenodes.entrynode";
    public static final String PREF_ENTRY_NODE_DEFAULT = "cabinets";
    public static final String ARG_ENTRY_NODE = "entryNode";
    public static final String CABINETS_NODE = "cabinets";
    public static final String WORKAREALINKPANEL_CONTROL = "workarealinkpanel";
}