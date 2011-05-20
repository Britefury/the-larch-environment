//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Math;

import java.io.Serializable;


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
	
	
	public void addPoint(double x, double y)
	{
		lowerX = Math.min( lowerX, x );
		lowerY = Math.min( lowerY, y );
		upperX = Math.max( upperX, x );
		upperY = Math.max( upperY, y );
	}
	
	public void addPoint(Point2 p)
	{
		lowerX = Math.min( lowerX, p.x );
		lowerY = Math.min( lowerY, p.y );
		upperX = Math.max( upperX, p.x );
		upperY = Math.max( upperY, p.y );
	}
	
	public void addBox(AABox2 b)
	{
		lowerX = Math.min( lowerX, b.lowerX );
		lowerY = Math.min( lowerY, b.lowerY );
		upperX = Math.max( upperX, b.upperX );
		upperY = Math.max( upperY, b.upperY );
	}
	
	
	public Point2 getLower()
	{
		return new Point2( lowerX, lowerY );
	}
	
	public Point2 getUpper()
	{
		return new Point2( upperX, upperY );
	}
	
	public Vector2 getSize()
	{
		return new Vector2( upperX - lowerX, upperY - lowerY );
	}
	
	
	public double getLowerX()
	{
		return lowerX;
	}
	
	public double getLowerY()
	{
		return lowerY;
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


	public AABox2 copy()
	{
		return new AABox2( lowerX, lowerY, upperX, upperY );
	}
	

	public String toString()
	{
		return "AABox2( " + lowerX + "," + lowerY + " -> " + upperX + "," + upperY + " )";
	}
}
