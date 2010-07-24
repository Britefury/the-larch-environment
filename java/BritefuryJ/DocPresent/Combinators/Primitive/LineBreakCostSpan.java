//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPLineBreakCostSpan;
import BritefuryJ.DocPresent.Combinators.Pres;

public class LineBreakCostSpan extends Pres
{
	private Pres children[];
	
	
	public LineBreakCostSpan(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public LineBreakCostSpan(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPLineBreakCostSpan element = new DPLineBreakCostSpan( Primitive.containerParams.get( ctx.getStyle() ) );
		element.setChildren( mapPresent( ctx.withStyle( Primitive.useContainerParams( ctx.getStyle() ) ), children ) );
		return element;
	}
}
