//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Math;

import java.io.Serializable;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.Util.HashUtils;


public class AABox2 implements Serializable
{
	private static final long serialVersionUID = 1L;

	private double lowerX, lowerY, upperX, upperY;
	
	
	public AABox2()
	{
		lowerX = lowerY = 1.0;
		upperX = upperY = -1.0;
	}
	
	public AABox2(double lowerX, double lowerY, double upperX, double upperY)
	{
		this.lowerX = lowerX;
		this.lowerY = lowerY;
		this.upperX = upperX;
		this.upperY = upperY;
	}
	
	public AABox2(Point2 a)
	{
		lowerX = upperX = a.x;
		lowerY = upperY = a.y;
	}
	
	public AABox2(Point2 a, Point2 b)
	{
		lowerX = Math.min( a.x, b.x );
		lowerY = Math.min( a.y, b.y );
		upperX = Math.max( a.x, b.x );
		upperY = Math.max( a.y, b.y );
	}
	
	public AABox2(Point2 a, Vector2 size)
	{
		assert size.x >= 0.0  &&  size.y >= 0.0;
		lowerX = a.x;
		lowerY = a.y;
		upperX = a.x + size.x;
		upperY = a.y + size.y;
	}
	
	
	public boolean isEmpty()
	{
		return lowerX > upperX  ||  lowerY > upperY;
	}
	
	
	public boolean containsPoint(Point2 p)
	{
		return p.x >= lowerX  &&  p.x <= upperX  &&  p.y >= lowerY  &&  p.y <= upperY;
	}
	
	public boolean intersects(AABox2 b)
	{
		return lowerX <= b.upperX  &&  lowerY <= b.upperY   &&   upperX >= b.lowerX  &&  upperY >= b.lowerY;
	}
	
	
	public AABox2 intersection(AABox2 b)
	{
		return new AABox2( Math.max( lowerX, b.lowerX ), Math.max( lowerY, b.lowerY ),  Math.min( upperX, b.upperX ), Math.min( upperY, b.upperY ) );
	}
	
	
	public double sqrDistanceTo(Point2 p)
	{
		double dx = 0.0, dy = 0.0;

		if ( p.x < lowerX )
		{
			dx = lowerX - p.x;
		}
		else if ( p.x > upperX )
		{
			dx = p.x - upperX;
		}

		if ( p.y < lowerY )
		{
			dy = lowerY - p.y;
		}
		else if ( p.y > upperY )
		{
			dy = p.y - upperY;
		}
		
		return dx * dx  +  dy * dy;
	}
	
	
	public Point2[] closestPoints(AABox2 b)
	{
		Point2 p0 = new Point2(), p1 = new Point2();
		
		if ( lowerX > b.upperX )
		{
			// @this is to the right of @b
			p0.x = lowerX;
			p1.x = b.upperX;
		}
		else if ( upperX < b.lowerX )
		{
			// @this is to the left of @b
			p0.x = upperX;
			p1.x = b.lowerX;
		}
		else
		{
			// There is overlap in X - take the centre of the overlapping region
			p0.x = p1.x = ( Math.max( lowerX, b.lowerX ) + Math.min( upperX, b.upperX ) ) * 0.5;
		}
		
		if ( lowerY > b.upperY )
		{
			// @this is below @b
			p0.y = lowerY;
			p1.y = b.upperY;
		}
		else if ( upperY < b.lowerY )
		{
			// @this is above @b
			p0.y = upperY;
			p1.y = b.lowerY;
		}
		else
		{
			// There is overlap in Y - take the centre of the overlapping region
			p0.y = p1.y = ( Math.max( lowerY, b.lowerY ) + Math.min( upperY, b.upperY ) ) * 0.5;
		}
		
		return new Point2[] { p0, p1 };
	}
	
	
	public Point2 closestPointTo(Point2 p)
	{
		Point2 c = new Point2();
		c.x = Math.min( Math.max( p.x, lowerX ), upperX );
		c.y = Math.min( Math.max( p.y, lowerY ), upperY );
		return c;
	}
	
	
	public void addPoint(double x, double y)
	{
		if ( isEmpty() )
		{
			lowerX = upperX = x;
			lowerY = upperY = y;
		}
		else
		{
			lowerX = Math.min( lowerX, x );
			lowerY = Math.min( lowerY, y );
			upperX = Math.max( upperX, x );
			upperY = Math.max( upperY, y );
		}
	}
	
	public void addPoint(Point2 p)
	{
		if ( isEmpty() )
		{
			lowerX = upperX = p.x;
			lowerY = upperY = p.y;
		}
		else
		{
			lowerX = Math.min( lowerX, p.x );
			lowerY = Math.min( lowerY, p.y );
			upperX = Math.max( upperX, p.x );
			upperY = Math.max( upperY, p.y );
		}
	}
	
