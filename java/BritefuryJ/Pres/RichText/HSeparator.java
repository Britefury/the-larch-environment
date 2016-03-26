//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.StyleSheet.StyleValues;

public class HSeparator extends Pres
{
	public HSeparator()
	{
	}

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double majorPadding = style.get( RichText.separatorMajorPadding, Double.class );
		double minorPadding = style.get( RichText.separatorMinorPadding, Double.class );
		return RichText.separatorStyle( style ).applyTo( 
				new Box( 0.0, 1.0 ).alignHExpand().pad( majorPadding, minorPadding ) ).present( ctx, style );
	}
}
