//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class ShapeNode extends DiagramNode
{
	protected Shape shape;
	protected AABox2 parentSpaceBox;
	
	
	public ShapeNode(Shape shape)
	{
		super();
		this.shape = shape;
		Rectangle2D.Double rect = (Rectangle2D.Double)shape.getBounds2D();
		parentSpaceBox = new AABox2( rect.x, rect.y, rect.x + rect.width, rect.y + rect.height );
	}


	public void draw(Graphics2D graphics, DrawContext context)
	{
		Paint fillPaint = context.getFillPaint();
		Paint strokePaint = context.getStrokePaint();
		if ( fillPaint != null )
		{
			graphics.setPaint( fillPaint );
			graphics.fill( shape );
		}
		if ( strokePaint != null )
		{
			graphics.setPaint( strokePaint );
			graphics.draw( shape );
		}
	}


	public AABox2 getParentSpaceBoundingBox()
	{
		return parentSpaceBox;
	}

	
	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		if ( parentSpaceBox.containsPoint( localPos ) )
		{
			return shape.contains( localPos.x, localPos.y );
		}
		else
		{
			return false;
		}
	}


	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		if ( parentSpaceBox.containsPoint( parentPos ) )
		{
			return shape.contains( parentPos.x, parentPos.y );
		}
		else
		{
			return false;
		}
	}
	
	
	
	public static ShapeNode rectangle(double x, double y, double w, double h)
	{
		return new ShapeNode( new Rectangle2D.Double( x, y, w, h ) );
	}
	
	public static ShapeNode roundRectangle(double x, double y, double w, double h, double arcWidth, double arcHeight)
	{
		return new ShapeNode( new RoundRectangle2D.Double( x, y, w, h, arcWidth, arcHeight ) );
	}
	
	public static ShapeNode arc2d(double x, double y, double w, double h, double start, double extent, int type)
	{
		return new ShapeNode( new Arc2D.Double( x, y, w, h, start, extent, type ) );
	}
	
	public static ShapeNode ellipse(double x, double y, double w, double h)
	{
		return new ShapeNode( new Ellipse2D.Double( x, y, w, h ) );
	}
	
	public static ShapeNode line(double x1, double y1, double x2, double y2)
	{
		return new ShapeNode( new Line2D.Double( x1, y1, x2, y2 ) );
	}
	
	public static ShapeNode quadCurve(double x1, double y1, double ctrlx, double ctrly, double x2, double y2)
	{
		return new ShapeNode( new QuadCurve2D.Double( x1, y1, ctrlx, ctrly, x2, y2 ) );
	}
	
	public static ShapeNode cubicCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2)
	{
		return new ShapeNode( new CubicCurve2D.Double( x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2 ) );
	}
}
