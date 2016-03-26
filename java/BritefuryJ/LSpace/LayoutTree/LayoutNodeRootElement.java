//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;

public class LayoutNodeRootElement extends LayoutNodeBin
{
	public LayoutNodeRootElement(LSRootElement element)
	{
		super( element );
	}
	
	
	
	protected void handleQueueResize()
	{
		super.handleQueueResize();
		
		LSRootElement rootElement = (LSRootElement)element;
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
