//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
