//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSCaretSlot;
import BritefuryJ.LSpace.Util.TextVisual;

public class LayoutNodeCaretSlot extends EditableContentLeafLayoutNodeSharedReq
{
	public LayoutNodeCaretSlot(LSCaretSlot element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		LSCaretSlot text = (LSCaretSlot)element;
		layoutReqBox = text.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		LSCaretSlot text = (LSCaretSlot)element;
		layoutReqBox = text.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}
}
