
package org.portico.conprep.ui.main;

import org.portico.conprep.ui.helper.HelperClass;

import com.documentum.web.common.ArgumentList;
import com.documentum.webtop.webcomponent.main.Main;

public class MainEntry extends Main
{
	/*
	static{
		try{
			// Note: 'ccsf.createService()' loads the character conversion class and loads into memory
			//        static block, so that subsequent calls will not keep creating this class again
			//        so performance could be okay
            	CharacterConversionServiceFactory ccsf = CharacterConversionUtil.getFactory();
    			ccsf.createService();
			

		}catch(Exception e){}
	}
*/
    public MainEntry()
    {
		super();
    }

    public void onInit(ArgumentList argumentlist)
    {
        super.onInit(argumentlist);
        // Do not remove this System.out.println, it is a start marker in System.out log file and DfLogger log file
        System.out.println(HelperClass.getThisDateTime() + " " + "ConPrep UI ....:Initializing in Portico-Conprep-Ui-MainComponent........................................");
        HelperClass.porticoOutput(0, "Initializing in Portico-Conprep-Ui-MainComponent........................................");
        String submissionAreaName = HelperClass.getSubmissionAreaName(getDfSession());
        HelperClass.porticoOutput(0, "MainEntry-onInit()-submissionAreaName="+submissionAreaName);
        String submissionAreaId = HelperClass.createCabinetObject(getDfSession(), submissionAreaName);
        HelperClass.porticoOutput(0, "MainEntry-onInit()-submissionAreaId="+submissionAreaId);
		HelperClass.LoadSessionInformation(getDfSession());
    }
}