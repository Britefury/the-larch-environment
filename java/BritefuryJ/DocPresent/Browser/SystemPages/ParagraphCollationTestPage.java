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

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ParagraphCollationTestPage extends SystemPage
{
	protected ParagraphCollationTestPage()
	{
		register( "tests.paracollate" );
	}
	
	
	public String getTitle()
	{
		return "Paragraph collation test";
	}

	protected String getDescription()
	{
		return "Collateable elements such as proxy, span, and segment will place their children into a collation root element, such as a paragraph. " +
		"The red text is within a span, which is within a paragraph, which contains black text on either side. Note that it flows as if it is all part of one paragraph. " +
		"Also demonstrates background painting, with hover enabled."; 
	}

	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected ArrayList<DPWidget> makeTextNodes(String text, PrimitiveStyleSheet style)
	{
		String[] words = text.split( " " );
		ArrayList<DPWidget> nodes = new ArrayList<DPWidget>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( style.text( words[i] ) );
		}
		return nodes;
	}
	
	protected ArrayList<DPWidget> addLineBreaks(ArrayList<DPWidget> nodesIn, int step, PrimitiveStyleSheet style)
	{
		ArrayList<DPWidget> nodesOut = new ArrayList<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( style.lineBreak( style.text( " " ) ) );
			}
			else
			{
				nodesOut.add( style.text( " " ) );
			}
		}
		return nodesOut;
	}
	
	
	protected DPWidget makeParagraph(String title, double indentation, int lineBreakStep, PrimitiveStyleSheet textStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep, textStyle );
		}
		return textStyle.withParagraphIndentation( indentation ).paragraph( children );
	}
	
	protected DPWidget makeSpan(String title, int lineBreakStep, PrimitiveStyleSheet textStyle, PrimitiveStyleSheet spanStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep, textStyle );
		}
		return spanStyle.span( children );
	}
	
	protected DPWidget makeParagraphWithNestedSpan(String title, double indentation, int lineBreakStep, PrimitiveStyleSheet textStyle, PrimitiveStyleSheet nestedTextStyle, PrimitiveStyleSheet spanStyle)
	{
		ArrayList<DPWidget> children = makeTextNodes( title + ": " + textBlock, textStyle );
		children = addLineBreaks( children, lineBreakStep, textStyle );
		children.add( children.size()/2, makeSpan( title + " (inner)", lineBreakStep, nestedTextStyle, spanStyle ) );
		return textStyle.withParagraphIndentation( indentation ).paragraph( children );
	}
	
	
	protected DPWidget createContents()
	{
		PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
		PrimitiveStyleSheet nestedTextStyleSheet = styleSheet.withForeground( Color.red );
		PrimitiveStyleSheet spanStyleSheet = styleSheet.withBackground( new OutlinePainter( new Color( 1.0f, 0.7f, 0.3f ) ) ).withHoverBackground( 
				new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ), new BasicStroke( 1.0f ) ) );
		
		DPWidget b2 = makeParagraph( "PER-WORD", 0.0, 1, styleSheet );
		DPWidget b3 = makeParagraph( "EVERY-4-WORDS", 0.0, 4, styleSheet);
		DPWidget b4 = makeParagraphWithNestedSpan( "NESTED-1", 0.0, 1, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPWidget b5 = makeParagraphWithNestedSpan( "NESTED-2", 0.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPWidget b6 = makeParagraphWithNestedSpan( "NESTED-4", 0.0, 4, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPWidget b7 = makeParagraph( "PER-WORD INDENTED", 50.0, 1, styleSheet );
		DPWidget b8 = makeParagraphWithNestedSpan( "NESTED-2-INDENTED", 50.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		DPWidget[] children = { b2, b3, b4, b5, b6, b7, b8 };
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( Arrays.asList( children ) );
	}
}
