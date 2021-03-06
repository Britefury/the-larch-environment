//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Sequence;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.StyleSheet.StyleValues;

public class VerticalSequenceView extends AbstractSequenceView
{
	public VerticalSequenceView(Object children[], Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
	{
		super( children, beginDelim, endDelim, separator, spacing, trailingSeparator );
	}
	
	public VerticalSequenceView(List<Object> children, Pres beginDelim, Pres endDelim, Pres separator, Pres spacing, TrailingSeparator trailingSeparator)
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
		
		ArrayList<Object> childElems = new ArrayList<Object>();
		childElems.ensureCapacity( children.length );
		
		if ( children.length > 0 )
		{
			for (int i = 0; i < children.length - 1; i++)
			{
				childElems.add( createLineParagraph( children[i], separator ) );
			}

			if ( trailingSeparatorRequired( children.length, trailingSeparator ) )
			{
				childElems.add( createLineParagraph( children[children.length - 1], separator ) );
			}
			else
			{
				childElems.add( children[children.length - 1] );
			}
		}

		Pres column = new Column( childElems );
		Pres indented = column.padX( indentation, 0.0 );
		
		
		if ( beginDelim != null  ||  endDelim != null )
		{
			ArrayList<Object> outerChildElems = new ArrayList<Object>();
			outerChildElems.ensureCapacity( 3 );
			
			if ( beginDelim != null )
			{
				outerChildElems.add( beginDelim );
			}
			
			outerChildElems.add( indented );
			
			if ( endDelim != null )
			{
				outerChildElems.add(  endDelim );
			}
			
			return new Column( 0, outerChildElems ).present( ctx, style );
		}
		else
		{
			return indented.present( ctx, style );
		}
	}

}
