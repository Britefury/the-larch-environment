//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
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

	
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		VerticalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitonXBoxes() );
	}

	protected void updateRequisitionY()
	{
		VerticalLayout.computeRequisitionY( layoutReqBox, getLeavesRefreshedRequistionYBoxes(), getRefPointIndex(), getSpacing() );
	}




	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		VerticalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags );
		
		refreshLeavesAllocationX( prevWidth );
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		VerticalLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, getRefPointIndex(), getSpacing() );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( Arrays.asList( leaves ), localPos, filter );
	}


	
	protected AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		DPWidget startLeaf = leaves[rangeStart];
		DPWidget endLeaf = leaves[rangeEnd-1];
		double yStart = startLeaf.getPositionInParentSpaceY();
		double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getAllocationInParentSpaceY();
		AABox2 box = new AABox2( 0.0, yStart, getAllocationX(), yEnd );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		return getLeaves();
	}
	
	public List<DPWidget> verticalNavigationList()
	{
		return getLeaves();
	}
}
