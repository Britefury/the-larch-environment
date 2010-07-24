//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.LineBreak;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Segment;
import BritefuryJ.DocPresent.Combinators.Primitive.Span;
import BritefuryJ.DocPresent.Combinators.Primitive.Text;
import BritefuryJ.DocPresent.Combinators.Primitive.Whitespace;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

abstract class RichParagraph extends Pres
{
	private String text;
	
	
	public RichParagraph(String text)
	{
		this.text = text;
	}
	
	
	
	private ArrayList<Object> textToWordsAndLineBreaks()
	{
		ArrayList<Object> elements = new ArrayList<Object>();

		boolean bGotChars = false, bGotTrailingSpace = false;
		int wordStartIndex = 0;
		
		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt( i );
			if ( c == ' ' )
			{
				if ( bGotChars )
				{
					bGotTrailingSpace = true;
				}
			}
			else
			{
				if ( bGotTrailingSpace )
				{
					// Make text element for word
					String word = text.substring( wordStartIndex, i );
					elements.add( new Text( word ) );
					elements.add( new LineBreak() );
					
					// Begin new word
					bGotChars = bGotTrailingSpace = false;
					wordStartIndex = i;
				}
				else
				{
					bGotChars = true;
				}
			}
		}
		
		if ( wordStartIndex < text.length() )
		{
			String word = text.substring( wordStartIndex );
			elements.add( new Text( word ) );
			elements.add( new LineBreak() );
		}
		
		return elements;
	}
	

	protected DPElement presentParagraph(PresentationContext ctx)
	{
		List<Object> paragraphContents = null;
		
		if ( text.equals( "" ) )
		{
			paragraphContents = Arrays.asList( new Object[] { new Text( "" ) } );
		}
		else
		{
			paragraphContents = textToWordsAndLineBreaks();
		}
		
		StyleSheetValues style = ctx.getStyle();

		if ( Primitive.isEditable( style ) )
		{
			boolean bAppendNewline = style.get( RichText.appendNewlineToParagraphs, Boolean.class );
			if ( bAppendNewline )
			{
				return new Paragraph( new Pres[] { new Segment( true, true, new Span( paragraphContents ) ), new Whitespace( "\n" ) } ).present( ctx );
			}
			else
			{
				return new Paragraph( new Pres[] { new Segment( true, true, new Span( paragraphContents ) ) } ).present( ctx );
			}
		}
		else
		{
			return new Paragraph( paragraphContents ).present( ctx );
		}
	}
	
}
