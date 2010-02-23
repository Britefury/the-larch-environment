//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Painter;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;

public class OutlinePainter implements Painter
{
	private final Paint outlinePaint;
	

	public OutlinePainter(Paint outlinePaint)
	{
		this.outlinePaint = outlinePaint;
	}
	
	
	public void drawShape(Graphics2D graphics, Shape shape)
	{
		graphics.setPaint( outlinePaint );
		graphics.draw( shape );
		graphics.setPaint( null );
	}
	
	public void drawShapes(Graphics2D graphics, Shape shapes[])
	{
		graphics.setPaint( outlinePaint );
		for (Shape shape: shapes)
		{
			graphics.draw( shape );
		}
		graphics.setPaint( null );
	}
}
