//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpaceBin;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public class SpaceBin extends Pres
{
	private Pres child;
	private double minWidth, minHeight;
	
	
	public SpaceBin(Object child, double minWidth, double minHeight)
	{
		this.child = coerce( child );
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPSpaceBin bin = new DPSpaceBin( Primitive.containerParams.get( ctx.getStyle() ), minWidth, minHeight );
		bin.setChild( child.present( ctx.withStyle( Primitive.useContainerParams( ctx.getStyle() ) ) ).layoutWrap() );
		return bin;
	}
}
