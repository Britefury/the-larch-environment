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
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;

public class TabbedBrowser implements Browser.BrowserListener
{
	private static String COMMAND_BACK = "back";
	private static String COMMAND_FORWARD = "forward";
	
	private JToolBar toolbar;
	private JTextField locationField;
	private JPanel locationPanel, panel;
	
	private LocationResolver resolver;
	
	private ArrayList<Browser> browsers;
	private Browser currentBrowser;
	private CommandHistoryListener commandHistoryListener;
	
	
	
	public TabbedBrowser(LocationResolver resolver, String location)
	{
		this.resolver = resolver;
		
		Browser browser = createBrowser( location );
		browsers = new ArrayList<Browser>();
		browsers.add( browser );
		setCurrentBrowser( browser );
		

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
		panel.add( currentBrowser.getComponent(), BorderLayout.CENTER );
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
	}
	
	
	public CommandHistoryController getCommandHistoryController()
	{
		return currentBrowser.getCommandHistoryController();
	}
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
	{
		commandHistoryListener = listener;
		currentBrowser.setCommandHistoryListener( listener );
	}
	

	
	public void reset(String location)
	{
		browsers.clear();
		browsers.add( currentBrowser );
		currentBrowser.reset( location );
		currentBrowser.viewportReset();
	}
	
	
	public void createTreeExplorer()
	{
		currentBrowser.createTreeExplorer();
	}

	public void viewportReset()
	{
		currentBrowser.viewportReset();
	}

	public void viewportOneToOne()
	{
		currentBrowser.viewportOneToOne();
	}


	
	private void onLocationField(String location)
	{
		currentBrowser.setLocation( location );
	}



	private void initialiseToolbar(JToolBar toolbar)
	{
		ActionListener backListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onBack();
			}
		};

		ActionListener forwardListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				onForward();
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
	
	
	
	private void setCurrentBrowser(Browser browser)
	{
		if ( browser != currentBrowser )
		{
			if ( currentBrowser != null )
			{
				currentBrowser.setCommandHistoryListener( null );
			}
			
			currentBrowser = browser;
			
			if ( currentBrowser != null  &&  commandHistoryListener != null )
			{
				currentBrowser.setCommandHistoryListener( commandHistoryListener );
			}
	
			if ( commandHistoryListener != null )
			{
				commandHistoryListener.onCommandHistoryChanged( getCommandHistoryController() );
			}
		}
	}
	
	
	
	private void onBack()
	{
		currentBrowser.back();
	}
	
	private void onForward()
	{
		currentBrowser.forward();
	}
	
	
	private Browser createBrowser(String location)
	{
		return new Browser( resolver, location, this );
	}
	
	
	public void onBrowserGoToLocation(Browser browser, String location)
	{
		if ( browser == currentBrowser )
		{
			locationField.setText( location );
		}
	}
}
