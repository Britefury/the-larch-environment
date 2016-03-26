//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Graphics;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;

public class FillPainter extends Painter
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
