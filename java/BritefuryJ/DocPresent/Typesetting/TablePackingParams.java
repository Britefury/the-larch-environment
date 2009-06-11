//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class TablePackingParams extends PackingParams
{
	public int x, y, colSpan, rowSpan;
	public int packFlagsX, packFlagsY;
	public double paddingX, paddingY;
	
	public TablePackingParams(int x, int colSpan, boolean bExpandX, double paddingX, int y, int rowSpan, boolean bExpandY, double paddingY)
	{
		this.x = x;
		this.colSpan = colSpan;
		this.packFlagsX = TSBox.packFlags( bExpandX );
		this.paddingX = paddingX;
		this.y = y;
		this.rowSpan = rowSpan;
		this.packFlagsY = TSBox.packFlags( bExpandY );
		this.paddingY = paddingY;
	}
}
