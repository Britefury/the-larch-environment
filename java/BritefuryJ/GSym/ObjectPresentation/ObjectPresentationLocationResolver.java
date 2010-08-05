//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectPresentation;

import java.util.HashMap;

import org.python.core.Py;
import org.python.core.PyString;

public class ObjectPresentationLocationResolver
{
	private HashMap<String, GSymObjectPresentationPerspective> perspectiveTable = new HashMap<String, GSymObjectPresentationPerspective>();
	
	
	public ObjectPresentationLocationResolver()
	{
	}
	
	
	public GSymObjectViewLocationTable __getitem__(String key)
	{
		GSymObjectPresentationPerspective perspective = perspectiveTable.get( key );
		if ( perspective == null )
		{
			throw Py.KeyError( "No perspective with class name '" + key + "'" );
		}
		return perspective.getObjectViewLocationTable();
	}
	
	
	public String registerPerspective(GSymObjectPresentationPerspective perspective)
	{
		String className = perspective.getClass().getName();
		
		perspectiveTable.put( className, perspective );
		
		return className;
	}
	
	
	public String getRelativeLocationForObject(GSymObjectPresentationPerspective perspective, Object x)
	{
		String relative = perspective.getObjectViewLocationTable().getRelativeLocationForObject( x );
		
		PyString className = Py.newString( perspective.getClass().getName() );
		
		PyString key = className.__repr__();
		
		return "[" + key + "]" + relative;
	}
}
