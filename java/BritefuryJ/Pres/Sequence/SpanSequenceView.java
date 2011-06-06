//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Sequence;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.LineBreakCostSpan;
import BritefuryJ.Pres.Primitive.ParagraphIndentMatchSpan;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.StyleSheet.StyleValues;

public class SpanSequenceView extends AbstractSequenceView
{
	public SpanSequenceView(Object children[], Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}
	
	public SpanSequenceView(List<Object> children, Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		boolean bAddLineBreaks = style.get( Sequence.addLineBreaks, Boolean.class );
		boolean bAddParagraphIndentMarkers = style.get( Sequence.matchOuterIndentation, Boolean.class );
		boolean bAddLineBreakCost = style.get( Sequence.addLineBreakCost, Boolean.class );
		
		int factor = 1;
		if ( separator != null )
		{
			factor++;
		}
		if ( spacing != null )
		{
			factor++;
		}
		if ( bAddLineBreaks )
		{
			factor++;
		}
		ArrayList<Object> childElems = new ArrayList<Object>();
		ArrayList<Object> spanElems = null;
		childElems.ensureCapacity( ( children.length - 1 ) * factor + 4 );

		if ( bAddParagraphIndentMarkers )
		{
			spanElems = new ArrayList<Object>();
			if ( beginDelim != null )
			{
				spanElems.add( beginDelim );
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
				if ( bAddLineBreaks )
				{
					childElems.add( new LineBreak() );
				}
			}

			childElems.add( children[children.length - 1] );
			
			if ( separator != null  &&  trailingSeparatorRequired( children.length, trailingSeparator ) )
			{
				childElems.add( separator );
			}
		}

		if ( bAddParagraphIndentMarkers )
		{
			spanElems.add( new ParagraphIndentMatchSpan( childElems ) );
			if ( endDelim != null )
			{
				spanElems.add( endDelim );
			}
		}
		else
		{
			if ( endDelim != null )
			{
				childElems.add( endDelim );
			}
			spanElems = childElems;
		}

		
		if ( bAddLineBreakCost )
		{
			return new LineBreakCostSpan( spanElems ).present( ctx, style );
		}
		else
		{
			return new Span( spanElems ).present( ctx, style );
		}
	}
}
