//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.StyleParams;

import java.awt.Cursor;

import BritefuryJ.DocPresent.Painter.Painter;

public class ScriptStyleParams extends ContainerStyleParams
{
	public static final ScriptStyleParams defaultStyleSheet = new ScriptStyleParams( null, null, 1.0, 1.0 );
	
	
	private final double columnSpacing, rowSpacing;
	
	
	public ScriptStyleParams(Painter background, Cursor pointerCursor, double columnSpacing, double rowSpacing)
	{
		super( background, pointerCursor );
		
		this.columnSpacing = columnSpacing;
		this.rowSpacing = rowSpacing;
	}


	public double getColumnSpacing()
	{
		return this.columnSpacing;
	}

	
	public double getRowSpacing()
	{
		return this.rowSpacing;
	}
}
