//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public abstract class ArrangedSequenceLayoutNode extends ArrangedLayoutNode
{
	private class VisibilityCulledBranchIterator implements Iterator<LSContainer>
	{
		private int rangeStart, rangeEnd;
		private int nextBranchIndex;
		
		public VisibilityCulledBranchIterator(int rangeStart, int rangeEnd)
		{
			this.rangeStart = rangeStart;
			this.rangeEnd = rangeEnd;
			nextBranchIndex = findNext( 0 );
		}
		
		
		@Override
		public boolean hasNext()
		{
			return nextBranchIndex != -1;
		}

		@Override
		public LSContainer next()
		{
			LSContainer branch = branches[nextBranchIndex];
			nextBranchIndex = findNext( nextBranchIndex + 1 );
			return branch;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		
		private int findNext(int start)
		{
			for (int i = start; i < branches.length; i++)
			{
				if ( rangeEnd > branchRanges[i*2]  &&  rangeStart < branchRanges[i*2+1] )
				{
					return i;
				}
			}
			return -1;
		}
	}
	
	
	
	protected LSElement leaves[];
	protected LSContainer branches[];
	protected int branchRanges[];				// Stored as an array of pairs; each pair is of the form (start_index, end_index)
	protected IdentityHashMap<LSContainer, AABox2[]> branchBoundsCache;
	
	
	
	
	public ArrangedSequenceLayoutNode(LSContainer element)
	{
		super( element );
	}


	//
	//
	// COLLATION METHODS
	//
	//
	
	protected void gatherCount(LSContainer branch, int counts[])
	{
		for (LSElement e: branch.getLayoutChildren())
		{
			if ( hasLayoutForElement( e ) )
			{
				counts[0]++;
			}
			else
			{
				counts[1]++;
				gatherCount( (LSContainer)e, counts );
			}
		}
	}
	
	protected void gatherItems(LSContainer branch, int indices[])
	{
		for (LSElement e: branch.getLayoutChildren())
		{
			if ( hasLayoutForElement( e ) )
			{
				leaves[indices[0]++] = e;
			}
			else
			{
				int branchIndex = indices[1]++;
				int start = indices[0];
				branches[branchIndex] = (LSContainer)e;
				gatherItems( (LSContainer)e, indices );
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
			gatherCount( (LSContainer)element, counts );
			
			leaves = new LSElement[counts[0]];
			branches = new LSContainer[counts[1]];
			branchRanges = new int[counts[1]*2];
			
			counts[0] = 0;
			counts[1] = 0;
			gatherItems( (LSContainer)element, counts );
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
	
	
	@Override
	protected void onAllocationRefreshed()
	{
		super.onAllocationRefreshed();
		refreshSubtree();
		for (LSElement c: branches)
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

	
	
	
	
	public LSContentLeaf getLeftContentLeafWithinLayoutlessElement(LSElement withinElement)
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
			LSContentLeaf leaf = leaves[i].getLayoutNode().getLeftContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeaf getRightContentLeafWithinLayoutlessElement(LSElement withinElement)
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
			LSContentLeaf leaf = leaves[i].getLayoutNode().getRightContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeafEditable getLeftEditableContentLeafWithinElement(LSElement withinElement)
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
			LSContentLeafEditable leaf = leaves[i].getLayoutNode().getLeftEditableContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeafEditable getRightEditableContentLeafWithinLayoutlessElement(LSElement withinElement)
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
			LSContentLeafEditable leaf = leaves[i].getLayoutNode().getRightEditableContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeaf getContentLeafToLeftOfLayoutlessElement(LSElement inElement)
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
			LSContentLeaf leaf = leaves[i].getLayoutNode().getRightContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeaf getContentLeafToRightOfLayoutlessElement(LSElement inElement)
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
			LSContentLeaf leaf = leaves[i].getLayoutNode().getLeftContentLeaf();
			if ( leaf != null )
			{
				return leaf;
			}
		}
		return null;
	}
	
	public LSContentLeafEditable getTopOrBottomEditableContentLeafWithinLayoutlessElement(LSElement withinElement, boolean bBottom, Point2 cursorPosInRootSpace)
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
				LSContentLeafEditable leaf = leaves[i].getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
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
				LSContentLeafEditable leaf = leaves[i].getLayoutNode().getTopOrBottomEditableContentLeaf( bBottom, cursorPosInRootSpace );
				if ( leaf != null )
				{
					return leaf;
				}
			}
		}
		return null;
	}


	public abstract LSElement getChildLeafClosestToLocalPointWithinBranch(LSContainer withinBranch, Point2 localPos, ElementFilter filter);



	public abstract InsertionPoint getInsertionPointClosestToLocalPoint(LSContainer withinBranch, Point2 localPos);

	public InsertionPoint getInsertionPointClosestToLocalPointHorizontal(LSContainer branch, Point2 localPos)
	{
		AABox2 branchBox = branch.getLocalAABox();

		LSElement leaf = branch.getLeafClosestToLocalPoint( localPos, null );
		if ( leaf != null )
		{
			// Walk back up until we have an element that is a direct child of @withinBranch
			while ( leaf.getParent() != branch )
			{
				leaf = leaf.getParent();
				if ( leaf == null )
				{
					throw new RuntimeException( "Could not trace back to branch" );
				}
			}

			LSElement child = leaf;
			double leftX = getFirstAABoxOfChild( child ).getLowerX();
			double rightX = getLastAABoxOfChild( child ).getUpperX();

			boolean left = Math.abs( localPos.x - leftX ) < Math.abs( localPos.x - rightX );

			double x = left  ?  leftX  :  rightX;

			int index = branch.getChildren().indexOf( child );
			index = left  ?  index  :  index + 1;
			return new InsertionPoint( index, new Point2( x, branchBox.getLowerY() ), new Point2( x, branchBox.getUpperY() ) );
		}

		return new InsertionPoint( 0, branchBox.getLeftEdge() );
	}


	public InsertionPoint getInsertionPointClosestToLocalPointVertical(LSContainer branch, Point2 localPos)
	{
		AABox2 branchBox = branch.getLocalAABox();

		LSElement leaf = branch.getLeafClosestToLocalPoint( localPos, null );
		if ( leaf != null )
		{
			// Walk back up until we have an element that is a direct child of @withinBranch
			while ( leaf.getParent() != branch )
			{
				leaf = leaf.getParent();
				if ( leaf == null )
				{
					throw new RuntimeException( "Could not trace back to branch" );
				}
			}

			LSElement child = leaf;
			double topY = getFirstAABoxOfChild( child ).getLowerY();
			double bottomY = getLastAABoxOfChild( child ).getUpperY();

			boolean top = Math.abs( localPos.y - topY ) < Math.abs( localPos.y - bottomY );

			double y = top  ?  topY  :  bottomY;

			int index = branch.getChildren().indexOf( child );
			index = top  ?  index  :  index + 1;
			return new InsertionPoint( index, new Point2( branchBox.getLowerX(), y ), new Point2( branchBox.getUpperX(), y ) );
		}

		return new InsertionPoint( 0, branchBox.getLeftEdge() );
	}



	protected AABox2 getFirstAABoxOfChild(LSElement child)
	{
		AABox2 box = null;
		if ( child instanceof LSContainer  &&  child.getLayoutNode() == null )
		{
			// Collated branch
			AABox2 bounds[] = computeBranchBoundsBoxes( (LSContainer)child );
			if ( bounds.length > 0 )
			{
				return bounds[0];
			}
		}

		return child.getLocalAABox();
	}

	protected AABox2 getLastAABoxOfChild(LSElement child)
	{
		AABox2 box = null;
		if ( child instanceof LSContainer  &&  child.getLayoutNode() == null )
		{
			// Collated branch
			AABox2 bounds[] = computeBranchBoundsBoxes( (LSContainer)child );
			if ( bounds.length > 0 )
			{
				return bounds[bounds.length-1];
			}
		}

		return child.getLocalAABox();
	}



	
	protected abstract AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd);


	protected int indexOfBranch(LSElement branch)
	{
		int index = 0;
		for (LSElement b: branches)
		{
			if ( b == branch )
			{
				return index;
			}
			index++;
		}
		return -1;
	}

	protected int[] getBranchRange(LSElement branch)
	{
		int index = indexOfBranch( branch );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		return new int[] { branchRanges[index*2], branchRanges[index*2+1] };
	}
	
	
	public AABox2[] computeBranchBoundsBoxes(LSContainer branch)
	{
		refreshSubtree();
		
		AABox2 bounds[];
		if ( branchBoundsCache == null )
		{
			branchBoundsCache = new IdentityHashMap<LSContainer, AABox2[]>();
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


	protected List<LSElement> getLeavesWithinBranch(LSContainer branch)
	{
		refreshSubtree();

		int index = indexOfBranch( branch );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find branch" );
		}
		return getLeaves().subList( branchRanges[index*2], branchRanges[index*2+1] );
	}




	public List<LSContainer> getBranches()
	{
		refreshSubtree();
		return Arrays.asList( branches );
	}
	

	public List<LSElement> getLeaves()
	{
		refreshSubtree();
		return Arrays.asList( leaves );
	}
	
	public int getNumLeaves()
	{
		refreshSubtree();
		return leaves.length;
	}


	LReqBoxInterface[] getLeavesRefreshedRequisitionXBoxes()
	{
		refreshSubtree();
		return getChildrenRefreshedRequistionXBoxes( Arrays.asList( leaves ) );
	}

	LReqBoxInterface[] getLeavesRefreshedRequisitionYBoxes()
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
		for (LSElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationX( prevWidth[i] );
			i++;
		}
	}

	void refreshLeavesAllocationY(LAllocV prevAllocV[])
	{
		refreshSubtree();
		int i = 0;
		for (LSElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocV[i] );
			i++;
		}
	}
	
	
	
	//
	//
	// VISIBILITY CULLING
	//
	//
	
	private final static Iterable<LSContainer> noContainers = Arrays.asList( new LSContainer[] {} );
	private final static Iterable<LSElement> noLeaves = Arrays.asList( new LSElement[] {} );
	
	public Iterable<?>[] getVisibilityCulledBranchAndLeafLists(AABox2 localBox)
	{
		refreshSubtree();
		
		int range[] = getVisibilityCullingRange( localBox );
		final int rangeStart = range[0], rangeEnd = range[1]; 
		if ( rangeStart == -1  ||  rangeEnd == -1 )
		{
			return new Iterable<?>[] { noContainers, noLeaves };
		}
		else if ( rangeStart == 0  &&  rangeEnd == leaves.length )
		{
			return new Iterable<?>[] { getBranches(), getLeaves() };
		}
		else
		{
			Iterable<LSContainer> culledBranches = new Iterable<LSContainer>()
			{
				@Override
				public Iterator<LSContainer> iterator()
				{
					return new VisibilityCulledBranchIterator( rangeStart, rangeEnd );
				}
			};
			Iterable<LSElement> culledLeaves = getLeaves().subList( rangeStart, rangeEnd );
			return new Iterable<?>[] { culledBranches, culledLeaves };
		}
	}
	
	protected int[] getVisibilityCullingRange(AABox2 localBox)
	{
		return new int[] { 0, leaves.length };
	}
}
