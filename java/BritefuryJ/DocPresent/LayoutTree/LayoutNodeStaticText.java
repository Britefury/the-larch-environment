//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.Util.TextVisual;

public class LayoutNodeStaticText extends StaticLayoutNode
{
	public LayoutNodeStaticText(DPStaticText element)
	{
		super( element );
		
		layoutReqBox = element.getVisual().getRequisition();
	}

	protected void updateRequisitionX()
	{
		DPStaticText staticText = (DPStaticText)element;
		layoutReqBox = staticText.getVisual().getRequisition();
	}

	protected void updateRequisitionY()
	{
		DPStaticText staticText = (DPStaticText)element;
		layoutReqBox = staticText.getVisual().getRequisition();
	}
	
	
	public void setVisual(TextVisual visual)
	{
		layoutReqBox = visual.getRequisition();
	}
}
