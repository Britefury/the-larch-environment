//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.HashMap;
import java.util.HashSet;

import org.python.core.Py;

public class DMSchema
{
	public static class UnknownClassException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public UnknownClassException(String className)
		{
			super( "Unknown class '" + className + "'" );
		}
	}

	public static class ClassAlreadyDefinedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public ClassAlreadyDefinedException(String className)
		{
			super( "Class '" + className + "' already defined" );
		}
	}

	
	
	
	public static class InvalidSchemaNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidSchemaNameException(String message)
		{
			super( message );
		}
	}

	
	private static final HashSet<String> disallowedSchemaNames = new HashSet<String>();

	
	static
	{
	}

	
	
	
	private String schemaName, shortName, moduleLocation;
	private HashMap<String, DMObjectClass> classes;
	
	
	
	public DMSchema(String name, String shortName, String location)
	{
		checkSchemaNameValidity( name );
		this.schemaName = name;
		this.shortName = shortName;
		this.moduleLocation = location;
		classes = new HashMap<String, DMObjectClass>();
	}
	
	
	
	public String getName()
	{
		return schemaName;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public String getLocation()
	{
		return moduleLocation;
	}
	
	
	
	public DMObjectClass get(String name)
	{
		DMObjectClass c = classes.get( name );
		if ( c == null )
		{
			throw new UnknownClassException( name );
		}
		return c;
	}
	
	protected void registerClass(String name, DMObjectClass c)
	{
		if ( classes.containsKey( name ) )
		{
			throw new ClassAlreadyDefinedException( name );
		}
		classes.put( name, c );
	}
	
	
	
	public DMObjectClass __getitem__(String name)
	{
		DMObjectClass c = classes.get( name );
		if ( c == null )
		{
			throw Py.KeyError( name );
		}
		return c;
	}
	
	
	
	public DMObjectClass newClass(String name, DMObjectField fields[])
	{
		return new DMObjectClass( this, name, fields );
	}

	public DMObjectClass newClass(String name, String fieldNames[])
	{
		return new DMObjectClass( this, name, fieldNames );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, DMObjectField fields[])
	{
		return new DMObjectClass( this, name, superClass, fields );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, String fieldNames[])
	{
		return new DMObjectClass( this, name, superClass, fieldNames );
	}



	
	
	
	private static void checkSchemaNameValidity(String name)
	{
		if ( DMNodeClass.validNamePattern.matcher( name ).matches() )
		{
			if ( disallowedSchemaNames.contains( name ) )
			{
				throw new InvalidSchemaNameException( "Invalid schema name '" + name + "'; name cannot be any of " + disallowedSchemaNames );
			}
		}
		else
		{
			throw new InvalidSchemaNameException( "Invalid schema name '" + name + "'; name should be an identifier" );
		}
	}
}
