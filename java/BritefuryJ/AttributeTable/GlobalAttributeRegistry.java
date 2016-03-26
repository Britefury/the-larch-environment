//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	
	
	protected static void registerAttribute(AttributeBase attribute)
	{
		String fullName = attribute.getFullName();
		if ( attributes.containsKey( fullName ) )
		{
			throw new AttributeBase.AttributeAlreadyExistsException( "An attribute already exists under the name " + fullName );
		}
		attributes.put( fullName, attribute );
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
