//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table.Generic;

import BritefuryJ.Editor.Table.AbstractBlankTableCell;
import BritefuryJ.Pres.Pres;

public class GenericBlankTableCell extends AbstractBlankTableCell
{
	private GenericTableModelInterface tableModel;
	private int x, y;
	
	
	public GenericBlankTableCell(GenericTableModelInterface tableModel, int x, int y, Pres blankPres)
	{
		super( blankPres );
		this.tableModel = tableModel;
		this.x = x;
		this.y = y;
	}
	
	
	@Override
	public void setValue(Object value)
	{
		tableModel.set( x, y, value );
	}
}
