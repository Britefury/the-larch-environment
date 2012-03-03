//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSGridRow;
import BritefuryJ.LSpace.LSRGrid;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.GridLayout;
import BritefuryJ.LSpace.Layout.LAllocBox;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocHelper;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.StyleParams.TableStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeRGrid extends ArrangedSequenceLayoutNode
{
	private LReqBoxInterface columnBoxes[], rowBoxes[];
	private LAllocBoxInterface columnAllocBoxes[], rowAllocBoxes[];
	private double columnLines[][], rowLines[][];


	public LayoutNodeRGrid(LSRGrid element)
	{
		super( element );
	}

	
	protected void updateRequisitionX()
	{
		LSRGrid grid = (LSRGrid)element;
		
		refreshSubtree();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		int numRows = leaves.length;
		int numColumns = grid.getNumColumns();
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		boolean bRowIsGridRow[] = new boolean[numRows];
		for (int i = 0; i < leaves.length; i++)
		{
			LSElement child = leaves[i];
			if ( child instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)child;
				childBoxes[i] = ((LayoutNodeGridRow)row.getLayoutNode()).getLeavesRefreshedRequisitionXBoxes();
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
			LSElement child = leaves[i];
			if ( child instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)child;
				row.getLayoutNode().getRequisitionBox().setRequisitionX( layoutReqBox );
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
		rowBoxes = getLeavesRefreshedRequisitionYBoxes();
		
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
		LSRGrid grid = (LSRGrid)element;
		
		int numRows = leaves.length;
		LReqBoxInterface childBoxes[][] = new LReqBoxInterface[numRows][];
		LAllocBoxInterface childAllocBoxes[][] = new LAllocBoxInterface[numRows][];
		double prevWidths[][] = new double[numRows][];
		int childAlignmentFlags[][] = new int[numRows][];
		boolean bRowIsGridRow[] = new boolean[numRows];
		for (int i = 0; i < leaves.length; i++)
		{
			LSElement child = leaves[i];
			if ( child instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)child;
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
				prevWidths[i] = new double[] { child.getAllocWidth() };
				childAlignmentFlags[i] = new int[] { child.getAlignmentFlags() };
				bRowIsGridRow[i] = false;
			}
		}

		GridLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, getAllocationBox(), columnAllocBoxes, childAllocBoxes, childAlignmentFlags, bRowIsGridRow, grid.getNumColumns(), numRows,
				getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		for (int r = 0; r < leaves.length; r++)
		{
			double rowPrevWidths[] = prevWidths[r];

			LSElement child = leaves[r];
			if ( child instanceof LSGridRow )
			{
				LSGridRow row = (LSGridRow)child;
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
		for (LSElement child: leaves)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	
	
	
	private int getColumnForLocalPoint(Point2 localPos)
	{
		if ( columnAllocBoxes.length == 0 )
		{
			return -1;
		}
		else if ( columnAllocBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBoxInterface columnI = columnAllocBoxes[0];
			for (int i = 0; i < columnAllocBoxes.length - 1; i++)
			{
				LAllocBoxInterface columnJ = columnAllocBoxes[i+1];
				double iUpperX = columnI.getAllocPositionInParentSpaceX() + columnI.getAllocWidth();
				double jLowerX = columnJ.getAllocPositionInParentSpaceX();
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return i;
				}
				
				columnI = columnJ;
			}
			
			return columnAllocBoxes.length-1;
		}
	}

	
	
	private int getRowForLocalPoint(Point2 localPos)
	{
		if ( rowAllocBoxes.length == 0 )
		{
			return -1;
		}
		else if ( rowAllocBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBoxInterface rowI = rowAllocBoxes[0];
			for (int i = 0; i < rowAllocBoxes.length - 1; i++)
			{
				LAllocBoxInterface rowJ = rowAllocBoxes[i+1];
				double iUpperY = rowI.getAllocPositionInParentSpaceY() + rowI.getAllocHeight();
				double jLowerY = rowJ.getAllocPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					return i;
				}
				
				rowI = rowJ;
			}
			
			return rowAllocBoxes.length-1;
		}
	}


	
	public int[] getCellPositionUnder(Point2 localPos)
	{
		int x = getColumnForLocalPoint( localPos );
		int y = getRowForLocalPoint( localPos );
		return new int[] { x, y };
	}
	

	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
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
	//
	// CELL BOUNDARY LINES
	//
	//
	
	protected static int[] getSpanFromBitSet(BitSet bits, int startIndex)
	{
		int start = bits.nextSetBit( startIndex );
		if ( start == -1 )
		{
			return new int[] { -1, -1 };
		}
		int end = bits.nextClearBit( start );
		if ( end == -1 )
		{
			end = bits.length();
		}
		return new int[] { start, end - 1 };
	}
	
	
	
	public void queueResize()
	{
		super.queueResize();
		
		columnLines = null;
		rowLines = null;
	}
	
	private void refreshBoundaries()
	{
		refreshSubtree();
		LSRGrid grid = (LSRGrid)element;
		if ( ( columnLines == null  ||  rowLines == null ) )
		{
			int numColumns = grid.getNumColumns();
			int numRows = leaves.length;
			int numColLines = Math.max( numColumns - 1, 0 );
			int numRowLines = Math.max( numRows - 1, 0 );
			columnLines = new double[numColLines][];
			rowLines = new double[numRowLines][];
			
			
			// Create and initialise bitsets for the column and row lines
			BitSet columnBits[] = new BitSet[numColLines];
			BitSet rowBits[] = new BitSet[numRowLines];
			
			for (int i = 0; i < columnBits.length; i++)
			{
				columnBits[i] = new BitSet( numRows );
			}
			for (int i = 0; i < rowBits.length; i++)
			{
				rowBits[i] = new BitSet( numColumns );
			}
			
			
			// For each cell, set the bits that mark its boundary
			int r = 0;
			for (LSElement rowElement: leaves)
			{
				if ( rowElement instanceof LSGridRow )
				{
					// A grid row
					LSGridRow row = (LSGridRow)rowElement;
					LayoutNodeGridRow rowLayout = (LayoutNodeGridRow)row.getLayoutNode();
					int rowSize = rowLayout.getLeaves().size();
					
					// Set 
					for (int c = 0; c < rowSize; c++)
					{
						if ( r > 0 )
						{
							rowBits[r-1].set( c );
						}
						if ( r < numRowLines )
						{
							rowBits[r].set( c );
						}
						if ( c < numColLines )
						{
							columnBits[c].set( r );
						}
					}
				}
				else
				{
					// Not a GridRow; expands to fill full width - activate all lines on surrounding rows
					for (int c = 0; c < numColumns; c++)
					{
						if ( r > 0 )
						{
							rowBits[r-1].set( c );
						}
						if ( r < numRowLines )
						{
							rowBits[r].set( c );
						}
					}
				}
				r++;
			}
			
			ArrayList<Double> spanStarts = new ArrayList<Double>();
			ArrayList<Double> spanEnds = new ArrayList<Double>();
			
			double halfColumnSpacing = getColumnSpacing() * 0.5;
			double halfRowSpacing = getRowSpacing() * 0.5;

			for (int columnLine = 0; columnLine < numColLines; columnLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int y = 0;
				while ( y < numRows )
				{
					int spanIndices[] = getSpanFromBitSet( columnBits[columnLine], y );
					if ( spanIndices[0] == -1  ||  spanIndices[1] == -1 )
					{
						break;
					}
					double topSpacing = spanIndices[0] == 0  ?  0.0  :  halfRowSpacing;
					double bottomSpacing = spanIndices[1] == numRows-1  ?  0.0  :  halfRowSpacing;
					spanStarts.add( getRowTop( spanIndices[0] ) - topSpacing );
					spanEnds.add( getRowBottom( spanIndices[1] ) + bottomSpacing );
					y = spanIndices[1] + 1;
				}
				
				double col[] = new double[spanStarts.size()*2+1];
				col[0] = getColumnRight( columnLine )  +  halfColumnSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					col[i*2+1] = spanStarts.get( i );
					col[i*2+2] = spanEnds.get( i );
				}
				columnLines[columnLine] = col;
			}
			
			for (int rowLine = 0; rowLine < numRowLines; rowLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int x = 0;
				while ( x < numColumns )
				{
					int spanIndices[] = getSpanFromBitSet( rowBits[rowLine], x );
					if ( spanIndices[0] == -1  ||  spanIndices[1] == -1 )
					{
						break;
					}
					double leftSpacing = spanIndices[0] == 0  ?  0.0  :  halfColumnSpacing;
					double rightSpacing = spanIndices[1] == numColumns-1  ?  0.0  :  halfColumnSpacing;
					spanStarts.add( getColumnLeft( spanIndices[0] ) - leftSpacing );
					spanEnds.add( getColumnRight( spanIndices[1] ) + rightSpacing );
					x = spanIndices[1] + 1;
				}
				
				double row[] = new double[spanStarts.size()*2+1];
				row[0] = getRowBottom( rowLine )  +  halfRowSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					row[i*2+1] = spanStarts.get( i );
					row[i*2+2] = spanEnds.get( i );
				}
				rowLines[rowLine] = row;
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
	//
	// COLUMN AND ROW QUERY METHODS
	//
	//
	
	public double getColumnLeft(int column)
	{
		LAllocBoxInterface box = columnAllocBoxes[column];
		return box.getAllocPositionInParentSpaceX();
	}

	public double getColumnRight(int column)
	{
		LAllocBoxInterface box = columnAllocBoxes[column];
		return box.getAllocPositionInParentSpaceX() + box.getActualWidth();
	}

	public double getRowTop(int row)
	{
		LAllocBoxInterface box = leaves[row].getLayoutNode().getAllocationBox();
		return box.getAllocPositionInParentSpaceY();
	}

	public double getRowBottom(int row)
	{
		LAllocBoxInterface box = leaves[row].getLayoutNode().getAllocationBox();
		return box.getAllocPositionInParentSpaceY() + box.getAllocHeight();
	}
	
	
	public double getColumnBoundaryX(int column)
	{
		LSRGrid grid = (LSRGrid)element;
		int numColumns = grid.getNumColumns();

		if ( column == 0 )
		{
			return getColumnLeft( 0 );
		}
		else if ( column == numColumns )
		{
			return getColumnRight( column - 1 );
		}
		else
		{
			double halfColumnSpacing = getColumnSpacing() * 0.5;
			return getColumnRight( column - 1 )  +  halfColumnSpacing;
		}
	}
	
	public double getRowBoundaryY(int row)
	{
		int numRows = leaves.length;

		if ( row == 0 )
		{
			return getRowTop( 0 );
		}
		else if ( row == numRows )
		{
			return getRowBottom( row - 1 );
		}
		else
		{
			double halfRowSpacing = getRowSpacing() * 0.5;
			return getRowBottom( row - 1 )  +  halfRowSpacing;
		}
	}

	
	public void drawCellLines(Graphics2D graphics)
	{
		refreshBoundaries();
		LayoutNodeTable.drawTableCellLines( graphics, columnLines, rowLines );
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
