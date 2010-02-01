//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPParagraphMarker;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.Math.Point2;

public class LayoutNodeParagraphMarker extends LeafLayoutNode
{
	public LayoutNodeParagraphMarker(DPParagraphMarker element)
	{
		super( element );
	}



	protected void updateRequisitionX()
	{
		DPParagraphMarker marker = (DPParagraphMarker)element;
		layoutReqBox.clearRequisitionX();
		marker.initMarkerRequisition( layoutReqBox );
	}

	protected void updateRequisitionY()
	{
		DPParagraphMarker marker = (DPParagraphMarker)element;
		layoutReqBox.clearRequisitionY();
		marker.initMarkerRequisition( layoutReqBox );
	}
	



	public DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
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
