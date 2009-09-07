//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class GridLayout
{
	private static LReqBox[] computeColumnXBoxes(LReqBox children[][], int numColumns, double columnSpacing)
	{
		LReqBox columnBoxes[] = new LReqBox[numColumns];
		for (int i = 0; i < numColumns; i++)
		{
			columnBoxes[i] = new LReqBox();
		}
		
		
		// First phase; fill only with children who span 1 column
		for (LReqBox row[]: children)
		{
			int c = 0;
			for (LReqBox child: row)
			{
				if ( child != null )
				{
					LReqBox b = columnBoxes[c];
					b.minWidth = Math.max( b.minWidth, child.minWidth );
					b.prefWidth = Math.max( b.prefWidth, child.prefWidth );
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




	public static void computeRowRequisitionY(LReqBox rowBox, LReqBox children[], int childAlignmentFlags[])
	{
		// First phase; fill only with children who span 1 row
		double rowAscent = 0.0, rowDescent = 0.0, rowHeight = 0.0;
		boolean bHasBaseline = false;

		int c = 0;
		for (LReqBox child: children)
		{
			if ( child != null )
			{
				VAlignment v = ElementAlignment.getVAlignment( childAlignmentFlags[c] );
				
				boolean bBaseline = v == VAlignment.BASELINES  ||  v == VAlignment.BASELINES_EXPAND;
				if ( bBaseline )
				{
					bHasBaseline = true;
				}
				
				if ( bBaseline  &&  child.hasBaseline() )
				{
					rowAscent = Math.max( rowAscent, child.reqAscent );
					rowDescent = Math.max( rowDescent, child.reqDescent );
				}
				else
				{
					rowHeight = Math.max( rowHeight, child.getReqHeight() );
				}
			}
			
			c++;
		}
		
		if ( bHasBaseline )
		{
			if ( rowHeight  >  ( rowAscent + rowDescent ) )
			{
				double deltaY = ( rowHeight  -  ( rowAscent + rowDescent ) )  *  0.5;
				rowBox.setRequisitionY( rowAscent + deltaY, rowDescent + deltaY, 0.0 );
			}
			else
			{
				rowBox.setRequisitionY( rowAscent, rowDescent, 0.0 );
			}
		}
		else
		{
			rowBox.setRequisitionY( rowHeight, 0.0 );
		}
	}


	
	public static void allocateRowY(LReqBox reqBox, LReqBox children[],	LAllocBox allocBox, LAllocBox childrenAlloc[], int childAlignmentFlags[])
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

	
	
	public static LReqBox[] computeRequisitionX(LReqBox box, LReqBox children[][], int numColumns, int numRows, double columnSpacing, double rowSpacing)
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



	public static void computeRequisitionY(LReqBox box, LReqBox rowBoxes[], double rowSpacing)
	{
		// Total space required by rows
		double reqHeight = 0.0;
		for (LReqBox rowBox: rowBoxes)
		{
			reqHeight += rowBox.getReqHeight();
		}
		double spacing = rowSpacing * (double)Math.max( rowBoxes.length - 1, 0 );
		reqHeight += spacing;
		
		box.setRequisitionY( reqHeight, 0.0 );
	}
	
	
	
	
	public static void allocateX(LReqBox box, LReqBox columnBoxes[], LReqBox children[][],
			LAllocBox allocBox, LAllocBox columnAllocBoxes[], LAllocBox childrenAlloc[][], 
			int childAlignmentFlags[][], int numColumns, int numRows,
			double columnSpacing, double rowSpacing, boolean bColumnExpand, boolean bRowExpand)
	{
		// Allocate space to the columns
		HorizontalLayout.allocateX( box, columnBoxes, allocBox, columnAllocBoxes, columnSpacing, bColumnExpand );
		
		// Allocate children
		for (int r = 0; r < children.length; r++)
		{
			LReqBox rowRequisition[] = children[r];
			LAllocBox rowAlloc[] = childrenAlloc[r];
			int rowAlignmentFlags[] = childAlignmentFlags[r];

			for (int c = 0; c < rowRequisition.length; c++)
			{
				LReqBox childRequisition = rowRequisition[c];
				if ( childRequisition != null )
				{
					LAllocBox childAlloc = rowAlloc[c];
					int alignmentFlags = rowAlignmentFlags[c];
					HAlignment hAlign = ElementAlignment.getHAlignment( alignmentFlags );
					
					LAllocBox colAlloc = columnAllocBoxes[c];
					double cellWidth = Math.max( colAlloc.allocationX, childRequisition.minWidth );
		
					allocBox.allocateChildXAligned( childAlloc, childRequisition, hAlign, colAlloc.positionInParentSpaceX, cellWidth );
				}
			}
		}
	}
	

	
	public static void allocateY(LReqBox box, LReqBox rowBoxes[], LAllocBox allocBox, LAllocBox rowAllocBoxes[], double rowSpacing, boolean bRowExpand)
	{
		// Allocate space to the rows
		VerticalLayout.allocateY( box, rowBoxes, allocBox, rowAllocBoxes, rowSpacing, bRowExpand );
	}
}
