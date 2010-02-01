//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeHBox extends LayoutNodeAbstractBox
{
	public LayoutNodeHBox(DPHBox element)
	{
		super( element );
	}

	
	
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		HorizontalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitonXBoxes(), getSpacing() );
	}

	protected void updateRequisitionY()
	{
		HorizontalLayout.computeRequisitionY( layoutReqBox, getLeavesRefreshedRequistionYBoxes(), getLeavesAlignmentFlags() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, getSpacing() );
		
		refreshLeavesAllocationX( prevWidth );
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		HorizontalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( Arrays.asList( leaves ), localPos, filter );
	}



	protected AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		DPWidget startLeaf = leaves[rangeStart];
		DPWidget endLeaf = leaves[rangeEnd-1];
		double xStart = startLeaf.getPositionInParentSpaceX();
		double xEnd = endLeaf.getPositionInParentSpaceX()  +  endLeaf.getAllocationInParentSpaceX();
		AABox2 box = new AABox2( xStart, 0.0, xEnd, getAllocationY() );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		return getLeaves();
	}
}
