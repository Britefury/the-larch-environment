//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Layout;

public class FlowGridLayout
{
	public static class ColumnBounds
	{
		private double width, spacing;
		private int numColumns;
		
		public ColumnBounds(double width, double spacing, int numColumns)
		{
			this.width = width;
			this.spacing = spacing;
			this.numColumns = numColumns;
		}
		
		
		public int getNumColumns()
		{
			return numColumns;
		}
		
		public double lowerX(int column)
		{
			return ( width + spacing ) * column;
		}

		public double upperX(int column)
		{
			return ( width + spacing ) * column + width;
		}
		
		public double centreLineToRightOf(int column)
		{
			return ( width + spacing ) * column  +  width  +  spacing * 0.5;
		}
		
		public int getColumnUnder(double x)
		{
			// The dividing line between colums lies in the middle of the spacing.
			// Spacing does not appear on the left hand side at all, so offset x by half the spacing
			x += spacing * 0.5;
			
			return (int)Math.floor( x / ( width + spacing ) );
		}
	}
	
	
	public static void computeRequisitionX_horizontal(LReqBoxInterface reqBox, LReqBoxInterface childReqBoxes[], double columnSpacing)
	{
		// Accumulate the width required for all the children
		
		double minWidth = 0.0, prefWidth = 0.0;
		double prefX = 0.0;
		for (LReqBoxInterface child: childReqBoxes)
		{
			// Minimum width and hadvance are the greatest minimum requirements for any child
			minWidth = Math.max( minWidth,  child.getReqMinWidth() );

			// Preferred width and hadvance are accumulated, with @columnSpacing between each one
			prefWidth = prefX + child.getReqPrefWidth();
			prefX = prefWidth + columnSpacing;
		}
		
		reqBox.setRequisitionX( minWidth, prefWidth, minWidth, prefWidth );
	}

	private static void computeRowRequisitionY_hozirontal(LReqBox rowReqBox, LReqBoxInterface childReqBoxes[], int childAllocationFlags[], double rowSpacing, int start, int end)
	{
		rowReqBox.clearRequisitionY();
		
		double rowHeight = 0.0, rowHeightAboveRef = 0.0,  rowHeightBelowRef = 0.0, rowHeightAndSpacing = 0.0, rowHeightBelowRefAndSpacing = 0.0;
		boolean bRefYAligned = false;
		for (int i = start; i < end; i++)
		{
			LReqBoxInterface child = childReqBoxes[i];
			VAlignment v = ElementAlignment.getVAlignment( childAllocationFlags[i] );
			
			double childHeight = child.getReqHeight();
			
			if ( v == VAlignment.REFY  ||  v == VAlignment.REFY_EXPAND )
			{
				double childHeightAboveRef = child.getReqRefY();
				double childHeightBelowRef = childHeight - childHeightAboveRef;
				double childHeightBelowRefAndSpacing = childHeightBelowRef + child.getReqVSpacing();
				
				rowHeight = Math.max( rowHeight, childHeight );
				rowHeightAboveRef = Math.max( rowHeightAboveRef, childHeightAboveRef );
				rowHeightBelowRef = Math.max( rowHeightBelowRef, childHeightBelowRef );
				rowHeightBelowRefAndSpacing = Math.max( rowHeightBelowRefAndSpacing, childHeightBelowRefAndSpacing );
				
				bRefYAligned = true;
			}
			else
			{
				double childHeightAndSpacing = childHeight + child.getReqVSpacing();

				rowHeight = Math.max( rowHeight, childHeight );
				rowHeightAndSpacing = Math.max( rowHeightAndSpacing, childHeightAndSpacing );
			}
		}
		rowHeight = Math.max( rowHeight, rowHeightAboveRef + rowHeightBelowRef );
		rowHeightAndSpacing = Math.max( rowHeightAndSpacing, rowHeightAboveRef + rowHeightBelowRefAndSpacing );
		
		if ( bRefYAligned )
		{
			rowReqBox.setRequisitionY( rowHeight, rowHeightAndSpacing - rowHeight, rowHeightAboveRef );
		}
		else
		{
			rowReqBox.setRequisitionY( rowHeight, rowHeightAndSpacing - rowHeight );
		}
	}

