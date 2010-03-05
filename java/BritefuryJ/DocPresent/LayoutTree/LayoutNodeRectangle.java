//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPRectangle;

public class LayoutNodeRectangle extends ContentLeafLayoutNode
{
	public LayoutNodeRectangle(DPRectangle element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		DPRectangle rectangle = (DPRectangle)element;
		
		double w = rectangle.getMinWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		DPRectangle rectangle = (DPRectangle)element;
		
		layoutReqBox.setRequisitionY( rectangle.getMinHeight(), 0.0 );
	}
}
