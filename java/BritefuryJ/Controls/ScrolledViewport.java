//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.SpaceBin;
import BritefuryJ.DocPresent.PersistentState.PersistentState;

public class ScrolledViewport extends AbstractScrolledViewport
{
	private double minWidth, minHeight;
	
	
	public ScrolledViewport(Object child, double minWidth, double minHeight, PersistentState state)
	{
		super( child, state );
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	

	@Override
	protected Pres createViewportBin(DPElement viewport)
	{
		return new SpaceBin( viewport, minWidth, minHeight );
	}
}
