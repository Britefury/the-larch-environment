//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class SolidBorder extends AbstractBorder
{
	private double thickness, inset, roundingX, roundingY;
	private Paint borderPaint, backgroundPaint, highlightBorderPaint, highlightBackgroundPaint;
	
	
	public SolidBorder()
	{
		this( 1.0, 1.0, 0.0, 0.0, Color.black, null );
	}
	
	public SolidBorder(double thickness, double inset, Paint borderPaint, Paint backgroundPaint)
	{
		this( thickness, inset, 0.0, 0.0, borderPaint, backgroundPaint, null, null );
	}
	
	public SolidBorder(double thickness, double inset, double roundingX, double roundingY, Paint borderPaint, Paint backgroundPaint)
	{
		this( thickness, inset, roundingX, roundingY, borderPaint, backgroundPaint, null, null );
	}
	
	public SolidBorder(double thickness, double inset, Paint borderPaint, Paint backgroundPaint, Paint highlightBorderPaint, Paint highlightBackgroundPaint)
	{
		this( thickness, inset, 0.0, 0.0, borderPaint, backgroundPaint, highlightBorderPaint, highlightBackgroundPaint );
	}
	
	public SolidBorder(double thickness, double inset, double roundingX, double roundingY, Paint borderPaint, Paint backgroundPaint, Paint highlightBorderPaint, Paint highlightBackgroundPaint)
	{
		this.thickness = thickness;
		this.inset = inset;
		this.roundingX = roundingX;
		this.roundingY = roundingY;
		this.borderPaint = borderPaint;
		this.backgroundPaint = backgroundPaint;
		this.highlightBorderPaint = highlightBorderPaint;
		this.highlightBackgroundPaint = highlightBackgroundPaint;
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
	
	
	@Override
	public boolean isHighlightable()
	{
		return highlightBorderPaint != null  ||  highlightBackgroundPaint != null;
	}


	@Override
	public void draw(Graphics2D graphics, double x, double y, double w, double h, boolean highlight)
	{
		Stroke prevStroke = graphics.getStroke();
		Paint prevPaint = graphics.getPaint();
		
		Paint background = highlight  &&  highlightBackgroundPaint != null   ?   highlightBackgroundPaint   :   backgroundPaint;
		Paint border = highlight  &&  highlightBorderPaint != null   ?   highlightBorderPaint   :   borderPaint;

		Shape borderShape;
		if ( roundingX != 0.0  ||  roundingY != 0.0 )
		{
			borderShape = new RoundRectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w - thickness, h - thickness, roundingX, roundingY );
		}
		else
		{
			borderShape = new Rectangle2D.Double( x + thickness*0.5, y + thickness*0.5, w - thickness, h - thickness );
		}
		
		if ( background != null )
		{
			graphics.setPaint( background );
			graphics.fill( borderShape );
		}

		
		
		Stroke s = new BasicStroke( (float)thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL );
		graphics.setStroke( s );
		graphics.setPaint( border );
		graphics.draw( borderShape );
		graphics.setStroke( prevStroke );
		graphics.setPaint( prevPaint );
	}
	
	
	public String toString()
	{
		return "FilledBorder( " + thickness + ", " + inset + ", " + roundingX + ", " + roundingY + ", " + borderPaint + ", " + backgroundPaint + ", " + highlightBorderPaint + ", " + highlightBackgroundPaint + " )";
	}
}
