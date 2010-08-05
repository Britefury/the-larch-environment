//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent.Browser;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.Browser.Location;
import BritefuryJ.DocPresent.Browser.PythonEvalPageLocationResolver;
import BritefuryJ.DocPresent.Browser.TabbedBrowser;

public class BrowserTest implements TabbedBrowser.TabbedBrowserListener
{
	private static class TransferActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			JComponent focusOwner = (JComponent)manager.getPermanentFocusOwner();
			if ( focusOwner != null )
			{
				String action = e.getActionCommand();
				Action a = focusOwner.getActionMap().get( action );
				if ( a != null )
				{
					a.actionPerformed( new ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, null ) );
				}
			}
		}
	}
	
	
	public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		
		BrowserTest test = new BrowserTest();
		test.createNewBrowserWindow( new Location( "" ) );
	}

	
	public void createNewBrowserWindow(Location location)
	{
		// EDIT MENU
		
		TransferActionListener transferActionListener = new TransferActionListener();
		
		JMenu editMenu = new JMenu( "Edit" );
		
		JMenuItem editCutItem = new JMenuItem( "Cut" );
		editCutItem.setActionCommand( (String)TransferHandler.getCutAction().getValue( Action.NAME ) );
		editCutItem.addActionListener( transferActionListener );
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) );
		editCutItem.setMnemonic( KeyEvent.VK_T );
		editMenu.add( editCutItem );
		
		JMenuItem editCopyItem = new JMenuItem( "Copy" );
		editCopyItem.setActionCommand( (String)TransferHandler.getCopyAction().getValue( Action.NAME ) );
		editCopyItem.addActionListener( transferActionListener );
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) );
		editCopyItem.setMnemonic( KeyEvent.VK_C );
		editMenu.add( editCopyItem );
		
		JMenuItem editPasteItem = new JMenuItem( "Paste" );
		editPasteItem.setActionCommand( (String)TransferHandler.getPasteAction().getValue( Action.NAME ) );
		editPasteItem.addActionListener( transferActionListener );
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) );
		editPasteItem.setMnemonic( KeyEvent.VK_P );
		editMenu.add( editPasteItem );

		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( editMenu );
		
		
		JFrame frame = new JFrame( "Browser test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		TabbedBrowser browser = new TabbedBrowser( new PythonEvalPageLocationResolver(), this, location );
		browser.getComponent().setPreferredSize( new Dimension( 800, 600 ) );
		frame.setJMenuBar( menuBar );
		frame.add( browser.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
