//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;

public class Box extends PresentationCombinator
{
	private double minWidth, minHeight;
	
	
	public Box(double minWidth, double minHeight)
	{
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return new DPBox( ctx.getStyle().getShapeParams(), "", minWidth, minHeight );
	}
}
