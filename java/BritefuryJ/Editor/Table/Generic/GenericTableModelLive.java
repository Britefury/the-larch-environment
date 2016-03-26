//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.Generic;

import BritefuryJ.Util.LiveTrackedList;

import java.util.List;

public class GenericTableModelLive extends GenericTableModelAbstractBase
{
	private static RowFactory defaultRowFactory = new RowFactory()
	{
		@Override
		public LiveTrackedList<Object> createRow()
		{
			return new LiveTrackedList<Object>();
		}
	};


	public GenericTableModelLive(List<List<Object>> data, ValueFactory cellFactory, RowFactory rowFactory, ValueCopier cellCopier)
	{
		super(data, cellFactory, rowFactory, cellCopier);
	}

	public GenericTableModelLive(ValueFactory cellFactory, RowFactory rowFactory, ValueCopier cellCopier)
	{
		this( new LiveTrackedList<List<Object>>(), cellFactory, rowFactory, cellCopier );
	}

	public GenericTableModelLive(List<List<Object>> data, ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this(data, cellFactory, defaultRowFactory, cellCopier);
	}

	public GenericTableModelLive(ValueFactory cellFactory, ValueCopier cellCopier)
	{
		this( new LiveTrackedList<List<Object>>(), cellFactory, defaultRowFactory, cellCopier );
	}
}
