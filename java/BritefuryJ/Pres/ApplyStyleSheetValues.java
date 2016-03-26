//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class ApplyStyleSheetValues extends Pres
{
	private StyleValues styleSheetValues;
	private Pres child;
	
	
	public ApplyStyleSheetValues(StyleValues styleSheetValues, Pres child)
	{
		this.styleSheetValues = styleSheetValues;
		this.child = child;
	}
	

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return child.present( ctx, styleSheetValues );
	}
}
