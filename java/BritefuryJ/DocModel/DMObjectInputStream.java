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

import BritefuryJ.DocModel.DMModule.UnknownClassException;
import BritefuryJ.DocModel.DMModuleResolver.CouldNotResolveModuleException;

public class DMObjectInputStream extends ObjectInputStream
{
	private DMModuleResolver resolver;
	private HashMap<String, DMModule> moduleTable;
	
	
	public DMObjectInputStream(InputStream stream, DMModuleResolver resolver) throws IOException
	{
		super( stream );
		
		this.resolver = resolver;
		moduleTable = new HashMap<String, DMModule>();
	}
	
	
	
	protected DMModule readDMModule() throws IOException, ClassNotFoundException, CouldNotResolveModuleException
	{
		boolean bNewModule = readBoolean();
		
		if ( bNewModule )
		{
			String shortName = (String)readObject();
			String location = (String)readObject();
			DMModule module = resolver.getModule( location );
			moduleTable.put( shortName, module );
			return module;
		}
		else
		{
			String shortName = (String)readObject();
			return moduleTable.get( shortName );
		}
	}
	
	protected DMObjectClass readDMObjectClass() throws IOException, ClassNotFoundException, CouldNotResolveModuleException, UnknownClassException
	{
		DMModule module = readDMModule();
		
		String className = (String)readObject();
		
		return module.get( className );
	}
}
