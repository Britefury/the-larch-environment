//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
