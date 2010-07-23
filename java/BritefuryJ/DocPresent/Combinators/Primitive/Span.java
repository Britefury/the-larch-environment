//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.Combinators.Pres;

public class Span extends Pres
{
	private Pres children[];
	
	
	public Span(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public Span(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPSpan element = new DPSpan( ctx.getStyle().getContainerParams() );
		element.setChildren( mapPresent( ctx.withStyle( ctx.getStyle().useContainerParams() ), children ) );
		return element;
	}
}
