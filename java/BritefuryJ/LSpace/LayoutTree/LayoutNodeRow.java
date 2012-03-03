//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRow;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.HorizontalLayout;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeRow extends LayoutNodeAbstractBox
{
	@SuppressWarnings("rawtypes")
	private static final Comparator visiblityStartComparator = new Comparator()
	{
		@Override
		public int compare(Object arg0, Object arg1)
		{
			LSElement child = (LSElement)arg0;
			Double x = (Double)arg1;
			
			double childRightEdge = child.getAABoxInParentSpace().getUpperX();
			
			return ((Double)childRightEdge).compareTo( x );
		}
	};
	
	@SuppressWarnings("rawtypes")
	private static final Comparator visibilityEndComparator = new Comparator()
	{
		@Override
		public int compare(Object arg0, Object arg1)
		{
			LSElement child = (LSElement)arg0;
			Double x = (Double)arg1;
			
			double childLeftEdge = child.getAABoxInParentSpace().getLowerX();
			
			return ((Double)childLeftEdge).compareTo( x );
		}
	};

	
	
	
	public LayoutNodeRow(LSRow element)
	{
		super( element );
	}

	
	
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		HorizontalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitionXBoxes(), getSpacing() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		HorizontalLayout.computeRequisitionY( layoutReqBox, getLeavesRefreshedRequisitionYBoxes(), getLeavesAlignmentFlags() );
	}
	

	

	@Override
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		double prevWidth[] = getLeavesAllocationX();
		
		HorizontalLayout.allocateX( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags, getSpacing() );
		
		refreshLeavesAllocationX( prevWidth );
	}
	
	
	
	@Override
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		LAllocV prevAllocV[] = getLeavesAllocV();
		
		HorizontalLayout.allocateY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointHorizontal( getLeaves(), localPos, filter );
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




	//
	//
	// Visibility
	//
	//
	
	protected int[] getVisibilityCullingRange(AABox2 localBox)
	{
		List<LSElement> elements = getLeaves();
		
		@SuppressWarnings("unchecked")
		int startPoint = Collections.binarySearch( elements, localBox.getLowerX(), visiblityStartComparator );
		if ( startPoint < 0 )
		{
			startPoint = -( startPoint + 1 );
		}
		@SuppressWarnings("unchecked")
		int endPoint = Collections.binarySearch( elements, localBox.getUpperX(), visibilityEndComparator );
		if ( endPoint < 0 )
		{
			endPoint = -( endPoint + 1 );
		}
		
		return new int[] { startPoint, endPoint };
	}
}
