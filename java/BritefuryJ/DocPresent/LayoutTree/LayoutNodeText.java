//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Util.TextVisual;

public class LayoutNodeText extends EditableContentLeafLayoutNodeSharedReq
{
	public LayoutNodeText(DPText element)
	{
		super( element, element.getVisual().getRequisition() );
	}

	protected void updateRequisitionX()
	{
		DPText text = (DPText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		DPText text = (DPText)element;
		layoutReqBox = text.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}
}
