//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSColumn;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Column extends AbstractBox
{
	private int refPointIndex;
	
	
	public Column(Object children[])
	{
		super( children );
		this.refPointIndex = -1;
	}
	
	public Column(int refPointIndex, Object children[])
	{
		super( children );
		this.refPointIndex = refPointIndex;
	}
	
	public Column(List<Object> children)
	{
		super( children );
		this.refPointIndex = -1;
	}

	public Column(int refPointIndex, List<Object> children)
	{
		super( children );
		this.refPointIndex = refPointIndex;
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElems = mapPresent( ctx, Primitive.useColumnParams( style ), children );
		LSColumn element = new LSColumn( Primitive.columnParams.get( style ), childElems );
		if ( refPointIndex != -1 )
		{
			element.setRefPointIndex( refPointIndex );
		}
		return element;
	}
}
