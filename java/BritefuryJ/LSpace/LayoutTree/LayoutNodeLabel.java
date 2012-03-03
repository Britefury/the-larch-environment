//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSLabel;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class LayoutNodeLabel extends LeafLayoutNodeSharedReq
{
	public LayoutNodeLabel(LSLabel element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		LSLabel label = (LSLabel)element;
		layoutReqBox = label.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		LSLabel label = (LSLabel)element;
		layoutReqBox = label.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}

	
	@Override
	public LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return null;
	}
}
