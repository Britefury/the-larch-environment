//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;




public class TableLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBoxInterface children[], TablePackingParams packingParams[], int numColumns, double columnSpacing)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		int i = 0;
		for (LReqBoxInterface child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan == 1 )
			{
				LReqBox b = columnBoxes[packing.x];
				b.minWidth = Math.max( b.minWidth, child.getReqMinWidth() );
				b.prefWidth = Math.max( b.prefWidth, child.getReqPrefWidth() );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBoxInterface child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endColumn = packing.x + packing.colSpan;
				
				double minWidthAvailable = 0.0, prefWidthAvailable = 0.0;
				for (int c = packing.x; c < endColumn; c++)
				{
					LReqBox colBox = columnBoxes[c];
					
					double spacing = c != endColumn-1  ?  columnSpacing  :  0.0;
					
					minWidthAvailable += colBox.minWidth + spacing;
					prefWidthAvailable += colBox.prefWidth + spacing;
				}
				
				
				// Now compare with what is required
				if ( minWidthAvailable  <  child.getReqMinWidth()  ||  prefWidthAvailable  <  child.getReqPrefWidth() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double colSpanRecip = 1.0 / (double)packing.colSpan;
					double additionalMinWidth = Math.max( child.getReqMinWidth() - minWidthAvailable, 0.0 );
					double additionalMinWidthPerColumn = additionalMinWidth * colSpanRecip;
					double additionalPrefWidth = Math.max( child.getReqPrefWidth() - prefWidthAvailable, 0.0 );
					double additionalPrefWidthPerColumn = additionalPrefWidth * colSpanRecip;
					
					for (int c = packing.x; c < endColumn; c++)
					{
						columnBoxes[c].minWidth += additionalMinWidthPerColumn;
						columnBoxes[c].prefWidth += additionalPrefWidthPerColumn;
					}
				}
			}
			
			i++;
		}
		
		for (LReqBox colBox: columnBoxes)
		{
			colBox.minHAdvance = colBox.minWidth;
			colBox.prefHAdvance = colBox.prefWidth;
		}

		return columnBoxes;
	}




	private static LReqBox[] computeRowYBoxes(LReqBoxInterface children[], TablePackingParams packingParams[], int childAllocationFlags[], int numRows, double rowSpacing)
	{
		double rowHeight[] = new double[numRows];
		double rowHeightAboveRef[] = new double[numRows];
		double rowHeightBelowRef[] = new double[numRows];
		boolean rowRefYAligned[] = new boolean[numRows];
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (LReqBoxInterface child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				VAlignment v = ElementAlignment.getVAlignment( childAllocationFlags[i] );
				int r = packing.y;
				
				double childReqHeight = child.getReqHeight();

				if ( v == VAlignment.REFY  ||  v == VAlignment.REFY_EXPAND )
				{
					double childRefY = child.getReqRefY();
					
					double childHeightAboveRef = childRefY;
					double childHeightBelowRef = childReqHeight - childRefY;

					rowHeight[r] = Math.max( rowHeight[r], childReqHeight );
					rowHeightAboveRef[r] = Math.max( rowHeightAboveRef[r], childHeightAboveRef );
					rowHeightBelowRef[r] = Math.max( rowHeightBelowRef[r], childHeightBelowRef );
					
					rowRefYAligned[r] = true;
				}
				else
				{
					rowHeight[r] = Math.max( rowHeight[r], childReqHeight );
				}
			}
			i++;
		}
		
		
		for (int r = 0; r < numRows; r++)
		{
			rowHeight[r] = Math.max( rowHeight[r], rowHeightAboveRef[r] + rowHeightBelowRef[r] );
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBoxInterface child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					double spacing = r != endRow-1  ?  rowSpacing  :  0.0;
					
					heightAvailable += rowHeight[r] + spacing;
				}
				
				
				// Now compare with what is required
				if ( heightAvailable  <  child.getReqHeight() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double additionalHeight = Math.max( child.getReqHeight() - heightAvailable, 0.0 );
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						rowHeight[r] += additionalHeightPerRow;
					}
				}
			}
			
			i++;
		}
		
		
		LReqBox rowBoxes[] = new LReqBox[numRows];
		
		for (int r = 0; r < numRows; r++)
		{
			double reqHeight = rowHeight[r];
			double reqHeightAboveRef = rowHeightAboveRef[r];
			
			if ( rowRefYAligned[r] )
			{
				rowBoxes[r] = new LReqBox( 0.0, 0.0, reqHeight, 0.0, reqHeightAboveRef );
			}
			else
			{
				rowBoxes[r] = new LReqBox( 0.0, 0.0, reqHeight, 0.0 );
			}
		}

		return rowBoxes;
	}
	
	
	
	public static LReqBox[] computeRequisitionX(LReqBoxInterface box, LReqBoxInterface children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing)
	{
		LReqBox columnBoxes[] = computeColumnXBoxes( children, packingParams, numColumns, columnSpacing );
		
		double minWidth = 0.0, prefWidth = 0.0;
		for (LReqBox colBox: columnBoxes)
		{
			minWidth += colBox.minWidth;
			prefWidth += colBox.prefWidth;
		}
		double spacing = columnSpacing * (double)Math.max( columnBoxes.length - 1, 0 );
		minWidth += spacing;
		prefWidth += spacing;
		
		box.setRequisitionX( minWidth, prefWidth, minWidth, prefWidth );
		
		return columnBoxes;
	}



	public static LReqBox[] computeRequisitionY(LReqBoxInterface box, LReqBoxInterface children[], TablePackingParams packingParams[], int childAllocationFlags[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing)
	{
		LReqBox rowBoxes[];
		
		rowBoxes = computeRowYBoxes( children, packingParams, childAllocationFlags, numRows, rowSpacing );
		
		double reqHeight = 0.0;
		for (LReqBox rowBox: rowBoxes)
		{
			reqHeight += rowBox.getReqHeight();
		}
		double spacing = rowSpacing * (double)Math.max( rowBoxes.length - 1, 0 );
		reqHeight += spacing;
		
		box.setRequisitionY( reqHeight, 0.0 );
		
		return rowBoxes;
	}
	
	
	
	
	public static void allocateX(LReqBoxInterface box, LReqBox columnBoxes[], LReqBoxInterface children[],
			LAllocBoxInterface allocBox, LAllocBox columnAllocBoxes[], LAllocBoxInterface childrenAlloc[], 
			TablePackingParams packingParams[], int childAlignmentFlags[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		HorizontalLayout.allocateX( box, columnBoxes, allocBox, columnAllocBoxes, columnSpacing, bColumnExpand );
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface childRequisition = children[i];
			LAllocBoxInterface childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];
			int alignmentFlags = childAlignmentFlags[i];
			HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
			
			int startCol = packing.x;
			int endCol = packing.x + packing.colSpan;
			LAllocBox startColAlloc = columnAllocBoxes[startCol], endColAlloc = columnAllocBoxes[endCol-1];
			double xStart = startColAlloc.positionInParentSpaceX;
			double xEnd = endColAlloc.positionInParentSpaceX  +  endColAlloc.allocationX;
			double widthAvailable = xEnd - xStart;
			double cellWidth = Math.max( widthAvailable, childRequisition.getReqMinWidth() );

			LAllocHelper.allocateChildXAligned( childAlloc, childRequisition, hAlign, xStart, cellWidth );
		}
	}
	

	
	public static void allocateY(LReqBoxInterface box, LReqBox rowBoxes[], LReqBoxInterface children[],
			LAllocBoxInterface allocBox, LAllocBox rowAllocBoxes[], LAllocBoxInterface childrenAlloc[],
			TablePackingParams packingParams[], int childAlignmentFlags[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
		
		LAllocV rowH[] = new LAllocV[numRows];
		for (int r = 0; r < numRows; r++)
		{
			rowH[r] = HorizontalLayout.computeVerticalAllocationForRow( rowBoxes[r], rowAllocBoxes[r] );
		}
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface childRequisition = children[i];
			LAllocBoxInterface childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];
			int alignmentFlags = childAlignmentFlags[i];
			VAlignment vAlign = ElementAlignment.getVAlignment( alignmentFlags );

			int startRow = packing.y;
			int endRow = packing.y + packing.rowSpan;
			LAllocBox startRowAlloc = rowAllocBoxes[startRow];
			double yStart = startRowAlloc.positionInParentSpaceY;

			LAllocV height = null;
			if ( packing.rowSpan == 1 )
			{
				height = rowH[packing.y];
			}
			else
			{
				LAllocBox endRowAlloc = rowAllocBoxes[endRow-1];
				double yEnd = endRowAlloc.positionInParentSpaceY + endRowAlloc.getAllocationY();
				double heightAvailable = yEnd - yStart;
				vAlign = VAlignment.noRefPoint( vAlign );
				height = new LAllocV( heightAvailable );
			}
				
			LAllocHelper.allocateChildYAligned( childAlloc, childRequisition, vAlign, yStart, height );
		}
	}
}
