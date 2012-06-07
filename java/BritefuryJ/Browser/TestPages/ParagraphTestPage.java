//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class ParagraphTestPage extends TestPage
{
	protected ParagraphTestPage()
	{
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
	
	protected static ArrayList<Object> makeTextNodes(String text)
	{
		String[] words = text.split( " " );
		ArrayList<Object> nodes = new ArrayList<Object>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( new Text( words[i] ) );
		}
		return nodes;
	}
	
	protected static ArrayList<Object> addLineBreaks(ArrayList<Object> nodesIn, int step)
	{
		ArrayList<Object> nodesOut = new ArrayList<Object>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			nodesOut.add( new Text( " " ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( new LineBreak() );
			}
		}
		return nodesOut;
	}
	
	
	protected Pres makeParagraph(String title, int lineBreakStep, StyleSheet style)
	{
		ArrayList<Object> children = makeTextNodes( title + ": " + textBlock );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		return style.applyTo( new Paragraph( children ) );
	}
	
	protected Pres makeParagraphWithNestedPara(String title, int lineBreakStep, StyleSheet textStyle, StyleSheet nestedTextStyle)
	{
		ArrayList<Object> children = makeTextNodes( title + ": " + textBlock );
		children = addLineBreaks( children, lineBreakStep );
		children.add( children.size()/2, makeParagraph( title + " (inner)", lineBreakStep, nestedTextStyle ) );
		return textStyle.applyTo( new Paragraph( children ) );
	}
	
	
	protected Pres createContents()
	{
		StyleSheet blackText = StyleSheet.style( Primitive.fontSize.as( 12 ), Primitive.foreground.as( Color.black ) );
		StyleSheet redText = StyleSheet.style( Primitive.fontSize.as( 12 ), Primitive.foreground.as( Color.red ) );
		
		Pres b2 = makeParagraph( "PER-WORD", 1, blackText );
		Pres b3 = makeParagraph( "EVERY-4-WORDS", 4, blackText);
		Pres b4 = makeParagraphWithNestedPara( "NESTED-1", 1, blackText, redText );
		Pres b5 = makeParagraphWithNestedPara( "NESTED-2", 2, blackText, redText );
		Pres b6 = makeParagraphWithNestedPara( "NESTED-4", 4, blackText, redText );
		Pres b7 = makeParagraph( "PER-WORD INDENTED", 1, blackText );
		Pres b8 = makeParagraphWithNestedPara( "NESTED-2-INDENTED", 2, blackText, redText );
		
		return new Body( new Pres[] { b2, b3, b4, b5, b6, b7, b8 } );
	}
}
