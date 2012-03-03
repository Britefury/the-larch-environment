//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSBlank;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.Math.Point2;

public class LayoutNodeBlank extends LeafLayoutNode
{
	public LayoutNodeBlank(LSBlank element)
	{
		super( element );
	}



	protected void updateRequisitionX()
	{
		layoutReqBox.setRequisitionX( 0.0, 0.0 );
	}

	protected void updateRequisitionY()
	{
		layoutReqBox.setRequisitionY( 0.0, 0.0 );
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
