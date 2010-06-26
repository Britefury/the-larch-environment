//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class DMObjectInputStream extends ObjectInputStream
{
	protected static class SchemaRef
	{
		private DMSchema schema;
		private int version;
		
		
		public SchemaRef(DMSchema schema, int version)
		{
			this.schema = schema;
			this.version = version;
		}
		
		
		public DMObjectReader getReader(String className)
		{
			return schema.getReader( className, version );
		}
	}

	
	private DMSchemaResolver resolver;
	private HashMap<String, SchemaRef> moduleTable;
	
	
	public DMObjectInputStream(InputStream stream, DMSchemaResolver resolver) throws IOException
	{
		super( stream );
		
		this.resolver = resolver;
		moduleTable = new HashMap<String, SchemaRef>();
	}
	
	
	
	protected SchemaRef readSchemaRef() throws IOException, ClassNotFoundException
	{
		boolean bNewModule = readBoolean();
		
		if ( bNewModule )
		{
			String shortName = (String)readObject();
			String location = (String)readObject();
			Integer version = (Integer)readObject();
			
			DMSchema schema = resolver.getSchema( location );
			
			// Ensure that the requested version is supported
			if ( version > schema.getVersion() )
			{
				// This input data uses a newer schema version than the one we have available here.
				// We cannot load this.
				throw new DMSchema.UnsupportedSchemaVersionException( location, schema.getVersion(), version );
			}
			
			SchemaRef schemaRef = new SchemaRef( schema, version );
			moduleTable.put( shortName, schemaRef );
			return schemaRef;
		}
		else
		{
			String shortName = (String)readObject();
			return moduleTable.get( shortName );
		}
	}
	
	protected DMObjectReader readDMObjectReader() throws IOException, ClassNotFoundException
	{
		SchemaRef schemaRef = readSchemaRef();
		
		String className = (String)readObject();
		
		return schemaRef.getReader( className );
	}
}
