//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.List;

import BritefuryJ.LSpace.InsertionPoint;
import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.GridLayout;
import BritefuryJ.LSpace.Layout.HorizontalLayout;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeGridRow extends ArrangedSequenceLayoutNode
{
	public LayoutNodeGridRow(LSContainer element)
	{
		super( element );
	}


	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		HorizontalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitionXBoxes(), 0.0 );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		GridLayout.computeRowRequisitionY( layoutReqBox, getLeavesRefreshedRequisitionYBoxes(), getLeavesAlignmentFlags() );
	}
	

	

	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags, 0.0 );
		
		refreshLeavesAllocationX( prevWidth );
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAlignmentFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		GridLayout.allocateRowY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAlignmentFlags );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	@Override
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( getLeaves(), localPos, filter );
	}

	@Override
	public LSElement getChildLeafClosestToLocalPointWithinBranch(LSContainer withinBranch, Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( getLeavesWithinBranch( withinBranch ), localPos, filter );
	}


	@Override
	public InsertionPoint getInsertionPointClosestToLocalPoint(LSContainer withinBranch, Point2 localPos)
	{
		return getInsertionPointClosestToLocalPointHorizontal( withinBranch, localPos );
	}



	@Override
	protected AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		if ( leaves.length == 0 )
		{
			return new AABox2[] {};
		}
		else
		{
			if ( rangeStart == rangeEnd )
			{
				return new AABox2[0];
			}
			else
			{
				LSElement startLeaf = leaves[rangeStart];
				LSElement endLeaf = leaves[rangeEnd-1];
				double xStart = startLeaf.getPositionInParentSpaceX();
				double xEnd = endLeaf.getPositionInParentSpaceX()  +  endLeaf.getActualWidthInParentSpace();
				AABox2 box = new AABox2( xStart, 0.0, xEnd, getAllocHeight() );
				return new AABox2[] { box };
			}
		}
	}

	
	
	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		return getLeaves();
	}
}
