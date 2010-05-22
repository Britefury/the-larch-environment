//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.Point2;

public class LayoutNodeViewport extends ArrangedLayoutNode
{
	public LayoutNodeViewport(DPViewport element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPViewport viewport = (DPViewport)element;
		DPElement child = viewport.getChild();
		if ( child != null )
		{
			child.getLayoutNode().refreshRequisitionX();
		}
		layoutReqBox.setRequisitionX( 0.0, 0.0 );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPViewport viewport = (DPViewport)element;
		DPElement child = viewport.getChild();
		if ( child != null )
		{
			child.getLayoutNode().refreshRequisitionY();
		}
		layoutReqBox.setRequisitionY( 0.0, 0.0 );
	}
	
	
	
	protected void updateAllocationX()
	{
		DPViewport viewport = (DPViewport)element;
		DPElement child = viewport.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LReqBoxInterface childReq = childLayout.getRequisitionBox();
			double prevWidth = childLayout.getAllocationBox().getAllocationX();
			double allocX = getAllocationBox().getAllocationX();
			double childAllocWidth = Math.max( childReq.getReqMinWidth(), allocX );
			LAllocHelper.allocateX( childLayout.getAllocationBox(), childReq, 0.0, childAllocWidth );
			childLayout.refreshAllocationX( prevWidth );
		}
	}

	protected void updateAllocationY()
	{
		DPViewport viewport = (DPViewport)element;
		DPElement child = viewport.getChild();
		if ( child != null )
		{
			LayoutNode childLayout = child.getLayoutNode();
			LReqBoxInterface childReq = childLayout.getRequisitionBox();
			LAllocV prevAllocV = childLayout.getAllocationBox().getAllocV();
			LAllocHelper.allocateY( childLayout.getAllocationBox(), childReq, 0.0, Math.max( childReq.getReqHeight(), getAllocationY() ) );
			childLayout.refreshAllocationY( prevAllocV );
		}
	}
	
	
	protected void onAllocationRefreshed()
	{
		super.onAllocationRefreshed();
		DPViewport viewport = (DPViewport)element;
		viewport.onAllocationRefreshed();
	}
	
	
	
	protected DPElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		DPViewport viewport = (DPViewport)element;
		DPElement child = viewport.getChild();
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
	
	public List<DPElement> horizontalNavigationList()
	{
		DPViewport viewport = (DPViewport)element;
		List<DPElement> children = viewport.getLayoutChildren();
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
