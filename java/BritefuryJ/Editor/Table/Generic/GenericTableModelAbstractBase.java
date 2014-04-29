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

public class GenericTableModelAbstractBase<RowType extends List<Object>, TableType extends List<RowType>> implements GenericTableModelInterface
{
	public static interface ValueFactory
	{
		Object createValue();
	}

	public static interface RowFactory <RowType extends List<Object>>
	{
		RowType createRow();
	}

	public static interface ValueCopier
	{
		Object copyValue(Object value);
	}


	private TableType data;
	private ValueFactory cellFactory;
	private RowFactory<RowType> rowFactory;
	private ValueCopier cellCopier;




	public GenericTableModelAbstractBase(TableType data, ValueFactory cellFactory, RowFactory<RowType> rowFactory, ValueCopier cellCopier)
	{
		this.data = data;
		this.cellFactory = cellFactory;
		this.rowFactory = rowFactory;
		this.cellCopier = cellCopier;
	}


	@Override
	public int getWidth()
	{
		int width = 0;
		for (List<Object> row: data)
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
	public List<Object> getRow(int rowIndex)
	{
		return data.get( rowIndex );
	}



	@Override
	public Object get(int x, int y)
	{
		return data.get( y ).get( x );
	}


	@Override
	public void set(int x, int y, Object value)
	{
		growHeight( y + 1 );
		List<Object> row = data.get( y );
		growRowWidth( row, x + 1 );
		row.set( x, value );
	}


	@Override
	public Object[][] getBlock(int x, int y, int w, int h)
	{
		Object[][] block = new Object[h][];
		for (int j = y, b = 0; b < h; j++, b++)
		{
			List<Object> srcRow = data.get( j );
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
			List<Object> destRow = data.get( j );

			int rowWidth = srcRow.length;

			growRowWidth( destRow, x + rowWidth );

			for (int i = x, a = 0; a < rowWidth; i++, a++)
			{
				destRow.set( i, cellCopier.copyValue( srcRow[a] ) );
			}
		}
	}

	@Override
	public void deleteBlock(int x, int y, int w, int h)
	{
		int bottomRowIndex = Math.min( y + h - 1, data.size() - 1 );
		for (int j = bottomRowIndex; j >= y; j--)
		{
			List<Object> row = data.get( j );

			if ( x == 0  &&  w >= row.size() )
			{
				// We are removing all elements in the row - remove it
				data.remove( j );
			}
			else
			{
				if ( x + w  >=  row.size() )
				{
					// We are removing all the elements from @x onwards - trim
					row.subList( x, row.size() ).clear();
				}
				else
				{
					for (int i = x; i < x + w; i++)
					{
						row.set( i, cellFactory.createValue() );
					}
				}
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
				data.add( rowFactory.createRow() );
			}
		}
	}

	private void growRowWidth(List<Object> row, int w)
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
