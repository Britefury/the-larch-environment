//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Graphics;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class FilledBorder extends AbstractBorder
{
	private double leftMargin, rightMargin, topMargin, bottomMargin, roundingX, roundingY;
	private Paint backgroundPaint, highlightBackgroundPaint;
	
	
	public FilledBorder()
	{
		this( 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null );
	}
	
	public FilledBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, 0.0, 0.0, null, null );
	}
	
	public FilledBorder(Paint backgroundPaint)
	{
		this( 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, backgroundPaint, null );
	}
	
	public FilledBorder(double roundingX, double roundingY, Paint backgroundPaint)
	{
		this( 0.0, 0.0, 0.0, 0.0, roundingX, roundingY, backgroundPaint, null );
	}
	
	public FilledBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, Paint backgroundPaint)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, 0.0, 0.0, backgroundPaint, null );
	}
	
	public FilledBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, double roundingX, double roundingY, Paint backgroundPaint)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, roundingX, roundingY, backgroundPaint, null );
	}

	public FilledBorder(Paint backgroundPaint, Paint highlightBackgroundPaint)
	{
		this( 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, backgroundPaint, highlightBackgroundPaint );
	}
	
	public FilledBorder(double roundingX, double roundingY, Paint backgroundPaint, Paint highlightBackgroundPaint)
	{
		this( 0.0, 0.0, 0.0, 0.0, roundingX, roundingY, backgroundPaint, highlightBackgroundPaint );
	}
	
	public FilledBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, Paint backgroundPaint, Paint highlightBackgroundPaint)
	{
		this( leftMargin, rightMargin, topMargin, bottomMargin, 0.0, 0.0, backgroundPaint, highlightBackgroundPaint );
	}
	
	public FilledBorder(double leftMargin, double rightMargin, double topMargin, double bottomMargin, double roundingX, double roundingY, Paint backgroundPaint, Paint highlightBackgroundPaint)
	{
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;
		this.topMargin = topMargin;
		this.bottomMargin = bottomMargin;
		this.roundingX = roundingX;
		this.roundingY = roundingY;
		this.backgroundPaint = backgroundPaint;
		this.highlightBackgroundPaint = highlightBackgroundPaint;
	}


	public FilledBorder highlight(Paint highlightBackgroundPaint)
	{
		return new FilledBorder( leftMargin, rightMargin, topMargin, bottomMargin, roundingX, roundingY, backgroundPaint, highlightBackgroundPaint );
	}

	
	

	public double getLeftMargin()
	{
		return leftMargin;
	}

	public double getRightMargin()
	{
		return rightMargin;
	}

	public double getTopMargin()
	{
		return topMargin;
	}

	public double getBottomMargin()
	{
		return bottomMargin;
	}
	
	
	@Override
	public boolean isHighlightable()
	{
		return highlightBackgroundPaint != null;
	}

	@Override
	public Shape getClipShape(Graphics2D graphics, double x, double y, double w, double h)
	{
		if ( roundingX != 0.0  ||  roundingY != 0.0 )
		{
			return new RoundRectangle2D.Double( x, y, w, h, roundingX, roundingY );
		}
		else
		{
			return new Rectangle2D.Double( x, y, w, h );
		}
	}
	
	@Override
	public void drawBackground(Graphics2D graphics, double x, double y, double w, double h, boolean highlight)
	{
		Paint p = highlight  &&  highlightBackgroundPaint != null   ?   highlightBackgroundPaint   :   backgroundPaint;
		if ( p != null )
		{
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( p );
			if ( roundingX != 0.0  ||  roundingY != 0.0 )
			{
				graphics.fill( new RoundRectangle2D.Double( x, y, w, h, roundingX, roundingY ) );
			}
			else
			{
				graphics.fill( new Rectangle2D.Double( x, y, w, h ) );
			}
			graphics.setPaint( prevPaint );
		}
	}
	
	
	public String toString()
	{
		return "FilledBorder( " + leftMargin + ", " + rightMargin + ", " + topMargin + ", " + bottomMargin + ", " + roundingX + ", " + roundingY + ", " + backgroundPaint + ", " + highlightBackgroundPaint +" )";
	}
}
