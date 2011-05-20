//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class GridLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBoxInterface children[][], boolean bRowIsGridRow[], int numColumns, double columnSpacing)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// Handle rows that are grid rows
		int r = 0;
		for (LReqBoxInterface row[]: children)
		{
			int c = 0;
			if ( bRowIsGridRow[r] )
			{
				for (LReqBoxInterface child: row)
				{
					if ( child != null )
					{
						LReqBox b = columnBoxes[c];
						b.minWidth = Math.max( b.minWidth, child.getReqMinWidth() );
						b.prefWidth = Math.max( b.prefWidth, child.getReqPrefWidth() );
					}
					c++;
				}
			}
			r++;
		}
		
		
		for (LReqBox colBox: columnBoxes)
		{
			colBox.minHAdvance = colBox.minWidth;
			colBox.prefHAdvance = colBox.prefWidth;
		}
		
		return columnBoxes;
	}




	public static void computeRowRequisitionY(LReqBoxInterface rowBox, LReqBoxInterface children[], int childAlignmentFlags[])
	{
		rowBox.clearRequisitionY();
		double rowHeight = 0.0, rowHeightAboveRef = 0.0,  rowHeightBelowRef = 0.0;
		boolean bRefYAligned = false;
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface child = children[i];
			VAlignment v = ElementAlignment.getVAlignment( childAlignmentFlags[i] );
			
			double childHeight = child.getReqHeight();
			
			if ( v == VAlignment.REFY  ||  v == VAlignment.REFY_EXPAND )
			{
				double childHeightAboveRef = child.getReqRefY();
				double childHeightBelowRef = childHeight - childHeightAboveRef;
				
				rowHeight = Math.max( rowHeight, childHeight );
				rowHeightAboveRef = Math.max( rowHeightAboveRef, childHeightAboveRef );
				rowHeightBelowRef = Math.max( rowHeightBelowRef, childHeightBelowRef );
				
				bRefYAligned = true;
			}
			else
			{
				rowHeight = Math.max( rowHeight, childHeight );
			}
		}
		rowHeight = Math.max( rowHeight, rowHeightAboveRef + rowHeightBelowRef );
		
		if ( bRefYAligned )
		{
			rowBox.setRequisitionY( rowHeight, 0.0, rowHeightAboveRef );
		}
		else
		{
			rowBox.setRequisitionY( rowHeight, 0.0 );
		}
	}


	
	public static void allocateRowY(LReqBoxInterface reqBox, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAlignmentFlags[])
	{
		LAllocV h = HorizontalLayout.computeVerticalAllocationForRow( reqBox, allocBox );
		
		for (int i = 0; i < children.length; i++)
		{
			if ( children != null )
			{
				LAllocHelper.allocateChildYAligned( childrenAlloc[i], children[i], childAlignmentFlags[i], 0.0, h );
			}
		}
	}

	
	
	public static LReqBox[] computeRequisitionX(LReqBoxInterface box, LReqBoxInterface children[][], boolean bRowIsGridRow[], int numColumns, int numRows, double columnSpacing, double rowSpacing)
	{
		LReqBox columnBoxes[] = computeColumnXBoxes( children, bRowIsGridRow, numColumns, columnSpacing );
		
		double minWidth = 0.0, prefWidth = 0.0;
		for (LReqBox colBox: columnBoxes)
		{
			minWidth += colBox.minWidth;
			prefWidth += colBox.prefWidth;
		}
		double spacing = columnSpacing * (double)Math.max( columnBoxes.length - 1, 0 );
		minWidth += spacing;
		prefWidth += spacing;
		
		// For all rows that consist of a single child, which was not contained in a GridRow, compute the additional space required (if any) to fit
		int r = 0;
		double minExtra = 0.0, prefExtra = 0.0;
		for (LReqBoxInterface row[]: children)
		{
			if ( !bRowIsGridRow[r] )
			{
				LReqBoxInterface child = row[0];
				if ( child != null )
				{
					minExtra = Math.max( minExtra, Math.max( child.getReqMinWidth() - minWidth, 0.0 ) );
					prefExtra = Math.max( prefExtra, Math.max( child.getReqPrefWidth() - prefWidth, 0.0 ) );
				}
			}
			r++;
		}
		

		// Distribute any additional width among all columns
		if ( minExtra > 0.0  ||  prefExtra > 0.0 )
		{
			double numColumnsRecip = 1.0 / columnBoxes.length;
			minExtra *= numColumnsRecip;
			prefExtra *= numColumnsRecip;
			for (LReqBox colBox: columnBoxes)
			{
				colBox.minWidth += minExtra;
				colBox.minHAdvance += minExtra;
				colBox.prefWidth += prefExtra;
				colBox.prefHAdvance += prefExtra;
			}
			minWidth += minExtra;
			prefWidth += prefExtra;
		}

		
		box.setRequisitionX( minWidth, prefWidth, minWidth, prefWidth );
		
		return columnBoxes;
	}



	public static void computeRequisitionY(LReqBoxInterface box, LReqBoxInterface rowBoxes[], double rowSpacing)
	{
		// Total space required by rows
		double reqHeight = 0.0;
		for (LReqBoxInterface rowBox: rowBoxes)
		{
			reqHeight += rowBox.getReqHeight();
		}
		double spacing = rowSpacing * (double)Math.max( rowBoxes.length - 1, 0 );
		reqHeight += spacing;
		
		box.setRequisitionY( reqHeight, 0.0 );
	}
	
	
	
	
	public static void allocateX(LReqBoxInterface box, LReqBoxInterface columnBoxes[], LReqBoxInterface children[][],
			LAllocBoxInterface allocBox, LAllocBoxInterface columnAllocBoxes[], LAllocBoxInterface childrenAlloc[][], 
			int childAlignmentFlags[][], boolean bRowIsGridRow[], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		HorizontalLayout.allocateX( box, columnBoxes, allocBox, columnAllocBoxes, columnSpacing, bColumnExpand );
		
		// Allocate children
		for (int r = 0; r < children.length; r++)
		{
			LReqBoxInterface rowRequisition[] = children[r];
			LAllocBoxInterface rowAlloc[] = childrenAlloc[r];
			int rowAlignmentFlags[] = childAlignmentFlags[r];

			if ( bRowIsGridRow[r] )
			{
				for (int c = 0; c < rowRequisition.length; c++)
				{
					LReqBoxInterface childRequisition = rowRequisition[c];
					if ( childRequisition != null )
					{
						LAllocBoxInterface childAlloc = rowAlloc[c];
						int alignmentFlags = rowAlignmentFlags[c];
						HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
						
						LAllocBoxInterface colAlloc = columnAllocBoxes[c];
						double cellWidth = Math.max( colAlloc.getAllocationX(), childRequisition.getReqMinWidth() );
			
						LAllocHelper.allocateChildXAligned( childAlloc, childRequisition, hAlign, colAlloc.getAllocPositionInParentSpaceX(), cellWidth );
					}
				}
			}
			else
			{
				LReqBoxInterface childRequisition = rowRequisition[0];
				if ( childRequisition != null )
				{
					LAllocBoxInterface childAlloc = rowAlloc[0];
					int alignmentFlags = rowAlignmentFlags[0];
					HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
					
					double cellWidth = Math.max( allocBox.getAllocationX(), childRequisition.getReqMinWidth() );
		
					LAllocHelper.allocateChildXAligned( childAlloc, childRequisition, hAlign, 0.0, cellWidth );
				}
			}
		}
	}
	

	
	public static void allocateY(LReqBoxInterface box, LReqBoxInterface rowBoxes[], LAllocBoxInterface allocBox, LAllocBoxInterface rowAllocBoxes[], double rowSpacing, boolean bRowExpand)
	{
		// Allocate space to the rows
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
	}
}
