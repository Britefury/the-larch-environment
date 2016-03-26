//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;

public class InheritedAttribute extends AttributeBase
{
	public InheritedAttribute(AttributeNamespace namespace, String name, Object defaultValue)
	{
		super( namespace, name, defaultValue );
	}
	
	public InheritedAttribute(AttributeNamespace namespace, String name, Class<?> valueClass, Object defaultValue)
	{
		super( namespace, name, valueClass, defaultValue );
	}

	
	@Override
	protected Object checkValue(Object value)
	{
		if ( valueClass != null )
		{
			try
			{
				return valueClass.cast( value );
			}
			catch (ClassCastException e)
			{
				notifyBadAttributeType( value, valueClass );
				return defaultValue;
			}
		}

		return value;
	}
}
