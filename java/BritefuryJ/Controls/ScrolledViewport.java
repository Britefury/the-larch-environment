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
import BritefuryJ.Pres.Primitive.SpaceBin;

public class ScrolledViewport extends AbstractScrolledViewport
{
	private double minWidth, minHeight;
	
	
	public ScrolledViewport(Object child, double minWidth, double minHeight, boolean scrollX, boolean scrollY, PersistentState state)
	{
		super( child, scrollX, scrollY, state );
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	
	public ScrolledViewport(Object child, double minWidth, double minHeight, PersistentState state)
	{
		this( child, minWidth, minHeight, true, true, state );
	}
	

	@Override
	protected Pres createViewportBin(LSElement viewport)
	{
		return new SpaceBin( minWidth, minHeight, false, viewport );
	}
}
