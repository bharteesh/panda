
package org.portico.conprep.ui.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.portico.common.events.KeyMetadataElementsConstants;
import org.portico.common.formatregistry.FormatRegistryService;
import org.portico.common.formatregistry.FormatRegistryServiceFactory;
import org.portico.common.formatregistry.FormatRegistryUtil;
import org.portico.common.formatregistry.xml.Format;
import org.portico.common.mimetypelookup.MimeTypeLookupService;
import org.portico.common.mimetypelookup.MimeTypeLookupServiceFactory;
import org.portico.common.mimetypelookup.MimeTypeLookupUtil;
import org.portico.common.providerlookup.ProviderLookupService;
import org.portico.common.providerlookup.ProviderLookupServiceFactory;
import org.portico.common.providerlookup.ProviderLookupUtil;
import org.portico.common.providerlookup.xml.MappingEntry;
import org.portico.common.providerlookup.xml.ProfileSetType;
import org.portico.common.providerlookup.xml.ProfileType;
import org.portico.common.submissionprofile.SubmissionProfileService;
import org.portico.common.submissionprofile.SubmissionProfileServiceFactory;
import org.portico.common.submissionprofile.SubmissionProfileUtil;
import org.portico.common.submissionprofile.facade.SubmissionProfileFacade;
import org.portico.common.submissionprofile.xml.ContentType;
import org.portico.common.submissionprofile.xml.SubmissionProfile;
import org.portico.conprep.ui.app.AppSessionContext;
import org.portico.conprep.ui.profile.ProfileUI;
import org.portico.conprep.ui.provider.ProviderUI;
import org.portico.conprep.workflow.impl.documentum.ActionTool;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfValidator;
import com.documentum.fc.client.IDfValueAssistance;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.ConfigService;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.role.RoleService;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.documentum.webcomponent.library.workflow.WorkflowService;


public class HelperClass
{
    public static Logger sLogger = null;

//  prov=PR-1|Wiley-8.0.xml|Wiley-7.0.xml|prov=PR-2|Ams_1.0.xml|Ams_2.0.xml
    public static String g_providerProfileMappingAsString = "";
    public static String g_hiddenProviderTag = "prov=";
    public static String g_hiddenProviderTagSeparator = "|";
    public static final String FORMAT_NAME = "FORMAT_NAME";
    public static final String MIME_TYPE = "MIME_TYPE";
    public static final String PUBLISHER_SUPPLIED = "Publisher-Supplied";
    public static final String USER_SUPPLIED = "User-Supplied";

    public HelperClass()
    {
    }

    public static boolean roleCheck(Component component, ArgumentList argumentList, Context context, String roleName)
    {
        boolean isValid = false;

        HelperClass.porticoOutput(0, "HelperClass-roleCheck-Start");

        try
        {
            isValid = RoleService.isUserAssignedRole(component.getDfSession().getLoginUserName(), roleName, argumentList, context);
        }
        catch (Exception e)
        {
            HelperClass.porticoOutput(1, "HelperClass-Exception in roleCheck-"+e.toString());
        }

        HelperClass.porticoOutput(0, "HelperClass-roleCheck-isValid="+isValid);

        return isValid;
    }

    public static boolean roleCheck(Component component, ArgumentList argumentList, Context context)
    {
		return roleCheck(component, argumentList, context, "conprep_inspector_role");
    }

    public static boolean performerCheck(IDfSession currentSession, String objectId, Hashtable addlnInfo)
    {
        boolean isValid = false;

        HelperClass.porticoOutput(0, "HelperClass-performerCheck-Start(From Storage) - objectId="+objectId);

        try
        {
			String batchPerformer = null;
			String batchId = "";

   			if(addlnInfo != null &&
   			    addlnInfo.containsKey(HelperClassConstants.BATCHPERFORMER))
		    {
                batchPerformer = (String)addlnInfo.get(HelperClassConstants.BATCHPERFORMER);
		    }

            // Note: Performer could be "" during 'processing' etc. which is legitimate
            //       So, pick from Storage only if 'Performer' is null
            if(batchPerformer == null) // || batchPerformer.equals(""))
            {
     			if(addlnInfo != null &&
    			    addlnInfo.containsKey(HelperClassConstants.BATCHOBJECTID))
    		    {
                    batchId = (String)addlnInfo.get(HelperClassConstants.BATCHOBJECTID);
		        }

                if(batchId == null || batchId.equals(""))
                {
                    String objectType = getObjectType(currentSession, objectId);
                    if(null != objectType && !objectType.equals(""))
                    {
						if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
						{
							batchId = objectId;
						}
						else
						{
    		    	        batchId = getParentBatchFolderId(currentSession, objectId);
					    }
				    }
			    }
			    batchPerformer = QcHelperClass.getBatchPerformer(currentSession, batchId, addlnInfo);
		    }

		    if(batchPerformer != null)
		    {
                if(batchPerformer.equals(currentSession.getLoginUserName()))
                {
                    isValid = true;
                }
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "HelperClass-Exception in performerCheck-"+e.toString());
        }
        finally
        {
		}

        HelperClass.porticoOutput(0, "HelperClass-performerCheck-End(From Storage) - objectId,isValid="+objectId+","+isValid);

        return isValid;
    }

// RANGA New Datamodel, to be removed later after all object type(s) refer to DBHelperClass constants
    public static String getInternalObjectType(String objectTypeName)
    {
        String objectType = "";

        if(objectTypeName.equals("submission_batch"))
        {
            objectType = "p_batch";
        }
        else if(objectTypeName.equals("workpad_batch"))
        {
            objectType = "p_work_pad";
        }
        else if(objectTypeName.equals("cu_state"))
        {
            objectType = "p_cu";
        }
        else if(objectTypeName.equals("fu_state"))
        {
            objectType = "p_fu";
        }
        else if(objectTypeName.equals("su_state"))
        {
            objectType = "p_su";
        }
        else if(objectTypeName.equals("cu_object"))
        {
            objectType = "p_cu";
        }
        else if(objectTypeName.equals("fu_object"))
        {
            objectType = "p_fu";
        }
        else if(objectTypeName.equals("su_object"))
        {
            objectType = "p_su";
        }
        else if(objectTypeName.equals("desc_dmd_object"))
        {
            objectType = "p_desc_md";
        }
        else if(objectTypeName.equals("provider_folder"))
        {
            objectType = "p_provider_folder";
        }
        else if(objectTypeName.equals("raw_unit"))
        {
            objectType = "p_raw_unit";
        }
        else if(objectTypeName.equals("work_area_folder"))
        {
            objectType = "dm_cabinet";
        }
        else if(objectTypeName.equals("submission_area_folder"))
        {
            objectType = "dm_cabinet";
        }
        else if(objectTypeName.equals("work_area_received_files_folder"))
        {
            objectType = "dm_folder";
        }
        else if(objectTypeName.equals("usermessage_object"))
        {
            objectType = "p_user_message";
        }
        else if(objectTypeName.equals("file_ref_object"))
        {
            objectType = "p_file_ref";
        }

        return objectType;
    }

    public static void porticoOutput(String msg)
    {
        // For now, we are logging all messages as info if they do not have a level(tracing)
        porticoOutput(0, msg);
    }

