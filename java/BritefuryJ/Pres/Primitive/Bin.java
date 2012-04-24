//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Bin extends Pres
{
	private Pres child;
	
	
	public Bin(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElem = null;
		if ( child != null )
		{
			StyleValues childStyle = Primitive.useContainerParams.get( style );
			childElem = child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) );
		}
		return new LSBin( Primitive.containerParams.get( style ), childElem );
	}
}
