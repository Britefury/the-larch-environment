//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.DPRGrid;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
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
		
		int numRows = leaves.length;
		int numColumns = grid.width();
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		for (int i = 0; i < leaves.length; i++)
		{
			DPWidget child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				childBoxes[i] = ((LayoutNodeGridRow)row.getLayoutNode()).getLeavesRefreshedRequisitonXBoxes();
			}
			else
			{
				childBoxes[i] = new LReqBoxInterface[] { child.getLayoutNode().refreshRequisitionX() };
			}
		}
		
		columnBoxes = GridLayout.computeRequisitionX( layoutReqBox, childBoxes, numColumns, numRows, getColumnSpacing(), getRowSpacing() );

		// Copy the X-requisition to the child rows
		for (int i = 0; i < leaves.length; i++)
		{
			DPWidget child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				((LayoutNodeGridRow)row.getLayoutNode()).layoutReqBox.setRequisitionX( layoutReqBox );
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
		
		DPRGrid grid = (DPRGrid)element;
		
		int numRows = leaves.length;
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		LAllocBoxInterface childAllocBoxes[][] = new LAllocBoxInterface[numRows][];
		double prevWidths[][] = new double[numRows][];
		int childAlignmentFlags[][] = new int[numRows][];
		for (int i = 0; i < leaves.length; i++)
		{
			DPWidget child = leaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				LayoutNodeGridRow rowLayoutNode = (LayoutNodeGridRow)row.getLayoutNode();
				childBoxes[i] = rowLayoutNode.getLeavesRequisitionBoxes();
				childAllocBoxes[i] = rowLayoutNode.getLeavesAllocationBoxes();
				prevWidths[i] = rowLayoutNode.getLeavesAllocationX();
				childAlignmentFlags[i] = rowLayoutNode.getLeavesAlignmentFlags();
				// Copy grid x-allocation to row x-allocation
				rowLayoutNode.layoutAllocBox.allocateX( layoutAllocBox );
			}
			else
			{
				LayoutNode childLayoutNode = child.getLayoutNode();
				childBoxes[i] = new LReqBoxInterface[] { childLayoutNode.getRequisitionBox() };
				childAllocBoxes[i] = new LAllocBoxInterface[] { childLayoutNode.getAllocationBox() };
				prevWidths[i] = new double[] { child.getAllocationX() };
				childAlignmentFlags[i] = new int[] { child.getAlignmentFlags() };
			}
		}

		GridLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, layoutAllocBox, columnAllocBoxes, childAllocBoxes, childAlignmentFlags, grid.width(), numRows,
				getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		for (int r = 0; r < leaves.length; r++)
		{
			double rowPrevWidths[] = prevWidths[r];

			DPWidget child = leaves[r];
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
		
		LReqBoxInterface childBoxes[] = getLeavesRequisitionBoxes();
		LAllocBoxInterface childAllocBoxes[] = getLeavesAllocationBoxes();
		LAllocV prevAllocVs[] = getLeavesAllocV();
		
		GridLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getRowSpacing(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
		
		columnBoxes = rowBoxes = null;
		columnAllocBoxes = rowAllocBoxes = null;
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



	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleSheet)element.getStyleSheet()).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleSheet)element.getStyleSheet()).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleSheet)element.getStyleSheet()).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleSheet)element.getStyleSheet()).getRowExpand();
	}
}
