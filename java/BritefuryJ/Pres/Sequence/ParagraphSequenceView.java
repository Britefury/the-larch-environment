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
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.ParagraphDedentMarker;
import BritefuryJ.Pres.Primitive.ParagraphIndentMarker;
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
