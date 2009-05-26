//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

public class DMObjectField
{
	private String name;
	
	
	public DMObjectField(String name)
	{
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public boolean equals(Object x)
	{
		if ( this == x )
		{
			return true;
		}
		
		if ( x instanceof DMObjectField )
		{
			DMObjectField fx = (DMObjectField)x;
			return name.equals( fx.name );
		}
		return false;
	}
	
	
	
	public static DMObjectField[] nameArrayToFieldArray(String fieldNames[])
	{
		DMObjectField f[] = new DMObjectField[fieldNames.length];
		for (int i = 0; i < fieldNames.length; i++)
		{
			f[i] = new DMObjectField( fieldNames[i] );
		}
		return f;
	}
}
