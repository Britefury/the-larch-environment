//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Sequence;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.LineBreak;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.ParagraphDedentMarker;
import BritefuryJ.DocPresent.Combinators.Primitive.ParagraphIndentMarker;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		boolean bAddParagraphIndentMarkers = style.get( Sequence.addParagraphIndentMarkers, Boolean.class );
		
		ArrayList<Object> childElems = new ArrayList<Object>();
		childElems.ensureCapacity( children.length + 2 );
		
		if ( beginDelim != null )
		{
			childElems.add( beginDelim );
		}
		
		if ( bAddParagraphIndentMarkers )
		{
			childElems.add( new ParagraphIndentMarker() );
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
			childElems.add( new ParagraphDedentMarker() );
		}
		
		if ( endDelim != null )
		{
			childElems.add( endDelim );
		}
		
		
		return new Paragraph( childElems ).present( ctx, style );
	}

}
