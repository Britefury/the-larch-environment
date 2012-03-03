//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.AspectRatioBin;

public class AspectRatioScrolledViewport extends AbstractScrolledViewport
{
	private double minWidth, aspectRatio;
	
	
	public AspectRatioScrolledViewport(Object child, double minWidth, double aspectRatio, PersistentState state)
	{
		super( child, state );
		this.minWidth = minWidth;
		this.aspectRatio = aspectRatio;
	}
	

	@Override
	protected Pres createViewportBin(LSElement viewport)
	{
		return new AspectRatioBin( viewport, minWidth, aspectRatio );
	}
}
