/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module
 * File           	SubmissionBatchObjectListWithMyBatches.java
 * Created on 		Jan 14, 2005
 *
 */
package org.portico.conprep.ui.objectlist;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.ui.docbasequery.BaseDocbaseQueryServiceWithMyBatches;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;
import org.portico.conprep.ui.helper.ValuePair;
import org.portico.conprep.ui.provider.ProviderUI;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DateInput;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Hidden;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webtop.webcomponent.objectlist.ObjectList;



public class SubmissionBatchObjectListWithMyBatches extends ObjectList
{
	/**
	 *
	 */
	public SubmissionBatchObjectListWithMyBatches()
	{
		super();
		m_StatusList = new ArrayList();
		m_statusFilter = null;
		m_statusLabel = null;
		m_hashData = new Hashtable();
		m_BatchWorkFlowId = new Hashtable();
		m_queryString = "";
		m_hashBatchStatusLookup = new Hashtable();
		m_hashRoleUserTable = new Hashtable();

        m_bSetFireSearchRequest = true;
		m_strDefaultStatusKey = "";
		m_bDefaultHoldKey = false;
		m_strDefaultProviderKey = "";
		m_strDefaultProfileKey = "";
		m_strDefaultObjectNameKey = "";
		m_strDefaultLastActivityKey = "";
		m_submissionAreaName = "";
/*
        m_fromCreationDate = null;
        m_toCreationDate = null;
        m_fromScheduleDate = null;
        m_toScheduleDate = null;
*/
	}

	public void onInit(ArgumentList argumentlist)
	{
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onInit()-Start");
		initializeCommonData();
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onInit()-Call(Start) super.onInit");
		// super.onInit(argumentlist);
        Datagrid datagrid = (Datagrid)getControl(SubmissionBatchObjectListWithMyBatches.MYBATCHES_DATAGRID_CONTROL, com.documentum.web.form.control.databound.Datagrid.class);
		readConfig(datagrid);
		// dataProvider = datagrid.getDataProvider(); // Commented not required
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onInit()-Call(End) super.onInit");

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onInit()- Before argumentlist");
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onInit()-argumentlist=" + argumentlist.toString());

        m_providerFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.PROVIDER_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_providerFilter.setMutable(true);

        m_profileFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.PROFILE_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_profileFilter.setMutable(true);

        m_performerFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.PERFORMER_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_performerFilter.setMutable(true);

        m_objectNameText = (Text)getControl(SubmissionBatchObjectListWithMyBatches.OBJECTNAME_CONTROL_FILTER, com.documentum.web.form.control.Text.class);
        m_lastActivityText = (Text)getControl(SubmissionBatchObjectListWithMyBatches.LASTACTIVITY_CONTROL_FILTER, com.documentum.web.form.control.Text.class);

        m_statusFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.STATUS_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_statusFilter.setMutable(true);
    	m_statusLabel = (Label)getControl(SubmissionBatchObjectListWithMyBatches.STATUS_LABEL, com.documentum.web.form.control.Label.class);

        // m_checkBoxOnHold = (Checkbox)getControl(SubmissionBatchObjectListWithMyBatches.ONHOLD_CONTROL_FILTER, com.documentum.web.form.control.Checkbox.class);
        m_onHoldFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.ONHOLD_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_onHoldFilter.setMutable(true);

        m_myViewsFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.MYVIEWS_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_myViewsFilter.setMutable(true);

		m_fromCreationDateControl = (DateInput)getControl(SubmissionBatchObjectListWithMyBatches.CREATIONDATE_CONTROL, com.documentum.web.form.control.DateInput.class);
		m_fromScheduleDateControl = (DateInput)getControl(SubmissionBatchObjectListWithMyBatches.SCHEDULEDATE_CONTROL, com.documentum.web.form.control.DateInput.class);
		// m_toCreationDateControl
		m_toCreationDateControl = (DateInput)getControl(SubmissionBatchObjectListWithMyBatches.TOCREATIONDATE_CONTROL, com.documentum.web.form.control.DateInput.class);
		m_toScheduleDateControl = (DateInput)getControl(SubmissionBatchObjectListWithMyBatches.TOSCHEDULEDATE_CONTROL, com.documentum.web.form.control.DateInput.class);// NEW

		m_bulkActionFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.BULKACTION_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_bulkActionFilter.setMutable(true);

		m_reportActionFilter = (DropDownList)getControl(SubmissionBatchObjectListWithMyBatches.REPORTACTION_CONTROL_FILTER, com.documentum.web.form.control.DropDownList.class);
    	m_reportActionFilter.setMutable(true);

		initializeCommonControls();

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches- Call super.onInit()");
		super.onInit(argumentlist);
	}

	public void initializeCommonData()
	{
		m_submissionAreaName = HelperClass.getSubmissionAreaName(getDfSession());
		m_StatusList = HelperClass.getBatchStatusMappingListFromConfig(getDfSession());
	    m_hashBatchStatusLookup = HelperClass.getLookupList(getDfSession(), DBHelperClass.BATCH_TYPE, HelperClassConstants.BATCH_STATE);
        // These are the defaults for the very First time when no cookies are available
	    m_strDefaultStatusKey = HelperClassConstants.INSPECTED; // HelperClass.getDefaultBatchStatus(getDfSession());
		m_bDefaultHoldKey = false;
		m_strDefaultProviderKey = ""; // MYBATCHES;
		m_strDefaultProfileKey = "";
		m_strDefaultObjectNameKey = "";
		m_strDefaultLastActivityKey = "";
		m_listProviderUI = AppSessionContext.getProviderUI();
		m_hashRoleUserTable = AppSessionContext.getUsersAndRolesUI(); // QcHelperClass.getRolesAndUsers(getDfSession());
		// m_strProviderProfileMappingAsString = AppSessionContext.getProviderProfileMappingAsStringUI();
		// HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-initializeCommonData-m_strProviderProfileMappingAsString="+m_strProviderProfileMappingAsString);
	}

	public void initializeCommonControls()
	{
		m_statusLabel.setLabel("Status");
		m_statusFilter.setMutable(true);
		m_statusFilter.clearOptions();
		Option option = null;
/*
   		option = new Option();
   		option.setValue("");
        option.setLabel("All");
        m_statusFilter.addOption(option);
*/
		if(m_StatusList != null && m_StatusList.size() > 0)
		{
			for(int indx=0; indx < m_StatusList.size(); indx++)
			{
				ValuePair valuePair = (ValuePair)m_StatusList.get(indx);
				String key = valuePair.getKey();
				String value = valuePair.getValue();
   				option = new Option();
	            option.setValue(key);
                option.setLabel(value);
                m_statusFilter.addOption(option);
			}
		}
		m_statusFilter.setValue(getCurrentBatchStatus());
		// m_checkBoxOnHold.setValue(getCurrentHoldStatus());

        // Provider
		m_providerFilter.setMutable(true);
		m_providerFilter.clearOptions();

   		option = new Option();
	    option.setValue("");
        option.setLabel("All");
        m_providerFilter.addOption(option);

/*
   		option = new Option();
	    option.setValue(MYBATCHES);
        option.setLabel(getString(MYBATCHES));
        m_providerFilter.addOption(option);
*/
		if(m_listProviderUI != null)
		{
			ProviderUI tProvider = null;
/*	original
			for(int provIndx=0; provIndx < m_listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)m_listProviderUI.get(provIndx);
                option = new Option();
            	option.setValue("/"+m_submissionAreaName+"/"+tProvider.getProviderName());
                option.setLabel(tProvider.getProviderName());
				m_providerFilter.addOption(option);
				hashProviderNameIdMapping.put(tProvider.getProviderID(), tProvider.getProviderName());
			}
*/
            TreeMap sortedProviderList = new TreeMap();
			for(int provIndx=0; provIndx < m_listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)m_listProviderUI.get(provIndx);
				sortedProviderList.put(tProvider.getProviderName(), tProvider.getProviderID());
			}

            if(sortedProviderList != null && sortedProviderList.size() > 0)
            {
                Iterator iterator = sortedProviderList.keySet().iterator();
                while(iterator.hasNext())
                {
			    	String key = (String)iterator.next();// name
					String value = (String)sortedProviderList.get(key); // id

            		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-initializeCommonControls-providerName(key)="+key);
            		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-initializeCommonControls-providerId(value)="+value);

                    option = new Option();
            	    // option.setValue("/"+m_submissionAreaName+"/"+key);
            	    option.setValue(key);
                    option.setLabel(key);
				    m_providerFilter.addOption(option);
				    hashProviderNameIdMapping.put(value, key);
			    }
			}
		}
		String selectedProviderID = getCurrentProvider();
        m_providerFilter.setValue(selectedProviderID);

/* Start Profile filter */

/*
In the HelperClass - AppSessionContext, populate providers and profiles as,
prov=PR-1|Wiley-8.0.xml|Wiley-7.0.xml|prov=PR-2|Ams_1.0.xml|Ams_2.0.xml
Put the above string in a hidden tag in this SubmissionBatchObjectListWithMyBatches.java code in the
'initializeCommonControls'. Read this hidden tag(when onClickProvider('runAtClient="true"') in the jsp's Javascript
using the above protocol and populate the profile list for that ProviderId

See doc for any size limitation on the hidden tag value, if so, have 1 hidden tag for 1 ProviderId ?
*/