	public void addBox(AABox2 b)
	{
		if ( !b.isEmpty() )
		{
			if ( isEmpty() )
			{
				lowerX = b.lowerX;
				lowerY = b.lowerY;
				upperX = b.upperX;
				upperY = b.upperY;
			}
			else
			{
				lowerX = Math.min( lowerX, b.lowerX );
				lowerY = Math.min( lowerY, b.lowerY );
				upperX = Math.max( upperX, b.upperX );
				upperY = Math.max( upperY, b.upperY );
			}
		}
	}
	
	
	public Point2 getLower()
	{
		return new Point2( lowerX, lowerY );
	}
	
	public Point2 getUpper()
	{
		return new Point2( upperX, upperY );
	}
	
	public Point2 getCentre()
	{
		return new Point2( ( lowerX + upperX ) * 0.5, ( lowerY + upperY ) * 0.5 );
	}
	
	public Vector2 getSize()
	{
		return new Vector2( upperX - lowerX, upperY - lowerY );
	}


	public Point2 getTopLeft()
	{
		return new Point2( lowerX, lowerY );
	}

	public Point2 getTopRight()
	{
		return new Point2( upperX, lowerY );
	}
	
	public Point2 getBottomLeft()
	{
		return new Point2( lowerX, upperY );
	}

	public Point2 getBottomRight()
	{
		return new Point2( upperX, upperY );
	}


	public Point2[] getLeftEdge()
	{
		return new Point2[] { getTopLeft(), getBottomLeft() };
	}

	public Point2[] getRightEdge()
	{
		return new Point2[] { getTopRight(), getBottomRight() };
	}

	public Point2[] getTopEdge()
	{
		return new Point2[] { getTopLeft(), getTopRight() };
	}

	public Point2[] getBottomEdge()
	{
		return new Point2[] { getBottomLeft(), getBottomRight() };
	}


	public double getLowerX()
	{
		return lowerX;
	}
	
	public double getLowerY()
	{
		return lowerY;
	}

	public double getCentreX()
	{
		return ( lowerX + upperX ) * 0.5;
	}

	public double getCentreY()
	{
		return ( lowerY + upperY ) * 0.5;
	}
	
	public double getUpperX()
	{
		return upperX;
	}
	
	public double getUpperY()
	{
		return upperY;
	}
	
	
	public double getWidth()
	{
		return upperX - lowerX;
	}
	
	public double getHeight()
	{
		return upperY - lowerY;
	}
	
	
	public AABox2 offset(Vector2 o)
	{
		return new AABox2( lowerX + o.x, lowerY + o.y, upperX + o.x, upperY + o.y );
	}


	public AABox2 copy()
	{
		return new AABox2( lowerX, lowerY, upperX, upperY );
	}
	

	public boolean equals(Object other)
	{
		if ( this == other )
		{
			return true;
		}
		
		if ( other instanceof AABox2 )
		{
			AABox2 b = (AABox2)other;
			return lowerX == b.lowerX  &&  lowerY == b.lowerY  &&  upperX == b.upperX  &&  upperY == b.upperY;
		}

		return false;
	}
	
	public int hashCode()
	{
		return HashUtils.quadHash( new Double( lowerX ).hashCode(), new Double( lowerY ).hashCode(), new Double( upperX ).hashCode(), new Double( upperY ).hashCode() );
	}


	
	public PyObject __getstate__()
	{
		return new PyTuple( Py.newFloat( lowerX ), Py.newFloat( lowerY ), Py.newFloat( upperX ), Py.newFloat( upperY ) );
	}
	
	public void __setstate__(PyObject state)
	{
		if ( state instanceof PyTuple )
		{
			PyTuple tup = (PyTuple)state;
			if ( tup.size() == 4 )
			{
				lowerX = tup.pyget( 0 ).asDouble();
				lowerY = tup.pyget( 1 ).asDouble();
				upperX = tup.pyget( 2 ).asDouble();
				upperY = tup.pyget( 3 ).asDouble();
			}
			else
			{
				throw Py.TypeError( "State tuple must contain four items" );
			}
		}
		else
		{
			throw Py.TypeError( "State must be a tuple" );
		}
	}
	
	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}
	
	
	
	public PyObject __copy__()
	{
		return Py.java2py( copy() );
	}
	
	public PyObject __deepcopy__(PyDictionary memo)
	{
		return Py.java2py( copy() );
	}
	
	
	public String toString()
	{
		return "AABox2( " + lowerX + "," + lowerY + " -> " + upperX + "," + upperY + " )";
	}
}
