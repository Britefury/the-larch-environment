//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
