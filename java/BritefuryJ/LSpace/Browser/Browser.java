//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;

import BritefuryJ.ChangeHistory.ChangeHistoryController;
import BritefuryJ.ChangeHistory.ChangeHistoryListener;
import BritefuryJ.Command.AbstractCommandConsole;
import BritefuryJ.Command.CommandBar;
import BritefuryJ.Command.CommandConsoleFactory;
import BritefuryJ.Controls.ScrolledViewport;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.PersistentState.PersistentStateStore;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.StyleSheet.StyleValues;

public class Browser
{
	protected interface BrowserListener
	{
		public void onBrowserChangeTitle(Browser browser, String title);
	}
	
	
	private static final String COMMAND_BACK = "back";
	private static final String COMMAND_FORWARD = "forward";
	private static final String COMMAND_RELOAD = "reload";


	private JTextField locationField;
	private JPanel panel;

	private PresentationComponent presComponent;
	private CommandBar commandBar;
	private ScrolledViewport.ScrolledViewportControl viewport;
	private BrowserHistory history;
	
	private PageLocationResolver resolver;
	private BrowserPage page;
	private BrowserListener listener;
	private ChangeHistoryListener changeHistoryListener;
	
	
	
	
	public Browser(PageLocationResolver resolver, Location location, PageController pageController, CommandConsoleFactory commandConsoleFactory)
	{
		this.resolver = resolver;
		history = new BrowserHistory( location );
		
		viewport = makeViewport( new Blank(), history.getCurrentState().getViewportState() );
		
		presComponent = new PresentationComponent();
		presComponent.setPageController( pageController );
		presComponent.getRootElement().setChild( viewport.getElement() );
		
		ActionMap actionMap = presComponent.getActionMap();
		actionMap.put( TransferHandler.getCutAction().getValue( Action.NAME ), TransferHandler.getCutAction() );
		actionMap.put( TransferHandler.getCopyAction().getValue( Action.NAME ), TransferHandler.getCopyAction() );
		actionMap.put( TransferHandler.getPasteAction().getValue( Action.NAME ), TransferHandler.getPasteAction() );


		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		initialiseToolbar(toolbar);
		
		
		locationField = new JTextField( location.getLocationString() );
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
		toolbar.add(locationField);
		
		
		JPanel header = new JPanel( new BorderLayout() );
		header.add(toolbar, BorderLayout.PAGE_START );
		
		
		panel = new JPanel( new BorderLayout() );
		panel.add( header, BorderLayout.PAGE_START );
		panel.add( presComponent, BorderLayout.CENTER );
		panel.setBackground( Color.WHITE );

		
		
		if ( commandConsoleFactory != null )
		{
			AbstractCommandConsole commandConsole = commandConsoleFactory.createCommandConsole( presComponent );
			commandBar = new CommandBar( presComponent, commandConsole, pageController );
						
			panel.add( commandBar.getComponent(), BorderLayout.PAGE_END );
		}
		
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
	
	public LSElement getRootElement()
	{
		return presComponent.getRootElement();
	}
	
	
	
	public Location getLocation()
	{
		return history.getCurrentState().getLocation();
	}
	
	public void goToLocation(Location location)
	{
		locationField.setText( location.getLocationString() );
		setLocation( location );
	}
	
	
	
	
	public void setListener(BrowserListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public ChangeHistoryController getChangeHistoryController()
	{
		if ( page != null )
		{
			return page.getChangeHistoryController();
		}
		else
		{
			return null;
		}
	}
	
	public void setChangeHistoryListener(ChangeHistoryListener listener)
	{
		changeHistoryListener = listener;
		if ( page != null )
		{
			page.setChangeHistoryListener( listener );
		}
	}
	

	
	public void reset(Location location)
	{
		history.visit( location );
		history.clear();
		locationField.setText( location.getLocationString() );
		viewportReset();
		resolve();
	}
	
	
	public void viewportReset()
	{
		viewport.getViewportElement().resetXform();
	}

	public void viewportOneToOne()
	{
		viewport.getViewportElement().oneToOne();
	}

	
	
	
	
	
	
	protected void back()
	{
		if ( history.canGoBack() )
		{
			onPreHistoryChange();
			history.back();
			Location location = history.getCurrentState().getLocation();
			locationField.setText( location.getLocationString() );
			resolve();
		}
	}
	
	protected void forward()
	{
		if ( history.canGoForward() )
		{
			onPreHistoryChange();
			history.forward();
			Location location = history.getCurrentState().getLocation();
			locationField.setText( location.getLocationString() );
			resolve();
		}
	}
	
	protected void reload()
	{
		resolve();
	}

	
	
	private ScrolledViewport.ScrolledViewportControl makeViewport(Object child, PersistentState state)
	{
		Pres childPres = Pres.coerce( child );
		ScrolledViewport vp = new ScrolledViewport( childPres, 0.0, 0.0, state );
		return (ScrolledViewport.ScrolledViewportControl)vp.createControl( PresentationContext.defaultCtx, StyleValues.instance.alignHExpand().alignVExpand() );
	}
	

	private void resolve()
	{
		// Get the location to resolve
		Location location = history.getCurrentState().getLocation();
		
		PersistentStateStore stateStore = history.getCurrentState().getPagePersistentState();
		BrowserPage p = resolver.resolveLocationAsPage( location, stateStore );

		viewport = makeViewport( p.getContentsPres(), history.getCurrentState().getViewportState() );
		commandBar.setPage( p );
		presComponent.getRootElement().setChild( viewport.getElement() );
		
		// Set the page
		setPage( p );
	}
	
	private void setPage(BrowserPage p)
	{
		if ( p != page )
		{
			if ( page != null )
			{
				page.setChangeHistoryListener( null );
			}
			
			page = p;
			
			if ( page != null  &&  changeHistoryListener != null )
			{
				page.setChangeHistoryListener( changeHistoryListener );
			}
			
			if ( changeHistoryListener != null )
			{
				changeHistoryListener.onChangeHistoryChanged( getChangeHistoryController() );
			}
			
			
			if ( listener != null )
			{
				listener.onBrowserChangeTitle( this, getTitle() );
			}
		}
	}
	
	
	private void setLocation(Location location)
	{
		onPreHistoryChange();
		history.visit( location );
		resolve();
	}
	
	
	private void onLocationField(String location)
	{
		setLocation( new Location( location ) );
	}
	
	
	private void onPreHistoryChange()
	{
		PersistentStateStore store = page.storePersistentState();
		history.getCurrentState().setPagePersistentState( store );
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
		
		ActionListener reloadListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				reload();
			}
		};
		
		
		toolbar.add( makeToolButton( "back arrow.png", COMMAND_BACK, "Go back", "Back", backListener ) );
		toolbar.add( makeToolButton( "forward arrow.png", COMMAND_FORWARD, "Go forward", "Forward", forwardListener ) );
		toolbar.add( makeToolButton( "reload.png", COMMAND_RELOAD, "Reload page", "Reload", reloadListener ) );
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
}
