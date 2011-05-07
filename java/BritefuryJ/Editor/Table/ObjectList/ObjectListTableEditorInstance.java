//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.GridRow;
import BritefuryJ.Pres.Primitive.RGrid;
import BritefuryJ.Utils.WeakValueIdentityHashMap;

public class ObjectListTableEditorInstance extends AbstractTableEditorInstance<ObjectListInterface>
{
	private WeakValueIdentityHashMap<Object, ObjectListRow> rowMap = new WeakValueIdentityHashMap<Object, ObjectListRow>();
	
	protected ObjectListTableEditorInstance(AbstractTableEditor<ObjectListInterface> editor, ObjectListInterface model)
	{
		super( editor, model );
	}
	
	

	@Override
	protected Pres presentTable()
	{
		ObjectListTableEditor listEditor = (ObjectListTableEditor)editor;
		int width = listEditor.columns.length;
		
		int numObjects = model.size();
		int numRows = listEditor.showEmptyRowAtBottom  ?  numObjects + 1  :  numObjects;
		Object rows[] = new Object[numRows];
		for (int j = 0; j < numObjects; j++)
		{
			rows[j] = projectRow( model.get( j ) );
		}
		
		if ( listEditor.showEmptyRowAtBottom )
		{
			Pres lastRow[] = new Pres[width];
			for (int i = 0; i < width; i++)
			{
				lastRow[i] = listEditor.emptyCellFac.createEmptyCell();
			}
			rows[numObjects] = new GridRow( lastRow );
		}
		
		return new RGrid( rows );
	}
	
	
	
	
	
	protected void onRowChanged(Object modelRow)
	{
		projectRow( modelRow ).onChanged();
	}
	
	
	
	private ObjectListRow projectRow(Object modelRow)
	{
		ObjectListRow row = rowMap.get( modelRow );
		if ( row == null )
		{
			row = new ObjectListRow( (ObjectListTableEditor)editor, modelRow );
			rowMap.put( modelRow, row );
		}
		return row;
	}



	@Override
	protected int getHeight()
	{
		return model.size();
	}

	@Override
	protected int getRowWidth(int row)
	{
		ObjectListTableEditor e = (ObjectListTableEditor)editor;
		return e.columns.length;
	}
}
