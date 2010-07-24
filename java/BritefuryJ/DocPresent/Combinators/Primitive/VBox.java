//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Combinators.Pres;

public class VBox extends Pres
{
	private Pres children[];
	private int refPointIndex;
	
	
	public VBox(Object children[])
	{
		this.children = mapCoerce( children );
		this.refPointIndex = -1;
	}
	
	public VBox(Object children[], int refPointIndex)
	{
		this.children = mapCoerce( children );
		this.refPointIndex = refPointIndex;
	}
	
	public VBox(List<Object> children)
	{
		this.children = mapCoerce( children );
		this.refPointIndex = -1;
	}

	public VBox(List<Object> children, int refPointIndex)
	{
		this.children = mapCoerce( children );
		this.refPointIndex = refPointIndex;
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPVBox element = new DPVBox( Primitive.vboxParams.get( ctx.getStyle() ) );
		element.setChildren( mapPresent( ctx.withStyle( Primitive.useContainerParams( ctx.getStyle() ) ), children ) );
		if ( refPointIndex != -1 )
		{
			element.setRefPointIndex( refPointIndex );
		}
		return element;
	}
}
