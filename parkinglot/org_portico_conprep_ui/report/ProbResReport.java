/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Component class for QC Action Report
 * File           	ProbResReport.java
 * Created on 		Feb 15, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.util.Hashtable;

import org.portico.common.config.LdapUtil;
import org.portico.conprep.ui.helper.DBHelperClass;
import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.HelperClassConstants;
import org.portico.conprep.ui.helper.QcHelperClass;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.DialogContainer;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.role.RoleService;

/**
 * Description	Does the following things
 * 					initiates fetching of action mapping xml
 * 					intiates fetching of qc report data
 * Author		wjh
 * Type		ProbResReport
 */
public class ProbResReport extends com.documentum.webtop.webcomponent.objectlist.ObjectList
{
	/**
	 *
	 */
	public ProbResReport()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onInit(com.documentum.web.common.ArgumentList)
	 */
	public void onInit(ArgumentList arg0)
	{
		batchObjId=arg0.get("objectId");
		m_callercomponent=arg0.get("callerComponent");
		HelperClass.porticoOutput("ProbResReport onInit() for BatchId,m_callercomponent="+batchObjId+","+m_callercomponent);

	    batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), batchObjId);
	    batchName = HelperClass.getObjectName(getDfSession(), batchObjId, DBHelperClass.BATCH_TYPE);

		initializeCommonInfo();

		Datagrid datagrid = (Datagrid)getControl(ProbResReport.DATAGRID_1, com.documentum.web.form.control.databound.Datagrid.class);
		dataProvider = datagrid.getDataProvider();
		onInitSw = true;
		super.onInit(arg0);
		try
		{
		    String actionconfigdborfs = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigdborfs");
		    if(actionconfigdborfs.equals("fs"))
		    {
			    String actionconfigfilesysloc = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigfilesysloc");
				qCActionsMaps = QCActionsMaps.getInstance(getDfSession(),actionconfigfilesysloc);
		    }
		    else
		    {
		    	String actionconfigdocbaseloc = LdapUtil.getAttribute("dc=ui", "cn=conprepui", "actionconfigdocbaseloc");
		    	qCActionsMaps = QCActionsMaps.getInstance(getDfSession(),actionconfigdocbaseloc);
		    }
			pRRResultSet = new PRRResultSet(arg0.get("objectId"), batchStatus, batchName, getDfSession());
			dataProvider.setScrollableResultSet(pRRResultSet.getTableResultSet());
			Control control = getContainer();
			if(control instanceof Form)
				((Form)control).setModal(false);
			setModal(false);
		}
		catch (DfException e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching ProbRes report data",e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching ProbRes report data",e);
		}
	}

	public void	initializeCommonInfo()
	{
		m_isValidPerformer = performerCheck();
        m_isValidRole = roleCheck();
        HelperClass.porticoOutput(0, "ProbResReport-initializeCommonInfo-m_isValidPerformer="+m_isValidPerformer);
        HelperClass.porticoOutput(0, "ProbResReport-initializeCommonInfo-m_isValidRole="+m_isValidRole);
    }

    public boolean isValidPerformer()
    {
		return m_isValidPerformer;
	}

    public boolean isValidRole()
    {
		return m_isValidRole;
	}


	public String getSource()
	{
		return pRRResultSet.getLeadSource(dataProvider.getDataField("r_object_id"));
	}

/*
    public String getLeadSource(IDfSession currentSession, String objectId)
    {
        String leadSource = "";
        //porticoOutput(0, "getLeadSource-Started for object="+ objectId);
        try
        {
			if(objectId != null && !objectId.equals(""))
			{
			    IDfSysObject iDfSysObject = (IDfSysObject)currentSession.getObject(new DfId(objectId));
	            leadSource = iDfSysObject.getString("p_lead_source_id"); // p_lead_source_id
                //porticoOutput(0, "getLeadSource:batchStatus="+ batchStatus);
		    }
        }
        catch(Exception e)
        {
            //porticoOutput(0, "Exception in getLeadSource()="+e.getMessage());
            e.printStackTrace();
        }
        finally
        {
        }
        //porticoOutput(0, "getLeadSource:Batch_ID="+ objectId + " leadSource="+leadSource);

        return leadSource;
    }
*/

	public String getBatch()
	{
		return batchObjId;
	}

    public boolean roleCheck()
    {
    	try
    	{
        	ArgumentList argumentList = new ArgumentList();
        	Context context = Context.getSessionContext();
	    	if(RoleService.isUserAssignedRole(getDfSession().getLoginUserName(),"conprep_inspector_role",argumentList,context))
	    	{
			    return true;
		    }
    	}
    	catch (Exception e)
    	{
    		HelperClass.porticoOutput("Exception in ProbResReport-roleCheck-"+e.getMessage());
    	}
		return false;
    }

    public String getBatchPerformer()
    {
		String batchPerformer = "";

    	try
    	{
      		IDfSysObject sysObj = (IDfSysObject)getDfSession().getObject(new DfId(batchObjId));
      		batchPerformer = sysObj.getString("p_performer");
        }
        catch(Exception e)
        {
        	HelperClass.porticoOutput("Exception in ProbResReport-getBatchPerformer-"+e.getMessage());
        }

        return batchPerformer;
	}

    public boolean performerCheck()
    {
		boolean isValidBatchPerformer = false;

    	try
    	{
        	if(getBatchPerformer().equals(getDfSession().getLoginUserName()))
        	{
        		isValidBatchPerformer = true;
        	}
        }
        catch(Exception e)
        {
        	HelperClass.porticoOutput("Exception in ProbResReport-performerCheck-"+e.getMessage());
        }

        return isValidBatchPerformer;
    }

