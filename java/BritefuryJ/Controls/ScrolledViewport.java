//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSSpaceBin;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.SpaceBin;

public class ScrolledViewport extends AbstractScrolledViewport
{
	private double width, height;
	private LSSpaceBin.SizeConstraint sizeConstraint;
	
	
	public ScrolledViewport(Object child, double width, double height, LSSpaceBin.SizeConstraint sizeConstraint, boolean scrollX, boolean scrollY, PersistentState state)
	{
		super( child, scrollX, scrollY, state );
		this.width = width;
		this.height = height;
		this.sizeConstraint = sizeConstraint;
	}
	
	public ScrolledViewport(Object child, double width, double height, LSSpaceBin.SizeConstraint sizeConstraint, PersistentState state)
	{
		this( child, width, height, sizeConstraint, true, true, state );
	}
	
	public ScrolledViewport(Object child, double width, double height, boolean scrollX, boolean scrollY, PersistentState state)
	{
		this( child, width, height, LSSpaceBin.SizeConstraint.LARGER, scrollX, scrollY, state );
	}
	
	public ScrolledViewport(Object child, double width, double height, PersistentState state)
	{
		this( child, width, height, LSSpaceBin.SizeConstraint.LARGER, true, true, state );
	}

	
	@Override
	protected Pres createViewportBin(LSElement viewport)
	{
		return new SpaceBin( width, height, sizeConstraint, viewport );
	}
}
