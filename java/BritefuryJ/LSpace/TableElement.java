//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
