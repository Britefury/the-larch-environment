//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.Combinators.PresentationCombinator;

public class HBox extends PresentationCombinator
{
	private PresentationCombinator children[];
	
	
	public HBox(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public HBox(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPHBox element = new DPHBox( ctx.getStyle().getHBoxParams() );
		element.setChildren( mapPresent( ctx, children ) );
		return element;
	}
}
