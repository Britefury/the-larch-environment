//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class BrowserContext
{
	private List<LocationResolver> resolvers = new ArrayList<LocationResolver>();
	
	
	public BrowserContext()
	{
	}
	
	public BrowserContext(List<LocationResolver> resolvers)
	{
		this.resolvers.addAll( resolvers );
	}
	
	
	
	public static BrowserContext browserContextWithSystemPages()
	{
		return new BrowserContext( Arrays.asList( new LocationResolver[] { SystemLocationResolver.getSystemResolver() } ) );
	}
	
	public static BrowserContext browserContextWithSystemPages(List<LocationResolver> resolvers)
	{
		ArrayList<LocationResolver> rs = new ArrayList<LocationResolver>();
		rs.add( SystemLocationResolver.getSystemResolver() );
		rs.addAll( resolvers );
		return new BrowserContext( rs );
	}
	
	
	
	protected void addResolvers(List<LocationResolver> resolvers)
	{
		this.resolvers.addAll( resolvers );
	}
	
	
	public Page resolveLocationAsPage(Location location)
	{
		for (LocationResolver resolver: resolvers)
		{
			Page p = resolver.resolveLocationAsPage( location );
			if ( p != null )
			{
				return p;
			}
		}
		
		if ( location.getLocationString().equals( "" ) )
		{
			return defaultRootPage;
		}
		else
		{
			return new ResolveErrorPage( location );
		}
	}
	
	
	
	
	
	
	
	
	private static class DefaultRootPage extends Page
	{
		public String getTitle()
		{
			return "Default";
		}

		
		public DPElement getContentsElement()
		{
			DPText title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 32 ) ).withTextSmallCaps( true ).staticText( "Default Root Page" );
			
			DPText contents = styleSheet.withFont( new Font( "SansSerif", Font.PLAIN, 16 ) ).staticText( "Empty document" );
			DPVBox contentBox = styleSheet.withVBoxSpacing( 40.0 ).vbox( Arrays.asList( new DPElement[] { title.alignHCentre(), contents.alignHExpand() } ) );

			DPVBox pageBox = styleSheet.vbox( Arrays.asList( new DPElement[] { SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE ),  contentBox.alignHExpand() } ) );
			
			return pageBox.alignHExpand();
		}
	}
	
	
	
	private static class ResolveErrorPage extends Page
	{
		private String location;
		
		public ResolveErrorPage(Location location)
		{
			this.location = location.getLocationString();
		}
		
		
		public String getTitle()
		{
			return "Error";
		}

		public DPElement getContentsElement()
		{
			DPText title = styleSheet.withFont( new Font( "Serif", Font.BOLD, 32 ) ).withTextSmallCaps( true ).staticText( "Could Not Resolve Location" );
			
			DPText loc = styleSheet.withFont( new Font( "SansSerif", Font.PLAIN, 16 ) ).staticText( location );
			DPText error = styleSheet.withFont( new Font( "SansSerif", Font.PLAIN, 16 ) ).staticText( "could not be resolved" );
			DPVBox errorBox = styleSheet.withVBoxSpacing( 10.0 ).vbox( Arrays.asList( new DPElement[] { loc.alignHCentre(), error.alignHCentre() } ) );
			
			DPVBox pageBox = styleSheet.withVBoxSpacing( 40.0 ).vbox( Arrays.asList( new DPElement[] { SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ),
					title.padY( 10.0 ).alignHCentre(), errorBox.padY( 10.0 ).alignHCentre() } ) );

			return pageBox.alignHExpand();
		}
	}
	
	
	
	
	private static DefaultRootPage defaultRootPage = new DefaultRootPage();
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
}
