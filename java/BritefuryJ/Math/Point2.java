package BritefuryJ.Math;

import BritefuryJ.Math.Vector2;

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



	public String toString()
	{
		return "Point2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}
}
