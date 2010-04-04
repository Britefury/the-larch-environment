//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Browser.BrowserContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.DefaultPerspective.GSymDefaultPerspective;
import BritefuryJ.GSym.View.GSymFragmentViewContext;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymBrowserContext
{
	private static class RootLocationFragmentViewFn implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			return PrimitiveStyleSheet.instance.staticText( "<<Root location>>" );
		}
	}
	
	private static class ResolveErrorFragmentViewFn implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentViewContext ctx, StyleSheet styleSheet, AttributeTable state)
		{
			String location = (String)x;
			return resolveErrorStyleSheet.staticText( "<<Could not resolve " + location + ">>" );
		}
	}
	
	
	
	private static class GSymBrowserContextPerspective implements GSymPerspective
	{
		private GSymViewFragmentFunction fragmentViewFn;
		
		
		public GSymBrowserContextPerspective(GSymViewFragmentFunction fragmentViewFn)
		{
			this.fragmentViewFn = fragmentViewFn;
		}
		
		@Override
		public EditHandler getEditHandler()
		{
			return null;
		}

		@Override
		public GSymViewFragmentFunction getFragmentViewFunction()
		{
			return fragmentViewFn;
		}

		@Override
		public StyleSheet getStyleSheet()
		{
			return PrimitiveStyleSheet.instance;
		}
		
		@Override
		public GSymSubject resolveLocation(GSymSubject enclosingSubject, Location.TokenIterator relativeLocation)
		{
			return enclosingSubject;
		}
	}
	
	
	
	
	private class GSymBrowserContextLocationResolver implements LocationResolver
	{
		public Page resolveLocationAsPage(Location location)
		{
			return GSymBrowserContext.this.resolveLocationAsPage( location );
		}
	}
	
	
	private BrowserContext browserContext = new BrowserContext( Arrays.asList( new LocationResolver[] { new GSymBrowserContextLocationResolver() } ) );
	private GSymDefaultPerspective defaultPerspective;
	private List<GSymLocationResolver> resolvers = new ArrayList<GSymLocationResolver>();
	
	
	
	public GSymBrowserContext()
	{
		super();
		defaultPerspective = new GSymDefaultPerspective( this );
		addResolvers( Arrays.asList( new GSymLocationResolver[] { defaultPerspective.getLocationResolver() } ) );
	}
	
	public GSymBrowserContext(List<GSymLocationResolver> resolvers)
	{
		super();
		defaultPerspective = new GSymDefaultPerspective( this );
		addResolvers( Arrays.asList( new GSymLocationResolver[] { defaultPerspective.getLocationResolver() } ) );
		addResolvers( resolvers );
	}
	
	
	
	protected void addResolvers(List<GSymLocationResolver> resolvers)
	{
		this.resolvers.addAll( resolvers );
	}
	
	
	public BrowserContext getBrowserContext()
	{
		return browserContext;
	}
	
	public GSymDefaultPerspective getDefaultPerspective()
	{
		return defaultPerspective;
	}
	
	

	public Location getLocationForObject(Object x)
	{
		return defaultPerspective.getLocationForObject( x );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		return defaultPerspective.getObjectAtLocation( location );
	}
	
	
	
	
	public GSymSubject resolveLocationAsSubject(Location location)
	{
		for (GSymLocationResolver resolver: resolvers)
		{
			GSymSubject e = resolver.resolveLocationAsSubject( location );
			if ( e != null )
			{
				return e;
			}
		}
		
		if ( location.getLocationString().equals( "" ) )
		{
			return new GSymSubject( null, rootLocationPerspective, AttributeTable.instance );
		}
		else
		{
			return new GSymSubject( location, resolveErrorPerspective, AttributeTable.instance );
		}
	}
	
	
	public Page resolveLocationAsPage(Location location)
	{
		for (GSymLocationResolver resolver: resolvers)
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
	private static PrimitiveStyleSheet resolveErrorStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 14 ) ).withForeground( new Color( 0.8f, 0.0f, 0.0f ) );
	private static GSymBrowserContextPerspective rootLocationPerspective = new GSymBrowserContextPerspective( new RootLocationFragmentViewFn() );
	private static GSymBrowserContextPerspective resolveErrorPerspective = new GSymBrowserContextPerspective( new ResolveErrorFragmentViewFn() );
}
