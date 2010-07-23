//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.Combinators.Pres;

public class RGrid extends Pres
{
	private Pres children[];
	
	
	public RGrid(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public RGrid(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPRGrid element = new DPRGrid( ctx.getStyle().getTableParams() );
		element.setChildren( mapPresent( ctx.withStyle( ctx.getStyle().useTableParams() ), children ) );
		return element;
	}
}
