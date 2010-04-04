//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.HashMap;

import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.DocPresent.Browser.Page;

public class SystemLocationResolver implements LocationResolver
{
	private static String rootLocation = "system";
	private static String prefix = rootLocation + "/";
	
	private HashMap<String, Page> pages = new HashMap<String, Page>();

	private static SystemLocationResolver systemResolver = new SystemLocationResolver();
	
	private SystemRootPage rootPage;
	
	private boolean bInitialised;
	
	
	private SystemLocationResolver()
	{
		bInitialised = false;
	}
	
	
	private void initialise()
	{
		if ( !bInitialised )
		{
			bInitialised = true;

			SystemDirectory.initialise();
			rootPage = new SystemRootPage();
		}
	}
	
	
	public static SystemLocationResolver getSystemResolver()
	{
		systemResolver.initialise();
		return systemResolver;
	}

	
	
	public Page resolveLocationAsPage(Location location)
	{
		Location.TokenIterator iter = location.iterator();
		if ( location.equals( rootLocation ) )
		{
			return rootPage;
		}
		
		Location.TokenIterator iterAfterPrefix = iter.consumeLiteral( prefix );
		if ( iterAfterPrefix != null )
		{
			String systemLocation = iterAfterPrefix.getSuffix();
			return pages.get( systemLocation );
		}
		else
		{
			return null;
		}
	}
	
	
	public void registerPage(String systemLocation, Page page)
	{
		pages.put( systemLocation, page );
	}
	
	
	
	protected static String systemLocationToLocation(String systemLocation)
	{
		return prefix + systemLocation;
	}
}
