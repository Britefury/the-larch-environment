//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.Math.Point2;

public class LayoutNodeEmpty extends LeafLayoutNode
{
	public LayoutNodeEmpty(DPEmpty element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		layoutReqBox.clearRequisitionX();
	}

	protected void updateRequisitionY()
	{
		layoutReqBox.clearRequisitionY();
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