        // This method sets the hidden provider-profile Mapping in a String form, which could be used to populate the
        // profile list on the page, when a provider is selected, this is avoid a server call
        Hidden hidden = (Hidden)getControl(HIDDENPROVIDERPROFILEMAP_LABEL, com.documentum.web.form.control.Hidden.class);
        hidden.setValue(AppSessionContext.getProviderProfileMappingAsStringUI());

        populateProfileFilter(selectedProviderID, getCurrentProfile());

/* End Profile filter */

        // Performer
		m_performerFilter.setMutable(true);
		m_performerFilter.clearOptions();

    	if(m_hashRoleUserTable != null && m_hashRoleUserTable.size() > 0)
		{
            option = new Option();
            option.setValue("");
            option.setLabel(getString("allperformers"));
    	    m_performerFilter.addOption(option);

		   	TreeSet userList = (TreeSet)m_hashRoleUserTable.get(CONPREP_INSPECTOR_ROLE);
			if(userList != null && userList.size() > 0)
			{
                Iterator iterate = userList.iterator();
                while(iterate.hasNext())
                {
			        String userName = (String)iterate.next();
                    option = new Option();
                    option.setValue(userName);
                    option.setLabel(userName);
    	    	    m_performerFilter.addOption(option);
			    }
			}
		}
        m_performerFilter.setValue(getCurrentPrefPerformer());

        // BatchName
        m_objectNameText.setValue(getCurrentObjectName());

        // LastActivity
        m_lastActivityText.setValue(getCurrentLastActivity());

        // OnHold
		m_onHoldFilter.setMutable(true);
		m_onHoldFilter.clearOptions();

   		option = new Option();
	    option.setValue(ONHOLD_IGNORE);
        option.setLabel(getString("onhold_ignore"));
        m_onHoldFilter.addOption(option);

   		option = new Option();
	    option.setValue(ONHOLD_TRUE);
        option.setLabel(getString("onhold_true"));
        m_onHoldFilter.addOption(option);

   		option = new Option();
	    option.setValue(ONHOLD_FALSE);
        option.setLabel(getString("onhold_false"));
        m_onHoldFilter.addOption(option);
        m_onHoldFilter.setValue(getCurrentHoldStatus());

        Date dt = getCurrentFromCreationDate();
        if(dt != null)
        {
			m_fromCreationDateControl.setValue(dt);
		}

        dt = getCurrentFromScheduleDate();
        if(dt != null)
        {
            m_fromScheduleDateControl.setValue(dt);
	    }

	    // m_toCreationDateControl

        dt = getCurrentToCreationDate();
        if(dt != null)
        {
			m_toCreationDateControl.setValue(dt);
		}

	    // m_toScheduleDateControl - NEW

        dt = getCurrentToScheduleDate();
        if(dt != null)
        {
			m_toScheduleDateControl.setValue(dt);
		}

        m_myViewsFilter.setMutable(true);
        m_myViewsFilter.clearOptions();

   		option = new Option();
	    option.setValue(MYVIEW_IGNORE);
        option.setLabel(getString("myview_ignore"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_MY_BATCHES);
        option.setLabel(getString("myview_my_batches"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_MY_PROBLEM);
        option.setLabel(getString("myview_my_problem"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_MY_CLAIMED_PROBLEM);
        option.setLabel(getString("myview_my_claimed_problem"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_MY_ONHOLD);
        option.setLabel(getString("myview_my_onhold"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_MY_INSPECTING);
        option.setLabel(getString("myview_my_inspecting"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_ALL_BATCHES);
        option.setLabel(getString("myview_all_batches"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_ALL_ONHOLD);
        option.setLabel(getString("myview_all_onhold"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_ALL_PROCESSING);
        option.setLabel(getString("myview_all_processing"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_ALL_PROBLEM);
        option.setLabel(getString("myview_all_problem"));
        m_myViewsFilter.addOption(option);

   		option = new Option();
	    option.setValue(MYVIEW_ALL_INSPECT);
        option.setLabel(getString("myview_all_inspect"));
        m_myViewsFilter.addOption(option);

        m_myViewsFilter.setValue(getCurrentPrefMyViews());

        // Bulk action filter
		m_bulkActionFilter.setMutable(true);
		m_bulkActionFilter.clearOptions();

        for(int bulkindx=0; bulkindx < BULKACTIONS.length; bulkindx++)
        {
   	    	option = new Option();
	        option.setValue(BULKACTIONS[bulkindx]);
            option.setLabel(BULKACTIONS[bulkindx]);
            m_bulkActionFilter.addOption(option);
	    }

        // Report action filter
		m_reportActionFilter.setMutable(true);
		m_reportActionFilter.clearOptions();

        for(int reportindx=0; reportindx < REPORTACTIONS.length; reportindx++)
        {
   	    	option = new Option();
	        option.setValue(REPORTACTIONS[reportindx]);
            option.setLabel(REPORTACTIONS[reportindx]);
            m_reportActionFilter.addOption(option);
	    }

	    // This is needed here.When the browser cookies are deleted, all the cookies set during the
	    // previous session are lost. All the default fields are displayd on the page but the respective cookies
	    // are not set yet. At this point when a bulk action is clicked the values are picked from the
	    // cookies, they have to be made available here.
	    // Note: Any operation that needs to read these cookies have to be preceeded by this 'setAllCookies', otherwise
	    //       the actions(like bulkactions etc.) may pick values not shown on the display fields.
	    //       eg: Default for status, if not found is 'INSPECTED', this is set on the status drop down and queries
	    //           are fired using these drop down values, but note the cookies have to be set at this stage
	    //           otherwise, the external component(s) will assume that 'status' cookie is not found(not readable) and will
	    //           kick off a 'status' = "" implying 'all' status, which is not what the we want. Setting the cookie
	    //           here will force the status cookie to be set to 'INSPECTED'.
	    setAllCookies();
	}

    public void populateProfileFilter(String selectedProviderID, String currentProfileIdToBeSet)
    {
		m_profileFilter.setMutable(true);
		m_profileFilter.clearOptions();

   		Option option = new Option();
	    option.setValue("");
        option.setLabel("All");
        m_profileFilter.addOption(option);

        // Set this as default, but later while populating the profile(s), check if the
        // profile in cookie matches any, if so set the selectedProfileId accordingly.
        String selectedProfileId = m_strDefaultProfileKey;

        boolean isProfileIdStillAvailable = false;

        // Populate profile(s) only if selected ProviderId is NOT NULL && it is not a space ("")=== All
        if(selectedProviderID != null && !selectedProviderID.equals(""))
        {
			// populate all the profiles tied to this provider
			// Use the String representation, which would be consistent with what we do here and also in the Javascript.
            ArrayList profileIdList = getProfileIdsForProvider(selectedProviderID);
            if(profileIdList != null && profileIdList.size() > 0)
            {
				for(int profileIndx=0; profileIndx < profileIdList.size(); profileIndx++)
				{
    				String str = (String)profileIdList.get(profileIndx);
                    option = new Option();
                    option.setValue(str);
                    // Always populate the profile Id, let us not get into trouble like the providerId and providerName
                    // If needed we will change the 'Submission/importForm' too, to display the profileId directly ????
                    option.setLabel(str);
       		    	m_profileFilter.addOption(option);
       		    	if(str.equals(currentProfileIdToBeSet))
       		    	{
						isProfileIdStillAvailable = true;
					}
				}
			}
		    else
		    {
				HelperClass.porticoOutput(1, "Error in SubmissionBatchObjectListWithMyBatches-populateProfileFilter-profileIdsList is empty");
			}
		}

		// Populate an 'all' to be the top entry
		// set the current profile value from the cookies, if it is part of the available profile(s)
		// Most of the times the profileId in the cookie should be fine, but if the profileId has been deleted for some reason,
		// we must atleast set the value to the default and just before search we will anyway update/set our cookies with
		// these new selected values.
		if(isProfileIdStillAvailable == true)
		{
			selectedProfileId = currentProfileIdToBeSet;
		}
		m_profileFilter.setValue(selectedProfileId);
	}

	public String getCurrentBatchStatus()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value == null)
		{
			value = m_strDefaultStatusKey;
		}
		else
		{
			value = readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_BATCH_STATUS);
		}

		return value;
	}

	// public void setCurrentBatchStatus(String value)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_BATCH_STATUS, value);
	// }

	public String getCurrentHoldStatus()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value == null)
		{
			value = ONHOLD_IGNORE;
		}
		else
		{
			value = readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_HOLD_STATUS);
		}

		return value;
	}

	// public void setCurrentHoldStatus(String value)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        // DO NOT UNCOMMENT this, we have browser(IE) restrictions on number of Cookies that can be effectively stored
        //    For now, we will not have the 'onHold' search item in the cookie
        // ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_HOLD_STATUS, value);
	// }

    // Direct cookie
	public String getCurrentObjectName()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_OBJECTNAME);

		if(value == null)
		{
			value = m_strDefaultObjectNameKey;
		}

		return value;
	}

