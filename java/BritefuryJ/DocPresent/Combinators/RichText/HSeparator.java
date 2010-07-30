//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class HSeparator extends Pres
{
	public HSeparator()
	{
	}

	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleValues style = ctx.getStyle();
		double majorPadding = style.get( RichText.separatorMajorPadding, Double.class );
		double minorPadding = style.get( RichText.separatorMinorPadding, Double.class );
		return RichText.separatorStyle( style ).applyTo( 
				new Box( 0.0, 1.0 ).alignHExpand().pad( majorPadding, minorPadding ) ).present( ctx );
	}
}
