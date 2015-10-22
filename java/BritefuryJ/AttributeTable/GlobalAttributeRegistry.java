//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import java.util.HashMap;

public class GlobalAttributeRegistry
{
	private static HashMap<String, AttributeNamespace> namespaces = new HashMap<String, AttributeNamespace>();
	private static HashMap<String, AttributeBase> attributes = new HashMap<String, AttributeBase>();
	
	
	protected static void registerNamespace(AttributeNamespace namespace)
	{
		String name = namespace.getName();
		if ( namespaces.containsKey( name ) )
		{
			throw new AttributeNamespace.NamespaceAlreadyExistsException( "An attribute namespace already exists under the name " + name );
		}
		namespaces.put( name, namespace );
	}
	
	protected static void unregisterNamespace(AttributeNamespace namespace)
	{
		String name = namespace.getName();
		if ( !namespaces.containsKey( name ) )
		{
			throw new AttributeNamespace.NamespaceAlreadyExistsException( "No attribute namespace exists under the name " + name );
		}
		namespaces.remove( name );
	}


	protected static void registerAttribute(AttributeBase attribute)
	{
		String fullName = attribute.getFullName();
		if ( attributes.containsKey( fullName ) )
		{
			throw new AttributeBase.AttributeAlreadyExistsException( "An attribute already exists under the name " + fullName );
		}
		attributes.put( fullName, attribute );
	}
	
	protected static void unregisterAttribute(AttributeBase attribute)
	{
		String fullName = attribute.getFullName();
		if ( !attributes.containsKey( fullName ) )
		{
			throw new AttributeBase.AttributeAlreadyExistsException( "No attribute exists under the name " + fullName );
		}
		attributes.remove( fullName );
	}


	public static AttributeNamespace getNamespace(String name)
	{
		return namespaces.get( name );
	}
	
	public static AttributeBase getAttribute(String fullName)
	{
		return attributes.get( fullName );
	}
}
