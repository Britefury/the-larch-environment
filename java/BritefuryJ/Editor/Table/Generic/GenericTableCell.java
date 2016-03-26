//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
