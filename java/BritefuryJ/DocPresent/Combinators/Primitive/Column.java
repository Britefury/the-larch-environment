//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPColumn;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Column extends AbstractBox
{
	private int refPointIndex;
	
	
	public Column(Object children[])
	{
		super( children );
		this.refPointIndex = -1;
	}
	
	public Column(Object children[], int refPointIndex)
	{
		super( children );
		this.refPointIndex = refPointIndex;
	}
	
	public Column(List<Object> children)
	{
		super( children );
		this.refPointIndex = -1;
	}

	public Column(List<Object> children, int refPointIndex)
	{
		super( children );
		this.refPointIndex = refPointIndex;
	}

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPColumn element = new DPColumn( Primitive.columnParams.get( style ) );
		element.setChildren( mapPresent( ctx, Primitive.useColumnParams( style ), children ) );
		if ( refPointIndex != -1 )
		{
			element.setRefPointIndex( refPointIndex );
		}
		return element;
	}
}
