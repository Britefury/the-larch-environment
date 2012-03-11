//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSBlank;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public abstract class LayoutNodeEmptySharedReq extends LeafLayoutNodeSharedReq
{
	public LayoutNodeEmptySharedReq(LSBlank element, LReqBox reqBox)
	{
		super( element, reqBox );
	}



	protected void updateRequisitionX()
	{
	}

	protected void updateRequisitionY()
	{
	}
	



	public LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		if ( filter == null  ||  filter.testElement( element ) )
		{
			return element;
		}
		else
		{
			return null;
		}
	}
}