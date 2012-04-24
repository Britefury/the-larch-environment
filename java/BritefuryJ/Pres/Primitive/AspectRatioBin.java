//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSAspectRatioBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class AspectRatioBin extends Pres
{
	private Pres child;
	private double minWidth, aspectRatio;
	
	
	public AspectRatioBin(double minWidth, double aspectRatio, Object child)
	{
		this.child = coerce( child );
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = Primitive.useContainerParams.get( style );
		LSElement childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
		return new LSAspectRatioBin( Primitive.containerParams.get( style ), minWidth, aspectRatio, childElem );
	}
}
