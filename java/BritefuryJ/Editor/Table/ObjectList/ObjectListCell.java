//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.Editor.Table.AbstractTableCell;

public class ObjectListCell extends AbstractTableCell
{
	protected ObjectListRow row;
	protected AbstractColumn column;
	
	
	public ObjectListCell(ObjectListRow row, AbstractColumn column)
	{
		this.row = row;
		this.column = column;
	}
	
	
	@Override
	public Object getValue()
	{
		return column.get( row.modelRow );
	}
	
	@Override
	public void setValue(Object value)
	{
		column.set( row.modelRow, value );
		row.onCellChanged( this );
	}
}