    // Direct cookie
	public void setCurrentObjectName(String value)
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_OBJECTNAME, value);
	}

    // Direct cookie
	public String getCurrentLastActivity()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_LASTACTIVITY);

		if(value == null)
		{
			value = m_strDefaultLastActivityKey;
		}

		return value;
	}

    // Direct cookie
	public void setCurrentLastActivity(String value)
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_LASTACTIVITY, value);
	}


    // Provider eg: AMS or MYBATCHES
	public String getCurrentProvider()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value == null)
		{
			value = m_strDefaultProviderKey;
		}
		else
		{
			value = readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER);
		}

		return value;
	}

	public String getCurrentProfile()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value == null)
		{
			value = m_strDefaultProfileKey;
		}
		else
		{
			value = readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROFILE);
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-getCurrentProfile-value="+value);

		return value;
	}

    // Note: The value would be for eg: "/Batches/AMS", ie the complete folder path
	// public void setCurrentProvider(String value)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PROVIDER, value);
	// }

	public Date getCurrentFromCreationDate()
	{
		Date dt = null;
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value != null)
		{
			// Input value = "MM/DD/YYYY"
			dt = getDateFromString(readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_CREATION_DATE), DATESEPARATOR);
		}

		return dt;
	}

	// public void setCurrentFromCreationDate(Date dateInput)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     String value = "";
    //     if(dateInput != null)
    //     {
    //         value = getStringFromDate(dateInput, DATESEPARATOR);
	// 	   }
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_CREATION_DATE, value);
	//  }

	public Date getCurrentToCreationDate()
	{
		Date dt = null;
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value != null)
		{
			// Input value = "MM/DD/YYYY"
			dt = getDateFromString(readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_CREATION_DATE), DATESEPARATOR);
		}

		return dt;
	}

	// public void setCurrentToCreationDate(Date dateInput)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     String value = "";
    //     if(dateInput != null)
    //     {
    //         value = getStringFromDate(dateInput, DATESEPARATOR);
	//     }

	// 	HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setCurrentToCreationDate()-Start-value="+value+"|for ipreferencestore.writeString");
		// ipreferencestore.writeString("ranga007", "test");
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_CREATION_DATE, value);
	// 	HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setCurrentToCreationDate()-End-value="+value+"|for ipreferencestore.writeString");
	// }

	public Date getCurrentFromScheduleDate()
	{
		Date dt = null;
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value != null)
		{
			// Input value = "MM/DD/YYYY"
			dt = getDateFromString(readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE), DATESEPARATOR);
		}

		return dt;
	}

	// public void setCurrentFromScheduleDate(Date dateInput)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     String value = "";
    //     if(dateInput != null)
    //     {
    //         value = getStringFromDate(dateInput, DATESEPARATOR);
	//     }
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE, value);
	// }

	public Date getCurrentToScheduleDate()
	{
		Date dt = null;
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value != null)
		{
			// Input value = "MM/DD/YYYY"
			dt = getDateFromString(readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE), DATESEPARATOR);
		}

		return dt;
	}

	// public void setCurrentToScheduleDate(Date dateInput)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     String value = "";
    //     if(dateInput != null)
    //     {
    //         value = getStringFromDate(dateInput, DATESEPARATOR);
	//     }
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE, value);
	// }

	public String getCurrentPrefPerformer()
	{
		// PREFERENCE_MYBATCHES_PERFORMER

        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE);

		if(value == null)
		{
			value = "";
		}
		else
		{
			value = readCombinedCookie(value, SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PERFORMER);
		}

		return value;
	}

    // Note: The value would be for eg: "/Batches/AMS", ie the complete folder path
	// public void setCurrentPrefPerformer(String value)
	// {
    //     IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
    //     ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_PERFORMER, value);
	// }

	public String getCurrentPrefMyViews()
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        String value = ipreferencestore.readString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_MYVIEWS);

		if(value == null || value.equals(""))
		{
			value = MYVIEW_IGNORE;
		}

		return value;
	}

	public void setCurrentPrefMyViews(String value)
	{
        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_MYVIEWS, value);
	}

    public void setCombinedCookie()
    {
		String tString = "";
		String combinedCookie = "";
		tString = m_statusFilter.getValue();
		if(tString == null)
		{
			tString = "";
		}
	    combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_BATCH_STATUS + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

		tString = m_onHoldFilter.getValue();
		if(tString == null)
		{
			tString = "";
		}
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_HOLD_STATUS + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

		tString = m_providerFilter.getValue();
		if(tString == null)
		{
			tString = "";
		}

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-setCombinedCookie-m_providerFilter.getValue()="+tString);

		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_PROVIDER + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

		tString = m_profileFilter.getValue();
		if(tString == null)
		{
			tString = "";
		}
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_PROFILE + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

        tString = getStringFromDate(m_fromCreationDateControl.toDate(), DATESEPARATOR);
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_FROM_CREATION_DATE + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

        tString = getStringFromDate(m_toCreationDateControl.toDate(), DATESEPARATOR);
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_TO_CREATION_DATE + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

        tString = getStringFromDate(m_fromScheduleDateControl.toDate(), DATESEPARATOR);
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

        tString = getStringFromDate(m_toScheduleDateControl.toDate(), DATESEPARATOR);
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

		tString = m_performerFilter.getValue();
		if(tString == null)
		{
			tString = "";
		}
		combinedCookie = combinedCookie + PREFERENCE_MYBATCHES_PERFORMER + KEY_VALUE_SEPARATOR + tString + COMBINED_COOKIE_SEPARATOR;

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-setCombinedCookie()-combinedCookie="+combinedCookie);

        IPreferenceStore ipreferencestore = PreferenceService.getPreferenceStore();
        ipreferencestore.writeString(SubmissionBatchObjectListWithMyBatches.PREFERENCE_MYBATCHES_COMBINED_COOKIE, combinedCookie);
	}

	public String readCombinedCookie(String combinedCookieIn, String key)
	{
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-readCombinedCookie()-combinedCookieIn="+combinedCookieIn);

		String value = "";
		if(combinedCookieIn != null)
		{
			int indx = combinedCookieIn.indexOf(key+KEY_VALUE_SEPARATOR);
			if(indx != -1)
			{
				int endIndx = combinedCookieIn.indexOf(COMBINED_COOKIE_SEPARATOR, indx);
				if(endIndx != -1)
				{
					// eg: mybStatus=HelperClassConstants.PROBLEM|
					// indx = 0, key.length()=10, endIndx = 21

					// eg: mybStatus=|
					// indx = 0, key.length()=10, endIndx = 10 => no value, discard this
					int startIndx = indx+key.length()+KEY_VALUE_SEPARATOR.length();

					if(endIndx > startIndx)
					{
						value = combinedCookieIn.substring(startIndx, endIndx);
					}
				}
			}
		}

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-readCombinedCookie()-key="+key+":"+"value="+value);

		return value;
	}

    public void onClickObject(Control control, ArgumentList argumentlist)
    {
        // HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-onClickObject()-argumentlist="+argumentlist);

        String objectType = argumentlist.get("type");
        String objectId = argumentlist.get("objectId");
        String isFolderObject = argumentlist.get("isFolder");
        boolean isCustomComponent = false;

        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onClickObject()-argumentlist=" + "type=" + objectType +
                                                      ":objectId=" + objectId +
                                                      ":isFolderObject="+isFolderObject);

        String folderPath = null;
        if(isFolderObject != null && isFolderObject.equals("1") && FolderUtil.isFolderType(objectId))
        {
		    folderPath = FolderUtil.getFolderPath(objectId, 0);
			if((folderPath != null) &&
    			(objectType.equals(DBHelperClass.BATCH_TYPE)))
    	    {
		        isCustomComponent = true;
			    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onClickObject()-setComponentJump-submission_raw_unit_objectlist-folderpath=" + folderPath);
                updateContextFromPath(folderPath);
                if(argumentlist.get("folderPath") != null)
                {
			        argumentlist.replace("folderPath", folderPath);
			    }
			    else
			    {
				    argumentlist.add("folderPath", folderPath);
			    }
                setComponentJump("submission_raw_unit_objectlist", argumentlist, getContext());
	         }
	    }
	    if(isCustomComponent == false)
	    {
			HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onClickObject()-setComponentJump-Regular-folderpath=" + folderPath);
            super.onClickObject(control, argumentlist);
	    }
	}

	protected void updateControlsFromPath(String s)
	{
		try
		{
       	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()-super updateControlsFromPath");
       	    super.updateControlsFromPath(s);
       	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()-Before buildVisibleAttributeList");

            // In some cases we would call 'updateControlsFromPath' and would need the error message also to be dispalyed
            //    so, let the respective calling method decide if error messages are to be cleared
       	    //MessageService.clear(this);

            Datagrid datagrid = (Datagrid)getControl(SubmissionBatchObjectListWithMyBatches.MYBATCHES_DATAGRID_CONTROL, com.documentum.web.form.control.databound.Datagrid.class);
       	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()-After buildVisibleAttributeList");
            m_queryString = BaseDocbaseQueryServiceWithMyBatches.baseBuildObjectListQuery(m_submissionAreaName,
				                                                                            getLoggedInUser(),
						                                                                    buildVisibleAttributeList(),
						                                                                    m_providerFilter.getValue(),
						                                                                    m_profileFilter.getValue(),
						                                                                    m_performerFilter.getValue(),
				                                                                            m_objectNameText.getValue().trim(),
				                                                                            m_lastActivityText.getValue().trim(),
				                                                                            m_statusFilter.getValue(),
                                                                                            m_onHoldFilter.getValue(),
                                                                                            getStringFromDate(m_fromCreationDateControl.toDate(), DATESEPARATOR),
                                                                                            getStringFromDate(m_fromScheduleDateControl.toDate(), DATESEPARATOR),
                                                                                            getStringFromDate(m_toScheduleDateControl.toDate(), DATESEPARATOR),
                                                                                            getStringFromDate(m_toCreationDateControl.toDate(), DATESEPARATOR));
      	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()-query="+m_queryString);
            datagrid.getDataProvider().setQuery(m_queryString);
            datagrid.getDataProvider().refresh();
       	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()-End query");
        }
        catch(Exception e)
        {
			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches(New)-updateCurrentDataGridControls()=" + e.toString());
		}
		finally
		{
		}
	}

