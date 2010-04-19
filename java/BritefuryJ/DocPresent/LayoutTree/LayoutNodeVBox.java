//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.VerticalLayout;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeVBox extends LayoutNodeAbstractBox
{
	public LayoutNodeVBox(DPVBox element)
	{
		super( element );
	}

	
	protected int getRefPointIndex()
	{
		return ((DPVBox)element).getRefPointIndex();
	}
	

	
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		VerticalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitonXBoxes() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		VerticalLayout.computeRequisitionY( layoutReqBox, getLeavesRefreshedRequistionYBoxes(), getRefPointIndex(), getSpacing() );
	}




	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags );
		
		refreshLeavesAllocationX( prevWidth );
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags, getRefPointIndex(), getSpacing() );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	protected DPElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( getLeaves(), localPos, filter );
	}


	
	protected AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		if ( leaves.length == 0 )
		{
			return new AABox2[] {};
		}
		else
		{
			DPElement startLeaf = leaves[rangeStart];
			DPElement endLeaf = leaves[rangeEnd-1];
			double yStart = startLeaf.getPositionInParentAllocationSpaceY();
			double yEnd = endLeaf.getPositionInParentAllocationSpaceY()  +  endLeaf.getAllocationInParentSpaceY();
			AABox2 box = new AABox2( 0.0, yStart, getAllocationX(), yEnd );
			return new AABox2[] { box };
		}
	}

	
	
	//
	// Focus navigation methods
	//
	
	public List<DPElement> horizontalNavigationList()
	{
		return getLeaves();
	}
	
	public List<DPElement> verticalNavigationList()
	{
		return getLeaves();
	}
}
