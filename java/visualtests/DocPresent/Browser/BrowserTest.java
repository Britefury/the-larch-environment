//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent.Browser;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.Browser.TabbedBrowser;

public class BrowserTest implements TabbedBrowser.TabbedBrowserListener
{
	public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		
		BrowserTest test = new BrowserTest();
		test.createNewBrowserWindow( "" );
	}

	
	public void createNewBrowserWindow(String location)
	{
		JFrame frame = new JFrame( "Browser test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		TabbedBrowser browser = new TabbedBrowser( null, this, location );
		browser.getComponent().setPreferredSize( new Dimension( 800, 600 ) );
		frame.add( browser.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