/*
	public void onClickObject(Control control, ArgumentList args) {
		String suObjId="";
		try
		{
		    suObjId=PRRResultSet.getSuObjId(args.get("objectId"), getDfSession());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching ProbRes report data",e);
		}
		finally
		{
    		HelperClass.porticoOutput(0, "args-in="+args.toString());
		    HelperClass.porticoOutput(0, "onClickObject() objId="+suObjId+" type="+args.get("type")+" isFolder="+args.get("isFolder"));
		    args.replace("objectId",suObjId);
		    HelperClass.porticoOutput(0, "args-out="+args.toString());
		    super.onClickObject(control,args);
		    onInitSw=true;
		    onRender();
		    //super.onClickObject(control,args);
		}
	}
*/

	public void onClearButton(Control control, ArgumentList argumentlist) throws Exception
	{
		HelperClass.porticoOutput(0, "ProbResReport-onClearButton()");
		String tempMsgId=argumentlist.get("msgObjectId");
		String severityText=argumentlist.get("severityText");
		//String temp= dataProvider.getDataField("msgObjId");
		HelperClass.porticoOutput(0, "ProbResReport-onClearButton() tempMsgId="+tempMsgId);
		//pRRResultSet.clearButton(temp);
		boolean isSuccessful = QcHelperClass.setUserMessage(getDfSession(),tempMsgId,batchObjId,"", PRRResultSet.getSeverityFromSeverityText(severityText));
		//QcHelperClass.setCommonBatchInfoForQC(getDfSession(),PRRResultSet.m_batchfolderId,"");
		onRender();
    }

	public void onClearAllWarnings(Control control, ArgumentList argumentlist) throws Exception
	{
		HelperClass.porticoOutput(0, "ProbResReport-onClearAllWarnings()");
		// Note: Below, calling 'onRender()' may not be required because any call to the server
		//       will automatically call the rendering, just in case we have added here
		if(PRRResultSet.clearAllWarnings(batchObjId, getDfSession())) onRender();
    }

	public void onRender()
	{
	  if (onInitSw)
	  {
	  		onInitSw = false;
			HelperClass.porticoOutput(0, "ProbResReport-onRender() bypass tree-walk");
			super.onRender();
	  }
	  else
	  {
		HelperClass.porticoOutput(0, "ProbResReport-onRender() tree-walk");
		try
		{
			//onInitSw = true;
			//String m_b = pRRResultSet.m_batchfolderId;
    		m_isValidPerformer = performerCheck();
            HelperClass.porticoOutput(0, "ProbResReport-onRender-m_isValidPerformer="+m_isValidPerformer);
		    batchStatus = HelperClass.getStatusForBatchObject(getDfSession(), batchObjId);
			HelperClass.porticoOutput(0, "ProbResReport-onRender-call new pRRResultSet");
	    	pRRResultSet = new PRRResultSet(batchObjId, batchStatus, batchName, getDfSession());
	    	dataProvider.setScrollableResultSet(pRRResultSet.getTableResultSet());
			super.onRender();
		}
		catch (DfException e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching ProbRes report data",e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching ProbRes report data",e);
		}
	  }
	}

	/*
	 * for current row -
	 * gives indentation to display assets as content tree
	 */
	public String getSourcePrefix()
	{
		String pre = "";
		Boolean boolObj = Boolean.valueOf(dataProvider.getDataField("isOrphan"));
		if(!boolObj.booleanValue())
		{
		}
		else
		{
			pre = "<I>";
		}
		return pre;
	}

	/**
	 * current row's object type
	public String getObjectType() {
		//HelperClass.porticoOutput(0, "in QC.getObjectType()");
		return dataProvider.getDataField("r_object_type");
	}
	**/

	/**
	 * current row's code
	 */
	public String getCode()
	{
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return dataProvider.getDataField("p_code");
	}

	public String getObjId()
	{
		return dataProvider.getDataField("r_object_id");
	}

	public String getObjectName()
	{
		return dataProvider.getDataField("object_name");
	}

	public boolean isValidBatchStatus()
	{
		if(batchStatus.equals(HelperClassConstants.RESOLVING_PROBLEM)) // || batchStatus.equals(HelperClassConstants.INSPECTING))
			return true;
		else
			return false;
	}

	public String getBatchStatus()
	{
		return batchStatus;
	}

	public String getSeverity()
	{
		//HelperClass.porticoOutput(0, "in QC.getSeverity()");
		return dataProvider.getDataField("Severity");
	}

	public String getBatchInfo() throws Exception
	{
		return PRRResultSet.getBatchInfo(getDfSession(),batchObjId,batchStatus,pRRResultSet.getActiveMessageCount()+"");
	}

	public String getObjType()
	{
		return dataProvider.getDataField("r_object_type");
	}

	public boolean isVirusChecked() throws Exception
	{
		return pRRResultSet.isVirusChecked(dataProvider.getDataField("r_object_id"));
	}

	public String getSuObjectId() throws Exception
	{
		return pRRResultSet.getSuObjectId(dataProvider.getDataField("r_object_id"));
	}

	public String getBatchFolder()
	{
		//HelperClass.porticoOutput(0, "in QC.getCode()");
		return batchObjId;
	}

	public String getSUObjectFromSUState(String suStateId)
	{
		return pRRResultSet.getSuObjectId(suStateId);
	}

	public String getParentArticleId(String objectIdIn)
	{
		return HelperClass.getParentArticleId(getDfSession(), objectIdIn);
	}

	public Hashtable getAction()
	{
		HelperClass.porticoOutput(0, "ProbResReport-Start-getAction");
		String temp=getCode();
		Hashtable acthash = new Hashtable();
		while (temp.indexOf("/")>0)
		{
			String code=temp.substring(0,temp.indexOf("/"));
			Object[] actions=qCActionsMaps.getActions(code);
			Object[] reentry=qCActionsMaps.getReentryPt(code);
			for (int i=0;i<actions.length;i++)
			{
				String action=(String)actions[i];
				String reent=(String)reentry[i];
				HelperClass.porticoOutput(0, "ProbResReport-storing action/reent="+action+"/"+reent);
				acthash.put(action,reent);
			}
			String temp1=temp.substring(temp.indexOf("/")+1);
			temp=temp1;
		}
		Object[] actions=qCActionsMaps.getActions(temp);
		Object[] reentry=qCActionsMaps.getReentryPt(temp);
		for (int i=0;i<actions.length;i++)
		{
			String action=(String)actions[i];
			String reent=(String)reentry[i];
			HelperClass.porticoOutput(0, "ProbResReport-storing action/reent="+action+"/"+reent);
			acthash.put(action,reent);
		}
		HelperClass.porticoOutput(0, "ProbResReport-hash-content="+acthash);
		HelperClass.porticoOutput(0, "ProbResReport-End-getAction");
		if(acthash.size()>0) return acthash;
		else return acthash;
	}

	public String getCallerComponent()
	{
		return m_callercomponent;
	}

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.component.Component#onExit()
	 */
	public void onExit()
	{
		super.onExit();
		if(pRRResultSet!=null)
		{
			pRRResultSet.clearData();
		}
	}

	public void closeThisComponent(Control control,ArgumentList args)
	{
		((DialogContainer)getContainer()).onClose(control, args);
	}

	/**
	 * stored per instance to facilitate row-wise navigation
	 */
	private QCActionsMaps qCActionsMaps;
	/**
	 * stored per instance to facilitate row-wise navigation
	 */
	private DataProvider dataProvider;
	/**
	 * ResultSet object used by data grid
	 */
	private PRRResultSet pRRResultSet;
	/**
	 * list of columns in collection object
	 */
	public static final String colNames[] = {
		"r_object_id",
		"object_name",
		"r_object_type",
		"p_code",
		"Text",
		"Severity",
		"Category",
		"isFolder",
		"msgObjId",
		"isOrphan",
		"sortKey",
	};

	boolean onInitSw;

	/**
	 * datagrid control name in jsp page
	 */
	public static final String DATAGRID_1 = "DATAGRID_1";

	private String batchObjId;
	private String batchStatus;
	private String batchName;
	private boolean m_isValidPerformer = false;
    private boolean m_isValidRole = false;
	private String m_callercomponent;
}














