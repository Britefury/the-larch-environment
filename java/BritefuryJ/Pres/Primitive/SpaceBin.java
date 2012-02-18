//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPSpaceBin;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class SpaceBin extends Pres
{
	private Pres child;
	private double minWidth, minHeight;
	
	
	public SpaceBin(double minWidth, double minHeight, Object child)
	{
		this.child = coerce( child );
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPSpaceBin bin = new DPSpaceBin( Primitive.containerParams.get( style ), minWidth, minHeight );
		StyleValues childStyle = Primitive.useContainerParams.get( style );
		bin.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
		return bin;
	}
}
