/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project          ConPrep WebTop
 * Module           WorkFlow Report
 * File             MesgDetails.java
 * Created on       Jan 31, 2005
 *
 */
package org.portico.conprep.ui.details;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.XMLParser;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Label;
import com.documentum.web.formext.component.Component;

/**
 * Description  This custom component is created to display mesg xml details
 * Author       pramaswamy
 * Type         MesgDetails
 */
public class MesgDetails extends Component {

    public MesgDetails() {
        super();
    }
    /**
     * @param contains - r_object_id of message xml object
     *                 - Id value parsed from xml content
     */
    public void onInit(ArgumentList argumentlist) {
        super.onInit(argumentlist);
        m_mesgId=argumentlist.get("mesgId");
        m_mesgObjId=argumentlist.get("objectId");

		HelperClass.porticoOutput(0, "ConPrep UI ....onInit MesgDetails argumentlist :"+argumentlist.toString());

        ByteArrayInputStream bais = null;
        String strTemp="";
        try {
            IDfDocument mesgObj = (IDfDocument)getDfSession().getObject(new DfId(m_mesgObjId));
            bais = mesgObj.getContent();
            ArrayList arrayList = XMLParser.lookupMultipleNodeValues(bais,"MessageSet.Message[Id="+m_mesgId+"]");
            StringBuffer sb = new StringBuffer();
            sb.append("<TABLE><TR><TD valign='top'><b>Source</b></TD><TD>");
            String temp = argumentlist.get("srcName");
            temp = temp == null? "" : temp;
            sb.append(temp);
            sb.append("</TD></TR><tr><td colspan='2'></td></tr>");
            for(int k=0;k<arrayList.size();k++){
                Hashtable hashtable = (Hashtable)arrayList.get(k);
                Enumeration enumeration = hashtable.keys();
                while(enumeration.hasMoreElements()){
                    String key = (String)enumeration.nextElement();
                    sb.append("<TR><TD valign='top'><b>");
                    sb.append(key);
                    sb.append("</b></TD><TD>");
                    sb.append(hashtable.get(key));
                    sb.append("</TD></TR><tr><td colspan='2'></td></tr>");
                }
            }
            sb.append("</TABLE>");
            Label label = (Label)getControl(MesgDetails.DATA_LBL_NAME, com.documentum.web.form.control.Label.class);
            label.setLabel(sb.toString());
            sb.delete(0,sb.capacity());
        } catch (DfException e) {
            e.printStackTrace();
        }finally{
            try {
                bais.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private String m_mesgId;
    private String m_mesgObjId;
    public static final String DATA_LBL_NAME="msgdetails";
}