/* This method is run at the client side javascript, to avoid sending a server call and refreshing the whole window
	public void onClickClearSearch(Control control,ArgumentList args)
	{
		// Reset the search fields

		m_statusFilter.setValue("");
		m_onHoldFilter.setValue(ONHOLD_IGNORE);
		m_objectNameText.setValue("");
		m_lastActivityText.setValue("");
    	m_fromCreationDateControl.clear();
		m_fromScheduleDateControl.clear();
		m_providerFilter.setValue("");
		m_performerFilter.setValue("");
    }
*/

	public void onClickSearch(Control control,ArgumentList args)
	{
        // When a Search is initiated by clicking the 'Search' button,
        // reset(MYVIEW_IGNORE) the MyViews Drop Down(It may no longer be relevant to the Search Criteria Entered)
        // This has to be set in the cookies, the next we come here, it should be set to 'MYVIEW_IGNORE'


        // HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-m_fromCreationDateControl.getFocusId(),getElementName="+m_fromCreationDateControl.getFocusId()+","+m_fromCreationDateControl.getElementName());
        // HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-m_fromScheduleDateControl.getFocusId(),getElementName="+m_fromScheduleDateControl.getFocusId()+","+m_fromScheduleDateControl.getElementName());

        m_myViewsFilter.setValue(MYVIEW_IGNORE);
        setCurrentPrefMyViews(m_myViewsFilter.getValue());

        processSearch();
    }

	public void processSearch()
	{
		if(m_bSetFireSearchRequest == true)
		{
			// Keep the 'Server side profileId(s) in SYNC with Client side profileId(s) based on the provider and the selected profile
// RANGA CHECK THIS ????
			// updateStateFromRequest();

            // Pick the Controls value and update the server side ProfileId(s) options
			populateProfileFilter(m_providerFilter.getValue(), m_profileFilter.getValue());

			if(validateInputData() == true)
			{
			    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onClickSearch");
			    // Set these to update the cookies

        	    // setCurrentBatchStatus(m_statusFilter.getValue());
    		    // setCurrentHoldStatus(m_onHoldFilter.getValue());
                // setCurrentProvider(m_providerFilter.getValue());
                // setCurrentFromCreationDate(m_fromCreationDateControl.toDate());
                // setCurrentToCreationDate(m_toCreationDateControl.toDate());
                // setCurrentFromScheduleDate(m_fromScheduleDateControl.toDate());
                // setCurrentToScheduleDate(m_toScheduleDateControl.toDate());
                // setCurrentPrefPerformer(m_performerFilter.getValue());

                // This call will combine all the above cookies
                setAllCookies();

			    // m_bSetFireSearchRequest = false;
	    	    updateControlsFromPath(null);
	    	    // New moved here from 'updateControlsFromPath' method
	    	    MessageService.clear(this);
	    	    // super.onRender();
		    }
	    }
	}

	public void setAllCookies()
	{
   	    setCombinedCookie();
	    setCurrentObjectName(m_objectNameText.getValue().trim());
	    setCurrentLastActivity(m_lastActivityText.getValue().trim());
	}

	public void onSelectMyViewsFilter(Control control,ArgumentList args)
	{
		boolean doSearch = true;

        // Element name is = "SubmissionBatchObjectListWithMyBatches_myviews_control_0", which can be used in
        //                 jsp javascript as document.getElementById("SubmissionBatchObjectListWithMyBatches_myviews_control_0")
   	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onSelectMyViewsFilter() element name="+m_myViewsFilter.getElementName());

		String value = m_myViewsFilter.getValue();
		setCurrentPrefMyViews(value);

   	    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-onSelectMyViewsFilter() Selected Value="+value);

		if(value.equals(MYVIEW_ALL_BATCHES))
		{
			m_statusFilter.setValue("");
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue("");
		}
		else if(value.equals(MYVIEW_MY_BATCHES))
		{
			m_statusFilter.setValue("");
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue(getLoggedInUser());
		}
		else if(value.equals(MYVIEW_ALL_ONHOLD))
		{
			m_statusFilter.setValue("");
			m_onHoldFilter.setValue(ONHOLD_TRUE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue("");
		}
		else if(value.equals(MYVIEW_MY_ONHOLD))
		{
			m_statusFilter.setValue("");
			m_onHoldFilter.setValue(ONHOLD_TRUE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue(getLoggedInUser());
		}
		else if(value.equals(MYVIEW_ALL_PROBLEM))
		{
			m_statusFilter.setValue(HelperClassConstants.PROBLEM+","+HelperClassConstants.SYSTEM_ERROR);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue("");
		}
		else if(value.equals(MYVIEW_MY_PROBLEM))
		{
			m_statusFilter.setValue(HelperClassConstants.PROBLEM+","+HelperClassConstants.SYSTEM_ERROR);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue(getLoggedInUser());
		}
		else if(value.equals(MYVIEW_MY_CLAIMED_PROBLEM))
		{
			m_statusFilter.setValue(HelperClassConstants.RESOLVING_PROBLEM);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue(getLoggedInUser());
		}
		else if(value.equals(MYVIEW_ALL_PROCESSING))
		{
			m_statusFilter.setValue(HelperClassConstants.AUTO_PROCESSING);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue("");
		}
		else if(value.equals(MYVIEW_ALL_INSPECT))
		{
			m_statusFilter.setValue(HelperClassConstants.INSPECT);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue("");
		}
		else if(value.equals(MYVIEW_MY_INSPECTING))
		{
			m_statusFilter.setValue(HelperClassConstants.INSPECTING);
			m_onHoldFilter.setValue(ONHOLD_IGNORE);
			m_objectNameText.setValue("");
			m_lastActivityText.setValue("");
			m_fromCreationDateControl.clear();
			m_toCreationDateControl.clear();
			m_fromScheduleDateControl.clear();
			m_toScheduleDateControl.clear();
			m_providerFilter.setValue("");
			m_profileFilter.setValue("");
			m_performerFilter.setValue(getLoggedInUser());
		}
		else if(value.equals(MYVIEW_IGNORE))
		{
			// Ignore, do not do anything.
		}
		else
		{
			doSearch = false;
			HelperClass.porticoOutput(1, "SubmissionBatchObjectListWithMyBatches-onSelectMyViewsFilter-Unknown selection-value="+value);
		}

		if(doSearch == true)
		{
	    	processSearch();
	    }
	}

	public boolean validateInputData()
	{
		boolean isValid = true;
		String errorMsg = "";

        // Note: isUndefinedDate == true, if no date value was entered, this is the Default
        //       isUndefinedDate == false, if a Date was Entered(may be invalid too) or Picked from the date selector

        // Check if a Date was 'Entered'(isUndefinedDate == false) and it was 'Invalid'(isValidDate == false)
        if(m_fromCreationDateControl.isUndefinedDate() == false &&
               m_fromCreationDateControl.isValidDate() == false)
        {
			isValid = false;
		}

        if(m_toCreationDateControl.isUndefinedDate() == false &&
               m_toCreationDateControl.isValidDate() == false)
        {
			isValid = false;
		}

        if(m_fromScheduleDateControl.isUndefinedDate() == false &&
               m_fromScheduleDateControl.isValidDate() == false)
        {
			isValid = false;
		}


        if(m_toScheduleDateControl.isUndefinedDate() == false &&
               m_toScheduleDateControl.isValidDate() == false)
        {
			isValid = false;
		}

		try
		{
			if(isValid == true)
			{
				if(m_fromScheduleDateControl.isUndefinedDate() == false &&
				      m_fromScheduleDateControl.isValidDate() == true &&
				      m_toScheduleDateControl.isUndefinedDate() == false &&
				      m_toScheduleDateControl.isValidDate() == true)
				{
					// If value of FromDate > ToDate - Error out
		    	    if(m_fromScheduleDateControl.toDate().getTime() > m_toScheduleDateControl.toDate().getTime())
		    	    {
						isValid = false;
					}
			    }
				if(m_fromCreationDateControl.isUndefinedDate() == false &&
				      m_fromCreationDateControl.isValidDate() == true &&
				      m_toCreationDateControl.isUndefinedDate() == false &&
				      m_toCreationDateControl.isValidDate() == true)
				{
					// If value of FromDate > ToDate - Error out
		    	    if(m_fromCreationDateControl.toDate().getTime() > m_toCreationDateControl.toDate().getTime())
		    	    {
						isValid = false;
					}
			    }
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "SubmissionBatchObjectListWithMyBatches-validateInputData-(compare)-Exception="+e.toString());
		}
		finally
		{
		}


		if(isValid == false)
		{
			ErrorMessageService.getService().setNonFatalError(this, "MSG_INVALID_CREATE_SCHEDULE_DATES", null);
		}

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-validateInputData()-isUndefinedCreationDate="+m_fromCreationDateControl.isUndefinedDate()+",isValidCreationDate="+m_fromCreationDateControl.isValidDate()+",date="+m_fromCreationDateControl.toDate());
		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-validateInputData()-isUndefinedScheduleDate="+m_fromScheduleDateControl.isUndefinedDate()+",isValidScheduleDate="+m_fromScheduleDateControl.isValidDate()+",date="+m_fromScheduleDateControl.toDate());

		return isValid;
	}

	public String getBatchScheduleTime(String batchObjectId)
	{
		String batchScheduleTime = null;
		try
		{
            String qualification = "p_batch WHERE r_object_id ="+  "'" + batchObjectId + "'";
	        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-getBatchScheduleTime-qualification="+qualification);
            IDfPersistentObject batchObject = (IDfPersistentObject) getDfSession().getObjectByQualification(qualification);
            if(batchObject != null)
            {
		    	batchScheduleTime = batchObject.getString("p_sched_timestamp");
		    	if(batchScheduleTime.equals(DfTime.DF_NULLDATE_STR))
		    	{
					batchScheduleTime = null;
				}
		    }
	    }
	    catch(Exception e)
	    {
			HelperClass.porticoOutput(1, "SubmissionBatchObjectListWithMyBatches-getBatchScheduleTime-Exception="+e.toString());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-getBatchScheduleTime-batchScheduleTime="+batchScheduleTime);

		return batchScheduleTime;
    }

    public Hashtable getHashData()
    {
		return m_hashData;
	}

    public Hashtable getBatchStatusLookUp()
    {
		return m_hashBatchStatusLookup;
	}

	public String getLoggedInUser()
	{
		String str = "";

		try
		{
			str = getDfSession().getLoginUserName();
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "SubmissionBatchObjectListWithMyBatches-getLoggedInUser-Exception="+e.toString());
		}
		finally
		{
		}

		return str;
	}


    // Delayed firing from the Page, not from the Component because on the Component the call to
    //         updateControlsFromPath happens about 3 times before the page is displayed
    public void fireQueryForHash()
    {
        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-fireQueryForHash()-Start Query for Hash");
        IDfCollection tIDfCollection = null;

        try
		{
    		if(m_hashData != null)
    		{
    			m_hashData.clear();
    		}

    		if(m_BatchWorkFlowId != null)
    		{
    			m_BatchWorkFlowId.clear();
    		}

    		if(m_queryString != null && !m_queryString.equals(""))
    		{
                DfQuery dfquery = new DfQuery();
                HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-fireQueryForHash()-Start Regular Query for Hash");
         		dfquery.setDQL(m_queryString);
                HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-fireQueryForHash()-End Regular Query for Hash");
                tIDfCollection = dfquery.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    String objectid = "";
                    String objectname = "";
                    String objecttype = "";
                    String objectstatus = "";
                    String objectcreationdate = "";
                    String objectonhold = "";
                    String objectrawunitcount = "";
                    //String objectarticlecount = "";
                    String performerfordisplay = "";
                    String schedtimestamp = "";
                    String lastactivity = "";
                    String problemstatecount = "";
                    String performer = "";
                    String useractiontaken = "";
                    String reentryactivity = "";
                    String batchWorkFlowId = "";
                    String currentProviderId = "";
                    String currentProfileId = "";
                    while(tIDfCollection.next())
                    {
    					objectid = tIDfCollection.getString("r_object_id");
                        Hashtable attrHash = new Hashtable();
                        objectname = tIDfCollection.getString(OBJECT_NAME);
                        if(objectname != null)
                        {
    						attrHash.put(OBJECT_NAME, objectname);
    					}

                        objecttype = tIDfCollection.getString(R_OBJECT_TYPE);
                        if(objecttype != null)
                        {
    						attrHash.put(R_OBJECT_TYPE, objecttype);
    					}
                        objectstatus = tIDfCollection.getString(P_STATE);
                        if(objectstatus != null)
                        {
    						attrHash.put(P_STATE, objectstatus);
    					}
                        objectcreationdate = tIDfCollection.getString(R_CREATION_DATE);
                        if(objectcreationdate != null)
                        {
    						attrHash.put(R_CREATION_DATE, objectcreationdate);
    					}

                        objectonhold = tIDfCollection.getString(P_ON_HOLD);
                        if(objectonhold != null)
                        {
    						attrHash.put(P_ON_HOLD, objectonhold);
    					}
                        objectrawunitcount = tIDfCollection.getString(P_RAWUNIT_COUNT);
                        if(objectrawunitcount != null)
                        {
    						attrHash.put(P_RAWUNIT_COUNT, objectrawunitcount);
    					}
                        //objectarticlecount = tIDfCollection.getString(P_ARTICLE_COUNT);
                        //if(objectarticlecount != null)
                        //{
    				//		attrHash.put(P_ARTICLE_COUNT, objectarticlecount);
    				//	}
                        performerfordisplay = tIDfCollection.getString(P_PERFORMER_FOR_DISPLAY);
                        if(performerfordisplay != null)
                        {
    						attrHash.put(P_PERFORMER_FOR_DISPLAY, performerfordisplay);
    					}
                        schedtimestamp = tIDfCollection.getString(P_SCHED_TIMESTAMP);
                        if(schedtimestamp != null && !schedtimestamp.equals(DfTime.DF_NULLDATE_STR))
                        {
    						attrHash.put(P_SCHED_TIMESTAMP, schedtimestamp);
    					}

                        lastactivity = tIDfCollection.getString(P_LAST_ACTIVITY);
                        if(lastactivity != null)
                        {
    						attrHash.put(P_LAST_ACTIVITY, lastactivity);
    					}
                        problemstatecount = tIDfCollection.getString(P_PROBLEM_STATE_COUNT);
                        if(problemstatecount != null)
                        {
    						attrHash.put(P_PROBLEM_STATE_COUNT, problemstatecount);
    					}
                        performer = tIDfCollection.getString(P_PERFORMER);
                        if(performer != null)
                        {
    						attrHash.put(P_PERFORMER, performer);
    					}
                        useractiontaken = tIDfCollection.getString(P_USER_ACTION_TAKEN);
                        if(useractiontaken != null)
                        {
    						attrHash.put(P_USER_ACTION_TAKEN, useractiontaken);
    					}
                        reentryactivity = tIDfCollection.getString(P_REENTRY_ACTIVITY);
                        if(reentryactivity != null)
                        {
    						attrHash.put(P_REENTRY_ACTIVITY, reentryactivity);
    					}
                        batchWorkFlowId = tIDfCollection.getString(P_BATCH_WORKFLOW_ID);
                        if(batchWorkFlowId != null)
                        {
    						attrHash.put(P_BATCH_WORKFLOW_ID, batchWorkFlowId);
    					}
    					currentProviderId = tIDfCollection.getString(P_PROVIDER_ID);
                        if(currentProviderId != null)
                        {
/*
							if(hashProviderNameIdMapping.containsKey(currentProviderId))
							{
								// Name equivalent
								currentProviderId = (String)hashProviderNameIdMapping.get(currentProviderId);
							}
*/
                            // Directly use the ProviderId in the place of ProviderName
    						attrHash.put(P_PROVIDER_ID, currentProviderId);
    					}
    					currentProfileId = tIDfCollection.getString(P_PROFILE_ID);
                        if(currentProfileId != null)
                        {
                            // Directly use the ProfileId in the place of ProfileName
    						attrHash.put(P_PROFILE_ID, currentProfileId);
    					}
    					HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-fireQueryForHash-values="+"objectid="+objectid+",currentProviderId="+currentProviderId+",objectname="+objectname+",lastactivity="+lastactivity+",objectstatus="+objectstatus);
    					m_hashData.put(objectid, attrHash);
    				}

     	        	tIDfCollection.close();
     	    	}
		    }
		}
		catch(Exception e)
		{
	        HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-fireQueryForHash="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
			try
			{
                if(tIDfCollection != null)
                {
			    	tIDfCollection.close();
			    }
           		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-fireQueryForHash CLOSE IDfCollection");
		    }
		    catch(Exception e)
		    {
				HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-fireQueryForHash-close" + e.toString());
			}
            HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-fireQueryForHash-Call-finally");
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-fireQueryForHash()-End Query for Hash");
	}

	// Input value = "MM/DD/YYYY"
	public static Date getDateFromString(String value, String separator)
	{
		Date dt = null;
		try
		{
			if(value != null && !value.equals(""))
			{
			    Calendar cal = Calendar.getInstance();
			    cal.clear();
			    String month = null;
			    String day = null;
			    String year = null;
			    StringTokenizer strTok = new StringTokenizer(value, separator);
                while (strTok.hasMoreTokens())
                {
			    	if(month == null)
			    	{
			    		month = strTok.nextToken();
			    	}
			    	else if(day == null)
			    	{
			    		day = strTok.nextToken();
			     	}
			    	else if(year == null)
			    	{
			    		year = strTok.nextToken();
			    	}
                }
                cal.set(Integer.parseInt(year),
			              Integer.parseInt(month)-1, // JAN == 0
                          Integer.parseInt(day));
                dt = cal.getTime();
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-getDateFromString-value="+value+","
			                                                                                                   +"separator="+separator+",exception="+e.toString());
		}
		finally
		{
		}

		if(dt != null)
		{
            HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-getDateFromString()-dt.toString()="+dt.toString());
		}

		return dt;
	}

    // Output value = "MM/DD/YYYY"
	public static String getStringFromDate(Date value, String separator)
	{
		String dtStr = "";
		try
		{
			if(value != null)
			{
			    Calendar cal = Calendar.getInstance();
			    cal.clear();
			    cal.setTime(value);
			    int month = cal.get(Calendar.MONTH)+1; // // JAN == 0
			    String monthStr = ""+month;
			    if(month >= 1 && month <= 9)
			    {
			    	monthStr = "0"+monthStr; // Safer to have the "MM" 2 digit string
			    }
			    int day = cal.get(Calendar.DATE);
			    String dayStr = ""+day;
			    if(day >= 0 && day <=9)
			    {
			    	dayStr = "0"+dayStr; // Safer to have the "DD" 2 digit string
			    }
                dtStr = ""+monthStr
                          +separator
                          +cal.get(Calendar.DATE)
                          +separator
                          +cal.get(Calendar.YEAR);
		    }
		}
		catch(Exception e)
		{
			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-getDateFromString-value="+value+","
			                                                                                                   +"separator="+separator+",exception="+e.toString());
		}
		finally
		{
		}

        HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-getStringFromDate()-dtStr="+dtStr);

        return dtStr;
	}

	public void setQueriedBatchesOnHold(Control control,ArgumentList args)
	{
		boolean isValid = true;
		ArrayList errorObjectList = new ArrayList();

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-Started");

		try
		{
			if(m_hashData != null && m_hashData.size() > 0)
			{
                for (Enumeration e = m_hashData.keys() ; e.hasMoreElements() ;)
                {
                    String currentObjectId = (String)e.nextElement();
                    String objectName = "";
                    Hashtable currentAttrHash = (Hashtable)m_hashData.get(currentObjectId);
            		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-currentObjectId="+currentObjectId);
                    if(currentAttrHash != null)
                    {
                        if(currentAttrHash.containsKey(OBJECT_NAME))
                        {
							objectName = (String)currentAttrHash.get(OBJECT_NAME);
						}
                        try
                        {
            		    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(P_ON_HOLD));
                            if(currentAttrHash.containsKey(P_ON_HOLD) == true &&
                                     ((String)currentAttrHash.get(P_ON_HOLD)).equals("0"))
                            {
            		    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-currentObjectId(yes p_onhold is 0)="+currentObjectId);
                                Hashtable addlnInfo = new Hashtable();
                                addlnInfo.put(HelperClassConstants.ISBATCHONHOLD, "false");
                                addlnInfo.put(HelperClassConstants.BATCHSTATUS, (String)currentAttrHash.get(P_STATE));
                                if(QcHelperClass.isValidSetHoldAction(getDfSession(), currentObjectId, addlnInfo) == true)
                                {
            		    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-currentObjectId(isValidSetHoldAction is true)="+currentObjectId);
                                    IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(currentObjectId));
        						    iDfSysObject.setBoolean("p_on_hold", true);
        						    iDfSysObject.save();
        						}
        					}
    				    }
    				    catch(Exception ein)
    				    {
    						isValid = false;
    						errorObjectList.add(objectName);
                			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOnHold-objectName="+objectName+","+"exception="+ein.toString());
    						ein.printStackTrace();
    					}
				    }
				}

				if(isValid == false)
				{
					Object[] objArray = null;
					if(errorObjectList != null && errorObjectList.size() > 0)
					{
					    objArray = errorObjectList.toArray();
					}
        			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOnHold, for the objectname(s)"+errorObjectList.toString());
			        ErrorMessageService.getService().setNonFatalError(this, "MSG_FAILED_BULK_SETHOLD_ACTION_PARTIAL", objArray, null);
				}
				else
				{
					MessageService.addMessage(this, "MSG_BULK_SET_HOLD_ACTION_SUCCESS");
				}

				// Refresh this page after this action
				updateControlsFromPath(null);
			}
		}
		catch(Exception e)
		{
   			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOnHold-"+"exception="+e.toString());
			ErrorMessageService.getService().setNonFatalError(this, "MSG_FAILED_BULK_SETHOLD_ACTION",e);
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOnHold()-finally");
		}
	}

	public void setQueriedBatchesOffHold(Control control,ArgumentList args)
	{
		boolean isValid = true;
		ArrayList errorObjectList = new ArrayList();

		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOffHold()-Started");

		try
		{
			if(m_hashData != null && m_hashData.size() > 0)
			{
                for (Enumeration e = m_hashData.keys() ; e.hasMoreElements() ;)
                {
                    String currentObjectId = (String)e.nextElement();
                    String objectName = "";
                    Hashtable currentAttrHash = (Hashtable)m_hashData.get(currentObjectId);
            		HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOffHold()-currentObjectId="+currentObjectId);
                    if(currentAttrHash != null)
                    {
                        if(currentAttrHash.containsKey(OBJECT_NAME))
                        {
							objectName = (String)currentAttrHash.get(OBJECT_NAME);
						}
                        try
                        {
            		    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOffHold()-currentObjectId((String)currentAttrHash.get(P_ON_HOLD))="+(String)currentAttrHash.get(P_ON_HOLD));
                            if(currentAttrHash.containsKey(P_ON_HOLD) == true &&
                                     ((String)currentAttrHash.get(P_ON_HOLD)).equals("1"))
                            {
                    		    HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOffHold()-currentObjectId(yes p_onhold is 1)="+currentObjectId);
                                IDfSysObject iDfSysObject = (IDfSysObject)getDfSession().getObject(new DfId(currentObjectId));
       						    iDfSysObject.setBoolean("p_on_hold", false);
       						    iDfSysObject.save();
        					}
    				    }
    				    catch(Exception ein)
    				    {
    						isValid = false;
    						errorObjectList.add(objectName);
                			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOffHold-objectName="+objectName+","+"exception="+ein.toString());
    						ein.printStackTrace();
    					}
				    }
				}

				if(isValid == false)
				{
					Object[] objArray = null;
					if(errorObjectList != null && errorObjectList.size() > 0)
					{
					    objArray = errorObjectList.toArray();
					}
        			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOffHold, for the objectname(s)"+errorObjectList.toString());
			        ErrorMessageService.getService().setNonFatalError(this, "MSG_FAILED_BULK_SETOFFHOLD_ACTION_PARTIAL", objArray, null);
				}
				else
				{
					MessageService.addMessage(this, "MSG_BULK_SET_OFFHOLD_ACTION_SUCCESS");
				}

				// Refresh this page after this action
				updateControlsFromPath(null);
			}
		}
		catch(Exception e)
		{
   			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-setQueriedBatchesOffHold-"+"exception="+e.toString());
			ErrorMessageService.getService().setNonFatalError(this, "MSG_FAILED_BULK_SETOFFHOLD_ACTION",e);
			e.printStackTrace();
		}
		finally
		{
			HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches(New)-setQueriedBatchesOffHold()-finally");
		}
	}

	public static ArrayList getProfileIdsForProvider(String providerId)
	{
		ArrayList profileIdList = new ArrayList();
		try
		{
			String providerProfileMappingAsString = AppSessionContext.getProviderProfileMappingAsStringUI();
            int startCurrentProviderIndex = providerProfileMappingAsString.indexOf(HelperClass.g_hiddenProviderTag+providerId+HelperClass.g_hiddenProviderTagSeparator);
            if(startCurrentProviderIndex != -1)
            {
               // Look for prov=PR-1|Wiley-8.0.xml|Wiley-7.0.xml|prov=PR-2|Ams_1.0.xml|Ams_2.0.xml
               //                                                *
                int endCurrentProviderIndex = providerProfileMappingAsString.indexOf(HelperClass.g_hiddenProviderTag, startCurrentProviderIndex+HelperClass.g_hiddenProviderTag.length());
                String currentProviderProfileLookupString = "";
                if(endCurrentProviderIndex != -1)
                {
                    currentProviderProfileLookupString = providerProfileMappingAsString.substring(startCurrentProviderIndex, endCurrentProviderIndex);
				}
				else
				{
					currentProviderProfileLookupString = providerProfileMappingAsString.substring(startCurrentProviderIndex);
				}
                HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-getProfileIdsForProvider-selectedProviderID="+providerId);
                HelperClass.porticoOutput(0, "SubmissionBatchObjectListWithMyBatches-getProfileIdsForProvider-currentProviderProfileLookupString="+currentProviderProfileLookupString);

                if(currentProviderProfileLookupString != null && !currentProviderProfileLookupString.equals(""))
                {
					StringTokenizer strtok = new StringTokenizer(currentProviderProfileLookupString, HelperClass.g_hiddenProviderTagSeparator);
                    while(strtok.hasMoreTokens())
                    {
						String str = strtok.nextToken();
						if(str.startsWith(HelperClass.g_hiddenProviderTag))
						{
							// Ignore this is the providerId part
						}
						else
						{
							// These are the profileId(s)
                            // Always populate the profile Id, let us not get into trouble like the providerId and providerName
                            // If needed we will change the 'Submission/importForm' too, to display the profileId directly ????
							profileIdList.add(str);
						}
					}
				}
		    }
        }
        catch(Exception e)
        {
   			HelperClass.porticoOutput(1, "Exception in SubmissionBatchObjectListWithMyBatches-getProfileIdsForProvider-"+"exception="+e.toString());
			e.printStackTrace();
		}
		finally
		{
		}

		return profileIdList;
    }

