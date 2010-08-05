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

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.BrowserContext;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.Location.TokenIterator;
import BritefuryJ.DocPresent.Browser.LocationResolver;
import BritefuryJ.DocPresent.Browser.Page;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Head;
import BritefuryJ.DocPresent.Combinators.RichText.TitleBar;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GSymGenericObjectPresenterRegistry;
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
		public Pres createViewFragment(Object x, GSymFragmentView ctx, SimpleAttributeTable state)
		{
			return rootLocationStyle.applyTo( new StaticText( "<<Root location>>" ) );
		}
	}
	
	private static class ResolveErrorFragmentViewFn implements GSymViewFragmentFunction
	{
		public Pres createViewFragment(Object x, GSymFragmentView ctx, SimpleAttributeTable state)
		{
			Location location = (Location)x;
			return resolveErrorStyleSheet.applyTo( new StaticText( "<<Could not resolve " + location.getLocationString() + ">>" ) );
		}
	}
	
	private static class SystemPageFragmentViewFn implements GSymViewFragmentFunction
	{
		public Pres createViewFragment(Object x, GSymFragmentView ctx, SimpleAttributeTable state)
		{
			Page p = (Page)x;
			return Pres.coerce( p.getContentsElement() );
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
			return perspective.resolveRelativeLocation( new GSymSubject( null, perspective, "", SimpleAttributeTable.instance, null ), location.iterator() );
		}
	}
	
	
	
	private class GSymBrowserContextLocationResolver implements LocationResolver
	{
		public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
		{
			return GSymBrowserContext.this.resolveLocationAsPage( location, persistentState );
		}
	}
	
	
	private GSymBrowserContextLocationResolver pageLocationResolver = new GSymBrowserContextLocationResolver();
	private ObjectPresentationLocationResolver objPresLocationResolver = new ObjectPresentationLocationResolver();
	private GSymGenericPerspective genericPerspective;
	private List<GSymLocationResolver> resolvers = new ArrayList<GSymLocationResolver>();
	
	
	
	public GSymBrowserContext(GSymGenericObjectPresenterRegistry genericPresenterRegistry, boolean bWithSystemPages)
	{
		super();
		genericPerspective = new GSymGenericPerspective( objPresLocationResolver, genericPresenterRegistry );
		if ( bWithSystemPages )
		{
			addResolvers( Arrays.asList( new GSymLocationResolver[] { new SystemPageLocationResolver( SystemLocationResolver.getSystemResolver() ) } ) );
		}
		addResolvers( Arrays.asList( new GSymLocationResolver[] { objPresLocationResolver } ) );
	}
	
	public GSymBrowserContext(GSymGenericObjectPresenterRegistry genericPresenterRegistry, boolean bWithSystemPages, List<GSymLocationResolver> resolvers)
	{
		this( genericPresenterRegistry, bWithSystemPages );
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
			return new GSymSubject( null, rootLocationPerspective, "Root page", SimpleAttributeTable.instance, null );
		}
		else
		{
			return new GSymSubject( location, resolveErrorPerspective, "Resolve error", SimpleAttributeTable.instance, null );
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
			Pres linkHeader = SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE );
			Pres title = new TitleBar( "Default Root Page" );
			
			Pres contents = StyleSheet.instance.withAttr( Primitive.fontSize, 16 ).applyTo( new StaticText( "Empty document" ) ).alignHCentre();
			
			Pres head = new Head( new Pres[] { linkHeader, title } );
			
			return new BritefuryJ.DocPresent.Combinators.RichText.Page( new Pres[] { head, contents } ).present();
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
			StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 16 );
			Pres linkHeader = SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE );
			Pres title = new TitleBar( "Could Not Resolve Location" );
			Pres head = new Head( new Pres[] { linkHeader, title } );
			
			Pres loc = contentsStyle.applyTo( new StaticText( location ) ).alignHCentre();
			Pres error = contentsStyle.applyTo( new StaticText( "could not be resolved" ) ).alignHCentre();
			Pres body = new Body( new Pres[] { loc, error } );
			
			
			return new BritefuryJ.DocPresent.Combinators.RichText.Page( new Pres[] { head, body } ).present();
		}
	}
	
	
	
	
	private static DefaultRootPage defaultRootPage = new DefaultRootPage();
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static final StyleSheet rootLocationStyle = StyleSheet.instance; 
	private static final StyleSheet resolveErrorStyleSheet = styleSheet.withAttr( Primitive.fontSize, 14 ).withAttr( Primitive.foreground, new Color( 0.8f, 0.0f, 0.0f ) );
	private static GSymPerspective rootLocationPerspective = new GSymPerspective( new RootLocationFragmentViewFn() );
	private static GSymPerspective resolveErrorPerspective = new GSymPerspective( new ResolveErrorFragmentViewFn() );
}
