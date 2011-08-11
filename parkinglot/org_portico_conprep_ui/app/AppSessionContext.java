
package org.portico.conprep.ui.app;

import java.util.ArrayList;
import java.util.Hashtable;

import com.documentum.web.common.SessionState;

// This class name is a misnomer, even though it appears as Session scoped, it has been changed
//      to Application Scope and Session Scope


public class AppSessionContext
{
    public synchronized static boolean getLoaded()
    {
		return isLoaded;
	}

    public synchronized static void setLoaded(boolean tIsLoaded)
    {
		isLoaded = tIsLoaded;
	}

    public static void setProviderUI(ArrayList providerInfo)
    {
        g_providerInfo = providerInfo;
    }

    public static ArrayList getProviderUI()
    {
        return g_providerInfo;
    }

    public static void setProviderProfileMappingAsStringUI(String providerProfileMappingAsString)
    {
        g_providerProfileMappingInfoAsString = providerProfileMappingAsString;
    }

    public static String getProviderProfileMappingAsStringUI()
    {
        return g_providerProfileMappingInfoAsString;
    }

    public static void setSubmissionAreaObjectIdUI(String batchFolderObjectId)
    {
        g_batchFolderObjectId = batchFolderObjectId;
    }

    public static String getSubmissionAreaObjectIdUI()
    {
        return g_batchFolderObjectId;
    }

    public static void setWorkflowActivityListUI(Hashtable wfActList)
    {
        g_wfActList = wfActList;
    }

    public static Hashtable getWorkflowActivityListUI()
    {
        return g_wfActList;
    }

    public static void setActionReentryPointMappingListUI(Hashtable actReentryList)
    {
        g_actReentryList = actReentryList;
    }

    public static Hashtable getActionReentryPointMappingListUI()
    {
        return g_actReentryList;
    }

    public static void setDefaultBatchList(ArrayList batchList)
    {
        g_batchList = batchList;
    }

    public static ArrayList getDefaultBatchList()
    {
        return g_batchList;
    }

    // Future
    public static void setSearchObjectList(ArrayList searchObjList)
    {
        g_searchObjList = searchObjList;
    }

    public static ArrayList getSearchObjectList()
    {
        return g_searchObjList;
    }

    // These are per Session of the logged in user
    public static void setUserRolesUI(ArrayList userRoles)
    {
        SessionState.setAttribute("__dmwtUserRolesList", userRoles);
    }

    public static ArrayList getUserRolesUI()
    {
        return (ArrayList)SessionState.getAttribute("__dmwtUserRolesList");
    }

    public static void setUsersAndRolesUI(Hashtable usersAndRoles)
    {
        SessionState.setAttribute("__dmwtUsersAndRolesTable", usersAndRoles);
    }

    public static Hashtable getUsersAndRolesUI()
    {
        return (Hashtable)SessionState.getAttribute("__dmwtUsersAndRolesTable");
    }

    public static void setCurrentUserAndGroupsListUI(ArrayList currentUserAndGroupsList)
    {
        SessionState.setAttribute("__dmwtCurrentUserAndGroupsListUI", currentUserAndGroupsList);
    }

    public static ArrayList getCurrentUserAndGroupsListUI()
    {
        return (ArrayList)SessionState.getAttribute("__dmwtCurrentUserAndGroupsListUI");
    }

/*
    private static final String SESSION_SCOPE_PROVIDER_UI = "__dmwtProviderUI";
    private static final String SESSION_SCOPE_BATCHFOLDEROBJECTID_UI = "__dmwtBatchFolderObjectIdUI";
    private static final String SESSION_SCOPE_WORKFLOWACTIVITYLIST_UI = "__dmwtWorkflowActivityOrderedListUI";
    private static final String SESSION_SCOPE_ACTIONREENTRYPOINTLIST_UI = "__dmwtActionRentryListUI";

    // Future
    private static final String SESSION_SCOPE_SEARCH_OBJECT_LIST = "__dmwtSearchObjectList";
    private static final String SESSION_SCOPE_DEFAULT_BATCH_LIST = "__dmwtDefaultBatchList";
*/
    // Session scoped objects
    private static final String SESSION_SCOPE_USERROLES_UI = "__dmwtUserRolesList";
    private static final String SESSION_SCOPE_USERSANDROLES_UI = "__dmwtUsersAndRolesTable";
    private static final String SESSION_SCOPE_CURRENTUSERANDGROUPS_UI = "__dmwtCurrentUserAndGroupsListUI";

    private static boolean isLoaded = false;

    // These set of information is stored in the application scope
    private static ArrayList g_providerInfo = null;
    private static String g_providerProfileMappingInfoAsString = "";
    private static String g_batchFolderObjectId = "";
    private static Hashtable g_wfActList = null;
    private static Hashtable g_actReentryList = null;
    private static ArrayList g_searchObjList = null;
    private static ArrayList g_batchList = null;
    // private static ArrayList g_userRoles = null;
    // private static Hashtable g_usersAndRoles = null;
    // private static ArrayList g_currentUserAndGroupsList = null;
}