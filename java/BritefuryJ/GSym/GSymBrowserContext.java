//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Browser.BrowserContext;
import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.GSym.ObjectView.GSymObjectView;

public class GSymBrowserContext extends BrowserContext
{
	private GSymObjectView objectView;
	
	
	
	public GSymBrowserContext()
	{
		super();
		objectView = new GSymObjectView( this );
		addResolvers( Arrays.asList( new LocationResolver[] { objectView.getLocationResolver() } ) );
	}
	
	public GSymBrowserContext(List<LocationResolver> resolvers)
	{
		super();
		objectView = new GSymObjectView( this );
		addResolvers( Arrays.asList( new LocationResolver[] { objectView.getLocationResolver() } ) );
		addResolvers( resolvers );
	}
	
	
	
	public String getLocationForObject(Object x)
	{
		return objectView.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(String location)
	{
		return objectView.getObjectAtLocation( location );
	}
}
