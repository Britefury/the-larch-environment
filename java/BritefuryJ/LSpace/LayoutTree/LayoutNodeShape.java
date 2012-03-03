//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSShape;

public class LayoutNodeShape extends ContentLeafLayoutNode
{
	public LayoutNodeShape(LSShape element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		LSShape shape = (LSShape)element;
		
		double w = shape.getShapeBounds().getUpperX();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		LSShape shape = (LSShape)element;
		
		layoutReqBox.setRequisitionY( shape.getShapeBounds().getUpperY(), 0.0, 0.0 );
	}
}
