//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;

public class AttributeWithValue
{
	protected AttributeBase attribute;
	protected Object value;
	
	
	public AttributeWithValue(AttributeBase attribute, Object value)
	{
		this.attribute = attribute;
		this.value = attribute.checkValue( value );
	}
	
	
	public AttributeBase getAttribute()
	{
		return attribute;
	}
	
	public Object getValue()
	{
		return value;
	}
	
	
	public String toString()
	{
		return attribute.toString() + "=" + value.toString();
	}
}
