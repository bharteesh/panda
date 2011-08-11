
package org.portico.conprep.ui.profile;

public class ProfileUI
{
    private String profileID = "";
    private String profileName = "";
    private String contentType = "";

    public ProfileUI()
    {

	}
	public void setProfileID(String profileID)
	{
		this.profileID = profileID;
	}
	public String getProfileID()
	{
		return profileID;
	}
	public void setProfileName(String profileName)
	{
		this.profileName = profileName;
	}
	public String getProfileName()
	{
		return profileName;
	}
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}
	public String getContentType()
	{
		return contentType;
	}
}