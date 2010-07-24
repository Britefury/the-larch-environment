//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPAspectRatioBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;

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
	public DPElement present(PresentationContext ctx)
	{
		DPAspectRatioBin bin = new DPAspectRatioBin( Primitive.containerParams.get( ctx.getStyle() ), minWidth, aspectRatio );
		bin.setChild( child.present( ctx.withStyle( Primitive.useContainerParams( ctx.getStyle() ) ) ).layoutWrap() );
		return bin;
	}
}
