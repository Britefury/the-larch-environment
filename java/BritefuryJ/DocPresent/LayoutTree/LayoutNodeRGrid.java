//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeRGrid extends ArrangedSequenceLayoutNode
{
	private LReqBoxInterface columnBoxes[], rowBoxes[];
	private LAllocBoxInterface columnAllocBoxes[], rowAllocBoxes[];


	public LayoutNodeRGrid(DPRGrid element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		DPRGrid grid = (DPRGrid)element;
		
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		int numRows = leaves.length;
		int numColumns = grid.width();
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		boolean bRowIsGridRow[] = new boolean[numRows];
		for (int i = 0; i < leaves.length; i++)
		{
			DPElement child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				childBoxes[i] = ((LayoutNodeGridRow)row.getLayoutNode()).getLeavesRefreshedRequisitonXBoxes();
				bRowIsGridRow[i] = true;
			}
			else
			{
				childBoxes[i] = new LReqBoxInterface[] { child.getLayoutNode().refreshRequisitionX() };
				bRowIsGridRow[i] = false;
			}
		}
		
		columnBoxes = GridLayout.computeRequisitionX( layoutReqBox, childBoxes, bRowIsGridRow, numColumns, numRows, getColumnSpacing(), getRowSpacing() );

		// Copy the X-requisition to the child rows
		for (int i = 0; i < leaves.length; i++)
		{
			DPElement child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				((LayoutNodeGridRow)row.getLayoutNode()).getRequisitionBox().setRequisitionX( layoutReqBox );
			}
		}

		columnAllocBoxes = new LAllocBoxInterface[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		//refreshSubtree();   -- unnecessary since this is done in updateRequisitionX()

		LReqBoxInterface layoutReqBox = getRequisitionBox();
		rowBoxes = getLeavesRefreshedRequistionYBoxes();
		
		GridLayout.computeRequisitionY( layoutReqBox, rowBoxes, getRowSpacing() );

		rowAllocBoxes = new LAllocBoxInterface[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = leaves[i].getLayoutNode().getAllocationBox();
		}
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();

		//refreshSubtree();   -- unnecessary since this is done in updateRequisitionX()
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPRGrid grid = (DPRGrid)element;
		
		int numRows = leaves.length;
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		LAllocBoxInterface childAllocBoxes[][] = new LAllocBoxInterface[numRows][];
		double prevWidths[][] = new double[numRows][];
		int childAlignmentFlags[][] = new int[numRows][];
		boolean bRowIsGridRow[] = new boolean[numRows];
		for (int i = 0; i < leaves.length; i++)
		{
			DPElement child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				LayoutNodeGridRow rowLayoutNode = (LayoutNodeGridRow)row.getLayoutNode();
				childBoxes[i] = rowLayoutNode.getLeavesRequisitionBoxes();
				childAllocBoxes[i] = rowLayoutNode.getLeavesAllocationBoxes();
				prevWidths[i] = rowLayoutNode.getLeavesAllocationX();
				childAlignmentFlags[i] = rowLayoutNode.getLeavesAlignmentFlags();
				// Copy grid x-allocation to row x-allocation
				LAllocHelper.allocateX( rowLayoutNode.getAllocationBox(), getAllocationBox() );
				bRowIsGridRow[i] = true;
			}
			else
			{
				LayoutNode childLayoutNode = child.getLayoutNode();
				childBoxes[i] = new LReqBoxInterface[] { childLayoutNode.getRequisitionBox() };
				childAllocBoxes[i] = new LAllocBoxInterface[] { childLayoutNode.getAllocationBox() };
				prevWidths[i] = new double[] { child.getAllocationX() };
				childAlignmentFlags[i] = new int[] { child.getAlignmentFlags() };
				bRowIsGridRow[i] = false;
			}
		}

		GridLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, getAllocationBox(), columnAllocBoxes, childAllocBoxes, childAlignmentFlags, bRowIsGridRow, grid.width(), numRows,
				getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		for (int r = 0; r < leaves.length; r++)
		{
			double rowPrevWidths[] = prevWidths[r];

			DPElement child = leaves[r];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				LayoutNodeGridRow rowLayoutNode = (LayoutNodeGridRow)row.getLayoutNode();
				
				child.getLayoutNode().onAllocationXRefreshed();
				for (int c = 0; c < rowLayoutNode.leaves.length; c++)
				{
					rowLayoutNode.leaves[c].getLayoutNode().refreshAllocationX( rowPrevWidths[c] );
				}
			}
			else
			{
				child.getLayoutNode().refreshAllocationX( rowPrevWidths[0] );
			}
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		//refreshSubtree();   -- unnecessary since this is done in updateRequisitionX()

		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		LAllocV prevAllocVs[] = getLeavesAllocV();
		
		GridLayout.allocateY( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, getRowSpacing(), getRowExpand() );
		
		int i = 0;
		for (DPElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
		
		columnBoxes = rowBoxes = null;
		columnAllocBoxes = rowAllocBoxes = null;
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
			double yStart = startLeaf.getPositionInParentSpaceY();
			double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getHeightInParentSpace();
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



	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams)element.getStyleParams()).getRowExpand();
	}
}
