//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;

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
		
		ActionMap actionMap = area.getPresentationComponent().getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );
		
		
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
	
	
	
	public void reset(String location)
	{
		history.visit( location );
		history.clear();
		viewportReset();
		resolve();
	}
	
	
	public void createTreeExplorer()
	{
		area.createTreeExplorer();
	}
	
	public void viewportReset()
	{
		area.reset();
	}

	public void viewportOneToOne()
	{
		area.oneToOne();
	}

	
	
	
	
	
	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			history.back();
			listener.onBrowserGoToLocation( this, history.getCurrentContext().getLocation() );
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			history.forward();
			listener.onBrowserGoToLocation( this, history.getCurrentContext().getLocation() );
			resolve();
		}
	}

	
	
	
	private void resolve()
	{
		if ( page != null )
		{
			page.removeBrowser( this );
			page = null;
		}
		
		String location = history.getCurrentContext().getLocation();
		
		page = SystemLocationResolver.getSystemResolver().resolveLocation( location );
		
		if ( page == null  &&  resolver != null )
		{
			page = resolver.resolveLocation( location );
		}

		if ( page == null )
		{
			if ( location.equals( "" ) )
			{
				area.setChild( createDefaultRootElement() );
			}
			else
			{
				area.setChild( createResolveErrorElement( location ) );
			}
		}
		else
		{
			page.addBrowser( this );
			area.setChild( page.getContentsElement() );		
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
		ElementStyleSheet pageBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 10.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		

		ElementStyleSheet linkBoxStyle = DPHBox.styleSheet( VAlignment.BASELINES, 0.0, false, 10.0 );
		DPHBox linkBox = new DPHBox( linkBoxStyle );
		
		linkBox.append( new DPLink( "WELCOME PAGE", "" ) );
		
		
		ElementStyleSheet titleStyle = DPText.styleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Could Not Resolve Location" );
		
		ElementStyleSheet errorBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.CENTRE, 10.0, false, 0.0 );
		DPVBox errorBox = new DPVBox( errorBoxStyle );
		
		ElementStyleSheet locationStyle = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.BLACK );
		ElementStyleSheet errorStyle = DPText.styleSheet( new Font( "Sans serif", Font.PLAIN, 16 ), Color.BLACK );

		DPText loc = new DPText( locationStyle, location );
		DPText error = new DPText( errorStyle, "could not be resolved" );
		
		errorBox.append( loc );
		errorBox.append( error );

		pageBox.append( linkBox );
		pageBox.append( title );
		pageBox.append( errorBox );
		
		return pageBox;
	}
	
	
	private DPWidget createDefaultRootElement()
	{
		ElementStyleSheet pageBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 0.0, false, 0.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		
		ElementStyleSheet contentBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.EXPAND, 40.0, false, 0.0 );
		DPVBox contentBox = new DPVBox( contentBoxStyle );
		
		ElementStyleSheet titleBoxStyle = DPVBox.styleSheet( VTypesetting.NONE, HAlignment.CENTRE, 40.0, false, 0.0 );
		DPVBox titleBox = new DPVBox( titleBoxStyle );
		

		pageBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		
		ElementStyleSheet titleStyle = DPStaticText.styleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "Default root page" );
		
		ElementStyleSheet contentsStyle = DPStaticText.styleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
		DPStaticText contents = new DPStaticText( contentsStyle, "Empty document" );

		titleBox.append( title );
		
		contentBox.append( titleBox );
		contentBox.append( contents );

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
