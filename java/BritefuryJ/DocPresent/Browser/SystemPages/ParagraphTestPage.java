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
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ParagraphTestPage extends SystemPage
{
	protected ParagraphTestPage()
	{
		register( "tests.paragraph" );
	}
	
	
	public String getTitle()
	{
		return "Paragraph test";
	}
	
	protected String getDescription()
	{
		return "The paragraph element breaks lines at line break elements."; 
	}

	
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPElement> makeTextNodes(String text, PrimitiveStyleSheet styleSheet)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( styleSheet.text( words[i] ) );
		}
		return nodes;
	}
	
	protected ArrayList<DPElement> addLineBreaks(ArrayList<DPElement> nodesIn, int step, PrimitiveStyleSheet styleSheet)
	{
		ArrayList<DPElement> nodesOut = new ArrayList<DPElement>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			nodesOut.add( styleSheet.text( " " ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( styleSheet.lineBreak() );
			}
		}
		return nodesOut;
	}
	
	
	protected DPParagraph makeParagraph(String title, int lineBreakStep, PrimitiveStyleSheet styleSheet)
	{
		ArrayList<DPElement> children = makeTextNodes( title + ": " + textBlock, styleSheet );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep, styleSheet );
		}
		return styleSheet.paragraph( children.toArray( new DPElement[0] ) );
	}
	
	protected DPParagraph makeParagraphWithNestedPara(String title, int lineBreakStep, PrimitiveStyleSheet textStyle, PrimitiveStyleSheet nestedTextStyle)
	{
		ArrayList<DPElement> children = makeTextNodes( title + ": " + textBlock, textStyle );
		children = addLineBreaks( children, lineBreakStep, textStyle );
		children.add( children.size()/2, makeParagraph( title + " (inner)", lineBreakStep, nestedTextStyle ) );
		return textStyle.paragraph( children.toArray( new DPElement[0] ) );
	}
	
	
	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet blackText = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( Color.black );
		PrimitiveStyleSheet redText = PrimitiveStyleSheet.instance.withFontSize( 12 ).withForeground( Color.red );
		
		DPElement b2 = makeParagraph( "PER-WORD", 1, blackText );
		DPElement b3 = makeParagraph( "EVERY-4-WORDS", 4, blackText);
		DPElement b4 = makeParagraphWithNestedPara( "NESTED-1", 1, blackText, redText );
		DPElement b5 = makeParagraphWithNestedPara( "NESTED-2", 2, blackText, redText );
		DPElement b6 = makeParagraphWithNestedPara( "NESTED-4", 4, blackText, redText );
		DPElement b7 = makeParagraph( "PER-WORD INDENTED", 1, blackText );
		DPElement b8 = makeParagraphWithNestedPara( "NESTED-2-INDENTED", 2, blackText, redText );
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( new DPElement[] { b2, b3, b4, b5, b6, b7, b8 } );
	}
}
