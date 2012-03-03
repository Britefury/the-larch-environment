//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSViewport;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;

public class LayoutNodeViewport extends ArrangedLayoutNode
{
	public LayoutNodeViewport(LSViewport element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSViewport viewport = (LSViewport)element;
		LSElement child = viewport.getChild();
		if ( child != null )
		{
			child.getLayoutNode().refreshRequisitionX();
		}
		layoutReqBox.setRequisitionX( 0.0, 0.0 );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSViewport viewport = (LSViewport)element;
		LSElement child = viewport.getChild();
		if ( child != null )
		{
			child.getLayoutNode().refreshRequisitionY();
		}
		layoutReqBox.setRequisitionY( 0.0, 0.0 );
	}
	
	
	
	protected void updateAllocationX()
	{
		LSViewport viewport = (LSViewport)element;
		LSElement child = viewport.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LReqBoxInterface childReq = childLayout.getRequisitionBox();
			double prevWidth = childLayout.getAllocationBox().getAllocWidth();
			double allocX = getAllocationBox().getAllocWidth();
			LAllocHelper.allocateX( childLayout.getAllocationBox(), childReq, 0.0, allocX );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		LSViewport viewport = (LSViewport)element;
		LSElement child = viewport.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LReqBoxInterface childReq = childLayout.getRequisitionBox();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			LAllocHelper.allocateY( childLayout.getAllocationBox(), childReq, 0.0, Math.max( childReq.getReqHeight(), getAllocHeight() ) );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	@Override
	protected void onAllocationRefreshed()
	{
		super.onAllocationRefreshed();
		LSViewport viewport = (LSViewport)element;
		viewport.onAllocationRefreshed();
	}
	
	
	
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		LSViewport viewport = (LSViewport)element;
		LSElement child = viewport.getChild();
		if ( child == null )
		{
			return null;
		}
		else
		{
			return getLeafClosestToLocalPointFromChild( child, localPos, filter );
		}
	}

	
	
	
	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		LSViewport viewport = (LSViewport)element;
		List<LSElement> children = viewport.getLayoutChildren();
		if ( children.size() > 0 )
		{
			return children;
		}
		else
		{
			return null;
		}
	}
}
