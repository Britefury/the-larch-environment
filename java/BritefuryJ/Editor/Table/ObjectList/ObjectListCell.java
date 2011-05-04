//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import BritefuryJ.Cell.AbstractCell;

public class ObjectListCell extends AbstractCell
{
	protected Object modelRow;
	protected AbstractColumn column;
	
	
	public ObjectListCell(Object modelRow, AbstractColumn column)
	{
		this.modelRow = modelRow;
		this.column = column;
	}
	
	
	@Override
	public Object getValue()
	{
		return column.get( modelRow );
	}
	
	@Override
	public void setValue(Object value)
	{
		column.set( modelRow, value );
	}
}
