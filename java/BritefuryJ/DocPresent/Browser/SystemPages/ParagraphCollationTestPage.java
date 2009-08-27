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

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ParagraphCollationTestPage extends SystemPage
{
	protected ParagraphCollationTestPage()
	{
		register( "tests.paracollate" );
	}
	
	
	protected String getTitle()
	{
		return "Paragraph collation test";
	}

	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPWidget> makeTextNodes(String text, TextStyleSheet style)
	{
		String[] words = text.split( " " );
		ArrayList<DPWidget> nodes = new ArrayList<DPWidget>();
		for (int i = 0; i < words.length; i++)
		{
			String word = words[i];
			nodes.add( new DPText( style, word ) );
		}
		return nodes;
	}
	
	protected ArrayList<DPWidget> addLineBreaks(ArrayList<DPWidget> nodesIn, int step)
	{
		ArrayList<DPWidget> nodesOut = new ArrayList<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				DPText space = new DPText( " " );
				DPLineBreak b = new DPLineBreak();
				b.setChild( space );
				nodesOut.add( b );
			}
			else
			{
				nodesOut.add( new DPText( " " ) );
			}
		}
		return nodesOut;
	}
	
	
	protected DPParagraph makeParagraph(String title, double spacing, double vSpacing, double indentation, int lineBreakStep, TextStyleSheet textStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		ParagraphStyleSheet boxs = new ParagraphStyleSheet( spacing, vSpacing, indentation );
		DPParagraph box = new DPParagraph( boxs );
		box.extend( children );
		return box;
	}
	
	protected DPSpan makeSpan(String title, int lineBreakStep, TextStyleSheet textStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		DPSpan span = new DPSpan();
		span.extend( children );
		return span;
	}
	
	protected DPParagraph makeParagraphWithNestedSpan(String title, double spacing, double vSpacing, double indentation, int lineBreakStep, TextStyleSheet textStyle, TextStyleSheet nestedTextStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		children = addLineBreaks( children, lineBreakStep );
		children.add( children.size()/2, makeSpan( title + " (inner)", lineBreakStep, nestedTextStyle ) );
		ParagraphStyleSheet boxs = new ParagraphStyleSheet( spacing, vSpacing, indentation );
		DPParagraph box = new DPParagraph( boxs );
		box.extend( children );
		return box;
	}
	
	
	protected DPWidget createContents()
	{
		TextStyleSheet blackText = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		TextStyleSheet redText = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 12 ), Color.red );
		
		DPWidget b1 = makeParagraph( "ONE-LINE", 0.0, 0.0, 0.0, 0, blackText );
		DPWidget b2 = makeParagraph( "PER-WORD", 0.0, 0.0, 0.0, 1, blackText );
		DPWidget b3 = makeParagraph( "EVERY-4-WORDS", 0.0, 0.0, 0.0, 4, blackText);
		DPWidget b4 = makeParagraphWithNestedSpan( "NESTED-1", 0.0, 0.0, 0.0, 1, blackText, redText );
		DPWidget b5 = makeParagraphWithNestedSpan( "NESTED-2", 0.0, 0.0, 0.0, 2, blackText, redText );
		DPWidget b6 = makeParagraphWithNestedSpan( "NESTED-4", 0.0, 0.0, 0.0, 4, blackText, redText );
		DPWidget b7 = makeParagraph( "PER-WORD INDENTED", 0.0, 0.0, 50.0, 1, blackText );
		DPWidget b8 = makeParagraphWithNestedSpan( "NESTED-2-INDENTED", 0.0, 0.0, 50.0, 2, blackText, redText );
		DPWidget[] children = { b1, b2, b3, b4, b5, b6, b7, b8 };
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, 30.0 );
		DPVBox box = new DPVBox( boxs );
		box.extend( children );
		return box;
	}
}
