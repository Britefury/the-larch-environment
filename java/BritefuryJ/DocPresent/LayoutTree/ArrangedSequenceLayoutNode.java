//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public abstract class ArrangedSequenceLayoutNode extends ArrangedLayoutNode
{
	protected DPElement leaves[], branches[];
	protected int branchRanges[];				// Stored as an array of pairs; each pair is of the form (start_index, end_index)
	protected IdentityHashMap<DPContainer, AABox2[]> branchBoundsCache;
	
	
	
	
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
		for (DPElement e: branch.getLayoutChildren())
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
		for (DPElement e: branch.getLayoutChildren())
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
			
			leaves = new DPElement[counts[0]];
			branches = new DPElement[counts[1]];
			branchRanges = new int[counts[1]*2];
			
			counts[0] = 0;
			counts[1] = 0;
			gatherItems( element, counts );
			branchBoundsCache = null;
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
		branchBoundsCache = null;
	}
	
	
	protected void onAllocationRefreshed()
	{
		super.onAllocationRefreshed();
		refreshSubtree();
		for (DPElement c: branches)
		{
			LayoutNode branchLayout = c.getLayoutNode();
			if ( branchLayout != null )
			{
				branchLayout.onAllocationRefreshed();
			}
		}
	}
	
	protected void onChildSizeRefreshed()
	{
		super.onChildSizeRefreshed();
		branchBoundsCache = null;
	}

	
	
	
	
	public DPContentLeaf getLeftContentLeafWithinElement(DPElement withinElement)
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
	
	public DPContentLeaf getRightContentLeafWithinElement(DPElement withinElement)
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
	
	public DPContentLeafEditable getLeftEditableContentLeafWithinElement(DPElement withinElement)
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
			DPContentLeafEditable leaf = leaves[i].getLayoutNode().getLeftEditableContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeafEditable getRightEditableContentLeafWithinElement(DPElement withinElement)
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
			DPContentLeafEditable leaf = leaves[i].getLayoutNode().getRightEditableContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public DPContentLeaf getContentLeafToLeftOfElement(DPElement inElement)
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
	
	public DPContentLeaf getContentLeafToRightOfElement(DPElement inElement)
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
	
	public DPContentLeafEditable getTopOrBottomEditableContentLeafWithinElement(DPElement withinElement, boolean bBottom, Point2 cursorPosInRootSpace)
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
				DPContentLeafEditable leaf = leaves[i].getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
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
				DPContentLeafEditable leaf = leaves[i].getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
				if ( leaf != null )
				{
					return leaf;
				}
			}
		}
		return null;
	}

	public DPElement getLeafClosestToLocalPointWithinElement(DPElement withinElement, Point2 localPos, ElementFilter filter)
	{
		return null;
	}


	
	
	protected abstract AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd);


	protected int indexOfBranch(DPElement branch)
	{
		int index = 0;
		for (DPElement b: branches)
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
		
		AABox2 bounds[];
		if ( branchBoundsCache == null )
		{
			branchBoundsCache = new IdentityHashMap<DPContainer, AABox2[]>();
			bounds = null;
		}
		else
		{
			bounds = branchBoundsCache.get( branch );
		}
		
		if ( bounds == null )
		{
			int index = indexOfBranch( branch );
			if ( index == -1 )
			{
				throw new RuntimeException( "Could not find branch" );
			}
			
			bounds = computeCollatedBranchBoundsBoxes( branchRanges[index*2], branchRanges[index*2+1] );
			branchBoundsCache.put( branch, bounds );
		}
		
		return bounds;
	}




	public List<DPElement> getLeaves()
	{
		refreshSubtree();
		return Arrays.asList( leaves );
	}
	
	public int getNumLeaves()
	{
		refreshSubtree();
		return leaves.length;
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
		for (DPElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationX( prevWidth[i] );
			i++;
		}
	}

	void refreshLeavesAllocationY(LAllocV prevAllocV[])
	{
		refreshSubtree();
		int i = 0;
		for (DPElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocV[i] );
			i++;
		}
	}
}
