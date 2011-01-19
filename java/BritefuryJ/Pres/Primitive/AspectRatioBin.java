//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPAspectRatioBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class AspectRatioBin extends Pres
{
	private Pres child;
	private double minWidth, aspectRatio;
	
	
	public AspectRatioBin(Object child, double minWidth, double aspectRatio)
	{
		this.child = coerce( child );
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPAspectRatioBin bin = new DPAspectRatioBin( Primitive.containerParams.get( style ), minWidth, aspectRatio );
		bin.setChild( child.present( ctx, Primitive.useContainerParams.get( style ) ).layoutWrap() );
		return bin;
	}
}
