//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonUI;

import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.Command.CommandConsoleFactory;
import BritefuryJ.IncrementalView.FragmentInspector;
import BritefuryJ.LSpace.PageController;
import BritefuryJ.Projection.Subject;

public class TabbedBrowser implements Browser.BrowserListener, ChangeListener, PageController
{
	private static final int MAX_TITLE_LENGTH = 16;
	
	
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
	
	
	
	
	public static interface TabbedBrowserListener
	{
		public void createNewBrowserWindow(Subject subject);
		public void onTabbledBrowserChangePage(TabbedBrowser browser);
	}
	
	
	
	
	
	
	
	
	private JTabbedPane tabs;
	
	private Subject rootSubject;
	
	private FragmentInspector inspector;
	
	private TabbedBrowserListener listener;
	
	private ArrayList<Browser> browsers;
	private Browser currentBrowser;
	
	private CommandConsoleFactory commandConsoleFactory;
	
	
	
	public TabbedBrowser(Subject rootSubject, Subject subject, FragmentInspector inspector, TabbedBrowserListener listener, CommandConsoleFactory commandConsoleFactory)
	{
		this.rootSubject = rootSubject;
		this.inspector = inspector;
		this.listener = listener;
		this.commandConsoleFactory = commandConsoleFactory;
		
		browsers = new ArrayList<Browser>();

		tabs = new JTabbedPane();
		tabs.addChangeListener( this );
		
		Browser browser = addNewBrowser( subject );
		setCurrentBrowser( browser );
	}
	
	
	
	public JComponent getComponent()
	{
		return tabs;
	}
	
	
	public Browser getCurrentBrowser()
	{
		return currentBrowser;
	}

	public List<Browser> getBrowsers() {
		ArrayList<Browser> b = new ArrayList<Browser>();
		b.addAll(browsers);
		return b;
	}
	
	
	public ChangeHistory getChangeHistory()
	{
		return currentBrowser.getChangeHistory();
	}
	

	
	public void reset(Subject subject)
	{
		browsers.clear();
		browsers.add( currentBrowser );
		currentBrowser.reset( subject );
		currentBrowser.viewportReset();
	}
	
	
	public void viewportReset()
	{
		currentBrowser.viewportReset();
	}

	public void viewportOneToOne()
	{
		currentBrowser.viewportOneToOne();
	}


	
	private void setCurrentBrowser(Browser browser)
	{
		if ( browser != currentBrowser )
		{
			currentBrowser = browser;
			
			if ( listener != null )
			{
				listener.onTabbledBrowserChangePage( this );
			}
		}
	}
	
	
	
	private Browser createBrowser(Subject subject)
	{
		Browser browser = new Browser( rootSubject, subject, inspector, this, commandConsoleFactory );
		browser.setListener( this );
		return browser;
	}
	
	
	public void onBrowserChangePage(Browser browser, Subject subject, String title)
	{
		int index = browsers.indexOf( browser );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find browser" );
		}
		
		TabComponent tab = (TabComponent)tabs.getTabComponentAt( index );
		tab.setTitle( title );
		
		tabs.setToolTipTextAt( index, title );
		
		if ( listener != null )
		{
			listener.onTabbledBrowserChangePage( this );
		}
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



	public Subject getCurrentBrowserSubject()
	{
		return currentBrowser.getSubject();
	}
	
	@Override
	public void openSubject(Subject subject, OpenOperation op)
	{
		if ( op == PageController.OpenOperation.OPEN_IN_CURRENT_TAB )
		{
			openSubjectInCurrentTab( subject );
		}
		else if ( op == PageController.OpenOperation.OPEN_IN_NEW_TAB )
		{
			openSubjectInNewTab( subject );
		}
		else if ( op == PageController.OpenOperation.OPEN_IN_NEW_WINDOW )
		{
			openSubjectInNewWindow( subject );
		}
	}
	
	public void openSubjectInCurrentTab(Subject subject)
	{
		currentBrowser.goToSubject( subject );
	}
	
	public void openSubjectInNewTab(Subject subject)
	{
		addNewBrowser( subject );
	}
	
	public void openSubjectInNewWindow(Subject subject)
	{
		if ( listener != null )
		{
			listener.createNewBrowserWindow( subject );
		}
		else
		{
			openSubjectInNewTab( subject );
		}
	}


	public void closeTab(Browser browser) {
		int index = browsers.indexOf(browser);
		if (index != -1) {
			tabs.remove(index);
			browsers.remove(index);
		}
	}
	
	
	
	private Browser addNewBrowser(Subject subject)
	{
		int index = browsers.size();

		Browser browser = createBrowser( subject );
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