    // Overloaded to use logging levels
    public static void porticoOutput(int level, String msg)
    {
        // For now, till we totally use DfLogger logging we will continue on System.out.println also
        // System.out.println(HelperClass.getThisDateTime()+"::"+"ConPrep UI ....:"+msg);// Can be commented later, if no debug required

        try
        {
            // org.apache.log4j.Logger logger = DfLogger.getLogger("tracing");
            if(sLogger == null)
            {
                sLogger = DfLogger.getLogger("tracing");
            }

            if(1 == level)
            {
                // Errors messages
                sLogger.error(msg);
            }
            else // Others
            {
                // Info messages
                sLogger.info(msg);
            }
        }
        catch(Exception e)
        {
            // Remove this comment when going live, so that we can still look at the application logged messages in
            // standard out
            // System.out.println(HelperClass.getThisDateTime()+"::"+"ConPrep UI ....:"+msg);
            System.out.println(HelperClass.getThisDateTime()+"::"+"ConPrep UI ....:"+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
    }

    public static String getThisDateTime()
    {
        Calendar cal = Calendar.getInstance();
        Date dt = cal.getTime();
        return dt.toString();
    }

    public static ProfileUI getProfileUIInfo(SubmissionProfileService profileService, String tProfileID)
    {
        porticoOutput(0, "Start Web Services Based - getProfileUIInfo()-getActualProfileInformation");
        ProfileUI tPorticoProfileUI = new ProfileUI();
        try
        {
           SubmissionProfileFacade tSubmissionProfile = profileService.getProfile(tProfileID);
           SubmissionProfile profile = tSubmissionProfile.getSubmissionProfile();
           // String tId = profile.getId();
           String tName = profile.getName();
           String tDescription = profile.getDescription();
           String tContentType = ((ContentType)profile.getContentType()).value();
           // String tProviderId = profile.getProviderId();

           porticoOutput(0, "Profile:tId=" + tProfileID);
           porticoOutput(0, "Profile:tName=" + tName);
           porticoOutput(0, "Profile:tDescription=" + tDescription);
           porticoOutput(0, "Profile:tContentType=" + tContentType);
           // porticoOutput(0, "Profile:tProviderId=" + tProviderId);

           // Change here, to add more profileUI data, if class has more attributes
           tPorticoProfileUI.setProfileID(tProfileID);
           tPorticoProfileUI.setProfileName(tName);
           tPorticoProfileUI.setContentType(tContentType);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in Web Services Based - getProfileUIInfo()"+e.toString());
            e.printStackTrace();
        }
        porticoOutput(0, "End Web Services Based - getProfileUIInfo()-getActualProfileInformation()");

        return tPorticoProfileUI;
    }

    public static ProviderUI getProviderUIInfo(ProviderLookupService providerService,
                                                      SubmissionProfileService profileService,
                                                      String tProviderID)
    {
        porticoOutput(0, "Start Web Services Based - getProviderUIInfo()-getActualProviderInformation");
        ProviderUI tPorticoProviderUI = null;
        ProfileUI currentProfileUI = null;
        try
        {
            MappingEntry tMappingEntry = providerService.getProviderMapping(tProviderID);
            String tId = tMappingEntry.getId();
            String tName = tMappingEntry.getName();
            // String tDescription = tMappingEntry.getDescription();
            ProfileSetType tProfileSetType = tMappingEntry.getProfileSet();

            String tDefaultProfileId = tProfileSetType.getDefaultProfileId();

           // Change here, to add more providerUI data, if class has more attributes

            tPorticoProviderUI = new ProviderUI();
            tPorticoProviderUI.setProviderID(tId);
            // JIRA - CONPREP-1647, setting the providerName same as providerId here will ensure that
            //                      whenever a providerName is called for, the returned value would be the providerId
            tPorticoProviderUI.setProviderName(tId);
            porticoOutput(0, "Provider:tId=" + tId);
            porticoOutput(0, "Provider:tName(same as tId)=" + tId);
            g_providerProfileMappingAsString = g_providerProfileMappingAsString + g_hiddenProviderTag + tId + g_hiddenProviderTagSeparator;

            if(tDefaultProfileId != null)
            {
                tPorticoProviderUI.setDefaultProfileID(tDefaultProfileId);

                // porticoOutput(0, "Provider:tDescription=" + tDescription);
                porticoOutput(0, "Provider:tDefaultProfileId=" + tDefaultProfileId);

                currentProfileUI = getProfileUIInfo(profileService, tDefaultProfileId);
                tPorticoProviderUI.setProfileUI(currentProfileUI); // First profile is the default profile
            }

            // AdditionalProfilesSetType tAdditionalProfilesSetType = tProvider.getAdditionalProfilesSet();

            // List all profiles, including default profile
            List tprofileList = tProfileSetType.getProfile();
            if(tprofileList != null && tprofileList.size() > 0)
            {
               for(int indx=0; indx < tprofileList.size(); indx++)
               {
                   ProfileType tProfileType = (ProfileType)tprofileList.get(indx);
                   String tProfileId = tProfileType.getId();
                   porticoOutput(0, "Provider:ProfileListId=" + tProfileId);
                   g_providerProfileMappingAsString = g_providerProfileMappingAsString + tProfileId + g_hiddenProviderTagSeparator;
                   if(tDefaultProfileId != null && tProfileId.equals(tDefaultProfileId))
                   {
                       porticoOutput(0, "This is the default profile, already added, exclude this DefaultProfileId="+tDefaultProfileId);
                       continue;
                   }
                   // For each profileId, get the actual Profile
                   currentProfileUI = getProfileUIInfo(profileService, tProfileId);
                   tPorticoProviderUI.setProfileUI(currentProfileUI);
               }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in Web Services Based - getProviderProfileInfo()"+e.toString());
            e.printStackTrace();
        }
        porticoOutput(0, "End Web Services Based - getProviderUIInfo()-getActualProviderInformation");

        return tPorticoProviderUI;
    }

//  Web sevices based
    public static ArrayList getProviderProfileInfoExt()
    {
        ArrayList listProviderUI = new ArrayList();
        ProviderUI tPorticoProviderUI = null;
        try
        {
            // create Provider Service
            ProviderLookupServiceFactory tProviderLookupServiceFactory = ProviderLookupUtil.getFactory();
            ProviderLookupService tProviderLookupService = tProviderLookupServiceFactory.createService();

            // create SubmissionProfileService Service
            SubmissionProfileServiceFactory tSubmissionProfileServiceFactory = SubmissionProfileUtil.getFactory();
            SubmissionProfileService tSubmissionProfileService = tSubmissionProfileServiceFactory.createService();


            List providerList = tProviderLookupService.getProviderMappingList();
            // List of providerId(s) are available now
            porticoOutput(0, "Start Web Services Based - getProviderProfileInfoExt()-List of Providers");
            for(int indx=0; indx < providerList.size(); indx++)
            {
                MappingEntry tMappingEntry = (MappingEntry)providerList.get(indx);
                String tProviderId = tMappingEntry.getId();
                // JIRA - CONPREP-1647
                String tProviderName = tMappingEntry.getId();
                porticoOutput(0, "Provider:providerList:providerId=" + tProviderId);
                porticoOutput(0, "Provider:providerList:providerName(same as tId)=" + tProviderName);
                tPorticoProviderUI = getProviderUIInfo(tProviderLookupService, tSubmissionProfileService, tProviderId);
                listProviderUI.add(tPorticoProviderUI);
            }
            porticoOutput(0, "End Web Services Based - getProviderProfileInfoExt()-List of Providers");
         }
         catch(Exception e)
         {
            porticoOutput(1, "Exception in Web Services Based - getProviderProfileInfo()"+e.toString());
            e.printStackTrace();
         }

         return listProviderUI;
    }

    public static ArrayList getProviderProfileInfo()
    {
        porticoOutput(0, "Start Web Services Based - getProviderProfileInfo()");
		// Reset the mapping string
		g_providerProfileMappingAsString = "";
        ArrayList listProviderUI = null;
        listProviderUI = getProviderProfileInfoExt();
        porticoOutput(0, "End Web Services Based - getProviderProfileInfo()");

        return listProviderUI;
    }

    public static ArrayList lookupServiceInfo(String serviceType, String serviceId, ArrayList attrList)
    {
        ArrayList outputList = new ArrayList();
        try
        {
            if(serviceId != null && attrList != null && attrList.size() > 0)
            {
                ValuePair tValuePair = null;
                if(serviceType.equals("provider"))
                {
                    // create Provider Service
                    ProviderLookupServiceFactory tProviderLookupServiceFactory = ProviderLookupUtil.getFactory();
                    ProviderLookupService providerService = tProviderLookupServiceFactory.createService();
                    MappingEntry tMappingEntry = providerService.getProviderMapping(serviceId);
                    String tId = tMappingEntry.getId();
                    // JIRA - CONPREP-1647
                    String tName = tId; // tMappingEntry.getName();
                    String tDescription = tId; // tMappingEntry.getName();// no desc
                    String attrName = "";
                    for(int indx=0; indx < attrList.size(); indx++)
                    {
                        attrName = (String)attrList.get(indx);
                        tValuePair = new ValuePair();
                        tValuePair.setKey(attrName);
                        if(attrName.equals("name"))
                        {
                            tValuePair.setValue(tName);
                        }
                        else if(attrName.equals("desc"))
                        {
                            tValuePair.setValue(tDescription);
                        }
                        outputList.add(tValuePair);
                    }
                }
                else if(serviceType.equals("profile"))
                {
                    // create SubmissionProfileService Service
                    SubmissionProfileServiceFactory tSubmissionProfileServiceFactory = SubmissionProfileUtil.getFactory();
                    SubmissionProfileService profileService = tSubmissionProfileServiceFactory.createService();
                    SubmissionProfileFacade tSubmissionProfile = profileService.getProfile(serviceId);
                    SubmissionProfile profile = tSubmissionProfile.getSubmissionProfile();
                    // String tId = profile.getId();
                    String tName = profile.getName();
                    String tDescription = profile.getDescription();
                    String tAlternateWorkflow = profile.getAlternateWorkflow();
                    String attrName = "";
                    for(int indx=0; indx < attrList.size(); indx++)
                    {
                        attrName = (String)attrList.get(indx);
                        tValuePair = new ValuePair();
                        tValuePair.setKey(attrName);
                        if(attrName.equals("name"))
                        {
                            tValuePair.setValue(tName);
                        }
                        else if(attrName.equals("desc"))
                        {
                            tValuePair.setValue(tDescription);
                        }
                        else if(attrName.equals("alternateworkflow"))
                        {
							tValuePair.setValue(tAlternateWorkflow);
						}
                        outputList.add(tValuePair);
                    }
                }
                else if(serviceType.equals("format"))
                {
                    // create Format Service
                    // FormatLookupServiceFactory tFormatLookupServiceFactory = FormatLookupUtil.getFactory();
                    // FormatLookupService formatService = tFormatLookupServiceFactory.createService();

                    // We do not have format name etc., the format Id itself is the format name.
                    String tId = serviceId;
                    String tName = serviceId;
                    String tDescription = serviceId;
                    String attrName = "";
                    for(int indx=0; indx < attrList.size(); indx++)
                    {
                        attrName = (String)attrList.get(indx);
                        tValuePair = new ValuePair();
                        tValuePair.setKey(attrName);
                        if(attrName.equals("name"))
                        {
                            tValuePair.setValue(tName);
                        }
                        else if(attrName.equals("desc"))
                        {
                            tValuePair.setValue(tDescription);
                        }
                        outputList.add(tValuePair);
                    }
                }
                else
                {
                    porticoOutput(1, "HelperClass-lookupServiceInfo-Unknown serviceType="+serviceType);
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-lookupServiceInfo-serviceType-serviceId=" + serviceType + ":" + serviceId + ":" + e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        return outputList;
    }

    public static ArrayList getObjectAttrValues(IDfSession currentSession, String objectType, String objectId, ArrayList attrList)
    {
        ArrayList outputList = new ArrayList();
        ValuePair tValuePair = null;
        String attrName = "";
        String attrValue = "";
        IDfCollection idfcollection = null;

        porticoOutput(0, "HelperClass-getObjectAttrValues(Started) objectType,objectId="+objectType+","+objectId);

        try
        {
            if(attrList != null && attrList.size() > 0 && objectType != null)
            {
    			// Pick those Documentum objectType attributes from Documentum
				if(objectType.equals(DBHelperClass.BATCH_TYPE) ||
				        objectType.equals(DBHelperClass.RAW_UNIT_TYPE))
				{
					String dqlString = "";
                    boolean appendSeparator = false;
                    for(int indx=0; indx < attrList.size(); indx++)
                    {
                        if(appendSeparator == true)
                        {
                            dqlString += ",";
                        }
                        dqlString += (String)attrList.get(indx);
                        appendSeparator = true;
                    }
                    dqlString = "SELECT " + dqlString + " FROM " + objectType + " WHERE r_object_id='" + objectId + "'";
                    DfQuery dfquery = new DfQuery();
                    dfquery.setDQL(dqlString);
                    porticoOutput(0, "HelperClass-getObjectAttrValues OPEN IDfCollection");
                    for(idfcollection = dfquery.execute(currentSession, 0); idfcollection.next();)
                    {
                        for(int indx=0; indx < attrList.size(); indx++)
                        {
                            attrName = (String)attrList.get(indx);
                            attrValue = idfcollection.getString(attrName);
                            tValuePair = new ValuePair();
                            tValuePair.setKey(attrName);
                            tValuePair.setValue(attrValue);
                            porticoOutput(0, "HelperClass-getObjectAttrValues key,value="+attrName+","+attrValue);
                            outputList.add(tValuePair);
                        }
                        break;
                    }
			    }
			    else
			    {
					// Pick other objectType attributes from Oracle
					Hashtable alistOut = DBHelperClass.getObjectAttributes(objectType, objectId, attrList);
					if(alistOut != null && alistOut.size() > 0)
					{
                        Enumeration enumerate = alistOut.keys();
                        while(enumerate.hasMoreElements())
                        {
                            attrName = (String)enumerate.nextElement();
                            attrValue = (String)alistOut.get(attrName);
                            tValuePair = new ValuePair();
                            tValuePair.setKey(attrName);
                            tValuePair.setValue(attrValue);
                            porticoOutput(0, "HelperClass-getObjectAttrValues key,value="+attrName+","+attrValue);
                            outputList.add(tValuePair);
					    }
					}
				}
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getObjectAttrValues()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getObjectAttrValues- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getObjectAttrValues()-close="+e.toString());
                e.printStackTrace();
            }
        }
        porticoOutput(0, "HelperClass-getObjectAttrValues(Ended) objectType,objectId="+objectType+","+objectId);

        return outputList;
    }

    public static String getMimeTypeInfo(String fileExtension)
    {
        String mimetype = "";
        try
        {
            porticoOutput(0, "HelperClass-getMimeTypeInfo-Input fileExtension="+fileExtension);
            if(fileExtension != null && fileExtension != "")
            {
                MimeTypeLookupServiceFactory tMimeTypeLookupServiceFactory = MimeTypeLookupUtil.getFactory();
                MimeTypeLookupService mimetypeService = tMimeTypeLookupServiceFactory.createService();
                mimetype = mimetypeService.getMimeType(fileExtension);
                porticoOutput(0, "HelperClass-getMimeTypeInfo-fileExtension:mimetype="+fileExtension+":"+mimetype);
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getMimeTypeInfo-fileExtension=" + fileExtension + ":" + e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        return mimetype;
    }

// RANGA 24AUG2006 Ask Vinay ????, should we have look up tables in Oracle for other object types
    // Returns ArrayList containing ValuePair(key,value) for Documentum look up
    public static ArrayList getValueAssistanceListFromDocbase(IDfSession currentSession, String objectType, String AttrName)
    {
        ArrayList allValues = new ArrayList();
        ValuePair tPorticoValuePair = null;
        porticoOutput(0, "HelperClass-getValueAssistanceListFromDocbase(Started)-objectType="+objectType);
        try
        {
			if(objectType.equals(DBHelperClass.BATCH_TYPE) ||
			        objectType.equals(DBHelperClass.RAW_UNIT_TYPE))
			{
                Hashtable aList = getLookupList(currentSession, objectType, AttrName);
                if(aList != null && aList.size() > 0)
                {
                    Enumeration enumerate = aList.keys();
                    while(enumerate.hasMoreElements())
                    {
                        String actualValue = (String)enumerate.nextElement();
                        String displayValue = (String)aList.get(actualValue);
                        tPorticoValuePair = new ValuePair();
                        tPorticoValuePair.setKey(actualValue);
                        tPorticoValuePair.setValue(displayValue);
                        allValues.add(tPorticoValuePair);
                        porticoOutput(0, "HelperClass-value assistance (Display-Value)value="+displayValue);
                        porticoOutput(0, "HelperClass-value assistance (Actual-Key)value="+actualValue);
                    }
			    }
			    else
			    {
					porticoOutput(1, "Error in HelperClass-No value assistance for objectType="+objectType+","+
					                                                               "AttrName="+AttrName);
				}
		    }
		    else
		    {
				// Pick from Oracle lookup(????) // Calling appln must use 'getLookupList' for efficiency
			}
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getValueAssistanceListFromDocbase-" + e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        porticoOutput(0, "HelperClass-getValueAssistanceListFromDocbase(Ended)-objectType="+objectType);

        return allValues;
    }

    // Returns direct hashtable(key,value)
    public static Hashtable getLookupList(IDfSession currentSession, String objectType, String attrName)
    {
        Hashtable allValues = new Hashtable();
        porticoOutput(0, "HelperClass-getLookupList(Started)-objectType,attrName="+objectType+","+attrName);
        try
        {
			if(objectType.equals(DBHelperClass.BATCH_TYPE) ||
			        objectType.equals(DBHelperClass.RAW_UNIT_TYPE))
			{
                IDfType tIDfType = null;
                tIDfType = (IDfType)currentSession.getType(objectType);
                IDfValidator tIDfValidator = tIDfType.getTypeValidator(null, null);
                IDfValueAssistance tIDfValueAssistance = tIDfValidator.getValueAssistance(attrName,null);
                IDfList tIDfList = tIDfValueAssistance.getActualValues();
                for(int i=0; i < tIDfList.getCount(); i++)
                {
                    String actualValue = tIDfList.getString(i);
                    String displayValue = tIDfValueAssistance.getDisplayValue(actualValue);
                    if(!allValues.containsKey(actualValue))
                    {
						allValues.put(actualValue, displayValue);
					}
                    porticoOutput(0, "HelperClass-getLookupList Actual-Key="+ actualValue +","+
                                                                "Display-Value="+displayValue);
                }
		    }
		    else
		    {
		    	// PMD2.0, no looks in the database, all objects have direct strings
				// Pick from Oracle lookup
				// DBHelperClass.getLookupData(objectType, attrName);
			}
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getLookupList-" + e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        porticoOutput(0, "HelperClass-getLookupList(Ended)-objectType,attrName="+objectType+","+attrName);

        return allValues;
    }

    public static ArrayList getSubmissionViewObjects(IDfSession currentSession, String batchID)
    {
        porticoOutput(0, "HelperClass-getSubmissionViewObjects Start time="+getThisDateTime());
        // Pick all the raw_units for this batchID
        ArrayList resultItems = new ArrayList();
        ArrayList listItems = new ArrayList();
        porticoOutput(0, "HelperClass-getSubmissionViewObjects OPEN IDfCollection");
        IDfCollection tIDfCollection = null;
        try
        {
            IDfFolder tIDfFolder = (IDfFolder)currentSession.getObject(new DfId(batchID));
            String batchFolderName = tIDfFolder.getObjectName();
            tIDfCollection = tIDfFolder.getContents(null);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    IDfTypedObject tIDfTypedObject = tIDfCollection.getTypedObject();
                    String currentItem = tIDfTypedObject.getString("object_name");
                    porticoOutput(0, "HelperClass-getSubmissionViewObjects="+currentItem);
                    if(tIDfTypedObject.getString("r_object_type").equalsIgnoreCase(DBHelperClass.RAW_UNIT_TYPE))
                    {
                        String currentObjectId = tIDfTypedObject.getString("r_object_id");
                        listItems.add(currentItem);
                        porticoOutput(0, "HelperClass-(raw_unit)getSubmissionViewObjects="+currentItem);
                    }
                }
            }
            CallPatternHandler tCallPatternHandler = new CallPatternHandler();
            tCallPatternHandler.setRootName("Batch-"+batchFolderName);
            tCallPatternHandler.addProcessingItems(listItems);
            tCallPatternHandler.processHandler();
            resultItems = tCallPatternHandler.getProcessedItems();
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getSubmissionViewObjects-" + e.toString());
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
                porticoOutput(0, "HelperClass-getChildrenOfFolderTypeObject CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getSubmissionViewObjects-close" + e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getSubmissionViewObjects End time="+getThisDateTime());

        return resultItems;
    }

// Do not remove this IMPORTANT for SUBMISSION-import  function ...........
/*
    public static String getBatchFolderObjectIdInfo(IDfSession currentSession)
    {
        String componentId = "submitbatchcontainer";
        String batchFolderObjectId = "";
        try
        {
            // Pick from config file
            IConfigElement iconfigelementForBatch = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            String batchFolderPath = iconfigelementForBatch.getChildValue("docbase_upload_folderpath");
            porticoOutput(0, "HelperClass-getBatchFolderObjectId()-batchFolderPath="+batchFolderPath);
            IDfFolder tIDfFolder = currentSession.getFolderByPath(batchFolderPath);
            IDfId tIDfId = tIDfFolder.getObjectId();
            batchFolderObjectId = tIDfId.getId();
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getBatchFolderObjectId:"+e.toString());
            e.printStackTrace();
        }

        porticoOutput(0, "HelperClass-getBatchFolderObjectId()-batchFolderObjectId="+batchFolderObjectId);

        return batchFolderObjectId;
    }
*/
// End

    public static ArrayList getProcessViewObjects(IDfSession currentSession, String batchID, String cuStateId, ProcessViewFilter tFilter)
    {
        porticoOutput(0, "Helper-getProcessViewObjects Start time="+getThisDateTime());
        ArrayList resultItems = new ArrayList();
        try
        {
            CallProcessViewHandler tCallProcessViewHandler = new CallProcessViewHandler(currentSession, batchID, cuStateId, tFilter);
            tCallProcessViewHandler.processHandler();
            resultItems = tCallProcessViewHandler.getProcessedItems();
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getProcessViewObjects-" + e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        porticoOutput(0, "HelperClass-getProcessViewObjects End time="+getThisDateTime());

        return resultItems;
    }

    // Pass batchObjectId
/*
    public static String getWorkflowObject(IDfSession currentSession, String ObjectId)
    {
        porticoOutput(0, "getWorkflowObject OPEN IDfCollection");
        IDfCollection workflowIDfCollection = null;
        String workflowtObjectId = null;
        try
        {
            if(null != objectId && !objectId.equals(""))
            {

                if(WorkflowService.isWorkflowId(ObjectId))
                {
                     workflowtObjectId = ObjectId;
                }
                else
                {
                    //IDfSysObject batchIDfSysObject = (IDfSysObject)
                    IDfFolder batchIDfFolder = (IDfFolder)currentSession.getObject(new DfId(ObjectId));
                    int workflowCount = 0;
                    // collection of 'r_workflow_id'
                    // order by latest wf, time_stamp
                    workflowIDfCollection = batchIDfFolder.getWorkflows(null, null);
                    if(workflowIDfCollection != null)
                    {
                        while(workflowIDfCollection.next())
                        {
                            IDfTypedObject tIDfTypedObject = workflowIDfCollection.getTypedObject();
                            // Pick only the First workflow
                            if(workflowCount == 0)
                            {
                                workflowtObjectId = tIDfTypedObject.getString("r_workflow_id");
                            }
                            workflowCount++;
                        }
                    }
                    porticoOutput(0, "getWorkflowObject:Batch_ID="+ ObjectId+":workflow count="+workflowCount);
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getWorkflowObject(), objectId="+ objectId + " " + e.toString());
            e.printStackTrace();
        }
        finally
        {
             try
             {
                if(workflowIDfCollection != null)
                {
                    workflowIDfCollection.close();
                }
                porticoOutput(0, "getWorkflowObject CLOSE IDfCollection");
             }
             catch(Exception e)
             {
                porticoOutput(1, "Exception in getWorkflowObject-close" + e.toString());
                e.printStackTrace();
             }
        }
        porticoOutput(0, "getWorkflowObject:ObjectId="+ ObjectId);

        return workflowtObjectId;
    }
*/

    public static String getWorkflowObject(IDfSession currentSession, String objectId)
    {
		return getWorkflowObject(currentSession, objectId, null);
	}

    public static String getWorkflowObject(IDfSession currentSession, String objectId, Hashtable addlnInfo)
    {
        porticoOutput(0, "HelperClass-getWorkflowObject(Started) objectId="+objectId);
        IDfCollection workflowIDfCollection = null;
        String workflowtObjectId = null;
        try
        {
            if(null != objectId && !objectId.equals(""))
            {
                if(WorkflowService.isWorkflowId(objectId))
                {
                     workflowtObjectId = objectId;
                }
                else
                {
					String currentObjectType = "";
					boolean isBatchWorkFlowIdKeyFound = false;
    			    if(addlnInfo != null)
    			    {
    			        if(addlnInfo.containsKey(HelperClassConstants.OBJECTTYPE))
			            {
                            currentObjectType = (String)addlnInfo.get(HelperClassConstants.OBJECTTYPE);
			            }
    			        if(addlnInfo.containsKey(HelperClassConstants.BATCHWORKFLOWID))
			            {
							isBatchWorkFlowIdKeyFound = true;
                            workflowtObjectId = (String)addlnInfo.get(HelperClassConstants.BATCHWORKFLOWID);
                            porticoOutput(0, "HelperClass-getWorkflowObject-isBatchWorkFlowIdKeyFound,workflowtObjectId="+isBatchWorkFlowIdKeyFound+","+workflowtObjectId);
			            }
				    }

				    if(isBatchWorkFlowIdKeyFound == false &&
				            (workflowtObjectId == null || workflowtObjectId.equals("")))
				    {
			            if(currentObjectType == null || currentObjectType.equals(""))
			            {
                            currentObjectType = getObjectType(currentSession, objectId);
				        }
                        if(currentObjectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
                        {
                            porticoOutput(0, "HelperClass-getWorkflowObject(From Storage) OPEN IDfCollection");
                            String attrNames = "r_object_id,r_workflow_id ";
                            DfQuery dfquery = new DfQuery();
                            String dqlString = "SELECT " + attrNames + " FROM " + "dmi_package" +
                                                  " WHERE any r_component_id=" + "'"+objectId+"'";
                            HelperClass.porticoOutput(0, "HelperClass-getWorkflowObject()-dqlString="+dqlString);

			                dfquery.setDQL(dqlString);
                            workflowIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
                            if(workflowIDfCollection != null)
                            {
                                while(workflowIDfCollection.next())
                                {
			                		workflowtObjectId = workflowIDfCollection.getString("r_workflow_id");
			                		break;
			                	}
			                }
/* This is also slow
                            String qualification = "dmi_package WHERE any r_component_id=" + "'"+objectId+"'";
                            HelperClass.porticoOutput(0, "QcHelperClass-getWorkflowObject-qualification="+qualification);
                            IDfPackage iDfPackage = (IDfPackage) currentSession.getObjectByQualification(qualification);
                            if( null != iDfPackage )
                            {
						        IDfId workflowIdfId = iDfPackage.getWorkflowId();
						        if(workflowIdfId != null)
						        {
                                    workflowtObjectId = workflowIdfId.getId();
							    }
                            }
*/
/* DFC API slow
                            IDfSysObject batchIDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
                            int workflowCount = 0;
                            // collection of 'r_workflow_id'
                            // order by latest wf, time_stamp
                            workflowIDfCollection = batchIDfSysObject.getWorkflows(null, null);
                            if(workflowIDfCollection != null)
                            {
                                while(workflowIDfCollection.next())
                                {
                                    IDfTypedObject tIDfTypedObject = workflowIDfCollection.getTypedObject();
                                    // Pick only the First workflow
                                    if(workflowCount == 0)
                                    {
                                        workflowtObjectId = tIDfTypedObject.getString("r_workflow_id");
                                    }
                                    else
                                    {
                                        porticoOutput(1, "WARNING-HelperClass-getWorkflowObject()more than 1 workflow object associated with:Batch_ID="+ objectId);
                                        break;
                                    }
                                    workflowCount++;
                                }
                            }
*/
                        }
				    }
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getWorkflowObject(), objectId="+ objectId + " " + e.toString());
            e.printStackTrace();
        }
        finally
        {
             try
             {
                if(workflowIDfCollection != null)
                {
                    workflowIDfCollection.close();
                }
                porticoOutput(0, "HelperClass-getWorkflowObject CLOSE IDfCollection");
             }
             catch(Exception e)
             {
                porticoOutput(1, "Exception in HelperClass-getWorkflowObject-close" + e.toString());
                e.printStackTrace();
             }
        }
        porticoOutput(0, "HelperClass-getWorkflowObject()(Ended)objectId,workflowid="+ objectId + "," + workflowtObjectId);

        return workflowtObjectId;
    }

    public static int getWorkflowRunTimeStatus(IDfSession currentSession, String workflowObjectId)
    {
        int i_runtime_status = -1;
        try
        {
            if(workflowObjectId != null)
            {
                if(WorkflowService.isWorkflowId(workflowObjectId))
                {
                    IDfWorkflow tIDfWorkflow = (IDfWorkflow)currentSession.getObject(new DfId(workflowObjectId));
                    i_runtime_status = tIDfWorkflow.getRuntimeState();
                    porticoOutput(0, "getWorkflowRunTimeStatus()-Valid workflow ID="+workflowObjectId);
                }
                else
                {
                    porticoOutput(1, "Error in HelperClass-getWorkflowRunTimeStatus()-Not a valid workflow ID="+workflowObjectId);
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getWorkflowRunTimeStatus()="+e.toString());
            e.printStackTrace();
        }

        return i_runtime_status;
    }

/* Workflow status meaning
Value    Meaning
 0        Dormant
 1        Running
 2        Finished
 3        Halted
 4        Terminated
*/
    // This function currently is not used - can be used for Workflow precondition-start/stop/resume/pause
    public static boolean isValidWorkflowAction(IDfSession currentSession, String doAction, String batchObjectId)
    {
        boolean isValid = false;
        // batchObjectId ("0b0152d480008a60" "WF"), ("0b0152d480008a60", "NO WF")
        String workflowObjectId = getWorkflowObject(currentSession, batchObjectId);
        int i_runtime_status = getWorkflowRunTimeStatus(currentSession, workflowObjectId);

        if(doAction.equals("START"))
        {
            if(i_runtime_status == -1)
            {
                // This batchObjectId is 'Not associated with a wf'
                isValid = true;
            }
        }
        else if(doAction.equals("STOP"))
        {
            // abort
            if(i_runtime_status != -1)
            {
                isValid = WorkflowService.canAbort(workflowObjectId, i_runtime_status, null);
            }
        }
        else if(doAction.equals("RESUME"))
        {
            // resume
            if(i_runtime_status != -1)
            {
                isValid = WorkflowService.canResume(workflowObjectId, i_runtime_status, null);
            }
        }
        else if(doAction.equals("PAUSE"))
        {
            // halt
            if(i_runtime_status != -1)
            {
                isValid = WorkflowService.canHalt(workflowObjectId, i_runtime_status, null);
            }
        }
        else
        {
            porticoOutput(1, "HelperClass-isValidWokflowAction:Unknown Action="+ doAction);
        }

        return isValid;
    }

    public static String getStatusForBatchObject(IDfSession currentSession, String batchObjectId)
    {
        String batchStatus = "";
        porticoOutput(0, "HelperClass-getStatusForBatchObject(Started-From Storage) for Batch_ID="+ batchObjectId);
        try
        {
            if(batchObjectId != null && !batchObjectId.equals(""))
            {
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                batchStatus = iDfSysObject.getString(HelperClassConstants.BATCH_STATE);
                porticoOutput(0, "HelperClass-getStatusForBatchObject:batchStatus="+ batchStatus);
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getStatusForBatchObject()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        porticoOutput(0, "HelperClass-getStatusForBatchObject(Ended-From Storage):Batch_ID="+ batchObjectId + " batchStatus="+batchStatus);

        return batchStatus;
    }

    public static String getBatchPerformer(IDfSession currentSession, String batchObjectId)
    {
        String batchPerformer = "";
        porticoOutput(0, "HelperClass-getBatchPerformer(Started-From Storage) for Batch_ID="+ batchObjectId);
        try
        {
            if(batchObjectId != null && !batchObjectId.equals(""))
            {
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                batchPerformer = iDfSysObject.getString("p_performer");
                porticoOutput(0, "HelperClass-getBatchPerformer:batchPerformer="+ batchPerformer);
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getBatchPerformer()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        porticoOutput(0, "HelperClass-getBatchPerformer(Ended-From Storage):Batch_ID="+ batchObjectId + " batchPerformer="+batchPerformer);

        return batchPerformer;
    }

    public static int getAssetCountForBatchObject(IDfSession currentSession, String batchObjectId)
    {
        int assetCount = 0;
        porticoOutput(0, "HelperClass-getAssetCountForBatchObject OPEN IDfCollection");
        IDfCollection tIDfCollection = null;
        String attrNames = "r_object_type";
        try
        {
            IDfFolder iIDfFolder = (IDfFolder)currentSession.getObject(new DfId(batchObjectId));
            tIDfCollection = iIDfFolder.getContents(attrNames);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    IDfTypedObject tChildIDfTypedObject = tIDfCollection.getTypedObject();
                    String contentObjectType = tChildIDfTypedObject.getString("r_object_type");
                    if(contentObjectType.equalsIgnoreCase(DBHelperClass.RAW_UNIT_TYPE))
                    {
                        assetCount++;
                    }
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception-HelperClass-getAssetCountForBatchObject():"+e.toString());
        }
        finally
        {
            try
            {
                if(tIDfCollection != null)
                {
                    tIDfCollection.close();
                }
                porticoOutput(0, "HelperClass-getAssetCountForBatchObject CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getAssetCountForBatchObject-close" + e.toString());
            }
        }

        porticoOutput(0, "HelperClass-getAssetCountForBatchObject Batch_ID-assetCount="+batchObjectId+"-"+assetCount);

        return assetCount;
    }

    public static boolean isValidSubmissionViewAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;
        porticoOutput(0, "HelperClass-isValidSubmissionViewAction(Started):batchObjectId="+ batchObjectId);
        String batchStatus = QcHelperClass.getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
        if(!batchStatus.equalsIgnoreCase(HelperClassConstants.WITHDRAWN) &&
             !batchStatus.equalsIgnoreCase(HelperClassConstants.RETAINED))
        {
            isValid = true;
        }
        porticoOutput(0, "HelperClass-isValidSubmissionViewAction:batchObjectId="+ batchObjectId + " isValid="+isValid);
        return isValid;
    }


    public static boolean isValidProcessViewAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;
        porticoOutput(0, "HelperClass-isValidProcessViewAction(Started):batchObjectId="+ batchObjectId);
        String batchStatus = QcHelperClass.getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
        if(!batchStatus.equalsIgnoreCase(HelperClassConstants.WITHDRAWN) &&
               !batchStatus.equalsIgnoreCase(HelperClassConstants.LOADED) &&
               // !batchStatus.equalsIgnoreCase("ON_HOLD") &&
               !batchStatus.equalsIgnoreCase(HelperClassConstants.QUEUED) &&
               !batchStatus.equalsIgnoreCase(HelperClassConstants.RETAINED))
        {
            isValid = true;
        }
        porticoOutput(0, "HelperClass-isValidProcessViewAction:batchObjectId="+ batchObjectId + " isValid="+isValid);
        return isValid;
    }

    public static boolean isValidWithdrawBatchAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
      boolean isValid = false;
      porticoOutput(0, "HelperClass-isValidWithdrawBatchAction(Started):batchObjectId="+ batchObjectId);
      try
      {
		  String lastActivity = getLastActivity(currentSession, batchObjectId, addlnInfo);
          if(lastActivity.equalsIgnoreCase("Ingest To Archive") ||
			    lastActivity.equalsIgnoreCase("Clean Up"))
		  {
			  // DO NOT do the delete(withdraw) action JIRA - CONPREP-1211, UI batch delete change
		  }
		  else
		  {
              String batchStatus = QcHelperClass.getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
              if(batchStatus.equalsIgnoreCase(HelperClassConstants.LOADED) ||
                   batchStatus.equalsIgnoreCase(HelperClassConstants.NEW))
              {
                  isValid=true;
              }
              else
              {
                  if(batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
                       batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
                       batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
                  {
		        		String batchPerformer = QcHelperClass.getBatchPerformer(currentSession, batchObjectId, addlnInfo);
                        if(batchPerformer.equals(currentSession.getLoginUserName()))
                        {
                            isValid=true;
                        }
                  }
                  else
                  {
                      if(batchStatus.equalsIgnoreCase(HelperClassConstants.PROBLEM) ||
                            batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
                            batchStatus.equalsIgnoreCase(HelperClassConstants.RELEASED) ||
                            batchStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
                      {
                          isValid=true;
                      }
                  }
              }
	      }
      }
      catch (DfException e)
      {
          porticoOutput(1, "Exception in HelperClass-isValidWithdrawBatchAction-"+e.toString());
      }
      finally
      {
      }
      porticoOutput(0, "HelperClass-isValidWithdrawBatchAction:batchObjectId="+ batchObjectId + " isValid="+isValid);
      return isValid;
    }

    public static String getLastActivity(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
		String lastActivity = "";
		try
		{
			if(addlnInfo != null &&
			    addlnInfo.containsKey(HelperClassConstants.BATCHLASTACTIVITY))
			{
                lastActivity = (String)addlnInfo.get(HelperClassConstants.BATCHLASTACTIVITY);
			}
            if(lastActivity == null || lastActivity.equals(""))
			{
				HelperClass.porticoOutput(0, "HelperClass-getLastActivity(NOT IN CACHE)-for Batch_ID="+ batchObjectId);
				if(batchObjectId != null)
				{
			    	lastActivity = getLastActivityFromStorage(currentSession, batchObjectId);
			    }
			}
		}
		catch(Exception e)
		{
	        porticoOutput(1, "Exception in HelperClass-getLastActivity()="+e.toString());
	        e.printStackTrace();
		}
		finally
		{
		}

		return lastActivity;
	}

	public static String getLastActivityFromStorage(IDfSession currentSession, String batchObjectId)
	{
	    String lastActivity = "";
	    porticoOutput(0, "HelperClass-getLastActivityFromStorage(Started-From Storage) for Batch_ID="+ batchObjectId);
	    try
	    {
	        if(batchObjectId != null && !batchObjectId.equals(""))
	        {
	            IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
	            lastActivity = iDfSysObject.getString("p_last_activity");
	            porticoOutput(0, "HelperClass-getLastActivityFromStorage:lastActivity="+ lastActivity);
	        }
	    }
	    catch(Exception e)
	    {
	        porticoOutput(1, "Exception in HelperClass-getLastActivityFromStorage()="+e.toString());
	        e.printStackTrace();
	    }
	    finally
	    {
	    }
	    porticoOutput(0, "HelperClass-getLastActivityFromStorage(Ended-From Storage):Batch_ID="+ batchObjectId + " lastActivity="+lastActivity);

	    return lastActivity;
	}


    public static boolean isValidPruneBatchAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;
        porticoOutput(0, "HelperClass-isValidPruneBatchAction(Started):batchObjectId="+ batchObjectId);
        String batchStatus = QcHelperClass.getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
        if(batchStatus.equalsIgnoreCase(HelperClassConstants.LOADED) ||
               batchStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM) ||
               batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
               // batchStatus.equalsIgnoreCase("ON_HOLD"))
        {
            isValid = true;
        }
        porticoOutput(0, "HelperClass-isValidPruneBatchAction:batchObjectId="+ batchObjectId + " isValid="+isValid);
        return isValid;
    }

    // This method not called because, in the action config it is directed to 'view:webtop/config/dm_sysobject_actions.xml'
/*
    public static boolean isValidViewContentAction(IDfSession currentSession, String objectId, Hashtable addlnInfo)
    {
        boolean isValid = true;
        String submissionBatchObjectType = getInternalObjectType("submission_batch");
        String rawUnitObjectType = getInternalObjectType("raw_unit");
        String currentObjectType = getObjectType(currentSession, objectId);

        if(currentObjectType.equals(submissionBatchObjectType))
        {
            String batchStatus = getStatusForBatchObject(currentSession, objectId);
            if(batchStatus.equalsIgnoreCase(getStatusMapping(submissionBatchObjectType, HelperClassConstants.WITHDRAWN)))
            {
                isValid = false;
            }
        }
        else if(currentObjectType.equals(rawUnitObjectType))
        {
            isValid = false;
        }
        porticoOutput(0, "isValidViewContentAction:objectId="+ objectId + " currentObjectType=" + currentObjectType + " isValid="+isValid);
        return isValid;
    }
*/
    // We are interested in only IDfSysObject and their types
    // To be changed for New(oracle) datamodel
    // We can afford to do this because, it is not part of preconditions due to 'scope' in action config file
    public static String getObjectType(IDfSession currentSession, String objectId)
    {
        porticoOutput(0, "HelperClass-getObjectType(Started) objectId="+objectId);
        String objectType = "";
        IDfCollection idfcollection = null;
        try
        {
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL("SELECT r_object_type FROM dm_sysobject WHERE r_object_id='" + objectId + "'");
            porticoOutput(0, "HelperClass-getObjectType OPEN IDfCollection");
            for(idfcollection = dfquery.execute(currentSession, 0); idfcollection.next();)
            {
                objectType = idfcollection.getString("r_object_type");
                porticoOutput(0, "HelperClass-getObjectType:objectType="+ objectType);
                break;
            }

            if(objectType == null || objectType.equals(""))
            {
				objectType = DBHelperClass.getObjectType(objectId);
			}
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getObjectType()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getObjectType- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getObjectType()-close="+e.toString());
                e.printStackTrace();
            }
        }
        porticoOutput(0, "HelperClass-getObjectType(Ended) objectId="+ objectId + " objectType="+objectType);

        return objectType;
    }

/*
    public static String getObjectType(IDfSession currentSession, String objectId)
    {
        String objectType = "";
        porticoOutput(0, "HelperClass-getObjectType:Started(From Storage) for objectId="+ objectId);
        try
        {
            // "0000000000000000" probably is not a valid id, this is passed as the
            // ObjectId when we invoke the 'Track Progress' component - default
            // documentum component
            if(null != objectId && !objectId.equals("") && !objectId.equals("0000000000000000"))
            {
                IDfId id = new DfId(objectId);
                if(null != id)
                {
                    IDfPersistentObject iDfPersistentObject = (IDfPersistentObject)currentSession.getObject(id);
                    if(iDfPersistentObject.hasAttr("r_object_type") == true)
                    {
                        objectType = iDfPersistentObject.getString("r_object_type");
                    }
                }
            }
            porticoOutput(0, "getObjectType:objectType="+ objectType);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getObjectType(), objectId="+ objectId + " " + e.toString());
            // e.printStackTrace();
        }
        finally
        {
        }
        porticoOutput(0, "getObjectType:Ended(From Storage)objectId="+ objectId + " objectType="+objectType);

        return objectType;
    }
*/


/*
    public static String getObjectName(IDfSession currentSession, String objectId)
    {
        String objectName = "";
        IDfCollection idfcollection = null;
        try
        {
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL("SELECT object_name FROM dm_sysobject WHERE r_object_id='" + objectId + "'");
            porticoOutput(0, "getObjectName OPEN IDfCollection");
            for(idfcollection = dfquery.execute(currentSession, 0); idfcollection.next();)
            {
                objectName = idfcollection.getString("object_name");
                porticoOutput(0, "getObjectName:objectName="+ objectName);
                break;
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in getObjectName()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "getObjectName- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in getObjectName()-close="+e.toString());
                e.printStackTrace();
            }
        }
        porticoOutput(0, "getObjectName:objectId="+ objectId + " objectName="+objectName);

        return objectName;
    }
*/

    public static String getObjectName(IDfSession currentSession, String objectId, String objectType)
    {
        String objectName = "";
        try
        {
			if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE))
			{
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
                objectName = iDfSysObject.getObjectName();
		    }
		    else if(objectType.equalsIgnoreCase(DBHelperClass.CU_TYPE) ||
		               objectType.equalsIgnoreCase(DBHelperClass.FU_TYPE) ||
		               objectType.equalsIgnoreCase(DBHelperClass.SU_TYPE))
		    {
				ArrayList alistIn = new ArrayList();
				alistIn.add(DBHelperClass.P_NAME);
				Hashtable alistOut = DBHelperClass. getObjectAttributes(objectType, objectId, alistIn);
				if(alistOut != null && alistOut.containsKey(DBHelperClass.P_NAME))
				{
					objectName = (String)alistOut.get(DBHelperClass.P_NAME);
				}
			}
            porticoOutput(0, "HelperClass-getObjectName:objectName="+ objectName);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getObjectName()="+e.toString());
            //e.printStackTrace();
        }
        finally
        {
        }
        porticoOutput(0, "HelperClass-getObjectName:objectId="+ objectId + " objectName="+objectName);

        return objectName;
    }


    public static boolean isValidReportDetailsListAction(IDfSession currentSession, String workflowObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;
        int workflowStatus = getWorkflowRunTimeStatus(currentSession, workflowObjectId);
        if(workflowStatus == IDfWorkflow.DF_WF_STATE_DORMANT ||
              workflowStatus == IDfWorkflow.DF_WF_STATE_HALTED ||
              workflowStatus == IDfWorkflow.DF_WF_STATE_RUNNING)
        {
            isValid = true;
        }
        porticoOutput(0, "HelperClass-isValidReportDetailsListAction:workflowId=" + workflowObjectId + " isValid="+isValid);
        return isValid;
    }

    public static ArrayList getRequiredAttributes(IDfSession currentSession, String objectType, ArrayList attrNames)
    {
        ArrayList requiredAttributes = new ArrayList();
        IDfCollection idfcollection = null;
        String attrString = "";
        try
        {
			if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE) ||
			       objectType.equalsIgnoreCase(DBHelperClass.RAW_UNIT_TYPE))
			{
                if(attrNames != null)
                {
                    for(int aindx=0; aindx < attrNames.size(); aindx++)
                    {
                        if(attrString.equals(""))
                        {
                        }
                        else
                        {
                            attrString = attrString + ",";
                        }
                        attrString = attrString + "'" + (String)attrNames.get(aindx) +"'";
                    }
                    String dqlString = "SELECT attr_name FROM dmi_dd_attr_info WHERE type_name='" + objectType + "'"
                                     + " AND attr_name IN "
                                     + "("
                                     + attrString
                                     +")"
                                     +" AND "
                                     + "not_null="
                                     + "'"
                                     + "1.0"
                                     + "'";
                    porticoOutput(0, "HelperClass-getRequiredAttributes:dqlString="+ dqlString);
                    DfQuery dfquery = new DfQuery();
                    dfquery.setDQL(dqlString);
                    porticoOutput(0, "HelperClass-getRequiredAttributes OPEN IDfCollection");
                    for(idfcollection = dfquery.execute(currentSession, 0); idfcollection.next();)
                    {
                        String tAttrName = idfcollection.getString("attr_name");
                        requiredAttributes.add(tAttrName);
                        porticoOutput(0, "HelperClass-getRequiredAttributes:Required attr_name="+ tAttrName);
                    }
                }
		    }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getRequiredAttributes()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getRequiredAttributes- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getRequiredAttributes()-close="+e.toString());
                e.printStackTrace();
            }
        }

        return requiredAttributes;
    }

/* To be removed, all other references will just use the correct status string
    public static String getStatusMapping(String objectType, String statusLike)
    {
        String retStatus = statusLike;
        String submissionBatchObjectType = getInternalObjectType("submission_batch");

        if(objectType.equals(submissionBatchObjectType))
        {
            if(retStatus.equalsIgnoreCase(HelperClassConstants.NEW))
            {
                retStatus = HelperClassConstants.NEW;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.LOADED))
            {
                retStatus = HelperClassConstants.LOADED;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.WITHDRAWN))
            {
                retStatus = HelperClassConstants.WITHDRAWN;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.QUEUED))
            {
                retStatus = HelperClassConstants.QUEUED;
            }
            else if(retStatus.equalsIgnoreCase("ON_HOLD"))
            {
                retStatus = "ON_HOLD";
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.RESOLVING_PROBLEM))
            {
                retStatus = HelperClassConstants.RESOLVING_PROBLEM;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING))
            {
                retStatus = HelperClassConstants.INSPECTING;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.SYSTEM_ERROR))
            {
                retStatus = HelperClassConstants.SYSTEM_ERROR;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.RELEASED))
            {
                retStatus = HelperClassConstants.RELEASED;
            }
            else if(retStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
            {
                retStatus = HelperClassConstants.INSPECTED;
            }
        }
        return retStatus;
    }
*/

    public static String getStatusActualValue(IDfSession currentSession, String objectType, String statusLike)
    {
        String retStatus = statusLike;

        if(objectType.equalsIgnoreCase(DBHelperClass.BATCH_TYPE) ||
                 objectType.equalsIgnoreCase(DBHelperClass.RAW_UNIT_TYPE))
        {
            ArrayList listStatusUI = HelperClass.getValueAssistanceListFromDocbase(currentSession, objectType, HelperClassConstants.BATCH_STATE);
            if(listStatusUI != null)
            {
                for(int indx=0; indx < listStatusUI.size(); indx++)
                {
                    ValuePair tPorticoValuePair = (ValuePair)listStatusUI.get(indx);
                    String thisKey = (String)tPorticoValuePair.getKey();
                    String thisValue = (String)tPorticoValuePair.getValue();
                    porticoOutput(0, "HelperClass-Status key="+thisKey);
                    porticoOutput(0, "HelperClass-Status value="+thisValue);
                    if(thisValue.equalsIgnoreCase(retStatus))
                    {
                        retStatus = thisKey;
                        porticoOutput(0, "HelperClass-getStatusActualValue(Matched)="+retStatus);
                        break;
                    }
                }
            }
	    }
        porticoOutput(0, "HelperClass-getStatusActualValue(Final)="+retStatus);
        return retStatus;
    }

    public static String getMimeType(String fileNameWithPath)
    {
        String mimetype = "";
        String extn = "";
        try
        {
            extn = UploadUtil.extractExtension(fileNameWithPath);
            porticoOutput(0, "HelperClass-getMimeType(filename:extn)="+fileNameWithPath + ":" +extn);
            mimetype = getMimeTypeInfo(extn);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass - getMimeType="+fileNameWithPath + ":" +extn+":"+e.toString());
        }
        finally
        {
        }
        return mimetype;
    }

    // input c:/vol/a.pdf, output c:/vol
    public static String getFilePathFromAbsoluteName(String fileName)
    {
        String winString = "\\";
        String unixString = "/";
        int indx = -1;
        String filePath = "";
        indx = fileName.lastIndexOf(unixString);
        if(indx == -1)
        {
            indx = fileName.lastIndexOf(winString);
        }
        if(indx != -1)
        {
            filePath = fileName.substring(0, indx);
        }
        porticoOutput(0, "HelperClass - getFilePathFromAbsoluteName="+filePath);
        return filePath;
    }

    // input 'a.pdf' , output 'a'
    public static String getFileNameWithoutExtn(String fileName)
    {
        int indx = -1;
        String filenameNoExtn = fileName;
        indx = fileName.lastIndexOf(".");
        if(indx != -1)
        {
            filenameNoExtn = fileName.substring(0, indx);
        }
        porticoOutput(0, "HelperClass - getFileNameWithoutExtn="+filenameNoExtn);
        return filenameNoExtn;
    }

    // input c:/a.pdf , output a.pdf
    public static String getFileNameFromAbsoluteName(String fileName)
    {
        String winString = "\\";
        String unixString = "/";
        int indx = -1;
        String filenameWithExtn = fileName;
        indx = fileName.lastIndexOf(unixString);
        if(indx == -1)
        {
            indx = fileName.lastIndexOf(winString);
        }
        if(indx != -1)
        {
            //filenameWithExtn = fileName.substring(0, indx);
            filenameWithExtn = fileName.substring(indx+1);
        }
        porticoOutput(0, "HelperClass - getFileNameFromAbsoluteName="+filenameWithExtn);
        return filenameWithExtn;
    }

    public static String getParentBatchFolderId(IDfSession currentSession, String objectId)
    {
        String parentBatchFolderId = "";

        porticoOutput(0, "HelperClass-getParentBatchFolderId()-Started for objectId="+objectId);

        if(objectId != null && !objectId.equals(""))
        {
            try
            {
				if(isBatchObject(currentSession, objectId))
				{
                    parentBatchFolderId = objectId;
			    }
			    else
                {
					// 'DBHelperClass.getParentBatchObjectId' returns the Documentum Batch Id from Oracle DB
                    parentBatchFolderId = DBHelperClass.getParentBatchObjectId(objectId);
                }
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getParentBatchFolderId()="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getParentBatchFolderId()-End for objectId,parentBatchFolderId="+objectId+","+parentBatchFolderId);

        return parentBatchFolderId;
    }

    public static boolean isBatchObject(IDfSession currentSession, String objectId)
    {
		boolean isValid = false;

		HelperClass.porticoOutput(0, "HelperClass-isBatchObject(Start)-objectId="+objectId);

		try
		{
            String qualification = "p_batch WHERE r_object_id ="+  "'" + objectId + "'";
	        HelperClass.porticoOutput(0, "HelperClass-isBatchObject-qualification="+qualification);
            IDfPersistentObject batchObject = (IDfPersistentObject) currentSession.getObjectByQualification(qualification);
            if(batchObject != null)
            {
				isValid = true;
		    }

		}
		catch(Exception e)
		{
            porticoOutput(1, "Exception in HelperClass-isBatchObject()="+e.toString());
            e.printStackTrace();
		}
		finally
		{
		}

		HelperClass.porticoOutput(0, "HelperClass-isBatchObject(End)-objectId,isValid="+objectId+","+isValid);

		return isValid;
	}

// Commented, has been changed for new(oracle) datamodel
/*
    public static String getParentBatchFolderId(IDfSession currentSession, String objectId)
    {
        String parentBatchFolderId = "";

        porticoOutput(0, "HelperClass-getParentBatchFolderId()-Started for objectId="+objectId);

        if(objectId != null && !objectId.equals(""))
        {
            try
            {
                parentBatchFolderId = getBatchObjectFromId(currentSession, objectId);
                if(parentBatchFolderId == null || parentBatchFolderId.equals(""))
                {
                    parentBatchFolderId = getParentBatchFolderIdEx(currentSession, objectId);
                }
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getParentBatchFolderId()="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getParentBatchFolderId()-End for objectId,parentBatchFolderId="+objectId+","+parentBatchFolderId);

        return parentBatchFolderId;
    }

    public static String getParentBatchFolderIdEx(IDfSession currentSession, String objectId)
    {
        String parentBatchFolderId = "";
        IDfCollection idfcollection = null;

        porticoOutput(0, "HelperClass-getParentBatchFolderIdEx()-Started for objectId="+objectId);

        try
        {
            // Note: For some reason give both r_object_id and i_folder_id
            String dqlString = "SELECT r_object_id, i_folder_id from dm_sysobject where r_object_id=" + "'" + objectId + "'";
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL(dqlString);
            porticoOutput(0, "HelperClass-getParentBatchFolderIdEx:dqlString="+ dqlString);
            porticoOutput(0, "HelperClass-getParentBatchFolderIdEx OPEN IDfCollection");
            for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
            {
                String parentFolderId = idfcollection.getString("i_folder_id");
                porticoOutput(0, "HelperClass-getParentBatchFolderIdEx:parentFolderId="+ parentFolderId);
                if(parentFolderId != null)
                {
                    String tParentBatchFolderId = getBatchObjectFromId(currentSession, parentFolderId);
                    if(tParentBatchFolderId == null || tParentBatchFolderId.equals(""))
                    {
                        parentBatchFolderId = getParentBatchFolderIdEx(currentSession, parentFolderId);
                    }
                    else
                    {
                        // We got the Batch folder Id
                        parentBatchFolderId = tParentBatchFolderId;
                        break;
                    }
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getParentBatchFolderIdEx()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getParentBatchFolderIdEx- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getParentBatchFolderIdEx()-close="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getParentBatchFolderIdEx()-End for objectId="+objectId);

        return parentBatchFolderId;
    }

    public static String getBatchObjectFromId(IDfSession currentSession, String objectId)
    {
        String batchFolderId = "";

        porticoOutput(0, "HelperClass-getBatchObjectFromId()-Started for objectId="+objectId);

        if(objectId != null)
        {
            try
            {
                IDfPersistentObject iDfPersistentObject = (IDfPersistentObject)currentSession.getObject(new DfId(objectId));
                if(iDfPersistentObject != null)
                {
                    if(iDfPersistentObject.hasAttr("r_object_type") == true)
                    {
                        String objectType = iDfPersistentObject.getString("r_object_type");
                        if(objectType.equals(getInternalObjectType("submission_batch")))
                        {
                            batchFolderId = objectId;
                        }
                    }
                    if(batchFolderId == null || batchFolderId.equals(""))
                    {
                        if(iDfPersistentObject.hasAttr("p_batch_id") == true) // a_category
                        {
                            batchFolderId = iDfPersistentObject.getString("p_batch_id");
                        }
                    }
                    if(batchFolderId == null || batchFolderId.equals(""))
                    {
                        porticoOutput(0, "WARNING HelperClass-getBatchObjectFromId()-p_batch_id not populated(use recursion)for objectId="+objectId);
                    }
                }
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getBatchObjectFromId()="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getBatchObjectFromId()-End for objectId,batchFolderId="+objectId+","+batchFolderId);

        return batchFolderId;
    }
*/

    public static String getSubmissionAreaName(IDfSession currentSession)
    {
        String submissionAreaName = "";
        String componentId = "configinfo";
        String elementName = "submission_area_name";
        try
        {
            // Pick from config file
            IConfigElement iconfigelementForBatch = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            submissionAreaName = iconfigelementForBatch.getChildValue(elementName);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getSubmissionAreaName:"+e.toString());
            e.printStackTrace();
        }

        porticoOutput(0, "HelperClass-getSubmissionAreaName()="+submissionAreaName);

        return submissionAreaName;
    }


    public static String getIdFromNameType(IDfSession currentSession, String objectName, String objectType)
    {
        String objectId = "";
        IDfCollection idfcollection = null;

        String dqlString = "SELECT r_object_id from dm_sysobject where " +
                           "object_name=" + "'" + objectName + "'" +
                           " and " +
                           "r_object_type=" +
                           "'" + objectType + "'";
        try
        {
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL(dqlString);
            porticoOutput(0, "HelperClass-getIdFromNameType:dqlString="+ dqlString);
            porticoOutput(0, "HelperClass-getIdFromNameType OPEN IDfCollection");
            for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
            {
                objectId = idfcollection.getString("r_object_id");
                porticoOutput(0, "HelperClass-getIdFromNameType:objectId="+ objectId);
                break;
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getIdFromNameType()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getIdFromNameType- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getIdFromNameType()-close="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getIdFromNameType:objectName="+ objectName + " objectId="+objectId);

        return objectId;
    }

    public static String getSubmissionAreaId(IDfSession currentSession)
    {
        String submissionAreaId = "";

        submissionAreaId = getIdFromNameType(currentSession, getSubmissionAreaName(currentSession), getInternalObjectType("submission_area_folder"));

        porticoOutput(0, "HelperClass-getSubmissionAreaId()="+submissionAreaId);

        return submissionAreaId;
    }

// In Main, call createCabinetObject(session, getSubmissionAreaName(session))
    public static String createCabinetObject(IDfSession currentSession, String objectName)
    {
        String objectId = "";
        String objectType = "dm_cabinet";
        try
        {
            objectId = getIdFromNameType(currentSession, objectName, objectType);
            if(objectId == null || objectId.equals(""))
            {
                IDfSysObject idfsysobject = (IDfSysObject)currentSession.newObject(objectType);
                idfsysobject.setObjectName(objectName);
                // idfsysobject.setWorldPermit(IDfACL.DF_PERMIT_WRITE);
                idfsysobject.save();
                porticoOutput(0, "HelperClass - createCabinetObject-saved");
                IDfId tIDfId = idfsysobject.getObjectId();
                objectId = tIDfId.getId();
            }
            else
            {
                porticoOutput(0, "HelperClass - createCabinetObject-Object already present");
            }
        }
        catch(Exception exception)
        {
            porticoOutput(1, "Exception in HelperClass - createCabinetObject="+exception.toString());
        }
        finally
        {
            // Release session if relevant
        }

        porticoOutput(0, "HelperClass - createCabinetObject-Done-objectId="+objectId);

        return objectId;
    }

    // Shared method with List Meta data, QcHelperClass-getCuStateList-for LinkFuToCu action
/*
    public static void populateContentUnitStateInfo(IDfSession currentSession, String batchObjectId, ArrayList resultList)
    {
        MetadataStructureUI metadataStructureUI = null;
        IDfCollection tIDfCollection = null;
        try
        {
            String workPadId = QcHelperClass.getWorkPadId(currentSession, batchObjectId);
            String attrNames = "r_object_type,r_object_id,object_name,title,p_display_label,p_sort_key";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + getInternalObjectType("cu_state") +
                               " where FOLDER(ID(" + "'"+workPadId+"'" +
                               ")) order by p_sort_key ASC";

            porticoOutput(0, "HelperClass-populateContentUnitStateInfo()-dqlString="+dqlString);

            dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    metadataStructureUI = new MetadataStructureUI();
                    metadataStructureUI.setObjectID(tIDfCollection.getString("r_object_id"));
                    metadataStructureUI.setObjectType(tIDfCollection.getString("r_object_type"));
                    metadataStructureUI.setObjectName(tIDfCollection.getString("p_display_label"));
                    metadataStructureUI.setObjectSorkKey(tIDfCollection.getString("p_sort_key"));
                    resultList.add(metadataStructureUI);
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception-HelperClass-populateContentUnitStateInfo():"+e.toString());
        }
        finally
        {
            try
            {
                if(tIDfCollection != null)
                {
                    tIDfCollection.close();
                }
                porticoOutput(0, "HelperClass-populateContentUnitStateInfo CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-populateContentUnitStateInfo-close" + e.toString());
            }
        }
    }
*/

    // used in import related components
    public static String massageFilePathName(String filePathName)
    {
        String massagedString = filePathName;
        char winChar = '\\';
        char unixChar = '/';
        String winString = "\\";
        String regex = "\\w:\\\\"; // Matching pattern like c:\ or z:\
        String subStr = "";

        if(massagedString.indexOf(winString) != -1)
        {
            subStr = massagedString.substring(0,3);
            if(subStr.matches(regex))
            {
                // Do not remove drive(C:) here 'Prune' will take care later
                // massagedString = massagedString.substring(3);
            }
            massagedString = massagedString.replace(winChar, unixChar);
        }

        porticoOutput(0, "HelperClass - massageFilePathName="+massagedString);

        return massagedString;
    }

    // Note: Given a SU State, it may either be created from a 'p_raw_unit' object(direct file)
    //       or from a 'p_system_unit' object(delayed files from a tar file)
    public static String getRawUnitIdFromSuState(IDfSession currentSession, String suStateId)
    {
        String rawOrSystemUnitId = "";
        ArrayList attrList = new ArrayList();
        attrList.add(DBHelperClass.P_CONTENT_ID);
        Hashtable outList = DBHelperClass.getObjectAttributes(DBHelperClass.SU_TYPE, suStateId, attrList);
        if(outList != null && outList.size() > 0)
        {
			if(outList.containsKey(DBHelperClass.P_CONTENT_ID))
			{
				rawOrSystemUnitId = (String)outList.get(DBHelperClass.P_CONTENT_ID);
			}
        }

        return rawOrSystemUnitId;
    }

    public static boolean postProcessingForWithdraw(IDfSession currentSession, String batchObjectId)
    {
        porticoOutput(0, "HelperClass-postProcessingForWithdraw-Call-Started-batchObjectId="+batchObjectId);

        boolean isSuccessful = false;
        String batchNamePrefix = "[DELETED]";

        try
        {
            IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
            String batchStatus = iDfSysObject.getString(HelperClassConstants.BATCH_STATE);
            if(batchStatus.equalsIgnoreCase(HelperClassConstants.NEW))
            {
                batchNamePrefix = "[NEW]";
            }
            String batchNewName = batchNamePrefix + iDfSysObject.getObjectName();
            iDfSysObject.setObjectName(batchNewName);
            iDfSysObject.setString("p_rawunit_count", "0");
            iDfSysObject.setString(HelperClassConstants.BATCH_STATE, HelperClassConstants.WITHDRAWN);
            iDfSysObject.save();

            isSuccessful = true;
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception-HelperClass-postProcessingForWithdraw():"+e.toString());
        }
        finally
        {
        }

        porticoOutput(0, "HelperClass-postProcessingForWithdraw-Call-End-isSuccessful="+isSuccessful);

        return isSuccessful;
    }

/*
    public static boolean markObjectsForDelete(IDfSession currentSession, String objectId, boolean deleteMe)
    {
        boolean isSuccessful = false;
        IDfCollection tIDfCollection = null;

        porticoOutput(0, "HelperClass-markObjectsForDelete-Call-Started-objectId="+objectId);

        try
        {
            String deleteFolder = getDeleteFolder(currentSession);
            HelperClass.porticoOutput(0, "HelperClass- markObjectsForDelete deleteFolder="+deleteFolder);
            if(deleteFolder != null && !deleteFolder.equals(""))
            {
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
                String objectName = iDfSysObject.getObjectName();

                DfTime dfTime = new DfTime(); // DF_TIME_PATTERN44 has '/' cannot be used as foldername, move complains
                String day = ""+dfTime.getDay(); // Will improve uniqueness for folder creation
                String appendRandomString = objectName + " " + day + " " + dfTime.asString(DfTime.DF_TIME_PATTERN40); // DF_TIME_PATTERN40

                HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete()-appendRandomString="+appendRandomString);

                IDfSysObject randomDeleteSubFolder = (IDfSysObject)currentSession.newObject("dm_folder");
                randomDeleteSubFolder.setObjectName(appendRandomString);
                randomDeleteSubFolder.link(deleteFolder);
                randomDeleteSubFolder.save();

                String randomDeleteSubFolderName = deleteFolder + "/" + appendRandomString;

                String whereClause = "";
                if(deleteMe == true)
                {
                    // move just the top batch folder
                    whereClause = " r_object_id=" + "'" + objectId + "'";
                }
                else
                {
                    // move only the children of the top batch/object folder
                    whereClause = " FOLDER(ID(" + "'" + objectId + "'" + "))";
                }

                // update dm_document objects link <new cabinet>, unlink <old cabinet> where ...
                // move all the dm_sysobject(s) under FOLDER ID '0b0152d480051ae8' to '/Temp/delete_folder'
                // folder
                // update dm_sysobject objects move '/Temp/delete_folder' where FOLDER(ID('0b0152d480051ae8'))

//                DfQuery dfquery = new DfQuery();
//                String dqlString = "UPDATE dm_sysobject OBJECTS move " + "'" + randomDeleteSubFolderName + "'" +
//                                   " where " + whereClause;
//                HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete()-dqlString="+dqlString);
//                dfquery.setDQL(dqlString);
//                tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_EXEC_QUERY);
//                if(tIDfCollection != null)
//                {
//                    while(tIDfCollection.next())
//                    {
//                        break;
//                    }
//                }

                DfQuery dfquery = new DfQuery();
                String dqlString = "SELECT r_object_id, r_object_type FROM dm_sysobject " +
                                   " where " + whereClause;

                HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete()-dqlString="+dqlString);

                dfquery.setDQL(dqlString);
                tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
                if(tIDfCollection != null)
                {
                    while(tIDfCollection.next())
                    {

                        String tempObjectId = tIDfCollection.getString("r_object_id");
                        HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete()- moving object ="+tempObjectId);
                        IDfSysObject tempObject = (IDfSysObject)currentSession.getObject( new DfId(tempObjectId) );

                        String presentFolderId = tempObject.getFolderId(0).toString();

                        tempObject.link( randomDeleteSubFolder.getObjectId().toString() );

                        tempObject.unlink( presentFolderId );
                        tempObject.save();
                        HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete()- saved object ="+tempObjectId);
                    }
                }


                isSuccessful = true;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            HelperClass.porticoOutput(1, "Exception-HelperClass-markObjectsForDelete():"+e.toString());
        }
        finally
        {
            try
            {
                if(tIDfCollection != null)
                {
                    tIDfCollection.close();
                }
                HelperClass.porticoOutput(0, "HelperClass-markObjectsForDelete() CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                HelperClass.porticoOutput(1, "Exception in HelperClass-markObjectsForDelete()-close" + e.toString());
            }
        }

        porticoOutput(0, "HelperClass-markObjectsForDelete-Call-End-isSuccessful="+isSuccessful);

        return isSuccessful;
    }
*/

    public static boolean isValidTableOfContentAction(IDfSession currentSession, String batchObjectId, Hashtable addlnInfo)
    {
        boolean isValid = false;

        try
        {
            // As per CONPREP-65, TOC has to be accessible Before and After Claims
            String batchStatus = QcHelperClass.getStatusForBatchObject(currentSession, batchObjectId, addlnInfo);
            if(batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECT) ||
                    batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTING) ||
                    batchStatus.equalsIgnoreCase(HelperClassConstants.RELEASED) ||
                    batchStatus.equalsIgnoreCase(HelperClassConstants.POST_PROCESSING) ||
                    batchStatus.equalsIgnoreCase(HelperClassConstants.INGESTED) ||
                    batchStatus.equalsIgnoreCase(HelperClassConstants.INSPECTED))
            {
                // REL_IN_PROGRESS added CONPREP-65
                // Note: Display(Post-Processing) == Actual(RELEASED,REL_IN_PROGRESS)
                isValid = true;
            }
	    }
	    catch(Exception e)
	    {
            porticoOutput(1, "Exception in HelperClass-isValidTableOfContentAction="+e.toString());
            e.printStackTrace();
		}
		finally
		{
		}

        porticoOutput(0, "HelperClass-isValidTableOfContentAction-batchObjectId="+batchObjectId + " isValid="+isValid);

        return isValid;
    }

    public static List getTableOfContent(IDfSession currentSession, String batchObjectId)
    {
        List tableOfContentList = null;

        porticoOutput(0, "HelperClass-getTableOfContent-Call-Started-batchObjectId="+batchObjectId);
        ActionTool actionTool = null;
        try
        {
            porticoOutput(0, "HelperClass-getTableOfContent-Start backend call");
            actionTool = new ActionTool(currentSession, DBHelperClass.getBatchAccessionIdFromBatchId(batchObjectId));
            actionTool.flush();
            tableOfContentList = actionTool.getToc();
            porticoOutput(0, "HelperClass-getTableOfContent-End backend call");
            porticoOutput(0, "HelperClass-getTableOfContent-Call-Ended");
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getTableOfContent="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            porticoOutput(0, "HelperClass-getTableOfContent-Call-finally");
            try
            {
                if(actionTool != null)
                {
                    actionTool.flush();
                    actionTool.clearSessionContext();
                }
            }
            catch(Exception eflush)
            {
            }
        }

        return tableOfContentList;
    }

/*
    public static String getAttrDisplayValue(IDfSession currentSession, String objectId, String attrName, String actualValue)
    {
        String attrDisplayValue = "";
        try
        {
            if(attrName != null && actualValue != null)
            {
                IDfPersistentObject pObj = currentSession.getObject(new DfId(objectId));
                IDfValidator v = pObj.getValidator();
                if(v != null)
                {
                    IDfValueAssistance va = v.getValueAssistance(attrName, null);
                    if(va != null)
                    {
                        attrDisplayValue = va.getDisplayValue(actualValue);
                    }
                }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getAttrDisplayValue="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            porticoOutput(0, "HelperClass-getAttrDisplayValue-Call-finally");
        }

        return attrDisplayValue;
    }
*/

    public static String getReentryPointName(String input)
    {
        String reentryPointName = "";

        if(input.equals("generatemets"))
        {
            reentryPointName = "Generate METS";
        }
        else if(input.equals("generatechecksum"))
        {
            reentryPointName = "Generate Checksum";
        }
        else if(input.equals("generaterandomsample"))
        {
            reentryPointName = "Generate Random Sample";
        }

        return reentryPointName;
    }

// Start users/roles

    // IDfSession not required now, since we store in the wdk config file, but later if the
    // table moves to Documentum it would be required
    public static ArrayList getAvailableUserRoles(IDfSession currentSession)
    {
        ArrayList roleList = new ArrayList();
        ValuePair tPorticoValuePair = null;
        String componentId = "configinfo";
        String elementName = "userroles";
        String childElementName = "userrole";
        try
        {
            // Pick from config file
            IConfigElement iConfigElementUserRoles = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            IConfigElement userrolesElement = iConfigElementUserRoles.getChildElement(elementName);
            Iterator userroleElements = userrolesElement.getChildElements(childElementName);

            while(userroleElements.hasNext())
            {
                IConfigElement iConfigElementUserRole = (IConfigElement)userroleElements.next();
                String value = iConfigElementUserRole.getAttributeValue("value");
                String display = iConfigElementUserRole.getAttributeValue("display");
                tPorticoValuePair = new ValuePair();
                tPorticoValuePair.setKey(value);
                tPorticoValuePair.setValue(display);
                roleList.add(tPorticoValuePair);
                HelperClass.porticoOutput(0, "QcHelperClass-getAvailableUserRoles()-value|display="+value+"|"+display);
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getAvailableUserRoles()="+e.toString());
            e.printStackTrace();
        }

        return roleList;
    }

    public static Hashtable getRolesAndUsers(IDfSession currentSession)
    {
        // roleUserHashTable contains Items of (rolename, userTreeSet)
        Hashtable roleUserHashTable = new Hashtable();
        ArrayList roleList = getAvailableUserRoles(currentSession);
        TreeSet userTreeSet = null;
        ValuePair tPorticoValuePair = null;

        for(int rindx=0; rindx < roleList.size(); rindx++)
        {
            userTreeSet = new TreeSet();
            tPorticoValuePair = (ValuePair)roleList.get(rindx);
            roleUserHashTable.put(tPorticoValuePair.getKey(), userTreeSet);
            HelperClass.porticoOutput(0, "QcHelperClass-Put List - getRolesAndUsers()-roleName=" + tPorticoValuePair.getKey());
        }

// select i_all_users_names, description from dm_group where group_name='conprep_users_group' order by i_all_users_names

        IDfCollection tIDfCollection = null;
        try
        {
            String attrNames = "i_all_users_names,description";
            String tabelName = "dm_group";
            String topLevelGroupName = "conprep_users_group";
            String orderByName = "i_all_users_names";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + tabelName +
                               " where " +
                               " group_name =" + "'" + topLevelGroupName + "'";
                               // " ORDER BY " + orderByName;

            HelperClass.porticoOutput(0, "QcHelperClass-getRolesAndUsers()-dqlString="+dqlString);

            dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    populateRoleUserHashTable(roleUserHashTable, tIDfCollection.getString("i_all_users_names"));
                }
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception-QcHelperClass-getRolesAndUsers():"+e.toString());
        }
        finally
        {
            try
            {
                if(tIDfCollection != null)
                {
                    tIDfCollection.close();
                }
                HelperClass.porticoOutput(0, "QcHelperClass-getRolesAndUsers() CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                HelperClass.porticoOutput(1, "Exception in QcHelperClass-getRolesAndUsers()-close" + e.toString());
            }
        }

        return roleUserHashTable;
    }

    public static void populateRoleUserHashTable(Hashtable roleUserHashTable, String userName)
    {
        boolean userHasRole = false;
        try
        {
            if(roleUserHashTable != null && roleUserHashTable.size() > 0)
            {
                String roleName = "";
                for (Enumeration enumKeys = roleUserHashTable.keys(); enumKeys.hasMoreElements() ;)
                {
                    roleName = (String)enumKeys.nextElement();
                    if(RoleService.isUserAssignedRole(userName, roleName,null, null))
                    {
                        userHasRole = true;
                        ((TreeSet)roleUserHashTable.get(roleName)).add(userName);
                    }
                }

                if(userHasRole == false)
                {
                    HelperClass.porticoOutput(0, "QcHelperClass-populateRoleUserHashTable()-No Available Role for this user="+userName);
                }
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-populateRoleUserHashTable()-" + e.toString());
        }
        finally
        {
        }
    }

// End users/roles

    public static ArrayList getCurrentUserAndGroups(IDfSession currentSession)
    {
        ArrayList currentUserAndGroups = new ArrayList();

// select * from dm_group where any users_names='tester2'

        IDfCollection tIDfCollection = null;
        try
        {
            currentUserAndGroups.add(currentSession.getLoginUserName());
            String attrNames = "group_name";
            String tabelName = "dm_group";
            String whereClause = "any users_names=" + "'" + currentSession.getLoginUserName() +"'";
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT " + attrNames + " FROM " + tabelName +
                               " where " + whereClause;

            HelperClass.porticoOutput(0, "QcHelperClass-getCurrentUserAndGroups()-dqlString="+dqlString);

            dfquery.setDQL(dqlString);
            tIDfCollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY);
            if(tIDfCollection != null)
            {
                while(tIDfCollection.next())
                {
                    currentUserAndGroups.add(tIDfCollection.getString("group_name"));
                }
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception-QcHelperClass-getCurrentUserAndGroups():"+e.toString());
        }
        finally
        {
            try
            {
                if(tIDfCollection != null)
                {
                    tIDfCollection.close();
                }
                HelperClass.porticoOutput(0, "QcHelperClass-getCurrentUserAndGroups() CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                HelperClass.porticoOutput(1, "Exception in QcHelperClass-getCurrentUserAndGroups()-close" + e.toString());
            }
        }

        return currentUserAndGroups;
    }


    public static String getDefaultBatchStatus(IDfSession currentSession)
    {
        String defaultBatchStatus = "";
        ArrayList roleList = new ArrayList();
        String componentId = "configinfo";
        String elementName = "default_batch_status";
        try
        {
            // Pick from config file
            IConfigElement iConfigElement = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            String tDefaultBatchStatus = iConfigElement.getChildValue(elementName);
            if(tDefaultBatchStatus != null)
            {
                // Note: defaultBatchStatus must not be null, it could be "" or any other value
                defaultBatchStatus = tDefaultBatchStatus;
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getDefaultBatchStatus()="+e.toString());
            e.printStackTrace();
        }

        HelperClass.porticoOutput(0, "QcHelperClass-getDefaultBatchStatus()-status="+defaultBatchStatus);

        return defaultBatchStatus;
    }

    public static ArrayList getBatchStatusMappingListFromConfig(IDfSession currentSession)
    {
        ArrayList batchMappingList = new ArrayList();
        ValuePair tPorticoValuePair = null;
        String componentId = "configinfo";
        String elementName = "batch_status_mapping";
        String childElementName = "mapping_pair";
        try
        {
            // Pick from config file
            IConfigElement iConfigElementUserRoles = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            IConfigElement batchStatusMappingElement = iConfigElementUserRoles.getChildElement(elementName);
            Iterator batchStatusMappingPairElements = batchStatusMappingElement.getChildElements(childElementName);

            while(batchStatusMappingPairElements.hasNext())
            {
                IConfigElement batchStatusMappingPairElement = (IConfigElement)batchStatusMappingPairElements.next();
                String value = batchStatusMappingPairElement.getAttributeValue("value");
                String display = batchStatusMappingPairElement.getAttributeValue("display");
                tPorticoValuePair = new ValuePair();
                tPorticoValuePair.setKey(value);
                tPorticoValuePair.setValue(display);
                batchMappingList.add(tPorticoValuePair);
                HelperClass.porticoOutput(0, "QcHelperClass-getBatchStatusMappingListFromConfig()-value|display="+value+"|"+display);
            }
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getBatchStatusMappingListFromConfig()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }

        return batchMappingList;
    }

    public static String getDeleteFolder(IDfSession currentSession)
    {
        String deleteFolderName = "";
        String componentId = "configinfo";
        String elementName = "delete_folder";
        try
        {
            // Pick from config file
            IConfigElement iConfigElementForWithdrawBatch = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            deleteFolderName = iConfigElementForWithdrawBatch.getChildValue(elementName);
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getDeleteFolder:"+e.toString());
            e.printStackTrace();
        }

        porticoOutput(0, "HelperClass-getDeleteFolder()="+deleteFolderName);

        return deleteFolderName;
    }

    // Start Future
    // List of items to populate the search Object List
    public static ArrayList getSearchObjectListInfo()
    {
        String searchString[] = {"Batch","Provider","Profile"};
        ArrayList listSearchObjects = new ArrayList();

        for(int indx=0; indx < searchString.length; indx++)
        {
            listSearchObjects.add(searchString[indx]);
        }

        return listSearchObjects;
    }
    // End Future

    // Remove this, after working on AppSessionContext
    public synchronized static void LoadSessionInformation(IDfSession currentSession)
    {
		// If previously for this complete Application, the App Scoped Object(s) were not loaded then,
		//    load them.
		if(AppSessionContext.getLoaded() == false)
		{
            AppSessionContext.setProviderUI(getProviderProfileInfo());
            porticoOutput(0, "HelperClass-LoadSessionInformation-setProviderUI-Loaded");

            // Note: 'g_providerProfileMappingAsString' static String is built within 'getProviderProfileInfo' and its calling methods
            AppSessionContext.setProviderProfileMappingAsStringUI(g_providerProfileMappingAsString);
            porticoOutput(0, "HelperClass-LoadSessionInformation-setProviderProfileMappingAsStringUI-Loaded");

            AppSessionContext.setSubmissionAreaObjectIdUI(getSubmissionAreaId(currentSession));
            porticoOutput(0, "HelperClass-LoadSessionInformation-setSubmissionAreaObjectIdUI-Loaded");

            AppSessionContext.setWorkflowActivityListUI(getOrderedWorkflowActivityList());
            porticoOutput(0, "HelperClass-LoadSessionInformation-setWorkflowActivityListUI-Loaded");

            AppSessionContext.setActionReentryPointMappingListUI(getActionReentryPointMappingList());
            porticoOutput(0, "HelperClass-LoadSessionInformation-setActionReentryPointMappingListUI-Loaded");

            // Future
            AppSessionContext.setSearchObjectList(getSearchObjectListInfo());
            porticoOutput(0, "HelperClass-LoadSessionInformation-setSearchObjectList-Loaded");

            AppSessionContext.setLoaded(true);
	    }

	    // These are per Session of the logged in user
        AppSessionContext.setUserRolesUI(getAvailableUserRoles(currentSession));
        porticoOutput(0, "HelperClass-LoadSessionInformation-setUserRolesUI-Loaded");

        AppSessionContext.setUsersAndRolesUI(getRolesAndUsers(currentSession));
        porticoOutput(0, "HelperClass-LoadSessionInformation-setUsersAndRolesUI-Loaded");

        AppSessionContext.setCurrentUserAndGroupsListUI(getCurrentUserAndGroups(currentSession));
        porticoOutput(0, "HelperClass-LoadSessionInformation-setCurrentUserAndGroupsListUI-Loaded");
    }

/*
    public static String getParentArticleId(IDfSession currentSession, String objectId)
    {
        String parentArticleId = "";

        porticoOutput(0, "HelperClass-getParentArticleId()-Started for objectId="+objectId);

        if(objectId != null && !objectId.equals(""))
        {
            try
            {
                parentArticleId = getObjectOfTypeFromId(currentSession, objectId, "p_cu_state");
                if(parentArticleId == null || parentArticleId.equals(""))
                {
                    parentArticleId = getParentArticleIdEx(currentSession, objectId);
                }
            }
            catch(Exception e)
            {
				parentArticleId = "";
                porticoOutput(1, "Exception in HelperClass-getParentArticleId()-objectId="+objectId);
                porticoOutput(1, "Exception in HelperClass-getParentArticleId()="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getParentArticleId()-End for objectId,parentArticleId="+objectId+","+parentArticleId);

        return parentArticleId;
    }
*/
    public static String getParentArticleId(IDfSession currentSession, String objectId)
    {
		return DBHelperClass.getParentArticleId(objectId);
    }

/*
    public static String getObjectOfTypeFromId(IDfSession currentSession, String currentobjectId, String currentobjectType)
    {
        String retObjectId = "";

        porticoOutput(0, "HelperClass-getObjectOfTypeFromId()-Started for currentobjectId,currentobjectType="+currentobjectId+","+currentobjectType);

        if(currentobjectId != null && !currentobjectId.equals(""))
        {
            try
            {
                IDfPersistentObject iDfPersistentObject = (IDfPersistentObject)currentSession.getObject(new DfId(currentobjectId));
                if(iDfPersistentObject != null)
                {
                    if(iDfPersistentObject.hasAttr("r_object_type") == true)
                    {
                        String objectType = iDfPersistentObject.getString("r_object_type");
                        if(objectType.equals(currentobjectType))
                        {
                            retObjectId = currentobjectId;
                        }
                    }
                    if(retObjectId == null || retObjectId.equals(""))
                    {
                        if(currentobjectType.equals("p_cu_state") && iDfPersistentObject.hasAttr("p_cu_state_id") == true) // a_category
                        {
                            retObjectId = iDfPersistentObject.getString("p_cu_state_id");
                        }
                    }
                    if(retObjectId == null || retObjectId.equals(""))
                    {
                        porticoOutput(0, "WARNING HelperClass-getObjectOfTypeFromId()- not populated(use recursion)for currentobjectId,currentobjectType="+currentobjectId+","+currentobjectType);
                    }
                }
            }
            catch(Exception e)
            {
				retObjectId = "";
                porticoOutput(1, "Exception in HelperClass-getObjectOfTypeFromId()-currentobjectId="+currentobjectId);
                porticoOutput(1, "Exception in HelperClass-getObjectOfTypeFromId()="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getObjectOfTypeFromId()-End for currentobjectId,retObjectId="+currentobjectId+","+retObjectId);

        return retObjectId;
    }
*/

/*
    public static String getParentArticleIdEx(IDfSession currentSession, String objectId)
    {
        String parentArticleId = "";
        IDfCollection idfcollection = null;

        porticoOutput(0, "HelperClass-getParentArticleIdEx()-Started for objectId="+objectId);

        try
        {
            // Note: Due to dql issues, give both r_object_id and i_folder_id
            String dqlString = "SELECT r_object_id, i_folder_id from dm_sysobject where r_object_id=" + "'" + objectId + "'";
            DfQuery dfquery = new DfQuery();
            dfquery.setDQL(dqlString);
            porticoOutput(0, "HelperClass-getParentArticleIdEx:dqlString="+ dqlString);
            porticoOutput(0, "HelperClass-getParentArticleIdEx OPEN IDfCollection");

            String tParentArticleId = "";
            String parentFolderId = "";

            for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
            {
                parentFolderId = idfcollection.getString("i_folder_id");
                porticoOutput(0, "HelperClass-getParentArticleIdEx:parentFolderId="+ parentFolderId);
                break;
			}

			if(idfcollection != null)
			{
				idfcollection.close();
			}

            if(parentFolderId != null)
            {
                tParentArticleId = getObjectOfTypeFromId(currentSession, parentFolderId, "p_cu_state");
                if(tParentArticleId == null || tParentArticleId.equals(""))
                {
                    tParentArticleId = getParentArticleId(currentSession, parentFolderId);
                }
                else
                {
                    parentArticleId = tParentArticleId;
                }
            }
        }
        catch(Exception e)
        {
			parentArticleId = "";
			porticoOutput(1, "Exception in HelperClass-getParentArticleIdEx()-objectId="+objectId);
            porticoOutput(1, "Exception in HelperClass-getParentArticleIdEx()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getParentArticleIdEx- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getParentArticleIdEx()-close="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getParentArticleIdEx()-End for objectId="+objectId);

        return parentArticleId;
    }
*/

/*
    // Currently cu_state_id is passed
    public static ArrayList getPageImageList(IDfSession currentSession, String objectId)
    {
        ArrayList retList = new ArrayList();
        IDfCollection idfcollection = null;
        String pdfFuType = "Rendition: Page Images";

        porticoOutput(0, "HelperClass-getPageImageList()-Started for objectId="+objectId);

        try
        {
			// select i_folder_id, r_object_id, p_fu_type from p_fu where p_fu_type in ('Rendition: Page Images')
            // Note: Due to dql issues, give both r_object_id and i_folder_id
            DfQuery dfquery = new DfQuery();
            String dqlString = "SELECT r_object_id, i_folder_id from p_fu " +
                               "where folder(id(" + "'" + objectId + "'" +"),descend) AND " +
                               "p_fu_type IN (" + "'" + pdfFuType + "'" + ")";

            dfquery.setDQL(dqlString);
            porticoOutput(0, "HelperClass-getPageImageList:dqlString(1)="+ dqlString);
            porticoOutput(0, "HelperClass-getPageImageList OPEN IDfCollection");

            ArrayList parentFolderIdList = new ArrayList();

            for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
            {
				String parentFolderId = idfcollection.getString("i_folder_id");
                parentFolderIdList.add(parentFolderId);
                porticoOutput(0, "HelperClass-getPageImageList:parentFolderId(s)="+ parentFolderId);
			}

			if(idfcollection != null)
			{
				idfcollection.close();
			}

            if(parentFolderIdList != null && parentFolderIdList.size() > 0)
            {

				for(int pindx=0; pindx < parentFolderIdList.size(); pindx++)
				{
					String parentFolderId = (String)parentFolderIdList.get(pindx);
				    dqlString = "SELECT r_object_id from p_su_state " +
				                               "where folder(id(" + "'" + parentFolderId + "'" +"),descend)";
                    dfquery.setDQL(dqlString);
                    porticoOutput(0, "HelperClass-getPageImageList:dqlString(2)="+ dqlString);
                    for(idfcollection = dfquery.execute(currentSession, IDfQuery.DF_READ_QUERY); idfcollection.next();)
                    {
				    	String suStateId = idfcollection.getString("r_object_id");
				    	retList.add(suStateId);
                        porticoOutput(0, "HelperClass-getPageImageList:suStateId="+ suStateId);
    			    }
    			    if(idfcollection != null)
    			    {
    			    	idfcollection.close();
    			    }
			    }
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-getPageImageList()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                {
                    idfcollection.close();
                }
                porticoOutput(0, "HelperClass-getPageImageList- CLOSE IDfCollection");
            }
            catch(Exception e)
            {
                porticoOutput(1, "Exception in HelperClass-getPageImageList()-close="+e.toString());
                e.printStackTrace();
            }
        }

        porticoOutput(0, "HelperClass-getPageImageList()-End for objectId="+objectId);

        return retList;
	}
*/

    // Currently cu_state_id is passed
    public static Hashtable getPageImageList(IDfSession currentSession, String objectId)
    {
		return DBHelperClass.getPageImageList(objectId);
	}

    public static boolean isBatchOnHold(IDfSession currentSession, String batchObjectId)
    {
        boolean isOnHold = false;
        porticoOutput(0, "HelperClass-isBatchOnHold-(Started From Storage) for Batch_ID="+ batchObjectId);
        try
        {
            if(batchObjectId != null && !batchObjectId.equals(""))
            {
                IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(batchObjectId));
                isOnHold = iDfSysObject.getBoolean("p_on_hold");
                porticoOutput(0, "HelperClass-isBatchOnHold:isOnHold="+ isOnHold);
            }
        }
        catch(Exception e)
        {
            porticoOutput(1, "Exception in HelperClass-isBatchOnHold()="+e.toString());
            e.printStackTrace();
        }
        finally
        {
        }
        porticoOutput(0, "HelperClass-isBatchOnHold-(Ended From Storage)Batch_ID="+ batchObjectId + " isOnHold="+isOnHold);

        return isOnHold;
    }

    // Returns all the workflow activity list for all the provider(s), stored in sessionScope
    public static Hashtable getOrderedWorkflowActivityList()
    {
        Hashtable wf_activity_list_for_all_profiles = new Hashtable();
        String componentId = "configinfo";
        String profileLevel = "profile";// Multiple profiles
        try
        {
            // Pick from config file
            IConfigElement iConfigTop = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            Iterator profileLevelIterator = iConfigTop.getChildElements(profileLevel);
            while(profileLevelIterator.hasNext())
            {
				IConfigElement profileItem = (IConfigElement)profileLevelIterator.next();
				String profileId = profileItem.getAttributeValue("id");
				ArrayList wf_activity_list_per_profile = new ArrayList();
				Iterator profileChildren = profileItem.getChildElements();
				while(profileChildren.hasNext())
				{
                    IConfigElement profileChildElement = (IConfigElement)profileChildren.next();
                    if(profileChildElement.getName().equals("wf_activity_list"))
                    {
						Iterator activityList = profileChildElement.getChildElements("activity");
						while(activityList.hasNext())
						{
							IConfigElement activityElement = (IConfigElement)activityList.next();
							wf_activity_list_per_profile.add(activityElement.getAttributeValue("name"));
						}
					}
				}
				ArrayList profileIdList = getArrayFromString(profileId, ",");
				if(profileIdList != null && profileIdList.size() > 0)
				{
                    for(int pindx=0; pindx < profileIdList.size(); pindx++)
                    {
						String singleProfileId = (String)profileIdList.get(pindx);
                        if(!wf_activity_list_for_all_profiles.containsKey(singleProfileId))
                        {
                            wf_activity_list_for_all_profiles.put(singleProfileId, wf_activity_list_per_profile);
			            }
			            else
			            {
			        		HelperClass.porticoOutput(1, "Warning in QcHelperClass-getOrderedWorkflowActivityList() duplicate profile entry="+singleProfileId);
			        	}
			        }
			    }
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getOrderedWorkflowActivityList()="+e.toString());
            e.printStackTrace();
        }

		printLoadedWfActivities(wf_activity_list_for_all_profiles);

        return wf_activity_list_for_all_profiles;
    }

    // Returns all the action workflow activity mapping list for all the provider(s), stored in sessionScope
    public static Hashtable getActionReentryPointMappingList()
    {
        Hashtable action_wf_activity_mapping_for_all_profiles = new Hashtable();
        String componentId = "configinfo";
        String profileLevel = "profile";// Multiple profiles
        try
        {
            // Pick from config file
            IConfigElement iConfigTop = ConfigService.getConfigLookup().lookupElement("component[id=" + componentId + "]", Context.getSessionContext());
            Iterator profileLevelIterator = iConfigTop.getChildElements(profileLevel);
            while(profileLevelIterator.hasNext())
            {
				IConfigElement profileItem = (IConfigElement)profileLevelIterator.next();
				String profileId = profileItem.getAttributeValue("id");
				Hashtable action_wf_activity_mapping_per_profile = new Hashtable();
				Iterator profileChildren = profileItem.getChildElements();
				while(profileChildren.hasNext())
				{
                    IConfigElement profileChildElement = (IConfigElement)profileChildren.next();
					if(profileChildElement.getName().equals("action_wf_activity_mapping"))
					{
						Iterator activityWfActivityMapping = profileChildElement.getChildElements("action_wf_activity");
						while(activityWfActivityMapping.hasNext())
						{
							IConfigElement activityWfActivityElement = (IConfigElement)activityWfActivityMapping.next();
							action_wf_activity_mapping_per_profile.put(activityWfActivityElement.getAttributeValue("action"),
							                                         activityWfActivityElement.getAttributeValue("activity"));
						}
					}
				}
				ArrayList profileIdList = getArrayFromString(profileId, ",");
				if(profileIdList != null && profileIdList.size() > 0)
				{
                    for(int pindx=0; pindx < profileIdList.size(); pindx++)
                    {
                        String singleProfileId = (String)profileIdList.get(pindx);
                        if(!action_wf_activity_mapping_for_all_profiles.containsKey(singleProfileId))
                        {
                            action_wf_activity_mapping_for_all_profiles.put(singleProfileId, action_wf_activity_mapping_per_profile);
			            }
			            else
			            {
			        		HelperClass.porticoOutput(1, "Warning in QcHelperClass-getActionReentryPointMappingList() duplicate profile entry="+singleProfileId);
			        	}
			        }
				}
			}
        }
        catch(Exception e)
        {
            HelperClass.porticoOutput(1, "Exception in QcHelperClass-getActionReentryPointMappingList()="+e.toString());
            e.printStackTrace();
        }

        printLoadedActionWfActivityMapping(action_wf_activity_mapping_for_all_profiles);

        return action_wf_activity_mapping_for_all_profiles;
    }

    public static ArrayList getArrayFromString(String inputString, String separator)
    {
		ArrayList alist = new ArrayList();

    	HelperClass.porticoOutput(0, "HelperClass-getArrayFromString-Start(str|sep)="+inputString+"|"+separator);

		if(inputString != null && !inputString.equals("") && separator != null && !separator.equals(""))
		{
            StringTokenizer strTokenizer = new StringTokenizer(inputString, separator);
            while (strTokenizer.hasMoreTokens())
            {
    	        String token =  strTokenizer.nextToken().trim();
    	        if(!token.equals(""))
    	        {
					alist.add(token);
    		    	HelperClass.porticoOutput(0, "HelperClass-getArrayFromString-token="+token);
			    }
            }
		}

    	HelperClass.porticoOutput(0, "HelperClass-getArrayFromString-End");

    	return alist;
	}

	public static void printLoadedWfActivities(Hashtable wf_activity_list_for_all_profiles)
	{
		if(wf_activity_list_for_all_profiles != null && wf_activity_list_for_all_profiles.size() > 0)
		{
			Enumeration allProfileEnum = wf_activity_list_for_all_profiles.keys();

			while(allProfileEnum.hasMoreElements())
			{
				String profileKey = (String)allProfileEnum.nextElement();
				HelperClass.porticoOutput(0, "HelperClass-printLoadedWfActivities-profileKey="+profileKey);
			    ArrayList wf_activities_per_profile = (ArrayList)wf_activity_list_for_all_profiles.get(profileKey);

			    if(wf_activities_per_profile != null && wf_activities_per_profile.size() > 0)
			    {
			    	for(int actIndx=0; actIndx < wf_activities_per_profile.size(); actIndx++)
			    	{
			    		HelperClass.porticoOutput(0, "HelperClass-printLoadedWfActivities-wf_activity="+(String)wf_activities_per_profile.get(actIndx));
			    	}
			    }
			    else
			    {
			    	HelperClass.porticoOutput(0, "HelperClass-printLoadedWfActivities-wf_activities is NULL or empty");
			    }
		    }
		}
	}

	public static void printLoadedActionWfActivityMapping(Hashtable action_wf_activity_mapping_for_all_profiles)
	{
		if(action_wf_activity_mapping_for_all_profiles != null && action_wf_activity_mapping_for_all_profiles.size() > 0)
		{
			Enumeration allProfileEnum = action_wf_activity_mapping_for_all_profiles.keys();

			while(allProfileEnum.hasMoreElements())
			{
				String profileKey = (String)allProfileEnum.nextElement();
				HelperClass.porticoOutput(0, "HelperClass-printLoadedActionWfActivityMapping-profileKey="+profileKey);
			    Hashtable action_wf_activity_mapping_per_profile = (Hashtable)action_wf_activity_mapping_for_all_profiles.get(profileKey);

			    if(action_wf_activity_mapping_per_profile != null && action_wf_activity_mapping_per_profile.size() > 0)
			    {
                    Enumeration mappingEnum = action_wf_activity_mapping_per_profile.keys();
                    while(mappingEnum.hasMoreElements())
                    {
			    		String key = (String)mappingEnum.nextElement();
			    		String value = (String)action_wf_activity_mapping_per_profile.get(key);
			    		HelperClass.porticoOutput(0, "HelperClass-printLoadedActionWfActivityMapping-action,wf_activity="+key+","+value);
			    	}
			    }
			    else
			    {
                    HelperClass.porticoOutput(0, "HelperClass-printLoadedActionWfActivityMapping-actionWfActivityMapping is NULL or empty");
				}
		    }
		}
	}

	public static TreeMap getFormatInfo()
	{
		TreeMap formatIdNameMapping = new TreeMap();

		try
		{
			FormatRegistryServiceFactory formatRegistryServiceFactory = FormatRegistryUtil.getFactory();
			FormatRegistryService formatRegistryService = formatRegistryServiceFactory.createService();
			Format[] formats = formatRegistryService.getFormats();

			for(int findx=0; findx < formats.length; findx++)
			{
				Format format = (Format)formats[findx];
				if(null != format)
				{
					HashMap values = new HashMap();
					values.put(FORMAT_NAME, format.getPorticoDefinedName());
					values.put(MIME_TYPE, format.getDefaultMimeType());
					formatIdNameMapping.put(format.getFormatId(), values);
				}
			}
		}
		catch(Exception e)
		{
            HelperClass.porticoOutput(1, "Exception in HelperClass-getFormatInfo()="+e.toString());
            e.printStackTrace();
		}
		finally
		{
		}

		return formatIdNameMapping;
	}

	public static ArrayList getReplaceActionKeyMetadataOriginList()
	{
		ArrayList allValues = new ArrayList();

		ValuePair tPorticoValuePair = null;
        tPorticoValuePair = new ValuePair();
        tPorticoValuePair.setKey(KeyMetadataElementsConstants.SU_ORIGIN_PROVIDER);

        // Display value
        tPorticoValuePair.setValue(PUBLISHER_SUPPLIED);
        allValues.add(tPorticoValuePair);

        tPorticoValuePair = new ValuePair();
        tPorticoValuePair.setKey(KeyMetadataElementsConstants.SU_ORIGIN_ARCHIVE);

        // Display value
        tPorticoValuePair.setValue(USER_SUPPLIED);
        allValues.add(tPorticoValuePair);

        return allValues;
	}

	public static ArrayList getAddNewFileActionKeyMetadataOriginList()
	{
		ArrayList allValues = new ArrayList();

		ValuePair tPorticoValuePair = null;
        tPorticoValuePair = new ValuePair();
        tPorticoValuePair.setKey(KeyMetadataElementsConstants.SU_ORIGIN_PROVIDER);

        // Display value
        tPorticoValuePair.setValue(PUBLISHER_SUPPLIED);
        allValues.add(tPorticoValuePair);

        return allValues;
	}

	public static TreeMap getMimeTypeList()
	{
		TreeMap mimeTypeSelfMapping = new TreeMap();

		try
		{
			MimeTypeLookupServiceFactory mimeTypeLookupServiceFactory = MimeTypeLookupUtil.getFactory();
			MimeTypeLookupService mimeTypeLookupService = mimeTypeLookupServiceFactory.createService();
			List<String> mimeTypeList = mimeTypeLookupService.getMimeTypeList();

			for(String mimeType : mimeTypeList)
			{
				if(null != mimeType && !mimeType.equals(""))
				{
					mimeTypeSelfMapping.put(mimeType, mimeType);
				}
			}
		}
		catch(Exception e)
		{
            HelperClass.porticoOutput(1, "Exception in HelperClass-getMimeTypeList()="+e.toString());
            e.printStackTrace();
		}
		finally
		{
		}

		return mimeTypeSelfMapping;
	}
}
