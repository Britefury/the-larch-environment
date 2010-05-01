//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Painter;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

public class FilledOutlinePainter extends Painter
{
	private final Paint fillPaint, outlinePaint;
	private final Stroke outlineStroke;
	

	public FilledOutlinePainter(Paint fillPaint, Paint outlinePaint, Stroke outlineStroke)
	{
		this.fillPaint = fillPaint;
		this.outlinePaint = outlinePaint;
		this.outlineStroke = outlineStroke;
	}
	
	public FilledOutlinePainter(Paint fillPaint, Paint outlinePaint)
	{
		this( fillPaint, outlinePaint, new BasicStroke( 1.0f ) );
	}
	
	
	public void drawShape(Graphics2D graphics, Shape shape)
	{
		Paint paint = graphics.getPaint();
		Stroke stroke = graphics.getStroke();
		
		graphics.setPaint( fillPaint );
		graphics.fill( shape );
		
		graphics.setPaint( outlinePaint );
		graphics.setStroke( outlineStroke );
		graphics.draw( shape );
		
		graphics.setPaint( paint );
		graphics.setStroke( stroke );
	}
	
	public void drawShapes(Graphics2D graphics, Shape shapes[])
	{
		Paint paint = graphics.getPaint();
		Stroke stroke = graphics.getStroke();
		
		graphics.setPaint( fillPaint );
		for (Shape shape: shapes)
		{
			graphics.fill( shape );
		}

		graphics.setPaint( outlinePaint );
		graphics.setStroke( outlineStroke );
		for (Shape shape: shapes)
		{
			graphics.draw( shape );
		}
		
		graphics.setPaint( paint );
		graphics.setStroke( stroke );
	}
}
