//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPBox;

public class LayoutNodeBox extends ContentLeafLayoutNode
{
	public LayoutNodeBox(DPBox element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		DPBox rectangle = (DPBox)element;
		
		double w = rectangle.getMinWidth();
		layoutReqBox.setRequisitionX( w, w );
	}

	protected void updateRequisitionY()
	{
		DPBox rectangle = (DPBox)element;
		
		layoutReqBox.setRequisitionY( rectangle.getMinHeight(), 0.0 );
	}
}
