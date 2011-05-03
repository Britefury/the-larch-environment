//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import java.util.ArrayList;
import java.util.List;

public class GenericTableModel implements GenericTableModelInterface
{
	public static interface CellFactory
	{
		Object createCell();
		Object copyCell(Object cell);
	}
	
	
	private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
	private CellFactory cellFactory;
	
	
	
	
	public GenericTableModel(CellFactory cellFactory)
	{
		this.cellFactory = cellFactory;
	}

	
	@Override
	public int getWidth()
	{
		int width = 0;
		for (ArrayList<Object> row: data)
		{
			width = Math.max( width, row.size() );
		}
		return width;
	}

	@Override
	public int getHeight()
	{
		return data.size();
	}

	@Override
	public List<?> getRowCells(int rowIndex)
	{
		return data.get( rowIndex );
	}

	@Override
	public Object[][] getBlock(int x, int y, int w, int h)
	{
		Object[][] block = new Object[h][];
		for (int j = y; j < y + h; j++)
		{
			ArrayList<Object> srcRow = data.get( j );
			int numCells = Math.max( 0, Math.min( w, srcRow.size() - x ) );
			Object destRow[] = new Object[numCells];
			if ( numCells > 0 )
			{
				System.arraycopy( srcRow.toArray(), x, destRow, 0, numCells );
			}
			block[j-y] = destRow;
		}
		
		return block;
	}

	@Override
	public void putBlock(int x, int y, Object[][] block)
	{
		int blockHeight = block.length;
		
		growHeight( y + blockHeight );
		
		for (int j = y; j < y + blockHeight; j++)
		{
			Object srcRow[] = block[j-y];
			ArrayList<Object> destRow = data.get( j );
			
			int rowWidth = srcRow.length;
			
			growRowWidth( destRow, x + rowWidth );
			
			for (int i = x; i < x + rowWidth; i++)
			{
				destRow.set( i, cellFactory.copyCell( srcRow[i-x] ) );
			}
		}
	}
	
	
	
	private void growHeight(int h)
	{
		int height = getHeight();
		if ( height < h )
		{
			for (int y = height; y < h; y++)
			{
				data.add( new ArrayList<Object>() );
			}
		}
	}
	
	private void growRowWidth(ArrayList<Object> row, int w)
	{
		int width = row.size();
		if ( width < w )
		{
			for (int x = width; x < w; x++)
			{
				row.add( cellFactory.createCell() );
			}
		}
	}
}
