//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Vector2 implements Cloneable
{
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
		try
		{
			return (Vector2)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	
	public boolean equals(Vector2 b)
	{
		if ( this == b )
		{
			return true;
		}
		
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
	
	public double dot(Vector2 v)
	{
		return x * v.x  +  y * v.y;
	}
	
	public Vector2 negate()
	{
		return new Vector2( -x, -y );
	}
	
	
	public double sqrLength()
	{
		return dot( this );
	}
	
	public double length()
	{
		return Math.sqrt( sqrLength() );
	}
	
	
	public Vector2 getNormalised()
	{
		double oneOverLength = 1.0 / length();
		return mul( oneOverLength );
	}
	
	
	public Vector2 transform(AffineTransform affine)
	{
		Point2D.Double origin = new Point2D.Double( 0.0, 0.0 );
		Point2D.Double v = new Point2D.Double( x, y );
		affine.transform( origin, origin );
		affine.transform( v, v );
		return new Vector2( v.x - origin.x, v.y - origin.y );
	}
	
	
	public String toString()
	{
		return "Vector2(" + Double.toString( x ) + "," + Double.toString( y ) + ")";
	}
}
