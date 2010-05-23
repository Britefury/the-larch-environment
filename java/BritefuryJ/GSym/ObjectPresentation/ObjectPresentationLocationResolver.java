//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.ObjectPresentation;

import java.util.HashMap;

import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.GSym.GSymLocationResolver;
import BritefuryJ.GSym.GSymSubject;

public class ObjectPresentationLocationResolver implements GSymLocationResolver
{
	private HashMap<String, GSymObjectPresentationPerspective> perspectiveTable = new HashMap<String, GSymObjectPresentationPerspective>();
	
	
	public ObjectPresentationLocationResolver()
	{
	}
	
	
	public String registerPerspective(GSymObjectPresentationPerspective perspective)
	{
		String className = perspective.getClass().getName();
		
		perspectiveTable.put( className, perspective );
		
		return className;
	}
	
	
	public Location getLocationForObject(GSymObjectPresentationPerspective perspective, Object x)
	{
		String relative = perspective.getRelativeLocationForObject( x );
		return new Location( "$objects/" + perspective.getClass().getName() + "/" + relative );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		Location.TokenIterator iterator = location.iterator();
		iterator = iterator.consumeLiteral( "$objects/" );
		
		if ( iterator != null )
		{
			Location.TokenIterator afterClassNameIter = iterator.consumeUpTo( "/" );
			if ( afterClassNameIter != null )
			{
				Location.TokenIterator afterSlashIter = afterClassNameIter.consumeLiteral( "/" );
				
				if ( afterSlashIter != null )
				{
					String className = afterClassNameIter.lastToken();
					
					GSymObjectPresentationPerspective perspective = perspectiveTable.get( className );
					
					if ( perspective != null )
					{
						return perspective.getObjectAtRelativeLocation( afterSlashIter );
					}
				}
			}
		}
		
		return null;
	}
	

	
	@Override
	public GSymSubject resolveLocationAsSubject(Location location)
	{
		Location.TokenIterator iterator = location.iterator();
		iterator = iterator.consumeLiteral( "$objects/" );
		
		if ( iterator != null )
		{
			Location.TokenIterator afterClassNameIter = iterator.consumeUpTo( "/" );
			if ( afterClassNameIter != null )
			{
				Location.TokenIterator afterSlashIter = afterClassNameIter.consumeLiteral( "/" );
				
				if ( afterSlashIter != null )
				{
					String className = afterClassNameIter.lastToken();
					
					GSymObjectPresentationPerspective perspective = perspectiveTable.get( className );
					
					if ( perspective != null )
					{
						return perspective.resolveRelativeLocation( null, afterSlashIter );
					}
				}
			}
		}
		
		return null;
	}
}
