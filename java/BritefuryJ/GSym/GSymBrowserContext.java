//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.DocPresent.Browser.PageLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Head;
import BritefuryJ.DocPresent.Combinators.RichText.Page;
import BritefuryJ.DocPresent.Combinators.RichText.TitleBar;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.GSymGenericObjectPresenterRegistry;
import BritefuryJ.GSym.GenericPerspective.GSymGenericPerspective;
import BritefuryJ.GSym.GenericPerspective.GenericSubject;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.ObjectPresentation.GSymObjectPresentationPerspective;
import BritefuryJ.GSym.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.GSym.View.GSymView;

public class GSymBrowserContext
{
	private static class PageSubject extends GSymSubject
	{
		private class PagePerspective extends GSymAbstractPerspective
		{
			@Override
			public Pres present(Object x, GSymFragmentView fragment, SimpleAttributeTable inheritedState)
			{
				return Pres.coerce( page.getContentsElement() );
			}

			@Override
			public SimpleAttributeTable getInitialInheritedState()
			{
				return SimpleAttributeTable.instance;
			}

			@Override
			public EditHandler getEditHandler()
			{
				return null;
			}
		}
		
		
		private BrowserPage page;
		private PagePerspective perspective = new PagePerspective();
		
		
		private PageSubject(BrowserPage page)
		{
			this.page = page;
		}
		

		@Override
		public Object getFocus()
		{
			return page;
		}

		@Override
		public GSymAbstractPerspective getPerspective()
		{
			return perspective;
		}

		@Override
		public String getTitle()
		{
			return page.getTitle();
		}
	}
	
	
	
	
	private class GSymBrowserContextLocationResolver implements PageLocationResolver
	{
		public BrowserPage resolveLocationAsPage(Location location, PersistentStateStore persistentState)
		{
			return GSymBrowserContext.this.resolveLocationAsPage( location, persistentState );
		}
	}
	
	
	private GSymBrowserContextLocationResolver pageLocationResolver = new GSymBrowserContextLocationResolver();
	private ObjectPresentationLocationResolver objPresLocationResolver = new ObjectPresentationLocationResolver();
	private GSymGenericPerspective genericPerspective;
	
	private PyStringMap resolverLocals = new PyStringMap();
	
	
	
	public GSymBrowserContext(GSymGenericObjectPresenterRegistry genericPresenterRegistry, boolean bWithSystemPages)
	{
		super();
		genericPerspective = new GSymGenericPerspective( objPresLocationResolver, genericPresenterRegistry );
		if ( bWithSystemPages )
		{
			resolverLocals.__setitem__( "system", Py.java2py( new SystemRootPage() ) );
		}
		resolverLocals.__setitem__( "objects", Py.java2py( objPresLocationResolver ) );
		
		registerMainSubject( new DefaultRootPage() );
	}
	
	
	
	public void registerMainSubject(Object subject)
	{
		registerNamedSubject( "main", subject );
	}
	
	
	public void registerNamedSubject(String name, Object subject)
	{
		if ( name.equals( "system" )  ||  name.equals( "objects" ) )
		{
			throw new RuntimeException( "Cannot register subject under name '" + name + "'" );
		}
		resolverLocals.__setitem__( name, Py.java2py( subject ) );
	}
	
	public void registerNamedSubject(String name, PyObject subject)
	{
		if ( name.equals( "system" )  ||  name.equals( "objects" ) )
		{
			throw new RuntimeException( "Cannot register subject under name '" + name + "'" );
		}
		resolverLocals.__setitem__( name, subject );
	}
	
	
	public PageLocationResolver getPageLocationResolver()
	{
		return pageLocationResolver;
	}
	
	public GSymGenericPerspective getGenericPerspective()
	{
		return genericPerspective;
	}
	
	
	public GenericSubject genericSubject(Object focus)
	{
		return new GenericSubject( focus );
	}
	
	public GenericSubject genericSubject(Object focus, String title)
	{
		return new GenericSubject( focus, title );
	}
	
	

	public Location getLocationForObject(GSymObjectPresentationPerspective perspective, Object x)
	{
		String relative = objPresLocationResolver.getRelativeLocationForObject( perspective, x );
		return new Location( "objects" + relative );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getLocationForObject( genericPerspective, x );
	}
	
	
	
	
	public Object resolveLocationAsObject(Location location)
	{
		String locationString = location.getLocationString();
		
		if ( locationString.equals( "" ) )
		{
			locationString = "main";
		}

		try
		{
			return Py.tojava( __builtin__.eval( Py.newString( locationString ), resolverLocals ), Object.class );
		}
		catch (Exception e)
		{
			return new GenericSubject( new ResolveError( location, e ), "Resolve error" );
		}
	}


	public GSymSubject resolveLocationAsSubject(Location location)
	{
		Object result = resolveLocationAsObject( location );
		
		if ( result instanceof GSymSubject )
		{
			return (GSymSubject)result;
		}
		else if ( result instanceof BrowserPage )
		{
			return new PageSubject( (BrowserPage)result );
		}
		else
		{
			return genericSubject( result );
		}
	}
	
	
	public BrowserPage resolveLocationAsPage(Location location, PersistentStateStore persistentState)
	{
		GSymSubject subject = resolveLocationAsSubject( location );
		GSymView view = new GSymView( subject, this, persistentState );
		return view.getPage();
	}
	
	
	
	
	
	

	private static class DefaultRootPage extends BrowserPage
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
			
			return new Page( new Pres[] { head, contents } ).present();
		}
	}
	
	
	
	private static class ResolveError implements Presentable
	{
		private String location;
		private Exception exception;
		
		public ResolveError(Location location, Exception exception)
		{
			this.location = location.getLocationString();
			this.exception = exception;
		}
		
		
		@Override
		public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
		{
			StyleSheet titleStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 24 );
			StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 16 );
			
			Pres errorTitle = titleStyle.applyTo( new StaticText( "Could not resolve" ) );
			Pres loc = contentsStyle.applyTo( new StaticText( location ) );
			Pres exc = Pres.coerce( exception );
			Pres body = new Body( new Pres[] { errorTitle.alignHCentre(), loc.alignHCentre(), exc.alignHCentre() } );
			
			
			return body;
		}
	}
}
