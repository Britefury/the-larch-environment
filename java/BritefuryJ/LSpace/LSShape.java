//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LayoutTree.LayoutNodeShape;
import BritefuryJ.LSpace.StyleParams.ShapeStyleParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class LSShape extends LSBlank
{
	private Shape shape;
	private AABox2 bounds;
	
	
	public LSShape(Shape shape)
	{
		this( ShapeStyleParams.defaultStyleParams, shape );
	}
	
	public LSShape(ShapeStyleParams styleParams, Shape shape)
	{
		super( styleParams );
		
		this.shape = shape;
		
		if ( shape != null )
		{
			Rectangle2D bounds2D = shape.getBounds2D();
			bounds = new AABox2( bounds2D.getMinX(), bounds2D.getMinY(), bounds2D.getMaxX(), bounds2D.getMaxY() );
		}
		else
		{
			bounds = new AABox2();
		}
		
		layoutNode = new LayoutNodeShape( this );
	}

	
	
	
	//
	//
	// Geometry methods
	//
	//

	public AABox2 getShapeBounds()
	{
		return bounds;
	}
	
	public AABox2 getLocalAABox()
	{
		return bounds;
	}


	public boolean containsParentSpacePoint(Point2 parentPos)
	{
		Point2 localPos = getParentToLocalXform().transform( parentPos );
		return containsLocalSpacePoint( localPos );
	}

	public boolean containsLocalSpacePoint(Point2 localPos)
	{
		if ( shape != null )
		{
			return shape.contains( new Point2D.Double( localPos.x, localPos.y ) );
		}
		else
		{
			return false;
		}
	}

	
	@Override
	protected AABox2 getVisibleBoxInLocalSpace()
	{
		return bounds;
	}
	

	public boolean isRedrawRequiredOnHover()
	{
		ShapeStyleParams s = (ShapeStyleParams)styleParams;
		return super.isRedrawRequiredOnHover()  ||  s.getHoverPainter() != null;
	}
	

	protected void draw(Graphics2D graphics)
	{
		if ( shape != null )
		{
			ShapeStyleParams p = (ShapeStyleParams)styleParams;
			
			Painter painter;
			if ( isHoverActive() )
			{
				Painter hoverPainter = p.getHoverPainter();
				painter = hoverPainter != null  ?  hoverPainter  :  p.getPainter();
			}
			else
			{
				painter = p.getPainter();
			}
			
			if ( painter != null )
			{
				painter.drawShape( graphics, shape );
			}
		}
	}
	
	
	
	public static Rectangle2D.Double rectangle(double x, double y, double w, double h)
	{
		return new Rectangle2D.Double( x, y, w, h );
	}

	public static Rectangle2D.Double rectangle(Point2 pos, Vector2 size)
	{
		return new Rectangle2D.Double( pos.x, pos.y, size.x, size.y );
	}

	public static RoundRectangle2D.Double roundRectangle(double x, double y, double w, double h, double roundingX, double roundingY)
	{
		return new RoundRectangle2D.Double( x, y, w, h, roundingX, roundingY );
	}

	public static RoundRectangle2D.Double roundRectangle(Point2 pos, Vector2 size, Vector2 rounding)
	{
		return new RoundRectangle2D.Double( pos.x, pos.y, size.x, size.y, rounding.x, rounding.y );
	}
	
	public static Ellipse2D.Double ellipse(double x, double y, double w, double h)
	{
		return new Ellipse2D.Double( x, y, w, h );
	}

	public static Ellipse2D.Double ellipse(Point2 pos, Vector2 size)
	{
		return new Ellipse2D.Double( pos.x, pos.y, size.x, size.y );
	}
	
	public static Path2D.Double path(Point2 points[], boolean bClosed)
	{
		if ( points.length > 1 )
		{
			Path2D.Double path = new Path2D.Double();
			path.moveTo( points[0].x, points[0].y );
			for (int i = 1; i < points.length; i++)
			{
				path.lineTo( points[i].x, points[i].y );
			}
			if ( bClosed )
			{
				path.lineTo( points[0].x, points[0].y );
			}
			return path;
		}
		else
		{
			return null;
		}
	}

	
	public static Point2[] filletCorner(Point2 a, Point2 b, Point2 c, double filletSize)
	{
		Vector2 u = a.sub( b ).getNormalised(), v = c.sub( b ).getNormalised();
		return new Point2[] { b.add( u.mul( filletSize ) ), b, b.add( v.mul( filletSize ) ) };
	}
	
	public static Path2D.Double filletedPath(Point2 points[], boolean bClosed, double filletSize)
	{
		if ( points.length >= 3 )
		{
			Path2D.Double path = new Path2D.Double();
			if ( filletSize > 0.0 )
			{
				if ( bClosed )
				{
					int last = points.length - 1;
					Point2 fillet0[] = filletCorner( points[last], points[0], points[1], filletSize );
					path.moveTo( fillet0[0].x, fillet0[0].y );
					path.quadTo( fillet0[1].x, fillet0[1].y, fillet0[2].x, fillet0[2].y );
					for (int i = 1; i < last; i++)
					{
						Point2 fillet[] = filletCorner( points[i-1], points[i], points[i+1], filletSize );
						path.lineTo( fillet[0].x, fillet[0].y );
						path.quadTo( fillet[1].x, fillet[1].y, fillet[2].x, fillet[2].y );
					}
					Point2 filletLast[] = filletCorner( points[last-1], points[last], points[0], filletSize );
					path.lineTo( filletLast[0].x, filletLast[0].y );
					path.quadTo( filletLast[1].x, filletLast[1].y, filletLast[2].x, filletLast[2].y );
					path.lineTo( fillet0[0].x, fillet0[0].y );
				}
				else
				{
					int last = points.length - 1;
					path.moveTo( points[0].x, points[0].y );
					for (int i = 1; i < last; i++)
					{
						Point2 fillet[] = filletCorner( points[i-1], points[i], points[i+1], filletSize );
						path.lineTo( fillet[0].x, fillet[0].y );
						path.quadTo( fillet[1].x, fillet[1].y, fillet[2].x, fillet[2].y );
					}
					path.lineTo( points[last].x, points[last].y );
				}
			}
			else
			{
				path.moveTo( points[0].x, points[0].y );
				for (int i = 1; i < points.length; i++)
				{
					path.lineTo( points[i].x, points[i].y );
				}

				if ( bClosed )
				{
					path.lineTo( points[0].x, points[0].y );
				}
			}
			return path;
		}
		else
		{
			return null;
		}
	}
}
