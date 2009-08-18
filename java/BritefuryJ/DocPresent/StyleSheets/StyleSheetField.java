//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public abstract class StyleSheetField
{
	public static class ValueTypeException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public ValueTypeException(String message)
		{
			super( message );
		}
	}
	
	
	
	protected int index;
	protected String name;
	protected Class<?> valueClass;
	
	
	protected StyleSheetField(String name, Class<?> valueClass)
	{
		index = -1;
		this.name = name;
		this.valueClass = valueClass;
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	public Class<?> getValueClass()
	{
		return valueClass;
	}
	
	
	protected void register(int index)
	{
		this.index = index;
	}
	
	
	protected boolean isRegistered()
	{
		return index != -1;
	}

	
	
	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof StyleSheetField )
		{
			StyleSheetField f = (StyleSheetField)x;
			
			return name.equals( f.name )  &&  valueClass.equals( f.valueClass );
		}
		else
		{
			return false;
		}
	}
}
