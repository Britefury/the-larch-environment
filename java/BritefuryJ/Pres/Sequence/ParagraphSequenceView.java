//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Sequence;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.ParagraphIndentMatchSpan;
import BritefuryJ.StyleSheet.StyleValues;

public class ParagraphSequenceView extends AbstractSequenceView
{
	public ParagraphSequenceView(Object children[], Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}
	
	public ParagraphSequenceView(List<Object> children, Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		boolean bAddParagraphIndentMarkers = style.get( Sequence.matchOuterIndentation, Boolean.class );
		
		int factor = 1;
		if ( separator != null )
		{
			factor++;
		}
		if ( spacing != null )
		{
			factor++;
		}
		ArrayList<Object> childElems = new ArrayList<Object>();
		ArrayList<Object> paragraphElems = null;
		childElems.ensureCapacity( ( children.length - 1 ) * factor + 4 );

		if ( bAddParagraphIndentMarkers )
		{
			paragraphElems = new ArrayList<Object>();
			if ( beginDelim != null )
			{
				paragraphElems.add( beginDelim );
			}
		}
		else
		{
			if ( beginDelim != null )
			{
				childElems.add( beginDelim );
			}
		}
		
		if ( children.length > 0 )
		{
			for (int i = 0; i < children.length - 1; i++)
			{
				childElems.add( children[i] );
				if ( separator != null )
				{
					childElems.add( separator );
				}
				if ( spacing != null )
				{
					childElems.add( spacing );
				}
				childElems.add( new LineBreak() );
			}

			childElems.add( children[children.length - 1] );
			
			if ( separator != null  &&  trailingSeparatorRequired( children.length, trailingSeparator ) )
			{
				childElems.add( separator );
			}
		}

		if ( bAddParagraphIndentMarkers )
		{
			paragraphElems.add( new ParagraphIndentMatchSpan( childElems ) );
			if ( endDelim != null )
			{
				paragraphElems.add( endDelim );
			}
		}
		else
		{
			if ( endDelim != null )
			{
				childElems.add( endDelim );
			}
			paragraphElems = childElems;
		}
		
		
		return new Paragraph( paragraphElems ).present( ctx, style );
	}

}
