/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module
 * File             Activity.java
 * Created on       Jan 31, 2005
 *
 */
package org.portico.conprep.ui.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.XMLParser;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;

/**
 * Description
 * Author       pramaswamy
 * Type         Activity
 */
public class Activity extends Component {

    /**
     *
     */
    public Activity() {
        super();
    }

    /* (non-Javadoc)
     * @see com.documentum.web.form.Control#onInit(com.documentum.web.common.ArgumentList)
     * gets all arguments passed from reportdetails summary classic component
     * except for workitemid, everything is unused for now
     */
    public void onInit(ArgumentList argumentlist) {
        super.onInit(argumentlist);
//      m_queueId=argumentlist.get("queueId");
        m_name=argumentlist.get("Name");
        m_workitemId=argumentlist.get("workitemId");
//      m_taskRuntimeState=argumentlist.get("taskRuntimeState");
//      m_taskRuntimeFlag=argumentlist.get("taskRuntimeFlag");
//      m_workflowId=argumentlist.get("workflowId");
//      m_workflowRuntimeState=argumentlist.get("workflowRuntimeState");
        m_batchfolderId=argumentlist.get("batchId");
        mesgs = new ArrayList();
        HelperClass.porticoOutput(0, "Activity-ConPrep UI ....onInit argumentlist :"+argumentlist);
    }

    /*
     * @see com.documentum.web.formext.component.Component#onRender()
     * set messages in workflow report page
     */
    public void onRender() {
        super.onRender();
        if((m_workitemId!=null) &&
            (m_workitemId.trim().length()>0)){
            HelperClass.porticoOutput(0, "Activity-ConPrep UI .... onRender m_workitemId " + m_workitemId);
            WorkFlow workflow = (WorkFlow)getContainer();
            HelperClass.porticoOutput(0, "Activity-ConPrep UI .... onRender m_name "+m_name);
            String strMesgsForCurrentActivity = workflow.getMesgHtml(m_workitemId);
            if(strMesgsForCurrentActivity==null) {
                HelperClass.porticoOutput(0, "Activity-ConPrep UI .... onRender mesg == null ");
                strMesgsForCurrentActivity = listUMS();
                workflow.setMesgHtml(m_workitemId,strMesgsForCurrentActivity);
                workflow.setMesgResultSet(m_name,mesgs);
            }
            Label label = (Label)getControl("triallabel", com.documentum.web.form.control.Label.class);
            label.setLabel(strMesgsForCurrentActivity);
        }
    }

