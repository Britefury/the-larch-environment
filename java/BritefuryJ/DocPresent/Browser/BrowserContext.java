//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
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
	
	
	public Page resolveLocationAsPage(Location location, PersistentStateStore persistentState)
	{
		for (LocationResolver resolver: resolvers)
		{
			Page p = resolver.resolveLocationAsPage( location, persistentState );
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
}