/*
    public void populateProviderOption()
    {
		m_providerList.setMutable(true);
		m_providerList.clearOptions();

		if(listProviderUI != null)
		{
			Option option = null;
			ProviderUI tProvider = null;
			for(int provIndx=0; provIndx < listProviderUI.size(); provIndx++)
			{
				tProvider = (ProviderUI)listProviderUI.get(provIndx);
				String currentProviderID = tProvider.getProviderID();
                option = new Option();
            	option.setValue(currentProviderID);
                option.setLabel(tProvider.getProviderName());
				m_providerList.addOption(option);
			}
		}
	}
*/
/*
	public void onSelectProviderFilter(Control control,ArgumentList args)
	{
		if(m_bSetFireNodeSelectedClientEvent == false)
		{
			fireNodeSelectedClientEvent(m_providerFilter.getValue());
        }
	}

    protected void fireNodeSelectedClientEvent(String s)
    {
		if(s != null)
		{
			s = BATCH_CABINET_FOLDER + "/" + s;
            ArgumentList argumentlist = new ArgumentList();
            argumentlist.add("id", s);
            setClientEvent("onNodeSelected", argumentlist);
            m_bSetFireNodeSelectedClientEvent = true;
	    }
    }

*/

/*
Extra query to get the workflow and workflow runtime status
SELECT p.r_object_id, p.r_workflow_id, w.r_runtime_state FROM dmi_package p, dm_workflow w where p.r_workflow_id=w.r_object_id
*/

