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

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.Layout.VerticalLayout;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeColumn extends LayoutNodeAbstractBox
{
	@SuppressWarnings("rawtypes")
	private static final Comparator visibilityStartComparator = new Comparator()
	{
		@Override
		public int compare(Object arg0, Object arg1)
		{
			LSElement child = (LSElement)arg0;
			Double y = (Double)arg1;
			
			double childBottomEdge = child.getAABoxInParentSpace().getUpperY();
			
			return ((Double)childBottomEdge).compareTo( y );
		}
	};
	
	@SuppressWarnings("rawtypes")
	private static final Comparator visibilityEndComparator = new Comparator()
	{
		@Override
		public int compare(Object arg0, Object arg1)
		{
			LSElement child = (LSElement)arg0;
			Double y = (Double)arg1;
			
			double childTopEdge = child.getAABoxInParentSpace().getLowerY();
			
			return ((Double)childTopEdge).compareTo( y );
		}
	};

	
	
	
	public LayoutNodeColumn(LSColumn element)
	{
		super( element );
	}

	
	protected int getRefPointIndex()
	{
		return ((LSColumn)element).getRefPointIndex();
	}
	
	protected boolean hasRefPointIndex()
	{
		return ((LSColumn)element).hasRefPointIndex();
	}
	
	
	protected int clampRefPointIndex(int refPointIndex)
	{
		int numLeaves = getNumLeaves();
		refPointIndex = Math.min( refPointIndex, numLeaves - 1 );
		if ( refPointIndex < 0 )
		{
			refPointIndex = numLeaves + refPointIndex;
			refPointIndex = Math.max( refPointIndex, 0 );
		}
		return refPointIndex;
	}
	

	
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		VerticalLayout.computeRequisitionX( layoutReqBox, getLeavesRefreshedRequisitionXBoxes() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		
		int refPointIndex = getRefPointIndex();
		boolean bHasRefPointIndex = hasRefPointIndex();
		if ( bHasRefPointIndex )
		{
			refPointIndex = clampRefPointIndex( refPointIndex );
		}
		else
		{
			if ( getNumLeaves() == 1 )
			{
				refPointIndex = 0;
			}
			else
			{
				refPointIndex = -1;
			}
		}
		
		VerticalLayout.computeRequisitionY( layoutReqBox, getLeavesRefreshedRequisitionYBoxes(), refPointIndex, getSpacing() );
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
		
		int refPointIndex = getRefPointIndex();
		boolean bHasRefPointIndex = hasRefPointIndex();
		if ( bHasRefPointIndex )
		{
			refPointIndex = clampRefPointIndex( refPointIndex );
		}
		else
		{
			refPointIndex = -1;
		}

		VerticalLayout.allocateY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags, refPointIndex, getSpacing() );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	@Override
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( getLeaves(), localPos, filter );
	}

	@Override
	public LSElement getChildLeafClosestToLocalPointWithinBranch(LSContainer withinBranch, Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( getLeavesWithinBranch( withinBranch ), localPos, filter );
	}


	@Override
	public InsertionPoint getInsertionPointClosestToLocalPoint(LSContainer withinBranch, Point2 localPos)
	{
		return getInsertionPointClosestToLocalPointVertical( withinBranch, localPos );
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
				double yStart = startLeaf.getPositionInParentSpaceY();
				double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getActualHeightInParentSpace();
				AABox2 box = new AABox2( 0.0, yStart, getActualWidth(), yEnd );
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
	
	public List<LSElement> verticalNavigationList()
	{
		return getLeaves();
	}



	//
	// Visibility
	//
	
	protected int[] getVisibilityCullingRange(AABox2 localBox)
	{
		List<LSElement> elements = getLeaves();
		
		// Need to find the start point
		@SuppressWarnings("unchecked")
		int startPoint = Collections.binarySearch( elements, (Double)localBox.getLowerY(), visibilityStartComparator );
		if ( startPoint < 0 )
		{
			startPoint = -( startPoint + 1 );
		}
		@SuppressWarnings("unchecked")
		int endPoint = Collections.binarySearch( elements, (Double)localBox.getUpperY(), visibilityEndComparator );
		if ( endPoint < 0 )
		{
			endPoint = -( endPoint + 1 );
		}
		
		return new int[] { startPoint, endPoint };
	}
}
