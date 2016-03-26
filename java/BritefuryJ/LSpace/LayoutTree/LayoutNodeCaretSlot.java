//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
