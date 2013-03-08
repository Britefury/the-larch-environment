//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.List;

import BritefuryJ.LSpace.*;
import BritefuryJ.LSpace.Layout.*;
import BritefuryJ.LSpace.StyleParams.TableStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class LayoutNodeFlowGrid extends ArrangedSequenceLayoutNode
{
	private LReqBox[] rowReqBoxes;
	private LAllocBox[] rowAllocBoxes;
	private FlowGridLayout.ColumnBounds columnBounds;
	
	
	
	public LayoutNodeFlowGrid(LSContainer element)
	{
		super( element );
		rowReqBoxes = null;
		rowAllocBoxes = null;
		columnBounds = null;
	}


	@Override
	protected void updateRequisitionX()
	{
		refreshSubtree();
		
		rowReqBoxes = null;
		rowAllocBoxes = null;
		columnBounds = null;

		LReqBoxInterface layoutReqBox = getRequisitionBox();
		FlowGridLayout.computeRequisitionX_horizontal( layoutReqBox, getLeavesRefreshedRequisitionXBoxes(), getColumnSpacing(), getTargetNumColumns() );
	}

	@Override
	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		int childAllocFlags[] = getLeavesAlignmentFlags();
		int numColumns = columnBounds != null  ?  columnBounds.getNumColumns()  :  0;
		rowReqBoxes = FlowGridLayout.computeRequisitionY_hozirontal( layoutReqBox, getLeavesRefreshedRequisitionYBoxes(), childAllocFlags, getRowSpacing(), numColumns );
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
		
		columnBounds = FlowGridLayout.allocateX_horizontal( layoutReqBox, childBoxes, getAllocationBox(), childAllocBoxes, childAllocFlags,
				getColumnSpacing(), getTargetNumColumns(), getColumnExpand() );
		
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
		
		rowAllocBoxes = new LAllocBox[rowReqBoxes.length];
		for (int i = 0; i < rowReqBoxes.length; i++)
		{
			rowAllocBoxes[i] = new LAllocBox( null );
		}

		int numColumns = columnBounds != null  ?  columnBounds.getNumColumns()  :  0;
		FlowGridLayout.allocateY_horizontal( layoutReqBox, rowReqBoxes, childBoxes, getAllocationBox(), rowAllocBoxes, childAllocBoxes, childAllocFlags, getRowSpacing(), true, numColumns );
		
		refreshLeavesAllocationY( prevAllocV );
	}
	
	
	
	
	@Override
	protected AABox2[] computeCollatedBranchBoundsBoxes(int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		if ( leaves.length == 0 )
		{
			return new AABox2[0];
		}
		else
		{
			if ( rangeStart == rangeEnd )
			{
				return new AABox2[0];
			}
			else
			{
				// rangeEnd is the end of the range, we need
				int rangeLast = rangeEnd - 1;
				
				int numColumns = columnBounds.getNumColumns();
				int startRow = rangeStart / numColumns;
				int startCol = rangeStart % numColumns;
				int endRow = rangeLast / numColumns;
				int endCol = rangeLast % numColumns;
				
				double lowerX, upperX, lowerY, upperY;
				if ( startRow == endRow )
				{
					lowerX = columnBounds.lowerX( startCol );
					upperX = columnBounds.upperX( endCol );
					lowerY = rowAllocBoxes[startRow].getAllocPositionInParentSpaceY();
					upperY = lowerY + rowAllocBoxes[startRow].getAllocHeight();
					return new AABox2[] { new AABox2( lowerX, lowerY, upperX, upperY ) };
				}
				else
				{
					int numBoxes = (endRow - startRow) + 1;
					AABox2[] boxes = new AABox2[numBoxes];
					
					// Box for start row
					lowerX = columnBounds.lowerX( startCol );
					upperX = columnBounds.upperX( numColumns - 1 );
					lowerY = rowAllocBoxes[startRow].getAllocPositionInParentSpaceY();
					upperY = lowerY + rowAllocBoxes[startRow].getAllocHeight();
					boxes[0] = new AABox2( lowerX, lowerY, upperX, upperY );
					
					// Box for end row
					lowerX = columnBounds.lowerX( 0 );
					upperX = columnBounds.upperX( endCol );
					lowerY = rowAllocBoxes[endRow].getAllocPositionInParentSpaceY();
					upperY = lowerY + rowAllocBoxes[endRow].getAllocHeight();
					boxes[numBoxes - 1] = new AABox2( lowerX, lowerY, upperX, upperY );
					
					// Intermediate boxes
					lowerX = columnBounds.lowerX( 0 );
					upperX = columnBounds.upperX( numColumns - 1 );
					for (int row = startRow + 1, i = 1; row < endRow; row++, i++)
					{
						lowerY = rowAllocBoxes[row].getAllocPositionInParentSpaceY();
						upperY = lowerY + rowAllocBoxes[row].getAllocHeight();
						boxes[i] = new AABox2( lowerX, lowerY, upperX, upperY );
					}
					
					return boxes;
				}
			}
		}
	}



	private LSElement getChildLeafWithinRangeClosestToLocalPoint(Point2 localPos, ElementFilter filter, int rangeStart, int rangeEnd)
	{
		refreshSubtree();
		
		int cellPos[] = getCellPositionUnder( localPos );
		
		if ( columnBounds != null  &&  cellPos != null )
		{
			int childIndex = cellPos[1] * columnBounds.getNumColumns()  +  cellPos[0];
			
			childIndex = Math.min( childIndex, leaves.length - 1 );
			if ( childIndex < rangeStart  ||  childIndex >= rangeEnd )
			{
				return null;
			}

			LSElement child = leaves[childIndex];
			
			
			LSElement c = getLeafClosestToLocalPointFromChild( child, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			
			LSElement next = null;
			for (int j = childIndex + 1; j < rangeEnd; j++)
			{
				next = getLeafClosestToLocalPointFromChild( leaves[j], localPos, filter );
				if ( next != null )
				{
					break;
				}
			}

			LSElement prev = null;
			for (int j = childIndex - 1; j >= rangeStart; j--)
			{
				prev = getLeafClosestToLocalPointFromChild( leaves[j], localPos, filter );
				if ( prev != null )
				{
					break;
				}
			}
	
			
			if ( prev == null  &&  next == null )
			{
				return null;
			}
			else if ( prev == null  &&  next != null )
			{
				return next;
			}
			else if ( prev != null  &&  next == null )
			{
				return prev;
			}
			else
			{
				double sqrDistToPrev = prev.getLocalToAncestorXform( element ).transform( prev.getLocalAABox() ).sqrDistanceTo( localPos );
				double sqrDistToNext = next.getLocalToAncestorXform( element ).transform( next.getLocalAABox() ).sqrDistanceTo( localPos );
				return sqrDistToPrev > sqrDistToNext  ?  prev  :  next;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafWithinRangeClosestToLocalPoint( localPos, filter, 0, leaves.length );
	}
	
	@Override
	public LSElement getChildLeafClosestToLocalPointWithinBranch(LSContainer withinBranch, Point2 localPos, ElementFilter filter)
	{
		int range[] = getBranchRange( withinBranch );
		return getChildLeafWithinRangeClosestToLocalPoint( localPos, filter, range[0], range[1] );
	}



	@Override
	public InsertionPoint getInsertionPointClosestToLocalPoint(LSContainer withinBranch, Point2 localPos)
	{
		refreshSubtree();

		int cellPos[] = getCellPositionUnder( localPos );
		if ( cellPos != null )
		{
			int x = cellPos[0], y = cellPos[1];
			int branchBounds[] = withinBranch == element  ?  new int[] { 0, leaves.length }  :  getBranchRange( withinBranch );

			if ( branchBounds != null )
			{
				int cellIndex = y*columnBounds.getNumColumns()+x;
				if ( cellIndex >= branchBounds[0]  && cellIndex < branchBounds[1] )
				{
					LSElement leaf = leaves[cellIndex];

					// Walk back up until we have an element that is a direct child of @withinBranch
					while ( leaf.getParent() != withinBranch )
					{
						leaf = leaf.getParent();
						if ( leaf == null )
						{
							throw new RuntimeException( "Could not trace back to branch" );
						}
					}

					LSElement child = leaf;
					Point2 left[], right[];
					int childBounds[];
					if ( child instanceof LSContainer  &&  child.getLayoutNode() == null )
					{
						childBounds = getBranchRange( child );
					}
					else
					{
						int index = getLeaves().indexOf( child );
						childBounds = new int[] { index, index + 1 };
					}
					int numColumns = columnBounds.getNumColumns();
					double beginX = columnBounds.lowerX( childBounds[0] % numColumns );
					double endX = columnBounds.upperX( ( childBounds[1] - 1 ) % numColumns );
					LAllocBox beginRow = rowAllocBoxes[childBounds[0] / numColumns];
					LAllocBox endRow = rowAllocBoxes[( childBounds[1] - 1 ) / numColumns];
					double beginY0 = beginRow.getAllocPositionInParentSpaceY(),  beginY1 = beginY0 + beginRow.getAllocHeight();
					double endY0 = endRow.getAllocPositionInParentSpaceY(),  endY1 = endY0 + endRow.getAllocHeight();
					left = new Point2[] { new Point2( beginX, beginY0 ), new Point2( beginX, beginY1 ) };
					right = new Point2[] { new Point2( endX, endY0 ), new Point2( endX, endY1 ) };
					AABox2 leftBox = new AABox2( left[0], left[1] );
					AABox2 rightBox = new AABox2( right[0], right[1] );

					double leftSqrDist = leftBox.sqrDistanceTo( localPos );
					double rightSqrDist = rightBox.sqrDistanceTo( localPos );
					int index;
					Point2 line[];
					if ( leftSqrDist < rightSqrDist )
					{
						index = childBounds[0];
						line = left;
					}
					else
					{
						index = childBounds[1];
						line = right;
					}

					return new InsertionPoint( index, line );
				}
				else if ( cellIndex >= branchBounds[1]  &&  branchBounds[1] == leaves.length )
				{
					// targetPos is beyond the end of the flow grid; insert at the end
					int numColumns = columnBounds.getNumColumns();
					double endX = columnBounds.upperX( ( branchBounds[1] - 1 ) % numColumns );
					LAllocBox endRow = rowAllocBoxes[( branchBounds[1] - 1 ) / numColumns];
					double endY0 = endRow.getAllocPositionInParentSpaceY(),  endY1 = endY0 + endRow.getAllocHeight();
					Point2 line[] = new Point2[] { new Point2( endX, endY0 ), new Point2( endX, endY1 ) };

					return new InsertionPoint( branchBounds[1], line );
				}
			}
		}

		AABox2 branchBox = withinBranch.getLocalAABox();
		return new InsertionPoint( 0, branchBox.getLeftEdge() );
	}



	public int getNumColumns()
	{
		refreshSubtree();
		if ( columnBounds != null )
		{
			return columnBounds.getNumColumns();
		}
		else
		{
			return 0;
		}
	}


	public int getNumRows()
	{
		refreshSubtree();
		return rowReqBoxes.length;
	}


	public boolean hasChildAt(int x, int y)
	{
		refreshSubtree();
		if ( columnBounds != null )
		{
			int numColumns = columnBounds.getNumColumns();
			return ( y * numColumns + x )  <  leaves.length;
		}
		else
		{
			return false;
		}
	}

	public LSElement getChildAt(int x, int y)
	{
		refreshSubtree();
		if ( columnBounds != null )
		{
			int numColumns = columnBounds.getNumColumns();
			int index = y * numColumns + x;
			return index < leaves.length  ?  leaves[y * numColumns + x]  : null;
		}
		else
		{
			return null;
		}
	}


	public int[] getPositionOfChildCoveringCell(int x, int y)
	{
		refreshSubtree();
		if ( columnBounds != null )
		{
			int numColumns = columnBounds.getNumColumns();
			if ( ( y * numColumns + x )  <  leaves.length )
			{
				return new int[] { x, y };
			}
		}
		return null;
	}

	public LSElement getChildCoveringCell(int x, int y)
	{
		return getChildAt( x, y );
	}


	public int[] getCellPositionUnder(Point2 localPos)
	{
		refreshSubtree();
		
		// Find the closest row
		int rowIndex = -1;
		
		if ( rowAllocBoxes.length == 0 )
		{
			return null;
		}
		else if ( rowAllocBoxes.length == 1 )
		{
			rowIndex = 0;
		}
		else
		{
			LAllocBox rowI = rowAllocBoxes[0];
			for (int i = 0; i < rowAllocBoxes.length - 1; i++)
			{
				LAllocBox rowJ = rowAllocBoxes[i+1];
				double iUpperY = rowI.getAllocPositionInParentSpaceY() + rowI.getAllocHeight();
				double jLowerY = rowJ.getAllocPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					rowIndex = i;
					break;
				}
				
				rowI = rowJ;
			}

			if ( rowIndex == -1 )
			{
				rowIndex = rowAllocBoxes.length - 1;
			}
		}
		
		if ( columnBounds != null )
		{
			// Find the closest column
			int columnIndex = columnBounds.getColumnUnder( localPos.x );
			
			return new int[] { columnIndex, rowIndex };
		}
		else
		{
			return null;
		}
	}


	public double getColumnBoundaryX(int column)
	{
		refreshSubtree();
		
		if ( columnBounds != null )
		{
			int numColumns = columnBounds.getNumColumns();
			
			if ( column == 0 )
			{
				return columnBounds.lowerX( 0 );
			}
			else if ( column == numColumns )
			{
				return columnBounds.upperX( column - 1 );
			}
			else
			{
				return columnBounds.centreLineToRightOf( column - 1 );
			}
		}
		else
		{
			return 0.0;
		}
	}


	public double getRowBoundaryY(int row)
	{
		refreshSubtree();
		int numRows = rowReqBoxes.length;
		
		if ( numRows == 0 )
		{
			return 0.0;
		}
		else
		{
			if ( row == 0 )
			{
				return rowAllocBoxes[0].getAllocPositionInParentSpaceY();
			}
			else if ( row == numRows )
			{
				LAllocBox bottom = rowAllocBoxes[row-1];
				return bottom.getAllocPositionInParentSpaceY() + bottom.getAllocHeight();
			}
			else
			{
				double halfRowSpacing = getRowSpacing() * 0.5;
				LAllocBox bottom = rowAllocBoxes[row-1];
				return bottom.getAllocPositionInParentSpaceY() + bottom.getAllocHeight()  +  halfRowSpacing;
			}
		}
	}
	
	
	public void drawCellLines(Graphics2D graphics)
	{
		refreshSubtree();
		
		if ( leaves.length > 0 )
		{
			// Columns
			int numColumns = columnBounds.getNumColumns();
			int numRows = rowReqBoxes.length;
			
			int lastRowLength = leaves.length % numColumns;
			if ( lastRowLength == 0 )
			{
				lastRowLength = numColumns;
			}
			
			double y0 = getRowBoundaryY( 0 );
			double y1Full = getRowBoundaryY( numRows );
			double y1ExceptLast = getRowBoundaryY( numRows - 1 );
			for (int i = 1; i < numColumns; i++)
			{
				double x = columnBounds.centreLineToRightOf( i - 1 );
				double y1 = i <= lastRowLength  ?  y1Full  :  y1ExceptLast;
				
				graphics.draw( new Line2D.Double( x, y0, x, y1 ) );
			}
			
			double x0 = getColumnBoundaryX( 0 );
			double x1 = getColumnBoundaryX( numColumns );
			double halfRowSpacing = getRowSpacing() * 0.5;
			for (int i = 1; i < numRows; i++)
			{
				LAllocBox bottom = rowAllocBoxes[i-1];
				double y = bottom.getAllocPositionInParentSpaceY() + bottom.getAllocHeight()  +  halfRowSpacing;
	
				graphics.draw( new Line2D.Double( x0, y, x1, y ) );
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
	
	
	
	private double getColumnSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnSpacing();
	}
	
	private boolean getColumnExpand()
	{
		return ((TableStyleParams)element.getStyleParams()).getColumnExpand();
	}

	private double getRowSpacing()
	{
		return ((TableStyleParams)element.getStyleParams()).getRowSpacing();
	}

	private int getTargetNumColumns()
	{
		return ((LSFlowGrid)element).getTargetNumColumns();
	}
}
