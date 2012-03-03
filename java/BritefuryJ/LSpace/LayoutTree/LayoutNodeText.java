//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.Util.TextVisual;

public class LayoutNodeText extends EditableContentLeafLayoutNodeSharedReq
{
	public LayoutNodeText(LSText element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		LSText text = (LSText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		LSText text = (LSText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}
}
