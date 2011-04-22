//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.DefaultPerspectiveSubject;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.BrowserPage;
import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.PageLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.IncrementalView.BrowserIncrementalView;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresentationLocationResolver;
import BritefuryJ.ObjectPresentation.ObjectPresentationPerspective;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.StyleSheet.StyleSheet;

public class ProjectiveBrowserContext
{
	private static class PageSubject extends Subject
	{
		private class PagePerspective extends AbstractPerspective
		{
			@Override
			protected Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
			{
				return Pres.coerceNonNull( page.getContentsElement() );
			}

			@Override
			public ClipboardHandlerInterface getClipboardHandler()
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
		public AbstractPerspective getPerspective()
		{
			return perspective;
		}

		@Override
		public String getTitle()
		{
			return page.getTitle();
		}
	}
	
	
	
	
	private class ProjectiveBrowserContextLocationResolver implements PageLocationResolver
	{
		public BrowserPage resolveLocationAsPage(Location location, PersistentStateStore persistentState)
		{
			return ProjectiveBrowserContext.this.resolveLocationAsPage( location, persistentState );
		}
	}
	
	
	private ProjectiveBrowserContextLocationResolver pageLocationResolver = new ProjectiveBrowserContextLocationResolver();
	private ObjectPresentationLocationResolver objPresLocationResolver = new ObjectPresentationLocationResolver();
	
	private PyStringMap resolverLocals = new PyStringMap();
	
	
	
	public ProjectiveBrowserContext(boolean bWithSystemPages)
	{
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
	
	
	public DefaultPerspectiveSubject defaultSubject(Object focus)
	{
		return new DefaultPerspectiveSubject( focus );
	}
	
	public DefaultPerspectiveSubject defaultSubject(Object focus, String title)
	{
		return new DefaultPerspectiveSubject( focus, title );
	}
	
	

	public Location getLocationForObject(ObjectPresentationPerspective perspective, Object x)
	{
		String relative = objPresLocationResolver.getRelativeLocationForObject( perspective, x );
		return new Location( "objects" + relative );
	}
	
	public Location getLocationForObject(Object x)
	{
		return getLocationForObject( DefaultPerspective.instance, x );
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
		catch (Throwable e)
		{
			return new DefaultPerspectiveSubject( new ResolveError( location, e ), "Resolve error" );
		}
	}


	public Subject resolveLocationAsSubject(Location location)
	{
		Object result = resolveLocationAsObject( location );
		
		if ( result instanceof Subject )
		{
			return (Subject)result;
		}
		else if ( result instanceof BrowserPage )
		{
			return new PageSubject( (BrowserPage)result );
		}
		else
		{
			return defaultSubject( result );
		}
	}
	
	
	public BrowserPage resolveLocationAsPage(Location location, PersistentStateStore persistentState)
	{
		Subject subject = resolveLocationAsSubject( location );
		BrowserIncrementalView view = new BrowserIncrementalView( subject, this, persistentState );
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
			
			Pres contents = StyleSheet.instance.withAttr( Primitive.fontSize, 16 ).applyTo( new Label( "Empty document" ) ).alignHCentre();
			
			Pres head = new Head( new Pres[] { linkHeader, title } );
			
			return new Page( new Pres[] { head, contents } ).present();
		}
	}
	
	
	
	private static class ResolveError implements Presentable
	{
		private String location;
		private Throwable exception;
		
		public ResolveError(Location location, Throwable exception)
		{
			this.location = location.getLocationString();
			this.exception = exception;
		}
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			StyleSheet titleStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 24 );
			StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 16 );
			
			Pres errorTitle = titleStyle.applyTo( new Label( "Could not resolve" ) );
			Pres loc = contentsStyle.applyTo( new StaticText( location ) );
			Pres exc = Pres.coerceNonNull( exception );
			Pres body = new Body( new Pres[] { errorTitle.alignHCentre(), loc.alignHCentre(), exc.alignHCentre() } );
			
			
			return body;
		}
	}
}
