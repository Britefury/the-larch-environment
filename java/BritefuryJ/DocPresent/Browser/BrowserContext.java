//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;

public class BrowserContext
{
	private List<LocationResolver> resolvers = new ArrayList<LocationResolver>();
	
	
	public BrowserContext()
	{
		this.resolvers.add( SystemLocationResolver.getSystemResolver() );
	}
	
	public BrowserContext(List<LocationResolver> resolvers)
	{
		this();
		this.resolvers.addAll( resolvers );
	}
	
	
	protected void addResolvers(List<LocationResolver> resolvers)
	{
		this.resolvers.addAll( resolvers );
	}
	
	
	public DPElement resolveLocationAsElement(String location)
	{
		for (LocationResolver resolver: resolvers)
		{
			DPElement e = resolver.resolveLocationAsElement( location );
			if ( e != null )
			{
				return e;
			}
		}
		
		return null;
	}
	
	
	public Page resolveLocationAsPage(String location)
	{
		for (LocationResolver resolver: resolvers)
		{
			Page p = resolver.resolveLocationAsPage( location );
			if ( p != null )
			{
				return p;
			}
		}
		
		return null;
	}
}
