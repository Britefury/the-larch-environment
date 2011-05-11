//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Segment;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.Primitive.Whitespace;
import BritefuryJ.StyleSheet.StyleValues;

abstract class RichParagraph extends AbstractRichText
{
	public RichParagraph(Object contents[])
	{
		super( contents );
	}
	
	public RichParagraph(List<Object> contents)
	{
		super( contents );
	}
	
	public RichParagraph(String contents)
	{
		super( contents );
	}
	
	
	
	protected DPElement presentParagraph(PresentationContext ctx, StyleValues style)
	{
		List<Object> paragraphContents = null;
		
		if ( isEmpty() )
		{
			paragraphContents = Arrays.asList( new Object[] { new Text( "" ).alignHPack().alignVRefY() } );
		}
		else
		{
			paragraphContents = splitContents();
		}
		
		if ( Primitive.isEditable( style ) )
		{
			boolean bAppendNewline = style.get( RichText.appendNewlineToParagraphs, Boolean.class );
			if ( bAppendNewline )
			{
				return new Paragraph( new Pres[] { new Segment( true, true, new Span( paragraphContents ) ), new Whitespace( "\n" ) } ).present( ctx, style );
			}
			else
			{
				return new Paragraph( new Pres[] { new Segment( true, true, new Span( paragraphContents ) ) } ).present( ctx, style );
			}
		}
		else
		{
			return new Paragraph( paragraphContents ).present( ctx, style );
		}
	}
	
}
