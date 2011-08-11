
package org.portico.conprep.ui.submission.importform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.control.LinkDetectorApplet;
import com.documentum.web.form.Control;
import com.documentum.web.form.IVisitor;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.formext.component.ComboContainer;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.docbase.DocbaseAttributeValue;

public class SubmitBatchComponent extends Component
{

    private final class Visitor
        implements IVisitor
    {

        public boolean visit(Control control)
        {
            try
            {
                if(control.isEnabled())
                    if(control instanceof DocbaseAttributeValue)
                    {
                        DocbaseAttributeValue docbaseattributevalue = (DocbaseAttributeValue)control;
                        if(docbaseattributevalue.isDirty())
                        {
                            String s1 = "dmf_" + docbaseattributevalue.getAttribute();
                            Object obj1 = (List)m_map.get(s1);
                            if(obj1 == null)
                            {
                                obj1 = new ArrayList();
                                m_map.put(s1, obj1);
                            }
                            if(docbaseattributevalue.isRepeating())
                            {
                                Vector vector = docbaseattributevalue.getValues();
                                StringBuffer stringbuffer = new StringBuffer(128);
                                if(vector != null)
                                {
                                    for(int i = 0; i < vector.size(); i++)
                                    {
                                        if(i > 0)
                                            stringbuffer.append("__prs__");
                                        String s2 = (String)vector.get(i);
                                        String s3 = docbaseattributevalue.getActualValue(s2);
                                        if(docbaseattributevalue.getDataType() == 0)
                                            s3 = s3.equals("T") ? "true" : "false";
                                        stringbuffer.append(s3);
                                    }

                                }
                                ((List) (obj1)).add(docbaseattributevalue.getActualValue(stringbuffer.toString()));
                            } else
                            {
                                ((List) (obj1)).add(docbaseattributevalue.getActualValue(docbaseattributevalue.getValue()));
                            }
                        }
                    } else
                    {
                        String s = control.getName();
                        if(s != null && m_dfType.findTypeAttrIndex(s) != -1)
                        {
                            Object obj = (List)m_map.get(s);
                            if(obj == null)
                            {
                                obj = new ArrayList();
                                m_map.put(s, obj);
                            }
                            if(control instanceof StringInputControl)
                                ((List) (obj)).add(((StringInputControl)control).getValue());
                            else
                            if(control instanceof BooleanInputControl)
                                ((List) (obj)).add(new Boolean(((BooleanInputControl)control).getValue()));
                        }
                    }
            }
            catch(DfException dfexception)
            {
                throw new WrapperRuntimeException("Failed to get attributes", dfexception);
            }
            return true;
        }

        public String getPropertySettings()
        {
            StringBuffer stringbuffer = new StringBuffer(256);
            Iterator iterator = m_map.keySet().iterator();
            boolean flag = false;
            while(iterator.hasNext())
            {
                String s = (String)iterator.next();
                List list = (List)m_map.get(s);
                for(int i = 0; i < list.size(); i++)
                {
                    if(flag)
                        stringbuffer.append("~~pfs~~");
                    else
                        flag = true;
                    stringbuffer.append(s).append("~~pfs~~").append((String)list.get(i));
                }

            }
            return stringbuffer.toString();
        }

        IDfType m_dfType;
        Map m_map;

        public Visitor(String s)
        {
            m_dfType = null;
            m_map = new HashMap(11, 1.0F);
            try
            {
                m_dfType = getDfSession().getType(s);
            }
            catch(DfException dfexception)
            {
                throw new WrapperRuntimeException("Failed to get object type: " + s, dfexception);
            }
        }
    }

    public SubmitBatchComponent()
    {
        m_strFolderId = "";
        m_strType = "";
        m_strFilenameWithPath = "";
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        m_strFolderId = argumentlist.get("objectId");
        m_strType = argumentlist.get("type");
        m_strFilenameWithPath = argumentlist.get("filenameWithPath");

/* Probably we can set the type on JSP with this - check it out
        DocbaseObject docbaseobject = (DocbaseObject)getControl("obj", com.documentum.web.formext.control.docbase.DocbaseObject.class);
        docbaseobject.setType(argumentlist.get("type"));
*/
        HelperClass.porticoOutput("Time End onInit="+HelperClass.getThisDateTime());
    }

    public void onRender()
    {
        super.onRender();
    }

    public boolean onCommitChanges()
    {
        return getIsValid();
    }

    // important
    public void onXMLAppListUpdated(Control control, ArgumentList argumentlist)
    {
        LinkDetectorApplet linkdetectorapplet = (LinkDetectorApplet)getControl("xmlappdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
        String s = (String)linkdetectorapplet.getResult();
        // populateCategories(s);
        Control control1 = getContainer();
        if(control1 instanceof ComboContainer)
            ((ComboContainer)control1).resumeAutoCommit();
        setComponentPage("start");
    }

    public String getFolderId()
    {
        return m_strFolderId;
    }

    public String getFilePath()
    {
        return m_strFilenameWithPath;
    }

    // important
    public String getType()
    {
        return m_strType;
    }


    private String m_strFolderId;
    private String m_strFilenameWithPath;
    private String m_strType;
}