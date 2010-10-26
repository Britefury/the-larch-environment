//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.WeakHashMap;

public class DMObjectInputStream
{
	private static class SchemaRef
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

	
	private static class State
	{
		private HashMap<String, SchemaRef> moduleTable = new HashMap<String, SchemaRef>();
	}
	
	
	
	
	private static WeakHashMap<ObjectInputStream, State> stateTable = new WeakHashMap<ObjectInputStream, State>();

	
	private static State getState(ObjectInputStream stream)
	{
		State state = stateTable.get( stream );
		if ( state == null )
		{
			state = new State();
			stateTable.put( stream, state );
		}
		return state;
	}
	
	
	private static SchemaRef readSchemaRef(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		State state = getState( stream );
		boolean bNewModule = stream.readBoolean();
		
		if ( bNewModule )
		{
			String shortName = (String)stream.readObject();
			String location = (String)stream.readObject();
			Integer version = (Integer)stream.readObject();
			
			DMSchema schema = DMSchemaResolver.getSchema( location );
			
			// Ensure that the requested version is supported
			if ( version > schema.getVersion() )
			{
				// This input data uses a newer schema version than the one we have available here.
				// We cannot load this.
				throw new DMSchema.UnsupportedSchemaVersionException( location, schema.getVersion(), version );
			}
			
			SchemaRef schemaRef = new SchemaRef( schema, version );
			state.moduleTable.put( shortName, schemaRef );
			return schemaRef;
		}
		else
		{
			String shortName = (String)stream.readObject();
			return state.moduleTable.get( shortName );
		}
	}
	
	protected static DMObjectReader readDMObjectReader(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		SchemaRef schemaRef = readSchemaRef( stream );
		
		String className = (String)stream.readObject();
		
		return schemaRef.getReader( className );
	}
}
