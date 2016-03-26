//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpan;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

public class Span extends SequentialPres
{
	public Span(Object children[])
	{
		super( children );
	}
	
	public Span(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElems = mapPresent( ctx, Primitive.useContainerParams.get( style ), children );
		return new LSSpan( Primitive.containerParams.get( style ), childElems );
	}
}
