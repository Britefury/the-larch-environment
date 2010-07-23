//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

public class InheritedAttributeNonNull extends AttributeBase
{
	public InheritedAttributeNonNull(String namespace, String name, Object defaultValue)
	{
		super( namespace, name, defaultValue );
	}
	
	public InheritedAttributeNonNull(String namespace, String name, Class<?> valueClass, Object defaultValue)
	{
		super( namespace, name, valueClass, defaultValue );
	}

	
	@Override
	protected Object checkValue(Object value)
	{
		if ( value == null )
		{
			notifyAttributeShouldNotBeNull( valueClass );
			return defaultValue;
		}
		
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
