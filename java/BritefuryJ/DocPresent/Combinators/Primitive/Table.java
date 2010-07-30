//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Table extends Pres
{
	public static class TableCell
	{
		private Pres child;
		private int colSpan, rowSpan;
		
		
		public TableCell(Pres child, int colSpan, int rowSpan)
		{
			this.child = child;
			this.colSpan = colSpan;
			this.rowSpan = rowSpan;
		}

		public TableCell(Object child, int colSpan, int rowSpan)
		{
			this( Pres.coerce( child ), colSpan, rowSpan );
		}
	}
	
	
	
	private TableCell childCells[][];
	
	
	public Table()
	{
		childCells = new TableCell[0][];
	}
	
	public Table(Object children[][])
	{
		childCells = new TableCell[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			Object row[] = children[y];
			childCells[y] = new TableCell[row.length];
			for (int x = 0; x < row.length; x++)
			{
				childCells[y][x] = new TableCell( coerce( row[x] ), 1, 1 );
			}
		}
	}
	
	public Table(TableCell children[][])
	{
		childCells = new TableCell[children.length][];
		for (int y = 0; y < children.length; y++)
		{
			TableCell row[] = children[y];
			childCells[y] = new TableCell[row.length];
			System.arraycopy( row, 0, this.childCells[y], 0, row.length );
		}
	}
	
	
	public void put(int x, int y, Pres child)
	{
		put( x, y, 1, 1, child );
	}
	
	public void put(int x, int y, int colSpan, int rowSpan, Pres child)
	{
		if ( y >= childCells.length  &&  child != null )
		{
			// We need to expand the number of rows
			TableCell newRows[][] = new TableCell[y+1][];
			System.arraycopy( childCells, 0, newRows, 0, childCells.length );
			
			for (int r = childCells.length; r <= y; r++)
			{
				newRows[r] = new TableCell[0];
			}
			childCells = newRows;
		}
		
		
		TableCell row[] = childCells[y];
		if ( x >= row.length  &&  child != null )
		{
			// We need to expand the number of columns in this row
			TableCell newCols[] = new TableCell[x+1];
			System.arraycopy( row, 0, newCols, 0, row.length );
			
			for (int c = row.length; c <= x; c++)
			{
				newCols[c] = null;
			}

			row = newCols;
			childCells[y] = row;
		}
		
		
		row[x] = child != null  ?  new TableCell( child, colSpan, rowSpan )  :  null;
		
		
		if ( child == null )
		{
			// We removed a child
			
			if ( x == ( row.length - 1 ) )
			{
				// We removed a child from the end of the row; shorten it
				
				int numColumns = 0;
				for (int c = 0; c < row.length; c++)
				{
					if ( row[c].child != null )
					{
						numColumns = Math.max( numColumns, c + 1 );
					}
				}
				TableCell newRow[] = new TableCell[numColumns];
				System.arraycopy( row, 0, newRow, 0, numColumns );
				childCells[y] = newRow;
				row = newRow;
				
				
				if ( y == ( childCells.length - 1 )  &&  numColumns == 0 )
				{
					// The row we just shortened is empty, and its the last row. Shorten the row list accordingly
					int numRows = 0;
					for (int r = 0; r < childCells.length; r++)
					{
						if ( childCells[r].length > 0 )
						{
							numRows = Math.max( numRows, r + 1 );
						}
					}
					
					TableCell newCells[][] = new TableCell[numRows][];
					System.arraycopy( childCells, 0, newCells, 0, numRows );
					childCells = newCells;
				}
			}
		}
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleValues childStyle = Primitive.useContainerParams.get( style );		
		DPTable element = new DPTable( Primitive.tableParams.get( style ) );
		if ( childCells != null )
		{
			DPTable.TableCell elemCells[][] = new DPTable.TableCell[childCells.length][];
			for (int y = 0; y < childCells.length; y++)
			{
				TableCell row[] = childCells[y];
				DPTable.TableCell elemRow[] = new DPTable.TableCell[row.length];
				elemCells[y] = elemRow;
				for (int x = 0; x < row.length; x++)
				{
					TableCell cell = row[x];
					elemRow[x] = cell != null  ?  new DPTable.TableCell( cell.child.present( ctx, childStyle ).layoutWrap(), cell.colSpan, cell.rowSpan )  :  null;  
				}
			}
			element.setCells( elemCells );
		}
		return element;
	}
}
