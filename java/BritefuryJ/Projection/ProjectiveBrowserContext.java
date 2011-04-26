//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.DefaultPerspective.DefaultPerspectiveSubject;
import BritefuryJ.DefaultPerspective.Presentable;
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
				return Pres.coerceNonNull( page.getContentsPres() );
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
			super( null );
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
	
	private HashMap<String, PyObject> resolverLocals = new HashMap<String, PyObject>();
	
	
	
	public ProjectiveBrowserContext(boolean bWithSystemPages)
	{
		if ( bWithSystemPages )
		{
			resolverLocals.put( "system", Py.java2py( new SystemRootPage() ) );
		}
		resolverLocals.put( "objects", Py.java2py( objPresLocationResolver ) );
		
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
		resolverLocals.put( name, Py.java2py( subject ) );
	}
	
	public void registerNamedSubject(String name, PyObject subject)
	{
		if ( name.equals( "system" )  ||  name.equals( "objects" ) )
		{
			throw new RuntimeException( "Cannot register subject under name '" + name + "'" );
		}
		resolverLocals.put( name, subject );
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
	
	
	
	private static final Pattern separator = Pattern.compile( Pattern.quote( "." ) );
	private static final Pattern identifier = Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]++" );
	private static final PyString resolveName = Py.newString( "__resolve__".intern() );

	private Object evaluateLocationString(Location location)
	{
		String names[] = separator.split( location.getLocationString() );
		
		for (String name: names)
		{
			if ( !identifier.matcher( name ).matches() )
			{
				return new DefaultPerspectiveSubject( new LocationSyntaxError( location ), "Invalid location syntax" );
			}
		}
		
		String name = names[0];
		PyObject target = resolverLocals.get( name );
		for (int i = 1; i < names.length; i++)
		{
			if ( target == null )
			{
				return target;
			}
			
			name = names[i];
			
			PyObject resolveChild = null;
			PyObject resolveMethod;
			try
			{
				resolveMethod = __builtin__.getattr( target, resolveName );
			}
			catch (PyException e)
			{
				resolveMethod = null;
			}
			
			if ( resolveMethod != null  &&  resolveMethod.isCallable() )
			{
				resolveChild = resolveMethod.__call__( Py.newString( name ) );
			}
			
			if ( resolveChild == null )
			{
				resolveChild = __builtin__.getattr( target, Py.newString( name ) );
			}
			
			
			target = resolveChild;
		}
		
		
		return Py.tojava( target, Object.class );
	}
	
	public Object resolveLocationAsObject(Location location)
	{
		String locationString = location.getLocationString();
		
		if ( locationString.equals( "" ) )
		{
			location = new Location( "main" );
		}

		try
		{
			return evaluateLocationString( location );
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

		
		public Pres getContentsPres()
		{
			Pres linkHeader = SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE );
			Pres title = new TitleBar( "Default Root Page" );
			
			Pres contents = StyleSheet.instance.withAttr( Primitive.fontSize, 16 ).applyTo( new Label( "Empty document" ) ).alignHCentre();
			
			Pres head = new Head( new Pres[] { linkHeader, title } );
			
			return new Page( new Pres[] { head, contents } );
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
			Pres page = new Page( new Pres[] { body } );
			return page;
		}
	}

	
	private static class LocationSyntaxError implements Presentable
	{
		private String location;
		
		public LocationSyntaxError(Location location)
		{
			this.location = location.getLocationString();
		}
		
		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			StyleSheet titleStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 24 );
			StyleSheet contentsStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 16 );
			StyleSheet validSyntaxStyle = StyleSheet.instance.withAttr( Primitive.fontSize, 14 );
			
			Pres errorTitle = titleStyle.applyTo( new Label( "Invalid location string syntax invalid" ) );
			Pres loc = contentsStyle.applyTo( new StaticText( location ) );
			Pres validSyntax = validSyntaxStyle.applyTo( new Label( "Valid syntax: dot separated identifiers" ) );
			Pres body = new Body( new Pres[] { errorTitle.alignHCentre(), loc.alignHCentre(), validSyntax.alignHCentre() } );
			Pres page = new Page( new Pres[] { body } );
			return page;
		}
	}
}
