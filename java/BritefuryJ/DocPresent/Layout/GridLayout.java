//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class GridLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBoxInterface children[][], int numColumns, double columnSpacing)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		for (LReqBoxInterface row[]: children)
		{
			int c = 0;
			for (LReqBoxInterface child: row)
			{
				if ( child != null )
				{
					LReqBox b = columnBoxes[c];
					b.minWidth = Math.max( b.minWidth, child.getMinWidth() );
					b.prefWidth = Math.max( b.prefWidth, child.getPrefWidth() );
				}
				c++;
			}
		}
		
		
		for (LReqBox colBox: columnBoxes)
		{
			colBox.minHAdvance = colBox.minWidth;
			colBox.prefHAdvance = colBox.prefWidth;
		}
		
		return columnBoxes;
	}




	public static void computeRowRequisitionY(LReqBox rowBox, LReqBoxInterface children[], int childAlignmentFlags[])
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
				double childRefY = child.getRefY();
				double childHeightAboveRef = childRefY;
				double childHeightBelowRef = childHeight - childRefY;
				
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


	
	public static void allocateRowY(LReqBox reqBox, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAlignmentFlags[])
	{
		LAllocV h = HorizontalLayout.computeVerticalAllocationForRow( reqBox, allocBox );
		
		for (int i = 0; i < children.length; i++)
		{
			if ( children != null )
			{
				allocBox.allocateChildYAligned( childrenAlloc[i], children[i], childAlignmentFlags[i], 0.0, h );
			}
		}
	}

	
	
	public static LReqBox[] computeRequisitionX(LReqBox box, LReqBoxInterface children[][], int numColumns, int numRows, double columnSpacing, double rowSpacing)
	{
		LReqBox columnBoxes[] = computeColumnXBoxes( children, numColumns, columnSpacing );
		
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



	public static void computeRequisitionY(LReqBox box, LReqBoxInterface rowBoxes[], double rowSpacing)
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
	
	
	
	
	public static void allocateX(LReqBox box, LReqBoxInterface columnBoxes[], LReqBoxInterface children[][],
			LAllocBox allocBox, LAllocBoxInterface columnAllocBoxes[], LAllocBoxInterface childrenAlloc[][], 
			int childAlignmentFlags[][], int numColumns, int numRows,
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

			for (int c = 0; c < rowRequisition.length; c++)
			{
				LReqBoxInterface childRequisition = rowRequisition[c];
				if ( childRequisition != null )
				{
					LAllocBoxInterface childAlloc = rowAlloc[c];
					int alignmentFlags = rowAlignmentFlags[c];
					HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
					
					LAllocBoxInterface colAlloc = columnAllocBoxes[c];
					double cellWidth = Math.max( colAlloc.getAllocationX(), childRequisition.getMinWidth() );
		
					allocBox.allocateChildXAligned( childAlloc, childRequisition, hAlign, colAlloc.getPositionInParentSpaceX(), cellWidth );
				}
			}
		}
	}
	

	
	public static void allocateY(LReqBox box, LReqBoxInterface rowBoxes[], LAllocBox allocBox, LAllocBoxInterface rowAllocBoxes[], double rowSpacing, boolean bRowExpand)
	{
		// Allocate space to the rows
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
	}
}
