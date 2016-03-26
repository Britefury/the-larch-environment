//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

import java.awt.*;
import java.awt.geom.Path2D;


public class InsertionPoint
{
	private int index;
	private Point2 startPoint, endPoint;
	private Path2D.Double path;


	public InsertionPoint(int index, Point2 startPoint, Point2 endPoint)
	{
		this.index = index;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	public InsertionPoint(int index, Point2 line[])
	{
		this.index = index;
		this.startPoint = line[0];
		this.endPoint = line[1];
	}


	public int getIndex()
	{
		return index;
	}

	public Point2 getStartPoint()
	{
		return startPoint;
	}

	public Point2 getEndPoint()
	{
		return endPoint;
	}


	public void draw(Graphics2D graphics)
	{
		graphics.fill( getPath() );
	}


	private Path2D.Double getPath()
	{
		if ( path == null )
		{
			// Build the insertion shape
			Vector2 u = endPoint.sub( startPoint );
			double length = u.length();
			u = u.mul( 1.0 / length );
			Vector2 v = u.rotated90CCW();

			double arrowWidth = Math.max( Math.min( length * 0.1667, 4.0 ), 2.0 );
			double arrowLength = Math.min( arrowWidth * 1.5, length * 0.4 );

			Point2 verts[] = new Point2[] {
					startPoint.add( v.mul( -arrowWidth ) ),
					startPoint.add( v.mul( arrowWidth ) ),
					startPoint.add( v.mul( 1.0 ) ).add( u.mul( arrowLength ) ),
					endPoint.add( v.mul( 1.0 ) ).sub( u.mul( arrowLength ) ),
					endPoint.add( v.mul( arrowWidth ) ),
					endPoint.add( v.mul( -arrowWidth ) ),
					endPoint.add( v.mul( -1.0 ) ).sub( u.mul( arrowLength ) ),
					startPoint.add( v.mul( -1.0 ) ).add( u.mul( arrowLength ) )
			};

			path = new Path2D.Double();
			Point2 first = verts[0];
			path.moveTo( first.x, first.y );
			for (int i = 1; i < verts.length; i++)
			{
				Point2 vtx = verts[i];
				path.lineTo( vtx.x, vtx.y );
			}

			path.closePath();
		}

		return path;
	}
}
