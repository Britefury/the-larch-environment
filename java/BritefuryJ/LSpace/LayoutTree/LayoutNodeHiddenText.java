//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSHiddenText;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public class LayoutNodeHiddenText extends ContentLeafLayoutNodeSharedReq
{
	protected static LReqBox hiddenTextReqBox = new LReqBox();
	
	public LayoutNodeHiddenText(LSHiddenText element)
	{
		super( element, hiddenTextReqBox );
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
