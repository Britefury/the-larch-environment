//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
