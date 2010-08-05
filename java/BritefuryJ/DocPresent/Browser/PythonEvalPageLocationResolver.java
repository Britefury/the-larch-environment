//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Browser;

import org.python.core.Py;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Head;
import BritefuryJ.DocPresent.Combinators.RichText.TitleBar;
import BritefuryJ.DocPresent.PersistentState.PersistentStateStore;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class PythonEvalPageLocationResolver implements PageLocationResolver
{
	private PythonInterpreter interpreter;
	
	
	public PythonEvalPageLocationResolver()
	{
		PyStringMap locals = new PyStringMap();
		locals.__setitem__( "system", Py.java2py( new SystemRootPage() ) );
		
		interpreter = new PythonInterpreter( locals );
	}
	
	
	@Override
	public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
	{
		String locationString = location.getLocationString();
		
		if ( locationString.equals( "" ) )
		{
			return defaultRootPage;
		}
		else
		{
			Page p;
			try
			{
				p = Py.tojava( interpreter.eval( locationString ), Page.class );
			}
			catch (Exception e)
			{
				return new ResolveErrorPage( location, e );
			}
			
			return p;
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
		private Exception exception;
		
		public ResolveErrorPage(Location location, Exception exception)
		{
			this.location = location.getLocationString();
			this.exception = exception;
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
			
			Pres errorTitle = contentsStyle.applyTo( new StaticText( "Could not resolve" ) ).alignHCentre();
			Pres loc = contentsStyle.applyTo( new StaticText( location ) ).alignHCentre();
			Pres excClass = contentsStyle.applyTo( new StaticText( "Caught exception of type '" + exception.getClass().getName() + "'" ) );
			String excLines[] = exception.toString().split( "\n" );
			Pres excLineTexts[] = new Pres[excLines.length];
			for (int i = 0; i < excLines.length; i++)
			{
				excLineTexts[i] = new StaticText( excLines[i] );
			}
			Pres excText = contentsStyle.applyTo( new VBox( excLineTexts ) );
			Pres body = new Body( new Pres[] { errorTitle, loc, excClass, excText } );
			
			
			return new BritefuryJ.DocPresent.Combinators.RichText.Page( new Pres[] { head, body } ).present();
		}
	}


	private static DefaultRootPage defaultRootPage = new DefaultRootPage();
}
