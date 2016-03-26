//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpacer;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Spacer extends Pres
{
	private double minWidth, minHeight;
	
	
	public Spacer(double minWidth, double minHeight)
	{
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSSpacer( Primitive.elementParams.get( style ), minWidth, minHeight );
	}
}
