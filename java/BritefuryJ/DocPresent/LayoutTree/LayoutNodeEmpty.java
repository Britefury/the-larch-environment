//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.Math.Point2;

public abstract class LayoutNodeEmpty extends LeafLayoutNode
{
	public LayoutNodeEmpty(DPEmpty element)
	{
		super( element );
	}



	protected void updateRequisitionX()
	{
	}

	protected void updateRequisitionY()
	{
	}
	



	public DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
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
