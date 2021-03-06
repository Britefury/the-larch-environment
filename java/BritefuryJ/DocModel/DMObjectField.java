//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.util.HashSet;

public class DMObjectField
{
	public static class InvalidFieldNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidFieldNameException(String message)
		{
			super( message );
		}
	}

	
	private static final HashSet<String> disallowedFieldNames = new HashSet<String>();

	
	static
	{
	}

	
	
	private String name;
	
	
	
	
	public DMObjectField(String name)
	{
		checkFieldNameValidity( name );
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
	
	
	
	private static void checkFieldNameValidity(String name)
	{
		if ( DMNodeClass.validNamePattern.matcher( name ).matches() )
		{
			if ( disallowedFieldNames.contains( name ) )
			{
				throw new InvalidFieldNameException( "Invalid field name '" + name + "'; name cannot be any of " + disallowedFieldNames );
			}
		}
		else
		{
			throw new InvalidFieldNameException( "Invalid field name '" + name + "'; name should be an identifier" );
		}
	}
}
