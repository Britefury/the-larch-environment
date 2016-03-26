//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
