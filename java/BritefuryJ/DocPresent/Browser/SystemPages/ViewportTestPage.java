//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ViewportTestPage extends SystemPage
{
	protected ViewportTestPage()
	{
		register( "tests.viewport" );
	}
	
	
	public String getTitle()
	{
		return "Viewport test";
	}
	
	protected String getDescription()
	{
		return "The viewport element allows zooming and panning of its contents."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPElement> makeTextNodes(String text, PrimitiveStyleSheet styleSheet)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( styleSheet.text( words[i] ) );
			nodes.add( styleSheet.text( " " ) );
			nodes.add( styleSheet.lineBreak() );
		}
		return nodes;
	}
	
	
	protected DPParagraph makeParagraph(String title, int lineBreakStep, PrimitiveStyleSheet styleSheet)
	{
		return styleSheet.paragraph( makeTextNodes( title + ": " + textBlock, styleSheet ) );
	}
	
	
	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet blackText = PrimitiveStyleSheet.instance.withFont( new Font( "Sans serif", Font.PLAIN, 12 ) ).withForeground( Color.black );
		
		PrimitiveStyleSheet borderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
		
		DPElement b2 = makeParagraph( "Exampe text:", 1, blackText );
		
		DPElement viewport = borderStyle.border( styleSheet.viewport( b2, 0.0, 200.0 ).alignHExpand().alignVExpand() ).alignHExpand().alignVExpand();
		DPElement vbox = PrimitiveStyleSheet.instance.withVBoxSpacing( 5.0 ).vbox( Arrays.asList( new DPElement[] { PrimitiveStyleSheet.instance.staticText( "Viewport:" ), viewport } ) ).alignHExpand().alignVExpand();
		return vbox.pad( 50.0, 50.0 ).alignHExpand().alignVExpand();
	}
}
