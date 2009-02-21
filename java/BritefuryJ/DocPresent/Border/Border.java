//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Border;

import java.awt.Graphics2D;

public abstract class Border
{
	public abstract double getLeftMargin();
	public abstract double getRightMargin();
	public abstract double getTopMargin();
	public abstract double getBottomMargin();
	
	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
	}
}
