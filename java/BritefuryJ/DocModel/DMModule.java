//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.HashMap;

import org.python.core.Py;

public class DMModule
{
	public static class UnknownClassException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	public static class ClassAlreadyDefinedException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	private String moduleName, shortName, moduleLocation;
	private HashMap<String, DMObjectClass> classes;
	
	
	
	public DMModule(String name, String shortName, String location)
	{
		this.moduleName = name;
		this.shortName = shortName;
		this.moduleLocation = location;
		classes = new HashMap<String, DMObjectClass>();
	}
	
	
	
	public String getName()
	{
		return moduleName;
	}
	
	public String getShortName()
	{
		return shortName;
	}
	
	public String getLocation()
	{
		return moduleLocation;
	}
	
	
	
	public DMObjectClass get(String name) throws UnknownClassException
	{
		DMObjectClass c = classes.get( name );
		if ( c == null )
		{
			throw new UnknownClassException();
		}
		return c;
	}
	
	public void registerClass(String name, DMObjectClass c) throws ClassAlreadyDefinedException
	{
		if ( classes.containsKey( name ) )
		{
			throw new ClassAlreadyDefinedException();
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
	
	
	
	public DMObjectClass newClass(String name, DMObjectField fields[]) throws ClassAlreadyDefinedException
	{
		return new DMObjectClass( this, name, fields );
	}

	public DMObjectClass newClass(String name, String fieldNames[]) throws ClassAlreadyDefinedException
	{
		return new DMObjectClass( this, name, fieldNames );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, DMObjectField fields[]) throws ClassAlreadyDefinedException
	{
		return new DMObjectClass( this, name, superClass, fields );
	}

	public DMObjectClass newClass(String name, DMObjectClass superClass, String fieldNames[]) throws ClassAlreadyDefinedException
	{
		return new DMObjectClass( this, name, superClass, fieldNames );
	}
}
