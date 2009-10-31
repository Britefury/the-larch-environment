//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
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
	
	
	private static String COMMAND_BACK = "back";
	private static String COMMAND_FORWARD = "forward";
	

	
	private JToolBar toolbar;

	private JTextField locationField;
	private JPanel locationPanel, panel;

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
		
		area = new DPPresentationArea( null, history.getCurrentContext().getViewTransformation() );
		area.setPageController( pageController );
		
		ActionMap actionMap = area.getPresentationComponent().getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );
		

		toolbar = new JToolBar();
		toolbar.setFloatable( false );
		initialiseToolbar( toolbar );
		
		
		JLabel locationLabel = new JLabel( "Location:" );
		locationLabel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 10 ) );
		locationField = new JTextField( location );
		locationField.setMaximumSize( new Dimension( locationField.getMaximumSize().width, locationField.getMinimumSize().height ) );
		locationField.setBorder( BorderFactory.createLineBorder( Color.black, 1 ) );
		locationField.setDragEnabled( true );
	
		ActionListener locationActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onLocationField( locationField.getText() );
			}
		};
		
		locationField.addActionListener( locationActionListener );
		
		
		locationPanel = new JPanel();
		locationPanel.setLayout( new BoxLayout( locationPanel, BoxLayout.X_AXIS ) );
		locationPanel.add( locationLabel );
		locationPanel.add( locationField );
		locationPanel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 5 ) );

		
		JPanel header = new JPanel( new BorderLayout() );
		header.add( toolbar, BorderLayout.PAGE_START );
		header.add( locationPanel, BorderLayout.PAGE_END );
		
		
		panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		panel.add( header, BorderLayout.PAGE_START );
		panel.add( area.getComponent(), BorderLayout.CENTER );

		
		resolve();
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
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

		area.setViewTransformation( history.getCurrentContext().getViewTransformation() );
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
	
	
	
	private void onLocationField(String location)
	{
		setLocation( location );
	}



	
	private void initialiseToolbar(JToolBar toolbar)
	{
		ActionListener backListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				back();
			}
		};

		ActionListener forwardListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				forward();
			}
		};
		
		
		toolbar.add( makeToolButton( "back arrow.png", COMMAND_BACK, "Go back", "Back", backListener ) );
		toolbar.add( makeToolButton( "forward arrow.png", COMMAND_FORWARD, "Go forward", "Forward", forwardListener ) );
	}
	
	private JButton makeToolButton(String imageFilename, String actionCommand, String tooltipText, String altText, ActionListener listener)
	{
		String imagePath = "icons/" + imageFilename;
		
		JButton button = new JButton();
		button.setActionCommand( actionCommand );
		button.setToolTipText( tooltipText );
		button.addActionListener( listener );
		button.setFocusable( false );
		
		ImageIcon icon = new ImageIcon( imagePath, altText );
		if ( icon.getImageLoadStatus() != MediaTracker.ABORTED  &&  icon.getImageLoadStatus() != MediaTracker.ERRORED )
		{
			button.setIcon( icon );
		}
		else
		{
			button.setText( altText );
			System.err.println( "Could not load image " + imagePath );
		}
		
		return button;
	}
	
	
	
	
	
	
	
	
	private static class DefaultRootPage extends Page
	{
		public String getTitle()
		{
			return "Default";
		}

		
		public DPWidget getContentsElement()
		{
			DPVBox pageBox = new DPVBox( null );
			
			VBoxStyleSheet contentBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 40.0 );
			DPVBox contentBox = new DPVBox( null, contentBoxStyle );
			

			
			StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
			DPStaticText title = new DPStaticText( null, titleStyle, "Default root page" );
			
			StaticTextStyleSheet contentsStyle = new StaticTextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
			DPStaticText contents = new DPStaticText( null, contentsStyle, "Empty document" );

			contentBox.append( title.alignHCentre() );
			contentBox.append( contents.alignHExpand() );

			pageBox.append( SystemRootPage.createLinkHeader( null, SystemRootPage.LINKHEADER_SYSTEMPAGE ) );
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
			DPVBox pageBox = new DPVBox( null, pageBoxStyle );
			

			TextStyleSheet titleStyle = new TextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
			DPText title = new DPText( null, titleStyle, "Could Not Resolve Location" );
			
			VBoxStyleSheet errorBoxStyle = new VBoxStyleSheet( VTypesetting.NONE, 10.0 );
			DPVBox errorBox = new DPVBox( null, errorBoxStyle );
			
			TextStyleSheet locationStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
			TextStyleSheet errorStyle = new TextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );

			DPText loc = new DPText( null, locationStyle, location );
			DPText error = new DPText( null, errorStyle, "could not be resolved" );
			
			errorBox.append( loc.alignHCentre() );
			errorBox.append( error.alignHCentre() );

			pageBox.append( SystemRootPage.createLinkHeader( null, SystemRootPage.LINKHEADER_ROOTPAGE ) );
			pageBox.append( title.padY( 10.0 ).alignHCentre() );
			pageBox.append( errorBox.padY( 10.0 ).alignHCentre() );
			
			return pageBox.alignHExpand();
		}
	}
}
