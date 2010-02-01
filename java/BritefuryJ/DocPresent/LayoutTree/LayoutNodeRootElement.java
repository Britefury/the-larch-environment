//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;

public class LayoutNodeRootElement extends LayoutNodeBin
{
	public LayoutNodeRootElement(DPPresentationArea element)
	{
		super( element );
	}
	
	
	
	protected void handleQueueResize()
	{
		super.handleQueueResize();
		
		DPPresentationArea rootElement = (DPPresentationArea)getElement();
		rootElement.queueReallocation();
	}

	
	
	public void allocateX(LReqBoxInterface requisition, double x, double width)
	{
		layoutAllocBox.allocateX( requisition, x, width );
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, double height)
	{
		layoutAllocBox.allocateY( requisition, y, height );
	}
	
	
	
	public LAllocV getAllocV()
	{
		return layoutAllocBox.getAllocV();
	}
}
