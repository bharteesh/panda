package org.portico.conprep.ui.session;

import org.portico.common.config.LdapUtil;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Password;
import com.documentum.web.form.control.Text;
import com.documentum.web.formext.component.Component;

public class ChangePassword extends Component
{

    public static final String SUB_CONTEXT_DOCUMENTUM="dc=documentum";
    public static final String ELEMENT_DOCBASE_CONFIG="cn=docbaseconfig";
    public static final String ATTRIBUTE_DOCBASE_NAME="docbase";
    public static final String ATTRIBUTE_DOCBASE_DESCRIPTION="description";

    public static final String CONTROL_USERNAME = "username";
    public static final String CONTROL_DOMAIN = "domain";
    public static final String CONTROL_DOCBASE = "docbase";
    public static final String CONTROL_PASSWORD = "password";
    public static final String CONTROL_OLDPASSWORD = "oldPassword";
    public static final String CONTROL_NEWPASSWORD = "newPassword";
    public static final String CONTROL_CONFIRMPASSWORD = "confirmPassword";
    public static final String CONTROL_ERRMSGPANEL = "errorMessagePanel";
    public static final String CONTROL_ERRMSG = "errorMessage";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_DOCBASE = "docbase";
    private static final String ARG_DOMAIN = "domain";
    private static final String CONFIG_EDITABLEUSERNAME = "editableusername";
    private static final String ERR_MSG_FROM_SERVER = "Error message from server was:";

    private static final String LDAP_SUB_CONTEXT_USER = "ou=People";
    private static final String LDAP_SUB_CONTEXT_USER_PASSWD = "userPassword";
    private static final String LDAP_SUB_CONTEXT_USER_UID = "uid=";

    public ChangePassword()
    {
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        String s = argumentlist.get("username");
        Boolean boolean1 = lookupBoolean("editableusername");
        if(boolean1 == null)
        {
            boolean1 = Boolean.FALSE;
        }
        clearErrorMessage();
        initUsername(boolean1.booleanValue(), s);
    }

    public void onChangePassword(Button button, ArgumentList argumentlist)
    {
        clearErrorMessage();
        if(getIsValid())
        {
            try
            {
                String docbase = LdapUtil.getAttribute(SUB_CONTEXT_DOCUMENTUM,
                                                ELEMENT_DOCBASE_CONFIG,
                                                ATTRIBUTE_DOCBASE_NAME).trim();

                String username = ((Text)getControl("username")).getValue();
                String oldPassword = ((Password)getControl("oldPassword")).getValue();
                String newPassword = ((Password)getControl("newPassword")).getValue();
                String domain = "";
                // CHECK IF THE CURRENT PASSWORD IS CORRECT
                boolean isOldPasswordCorrect = checkPassword(username,docbase,oldPassword);
                
                if(isOldPasswordCorrect){ 
                // CHANGE THE PASSWORD
                	changePassword(username, docbase, domain, oldPassword, newPassword);
                	setReturnValue("docbase", docbase);
                    setReturnValue("username", username);
                    setReturnValue("password", newPassword);
                    setReturnValue("domain", domain);
                    setComponentReturn();
                }else{
                	setErrorMessage("could not verify your old password. Please check your password and try again.");
                }
                
            }
            catch(Exception exception)
            {
                String s2 = getServerError(exception.getMessage());
                if(s2 == null)
                {
                    s2 = exception.getMessage();
                }
                setErrorMessage(s2);
            }
        }
    }

    public void onCancel(Button button, ArgumentList argumentlist)
    {
        setComponentReturn();
    }

    protected void clearErrorMessage()
    {
        Panel panel = (Panel)getControl("errorMessagePanel", com.documentum.web.form.control.Panel.class);
        panel.setVisible(false);
        Label label = (Label)getControl("errorMessage", com.documentum.web.form.control.Label.class);
        label.setLabel("");
    }

    protected void setErrorMessage(String s)
    {
        Label label = (Label)getControl("errorMessage", com.documentum.web.form.control.Label.class);
        label.setLabel(s);
        Panel panel = (Panel)getControl("errorMessagePanel", com.documentum.web.form.control.Panel.class);
        panel.setVisible(true);
    }

    protected void initUsername(boolean flag, String s)
    {
        Text text = (Text)getControl("username", com.documentum.web.form.control.Text.class);
        text.setValue(s);
        text.setEnabled(flag);
    }

    protected void changePassword(String username, String docbase, String domain, String oldPassword, String newPassword)
        throws Exception
    {
    	
    	
    	LdapUtil.modifyAttribute(LDAP_SUB_CONTEXT_USER, 
    					LDAP_SUB_CONTEXT_USER_UID+username,
    					LDAP_SUB_CONTEXT_USER_PASSWD,
    					newPassword);
    	
    	/*
    	IAuthenticationService iauthenticationservice = AuthenticationService.getService();
        iauthenticationservice.changePassword(getPageContext().getSession(), username, docbase, domain, oldPassword, newPassword);
        */
    }

    protected boolean checkPassword(String username, String docbase, String password)
    {
    	try{
	    	IDfSession dfSession = getDfSession(docbase,username,password);
	    	if (null != dfSession && dfSession.isConnected()){
	    		dfSession.disconnect();
	    		return true;
	    	}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }
    
    private String getServerError(String s)
    {
        String s1 = null;
        try
        {
            if(s != null && s.length() > 0)
            {
                int i = s.indexOf("Error message from server was:");
                if(i >= 0)
                {
                    for(i += "Error message from server was:".length(); Character.isWhitespace(s.charAt(i)); i++) { }
                    int j = s.indexOf("\"", i);
                    int k = s.length();
                    if(j > i)
                    {
                        k = s.indexOf("\"", j + 1) + 1;
                        if(k == 0)
                        {
                            k = s.length();
                        }
                    }
                    s1 = s.substring(i, k);
                }
            }
        }
        catch(Exception exception) { }
        return s1;
    }
    
    public static IDfSession getDfSession(String docbaseName, String userName, String passwd )throws DfException{
		IDfLoginInfo logininfo = new DfLoginInfo();
		logininfo.setUser(userName);
		logininfo.setPassword(passwd);
		return DfClient.getLocalClient().newSession(docbaseName, logininfo);
		
	}
}
