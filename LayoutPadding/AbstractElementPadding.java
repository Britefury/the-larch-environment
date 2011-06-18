//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout.Padding;

public abstract class AbstractElementPadding
{
	public abstract double getLeftPadding();
	public abstract double getRightPadding();
	public abstract double getTopPadding();
	public abstract double getBottomPadding();
	
	public abstract AbstractElementPadding padX(double left, double right);
	public abstract AbstractElementPadding padY(double top, double bottom);
	public abstract AbstractElementPadding pad(double left, double right, double top, double bottom);
}
