//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import BritefuryJ.DocPresent.DPElement;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		double majorPadding = style.get( RichText.separatorMajorPadding, Double.class );
		double minorPadding = style.get( RichText.separatorMinorPadding, Double.class );
		return RichText.separatorStyle( style ).applyTo( 
				new Box( 0.0, 1.0 ).alignHExpand().pad( majorPadding, minorPadding ) ).present( ctx, style );
	}
}
