//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class LazyPres extends Pres
{
	public static interface PresFactory
	{
		public Pres createPres();
	}
	
	
	private PresFactory fac;
	
	
	public LazyPres(PresFactory fac)
	{
		this.fac = fac;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		Pres p = fac.createPres();
		return p.present( ctx, style );
	}
}
