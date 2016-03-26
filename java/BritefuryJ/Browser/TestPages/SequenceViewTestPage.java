//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Whitespace;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.Pres.RichText.Heading3;
import BritefuryJ.Pres.Sequence.HorizontalSequenceView;
import BritefuryJ.Pres.Sequence.ParagraphSequenceView;
import BritefuryJ.Pres.Sequence.TrailingSeparator;
import BritefuryJ.Pres.Sequence.VerticalInlineSequenceView;
import BritefuryJ.Pres.Sequence.VerticalSequenceView;
import BritefuryJ.StyleSheet.StyleSheet;

public class SequenceViewTestPage extends TestPage
{
	protected SequenceViewTestPage()
	{
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
			p[i] = new Label( texts[i] );
		}
		return p;
	}
	
	protected Pres createContents()
	{
		String[] lessTexts = new String[] { "abcdef", "hello", "world" };
		String[] texts = new String[] { "abcdef", "123456", "hello", "world", "this", "is", "a", "test", "of", "the", "list", "layout", "system" };


		StyleSheet puncStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.5f, 0.0f ) ) );
		Pres beginDelim = puncStyle.applyTo( new Label( "[" ) );
		Pres endDelim = puncStyle.applyTo( new Label( "]" ) );
		Pres separator = puncStyle.applyTo( new Label( "," ) );
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
