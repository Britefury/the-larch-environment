//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Editor.Table.TableEditorStyle;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.GridRow;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.RGrid;
import BritefuryJ.Utils.WeakValueIdentityHashMap;

public class ObjectListTableEditorInstance extends AbstractTableEditorInstance<ObjectListInterface>
{
	private WeakValueIdentityHashMap<Object, ObjectListRow> rowMap = new WeakValueIdentityHashMap<Object, ObjectListRow>();
	
	protected ObjectListTableEditorInstance(AbstractTableEditor<ObjectListInterface> editor, ObjectListInterface model, boolean editable)
	{
		super( editor, model, editable );
	}
	
	

	@Override
	protected Pres presentTable()
	{
		ObjectListTableEditor listEditor = (ObjectListTableEditor)editor;
		int width = listEditor.columns.length;
		
		int numObjects = model.size();
		int numColumns = tableWToElementW( width );
		int numRows = tableHToElementH( numObjects );
		Object rows[] = new Object[numRows];

		if ( hasTopHeader() )
		{
			Object firstRow[] = new Object[numColumns];
			if ( hasLeftHeader() )
			{
				firstRow[0] = new Label( "" );
			}
			for (int i = 0; i < width; i++)
			{
				int ex = tableXToElementX( i );
				firstRow[ex] = listEditor.columns[i].presentHeader();
			}
			rows[0] = new GridRow( firstRow );
		}
		
		for (int j = 0; j < numObjects; j++)
		{
			ObjectListRow p = projectRow( model.get( j ) );
			int ey = tableYToElementY( j );
			if ( hasLeftHeader() )
			{
				Pres title = new Label( String.valueOf( j ) ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
				rows[ey] = new GridRow( new Object[] { title, p } );
			}
			else
			{
				rows[ey] = new GridRow( new Object[] { p } );
			}
		}
		
		if ( canGrowDown() )
		{
			Object lastRow[] = new Object[numColumns];
			if ( hasLeftHeader() )
			{
				lastRow[0] = new Label( "" );
			}
			for (int i = 0; i < width; i++)
			{
				int ex = tableXToElementX( i );
				lastRow[ex] = new ObjectListBlankCell( this, listEditor.columns[i] );
			}
			int ey = tableYToElementY( numObjects );
			rows[ey] = new GridRow( lastRow );
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
		return getMaxRowWidth();
	}
	
	
	@Override
	protected int getMaxRowWidth()
	{
		ObjectListTableEditor e = (ObjectListTableEditor)editor;
		return e.columns.length;
	}



	protected Object newRow()
	{
		ObjectListTableEditor e = (ObjectListTableEditor)editor;
		return e.newRow( model );
	}
}
