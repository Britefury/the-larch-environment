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

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Painter.FilledOutlinePainter;
import BritefuryJ.DocPresent.Painter.OutlinePainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

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
	
	
	protected Pres makeParagraph(String title, double indentation, int lineBreakStep, StyleSheet textStyle)
	{
		ArrayList<Object> children = ParagraphTestPage.makeTextNodes( title + ": " + textBlock );
		if ( lineBreakStep > 0 )
		{
			children = ParagraphTestPage.addLineBreaks( children, lineBreakStep );
		}
		return textStyle.withAttr( Primitive.paragraphIndentation, indentation ).applyTo( new Paragraph( children ) );
	}
	
	protected Pres makeSpan(String title, int lineBreakStep, StyleSheet spanStyle)
	{
		ArrayList<Object> children = ParagraphTestPage.makeTextNodes( title + ": " + textBlock );
		if ( lineBreakStep > 0 )
		{
			children = ParagraphTestPage.addLineBreaks( children, lineBreakStep );
		}
		return spanStyle.applyTo( new Span( children ) );
	}
	
	protected Pres makeParagraphWithNestedSpan(String title, double indentation, int lineBreakStep, StyleSheet textStyle, StyleSheet nestedTextStyle, StyleSheet spanStyle)
	{
		ArrayList<Object> children = ParagraphTestPage.makeTextNodes( title + ": " + textBlock );
		children = ParagraphTestPage.addLineBreaks( children, lineBreakStep );
		children.add( children.size()/2, makeSpan( title + " (inner)", lineBreakStep, spanStyle ) );
		return textStyle.withAttr( Primitive.paragraphIndentation, indentation ).applyTo( new Paragraph( children ) );
	}
	
	
	protected Pres createContents()
	{
		StyleSheet styleSheet = StyleSheet.instance;
		StyleSheet nestedTextStyleSheet = styleSheet.withAttr( Primitive.foreground, Color.red );
		StyleSheet spanStyleSheet = styleSheet.withAttr( Primitive.background, new OutlinePainter( new Color( 1.0f, 0.7f, 0.3f ) ) ).withAttr(
				Primitive.hoverBackground, new FilledOutlinePainter( new Color( 1.0f, 1.0f, 0.7f ), new Color( 1.0f, 1.0f, 0.0f ), new BasicStroke( 1.0f ) ) );
		
		Pres b2 = makeParagraph( "PER-WORD", 0.0, 1, styleSheet );
		Pres b3 = makeParagraph( "EVERY-4-WORDS", 0.0, 4, styleSheet);
		Pres b4 = makeParagraphWithNestedSpan( "NESTED-1", 0.0, 1, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		Pres b5 = makeParagraphWithNestedSpan( "NESTED-2", 0.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		Pres b6 = makeParagraphWithNestedSpan( "NESTED-4", 0.0, 4, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		Pres b7 = makeParagraph( "PER-WORD INDENTED", 50.0, 1, styleSheet );
		Pres b8 = makeParagraphWithNestedSpan( "NESTED-2-INDENTED", 50.0, 2, styleSheet, nestedTextStyleSheet, spanStyleSheet );
		Pres[] children = { b2, b3, b4, b5, b6, b7, b8 };
		return new Body( children );
	}
}
