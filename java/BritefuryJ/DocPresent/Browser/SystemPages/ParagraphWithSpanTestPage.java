//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ParagraphWithSpanTestPage extends SystemPage
{
	protected ParagraphWithSpanTestPage()
	{
		register( "tests.parawithspan" );
	}
	
	
	public String getTitle()
	{
		return "Paragraph with span test";
	}

	protected String getDescription()
	{
		return "Collateable elements such as proxy, span, and segment will place their children into a collation root element, such as a paragraph. " +
		"The red text is within a span, which is within a paragraph, which contains black text on either side. Note that it flows as if it is all part of one paragraph. " +
		"Also demonstrates background painting, with hover enabled."; 
	}

	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPElement> makeTextNodes(String text, PrimitiveStyleSheet style)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( style.text( words[i] ) );
		}
		return nodes;
	}
	
	protected ArrayList<DPElement> addLineBreaks(ArrayList<DPElement> nodesIn, int step, PrimitiveStyleSheet style)
	{
		ArrayList<DPElement> nodesOut = new ArrayList<DPElement>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			nodesOut.add( style.text( " " ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( style.lineBreak() );
			}
		}
		return nodesOut;
	}
	
	
	protected DPElement makeParagraph(String title, double indentation, int lineBreakStep, PrimitiveStyleSheet textStyle)
	{
		ArrayList<DPElement> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep, textStyle );
		}
		return textStyle.withParagraphIndentation( indentation ).paragraph( children );
	}
	
	protected DPElement makeSpan(String title, int lineBreakStep, PrimitiveStyleSheet textStyle, PrimitiveStyleSheet spanStyle)
	{
		ArrayList<DPElement> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep, textStyle );
		}
		return spanStyle.span( children );
	}
	
	protected DPElement makeParagraphWithNestedSpan(String title, double indentation, int lineBreakStep, PrimitiveStyleSheet textStyle, PrimitiveStyleSheet nestedTextStyle, PrimitiveStyleSheet spanStyle)
	{
		ArrayList<DPElement> children = makeTextNodes( title + ": " + textBlock, textStyle );
		children = addLineBreaks( children, lineBreakStep, textStyle );
		children.add( children.size()/2, makeSpan( title + " (inner)", lineBreakStep, nestedTextStyle, spanStyle ) );
		return textStyle.withParagraphIndentation( indentation ).paragraph( children );
	}
	
	
	protected DPElement createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet nestedTextStyleSheet = styleSheet.withForeground( Color.red );
		PrimitiveStyleSheet spanStyleSheet = styleSheet.withBackground( new OutlinePainter( new Color( 1.0f, 0.7f, 0.3f ) ) ).withHoverBackground( 
				new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ), new BasicStroke( 1.0f ) ) );
		
		DPElement b2 = makeParagraph( "PER-WORD", 0.0, 1, styleSheet );
		DPElement b3 = makeParagraph( "EVERY-4-WORDS", 0.0, 4, styleSheet);
		DPElement b4 = makeParagraphWithNestedSpan( "NESTED-1", 0.0, 1, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPElement b5 = makeParagraphWithNestedSpan( "NESTED-2", 0.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPElement b6 = makeParagraphWithNestedSpan( "NESTED-4", 0.0, 4, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPElement b7 = makeParagraph( "PER-WORD INDENTED", 50.0, 1, styleSheet );
		DPElement b8 = makeParagraphWithNestedSpan( "NESTED-2-INDENTED", 50.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPElement[] children = { b2, b3, b4, b5, b6, b7, b8 };
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( Arrays.asList( children ) );
	}
}
