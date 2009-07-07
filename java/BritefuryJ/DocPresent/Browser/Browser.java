//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class Browser implements PageController
{
	protected interface BrowserListener
	{
		public void onBrowserGoToLocation(Browser browser, String location);
	}
	
	
	private DPPresentationArea area;
	private BrowserHistory history;
	
	private LocationResolver resolver;
	private Page page;
	private BrowserListener listener;
	
	
	
	public Browser(LocationResolver resolver, String location, BrowserListener listener)
	{
		this.resolver = resolver;
		history = new BrowserHistory( location );
		this.listener = listener;
		
		area = new DPPresentationArea();
		area.setPageController( this );
		
		
		resolve();
	}
	
	
	
	public JComponent getComponent()
	{
		return area.getComponent();
	}
	
	
	
	public String getLocation()
	{
		return history.getCurrentContext().getLocation();
	}
	
	public void setLocation(String location)
	{
		history.visit( location );
		resolve();
	}
	
	
	
	
	
	
	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			history.back();
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			history.forward();
			resolve();
		}
	}

	
	
	
	private void resolve()
	{
		if ( page != null )
		{
			page.setBrowser( null );
			page = null;
		}
		
		String location = history.getCurrentContext().getLocation();
		
		if ( location.equals( "" ) )
		{
			area.setChild( createWelcomeElement() );
		}
		else
		{
			page = SystemLocationResolver.getSystemResolver().resolveLocation( location );
			
			if ( page == null  &&  resolver != null )
			{
				page = resolver.resolveLocation( location );
			}
			
			if ( page == null )
			{
				area.setChild( createResolveErrorElement( location ) );
			}
			else
			{
				page.setBrowser( this );
				area.setChild( page.getContentsElement() );		
			}
		}
	}
	
	
	protected void onPageContentsModified(Page page)
	{
		if ( page != this.page )
		{
			throw new RuntimeException( "Received page contents modified notification from invalid page" );
		}
		
		area.setChild( page.getContentsElement() );		
	}
	
	
	
	private DPWidget createResolveErrorElement(String location)
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		

		HBoxStyleSheet linkBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPHBox linkBox = new DPHBox( linkBoxStyle );
		
		linkBox.append( new DPLink( "WELCOME PAGE", "" ) );
		
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Could Not Resolve Location" );
		
		VBoxStyleSheet errorBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 10.0, false, 0.0 );
		DPVBox errorBox = new DPVBox( errorBoxStyle );
		
		TextStyleSheet locationStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
		TextStyleSheet errorStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

		DPText loc = new DPText( locationStyle, location );
		DPText error = new DPText( errorStyle, "could not be resolved" );
		
		errorBox.append( loc );
		errorBox.append( error );

		pageBox.append( linkBox );
		pageBox.append( title );
		pageBox.append( errorBox );
		
		return pageBox;
	}
	
	
	private DPWidget createWelcomeElement()
	{
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		HBoxStyleSheet linkBoxStyle = new HBoxStyleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPHBox linkBox = new DPHBox( linkBoxStyle );
		
		linkBox.append( new DPLink( "SYSTEM PAGE", "system" ) );
		
		
		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Welcome to gSym" );
		
		VBoxStyleSheet contentBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.CENTRE, 10.0, false, 0.0 );
		DPVBox contentBox = new DPVBox( contentBoxStyle );
		
		TextStyleSheet instructionsStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

		DPText ins = new DPText( instructionsStyle, "Please enter a location in the location box above." );
		
		contentBox.append( ins );

		pageBox.append( linkBox );
		pageBox.append( title );
		pageBox.append( contentBox );
		
		return pageBox;
	}
	
	
	public void goToLocation(String location)
	{
		history.visit( location );
		listener.onBrowserGoToLocation( this, location );
		resolve();
	}
}
