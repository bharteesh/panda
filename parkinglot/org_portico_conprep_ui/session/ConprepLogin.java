package org.portico.conprep.ui.session;

import javax.servlet.jsp.PageContext;

import org.portico.common.config.LdapUtil;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.BrandingService;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;

public class ConprepLogin extends com.documentum.web.formext.session.Login
{

    public static final String SUB_CONTEXT_DOCUMENTUM="dc=documentum";
    public static final String ELEMENT_DOCBASE_CONFIG="cn=docbaseconfig";
    public static final String ATTRIBUTE_DOCBASE_NAME="docbase";
    public static final String ATTRIBUTE_DOCBASE_DESCRIPTION="description";
    public static final String ATTRIBUTE_DOCBASE_USER_GROUP="users_group";

    public ConprepLogin()
    {

        m_strStartURL = null;
        m_strStartComponent = null;
        m_strStartPage = null;
        m_entryArgs = null;
        m_bShowOptions = false;
        m_bChangePassword = false;
        m_bInitialAccessibilityState = false;
        m_strDocbase = null;
        m_strUsername = null;
        m_strDomain = null;
    }

     public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        Button button = (Button)getControl("changePassword", com.documentum.web.form.control.Button.class);
        button.setLabel(getString("MSG_CHANGE_PASSWORD"));
        button.setVisible(true);
    }

    public void onLogin(Button button, ArgumentList argumentlist)
    {
        clearErrorMessage();
        super.validate();
        if(getIsValid())
            try
            {
                //m_strDocbase = lookupString("docbasename");
                m_strDocbase = LdapUtil.getAttribute(SUB_CONTEXT_DOCUMENTUM,
                                                        ELEMENT_DOCBASE_CONFIG,
                                                        ATTRIBUTE_DOCBASE_NAME).trim();
                m_strUsername = ((Text)getControl("username")).getValue();
                String s = ((Password)getControl("password")).getValue();
                m_strDomain = ((Text)getControl("domain", com.documentum.web.form.control.Text.class)).getValue();
                try
                {
                    authenticate(m_strDocbase, m_strUsername, s, m_strDomain);

                }
                catch(com.documentum.web.formext.session.PasswordExpiredException passwordexpiredexception)
                {
                    m_bChangePassword = true;
                }
                if(m_bChangePassword)
                {
                    ArgumentList argumentlist1 = new ArgumentList();
                    argumentlist1.add("username", m_strUsername);
                    argumentlist1.add("docbase", m_strDocbase);
                    argumentlist1.add("domain", m_strDomain);
                    setComponentNested("changepassword", argumentlist1, getContext(), this);
                } else
                {
                    //VERIFY IF THE USER BELONGS TO THE CONPREP USER'S GROUP
                    String conprepGroup = LdapUtil.getAttribute(SUB_CONTEXT_DOCUMENTUM,
                                                                ELEMENT_DOCBASE_CONFIG,
                                                                ATTRIBUTE_DOCBASE_USER_GROUP);
                    StringBuffer dql = new StringBuffer();
                    dql.append("select group_name from dm_group where any i_all_users_names='");
                    dql.append(m_strUsername);
                    dql.append("' AND group_name = '");
                    dql.append(conprepGroup);
                    dql.append("'");
                    IDfQuery qry = new DfQuery();
                    qry.setDQL(dql.toString());
                    IDfCollection col = qry.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                    if(col.next() ){
                        String docbaseDesc = LdapUtil.getAttribute(SUB_CONTEXT_DOCUMENTUM,
                                ELEMENT_DOCBASE_CONFIG,
                                ATTRIBUTE_DOCBASE_DESCRIPTION);
                        getPageContext().setAttribute(ATTRIBUTE_DOCBASE_DESCRIPTION,
                                            docbaseDesc,
                                            PageContext.SESSION_SCOPE);
                        handleSuccess();
                    }else{
                        setComponentPage("noaccess");
                    }
                    if( null != col){
                        col.close();
                    }
                }
            }
            catch(Exception exception)
            {
                String s1 = getString("LOGIN_ERROR_MSG");
                setLoginErrorMessage(s1);
            }
    }

    protected void writePreferences()
    {
        super.writePreferences();
        String theme = lookupString("guitheme");
        BrandingService.setTheme(theme);
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString("theme", theme);

    }

    protected void setLoginErrorMessage(String s)
    {
        Label label = (Label)getControl("errorMessage", com.documentum.web.form.control.Label.class);
        label.setLabel(s);
        Panel panel = (Panel)getControl("errorMessagePanel", com.documentum.web.form.control.Panel.class);
        panel.setVisible(true);
    }


    private String m_strStartURL;
    private String m_strStartComponent;
    private String m_strStartPage;
    private ArgumentList m_entryArgs;
    private boolean m_bShowOptions;
    private boolean m_bChangePassword;
    private boolean m_bInitialAccessibilityState;
    private String m_strDocbase;
    private String m_strUsername;
    private String m_strDomain;

}