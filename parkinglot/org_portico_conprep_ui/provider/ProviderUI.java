
package org.portico.conprep.ui.provider;
import java.io.Serializable;
import java.util.ArrayList;

import org.portico.conprep.ui.profile.ProfileUI;

public class ProviderUI implements Serializable
{
    private ArrayList listProfileUI = new ArrayList();
    private String defaultProfileID;
    private String providerID;
    private String providerName;

    public ProviderUI()
    {

	}
	public ArrayList getListProfileUI()
	{
		return listProfileUI;
	}
	public void setProfileUI(ProfileUI profileUI)
	{
		listProfileUI.add(profileUI);
	}
	public void setDefaultProfileID(String profileID)
	{
		defaultProfileID = profileID;
	}
	public String getDefaultProfileID()
	{
		return defaultProfileID;
	}
	public void setProviderID(String providerID)
	{
		this.providerID = providerID;
	}
	public String getProviderID()
	{
		return providerID;
	}
	public void setProviderName(String providerName)
	{
		this.providerName = providerName;
	}
	public String getProviderName()
	{
		return providerName;
	}
}
