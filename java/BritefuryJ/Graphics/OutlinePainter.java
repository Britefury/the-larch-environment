//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Graphics;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

public class OutlinePainter extends Painter
{
	private final Paint outlinePaint;
	private final Stroke outlineStroke;
	

	public OutlinePainter(Paint outlinePaint, Stroke outlineStroke)
	{
		this.outlinePaint = outlinePaint;
		this.outlineStroke = outlineStroke;
	}
	
	public OutlinePainter(Paint outlinePaint)
	{
		this( outlinePaint, new BasicStroke( 1.0f ) );
	}
	
	
	public void drawShape(Graphics2D graphics, Shape shape)
	{
		Paint paint = graphics.getPaint();
		Stroke stroke = graphics.getStroke();
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
