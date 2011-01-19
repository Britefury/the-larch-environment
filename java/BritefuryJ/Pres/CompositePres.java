//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class CompositePres extends Pres
{
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return pres( ctx, style ).present( ctx, style );
	}

	public abstract Pres pres(PresentationContext ctx, StyleValues style);
}
