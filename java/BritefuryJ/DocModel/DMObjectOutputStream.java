//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;

public class DMObjectOutputStream
{
	private static class State
	{
		private HashMap<DMSchema, String> moduleToName = new HashMap<DMSchema, String>();
		private HashSet<String> names = new HashSet<String>();
	}
	
	
	private static WeakHashMap<ObjectOutputStream, State> stateTable = new WeakHashMap<ObjectOutputStream, State>();
	
	
	private static State getState(ObjectOutputStream stream)
	{
		State state = stateTable.get( stream );
		if ( state == null )
		{
			state = new State();
			stateTable.put( stream, state );
		}
		return state;
	}
	
	
	
	private static void writeSchemaRef(ObjectOutputStream stream, DMSchema schema) throws IOException
	{
		State state = getState( stream );
		String modName = state.moduleToName.get( schema );
		if ( modName == null )
		{
			String shortName = schema.getShortName();
			modName = shortName;
			int index = 2;
			while ( state.names.contains( modName ) )
			{
				modName = shortName + index;
				index++;
			}
			
			state.moduleToName.put( schema, modName );
			state.names.add( modName );
			
			
			
			stream.writeBoolean( true );
			stream.writeObject( modName );
			stream.writeObject( schema.getLocation() );
			stream.writeObject( schema.getVersion() );
		}
		else
		{
			stream.writeBoolean( false );
			stream.writeObject( modName );
		}
	}

	public static void writeDMObjectClass(ObjectOutputStream stream, DMObjectClass cls) throws IOException
	{
		writeSchemaRef( stream, cls.getSchema() );
		stream.writeObject( cls.getName() );
	}
}
