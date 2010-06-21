//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.HashMap;
import java.util.regex.Pattern;


public class DMNodeClass
{
	protected static Pattern validNamePattern = Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" );

	
	protected String name;
	protected HashMap<String, DMClassAttribute> classAttributes = new HashMap<String, DMClassAttribute>();
	
	
	
	public DMNodeClass(String name)
	{
		this.name = name.intern();
	}

	
	public String getName()
	{
		return name;
	}
	
	
	public DMNodeClass getSuperclass()
	{
		return null;
	}
	
	public boolean isSubclassOf(DMNodeClass c)
	{
		return false;
	}
	
	
	public DMClassAttribute getClassAttribute(String name)
	{
		return classAttributes.get( name );
	}
	
	
	
	protected void registerClassAttribute(DMClassAttribute attr)
	{
		String attrName = attr.getName();
		if ( classAttributes.containsKey( attrName ) )
		{
			throw new RuntimeException( "Class '" + name + "' already has a class attribute named " + attrName );
		}
		
		classAttributes.put( attrName, attr );
	}
}
