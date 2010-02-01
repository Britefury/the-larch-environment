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
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public abstract class ArrangedSequenceLayoutNode extends ArrangedLayoutNode
{
	protected DPWidget leaves[], branches[];
	protected int branchRanges[];				// Stored as an array of pairs; each pair is of the form (start_index, end_index)
	
	
	
	
	public ArrangedSequenceLayoutNode(DPContainer element)
	{
		super( element );
	}


	//
	//
	// COLLATION METHODS
	//
	//
	
	protected void gatherCount(DPContainer branch, int counts[])
	{
		for (DPWidget e: branch.getLayoutChildren())
		{
			if ( hasLayoutForElement( e ) )
			{
				counts[0]++;
			}
			else
			{
				counts[1]++;
				gatherCount( (DPContainer)e, counts );
			}
		}
	}
	
	protected void gatherItems(DPContainer branch, int indices[])
	{
		for (DPWidget e: branch.getLayoutChildren())
		{
			if ( hasLayoutForElement( e ) )
			{
				leaves[indices[0]++] = e;
			}
			else
			{
				int branchIndex = indices[1]++;
				int start = indices[0];
				branches[branchIndex] = e;
				gatherItems( (DPContainer)e, indices );
				int end = indices[0];
				branchRanges[branchIndex*2] = start;
				branchRanges[branchIndex*2+1] = end;
			}
		}
	}
	
	
	protected void refreshSubtree()
	{
		if ( leaves == null )
		{
			int counts[] = new int [2];
			gatherCount( element, counts );
			
			leaves = new DPWidget[counts[0]];
			branches = new DPWidget[counts[1]];
			branchRanges = new int[counts[1]*2];
			
			counts[0] = 0;
			counts[1] = 0;
			gatherItems( element, counts );
		}
	}
	
	
	
	public void onChildListModified()
	{
		resetSubtree();
	}
	
	
	private void resetSubtree()
	{
		leaves = null;
		branches = null;
		branchRanges = null;
	}
	
	
	protected void onSizeRefreshed()
	{
		super.onSizeRefreshed();
		refreshSubtree();
		for (DPWidget c: branches)
		{
			LayoutNode branchLayout = c.getLayoutNode();
			if ( branchLayout != null )
			{
				branchLayout.onSizeRefreshed();
			}
		}
	}
	
	protected void onChildSizeRefreshed()
	{
		super.onChildSizeRefreshed();
	}

	
	
	
	
	public DPContentLeaf getLeftContentLeafWithinElement(DPWidget withinElement)
	{
		refreshSubtree();
		int branchIndex = indexOfBranch( withinElement );
		if ( branchIndex == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		int start = branchRanges[branchIndex*2], end = branchRanges[branchIndex*2+1];
		for (int i = start; i < end; i++)
		{
			DPContentLeaf leaf = leaves[i].getLayoutNode().getLeftContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeaf getRightContentLeafWithinElement(DPWidget withinElement)
	{
		refreshSubtree();
		int branchIndex = indexOfBranch( withinElement );
		if ( branchIndex == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		int start = branchRanges[branchIndex*2], end = branchRanges[branchIndex*2+1];
		for (int i = end - 1; i >= start; i--)
		{
			DPContentLeaf leaf = leaves[i].getLayoutNode().getRightContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeaf getContentLeafToLeftOfElement(DPWidget inElement)
	{
		refreshSubtree();
		int branchIndex = indexOfBranch( inElement );
		if ( branchIndex == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		int start = branchRanges[branchIndex*2];
		for (int i = start - 1; i >= 0; i--)
		{
			DPContentLeaf leaf = leaves[i].getLayoutNode().getRightContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeaf getContentLeafToRightOfElement(DPWidget inElement)
	{
		refreshSubtree();
		int branchIndex = indexOfBranch( inElement );
		if ( branchIndex == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		int end = branchRanges[branchIndex*2+1];
		for (int i = end; i < leaves.length; i++)
		{
			DPContentLeaf leaf = leaves[i].getLayoutNode().getLeftContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeaf getTopOrBottomContentLeafWithinElement(DPWidget withinElement, boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		refreshSubtree();
		int branchIndex = indexOfBranch( withinElement );
		if ( branchIndex == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		int start = branchRanges[branchIndex*2], end = branchRanges[branchIndex*2+1];
		if ( bBottom )
		{
			for (int i = start; i < end; i++)
			{
				DPContentLeaf leaf = leaves[i].getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
				if ( leaf != null )
				{
					return leaf;
				}
			}
		}
		else
		{
			for (int i = end - 1; i >= start; i--)
			{
				DPContentLeaf leaf = leaves[i].getLayoutNode().getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
				if ( leaf != null )
				{
					return leaf;
				}
			}
		}
		return null;
	}

	public DPWidget getLeafClosestToLocalPointWithinElement(DPWidget withinElement, Point2 localPos, WidgetFilter filter)
	{
		return null;
	}


	
	
	protected abstract AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd);


	protected int indexOfBranch(DPWidget branch)
	{
		int index = 0;
		for (DPWidget b: branches)
		{
			if ( b == branch )
			{
				return index;
			}
			index++;
		}
		return -1;
	}
	
	
	public AABox2[] computeBranchBoundsBoxes(DPContainer branch)
	{
		refreshSubtree();
		
		int index = indexOfBranch( branch );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		
		return computeCollatedBranchBoundsBoxes( branchRanges[index*2], branchRanges[index*2+1] );
	}




	public List<DPWidget> getLeaves()
	{
		refreshSubtree();
		return Arrays.asList( leaves );
	}


	LReqBoxInterface[] getLeavesRefreshedRequisitonXBoxes()
	{
		refreshSubtree();
		return getChildrenRefreshedRequistionXBoxes( Arrays.asList( leaves ) );
	}

	LReqBoxInterface[] getLeavesRefreshedRequistionYBoxes()
	{
		refreshSubtree();
		return getChildrenRefreshedRequistionYBoxes( Arrays.asList( leaves ) );
	}

	LReqBoxInterface[] getLeavesRequisitionBoxes()
	{
		refreshSubtree();
		return getChildrenRequisitionBoxes( Arrays.asList( leaves ) );
	}

	LAllocBoxInterface[] getLeavesAllocationBoxes()
	{
		refreshSubtree();
		return getChildrenAllocationBoxes( Arrays.asList( leaves ) );
	}

	int[] getLeavesAlignmentFlags()
	{
		refreshSubtree();
		return getChildrenAlignmentFlags( Arrays.asList( leaves ) );
	}

	<T extends PackingParams> T[] getLeavesPackingParams(T packingParams[])
	{
		refreshSubtree();
		return getChildrenPackingParams( Arrays.asList( leaves ), packingParams );
	}
	
	
	double[] getLeavesAllocationX()
	{
		refreshSubtree();
		return getChildrenAllocationX( Arrays.asList( leaves ) );
	}

	double[] getLeavesAllocationY()
	{
		refreshSubtree();
		return getChildrenAllocationY( Arrays.asList( leaves ) );
	}

	LAllocV[] getLeavesAllocV()
	{
		refreshSubtree();
		return getChildrenAllocV( Arrays.asList( leaves ) );
	}
	
	
	void refreshLeavesAllocationX(double prevWidth[])
	{
		refreshSubtree();
		int i = 0;
		for (DPWidget child: leaves)
		{
			child.getLayoutNode().refreshAllocationX( prevWidth[i] );
			i++;
		}
	}

	void refreshLeavesAllocationY(LAllocV prevAllocV[])
	{
		refreshSubtree();
		int i = 0;
		for (DPWidget child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocV[i] );
			i++;
		}
	}
}
