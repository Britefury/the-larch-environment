//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.util.Comparator;

public abstract class AttributeBase
{
	public static class AttributeNameComparator implements Comparator<AttributeBase>
	{
		@Override
		public int compare(AttributeBase o1, AttributeBase o2)
		{
			return o1.getFullName().compareTo( o2.getFullName() );
		}
	}
	
	
	protected AttributeNamespace namespace;
	protected String  name;
	protected Class<?> valueClass;
	protected Object defaultValue;
	
	
	public AttributeBase(AttributeNamespace namespace, String name, Object defaultValue)
	{
		this( namespace, name, null, defaultValue );
	}
	
	public AttributeBase(AttributeNamespace namespace, String name, Class<?> valueClass, Object defaultValue)
	{
		this.namespace = namespace;
		this.name = name;
		this.valueClass = valueClass;
		this.defaultValue = defaultValue;
	}
	
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFullName()
	{
		return namespace.getName() + "." + name;
	}
	
	public Class<?> getValueClass()
	{
		return valueClass;
	}
	
	public Object getDefaultValue()
	{
		return defaultValue;
	}
	
	
	
	abstract protected Object checkValue(Object value);
	
	
	protected AttributeTable use(AttributeTable attributeTable)
	{
		return attributeTable;
	}



	protected void notifyBadAttributeType(Object value, Class<?> expectedType)
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should have value of type '" + expectedType.getName() + "', has value '" + value + "'; type '" + value.getClass().getName() + "'" );
	}

	protected void notifyAttributeShouldNotBeNull(Class<?> expectedType)
	{
		if ( expectedType == null )
		{
			notifyAttributeShouldNotBeNull();
		}
		else
		{
			System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should not have a null value; type='" + expectedType.getName() + "'" );
		}
	}

	protected void notifyAttributeShouldNotBeNull()
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should not have a null value" );
	}
}