// Filter values
    private ArrayList m_StatusList;
    private String m_strDefaultStatusKey;
    private boolean m_bDefaultHoldKey = false;
	private String m_strDefaultProviderKey;
	private String m_strDefaultProfileKey;
	private String m_strDefaultObjectNameKey;
	private String m_strDefaultLastActivityKey;

    private Label m_statusLabel;
    private DropDownList m_statusFilter;

    private DropDownList m_onHoldFilter;

    private DropDownList m_myViewsFilter;

    // private Label m_objectnameLabel;
	private Text m_objectNameText;

    // private Label m_lastactivityLabel;
	private Text m_lastActivityText;

    // private Label m_providerLabel;
    private DropDownList m_providerFilter;
    private DropDownList m_profileFilter;
    private DropDownList m_performerFilter;

    private DateInput m_fromCreationDateControl;
    private DateInput m_fromScheduleDateControl;

    private DateInput m_toCreationDateControl;
    private DateInput m_toScheduleDateControl; // NEW

    // private DataProvider dataProvider;

    private Hashtable m_hashData;
    private String m_queryString;
    private Hashtable m_BatchWorkFlowId;
    private Hashtable m_hashBatchStatusLookup;
    private Hashtable m_hashRoleUserTable;

    private boolean m_bSetFireSearchRequest = true;

    private ArrayList m_listProviderUI;

    private Hashtable hashProviderNameIdMapping = new Hashtable();

    private String m_submissionAreaName = "";

    public static final String CONPREP_INSPECTOR_ROLE = "conprep_inspector_role";

    public static final String MYBATCHES_DATAGRID_CONTROL = "doclist_grid";
    public static final String STATUS_CONTROL_FILTER = "status_filter";
    public static final String STATUS_LABEL = "Status";
    public static final String ONHOLD_CONTROL_FILTER = "onhold_filter";
    public static final String OBJECTNAME_CONTROL_FILTER = "objectname_filter";
    public static final String LASTACTIVITY_CONTROL_FILTER = "lastactivity_filter";
    public static final String PROVIDER_CONTROL_FILTER = "provider_filter";
    public static final String PROFILE_CONTROL_FILTER = "profile_filter";
    public static final String PERFORMER_CONTROL_FILTER = "performer_filter";
    public static final String CREATIONDATE_CONTROL = "createdate_control";
    public static final String TOCREATIONDATE_CONTROL = "tocreatedate_cntrl";
    public static final String SCHEDULEDATE_CONTROL = "scheduledate_control";
    public static final String TOSCHEDULEDATE_CONTROL = "toscheddate_cntrl"; // NEW
    public static final String MYVIEWS_CONTROL_FILTER = "myviews_control";

    // NLS based
    public static final String ONHOLD_LABEL = "OnHold";
    public static final String OBJECTNAME_LABEL = "objectnamelabel"; // "Batch Name";
    public static final String LASTACTIVITY_LABEL = "lastactivitylabel"; // "Last Activity";
    public static final String PROVIDER_LABEL = "providerlabel"; // "MyBatches/Provider";
    public static final String PROFILE_LABEL = "profilelabel";
    public static final String SEARCH_LABEL = "searchlabel";
    public static final String PERFORMER_LABEL = "performer";
    // From == CREATE
    public static final String CREATEDATE_LABEL = "createdatelabel";
    // To == TOCREATE
    // public static final String TOCREATEDATE_LABEL = "tocreatedatelabel";
    public static final String SCHEDULEDATE_LABEL = "scheduledatelabel";
    public static final String TOSCHEDULEDATE_LABEL = "toscheddatelabel"; // NEW
    public static final String CLEAR_LABEL = "clearlabel";

    public static final String MYBATCHES = "MyBatches";
    public static final String DATESEPARATOR = "/";
    public static final String ONHOLD_IGNORE = "";
    public static final String ONHOLD_TRUE = "true";
    public static final String ONHOLD_FALSE = "false";

    public static final String MYVIEW_ALL_BATCHES = "myview_all_batches";
    public static final String MYVIEW_MY_BATCHES = "myview_my_batches";

    public static final String MYVIEW_ALL_ONHOLD = "myview_all_onhold";
    public static final String MYVIEW_MY_ONHOLD = "myview_my_onhold";

    public static final String MYVIEW_ALL_PROBLEM = "myview_all_problem";
    public static final String MYVIEW_MY_PROBLEM = "myview_my_problem";

    public static final String MYVIEW_MY_CLAIMED_PROBLEM = "myview_my_claimed_problem";

    public static final String MYVIEW_ALL_PROCESSING = "myview_all_processing";

    public static final String MYVIEW_ALL_INSPECT = "myview_all_inspect";
    public static final String MYVIEW_MY_INSPECTING = "myview_my_inspecting";

    public static final String MYVIEW_IGNORE = "myview_ignore";


    // Attributes to be in Hash for better performance

