package org.portico.conprep.ui.helper;
import java.util.ArrayList;

public class SubmissionPatternResultItem
{
    private String thisToken = "";
    private String parentToken = "";
    private String originalToken = "";


    // 'childTokens' not populated for this process
    private ArrayList childTokens = new ArrayList();

    public void setThisToken(String tToken)
    {
		thisToken = tToken;
	}
    public String getThisToken()
    {
		return thisToken;
	}
    public void setParentToken(String tToken)
    {
		parentToken = tToken;
	}
    public String getParentToken()
    {
		return parentToken;
	}
    public void setOriginalToken(String tOrgToken)
    {
		originalToken = tOrgToken;
	}
    public String getOriginalToken()
    {
		return originalToken;
	}


    public void addItems(String tToken)
    {
		childTokens.add(tToken);
	}
    public ArrayList getItems()
    {
		return childTokens;
	}
}
