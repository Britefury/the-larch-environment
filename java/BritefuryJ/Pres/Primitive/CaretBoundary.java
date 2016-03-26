//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSCaretBoundary;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSHiddenText;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class CaretBoundary extends Pres
{
	public CaretBoundary()
	{
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSCaretBoundary();
	}
}
