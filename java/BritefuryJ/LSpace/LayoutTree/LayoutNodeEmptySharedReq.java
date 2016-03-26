//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
