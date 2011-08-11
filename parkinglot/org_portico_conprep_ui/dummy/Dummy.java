/*
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 * Project        	ConPrep WebTop
 * Module         	Dummy
 * File           	Dummy.java
 * Created on 		Dec 15 2004
 *
 */
package org.portico.conprep.ui.dummy;

import java.util.ArrayList;

import org.portico.conprep.ui.helper.HelperClass;
import org.portico.conprep.ui.helper.ValuePair;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.formext.component.Component;

/**
 * Description	temporary class to handle unimplemented components
 * Author		pramaswamy
 * Type			Dummy
 */
public class Dummy extends Component {
    public Dummy() {
    }
    public void onInit( ArgumentList arg ){
		DataProvider dataProvider = ((DataDropDownList)getControl("drop", DataDropDownList.class)).getDataProvider();
		dataProvider.setDfSession(getDfSession());
		dataProvider.setQuery("select name, code from priya.country");
		dataProvider.refresh();
    }
	public void onOk( Button button, ArgumentList arg )	{
		ArrayList outList = null;
		ValuePair tValuePair = null;
		ArrayList attrList = new ArrayList();
		String serviceType = "";
		String serviceId = "";

		serviceType = "provider";
		serviceId = "PR-2";
		attrList.add("name");
		attrList.add("desc");
		attrList.add("abc");

		outList = HelperClass.lookupServiceInfo(serviceType, serviceId, attrList);
		HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo for-"+serviceType+"::"+serviceId);
		if(outList != null & outList.size() > 0)
		{
            for(int indx=0; indx < outList.size(); indx++)
            {
			    tValuePair = (ValuePair)outList.get(indx);
				HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo-(provider)list="+tValuePair.getKey()+"::"+tValuePair.getValue());
			}
		}

		serviceType = "profile";
		serviceId = "SP-2";
		attrList.clear();
		attrList.add("name");
		attrList.add("desc");
		attrList.add("def");

		outList = HelperClass.lookupServiceInfo(serviceType, serviceId, attrList);
		HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo for-"+serviceType+"::"+serviceId);
		if(outList != null & outList.size() > 0)
		{
            for(int indx=0; indx < outList.size(); indx++)
            {
			    tValuePair = (ValuePair)outList.get(indx);
				HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo-(profile)list="+tValuePair.getKey()+"::"+tValuePair.getValue());
			}
		}

		serviceType = "format";
		serviceId = "g1";
		attrList.clear();
		attrList.add("name");
		attrList.add("desc");
		attrList.add("def");

		outList = HelperClass.lookupServiceInfo(serviceType, serviceId, attrList);
		HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo for-"+serviceType+"::"+serviceId);
		if(outList != null & outList.size() > 0)
		{
            for(int indx=0; indx < outList.size(); indx++)
            {
			    tValuePair = (ValuePair)outList.get(indx);
				HelperClass.porticoOutput("Dummy-Call HelperClass.lookupServiceInfo-(format)list="+tValuePair.getKey()+"::"+tValuePair.getValue());
			}
		}



		getTopForm().setFormReturn();
	}
}