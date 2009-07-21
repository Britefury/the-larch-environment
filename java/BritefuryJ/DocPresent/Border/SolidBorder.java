//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Border;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class SolidBorder extends Border
{
	private double thickness, inset, roundingX, roundingY;
	private Paint borderPaint, backgroundPaint;
	
	
	public SolidBorder()
	{
		this( 1.0, 1.0, 0.0, 0.0, Color.black, null );
	}
	
	public SolidBorder(double thickness, double inset, Paint borderPaint, Paint backgroundPaint)
	{
		this( thickness, inset, 0.0, 0.0, borderPaint, backgroundPaint );
	}
	
	public SolidBorder(double thickness, double inset, double roundingX, double roundingY, Paint borderPaint, Paint backgroundPaint)
	{
		this.thickness = thickness;
		this.inset = inset;
		this.roundingX = roundingX;
		this.roundingY = roundingY;
		this.borderPaint = borderPaint;
		this.backgroundPaint = backgroundPaint;
	}
	
	

	public double getLeftMargin()
	{
		return thickness + inset;
	}

	public double getRightMargin()
	{
		return thickness + inset;
	}

	public double getTopMargin()
	{
		return thickness + inset;
	}

	public double getBottomMargin()
	{
		return thickness + inset;
	}


	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
		Stroke prevStroke = graphics.getStroke();
		Paint prevPaint = graphics.getPaint();
		if ( backgroundPaint != null )
		{
			graphics.setPaint( backgroundPaint );
			if ( roundingX != 0.0  ||  roundingY != 0.0 )
			{
				graphics.fill( new RoundRectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w - thickness, h - thickness, roundingX, roundingY ) );
			}
			else
			{
				graphics.fill( new Rectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w - thickness, h - thickness ) );
			}
		}

		
		
		Stroke s = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
		graphics.setStroke( s );
		graphics.setPaint( borderPaint );
		
		if ( roundingX != 0.0  ||  roundingY != 0.0 )
		{
			graphics.draw( new RoundRectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w-thickness, h-thickness, roundingX, roundingY ) );
		}
		else
		{
			graphics.draw( new Rectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w-thickness, h-thickness ) );
		}
		
		graphics.setStroke( prevStroke );
		graphics.setPaint( prevPaint );
	}
}
