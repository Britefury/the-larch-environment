//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;


public class Point2 {
	public double x, y;
	
	
	
	public Point2()
	{
		x = y = 0.0;
	}
	
	public Point2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Point2(Vector2 v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	
	
	public Point2 clone()
	{
		try {
			return (Point2)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	
	public boolean equals(Point2 b)
	{
		if ( this == b )
		{
			return true;
		}
		
		return x == b.x  &&  y == b.y;
	}

	
	
	public Point2 add(Vector2 b)
	{
		return new Point2( x+b.x, y+b.y );
	}
	
	public Vector2 sub(Point2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Point2 sub(Vector2 b)
	{
		return new Point2( x-b.x, y-b.y );
	}
	
	public Point2 scale(double s)
	{
		return new Point2( x*s, y*s );
	}
	
	
	
	public Vector2 toVector2()
	{
		return new Vector2( x, y );
	}
	
	
	public static Point2 average(Point2 a, Point2 b)
	{
		return new Point2( ( a.x + b.x ) * 0.5,  ( a.y + b.y ) * 0.5 );
	}



	public String toString()
	{
		return "Point2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}
}
