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

public class GenericTableModel extends GenericTableModelAbstractBase
{
	private static RowFactory defaultRowFactory = new RowFactory()
	{
		@Override
		public List<Object> createRow()
		{
			return new ArrayList<Object>();
		}
	};
	
	
	private IncrementalValueMonitor incr = new IncrementalValueMonitor();
	
	
	
	
	public GenericTableModel(List<List<Object>> data, ValueFactory cellFactory, RowFactory rowFactory, ValueCopier cellCopier)
	{
		super(data, cellFactory, rowFactory, cellCopier);
	}

	public GenericTableModel(ValueFactory cellFactory, RowFactory rowFactory, ValueCopier cellCopier)
	{
		this( new ArrayList<List<Object>>(), cellFactory, rowFactory, cellCopier );
	}

	public GenericTableModel(List<List<Object>> data, ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this(data, cellFactory, defaultRowFactory, cellCopier);
	}

	public GenericTableModel(ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this( new ArrayList<List<Object>>(), cellFactory, defaultRowFactory, cellCopier );
	}

	
	@Override
	public int getWidth()
	{
		incr.onAccess();
		return super.getWidth();
	}

	@Override
	public int getHeight()
	{
		incr.onAccess();
		return super.getHeight();
	}

	@Override
	public List<Object> getRow(int rowIndex)
	{
		incr.onAccess();
		return super.getRow(rowIndex);
	}
	
	

	@Override
	public Object get(int x, int y)
	{
		incr.onAccess();
		return super.get(x, y);
	}


	@Override
	public void set(int x, int y, Object value)
	{
		super.set(x, y, value);
		incr.onChanged();
	}


	@Override
	public Object[][] getBlock(int x, int y, int w, int h)
	{
		incr.onAccess();
		return super.getBlock(x, y, w, h);
	}

	@Override
	public void putBlock(int x, int y, Object[][] block)
	{
		super.putBlock(x, y, block);
		incr.onChanged();
	}
	
	@Override
	public void deleteBlock(int x, int y, int w, int h)
	{
		super.deleteBlock(x, y, w, h);
		incr.onChanged();
	}
}
