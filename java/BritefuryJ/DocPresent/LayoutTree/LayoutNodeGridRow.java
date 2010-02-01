//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeGridRow extends ArrangedSequenceLayoutNode
{
	public LayoutNodeGridRow(DPContainer element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		HorizontalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitonXBoxes(), 0.0 );
	}

	protected void updateRequisitionY()
	{
		GridLayout.computeRowRequisitionY( layoutReqBox, getLeavesRefreshedRequistionYBoxes(), getLeavesAlignmentFlags() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, 0.0 );
		
		refreshLeavesAllocationX( prevWidth );
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAlignmentFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		GridLayout.allocateRowY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAlignmentFlags );
		
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
