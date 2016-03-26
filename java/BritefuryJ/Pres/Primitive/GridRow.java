//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSGridRow;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

public class GridRow extends SequentialPres
{
	public GridRow(Object children[])
	{
		super( children );
	}
	
	public GridRow(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElems = mapPresent( ctx, Primitive.useGridRowParams( style ), children );
		return new LSGridRow( Primitive.gridRowParams.get( style ), childElems );
	}
}
