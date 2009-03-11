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
	private double thickness, roundingX, roundingY;
	private Color colour, backgroundColour;
	
	
	public SolidBorder()
	{
		this( 0.0, 0.0, 0.0, Color.black, null );
	}
	
	public SolidBorder(double thickness, Color colour, Color backgroundColour)
	{
		this( thickness, 0.0, 0.0, colour, backgroundColour );
	}
	
	public SolidBorder(double thickness, double roundingX, double roundingY, Color colour, Color backgroundColour)
	{
		this.thickness = thickness;
		this.roundingX = roundingX;
		this.roundingY = roundingY;
		this.colour = colour;
		this.backgroundColour = backgroundColour;
	}
	
	

	public double getLeftMargin()
	{
		return thickness;
	}

	public double getRightMargin()
	{
		return thickness;
	}

	public double getTopMargin()
	{
		return thickness;
	}

	public double getBottomMargin()
	{
		return thickness;
	}


	public void draw(Graphics2D graphics, double x, double y, double w, double h)
	{
		if ( backgroundColour != null )
		{
			graphics.setColor( backgroundColour );
			if ( roundingX != 0.0  ||  roundingY != 0.0 )
			{
				graphics.fill( new RoundRectangle2D.Double( x + thickness, y + thickness, w - thickness, h - thickness, roundingX, roundingY ) );
			}
			else
			{
				graphics.fill( new Rectangle2D.Double( x + thickness, y + thickness, w - thickness, h - thickness ) );
			}
		}

		
		Stroke curStroke = graphics.getStroke();
		Paint curPaint = graphics.getPaint();
		
		Stroke s = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
		graphics.setStroke( s );
		graphics.setPaint( colour );
		
		if ( roundingX != 0.0  ||  roundingY != 0.0 )
		{
			graphics.draw( new RoundRectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w-thickness, h-thickness, roundingX, roundingY ) );
		}
		else
		{
			graphics.draw( new Rectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w-thickness, h-thickness ) );
		}
		
		graphics.setStroke( curStroke );
		graphics.setPaint( curPaint );
	}
}
