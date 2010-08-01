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
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
	public DPElement present(PresentationContext ctx, StyleValues style)
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

		Pres vbox = new VBox( childElems );
		Pres indented = vbox.padX( indentation, 0.0 );
		
		
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
			
			return new VBox( outerChildElems, 0 ).present( ctx, style );
		}
		else
		{
			return indented.present( ctx, style );
		}
	}

}
