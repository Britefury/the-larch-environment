//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.List;

import BritefuryJ.Editor.Table.AbstractTableCell;

public class GenericTableCell extends AbstractTableCell
{
	private GenericTableModelInterface tableModel;
	private int x, y;
	
	
	public GenericTableCell(GenericTableModelInterface tableModel, int x, int y)
	{
		this.tableModel = tableModel;
		this.x = x;
		this.y = y;
	}
	
	
	@Override
	public Object getValue()
	{
		if ( y < tableModel.getHeight() )
		{
			List<Object> row = tableModel.getRow( y );
			if ( x < row.size() )
			{
				return row.get( x );
			}
		}
		
		return null;
	}

	@Override
	public void setValue(Object value)
	{
		tableModel.set( x, y, value );
	}
}
