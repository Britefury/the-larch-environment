//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import java.awt.Color;
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
import BritefuryJ.DocPresent.Browser.Location.TokenIterator;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GSymGenericPerspective;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.GSym.View.GSymView;
import BritefuryJ.GSym.View.GSymViewFragmentFunction;

public class GSymBrowserContext
{
	private static class RootLocationFragmentViewFn implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
		{
			return PrimitiveStyleSheet.instance.staticText( "<<Root location>>" );
		}
	}
	
	private static class ResolveErrorFragmentViewFn implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
		{
			Location location = (Location)x;
			return resolveErrorStyleSheet.staticText( "<<Could not resolve " + location.getLocationString() + ">>" );
		}
	}
	
	private static class SystemPageFragmentViewFn implements GSymViewFragmentFunction
	{
		public DPElement createViewFragment(Object x, GSymFragmentView ctx, StyleSheet styleSheet, AttributeTable state)
		{
			Page p = (Page)x;
			return p.getContentsElement();
		}
	}
	
	private static class SystemPageRelativeLocationResolver implements GSymRelativeLocationResolver
	{
		private LocationResolver systemLocationResolver;
		
		public SystemPageRelativeLocationResolver(LocationResolver systemLocationResolver)
		{
			this.systemLocationResolver = systemLocationResolver;
		}

		
		@Override
		public GSymSubject resolveRelativeLocation(GSymSubject enclosingSubject, TokenIterator locationIterator)
		{
			Location location = new Location( locationIterator.getSuffix() );
			Page p = systemLocationResolver.resolveLocationAsPage( location, null );
			if ( p != null )
			{
				return enclosingSubject.withFocus( p ).withTitle( p.getTitle() );
			}
			else
			{
				return null;
			}
		}
	}
	
	private class SystemPageLocationResolver implements GSymLocationResolver
	{
		private GSymPerspective perspective;
		
		
		public SystemPageLocationResolver(LocationResolver systemLocationResolver)
		{
			perspective = new GSymPerspective( new SystemPageFragmentViewFn(), new SystemPageRelativeLocationResolver( systemLocationResolver ) );
		}
		
		
		@Override
		public GSymSubject resolveLocationAsSubject(Location location)
		{
			return perspective.resolveRelativeLocation( new GSymSubject( null, perspective, "", AttributeTable.instance, null ), location.iterator() );
		}
	}
	
	
	
	private class GSymBrowserContextLocationResolver implements LocationResolver
	{
		public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
		{
			return GSymBrowserContext.this.resolveLocationAsPage( location, persistentState );
		}
	}
	
	
	private BrowserContext browserContext = new BrowserContext( Arrays.asList( new LocationResolver[] { new GSymBrowserContextLocationResolver() } ) );
	private ObjectPresentationLocationResolver objPresLocationResolver = new ObjectPresentationLocationResolver();
	private GSymGenericPerspective genericPerspective;
	private List<GSymLocationResolver> resolvers = new ArrayList<GSymLocationResolver>();
	
	
	
	public GSymBrowserContext(boolean bWithSystemPages)
	{
		super();
		genericPerspective = new GSymGenericPerspective( objPresLocationResolver );
		if ( bWithSystemPages )
		{
			addResolvers( Arrays.asList( new GSymLocationResolver[] { new SystemPageLocationResolver( SystemLocationResolver.getSystemResolver() ) } ) );
		}
		addResolvers( Arrays.asList( new GSymLocationResolver[] { objPresLocationResolver } ) );
	}
	
	public GSymBrowserContext(boolean bWithSystemPages, List<GSymLocationResolver> resolvers)
	{
		this( bWithSystemPages );
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
	
	public GSymGenericPerspective getGenericPerspective()
	{
		return genericPerspective;
	}
	
	

	public Location getLocationForObject(GSymObjectPresentationPerspective perspective, Object x)
	{
		return objPresLocationResolver.getLocationForObject( perspective, x );
	}
	
	public Location getLocationForObject(Object x)
	{
		return objPresLocationResolver.getLocationForObject( genericPerspective, x );
	}
	
	public Object getObjectAtLocation(Location location)
	{
		return objPresLocationResolver.getObjectAtLocation( location );
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
			return new GSymSubject( null, rootLocationPerspective, "Root page", AttributeTable.instance, null );
		}
		else
		{
			return new GSymSubject( location, resolveErrorPerspective, "Resolve error", AttributeTable.instance, null );
		}
	}
	
	
	public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
	{
		for (GSymLocationResolver resolver: resolvers)
		{
			GSymSubject subject = resolver.resolveLocationAsSubject( location );
			if ( subject != null )
			{
				GSymView view = new GSymView( subject, this, persistentState );
				return view.getPage();
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
			DPText title = styleSheet.withFontFace( "Serif" ).withFontBold( true ).withFontSize( 32 ).withTextSmallCaps( true ).staticText( "Default Root Page" );
			
			DPText contents = styleSheet.withFontSize( 16 ).staticText( "Empty document" );
			DPVBox contentBox = styleSheet.withVBoxSpacing( 40.0 ).vbox( new DPElement[] { title.alignHCentre(), contents.alignHExpand() } );

			DPVBox pageBox = styleSheet.vbox( new DPElement[] { SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE ),  contentBox.alignHExpand() } );
			
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
			DPText title = styleSheet.withFontFace( "Serif" ).withFontBold( true ).withFontSize( 32 ).withTextSmallCaps( true ).staticText( "Could Not Resolve Location" );
			
			DPText loc = styleSheet.withFontSize( 16 ).staticText( location );
			DPText error = styleSheet.withFontSize( 16 ).staticText( "could not be resolved" );
			DPVBox errorBox = styleSheet.withVBoxSpacing( 10.0 ).vbox( new DPElement[] { loc.alignHCentre(), error.alignHCentre() } );
			
			DPVBox pageBox = styleSheet.withVBoxSpacing( 40.0 ).vbox( new DPElement[] { SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ),
					title.padY( 10.0 ).alignHCentre(), errorBox.padY( 10.0 ).alignHCentre() } );

			return pageBox.alignHExpand();
		}
	}
	
	
	
	
	private static DefaultRootPage defaultRootPage = new DefaultRootPage();
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet resolveErrorStyleSheet = styleSheet.withFontSize( 14 ).withForeground( new Color( 0.8f, 0.0f, 0.0f ) );
	private static GSymPerspective rootLocationPerspective = new GSymPerspective( new RootLocationFragmentViewFn() );
	private static GSymPerspective resolveErrorPerspective = new GSymPerspective( new ResolveErrorFragmentViewFn() );
}
