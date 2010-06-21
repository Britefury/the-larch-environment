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
	private DMSchemaResolver resolver;
	private HashMap<String, DMSchema> moduleTable;
	
	
	public DMObjectInputStream(InputStream stream, DMSchemaResolver resolver) throws IOException
	{
		super( stream );
		
		this.resolver = resolver;
		moduleTable = new HashMap<String, DMSchema>();
	}
	
	
	
	protected DMSchema readDMModule() throws IOException, ClassNotFoundException
	{
		boolean bNewModule = readBoolean();
		
		if ( bNewModule )
		{
			String shortName = (String)readObject();
			String location = (String)readObject();
			DMSchema schema = resolver.getSchema( location );
			moduleTable.put( shortName, schema);
			return schema;
		}
		else
		{
			String shortName = (String)readObject();
			return moduleTable.get( shortName );
		}
	}
	
	protected DMObjectClass readDMObjectClass() throws IOException, ClassNotFoundException
	{
		DMSchema schema = readDMModule();
		
		String className = (String)readObject();
		
		return schema.get( className );
	}
}
