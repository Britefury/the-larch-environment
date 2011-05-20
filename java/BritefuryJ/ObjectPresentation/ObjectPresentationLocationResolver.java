//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ObjectPresentation;

import java.util.HashMap;
import java.util.IdentityHashMap;

import org.python.core.Py;
import org.python.core.PyString;

public class ObjectPresentationLocationResolver
{
	private HashMap<String, ObjectPresentationPerspective> perspectiveTable = new HashMap<String, ObjectPresentationPerspective>();
	private HashMap<ObjectPresentationPerspective, String> perspectiveNameTable = new HashMap<ObjectPresentationPerspective, String>();
	private IdentityHashMap<ObjectPresentationPerspective, ObjectViewLocationTable> locationTables = new IdentityHashMap<ObjectPresentationPerspective, ObjectViewLocationTable>();
	
	
	public ObjectPresentationLocationResolver()
	{
	}
	
	
	public ObjectViewLocationTable __resolve__(PyString key)
	{
		String perspectiveName = key.asString();
		ObjectPresentationPerspective perspective = perspectiveTable.get( perspectiveName );
		if ( perspective == null )
		{
			throw Py.KeyError( "No perspective named '" + perspectiveName + "'" );
		}
		return  locationTables.get( perspective );
	}
	
	
	public String getRelativeLocationForObject(ObjectPresentationPerspective perspective, Object x)
	{
		String perspectiveName = getPerspectiveName( perspective );
		
		ObjectViewLocationTable locationTable = locationTables.get( perspective );
		if ( locationTable == null )
		{
			locationTable = new ObjectViewLocationTable();
			locationTables.put( perspective, locationTable );
		}


		String relative = locationTable.getRelativeLocationForObject( x );
		
		return "." + perspectiveName + relative;
	}
	
	
	
	private String getPerspectiveName(ObjectPresentationPerspective perspective)
	{
		String perspectiveName = perspectiveNameTable.get( perspective );
		
		if ( perspectiveName == null )
		{
			String className = perspective.getClass().getName();
			className = className.substring( className.lastIndexOf( "." ) + 1 );

			perspectiveName = className;
			if ( perspectiveTable.containsKey( perspectiveName ) )
			{
				int i = 2;
				perspectiveName = className + i;
				while ( perspectiveTable.containsKey( perspectiveName ) )
				{
					i++;
					perspectiveName = className + i;
				}
				perspectiveTable.put( perspectiveName, perspective );
				perspectiveNameTable.put( perspective, perspectiveName );
			}
			else
			{
				perspectiveTable.put( perspectiveName, perspective );
				perspectiveNameTable.put( perspective, perspectiveName );
			}
		}
		
		return perspectiveName;
	}
}
