//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.StyleSheet.StyleValues;

public class VerticalInlineSequenceView extends AbstractSequenceView
{
	public VerticalInlineSequenceView(Object children[], Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}
	
	public VerticalInlineSequenceView(List<Object> children, Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}

	
	private Pres createLineParagraph(Pres child, Pres separator)
	{
		if ( separator != null )
		{
			return new Paragraph( new Pres[] { child, separator } );
		}
		else
		{
			return child;
		}
	}

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double indentation = style.get( Sequence.indentation, Double.class );
		
		if ( children.length <= 1 )
		{
			// Paragraph with contents: [ beginDelim ] + children + [ endDelim ]
			ArrayList<Object> childElems = new ArrayList<Object>();
			if ( beginDelim != null )
			{
				childElems.add( beginDelim );
			}
			
			childElems.addAll( Arrays.asList( children ) );
			
			if ( trailingSeparatorRequired( children.length, trailingSeparator )  &&  children.length == 1 )
			{
				childElems.add( separator );
			}
			
			if ( endDelim != null )
			{
				childElems.add( endDelim );
			}
			
			return new Paragraph( childElems ).present( ctx, style );
		}
		else
		{
			// First line
			Pres first = null;
			if ( beginDelim != null  ||  separator != null )
			{
				ArrayList<Object> firstChildElems = new ArrayList<Object>();
				firstChildElems.ensureCapacity( 3 );
				if ( beginDelim != null )
				{
					firstChildElems.add( beginDelim );
				}
				firstChildElems.add( children[0] );
				if ( separator != null )
				{
					firstChildElems.add( separator );
				}
				first = new Paragraph( firstChildElems );
			}
			else
			{
				first = children[0];
			}
			
			
			// Middle lines
			ArrayList<Object> childElems = new ArrayList<Object>();
			childElems.ensureCapacity( children.length );
			for (int i = 1; i < children.length - 1; i++)
			{
				childElems.add( createLineParagraph( children[i], separator ) );
			}
			
			// Last line
			if ( trailingSeparatorRequired( children.length, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( children[children.length-1], separator ) );
			}
			else
			{
				childElems.add( createLineParagraph( children[children.length-1], null ) );
			}
			
			Pres middleColumn = new Column( childElems );
			Pres indent = middleColumn.padX( indentation );
			
			
			if ( endDelim != null )
			{
				return new Column( new Pres[] { first, indent, endDelim }, 0 ).present( ctx, style );
			}
			else
			{
				return new Column( new Pres[] { first, indent }, 0 ).present( ctx, style );
			}
		}
	}

}