    /**
     * @return list of messages to display
     * loop through each type of mesg object list
     */
    public String listUMS(){
        StringBuffer sb1 = new StringBuffer();
        HelperClass.porticoOutput(0, "Activity-ConPrep UI ....listUMS m_currentworkitemid :"+m_workitemId);

        StringBuffer sb = new StringBuffer();
        sb.append("select r_act_def_id from dmi_workitem where r_object_id='");
        sb.append(m_workitemId);
        sb.append("'");
        try {
            IDfCollection idfcollection = null;
            IDfClientX clientx = new DfClientX();
            IDfQuery q = clientx.getQuery(); //Create query object
            HelperClass.porticoOutput(0, "Activity-ConPrep UI ....listUMS get activity id query :"+sb.toString());
            q.setDQL(sb.toString());
            idfcollection = q.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
                while(idfcollection.next()){
                    m_activityId = idfcollection.getString("r_act_def_id");
                }
            idfcollection.close();
            sb.delete(0,sb.capacity());
            idfcollection=null;
            DfLogger.debug(this,"Activity-ConPrep UI .... listUMS activity id :"+m_activityId,null,null);

                fetchUMS();
                sb1.append("<TABLE><TBODY>");
                //fetch ums related to batch object
                if(!m_batchumidlist.isEmpty()) {
                    HelperClass.porticoOutput(0, "Activity-ConPrep UI .... listUMS m_batchumidlist " +m_batchumidlist);
                    //sb1.append("<TR><TD>Batches</TD><TD></TD></TR>");
                    Enumeration enumeration = m_batchumidlist.keys();
                    while(enumeration.hasMoreElements()){
                        String umId = (String)enumeration.nextElement();
                        sb1.append("<TR><TD>"+(String)m_batchumidlist.get(umId)+"(Batch)</TD><TD></TD></TR>");
                        sb1.append(getMesgText(umId,(String)m_batchumidlist.get(umId)));
                    }
                }
                //fetch ums related to cs
                if(!m_csumidlist.isEmpty()) {
                    HelperClass.porticoOutput(0, "Activity-ConPrep UI .... listUMS in CS m_csumidlist " +m_csumidlist);
                    sb1.append("<TR><TD>CS</TD><TD></TD></TR>");
                    Enumeration enumeration = m_csumidlist.keys();
                    while(enumeration.hasMoreElements()){
                        String umId = (String)enumeration.nextElement();
                        sb1.append(getMesgText(umId,(String)m_csumidlist.get(umId)));
                    }
                }
                //fetch ums related to cu
                if(!m_cuumidlist.isEmpty()) {
                    HelperClass.porticoOutput(0, "Activity-ConPrep UI .... listUMS m_cuumidlist " +m_cuumidlist);
                    sb1.append("<TR><TD>CU</TD><TD></TD></TR>");
                    Enumeration enumeration = m_cuumidlist.keys();
                    while(enumeration.hasMoreElements()){
                        String umId = (String)enumeration.nextElement();
                        sb1.append(getMesgText(umId,(String)m_cuumidlist.get(umId)));
                    }
                }
                //fetch ums related to fu
                if(!m_fuumidlist.isEmpty()) {
                    HelperClass.porticoOutput(0, "Activity-ConPrep UI .... listUMS m_fuumidlist " +m_fuumidlist);
                    sb1.append("<TR><TD>FU</TD><TD></TD></TR>");
                    Enumeration enumeration = m_fuumidlist.keys();
                    while(enumeration.hasMoreElements()){
                        String umId = (String)enumeration.nextElement();
                        sb1.append(getMesgText(umId,(String)m_fuumidlist.get(umId)));
                    }
                }
                //fetch ums related to su
                if(!m_suumidlist.isEmpty()) {
                    HelperClass.porticoOutput(0, "Activity-ConPrep UI .... listUMS m_suumidlist " +m_suumidlist);
                    sb1.append("<TR><TD>SU</TD><TD></TD></TR>");
                    Enumeration enumeration = m_suumidlist.keys();
                    while(enumeration.hasMoreElements()){
                        String umId = (String)enumeration.nextElement();
                        sb1.append(getMesgText(umId,(String)m_suumidlist.get(umId)));
                    }
                }
                sb1.append("</TBODY></TABLE>");

        } catch (DfException e) {
            e.printStackTrace();
            ErrorMessageService.getService().setNonFatalError(getCallerForm(),"fetchUMS",e);
        } catch (Exception e1) {
        e1.printStackTrace();
        ErrorMessageService.getService().setNonFatalError(getCallerForm(),"fetchUMS",e1);
        } finally {
            m_batchumidlist.clear();
            m_csumidlist.clear();
            m_cuumidlist.clear();
            m_fuumidlist.clear();
            m_suumidlist.clear();
        }

        return sb1.toString();
    }

    /**
     * fetches r_object_id of user message objects and
     * put them in appripriate bucket
     */
    public void fetchUMS(){

        //IDfCollection idfcollection = null;
        //StringBuffer sb = new StringBuffer();
        //try {
            //IDfClientX clientx = new DfClientX();
            //IDfQuery q = clientx.getQuery();

            //sb.append("select ums.r_object_id, sys.object_name, sys.r_object_type from ptc_user_message_set ums, dm_sysobject sys,dmi_workitem wi where ums.ptc_associated_object_id=sys.r_object_id and ums.ptc_sequence_no = wi.r_act_seqno and ums.ptc_activity_id='");
            //sb.append(m_activityId);
            //sb.append("' and wi.r_object_id='");
            //sb.append(m_workitemId);
            //sb.append("' and FOLDER(ID('");
            //sb.append(m_batchfolderId);
            //sb.append("'),DESCEND)");

            HelperClass.porticoOutput(0, "obsolete Activity-ConPrep UI ....fetchUMS");
            //q.setDQL(sb.toString());
            //idfcollection = q.execute(getDfSession(), IDfQuery.DF_READ_QUERY);
            //  while(idfcollection.next()){
            //      if(idfcollection.getString("r_object_type").equals("ptc_batch")) {
            //          m_batchumidlist.put(idfcollection.getString("r_object_id"),idfcollection.getString("object_name"));
            //      }
            //      else if(idfcollection.getString("r_object_type").equals("ptc_cs_folder")) {
            //          m_csumidlist.put(idfcollection.getString("r_object_id"),idfcollection.getString("object_name"));
            //      }
            //      else if(idfcollection.getString("r_object_type").equals("ptc_cu")) {
            //          m_cuumidlist.put(idfcollection.getString("r_object_id"),idfcollection.getString("object_name"));
            //      }
            //      else if(idfcollection.getString("r_object_type").equals("ptc_fu")) {
            //          m_fuumidlist.put(idfcollection.getString("r_object_id"),idfcollection.getString("object_name"));
            //      }
            //      else if(idfcollection.getString("r_object_type").equals("ptc_su")) {
            //          m_suumidlist.put(idfcollection.getString("r_object_id"),idfcollection.getString("object_name"));
            //      }
            //  }
        //} catch (DfException e) {
        //  e.printStackTrace();
        //  ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching UMS",e);
        //} catch (Exception e1) {
        //  e1.printStackTrace();
        //  ErrorMessageService.getService().setNonFatalError(getCallerForm(),"Error in fetching UMS",e1);
        //} finally{
        //  try {
        //      idfcollection.close();
        //  } catch (DfException e2) {
        //      e2.printStackTrace();
        //  }
        //  sb.delete(0,sb.capacity());
        //  idfcollection=null;
        //}
    }

