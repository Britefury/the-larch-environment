package Britefury.Math;

public class Vector2 implements Cloneable {
	public double x, y;
	
	
	
	public Vector2()
	{
		x = y = 0.0;
	}
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	
	public Vector2 clone()
	{
		try {
			return (Vector2)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	
	public boolean equals(Vector2 b)
	{
		return x == b.x  &&  y == b.y;
	}
	
	
	
	public Vector2 add(Vector2 b)
	{
		return new Vector2( x+b.x, y+b.y );
	}
	
	public Vector2 sub(Vector2 b)
	{
		return new Vector2( x-b.x, y-b.y );
	}
	
	public Vector2 mul(double s)
	{
		return new Vector2( x*s, y*s );
	}
	
	
	public String toString()
	{
		return "Vector2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}
}
