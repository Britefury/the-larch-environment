//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
