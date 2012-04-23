//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class SpaceBin extends Pres
{
	private Pres child;
	private double width, height;
	private LSSpaceBin.SizeConstraint sizeConstraint;
	
	
	public SpaceBin(double width, double height, LSSpaceBin.SizeConstraint sizeConstraint, Object child)
	{
		this.child = coerce( child );
		this.width = width;
		this.height = height;
		this.sizeConstraint = sizeConstraint;
	}
	
	public SpaceBin(double width, double height, Object child)
	{
		this( width, height, LSSpaceBin.SizeConstraint.LARGER, child );
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSSpaceBin bin = new LSSpaceBin( Primitive.containerParams.get( style ), width, height, sizeConstraint );
		StyleValues childStyle = Primitive.useContainerParams.get( style );
		bin.setChild( child.present( ctx, childStyle ).layoutWrap( childStyle.get( Primitive.hAlign, HAlignment.class ), childStyle.get( Primitive.vAlign, VAlignment.class ) ) );
		return bin;
	}
}
