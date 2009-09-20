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

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
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

public class Browser
{
	protected interface BrowserListener
	{
		public void onBrowserGoToLocation(Browser browser, String location);
		public void onBrowserChangeTitle(Browser browser, String title);
	}
	
	
	private DPPresentationArea area;
	private BrowserHistory history;
	
	private LocationResolver resolver;
	private Page page;
	private BrowserListener listener;
	private CommandHistoryListener commandHistoryListener;
	
	private static DefaultRootPage defaultRootPage = new DefaultRootPage();
	
	
	
	
	public Browser(LocationResolver resolver, String location, PageController pageController)
	{
		this.resolver = resolver;
		history = new BrowserHistory( location );
		
		area = new DPPresentationArea();
		area.setPageController( pageController );
		
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
	
	public String getTitle()
	{
		return page.getTitle();
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
	
	
	
	public void setListener(BrowserListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public CommandHistoryController getCommandHistoryController()
	{
		if ( page != null )
		{
			return page.getCommandHistoryController();
		}
		else
		{
			return null;
		}
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
		commandHistoryListener = listener;
		if ( page != null )
		{
			page.setCommandHistoryListener( listener );
		}
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
			if ( listener != null )
			{
				listener.onBrowserGoToLocation( this, history.getCurrentContext().getLocation() );
			}
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			history.forward();
			if ( listener != null )
			{
				listener.onBrowserGoToLocation( this, history.getCurrentContext().getLocation() );
			}
			resolve();
		}
	}

	
	
	
	private void resolve()
	{
		Page p = page;
		
		// Remove this browser from existing page
		if ( p != null )
		{
			p.removeBrowser( this );
			p = null;
		}
		
		// Get the location to resolve
		String location = history.getCurrentContext().getLocation();
		
		// Look in the system pages first
		p = SystemLocationResolver.getSystemResolver().resolveLocation( location );
		
		if ( p == null  &&  resolver != null )
		{
			// Could not find in system pages; try client supplied resolver
			p = resolver.resolveLocation( location );
		}

		// Resolve error:
		if ( p == null )
		{
			if ( location.equals( "" ) )
			{
				// Empty location - use default root page
				p = defaultRootPage;
			}
			else
			{
				// Resolve error
				p = new ResolveErrorPage( location );
			}
		}

		// Add browser, and add component
		p.addBrowser( this );
		area.setChild( p.getContentsElement().alignHExpand() );		
		
		// Set the page
		setPage( p );
	}
	
	private void setPage(Page p)
	{
		if ( p != page )
		{
			if ( page != null )
			{
				page.setCommandHistoryListener( null );
			}
			
			page = p;
			
			if ( page != null  &&  commandHistoryListener != null )
			{
				page.setCommandHistoryListener( commandHistoryListener );
			}
			
			if ( commandHistoryListener != null )
			{
				commandHistoryListener.onCommandHistoryChanged( getCommandHistoryController() );
			}
			
			
			if ( listener != null )
			{
				listener.onBrowserChangeTitle( this, getTitle() );
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
	
	
	
	public void goToLocation(String location)
	{
		history.visit( location );
		listener.onBrowserGoToLocation( this, location );
		resolve();
	}
	
	
	
	
	
	
	
	private static class DefaultRootPage extends Page
	{
		public String getTitle()
		{
			return "Default";
		}

		
		public DPWidget getContentsElement()
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
	};
	
	
	
	private static class ResolveErrorPage extends Page
	{
		private String location;
		
		public ResolveErrorPage(String location)
		{
			this.location = location;
		}
		
		
		public String getTitle()
		{
			return "Error";
		}

		public DPWidget getContentsElement()
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
	}
}
