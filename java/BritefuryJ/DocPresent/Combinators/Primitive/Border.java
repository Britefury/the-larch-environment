//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public class Border extends Pres
{
	private Pres child;
	
	
	public Border(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPBorder bin = new DPBorder( Primitive.getBorderParams( ctx.getStyle() ) );
		bin.setChild( child.present( ctx.withStyle( Primitive.useBorderParams.get( ctx.getStyle() ) ) ).layoutWrap() );
		return bin;
	}
}
