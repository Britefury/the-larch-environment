//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ScrolledViewportTestPage extends SystemPage
{
	protected ScrolledViewportTestPage()
	{
		register( "tests.controls.scrolledviewport" );
	}
	
	
	public String getTitle()
	{
		return "Scrolled viewport test";
	}
	
	protected String getDescription()
	{
		return "The scrolled viewport control combined a viewport with scrollbars."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPElement> makeTextNodes(String text, PrimitiveStyleSheet styleSheet)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( styleSheet.staticText( words[i] ) );
			nodes.add( styleSheet.staticText( " " ) );
			nodes.add( styleSheet.lineBreak() );
		}
		return nodes;
	}
	
	
	protected DPParagraph makeParagraph(String title, int lineBreakStep, PrimitiveStyleSheet styleSheet)
	{
		return styleSheet.paragraph( makeTextNodes( title + ": " + textBlock, styleSheet ).toArray( new DPElement[0] ) );
	}
	
	
	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet blackText = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( Color.black );
		
		PrimitiveStyleSheet borderStyle = PrimitiveStyleSheet.instance.withBorder( new SolidBorder( 2.0, 2.0, 5.0, 5.0, Color.black, null ) );
		
		ControlsStyleSheet controls = ControlsStyleSheet.instance;
		
		DPElement b2 = makeParagraph( "Exampe text:", 1, blackText );
		
		DPElement viewport = styleSheet.spaceBin( controls.scrolledViewport( b2, 0.0, 0.0, new PersistentState() ).getElement().alignHExpand().alignVExpand(), 0.0, 200.0 );
		DPElement border = borderStyle.border( viewport.alignHExpand().alignVExpand() ).alignHExpand().alignVExpand();
		DPElement vbox = PrimitiveStyleSheet.instance.withVBoxSpacing( 5.0 ).vbox( new DPElement[] { PrimitiveStyleSheet.instance.staticText( "Viewport:" ), border } ).alignHExpand().alignVExpand();
		return vbox.pad( 50.0, 50.0 ).alignHExpand().alignVExpand();
	}
}
