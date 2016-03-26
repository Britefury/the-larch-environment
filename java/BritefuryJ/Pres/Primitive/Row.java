//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRow;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Row extends AbstractBox
{
	public Row(Object children[])
	{
		super( children );
	}
	
	public Row(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElems = mapPresent( ctx, Primitive.useRowParams( style ), children );
		return new LSRow( Primitive.rowParams.get( style ), childElems );
	}
}
