//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;




public class TablePack
{
	private static TSBox[] computeColumnXBoxes(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX)
	{
		TSBox columnBoxes[] = new TSBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new TSBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		int i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan == 1 )
			{
				double totalPad = packing.paddingX * 2.0;
				TSBox b = columnBoxes[packing.x];
				b.minWidth = Math.max( b.minWidth, child.minWidth + totalPad );
				b.prefWidth = Math.max( b.prefWidth, child.prefWidth + totalPad );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.colSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endColumn = packing.x + packing.colSpan;
				
				double minWidthAvailable = 0.0, prefWidthAvailable = 0.0;
				for (int c = packing.x; c < endColumn; c++)
				{
					TSBox colBox = columnBoxes[c];
					
					double spacing = c != endColumn-1  ?  spacingX  :  0.0;
					
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




	private static TSBox[] computeRowYBoxes(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingY)
	{
		TSBox rowBoxes[] = new TSBox[numRows];
		for (int i = 0; i < numColumns; i++)
		{
			rowBoxes[i] = new TSBox();
		}
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				double totalPad = packing.paddingY * 2.0;
				TSBox b = rowBoxes[packing.y];
				b.setRequisitionY( Math.max( b.getReqHeight(), child.getReqHeight() + totalPad ),  0.0 );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					TSBox rowBox = rowBoxes[r];
					
					double spacing = r != endRow-1  ?  spacingY  :  0.0;
					
					heightAvailable += rowBox.getReqHeight() + spacing;
				}
				
				
				// Now compare with what is required
				if ( heightAvailable  <  child.getReqHeight() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double additionalHeight = Math.max( child.getReqHeight() - heightAvailable, 0.0 );
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						TSBox rowBox = rowBoxes[r];
						rowBox.setRequisitionY( rowBox.getReqHeight() + additionalHeightPerRow, 0.0 );
					}
				}
			}
			
