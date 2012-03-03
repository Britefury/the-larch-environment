//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSOverlay;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.Layout.OverlayLayout;
import BritefuryJ.Math.Point2;

public class LayoutNodeOverlay extends ArrangedLayoutNode
{
	public LayoutNodeOverlay(LSOverlay element)
	{
		super( element );
	}
	
	
	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSOverlay overlay = (LSOverlay)element;
		List<LSElement> children = overlay.getChildren();
		
		OverlayLayout.computeRequisitionX( layoutReqBox, getChildrenRefreshedRequistionXBoxes( children ) );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSOverlay overlay = (LSOverlay)element;
		List<LSElement> children = overlay.getChildren();
		
		OverlayLayout.computeRequisitionY( layoutReqBox, getChildrenRefreshedRequistionYBoxes( children ), getChildrenAlignmentFlags( children ) );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSOverlay overlay = (LSOverlay)element;
		List<LSElement> children = overlay.getChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[children.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[children.size()];
		double prevWidths[] = new double[children.size()];
		int childAlignmentFlags[] = new int[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			LSElement child = children.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevWidths[i] = layoutNode.getAllocWidth();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		OverlayLayout.allocateX( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAlignmentFlags );
		
		int i = 0;
		for (LSElement child: children)
		{
			child.getLayoutNode().refreshAllocationX( prevWidths[i] );
			i++;
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSOverlay overlay = (LSOverlay)element;
		List<LSElement> children = overlay.getChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[children.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[children.size()];
		LAllocV prevAllocVs[] = new LAllocV[children.size()];
		int childAlignmentFlags[] = new int[children.size()];
		for (int i = 0; i < children.size(); i++)
		{
			LSElement child = children.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevAllocVs[i] = layoutNode.getAllocV();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		OverlayLayout.allocateY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAlignmentFlags );
		
		int i = 0;
		for (LSElement child: children)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	
	

	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointOverlay( ( (LSContainer)element ).getLayoutChildren(), localPos, filter );
	}



	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		return ( (LSContainer)element ).getLayoutChildren();
	}
}
