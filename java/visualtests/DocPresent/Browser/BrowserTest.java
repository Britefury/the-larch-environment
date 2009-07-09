//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent.Browser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Browser.RootPage;
import BritefuryJ.DocPresent.Browser.TabbedBrowser;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;

public class BrowserTest
{
	public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		
		JFrame frame = new JFrame( "Browser test" );
		frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		
		RootPage rootPage = new RootPage()
		{
			public DPWidget getTitleElement()
			{
				StaticTextStyleSheet titleStyle = new StaticTextStyleSheet( new Font( "Serif", Font.BOLD, 32 ), Color.BLACK );
				return new DPStaticText( titleStyle, "Browser test" );
			}

			public DPWidget getPageContents()
			{
				StaticTextStyleSheet instructionsStyle = new StaticTextStyleSheet( new Font( "SansSerif", Font.PLAIN, 16 ), Color.BLACK );
				return new DPStaticText( instructionsStyle, "Please enter a location in the location box above." );
			}
		};
		
		TabbedBrowser browser = new TabbedBrowser( null, rootPage, "" );
		browser.getComponent().setPreferredSize( new Dimension( 800, 600 ) );
		frame.add( browser.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