/*
    query=select 1,upper(object_name),r_object_id as sortbyobjid, r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,p_state,r_creation_date,p_on_hold,p_rawunit_count,p_article_count,p_performer_for_display,p_sched_timestamp,p_last_activity,p_problem_state_count,p_performer,p_user_action_taken,p_reentry_activity,'1' as isfolder from p_batch where a_is_hidden=false and any i_folder_id='0b0152d480009337' order by r_creation_date desc
*/
    public static final String R_OBJECT_ID = "r_object_id";
    public static final String OBJECT_NAME = "object_name";
    public static final String LAST_ACTIVITY = "p_last_activity";
    public static final String R_OBJECT_TYPE = "r_object_type";
    public static final String P_STATE = "p_state";
    public static final String R_CREATION_DATE = "r_creation_date";
    public static final String P_ON_HOLD = "p_on_hold";
    public static final String P_RAWUNIT_COUNT = "p_rawunit_count";
    public static final String P_ARTICLE_COUNT = "p_article_count";
    public static final String P_PERFORMER_FOR_DISPLAY = "p_performer_for_display";
    public static final String P_SCHED_TIMESTAMP = "p_sched_timestamp";
    public static final String P_LAST_ACTIVITY = "p_last_activity";
    public static final String P_PROBLEM_STATE_COUNT = "p_problem_state_count";
    public static final String P_PERFORMER = "p_performer";
    public static final String P_USER_ACTION_TAKEN = "p_user_action_taken";
    public static final String P_REENTRY_ACTIVITY = "p_reentry_activity";
    public static final String P_BATCH_WORKFLOW_ID = "p_workflow_id";
    public static final String P_PROVIDER_ID = "p_provider_id";
    public static final String P_PROFILE_ID = "p_profile_id";

    public static final String KEY_VALUE_SEPARATOR = "=";
    public static final String COMBINED_COOKIE_SEPARATOR = "|";

    // Cookies-Direct
    public static final String PREFERENCE_MYBATCHES_MYVIEWS = "mybMyViews";
    public static final String PREFERENCE_MYBATCHES_OBJECTNAME = "mybOname"; // "currentMybatchesObjectName";
    public static final String PREFERENCE_MYBATCHES_LASTACTIVITY = "mybLastActivity"; // "currentMybatchesLastActivity";

    // Cookies-Combined, will hold all the Tag(s)
    // eg:mybStatus=RDY_FOR_QC1,SYSTEM_ERROR|mybHold=Ignore|mybProvider=/Batches/Wiley Publishers, Inc.|mybFromCreateDate=12/31/9999|mybFromScheduleDate=12/31/9999|mybPerformer=seshadri.ranganathan|mybToCreateDate=12/31/9999|mybToScheduleDate=12/31/9999|
    public static final String PREFERENCE_MYBATCHES_COMBINED_COOKIE = "mybCombo";

    // Tag(s) for Combined Cookie
    public static final String PREFERENCE_MYBATCHES_BATCH_STATUS = "mybStatus";// currentMybatchesBatchStatus
    public static final String PREFERENCE_MYBATCHES_HOLD_STATUS = "mybHold"; // "currentMybatchesHoldStatus";
    public static final String PREFERENCE_MYBATCHES_PROVIDER = "mybProvider"; // "currentMybatchesProvider";
    public static final String PREFERENCE_MYBATCHES_PROFILE = "mybProfile"; // "currentMybatchesProfile";
    public static final String PREFERENCE_MYBATCHES_FROM_CREATION_DATE = "mybFromCreateDate"; // "currentMybatchesFromCreationDate";
    public static final String PREFERENCE_MYBATCHES_FROM_SCHEDULE_DATE = "mybFromScheduleDate"; // "currentMybatchesFromScheduleDate";
    public static final String PREFERENCE_MYBATCHES_PERFORMER = "mybPerformer"; // "currentMybatchesPerformer";
    public static final String PREFERENCE_MYBATCHES_TO_CREATION_DATE = "mybToCreateDate"; // "currentMybatchesToCreationDate";
    public static final String PREFERENCE_MYBATCHES_TO_SCHEDULE_DATE = "mybToScheduleDate"; // "currentMybatchesToScheduleDate"; // NEW

    // Bulk actions
    private DropDownList m_bulkActionFilter;
    public static final String BULKACTION_CONTROL_FILTER = "bulkaction_control";

    public static final String[] BULKACTIONS = {"Annotate Batches",
                                                "Put Batches on Hold",
                                                "Remove Batches from Hold",
                                                "Schedule Batches",
                                                "Reschedule Batches",
                                                "Unschedule Batches",
                                                // "Execute StartAllOver",
                                                "Reset Batches",
                                                "Rename Batches",
                                                "Delete Batches",
                                                "Adjust Batch Priority",
                                                "Change Profile",
                                                "Update Custom Queue"
                                                };
    // Report actions
    private DropDownList m_reportActionFilter;
    public static final String REPORTACTION_CONTROL_FILTER = "reportaction_control";
    public static final String[] REPORTACTIONS = {"Batch Report",
                                                "Batch Error Report",
                                                "Batch Warnings Report",
                                                "Invalid Supplied Files Error Report", // use 'CustomBatchListErrorReportTypeA' component
                                                "CS Name Assignment Problems Error Report", // use 'CustomBatchListErrorReportTypeB' component
                                                "DMD Error Report", // use 'CustomBatchListErrorReportTypeB' component
                                                "Expected FU SU Error Report", // use 'CustomBatchListErrorReportTypeB' component
                                                "File Referenced But Not Found Error Report" // use 'CustomBatchListErrorReportTypeB' component
                                                };
    // Hidden Control
    public static final String HIDDENPROVIDERPROFILEMAP_LABEL = "hiddenProviderProfileMap";
}