	public static LReqBox[] computeRequisitionY_hozirontal(LReqBoxInterface reqBox, LReqBoxInterface childReqBoxes[], int childAllocationFlags[], double rowSpacing, int numColumns)
	{
		int numRows = childReqBoxes.length / numColumns;
		if ( childReqBoxes.length % numColumns  >  0 )
		{
			numRows++;
		}
		
		LReqBox rowReqBoxes[] = new LReqBox[numRows];
		
		int childIndex = 0;
		for (int i = 0; i < numRows; i++)
		{
			LReqBox rowReqBox = new LReqBox();
			rowReqBoxes[i] = rowReqBox;
			int end = Math.min( childIndex + numColumns, childReqBoxes.length );
			computeRowRequisitionY_hozirontal( rowReqBox, childReqBoxes, childAllocationFlags, rowSpacing, childIndex, end );
			childIndex = end;
		}
		
		VerticalLayout.computeRequisitionY( reqBox, rowReqBoxes, -1, rowSpacing );
		
		return rowReqBoxes;
	}
	
	
	public static ColumnBounds allocateX_horizontal(LReqBoxInterface reqBox, LReqBoxInterface childReqBoxes[], LAllocBoxInterface allocBox, LAllocBoxInterface childAllocBoxes[], int childAllocationFlags[],
			double columnSpacing, int targetNumColumns, boolean expandColumns)
	{
		if ( childReqBoxes.length > 0 )
		{
			// Compute the maximum preferred width of the child elements
			double maxPrefChildWidth = 0.0;
			for (LReqBoxInterface child: childReqBoxes)
			{
				// Minimum width and hadvance are the greatest minimum requirements for any child
				maxPrefChildWidth = Math.max( maxPrefChildWidth,  child.getReqPrefWidth() );
			}

			
			// Compute the number of columns
			// The required minimum width will be the amount of horizontal space required to fit any of the children
			//
			// a = space available (allocBox.getAllocWidth())
			// n = # of columns (numColumns)
			// s = space between columns (columnSpacing)
			// m = width of column (reqBox.getReqMinWidth())
			//
			// a = mn + s(n-1)
			// therfore:
			// n = (a+s) / (m+s)
			int numColumnsByMin = Math.max( (int)Math.floor( ( allocBox.getAllocWidth() + columnSpacing )  /  ( reqBox.getReqMinWidth() + columnSpacing ) ), 1 );
			int numColumnsByPref = Math.max( (int)Math.floor( ( allocBox.getAllocWidth() + columnSpacing )  /  ( maxPrefChildWidth + columnSpacing ) ), 1 );
			
			// The number of columns computed by minimum and preferred child width serve as our lower and upper bounds
			int numColumnsLowerBound = Math.min( numColumnsByMin, numColumnsByPref );
			int numColumnsUpperBound = Math.max( numColumnsByMin, numColumnsByPref );
			
			int numColumns;
			
			if ( targetNumColumns > 0 )
			{
				numColumns = Math.min( targetNumColumns, numColumnsUpperBound );
			}
			else
			{
				// Choose the lower bound, giving child elements as much space as possible
				numColumns = numColumnsLowerBound;
			}
			
			double allocBoxAllocationX = allocBox.getAllocWidth();
			
			double totalSpacing = columnSpacing * ( numColumns - 1 );
			
			double columnWidth = ( allocBoxAllocationX - totalSpacing ) / numColumns;
			
			if ( !expandColumns )
			{
				columnWidth = Math.min( columnWidth, maxPrefChildWidth );
			}
			
			int columnIndex = 0;
			double x = 0.0;
			for (int i = 0; i < childReqBoxes.length; i++)
			{
				LAllocHelper.allocateChildXAligned( childAllocBoxes[i], childReqBoxes[i], childAllocationFlags[i], x, columnWidth );
				x += columnWidth + columnSpacing;
				
				columnIndex++;
				if ( columnIndex == numColumns )
				{
					columnIndex = 0;
					x = 0.0;
				}
			}
			
			return new ColumnBounds( columnWidth, columnSpacing, numColumns );
		}
		else
		{
			return null;
		}
	}
	
	public static void allocateY_horizontal(LReqBoxInterface reqBox, LReqBoxInterface rowReqBoxes[], LReqBoxInterface childReqBoxes[],
			LAllocBoxInterface allocBox, LAllocBoxInterface rowAllocBoxes[], LAllocBoxInterface childAllocBoxes[],
			int childAllocationFlags[], double rowSpacing, boolean bRowExpand, int numColumns)
	{
		// Allocate space to the rows
		VerticalLayout.allocateY( reqBox, rowReqBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
		
		int start = 0;
		for (int rowIndex = 0; rowIndex < rowReqBoxes.length; rowIndex++)
		{
			LReqBoxInterface rowReqBox = rowReqBoxes[rowIndex]; 
			LAllocBoxInterface rowAllocBox = rowAllocBoxes[rowIndex]; 
			LAllocV h = HorizontalLayout.computeVerticalAllocationForRow( rowReqBox, rowAllocBox );

			int end = Math.min( start + numColumns, childReqBoxes.length );
			for (int i = start; i < end; i++)
			{
				LAllocHelper.allocateChildYAligned( childAllocBoxes[i], childReqBoxes[i], childAllocationFlags[i], rowAllocBox.getAllocPositionInParentSpaceY(), h );
			}
			
			start = end;
		}
	}
}