    /**
     * @param mesgObjId r_object_id of user message object
     * @param assetName asset associated with the message object
     * @return  single message display row
     */
    public String getMesgText(String mesgObjId,String assetName){
        ByteArrayInputStream bais = null;
        StringBuffer sb = new StringBuffer();
        try {
            IDfDocument mesgObj = (IDfDocument)getDfSession().getObject(new DfId(mesgObjId));
            if(mesgObj.getContentSize()>0){
                bais = mesgObj.getContent();
                ArrayList txtList = XMLParser.lookupMultipleNodeValues(bais,"MessageSet.Message.Text");
                bais.reset();
                ArrayList severityList = XMLParser.lookupMultipleNodeValues(bais,"MessageSet.Message.Severity");
                bais.reset();
                ArrayList categoryList = XMLParser.lookupMultipleNodeValues(bais,"MessageSet.Message.Category");
                bais.reset();
                ArrayList msgIdList = XMLParser.lookupMultipleNodeValues(bais,"MessageSet.Message[Id]");

                for(int k=0;k<txtList.size();k++){

                    //mesgs
                    Hashtable mesg = new Hashtable();
                    mesg.put("Source",assetName);
                    mesg.put("Id",msgIdList.get(k));
                    mesg.put("Severity",severityList.get(k));
                    mesg.put("Category",categoryList.get(k));
                    mesg.put("Text",txtList.get(k));
                    mesgs.add(mesg);

                    sb.append("<TR><TD></TD><TD><span  title='");

                    sb.append("Source        :");
                    sb.append(assetName);
                    sb.append("\n");
                    sb.append("Message Id :");
                    sb.append((String)msgIdList.get(k));
                    sb.append("\n");
                    sb.append("Severity      :");
                    sb.append((String)severityList.get(k));
                    sb.append("\n");
                    sb.append("Category    :");
                    sb.append((String)categoryList.get(k));

                    sb.append("'>");

                    String trimmedmesg = (String)txtList.get(k);
                    int endIdx = trimmedmesg.length();
                    endIdx=(endIdx>50)? 50:endIdx;
                    sb.append(trimmedmesg.substring(0,endIdx));

                    sb.append("<a href=\"#\" onClick=\"obj=window.open('");
                    sb.append(makeUrl(getPageContext().getRequest(),"/component/mesgdetails?objectId="+mesgObjId+"&mesgId="+msgIdList.get(k)+"&srcName="+assetName));
                    sb.append("', 'usermsg', 'toolbar=no, directories=no, location=no, status=no, menubar=no, resizable=yes, scrollbars=no, width=500, height=350');obj.focus();\">...</a>");
                    sb.append("</span></TD></TR>");
                }//for each mesg
            }//if mesg has content
        } catch (DfException e) {
            e.printStackTrace();
            ErrorMessageService.getService().setNonFatalError(getCallerForm(),"fetchUMS",e);
        }finally{
            try {
                if(bais!=null){
                    bais.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                ErrorMessageService.getService().setNonFatalError(getCallerForm(),"fetchUMS",e1);
            }
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.documentum.web.formext.component.Component#onExit()
     * release all data
     */
    public void onExit() {
        super.onExit();
        m_batchumidlist.clear();
        m_csumidlist.clear();
        m_cuumidlist.clear();
        m_fuumidlist.clear();
        m_suumidlist.clear();
        mesgs.clear();
    }

    private Hashtable m_batchumidlist = new Hashtable();
    private Hashtable m_csumidlist = new Hashtable();
    private Hashtable m_cuumidlist = new Hashtable();
    private Hashtable m_fuumidlist = new Hashtable();
    private Hashtable m_suumidlist = new Hashtable();
//  private String m_queueId;
    private String m_name; //nothing but, activity name
    private String m_workitemId;
//  private String m_taskRuntimeState;
//  private String m_taskRuntimeFlag;
//  private String m_workflowId;
//  private String m_workflowRuntimeState;
    private String m_activityId;
    private String m_batchfolderId;
    private ArrayList mesgs;
}
