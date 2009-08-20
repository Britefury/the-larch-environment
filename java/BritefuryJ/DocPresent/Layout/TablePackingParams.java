//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class TablePackingParams extends PackingParams
{
	public int x, y, colSpan, rowSpan;
	public double paddingX, paddingY;
	
	public TablePackingParams(int x, int colSpan, double paddingX, int y, int rowSpan, double paddingY)
	{
		this.x = x;
		this.colSpan = colSpan;
		this.paddingX = paddingX;
		this.y = y;
		this.rowSpan = rowSpan;
		this.paddingY = paddingY;
	}
}
