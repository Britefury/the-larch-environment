//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSOverlay;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.StyleSheet.StyleValues;

public class Overlay extends SequentialPres
{
	public Overlay(Object children[])
	{
		super( children );
	}
	
	public Overlay(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement[] childElements = mapPresent( ctx, Primitive.useOverlayParams( style ), children );
		StyleValues childStyle = Primitive.useOverlayParams( style );
		HAlignment childHAlign = childStyle.get( Primitive.hAlign, HAlignment.class );
		VAlignment childVAlign = childStyle.get( Primitive.vAlign, VAlignment.class );
		for (int i = 0; i < childElements.length; i++)
		{
			childElements[i] = childElements[i].layoutWrap( childHAlign, childVAlign );
		}
		return new LSOverlay( Primitive.overlayParams.get( style ), childElements );
	}
}
