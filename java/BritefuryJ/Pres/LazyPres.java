//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
