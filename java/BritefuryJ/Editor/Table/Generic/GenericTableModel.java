//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Incremental.IncrementalValueMonitor;

public class GenericTableModel implements GenericTableModelInterface
{
	public static interface ValueFactory
	{
		Object createValue();
	}
	
	public static interface ValueCopier
	{
		Object copyValue(Object value);
	}
	
	
	private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
	private ValueFactory cellFactory;
	private ValueCopier cellCopier;
	private IncrementalValueMonitor incr = new IncrementalValueMonitor();
	
	
	
	
	public GenericTableModel(ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this.cellFactory = cellFactory;
		this.cellCopier = cellCopier;
	}

	
	@Override
	public int getWidth()
	{
		incr.onAccess();
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
		incr.onAccess();
		return data.size();
	}

	@Override
	public List<Object> getRow(int rowIndex)
	{
		incr.onAccess();
		return data.get( rowIndex );
	}
	
	

	@Override
	public Object get(int x, int y)
	{
		incr.onAccess();
		return data.get( y ).get( x );
	}


	@Override
	public void set(int x, int y, Object value)
	{
		growHeight( y + 1 );
		ArrayList<Object> row = data.get( y );
		growRowWidth( row, x + 1 );
		row.set( x, value );
		incr.onChanged();
	}


	@Override
	public Object[][] getBlock(int x, int y, int w, int h)
	{
		incr.onAccess();
		Object[][] block = new Object[h][];
		for (int j = y, b = 0; b < h; j++, b++)
		{
			ArrayList<Object> srcRow = data.get( j );
			int numCells = Math.max( 0, Math.min( w, srcRow.size() - x ) );
			Object destRow[] = new Object[numCells];
			if ( numCells > 0 )
			{
				System.arraycopy( srcRow.toArray(), x, destRow, 0, numCells );
			}
			block[b] = destRow;
		}
		
		return block;
	}

	@Override
	public void putBlock(int x, int y, Object[][] block)
	{
		int blockHeight = block.length;
		
		growHeight( y + blockHeight );
		
		for (int j = y, b = 0; b < blockHeight; j++, b++)
		{
			Object srcRow[] = block[b];
			ArrayList<Object> destRow = data.get( j );
			
			int rowWidth = srcRow.length;
			
			growRowWidth( destRow, x + rowWidth );
			
			for (int i = x, a = 0; a < rowWidth; i++, a++)
			{
				destRow.set( i, cellCopier.copyValue( srcRow[a] ) );
			}
		}
		incr.onChanged();
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
				row.add( cellFactory.createValue() );
			}
		}
	}
}
