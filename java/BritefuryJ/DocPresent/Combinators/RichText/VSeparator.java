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

public class VSeparator extends Pres
{
	public VSeparator()
	{
	}

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double majorPadding = style.get( RichText.separatorMajorPadding, Double.class );
		double minorPadding = style.get( RichText.separatorMinorPadding, Double.class );
		return RichText.separatorStyle( style ).applyTo(
				new Box( 1.0, 0.0 ).alignVExpand().pad( majorPadding, minorPadding ) ).present( ctx, style );
	}
}
