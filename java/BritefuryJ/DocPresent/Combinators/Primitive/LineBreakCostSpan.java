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
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;

public class LineBreakCostSpan extends SequentialPres
{
	public LineBreakCostSpan(Object children[])
	{
		super( children );
	}
	
	public LineBreakCostSpan(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPLineBreakCostSpan element = new DPLineBreakCostSpan( Primitive.containerParams.get( ctx.getStyle() ) );
		element.setChildren( mapPresent( ctx.withStyle( Primitive.useContainerParams.get( ctx.getStyle() ) ), children ) );
		return element;
	}
}
