//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.python.core.Py;


public class AttributeNamespace
{
	protected static Pattern validNamePattern = Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" );

	
	public static class InvalidAttributeNamespaceNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidAttributeNamespaceNameException(String message)
		{
			super( message );
		}
	}

	
	public static class NamespaceAlreadyExistsException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public NamespaceAlreadyExistsException(String message)
		{
			super( message );
		}
	};
	
	
	private String name;
	private HashMap<String, AttributeBase> attributes = new HashMap<String, AttributeBase>();
	
	
	
	
	public AttributeNamespace(String name)
	{
		if ( !validNamePattern.matcher( name ).matches() )
		{
			throw new InvalidAttributeNamespaceNameException( "Invalid attribute namespace name '" + name + "'; name should be an identifier" );
		}
		this.name = name;
		GlobalAttributeRegistry.registerNamespace( this );
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	
	protected void registerAttribute(AttributeBase attribute)
	{
		String attrName = attribute.getName();
		if ( attributes.containsKey( attrName ) )
		{
			throw new AttributeBase.AttributeAlreadyExistsException( "The namespace " + name + " already contains an attribute under the name " + attrName );
		}
		attributes.put( attrName, attribute );
		GlobalAttributeRegistry.registerAttribute( attribute );
	}
	
	
	public AttributeBase get(String name)
	{
		return attributes.get( name );
	}
	
	public AttributeBase __getitem__(String name)
	{
		AttributeBase attr = get( name );
		if ( attr == null )
		{
			throw Py.KeyError( "No attribute named " + name );
		}
		return attr;
	}
}
