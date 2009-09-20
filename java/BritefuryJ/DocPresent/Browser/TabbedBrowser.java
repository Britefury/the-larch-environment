//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

import BritefuryJ.CommandHistory.CommandHistoryController;
import BritefuryJ.CommandHistory.CommandHistoryListener;
import BritefuryJ.DocPresent.PageController;

public class TabbedBrowser implements Browser.BrowserListener, ChangeListener, PageController
{
	private static int MAX_TITLE_LENGTH = 16;
	
	private static class TabComponent extends JPanel
	{
		private static final long serialVersionUID = 1L;


		private static class CloseButton extends JButton implements ActionListener
		{
			private static final long serialVersionUID = 1L;

			
			private TabComponent tabComponent;
			
			
			// Mouse listener that provides roll-over effect
			private final static MouseListener mouseListener = new MouseAdapter()
			{
				public void mouseEntered(MouseEvent e)
				{
					Component component = e.getComponent();
					if ( component instanceof AbstractButton )
					{
						((AbstractButton)component).setBorderPainted( true );
					}
				}

				public void mouseExited(MouseEvent e)
				{
					Component component = e.getComponent();
					if ( component instanceof AbstractButton )
					{
						((AbstractButton)component).setBorderPainted( false );
					}
				}
			};
			
			
			public CloseButton(TabComponent tabComponent)
			{
				this.tabComponent = tabComponent;
				
				// Size
				int size = 17;
				setPreferredSize( new Dimension( size, size ) );
				// Tool-tip
				setToolTipText( "Close this tab" );
				// Ensure consistent appearance despite theme
				setUI( new BasicButtonUI() );
				// Transparent background
				setContentAreaFilled( false );
				// Not focusable
				setFocusable( false );
				setBorder( BorderFactory.createEtchedBorder() );
				setBorderPainted( false );
				// Mouse listener provides roll-over effect
				addMouseListener( mouseListener );
				setRolloverEnabled( true );
				// Listen for click
				addActionListener( this );
			}
			
			
			public void actionPerformed(ActionEvent e)
			{
				tabComponent.sendCloseEvent();
			}
			
			
			public void updateUI()
			{
			}
			
			
			public void paint(Graphics g)
			{
				super.paintComponent( g );
				Graphics2D g2 = (Graphics2D)g.create();
				// Shift by 1,1 if the button is currently being pressed
				if ( getModel().isPressed() )
				{
					g2.translate( 1, 1 );
				}
				
				g2.setStroke( new BasicStroke( 1 ) );
				// Choose colour depending on roll-over state
				g2.setColor( new Color( 128, 0, 0 ) );
				if ( getModel().isRollover() )
				{
					g2.setColor( Color.red );
				}
				
				// Draw a cross
				int delta = 5;
				g2.drawLine( delta, delta, getWidth() - delta - 1, getHeight() - delta - 1 );
				g2.drawLine( getWidth() - delta - 1, delta, delta, getHeight() - delta - 1 );
				g2.dispose();
			}
		}
		
		
		private TabbedBrowser tabbedBrowser;
		private JLabel titleLabel;

		
		public TabComponent(TabbedBrowser tabbedBrowser, String title)
		{
			// No gaps in flow layout
			super( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
			
			this.tabbedBrowser = tabbedBrowser;
			
			setOpaque( false );
			setFocusable( false );
			
			
			// Make the title label
			titleLabel = new JLabel( "test" );
			setTitle( title );
			
			// Add the label, with a 5-pixel border to the right
			add( titleLabel );
			titleLabel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 5 ) );
			
			// Add the close button
			add( new CloseButton( this ) );
			
			// 2-pixel border to the top
			setBorder( BorderFactory.createEmptyBorder( 2, 0, 0, 0 ) );
		}
		
		
		public void setTitle(String title)
		{
			if ( title.length() > MAX_TITLE_LENGTH )
			{
				title = title.substring( 0, MAX_TITLE_LENGTH ) + "...";
			}
			titleLabel.setText( title );
		}
		
		
		private void sendCloseEvent()
		{
			tabbedBrowser.closeTab( this );
		}
	}
	
	
	
	
	
	
	
	
	private static String COMMAND_BACK = "back";
	private static String COMMAND_FORWARD = "forward";
	
	private JToolBar toolbar;
	private JTextField locationField;
	private JPanel locationPanel, panel;
	private JTabbedPane tabs;
	
	private LocationResolver resolver;
	
	private ArrayList<Browser> browsers;
	private Browser currentBrowser;
	private CommandHistoryListener commandHistoryListener;
	
	
	
	public TabbedBrowser(LocationResolver resolver, String location)
	{
		this.resolver = resolver;
		
		browsers = new ArrayList<Browser>();

		tabs = new JTabbedPane();
		tabs.addChangeListener( this );
		
		Browser browser = addNewBrowser( location );
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
		panel.add( tabs, BorderLayout.CENTER );
	}
	
	
	
	public JComponent getComponent()
	{
		return panel;
	}
	
	
	public Browser getCurrentBrowser()
	{
		return currentBrowser;
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
		Browser browser = new Browser( resolver, location, this );
		browser.setListener( this );
		return browser;
	}
	
	
	public void onBrowserGoToLocation(Browser browser, String location)
	{
		if ( browser == currentBrowser )
		{
			locationField.setText( location );
		}
	}
	
	public void onBrowserChangeTitle(Browser browser, String title)
	{
		int index = browsers.indexOf( browser );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find browser" );
		}
		
		TabComponent tab = (TabComponent)tabs.getTabComponentAt( index );
		tab.setTitle( title );
		
		tabs.setToolTipTextAt( index, title );
	}



	public void stateChanged(ChangeEvent e)
	{
		if ( e.getSource() != tabs )
		{
			throw new RuntimeException( "ChangeEvent came from unknown source " + e.getSource() );
		}
		
		int selectedIndex = tabs.getSelectedIndex();
		setCurrentBrowser( browsers.get( selectedIndex ) );
	}



	public String getCurrentBrowserLocation()
	{
		return currentBrowser.getLocation();
	}
	
	public void openLocation(String location, OpenOperation op)
	{
		if ( op == PageController.OpenOperation.OPEN_IN_CURRENT_TAB )
		{
			openLocationInCurrentTab( location );
		}
		else if ( op == PageController.OpenOperation.OPEN_IN_NEW_TAB )
		{
			openLocationInNewTab( location );
		}
	}
	
	public void openLocationInCurrentTab(String location)
	{
		currentBrowser.goToLocation( location );
	}
	
	public void openLocationInNewTab(String location)
	{
		addNewBrowser( location );
	}
	
	
	
	private Browser addNewBrowser(String location)
	{
		int index = browsers.size();

		Browser browser = createBrowser( location );
		browsers.add( browser );
		
		String title = browser.getTitle();

		tabs.addTab( title, browser.getComponent() );
		tabs.setTabComponentAt( index, new TabComponent( this, title ) );
		
		return browser;
	}
	
	private void closeTab(TabComponent tab)
	{
		if ( browsers.size() > 1 )
		{
			int index = tabs.indexOfTabComponent( tab );
			
			if ( index == -1 )
			{
				throw new RuntimeException( "Could not find tab" );
			}
			
			tabs.remove( index );
			browsers.remove( index );
		}
	}
}
