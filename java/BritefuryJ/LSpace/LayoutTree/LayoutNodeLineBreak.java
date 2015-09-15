//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSLineBreak;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;

public class LayoutNodeLineBreak extends LeafLayoutNodeSharedReq
{
    private static LReqBox lineBreakReq = new LReqBox(0.0, 0.0, 0.0, 0.0);

	public LayoutNodeLineBreak(LSLineBreak element)
	{
		super( element, lineBreakReq );
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
