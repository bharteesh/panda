package org.publisher.elsevier;


public class ValuePair
{
    String key = "";
    String value = "";
    public ValuePair()
    {
    }

    public void setKey(String tKey)
    {
	key = tKey;
    }

    public String getKey()
    {
	return key;
    }

    public void setValue(String tValue)
    {
	value = tValue;
    }

    public String getValue()
    {
	return value;
    }
}
