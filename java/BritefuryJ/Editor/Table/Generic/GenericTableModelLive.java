//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import BritefuryJ.Util.LiveTrackedList;

public class GenericTableModelLive extends GenericTableModelAbstractBase<LiveTrackedList<Object>, LiveTrackedList<LiveTrackedList<Object>>>
{
	private static RowFactory<LiveTrackedList<Object>> defaultRowFactory = new RowFactory<LiveTrackedList<Object>>()
	{
		@Override
		public LiveTrackedList<Object> createRow()
		{
			return new LiveTrackedList<Object>();
		}
	};


	public GenericTableModelLive(LiveTrackedList<LiveTrackedList<Object>> data, ValueFactory cellFactory, RowFactory<LiveTrackedList<Object>> rowFactory, ValueCopier cellCopier)
	{
		super(data, cellFactory, rowFactory, cellCopier);
	}

	public GenericTableModelLive(ValueFactory cellFactory, RowFactory<LiveTrackedList<Object>> rowFactory, ValueCopier cellCopier)
	{
		this( new LiveTrackedList<LiveTrackedList<Object>>(), cellFactory, rowFactory, cellCopier );
	}

	public GenericTableModelLive(LiveTrackedList<LiveTrackedList<Object>> data, ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this(data, cellFactory, defaultRowFactory, cellCopier);
	}

	public GenericTableModelLive(ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this( new LiveTrackedList<LiveTrackedList<Object>>(), cellFactory, defaultRowFactory, cellCopier );
	}
}
