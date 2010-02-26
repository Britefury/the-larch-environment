//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.Painter;


public class TableStyleParams extends ContainerStyleParams
{
	public static final TableStyleParams defaultStyleParams = new TableStyleParams( null, null, 0.0, false, 0.0, false );
	
	
	protected final double columnSpacing;
	protected final boolean columnExpand;

	protected final double rowSpacing;
	protected final boolean rowExpand;


	public TableStyleParams(Painter background, Cursor pointerCursor, double columnSpacing, boolean columnExpand, double rowSpacing, boolean rowExpand)
	{
		super( background, pointerCursor );
		
		this.columnSpacing = columnSpacing;
		this.columnExpand = columnExpand;

		this.rowSpacing = rowSpacing;
		this.rowExpand = rowExpand;
	}


	public double getColumnSpacing()
	{
		return columnSpacing;
	}

	public boolean getColumnExpand()
	{
		return columnExpand;
	}


	public double getRowSpacing()
	{
		return rowSpacing;
	}

	public boolean getRowExpand()
	{
		return rowExpand;
	}
}
