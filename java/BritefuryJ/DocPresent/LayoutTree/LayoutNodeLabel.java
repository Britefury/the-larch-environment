//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPLabel;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class LayoutNodeLabel extends LeafLayoutNodeSharedReq
{
	public LayoutNodeLabel(DPLabel element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		DPLabel label = (DPLabel)element;
		layoutReqBox = label.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		DPLabel label = (DPLabel)element;
		layoutReqBox = label.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}

	
	@Override
	public DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return null;
	}
}
