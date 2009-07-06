//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.HashMap;

import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.DocPresent.Browser.Page;

public class SystemLocationResolver implements LocationResolver
{
	private static String prefix = "system.";
	
	private HashMap<String, SystemPage> pages = new HashMap<String, SystemPage>();

	private static SystemLocationResolver systemResolver = new SystemLocationResolver();
	
	static
	{
		SystemDirectory.initialise();
	}
	
	
	
	private SystemLocationResolver()
	{
	}
	
	
	public static SystemLocationResolver getSystemResolver()
	{
		return systemResolver;
	}

	
	
	public Page resolveLocation(String location)
	{
		if ( location.startsWith( prefix ) )
		{
			String systemLocation = location.substring( prefix.length() );
			return pages.get( systemLocation );
		}
		else
		{
			return null;
		}
	}
	
	
	public void registerPage(String systemLocation, SystemPage page)
	{
		pages.put( systemLocation, page );
	}
}
