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

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.PageController;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemLocationResolver;
import BritefuryJ.DocPresent.Browser.SystemPages.SystemRootPage;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
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
			area.setChild( page.getContentsElement().alignHExpand() );		
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
		VBoxStyleSheet pageBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 40.0 );
		DPVBox pageBox = new DPVBox( pageBoxStyle );
		

		TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPText title = new DPText( titleStyle, "Could Not Resolve Location" );
		
		VBoxStyleSheet errorBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
		DPVBox errorBox = new DPVBox( errorBoxStyle );
		
		TextStyleSheet locationStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
		TextStyleSheet errorStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

		DPText loc = new DPText( locationStyle, location );
		DPText error = new DPText( errorStyle, "could not be resolved" );
		
		errorBox.append( loc.alignHCentre() );
		errorBox.append( error.alignHCentre() );

		pageBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_ROOTPAGE ) );
		pageBox.append( title.padY( 10.0 ).alignHCentre() );
		pageBox.append( errorBox.padY( 10.0 ).alignHCentre() );
		
		return pageBox.alignHExpand();
	}
	
	
	private DPWidget createDefaultRootElement()
	{
		DPVBox pageBox = new DPVBox();
		
		VBoxStyleSheet contentBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 40.0 );
		DPVBox contentBox = new DPVBox( contentBoxStyle );
		

		
		StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
		DPStaticText title = new DPStaticText( titleStyle, "Default root page" );
		
		StaticTextStyleSheet contentsStyle = new StaticTextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
		DPStaticText contents = new DPStaticText( contentsStyle, "Empty document" );

		contentBox.append( title.alignHCentre() );
		contentBox.append( contents.alignHExpand() );

		pageBox.append( SystemRootPage.createLinkHeader( SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
		pageBox.append( contentBox.alignHExpand() );

		
		return pageBox.alignHExpand();
	}
	
	
	public void goToLocation(String location)
	{
		history.visit( location );
		listener.onBrowserGoToLocation( this, location );
		resolve();
	}
}