			i++;
		}

		return rowBoxes;
	}





	private static TSBox[] computeRowYBoxesWithBaselines(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingY)
	{
		TSBox rowBoxes[] = new TSBox[numRows];
		for (int i = 0; i < numColumns; i++)
		{
			rowBoxes[i] = new TSBox();
			rowBoxes[i].bHasBaseline = true;
		}
		
		
		// First phase; fill only with children who span 1 row
		int i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan == 1 )
			{
				TSBox b = rowBoxes[packing.y];
				
				double ascent, descent;

				if ( child.bHasBaseline )
				{
					ascent = child.reqAscent + packing.paddingY;
					descent = child.reqDescent + packing.paddingY;
				}
				else
				{
					ascent = descent = child.getReqHeight() * 0.5 + packing.paddingY;
				}

				b.reqAscent = Math.max( b.reqAscent, ascent );
				b.reqDescent = Math.max( b.reqDescent, descent );
			}
			i++;
		}
		
		
		// Second phase; fill with children who span >1 columns
		i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = packingParams[i];
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					TSBox rowBox = rowBoxes[r];
					
					double spacing = r != endRow-1  ?  spacingY  :  0.0;
					
					heightAvailable += rowBox.getReqHeight() + spacing;
				}
				
				
				// Now compare with what is required
				if ( heightAvailable  <  child.getReqHeight() )
				{
					// Need more width; compute how much we need, and distribute among columns
					double additionalHeight = Math.max( child.getReqHeight() - heightAvailable, 0.0 );
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						TSBox rowBox = rowBoxes[r];
						rowBox.reqDescent += additionalHeightPerRow;
					}
				}
			}
			
			i++;
		}

		return rowBoxes;
	}
	
	
	
	public static TSBox[] computeRequisitionX(TSBox box, TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		TSBox columnBoxes[] = computeColumnXBoxes( children, packingParams, numColumns, numRows, spacingX );
		
		double minWidth = 0.0, prefWidth = 0.0;
		for (TSBox colBox: columnBoxes)
		{
			minWidth += colBox.minWidth;
			prefWidth += colBox.prefWidth;
		}
		double spacing = spacingX * (double)Math.max( columnBoxes.length - 1, 0 );
		minWidth += spacing;
		prefWidth += spacing;
		
		box.setRequisitionX( minWidth, prefWidth, 0.0, 0.0 );
		
		return columnBoxes;
	}



	public static TSBox[] computeRequisitionY(TSBox box, TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		TSBox rowBoxes[];
		
		if ( rowAlignment == VAlignment.BASELINES )
		{
			rowBoxes = computeRowYBoxesWithBaselines( children, packingParams, numColumns, numRows, spacingX );
		}
		else
		{
			rowBoxes = computeRowYBoxes( children, packingParams, numColumns, numRows, spacingX );
		}
		
		double reqHeight = 0.0;
		for (TSBox rowBox: rowBoxes)
		{
			reqHeight += rowBox.getReqHeight();
		}
		double spacing = spacingY * (double)Math.max( rowBoxes.length - 1, 0 );
		reqHeight += spacing;
		
		box.setRequisitionY( reqHeight, 0.0 );
		
		return rowBoxes;
	}
	
	
	
	
	public static void allocateX(TSBox box, TSBox columnBoxes[], TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		// Allocate space to the columns
		HorizontalPack.allocateX( box, columnBoxes, spacingX, bExpandX );
		
		// Allocate children
		int i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = (TablePackingParams)packingParams[i];

			int startCol = packing.x;
			int endCol = packing.x + packing.colSpan;
			TSBox startColBox = columnBoxes[startCol], endColBox = columnBoxes[endCol-1];
			double xStart = startColBox.positionInParentSpaceX + packing.paddingX;
			double xEnd = endColBox.positionInParentSpaceX  +  endColBox.allocationX - packing.paddingX;
			double widthAvailable = xEnd - xStart;
			double cellWidth = Math.max( widthAvailable, child.minWidth );
			
			if ( cellWidth <= child.prefWidth )
			{
				box.allocateChildX( child, xStart, cellWidth );
			}
			else
			{
				if ( colAlignment == HAlignment.LEFT )
				{
					box.allocateChildX( child, xStart, child.prefWidth );
				}
				else if ( colAlignment == HAlignment.RIGHT )
				{
					box.allocateChildX( child, Math.max( xEnd - child.prefWidth, 0.0 ), child.prefWidth );
				}
				else if ( colAlignment == HAlignment.CENTRE )
				{
					box.allocateChildX( child, Math.max( xStart + ( widthAvailable - child.prefWidth ) * 0.5, 0.0 ), child.prefWidth );
				}
				else if ( colAlignment == HAlignment.EXPAND )
				{
					box.allocateChildX( child, xStart, cellWidth );
				}
			}
			
			i++;
		}
	}
	


	
	
	
	
	public static void allocateY(TSBox box, TSBox rowBoxes[], TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows,
			double spacingX, double spacingY, boolean bExpandX, boolean bExpandY, HAlignment colAlignment, VAlignment rowAlignment)
	{
		// Allocate space to the columns
		VerticalPack.allocateY( box, rowBoxes, spacingY, bExpandY );
		
		// Allocate children
		int i = 0;
		for (TSBox child: children)
		{
			TablePackingParams packing = (TablePackingParams)packingParams[i];

			if ( packing.rowSpan == 1  &&  rowAlignment == VAlignment.BASELINES  &&  child.bHasBaseline  &&  rowBoxes[packing.y].bHasBaseline )
			{
				TSBox rowBox = rowBoxes[packing.y];

				double yOffset = rowBox.reqAscent - child.reqAscent;			// Row ascent includes padding; so yOffset also includes padding
				double yStart = rowBox.positionInParentSpaceY;
				double yPos = yStart + Math.max( yOffset, 0.0 );
				box.allocateChildY( child, yPos, child.getReqHeight() );
			}
			else
			{
				int startRow = packing.y;
				int endRow = packing.y + packing.rowSpan;
				TSBox startRowBox = rowBoxes[startRow], endRowBox = rowBoxes[endRow-1];
				double yStart = startRowBox.positionInParentSpaceY + packing.paddingY;
				double yEnd = endRowBox.positionInParentSpaceY + endRowBox.allocationY - packing.paddingY;
				double heightAvailable = yEnd - yStart;
				double reqHeight = child.getReqHeight();
				double cellHeight = Math.max( heightAvailable, reqHeight );
				
				if ( rowAlignment == VAlignment.TOP )
				{
					box.allocateChildY( child, yStart, reqHeight );
				}
				else if ( rowAlignment == VAlignment.BOTTOM )
				{
					box.allocateChildY( child, Math.max( yEnd - reqHeight, 0.0 ), reqHeight );
				}
				else if ( rowAlignment == VAlignment.CENTRE )
				{
					box.allocateChildY( child, Math.max( yStart + ( heightAvailable - reqHeight ) * 0.5, 0.0 ), reqHeight );
				}
				else if ( rowAlignment == VAlignment.EXPAND )
				{
					box.allocateChildY( child, yStart, cellHeight );
				}
			}
			
			i++;
		}
	}
}
