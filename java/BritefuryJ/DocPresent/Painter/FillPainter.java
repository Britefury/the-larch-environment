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

public class FillPainter implements Painter
{
	private final Paint fillPaint;
	

	public FillPainter(Paint fillPaint)
	{
		this.fillPaint = fillPaint;
	}
	
	
	public void drawShape(Graphics2D graphics, Shape shape)
	{
		Paint paint = graphics.getPaint();
		graphics.setPaint( fillPaint );
		graphics.fill( shape );
		graphics.setPaint( paint );
	}
	
	public void drawShapes(Graphics2D graphics, Shape shapes[])
	{
		Paint paint = graphics.getPaint();
		graphics.setPaint( fillPaint );
		for (Shape shape: shapes)
		{
			graphics.fill( shape );
		}
		graphics.setPaint( paint );
	}
}
