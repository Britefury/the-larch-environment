//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class CompositePres extends Pres
{
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return pres( ctx, style ).present( ctx, style );
	}

	public abstract Pres pres(PresentationContext ctx, StyleValues style);
}
