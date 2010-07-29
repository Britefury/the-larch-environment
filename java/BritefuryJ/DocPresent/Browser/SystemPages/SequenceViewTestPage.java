//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Whitespace;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;
import BritefuryJ.DocPresent.Combinators.RichText.Heading3;
import BritefuryJ.DocPresent.Combinators.Sequence.HorizontalSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.ParagraphSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.VerticalInlineSequenceView;
import BritefuryJ.DocPresent.Combinators.Sequence.VerticalSequenceView;
import BritefuryJ.DocPresent.ListView.TrailingSeparator;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class SequenceViewTestPage extends SystemPage
{
	protected SequenceViewTestPage()
	{
		register( "tests.sequenceview" );
	}
	
	
	public String getTitle()
	{
		return "Sequence view test";
	}
	
	protected String getDescription()
	{
		return "The sequence view combinators separates their children with optional separators, and bounds the sequence with optional delimiters. There are a variety of layout styles."; 
	}

	
	protected Pres[] makeTexts(String texts[])
	{
		Pres p[] = new Pres[texts.length];
		for (int i = 0; i < texts.length; i++)
		{
			p[i] = new StaticText( texts[i] );
		}
		return p;
	}
	
	protected Pres createContents()
	{
		String[] lessTexts = new String[] { "abcdef", "hello", "world" };
		String[] texts = new String[] { "abcdef", "123456", "hello", "world", "this", "is", "a", "test", "of", "the", "list", "layout", "system" };
		
		
		StyleSheet2 puncStyle = StyleSheet2.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
		Pres beginDelim = puncStyle.applyTo( new StaticText( "[" ) );
		Pres endDelim = puncStyle.applyTo( new StaticText( "]" ) );
		Pres separator = puncStyle.applyTo( new StaticText( "," ) );
		Pres whitespace = new Whitespace( " " );
		
		return new Body( new Pres[] { new Heading2( "Sequence views" ),
				new Heading3( "Paragraph" ),
				new ParagraphSequenceView( makeTexts( texts ), beginDelim, endDelim, separator, whitespace, TrailingSeparator.NEVER ),
				new Heading3( "Horizontal" ),
				new HorizontalSequenceView( makeTexts( lessTexts ), beginDelim, endDelim, separator, whitespace, TrailingSeparator.NEVER ),
				new Heading3( "Vertical" ),
				new VerticalSequenceView( makeTexts( texts ), beginDelim, endDelim, separator, whitespace, TrailingSeparator.NEVER ),
				new Heading3( "Vertical in-line" ),
				new VerticalInlineSequenceView( makeTexts( texts ), beginDelim, endDelim, separator, whitespace, TrailingSeparator.NEVER ),
				} );
	}
}
