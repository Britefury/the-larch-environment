//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Math.Point2;


public interface TableElement
{
	public int getNumColumns();
	public int getNumRows();
	
	public boolean hasChildAt(int x, int y);
	public LSElement getChildAt(int x, int y);
	public int getChildColSpan(int x, int y);
	public int getChildRowSpan(int x, int y);
	public int[] getPositionOfChildCoveringCell(int x, int y);
	public LSElement getChildCoveringCell(int x, int y);
	public int[] getCellPositionUnder(Point2 localPos);
	
	public double getColumnBoundaryX(int column);
	public double getRowBoundaryY(int row);
}
