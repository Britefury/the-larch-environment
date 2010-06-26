//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

public class DMObjectOutputStream extends ObjectOutputStream
{
	private HashMap<DMSchema, String> moduleToName;
	private HashSet<String> names;
	
	
	
	public DMObjectOutputStream(OutputStream out) throws IOException
	{
		super( out );
		
		moduleToName = new HashMap<DMSchema, String>();
		names = new HashSet<String>();
	}
	
	
	private void writeSchemaRef(DMSchema schema) throws IOException
	{
		String modName = moduleToName.get( schema );
		if ( modName == null )
		{
			String shortName = schema.getShortName();
			modName = shortName;
			int index = 2;
			while ( names.contains( modName ) )
			{
				modName = shortName + index;
				index++;
			}
			
			moduleToName.put( schema, modName );
			names.add( modName );
			
			
			
			writeBoolean( true );
			writeObject( modName );
			writeObject( schema.getLocation() );
			writeObject( (Integer)schema.getVersion() );
		}
		else
		{
			writeBoolean( false );
			writeObject( modName );
		}
	}

	public void writeDMObjectClass(DMObjectClass cls) throws IOException
	{
		writeSchemaRef( cls.getSchema() );
		writeObject( cls.getName() );
	}
}
