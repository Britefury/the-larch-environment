//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;




public class TableLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBox children[], TablePackingParams packingParams[], int numColumns, double columnSpacing)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		int i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan == 1 )
			{
				LReqBox b = columnBoxes[packing.x];
				b.minWidth = Math.max( b.minWidth, child.minWidth );
				b.prefWidth = Math.max( b.prefWidth, child.prefWidth );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBox child: children)
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
				if ( minWidthAvailable  <  child.minWidth  ||  prefWidthAvailable  <  child.prefWidth )
				{
					// Need more width; compute how much we need, and distribute among columns
					double colSpanRecip = 1.0 / (double)packing.colSpan;
					double additionalMinWidth = Math.max( child.minWidth - minWidthAvailable, 0.0 );
					double additionalMinWidthPerColumn = additionalMinWidth * colSpanRecip;
					double additionalPrefWidth = Math.max( child.prefWidth - prefWidthAvailable, 0.0 );
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

		return columnBoxes;
	}




	private static LReqBox[] computeRowYBoxes(LReqBox children[], TablePackingParams packingParams[], int childAllocationFlags[], int numRows, double rowSpacing)
	{
		double rowAscent[] = new double[numRows];
		double rowDescent[] = new double[numRows];
		double rowHeight[] = new double[numRows];
		boolean rowHasBaseline[] = new boolean[numRows];
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (LReqBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				VAlignment v = ElementAlignment.getVAlignment( childAllocationFlags[i] );
				
				int r = packing.y;
				boolean bBaseline = v == VAlignment.BASELINES  ||  v == VAlignment.BASELINES_EXPAND;
				if ( bBaseline )
				{
					rowHasBaseline[r] = true;
				}
				
				if ( bBaseline  &&  child.hasBaseline() )
				{
					rowAscent[r] = Math.max( rowAscent[r], child.reqAscent );
					rowDescent[r] = Math.max( rowDescent[r], child.reqDescent );
				}
				else
				{
					rowHeight[r] = Math.max( rowHeight[r], child.getReqHeight() );
				}
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (LReqBox child: children)
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
			if ( rowHasBaseline[r] )
			{
				double reqHeight = rowHeight[r];
				double reqAscent = rowAscent[r];
				double reqDescent = rowDescent[r];
				if ( reqHeight  >  ( reqAscent + reqDescent ) )
				{
					double deltaY = ( reqHeight  -  ( reqAscent + reqDescent ) )  *  0.5;
					rowBoxes[r] = new LReqBox( 0.0, 0.0, reqAscent + deltaY, reqDescent + deltaY, 0.0 );
				}
				else
				{
					rowBoxes[r] = new LReqBox( 0.0, 0.0, reqAscent, reqDescent, 0.0 );
				}
			}
			else
			{
				rowBoxes[r] = new LReqBox( 0.0, 0.0, rowHeight[r], 0.0 );
			}
		}

		return rowBoxes;
	}
	
	
	
	public static LReqBox[] computeRequisitionX(LReqBox box, LReqBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
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
		
		box.setRequisitionX( minWidth, prefWidth, 0.0, 0.0 );
		
		return columnBoxes;
	}



	public static LReqBox[] computeRequisitionY(LReqBox box, LReqBox children[], TablePackingParams packingParams[], int childAllocationFlags[], int numColumns, int numRows,
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
	
	
	
	
	public static void allocateX(LReqBox box, LReqBox columnBoxes[], LReqBox children[],
			LAllocBox allocBox, LAllocBox columnAllocBoxes[], LAllocBox childrenAlloc[], 
			TablePackingParams packingParams[], int childAlignmentFlags[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		HorizontalLayout.allocateX( box, columnBoxes, allocBox, columnAllocBoxes, columnSpacing, bColumnExpand );
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBox childRequisition = children[i];
			LAllocBox childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];
			int alignmentFlags = childAlignmentFlags[i];
			HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
			
			int startCol = packing.x;
			int endCol = packing.x + packing.colSpan;
			LAllocBox startColAlloc = columnAllocBoxes[startCol], endColAlloc = columnAllocBoxes[endCol-1];
			double xStart = startColAlloc.positionInParentSpaceX;
			double xEnd = endColAlloc.positionInParentSpaceX  +  endColAlloc.allocationX;
			double widthAvailable = xEnd - xStart;
			double cellWidth = Math.max( widthAvailable, childRequisition.minWidth );

			allocBox.allocateChildXAligned( childAlloc, childRequisition, hAlign, xStart, cellWidth );
		}
	}
	


	
	
	
	
	public static void allocateY(LReqBox box, LReqBox rowBoxes[], LReqBox children[],
			LAllocBox allocBox, LAllocBox rowAllocBoxes[], LAllocBox childrenAlloc[],
			TablePackingParams packingParams[], int childAlignmentFlags[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
		
		// Allocate children
		for (int i = 0; i < children.length; i++)
		{
			LReqBox childRequisition = children[i];
			LAllocBox childAlloc = childrenAlloc[i];
			TablePackingParams packing = (TablePackingParams)packingParams[i];
			int alignmentFlags = childAlignmentFlags[i];
			VAlignment vAlign = ElementAlignment.getVAlignment( alignmentFlags );

			int startRow = packing.y;
			int endRow = packing.y + packing.rowSpan;
			if ( packing.rowSpan != 1 )
			{
				vAlign = VAlignment.noBaselines( vAlign );
			}
			LAllocBox startRowAlloc = rowAllocBoxes[startRow], endRowAlloc = rowAllocBoxes[endRow-1];
			double yStart = startRowAlloc.positionInParentSpaceY;
			double yEnd = endRowAlloc.positionInParentSpaceY + endRowAlloc.getAllocationY();
			double heightAvailable = yEnd - yStart;
				
			allocBox.allocateChildYAligned( childAlloc, childRequisition, vAlign, yStart, new LAllocV( heightAvailable ) );
		}
	}
}
