//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;

public class LayoutNodeRootElement extends LayoutNodeBin
{
	public LayoutNodeRootElement(PresentationComponent.RootElement element)
	{
		super( element );
	}
	
	
	
	protected void handleQueueResize()
	{
		super.handleQueueResize();
		
		PresentationComponent.RootElement rootElement = (PresentationComponent.RootElement)getElement();
		rootElement.queueReallocation();
	}

	
	
	public void allocateX(LReqBoxInterface requisition, double x, double width)
	{
		LAllocHelper.allocateX( getAllocationBox(), requisition, x, width );
	}
	
	public void allocateY(LReqBoxInterface requisition, double y, double height)
	{
		LAllocHelper.allocateY( getAllocationBox(), requisition, y, height );
	}
}
