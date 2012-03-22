//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.HashMap;

public class DMSchemaResolver
{
	private static HashMap<String, DMSchema> schemaTable = new HashMap<String, DMSchema>();
	
	
	public static class CouldNotResolveSchemaException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public CouldNotResolveSchemaException(String schemaName)
		{
			super( "Could not resolve schema: " + schemaName );
		}
	}
	
	public static DMSchema getSchema(String location) throws CouldNotResolveSchemaException
	{
		DMSchema schema = schemaTable.get( location );
		if ( schema == null )
		{
			throw new CouldNotResolveSchemaException( location );
		}
		return schema;
	}
	
	
	
	protected static void registerSchema(DMSchema schema)
	{
		if ( schemaTable.containsKey( schema.getLocation() ) )
		{
			System.err.println( "Registering new document model schema at location " + schema.getLocation() );
		}
		schemaTable.put( schema.getLocation(), schema );
	}
}
